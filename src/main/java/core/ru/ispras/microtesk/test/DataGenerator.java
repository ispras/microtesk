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
import static ru.ispras.microtesk.utils.PrintingUtils.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.IOperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownValue;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryBuilder;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;
import ru.ispras.microtesk.translator.simnml.coverage.TestBase;

/**
 * The job of the DataGenerator class is to processes an abstract instruction call sequence (uses
 * symbolic values) and to build a concrete instruction call sequence (uses only concrete values and
 * can be simulated and used to generate source code in assembly language). The DataGenerator class
 * performs all necessary data generation and all initializing calls to the generated instruction
 * sequence.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

final class DataGenerator {
  private final IModel model;
  private final TestBase testBase;
  private final PreparatorStore preparators;

  private Set<AddressingModeWrapper> initializedModes;
  private TestSequence.Builder sequenceBuilder;

  DataGenerator(IModel model, PreparatorStore preparators) {
    checkNotNull(model);
    checkNotNull(preparators);

    this.model = model;
    this.testBase = new TestBase();
    this.preparators = preparators;
    this.sequenceBuilder = null;
  }

  private ICallFactory getCallFactory() {
    return model.getCallFactory();
  }

  public TestSequence process(Sequence<Call> abstractSequence)
      throws ConfigurationException {
    checkNotNull(abstractSequence);

    initializedModes = new HashSet<>();
    sequenceBuilder = new TestSequence.Builder();

    try {
      for (Call abstractCall : abstractSequence) {
        processAbstractCall(abstractCall);
      }
      return sequenceBuilder.build();
    } finally {
      initializedModes = null;
      sequenceBuilder = null;
    }
  }

  private void processAbstractCall(Call abstractCall) throws ConfigurationException {
    checkNotNull(abstractCall);

    if (!abstractCall.isExecutable()) {
      sequenceBuilder.add(new ConcreteCall(abstractCall));
      return;
    }

    // Only executable calls are worth printing.
    trace("%nProcessing %s...", abstractCall.getText());
    final Primitive rootOp = abstractCall.getRootOperation();
    checkRootOp(rootOp);

    processSituations(rootOp);

    final IOperation op = makeOp(rootOp);
    final InstructionCall executable = getCallFactory().newCall(op);

    sequenceBuilder.add(new ConcreteCall(abstractCall, executable));
  }

  private void processSituations(Primitive primitive) throws ConfigurationException {
    checkNotNull(primitive);

    for (Argument arg : primitive.getArguments().values()) {
      if (Argument.Kind.OP == arg.getKind()) {
        processSituations((Primitive) arg.getValue());
      }
    }

    if (primitive.hasSituation()) {
      generateData(primitive);
    }
  }

  private void generateData(Primitive primitive) throws ConfigurationException {
    if (!primitive.hasSituation()) {
      throw new IllegalArgumentException();
    }

    final Situation situation = primitive.getSituation();
    trace("Processing situation %s for %s...", situation, primitive.getSignature());

    final TestBaseQueryCreator queryCreator =
      new TestBaseQueryCreator(model.getName(), situation, primitive);

    final TestBaseQuery query = queryCreator.getQuery();
    trace("Query to TestBase: " + query);

    final Map<String, UnknownValue> unknownValues = queryCreator.getUnknownValues();
    trace("Unknown values: " + unknownValues.keySet());

    final Map<String, Primitive> modes = queryCreator.getModes();
    trace("Modes used as arguments: " + modes);

    final TestBaseQueryResult queryResult = testBase.executeQuery(query);
    if (TestBaseQueryResult.Status.OK != queryResult.getStatus()) {
      trace(makeErrorMessage(queryResult));
      return;
    }

    final TestDataProvider dataProvider = queryResult.getDataProvider();
    if (!dataProvider.hasNext()) {
      trace("No data was generated for the query.");
      return;
    }

    final TestData testData = dataProvider.next();
    trace(testData.toString());

    // Set unknown immediate values
    for (Map.Entry<String, UnknownValue> e : unknownValues.entrySet()) {
      final String name = e.getKey();
      final UnknownValue target = e.getValue();

      final Node value = testData.getBindings().get(name);
      final int intValue = FortressUtils.extractInt(value);

      target.setValue(intValue);
    }

    // Set model state using preparators that create initializing
    // sequences based on addressing modes.
    for (Map.Entry<String, Node> e : testData.getBindings().entrySet()) {
      final String name = e.getKey();

      final Primitive mode = modes.get(name);
      if (null == mode) {
        continue;
      }

      final AddressingModeWrapper targetMode = new AddressingModeWrapper(mode);
      if (initializedModes.contains(targetMode)) {
        trace("%s has already been used to set up the processor state. " +
              "No initialization code will be created.", targetMode);
        continue;
      }

      final BitVector value = FortressUtils.extractBitVector(e.getValue());
      final List<Call> initializingCalls = makeInitializer(targetMode, value);

      addCallsToPrologue(initializingCalls);
      initializedModes.add(targetMode);
    }
  }

  private void addCallsToPrologue(List<Call> abstractCalls) throws ConfigurationException {
    checkNotNull(abstractCalls);
    for (Call abstractCall : abstractCalls) {
      checkNotNull(abstractCall);
      if (abstractCall.isExecutable()) {
        final Primitive rootOp = abstractCall.getRootOperation();
        checkRootOp(rootOp);

        final IOperation op = makeOp(rootOp);
        final InstructionCall executable = getCallFactory().newCall(op);

        sequenceBuilder.addToPrologue(new ConcreteCall(abstractCall, executable));
      }
      else {
        sequenceBuilder.addToPrologue(new ConcreteCall(abstractCall));
      }
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
      sb.append("\r\n  " + error);
    }

    return sb.toString();
  }

  private List<Call> makeInitializer(AddressingModeWrapper targetMode, BitVector value) {
    trace("Creating code to assign %s to %s...", value, targetMode);

    final Preparator preparator = preparators.getPreparator(targetMode.getModePrimitive());
    if (null != preparator) {
      return preparator.makeInitializer(targetMode.getModePrimitive(), value);
    }

    trace("No suitable preparator is found for %s.", targetMode.getModePrimitive().getSignature());
    return Collections.emptyList();
  }

  private int makeImm(Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM);
    return (Integer) argument.getValue();
  }

  private int makeImmRandom(Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_RANDOM);
    return ((RandomValue) argument.getValue()).getValue();
  }

  private int makeImmUnknown(Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_UNKNOWN);
    return ((UnknownValue) argument.getValue()).getValue();
  }

  private int makeImmLazy(Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_LAZY);
    return ((LazyValue) argument.getValue()).getValue();
  }

  private IAddressingMode makeMode(Argument argument) throws ConfigurationException {
    checkArgKind(argument, Argument.Kind.MODE);

    final Primitive mode = (Primitive) argument.getValue();
    final IAddressingModeBuilder builder = getCallFactory().newMode(mode.getName());

    for (Argument arg : mode.getArguments().values()) {
      final String argName = arg.getName();
      switch (arg.getKind()) {
        case IMM:
          builder.setArgumentValue(argName, makeImm(arg));
          break;

        case IMM_RANDOM:
          builder.setArgumentValue(argName, makeImmRandom(arg));
          break;

        case IMM_UNKNOWN:
          builder.setArgumentValue(argName, makeImmUnknown(arg));
          break;

        case IMM_LAZY:
          builder.setArgumentValue(argName, makeImmLazy(arg));
          break;

        default:
          throw new IllegalArgumentException(String.format(
            "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }

    return builder.getProduct();
  }

  private IOperation makeOp(Argument argument) throws ConfigurationException {
    checkArgKind(argument, Argument.Kind.OP);

    final Primitive abstractOp = (Primitive) argument.getValue();

    return makeOp(abstractOp);
  }

  private IOperation makeOp(Primitive abstractOp) throws ConfigurationException {
    checkOp(abstractOp);

    final String name = abstractOp.getName();
    final String context = abstractOp.getContextName();

    final IOperationBuilder builder = getCallFactory().newOp(name, context);

    for (Argument arg : abstractOp.getArguments().values()) {
      final String argName = arg.getName();
      switch (arg.getKind()) {
        case IMM:
          builder.setArgument(argName, makeImm(arg));
          break;

        case IMM_RANDOM:
          builder.setArgument(argName, makeImmRandom(arg));
          break;

        case IMM_UNKNOWN:
          builder.setArgument(argName, makeImmUnknown(arg));
          break;

        case IMM_LAZY:
          builder.setArgument(argName, makeImmLazy(arg));
          break;

        case MODE:
          builder.setArgument(argName, makeMode(arg));
          break;

        case OP:
          builder.setArgument(argName, makeOp(arg));
          break;

        default:
          throw new IllegalArgumentException(String.format(
            "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }

    return builder.build();
  }

  private static void checkOp(Primitive op) {
    if (Primitive.Kind.OP != op.getKind()) {
      throw new IllegalArgumentException(String.format(
        "%s is not an operation.", op.getName()));
    }
  }

  private static void checkRootOp(Primitive op) {
    checkOp(op);
    if (!op.isRoot()) {
      throw new IllegalArgumentException(String.format(
        "%s is not a root operation!", op.getName()));
    }
  }

  private static void checkArgKind(Argument arg, Argument.Kind expected) {
    if (arg.getKind() != expected) {
      throw new IllegalArgumentException(String.format(
        "Argument %s has kind %s while %s is expected.", arg.getName(), arg.getKind(), expected));
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
 * {@link DataType#INTEGER}.</li>
 * <li>All unknown immediate arguments (see {@link UnknownValue}) that have not been assigned values
 * are unknown variables (see {@link NodeVariable}) of type {@link DataType#INTEGER}.</li>
 * <li>All addressing modes are unknown variables (see {@link NodeVariable}) of type
 * {@link DataType#UNKNOWN}.</li>
 * </ul>
 * <p>
 * N.B. If nested operations have linked test situations, these situations are ignored and no
 * information about them is included in the query. These situations are processed separately. If
 * they have been previously processed, unknown immediate arguments that received values are treated
 * as known immediate values.
 * <p>
 * N.B. The above text describes the current behavior that may be changed in the future.
 * 
 * @author Andrei Tatarnikov
 */

