/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.checkRootOp;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.makeConcreteCall;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.makeInitializer;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.makeMode;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.newTestBase;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.data.ModeAllocator;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.SingleValueIterator;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryBuilder;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;

/**
 * The job of the {@link TestDataGenerator} class is to processes an abstract instruction call
 * sequence (uses symbolic values) and to build a concrete instruction call sequence (uses only
 * concrete values and can be simulated and used to generate source code in assembly language).
 * The {@link TestDataGenerator} class performs all necessary data generation and all initializing
 * calls to the generated instruction sequence.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

final class TestSequenceAdapter implements Adapter<TestSequence> {
  @Override
  public Class<TestSequence> getSolutionClass() {
    return TestSequence.class;
  }

  @Override
  public TestSequence adapt(final Sequence<Call> abstractSequence, final TestSequence solution) {
    return solution;
  }
}

final class TestDataGenerator implements Solver<TestSequence> {
  private final IModel model;
  private final TestBase testBase;
  private final PreparatorStore preparators;

  private Set<AddressingModeWrapper> initializedModes;
  private TestSequence.Builder sequenceBuilder;

  private long codeAddress = 0;

  TestDataGenerator(
      final IModel model,
      final PreparatorStore preparators,
      final GeneratorSettings settings) {
    checkNotNull(model);
    checkNotNull(preparators);

    this.model = model;
    this.testBase = newTestBase(settings);
    this.preparators = preparators;
    this.sequenceBuilder = null;
  }

  private ICallFactory getCallFactory() {
    return model.getCallFactory();
  }

  @Override
  public Class<TestSequence> getSolutionClass() {
    return TestSequence.class;
  }

  @Override
  public SolverResult<TestSequence> solve(final Sequence<Call> abstractSequence) {
    try {
      return new SolverResult<>(SolverResult.Status.OK,
                                new SingleValueIterator<>(process(abstractSequence)),
                                Collections.<String>emptyList());
    } catch (final ConfigurationException e) {
      return new SolverResult<>(SolverResult.Status.ERROR,
                                null,
                                Collections.singletonList(e.getMessage()));
    }
  }

  public TestSequence process(final Sequence<Call> abstractSequence)
      throws ConfigurationException {
    checkNotNull(abstractSequence);

    Memory.setUseTempCopies(true);

    initializedModes = new HashSet<>();
    sequenceBuilder = new TestSequence.Builder();

    try {
      // Allocate addressing modes.
      final ModeAllocator modeAllocator = ModeAllocator.get();
      if (null != modeAllocator) {
        modeAllocator.allocate(abstractSequence);
      }

      for (final Call abstractCall : abstractSequence) {
        processAbstractCall(abstractCall);
      }

      final TestSequence sequence = sequenceBuilder.build();

      sequence.setAddress(codeAddress);
      codeAddress += sequence.getByteSize();

      return sequence;
    } finally {
      initializedModes = null;
      sequenceBuilder = null;
    }
  }

  private void registerCall(final ConcreteCall call) {
    checkNotNull(call);

    call.execute();
    sequenceBuilder.add(call);
  }

  private void registerPrologueCall(final ConcreteCall call) {
    checkNotNull(call);

    call.execute();
    sequenceBuilder.addToPrologue(call);
  }

  private void processAbstractCall(final Call abstractCall) throws ConfigurationException {
    checkNotNull(abstractCall);

    // Only executable calls are worth printing.
    if (abstractCall.isExecutable()) {
      Logger.debug("%nProcessing %s...", abstractCall.getText());

      final Primitive rootOp = abstractCall.getRootOperation();
      checkRootOp(rootOp);

      processSituations(rootOp);
    }

    final ConcreteCall concreteCall = makeConcreteCall(abstractCall, getCallFactory());
    registerCall(concreteCall);
  }

  private void processSituations(final Primitive primitive) throws ConfigurationException {
    checkNotNull(primitive);

    for (Argument arg : primitive.getArguments().values()) {
      if (Argument.Kind.OP == arg.getKind()) {
        processSituations((Primitive) arg.getValue());
      }
    }

    generateData(primitive);
  }

  private TestData getDefaultTestData(
      final Primitive primitive, final TestBaseQueryCreator queryCreator) {
    final Map<String, Argument> args = new HashMap<>();
    args.putAll(queryCreator.getUnknownImmediateValues());
    args.putAll(queryCreator.getModes());

    final Map<String, Node> bindings = new HashMap<>();
    for (final Map.Entry<String, Argument> entry : args.entrySet()) {
      final String name = entry.getKey();
      final Argument arg = entry.getValue();

      if (arg.getMode() == ArgumentMode.IN || arg.getMode() == ArgumentMode.INOUT) {
        if (arg.getKind() == Argument.Kind.MODE) {
          try {
            final IAddressingMode concreteMode = makeMode(arg, getCallFactory());
            if (concreteMode.access().isInitialized()) {
              continue;
            }
          } catch (ConfigurationException e) {
            Logger.error(e.getMessage());
          }
        }

        final BitVector value = BitVector.newEmpty(arg.getType().getBitSize());
        Randomizer.get().fill(value);

        bindings.put(name, NodeValue.newBitVector(value));
      }
    }

    return new TestData(bindings);
  }

  private TestData getTestData(
    final Primitive primitive, final TestBaseQueryCreator queryCreator) {
    final Situation situation = primitive.getSituation();
    Logger.debug("Processing situation %s for %s...", situation, primitive.getSignature());

    if (situation == null) {
      return getDefaultTestData(primitive, queryCreator);
    }

    final TestBaseQuery query = queryCreator.getQuery();
    Logger.debug("Query to TestBase: " + query);

    final Map<String, Argument> unknownImmediateValues = queryCreator.getUnknownImmediateValues();
    Logger.debug("Unknown immediate values: " + unknownImmediateValues.keySet());

    final Map<String, Argument> modes = queryCreator.getModes();
    Logger.debug("Modes used as arguments: " + modes);

    final TestBaseQueryResult queryResult = testBase.executeQuery(query);
    if (TestBaseQueryResult.Status.OK != queryResult.getStatus()) {
      Logger.warning(makeErrorMessage(queryResult) + ": default test data will be used");
      return getDefaultTestData(primitive, queryCreator);
    }

    final java.util.Iterator<TestData> dataProvider = queryResult.getDataProvider();
    if (!dataProvider.hasNext()) {
      Logger.warning("No data was generated for the query: default test data will be used");
      return getDefaultTestData(primitive, queryCreator);
    }

    return dataProvider.next();
  }

  private void generateData(Primitive primitive) throws ConfigurationException {
    final Situation situation = primitive.getSituation();

    final TestBaseQueryCreator queryCreator =
        new TestBaseQueryCreator(model.getName(), situation, primitive);

    final TestData testData = getTestData(primitive, queryCreator);
    Logger.debug(testData.toString());

    // Set unknown immediate values
    for (final Map.Entry<String, Argument> e : queryCreator.getUnknownImmediateValues().entrySet()) {
      final String name = e.getKey();
      final Argument arg = e.getValue();
      final UnknownImmediateValue target = (UnknownImmediateValue) arg.getValue();

      final Node value = testData.getBindings().get(name);
      if (value.getKind() != Node.Kind.VALUE) {
        throw new IllegalStateException(String.format("%s is not a constant value.", value));
      }

      final Data data = ((NodeValue) value).getData();
      if (data.isType(DataTypeId.LOGIC_INTEGER)) {
        target.setValue(data.getInteger());
      } else if (data.isType(DataTypeId.BIT_VECTOR)) {
        target.setValue(data.getBitVector().bigIntegerValue());
      } else {
        throw new IllegalStateException(String.format(
            "%s cannot be converted to integer", value));
      }
    }

    // Set model state using preparators that create initializing
    // sequences based on addressing modes.
    for (final Map.Entry<String, Node> e : testData.getBindings().entrySet()) {
      final String name = e.getKey();

      final Argument arg = queryCreator.getModes().get(name);

      if (null == arg) {
        continue;
      }

      // No point to assign output variables even if values for them are provided.
      // We do not want extra code and conflicts when same registers are used
      // as input and output (see Bug #6057)
      if (arg.getMode() == ArgumentMode.OUT) {
        continue;
      }

      final Primitive mode = (Primitive) arg.getValue();

      final AddressingModeWrapper targetMode = new AddressingModeWrapper(mode);
      if (initializedModes.contains(targetMode)) {
        Logger.debug("%s has already been used to set up the processor state. " +
              "No initialization code will be created.", targetMode);
        continue;
      }

      final BitVector value = FortressUtils.extractBitVector(e.getValue());
      final List<Call> initializingCalls = makeInitializer(targetMode, value, preparators);

      addCallsToPrologue(initializingCalls);
      initializedModes.add(targetMode);
    }
  }

  private void addCallsToPrologue(List<Call> abstractCalls) throws ConfigurationException {
    checkNotNull(abstractCalls);
    for (Call abstractCall : abstractCalls) {
      final ConcreteCall concreteCall = makeConcreteCall(abstractCall, getCallFactory());
      registerPrologueCall(concreteCall);
    }
  }

  private String makeErrorMessage(TestBaseQueryResult queryResult) {
    final StringBuilder sb = new StringBuilder(String.format(
      "Failed to execute the query. Status: %s.", queryResult.getStatus()));

    if (!queryResult.hasErrors()) {
      return sb.toString();
    }
    sb.append(" Errors: ");
    for (String error : queryResult.getErrors()) {
      sb.append(System.lineSeparator() + "  " + error);
    }

    return sb.toString();
  }

  private static void acquireContext(
      final TestBaseQueryBuilder builder,
      final String prefix,
      final Primitive p) {
    for (final Argument arg : p.getArguments().values()) {
      final String ctxArgName = (prefix.isEmpty())
                                ? arg.getName()
                                : prefix + "." + arg.getName();
      builder.setContextAttribute(ctxArgName, arg.getTypeName());
      switch (arg.getKind()) {
      case OP:
      case MODE:
        acquireContext(builder, ctxArgName, (Primitive) arg.getValue());
        break;

      default:
      }
    }
  }

  /**
   * The TestBaseQueryCreator class forms a query for test data that will be sent to TestBase. It
   * dumps the following information:
   * <ol>
   * <li>Name of the microprocessor being tested.</li>
   * <li>Information about the test situation (its name and attributes).</li>
   * <li>Name of the operation the situation is linked to.</li>
   * <li>All arguments of the operation including immediate values, addressing modes with their
   * arguments and all arguments of nested operations.</li>
   * </ol>
   * Arguments are treated in the following way:
   * <ul>
   * <li>All immediate arguments that have values are constants (see {@link NodeValue}) of type
   * {@link DataType#BIT_VECTOR}.</li>
   * <li>All unknown immediate arguments (see {@link UnknownImmediateValue}) that have not been 
   * assigned values are unknown variables (see {@link NodeVariable}) of type
   * {@link DataType#BIT_VECTOR}.</li>
   * <li>All addressing modes are unknown variables (see {@link NodeVariable}) of type
   * {@link DataType#BIT_VECTOR}.</li>
   * </ul>
   * <p>
   * N.B. If nested operations have linked test situations, these situations are ignored and no
   * information about them is included in the query. These situations are processed separately. If
   * they have been previously processed, unknown immediate arguments that received values are treated
   * as known immediate values.
   * <p>
   * N.B. The above text describes the current behavior that may be changed in the future.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */

  private final class TestBaseQueryCreator {
    private final String processor;
    private final Situation situation;
    private final Primitive primitive;

    private boolean isCreated;
    private TestBaseQuery query;
    private Map<String, Argument> unknownImmediateValues;
    private Map<String, Argument> modes;

    public TestBaseQueryCreator(
        final String processor,
        final Situation situation,
        final Primitive primitive) {
      checkNotNull(processor);
      checkNotNull(primitive);

      this.processor = processor;
      this.situation = situation;
      this.primitive = primitive;

      this.isCreated = false;
      this.query = null;
      this.unknownImmediateValues = null;
      this.modes = null;
    }

    // TODO: can return null.
    public TestBaseQuery getQuery() {
      createQuery();
      return query;
    }

    public Map<String, Argument> getUnknownImmediateValues() {
      createQuery();

      checkNotNull(unknownImmediateValues);
      return unknownImmediateValues;
    }

    public Map<String, Argument> getModes() {
      createQuery();

      checkNotNull(modes);
      return modes;
    }

    private void createQuery() {
      if (isCreated) {
        return;
      }

      final TestBaseQueryBuilder queryBuilder = new TestBaseQueryBuilder();

      if (situation != null) {
        createContext(queryBuilder);
        createParameters(queryBuilder);
      }

      final BindingBuilder bindingBuilder = new BindingBuilder(queryBuilder, primitive);

      unknownImmediateValues = bindingBuilder.getUnknownValues();
      modes = bindingBuilder.getModes();

      query = queryBuilder.build();

      isCreated = true;
    }

    private void createContext(TestBaseQueryBuilder queryBuilder) {
      queryBuilder.setContextAttribute(TestBaseContext.PROCESSOR, processor);
      queryBuilder.setContextAttribute(TestBaseContext.INSTRUCTION, primitive.getName());
      queryBuilder.setContextAttribute(TestBaseContext.TESTCASE, situation.getName());

      queryBuilder.setContextAttribute(primitive.getName(), primitive.getName());
      acquireContext(queryBuilder, primitive.getName(), primitive);
    }

    private void createParameters(final TestBaseQueryBuilder queryBuilder) {
      for (Map.Entry<String, Object> attrEntry : situation.getAttributes().entrySet()) {
        queryBuilder.setParameter(attrEntry.getKey(), attrEntry.getValue());
      }
    }
  }

  private final class BindingBuilder {
    private final TestBaseQueryBuilder queryBuilder;
    private final Map<String, Argument> unknownValues;
    private final Map<String, Argument> modes;

    private BindingBuilder(
        final TestBaseQueryBuilder queryBuilder,
        final Primitive primitive) {
      checkNotNull(queryBuilder);
      checkNotNull(primitive);

      this.queryBuilder = queryBuilder;
      this.unknownValues = new HashMap<String, Argument>();
      this.modes = new HashMap<String, Argument>();

      visit(primitive.getName(), primitive);
    }

    public Map<String, Argument> getUnknownValues() {
      return unknownValues;
    }

    public Map<String, Argument> getModes() {
      return modes;
    }

    private void visit(final String prefix, final Primitive p) {
      for (final Argument arg : p.getArguments().values()) {
        final String argName = prefix.isEmpty() ?
            arg.getName() : String.format("%s.%s", prefix, arg.getName());

        switch (arg.getKind()) {
          case IMM:
            queryBuilder.setBinding(
                argName,
                new NodeValue(Data.newBitVector(BitVector.valueOf(
                    (BigInteger) arg.getValue(), arg.getType().getBitSize())))
                );
            break;

          case IMM_RANDOM:
            queryBuilder.setBinding(
                argName, 
                new NodeValue(Data.newBitVector(BitVector.valueOf(
                    ((RandomValue) arg.getValue()).getValue(), arg.getType().getBitSize())))
                );
            break;

          case IMM_UNKNOWN:
            final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();

            if (!unknownValue.isValueSet()) {
              queryBuilder.setBinding(
                  argName,
                  new NodeVariable(argName, DataType.BIT_VECTOR(arg.getType().getBitSize()))
                  );

              unknownValues.put(argName, arg);
            } else {
              queryBuilder.setBinding(
                  argName,
                  new NodeValue(Data.newBitVector(BitVector.valueOf(
                      unknownValue.getValue(), arg.getType().getBitSize())))
                  );
            }
            break;

          case MODE: {
            // The mode's arguments should be processed before processing the mode.
            // Otherwise, if there are unknown values, the mode cannot be instantiated.
            visit(argName, (Primitive) arg.getValue());

            // If a MODE has no return expression it is treated as OP and
            // it is NOT added to bindings and mode list
            if (arg.getMode() != ArgumentMode.NA) {
              final DataType dataType = DataType.BIT_VECTOR(arg.getType().getBitSize());
              Node bindingValue = null;

              try {
                if (arg.getMode() != ArgumentMode.NA) {
                  final IAddressingMode mode = makeMode(arg, getCallFactory());
                  final Location location = mode.access();

                  if (location.isInitialized()) {
                    bindingValue = NodeValue.newBitVector(
                        BitVector.valueOf(location.getValue(), location.getBitSize()));
                  } else {
                    bindingValue = new NodeVariable(argName, dataType);
                  }
                } else {
                  bindingValue = new NodeVariable(argName, dataType);
                }
              } catch (ConfigurationException e) {
                Logger.error("Failed to read data from %s. Reason: %s",
                    arg.getTypeName(), e.getMessage());

                bindingValue = new NodeVariable(argName, dataType);
              }

              queryBuilder.setBinding(argName, bindingValue);
              modes.put(argName, arg);
            }

            break;
          }

          case OP:
            visit(argName, (Primitive) arg.getValue());
            break;

          default:
            throw new IllegalArgumentException(String.format(
              "Illegal kind of argument %s: %s.", argName, arg.getKind()));
        }
      }
    }
  }
}

