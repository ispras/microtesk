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
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.makeErrorMessage;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.makeInitializer;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.makeMode;
import static ru.ispras.microtesk.test.TestDataGeneratorUtils.newTestBase;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
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
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestBaseQuery;
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
        new TestBaseQueryCreator(model, situation, primitive);

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
}