final class TestBaseQueryCreator {
  private final String processor;
  private final Situation situation;
  private final Primitive primitive;

  private boolean isCreated;
  private TestBaseQuery query;
  private Map<String, UnknownValue> unknownValues;
  private Map<String, Primitive> modes;

  public TestBaseQueryCreator(String processor, Situation situation, Primitive primitive) {
    checkNotNull(processor);
    checkNotNull(situation);
    checkNotNull(primitive);

    this.processor = processor;
    this.situation = situation;
    this.primitive = primitive;

    this.isCreated = false;
    this.query = null;
    this.unknownValues = null;
    this.modes = null;
  }

  public TestBaseQuery getQuery() {
    createQuery();

    checkNotNull(query);
    return query;
  }

  public Map<String, UnknownValue> getUnknownValues() {
    createQuery();

    checkNotNull(unknownValues);
    return unknownValues;
  }

  public Map<String, Primitive> getModes() {
    createQuery();

    checkNotNull(modes);
    return modes;
  }

  private void createQuery() {
    if (isCreated) {
      return;
    }

    final TestBaseQueryBuilder queryBuilder = new TestBaseQueryBuilder();

    createContext(queryBuilder);
    createParameters(queryBuilder);

    final BindingBuilder bindingBuilder = new BindingBuilder(queryBuilder, primitive);

    unknownValues = bindingBuilder.getUnknownValues();
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

  private static void acquireContext(TestBaseQueryBuilder builder, String prefix, Primitive p) {
    for (Argument arg : p.getArguments().values()) {
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

  private void createParameters(TestBaseQueryBuilder queryBuilder) {
    for (Map.Entry<String, Object> attrEntry : situation.getAttributes().entrySet()) {
      queryBuilder.setParameter(attrEntry.getKey(), attrEntry.getValue());
    }
  }

  private static final class BindingBuilder {
    private final TestBaseQueryBuilder queryBuilder;
    private final Map<String, UnknownValue> unknownValues;
    private final Map<String, Primitive> modes;

    private BindingBuilder(TestBaseQueryBuilder queryBuilder, Primitive primitive) {
      checkNotNull(queryBuilder);
      checkNotNull(primitive);

      this.queryBuilder = queryBuilder;
      this.unknownValues = new HashMap<String, UnknownValue>();
      this.modes = new HashMap<String, Primitive>();

      visit(primitive.getName(), primitive);
    }

    public Map<String, UnknownValue> getUnknownValues() {
      return unknownValues;
    }

    public Map<String, Primitive> getModes() {
      return modes;
    }

    private void visit(String prefix, Primitive p) {
      for (Argument arg : p.getArguments().values()) {
        final String argName = prefix.isEmpty() ?
          arg.getName() : String.format("%s.%s", prefix, arg.getName());

        switch (arg.getKind()) {
          case IMM:
            queryBuilder.setBinding(argName, NodeValue.newInteger((Integer) arg.getValue()));
            break;

          case IMM_RANDOM:
            queryBuilder.setBinding(argName,
              NodeValue.newInteger(((RandomValue) arg.getValue()).getValue()));
            break;

          case IMM_UNKNOWN:
            if (!((UnknownValue) arg.getValue()).isValueSet()) {
              queryBuilder.setBinding(argName,
                new NodeVariable(new Variable(argName, DataType.INTEGER)));
              unknownValues.put(argName, (UnknownValue) arg.getValue());
            } else {
              queryBuilder.setBinding(argName,
                NodeValue.newInteger(((UnknownValue) arg.getValue()).getValue()));
            }
            break;

          case MODE:
            queryBuilder.setBinding(argName,
              new NodeVariable(new Variable(argName, DataType.UNKNOWN)));
            modes.put(argName, (Primitive) arg.getValue());
            visit(argName, (Primitive) arg.getValue());
            break;

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
    for (Argument arg : mode.getArguments().values()) {
      result = prime * result + arg.getName().hashCode();
      result = prime * result + arg.getImmediateValue();
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

    final Iterator<Argument> thisIt = mode.getArguments().values().iterator();
    final Iterator<Argument> otherIt = otherMode.getArguments().values().iterator();

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