/**
 * Wrapper class for addressing mode primitives that allows checking equality and calculating
 * hash code. This is needed to avoid initializations of the same resources that would overwrite
 * each other. 
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

final class AddressingModeWrapper {
  private final Primitive mode;

  public AddressingModeWrapper(Primitive mode) {
    checkNotNull(mode);
    if (mode.getKind() != Primitive.Kind.MODE) {
      throw new IllegalArgumentException(mode.getSignature() + " is not an addresing mode.");
    }
    this.mode = mode;
  }

  public Primitive getModePrimitive() {
    return mode;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (Argument arg : mode.getArguments().values()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(arg.getName() + ": " + arg.getTypeName());
      sb.append(" = " + arg.getImmediateValue());
    }

    return String.format("%s %s(%s)", mode.getKind().getText(), mode.getName(), sb);
  }

  @Override
  public int hashCode() {
    final int prime = 31;

    int result = prime + mode.getName().hashCode();
    for (final Argument arg : mode.getArguments().values()) {
      result = prime * result + arg.getName().hashCode();
      result = prime * result + arg.getImmediateValue().hashCode();
    }

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final AddressingModeWrapper other = (AddressingModeWrapper) obj;
    final Primitive otherMode = other.mode;

    if (!mode.getName().equals(otherMode.getName())) {
      return false;
    }

    if (mode.getArguments().size() != otherMode.getArguments().size()) {
      return false;
    }

    final java.util.Iterator<Argument> thisIt = mode.getArguments().values().iterator();
    final java.util.Iterator<Argument> otherIt = otherMode.getArguments().values().iterator();

    while (thisIt.hasNext() && otherIt.hasNext()) {
      final Argument thisArg = thisIt.next();
      final Argument otherArg = otherIt.next();

      if (!thisArg.getName().equals(otherArg.getName())) {
        return false;
      }

      if (thisArg.getImmediateValue() != otherArg.getImmediateValue()) {
        return false;
      }
    }

    return true;
  }
}
