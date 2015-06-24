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

package ru.ispras.microtesk.test.sequence.engine;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.allocateModes;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.checkRootOp;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeConcreteCall;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeErrorMessage;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeInitializer;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeMode;

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
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.engine.common.AddressingModeWrapper;
import ru.ispras.microtesk.test.sequence.engine.common.TestBaseQueryCreator;
import ru.ispras.microtesk.test.sequence.iterator.SingleValueIterator;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;

/**
 * The job of the {@link DefaultEngine} class is to processes an abstract instruction call
 * sequence (uses symbolic values) and to build a concrete instruction call sequence (uses only
 * concrete values and can be simulated and used to generate source code in assembly language).
 * The {@link DefaultEngine} class performs all necessary data generation and all initializing
 * calls to the generated instruction sequence.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class DefaultEngine implements Engine<TestSequence> {
  private Set<AddressingModeWrapper> initializedModes;
  private TestSequence.Builder sequenceBuilder;

  private long codeAddress = 0;

  @Override
  public Class<TestSequence> getSolutionClass() {
    return TestSequence.class;
  }

  @Override
  public EngineResult<TestSequence> solve(
      final EngineContext engineContext, final Sequence<Call> abstractSequence) {
    checkNotNull(engineContext);
    checkNotNull(abstractSequence);

    try {
      return new EngineResult<>(EngineResult.Status.OK,
                                new SingleValueIterator<>(process(engineContext, abstractSequence)),
                                Collections.<String>emptyList());
    } catch (final ConfigurationException e) {
      return new EngineResult<>(EngineResult.Status.ERROR,
                                null,
                                Collections.singletonList(e.getMessage()));
    }
  }

  public TestSequence process(
      final EngineContext engineContext, final Sequence<Call> abstractSequence)
          throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(abstractSequence);

    Memory.setUseTempCopies(true);

    initializedModes = new HashSet<>();
    sequenceBuilder = new TestSequence.Builder();

    try {
      allocateModes(abstractSequence);

      for (final Call abstractCall : abstractSequence) {
        processAbstractCall(engineContext, abstractCall);
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

  private void processAbstractCall(final EngineContext engineContext, final Call abstractCall)
      throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(abstractCall);

    // Only executable calls are worth printing.
    if (abstractCall.isExecutable()) {
      Logger.debug("%nProcessing %s...", abstractCall.getText());

      final Primitive rootOp = abstractCall.getRootOperation();
      checkRootOp(rootOp);

      processSituations(engineContext, rootOp);
    }

    final ConcreteCall concreteCall = makeConcreteCall(engineContext, abstractCall);
    registerCall(concreteCall);
  }

  private void processSituations(final EngineContext engineContext, final Primitive primitive)
      throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(primitive);

    for (Argument arg : primitive.getArguments().values()) {
      if (Argument.Kind.OP == arg.getKind()) {
        processSituations(engineContext, (Primitive) arg.getValue());
      }
    }

    generateData(engineContext, primitive);
  }

  private TestData getDefaultTestData(
      final EngineContext engineContext,
      final Primitive primitive,
      final TestBaseQueryCreator queryCreator) {
    checkNotNull(engineContext);
    checkNotNull(primitive);
    checkNotNull(queryCreator);

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
            final IAddressingMode concreteMode = makeMode(engineContext, arg);
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
      final EngineContext engineContext,
      final Primitive primitive,
      final TestBaseQueryCreator queryCreator) {
    checkNotNull(engineContext);
    checkNotNull(primitive);
    checkNotNull(queryCreator);

    final Situation situation = primitive.getSituation();
    Logger.debug("Processing situation %s for %s...", situation, primitive.getSignature());

    if (situation == null) {
      return getDefaultTestData(engineContext, primitive, queryCreator);
    }

    final TestBaseQuery query = queryCreator.getQuery();
    Logger.debug("Query to TestBase: " + query);

    final Map<String, Argument> unknownImmediateValues = queryCreator.getUnknownImmediateValues();
    Logger.debug("Unknown immediate values: " + unknownImmediateValues.keySet());

    final Map<String, Argument> modes = queryCreator.getModes();
    Logger.debug("Modes used as arguments: " + modes);

    final TestBase testBase = engineContext.getTestBase();
    final TestBaseQueryResult queryResult = testBase.executeQuery(query);

    if (TestBaseQueryResult.Status.OK != queryResult.getStatus()) {
      Logger.warning(makeErrorMessage(queryResult) + ": default test data will be used");
      return getDefaultTestData(engineContext, primitive, queryCreator);
    }

    final java.util.Iterator<TestData> dataProvider = queryResult.getDataProvider();
    if (!dataProvider.hasNext()) {
      Logger.warning("No data was generated for the query: default test data will be used");
      return getDefaultTestData(engineContext, primitive, queryCreator);
    }

    return dataProvider.next();
  }

  private void generateData(final EngineContext engineContext, final Primitive primitive)
      throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(primitive);

    final Situation situation = primitive.getSituation();

    final TestBaseQueryCreator queryCreator =
        new TestBaseQueryCreator(engineContext, situation, primitive);

    final TestData testData = getTestData(engineContext, primitive, queryCreator);
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
      final List<Call> initializingCalls = makeInitializer(engineContext, targetMode, value);

      addCallsToPrologue(engineContext, initializingCalls);
      initializedModes.add(targetMode);
    }
  }

  private void addCallsToPrologue(final EngineContext engineContext, final List<Call> abstractCalls)
      throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(abstractCalls);

    for (Call abstractCall : abstractCalls) {
      final ConcreteCall concreteCall = makeConcreteCall(engineContext, abstractCall);
      registerPrologueCall(concreteCall);
    }
  }
}
