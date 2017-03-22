/*
 * Copyright 2013-2017 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.InstructionCall;
import ru.ispras.microtesk.model.api.IsaPrimitive;
import ru.ispras.microtesk.model.api.memory.LocationAccessor;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.Code;
import ru.ispras.microtesk.test.CodeAllocator;
import ru.ispras.microtesk.test.Executor;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.utils.AddressingModeWrapper;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

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
  @Override
  public Class<TestSequence> getSolutionClass() {
    return TestSequence.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public void onStartProgram() {
    // Empty
  }

  @Override
  public void onEndProgram() {
    // Empty
  }

  @Override
  public EngineResult<TestSequence> solve(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final boolean isDebug = Logger.isDebug();
    Logger.setDebug(engineContext.getOptions().getValueAsBoolean(Option.DEBUG));

    try {
      final TestSequence testSequence = processSequence(engineContext, abstractSequence);
      return new EngineResult<>(new SingleValueIterator<>(testSequence));
    } catch (final ConfigurationException e) {
      return new EngineResult<>(e.getMessage());
    } finally {
      Logger.setDebug(isDebug);
    }
  }

  private static TestSequence processSequence(
      final EngineContext engineContext,
      final List<Call> abstractSequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final int sequenceIndex =
        engineContext.getStatistics().getSequences();

    final List<ConcreteCall> concreteSequence =
        EngineUtils.makeConcreteCalls(engineContext, abstractSequence);

    final TestSequenceCreator creator = 
        new TestSequenceCreator(sequenceIndex, abstractSequence, concreteSequence);

    execute(
        engineContext,
        creator,
        engineContext.getCodeAllocationAddress(),
        concreteSequence,
        sequenceIndex
        );

    return creator.createTestSequence();
  }

  private static void execute(
      final EngineContext engineContext,
      final ExecutorListener listener,
      final long allocationAddress,
      final List<ConcreteCall> sequence,
      final int sequenceIndex) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(listener);
    InvariantChecks.checkNotNull(sequence);

    if (sequence.isEmpty()) {
      listener.setAllocationAddress(allocationAddress);
      return;
    }

    final LabelManager labelManager = new LabelManager(engineContext.getLabelManager());
    allocateData(engineContext, labelManager, sequence, sequenceIndex);

    final CodeAllocator codeAllocator =
        new CodeAllocator(engineContext.getModel(), labelManager, allocationAddress);

    codeAllocator.init();
    codeAllocator.allocateCalls(sequence, sequenceIndex);

    final Executor executor = new Executor(engineContext, true);
    executor.setListener(listener);

    final ConcreteCall first = sequence.get(0);
    final ConcreteCall last = sequence.get(sequence.size() - 1);

    final long startAddress = first.getAddress();
    final long endAddress = last.getAddress() + last.getByteSize();

    listener.setAllocationAddress(endAddress);
    final Code code = codeAllocator.getCode();

    long address = startAddress;
    do {
      listener.resetLastExecutedCall();
      final Executor.Status status = executor.execute(code, address);

      final boolean isValidAddress =
          status.isAddress() &&
          (code.hasAddress(status.getAddress()) || status.getAddress() == endAddress);

      if (isValidAddress) {
        address = status.getAddress();
      } else {
        if (Logger.isDebug()) {
          Logger.debug(
              "Jump to %s %s located outside of the sequence.",
              status.isAddress() ? "address" : "label",
              status
              );
        }

        final ConcreteCall lastExecutedCall = listener.getLastExecutedCall();
        final long nextAddress = lastExecutedCall.getAddress() + lastExecutedCall.getByteSize();

        if (Logger.isDebug()) {
          Logger.debug(
              "Execution will be continued from the next instruction 0x%016x.",
              nextAddress
              );
        }

        address = nextAddress;
      }
    } while (address != endAddress);
  }

  private static void allocateData(
      final EngineContext engineContext,
      final LabelManager labelManager,
      final List<ConcreteCall> sequence,
      final int sequenceIndex) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(labelManager);
    InvariantChecks.checkNotNull(sequence);

    for (final ConcreteCall call : sequence) {
      if (call.getData() != null) {
        final DataSection data = call.getData();
        data.setSequenceIndex(sequenceIndex);
        data.allocate(engineContext.getModel().getMemoryAllocator());
        data.registerLabels(labelManager);
      }
    }
  }

  private static class ExecutorListener implements Executor.Listener {
    private ConcreteCall lastExecutedCall = null;
    private long allocationAddress = 0;

    @Override
    public void onBeforeExecute(final EngineContext context, final ConcreteCall concreteCall) {
      // Empty
    }

    @Override
    public void onAfterExecute(final EngineContext context, final ConcreteCall concreteCall) {
      lastExecutedCall = concreteCall;
    }

    public final ConcreteCall getLastExecutedCall() {
      return lastExecutedCall;
    }

    public final void resetLastExecutedCall() {
      lastExecutedCall = null;
    }

    public final long getAllocationAddress() {
      return allocationAddress;
    }

    public final void setAllocationAddress(final long value) {
      allocationAddress = value;
    }
  }

  private static final class TestSequenceCreator extends ExecutorListener {
    private final int sequenceIndex;
    private final Map<ConcreteCall, Call> callMap;
    private final Set<AddressingModeWrapper> initializedModes;
    private final ExecutorListener listenerForInitializers;
    private final TestSequence.Builder testSequenceBuilder;

    private TestSequenceCreator(
        final int sequenceIndex,
        final List<Call> abstractSequence,
        final List<ConcreteCall> concreteSequence) {
      InvariantChecks.checkNotNull(abstractSequence);
      InvariantChecks.checkNotNull(concreteSequence);
      InvariantChecks.checkTrue(abstractSequence.size() == concreteSequence.size());

      this.sequenceIndex = sequenceIndex;
      this.callMap = new IdentityHashMap<>();
      this.initializedModes = new HashSet<>();
      this.listenerForInitializers = new ExecutorListener();

      for (int index = 0; index < abstractSequence.size(); ++index) {
        final Call abstractCall = abstractSequence.get(index);
        final ConcreteCall concreteCall = concreteSequence.get(index);

        InvariantChecks.checkNotNull(abstractCall);
        InvariantChecks.checkNotNull(concreteCall);

        callMap.put(concreteCall, abstractCall);
      }

      this.testSequenceBuilder = new TestSequence.Builder();
      this.testSequenceBuilder.add(concreteSequence);
    }

    public TestSequence createTestSequence() {
      return testSequenceBuilder.build();
    }

    @Override
    public void onBeforeExecute(
        final EngineContext engineContext,
        final ConcreteCall concreteCall) {
      InvariantChecks.checkNotNull(concreteCall);
      InvariantChecks.checkNotNull(engineContext);

      final Call abstractCall = callMap.get(concreteCall);
      if (null == abstractCall) {
        return; // Already processed
      }

      try {
        processCall(engineContext, abstractCall, concreteCall);
      } catch (final ConfigurationException e) {
        throw new GenerationAbortedException(
            "Failed to generate test data for " + concreteCall.getText(), e);
      } finally {
        callMap.put(concreteCall, null);
      }
    }

    private void processCall(
        final EngineContext engineContext,
        final Call abstractCall,
        final ConcreteCall concreteCall) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(abstractCall);
      InvariantChecks.checkNotNull(concreteCall);

      // Not executable calls do not need test data
      if (!abstractCall.isExecutable()) {
        return;
      }

      if (Logger.isDebug()) {
        Logger.debug("%nGenerating test data for %s...", concreteCall.getText());
      }

      final Primitive abstractPrimitive = abstractCall.getRootOperation();
      EngineUtils.checkRootOp(abstractPrimitive);

      final InstructionCall instructionCall = concreteCall.getExecutable();
      InvariantChecks.checkNotNull(instructionCall);

      final IsaPrimitive concretePrimitive = instructionCall.getRootPrimitive();
      InvariantChecks.checkNotNull(concretePrimitive);

      processPrimitive(engineContext, abstractPrimitive, concretePrimitive);
    }

    private void processPrimitive(
        final EngineContext engineContext,
        final Primitive abstractPrimitive,
        final IsaPrimitive concretePrimitive) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(abstractPrimitive);
      InvariantChecks.checkNotNull(concretePrimitive);

      // Unrolls shortcuts to establish correspondence between abstract and concrete primitives
      final IsaPrimitive fixedConcretePrimitive =
          findConcretePrimitive(abstractPrimitive, concretePrimitive);

      InvariantChecks.checkNotNull(
          fixedConcretePrimitive, abstractPrimitive.getName() + " not found.");

      for (final Argument argument : abstractPrimitive.getArguments().values()) {
        if (Argument.Kind.OP == argument.getKind()) {
          final String argumentName = argument.getName();
          final Primitive abstractArgument = (Primitive) argument.getValue();

          final IsaPrimitive concreteArgument =
              fixedConcretePrimitive.getArguments().get(argumentName);

          processPrimitive(engineContext, abstractArgument, concreteArgument);
        }
      }

      final List<Call> initializer = EngineUtils.makeInitializer(
          engineContext,
          abstractPrimitive,
          abstractPrimitive.getSituation(),
          initializedModes,
          fixedConcretePrimitive
          );

      processInitializer(engineContext, initializer);
    }

    private static IsaPrimitive findConcretePrimitive(
        final Primitive abstractPrimitive,
        final IsaPrimitive concretePrimitive) {
      InvariantChecks.checkNotNull(abstractPrimitive);
      InvariantChecks.checkNotNull(concretePrimitive);

      if (abstractPrimitive.getName().equals(concretePrimitive.getName())) {
        return concretePrimitive;
      }

      for (final IsaPrimitive concreteArgument : concretePrimitive.getArguments().values()) {
        final IsaPrimitive result = findConcretePrimitive(abstractPrimitive, concreteArgument);
        if (null != result) {
          return result;
        }
      }

      return null;
    }

    private void processInitializer(
        final EngineContext engineContext,
        final List<Call> abstractCalls) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(abstractCalls);

      final List<ConcreteCall> concreteCalls =
          EngineUtils.makeConcreteCalls(engineContext, abstractCalls);

      testSequenceBuilder.addToPrologue(concreteCalls);

      final LocationAccessor programCounter = engineContext.getModel().getPE().accessLocation("PC");
      final BigInteger programCounterValue = programCounter.getValue();

      if (!concreteCalls.isEmpty()) {
        Logger.debug("Executing initializing code...");
      }

      try {
        execute(
            engineContext,
            listenerForInitializers,
            getAllocationAddress(),
            concreteCalls,
            sequenceIndex
            );
      } finally {
        programCounter.setValue(programCounterValue);
        setAllocationAddress(listenerForInitializers.getAllocationAddress());
        Logger.debug("");
      }
    }
  }
}
