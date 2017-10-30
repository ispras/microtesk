/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.InstructionCall;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.memory.LocationAccessor;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.Code;
import ru.ispras.microtesk.test.CodeAllocator;
import ru.ispras.microtesk.test.ConcreteSequence;
import ru.ispras.microtesk.test.Executor;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.Printer;
import ru.ispras.microtesk.test.SelfCheck;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link SequenceConcretizer} class processes abstract instruction sequences to
 * construct concrete instruction sequences. This includes processing of test situations
 * to generate data and constructing initialization code. Test situations are processed in
 * the order of their execution, which involves simulation on a temporary context (presimulation).
 * Presimulation and test situation processing can be disabled. In this case, the abstract sequence
 * will be turned into a concrete sequence without data generation.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class SequenceConcretizer implements Iterator<ConcreteSequence>{
  private final EngineContext engineContext;
  private final boolean isPresimulation;
  private final Iterator<AbstractSequence> sequenceIterator;

  public SequenceConcretizer(
      final EngineContext engineContext,
      final boolean isPresimulation,
      final Iterator<AbstractSequence> sequenceIterator) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(sequenceIterator);

    this.engineContext = engineContext;
    this.isPresimulation = isPresimulation;
    this.sequenceIterator = sequenceIterator;
  }

  @Override
  public void init() {
    sequenceIterator.init();
  }

  @Override
  public boolean hasValue() {
    return sequenceIterator.hasValue();
  }

  @Override
  public ConcreteSequence value() {
    final AbstractSequence abstractSequence = sequenceIterator.value();
    return concretizeSequence(resolveDependencies(abstractSequence));
  }

  @Override
  public void next() {
    sequenceIterator.next();
  }

  @Override
  public void stop() {
    sequenceIterator.stop();
  }

  @Override
  public Iterator<ConcreteSequence> clone() {
    throw new UnsupportedOperationException();
  }

  private static AbstractSequence resolveDependencies(final AbstractSequence abstractSequence) {
    final Map<AbstractCall, Integer> abstractCalls = new IdentityHashMap<>();

    for (int index = 0; index < abstractSequence.getSequence().size(); index++) {
      final AbstractCall abstractCall = abstractSequence.getSequence().get(index);
      abstractCalls.put(abstractCall, index);
    }

    for (int index = 0; index < abstractSequence.getSequence().size(); index++) {
      final AbstractCall abstractCall =
          abstractSequence.getSequence().get(index);

      final AbstractCall dependencyAbstractCall =
          (AbstractCall) abstractCall.getAttributes().get("dependsOn");

      if (null != dependencyAbstractCall) {
        final int dependencyIndex = abstractCalls.get(dependencyAbstractCall);
        abstractCall.getAttributes().put("dependsOnIndex", dependencyIndex);
      }
    }

    return abstractSequence;
  }

  private ConcreteSequence concretizeSequence(final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(abstractSequence);

    // Makes a copy as the adapter may modify the abstract sequence.
    final AbstractSequence abstractSequenceCopy = new AbstractSequence(
        abstractSequence.getSection(), AbstractCall.copyAll(abstractSequence.getSequence()));

    final boolean isDebug = Logger.isDebug();
    Logger.setDebug(engineContext.getOptions().getValueAsBoolean(Option.DEBUG));

    try {
      engineContext.getModel().setUseTempState(true);
      return processSequence(engineContext, abstractSequenceCopy);
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException(e);
    } finally {
      Logger.setDebug(isDebug);
      engineContext.getModel().setUseTempState(false);
    }
  }

  private ConcreteSequence processSequence(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final int sequenceIndex =
        engineContext.getStatistics().getSequences();

    final List<ConcreteCall> concreteCalls =
        EngineUtils.makeConcreteCalls(engineContext, abstractSequence.getSequence());

    final ConcreteSequence.Builder builder =
        new ConcreteSequence.Builder(abstractSequence.getSection());
    builder.add(concreteCalls);

    final ConcreteSequence concreteSequence = builder.build();

    if (Logger.isDebug()) {
      Logger.debugHeader("Abstract Sequence");
      Printer.getConsole(engineContext.getOptions(), engineContext.getStatistics()).
          printSequence(engineContext.getModel().getPE(), builder.build());
    }

    final ConcreteSequenceCreator creator =
        new ConcreteSequenceCreator(sequenceIndex, abstractSequence, concreteSequence);

    execute(
        engineContext,
        creator,
        engineContext.getCodeAllocationAddress(),
        concreteSequence,
        sequenceIndex
        );

    creator.finishProcessing();
    engineContext.setCodeAllocationAddress(creator.getAllocationAddress());
    final ConcreteSequence result = creator.createTestSequence();

    // TODO: temporary implementation of self-checks.
    if (engineContext.getOptions().getValueAsBoolean(Option.SELF_CHECKS)) {
      final List<SelfCheck> selfChecks = createSelfChecks(abstractSequence.getSequence());
      result.setSelfChecks(selfChecks);
    }

    return result;
  }

  private void execute(
      final EngineContext engineContext,
      final ExecutorListener listener,
      final long allocationAddress,
      final ConcreteSequence concreteSequence,
      final int sequenceIndex) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(listener);
    InvariantChecks.checkNotNull(concreteSequence);

    if (concreteSequence.isEmpty()) {
      listener.setAllocationAddress(allocationAddress);
      return;
    }

    final List<ConcreteCall> sequence = concreteSequence.getAll();
    final LabelManager labelManager = new LabelManager(engineContext.getLabelManager());
    allocateData(engineContext, labelManager, sequence, sequenceIndex);

    final boolean isFetchDecodeEnabled =
        engineContext.getOptions().getValueAsBoolean(Option.FETCH_DECODE_ENABLED);

    final CodeAllocator codeAllocator = new CodeAllocator(
        engineContext.getModel(), labelManager, isFetchDecodeEnabled);

    codeAllocator.init();
    codeAllocator.setAddress(allocationAddress);
    codeAllocator.allocateSequence(concreteSequence, sequenceIndex);

    final ConcreteCall first = sequence.get(0);
    final ConcreteCall last = sequence.get(sequence.size() - 1);

    final long startAddress = first.getAddress();
    final long endAddress = last.getAddress() + last.getByteSize();

    listener.setAllocationAddress(endAddress);

    if (!isPresimulation || engineContext.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
      // Presimulation and processing of test situations are disabled.
      return;
    }

    final Code code = codeAllocator.getCode();
    final Executor executor = new Executor(engineContext, labelManager, true);

    executor.setPauseOnUndefinedLabel(false);
    executor.setListener(listener);

    long address = startAddress;
    do {
      listener.resetLastExecutedCall();
      final Executor.Status status = executor.execute(code, address);

      final boolean isValidAddress =
          code.hasAddress(status.getAddress()) || status.getAddress() == endAddress;

      if (isValidAddress) {
        address = status.getAddress();
      } else {
        if (Logger.isDebug()) {
          Logger.debug(
              "Jump to address %s located outside of the sequence.", status);
        }

        final ConcreteCall lastExecutedCall = listener.getLastExecutedCall();
        final long nextAddress = lastExecutedCall.getAddress() + lastExecutedCall.getByteSize();

        if (Logger.isDebug()) {
          Logger.debug(
              "Execution will be continued from the next instruction 0x%016x.", nextAddress );
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

  private static final class CallEntry {
    private final AbstractCall abstractCall;
    private final ConcreteCall concreteCall;
    private int processingCount;

    public CallEntry(final AbstractCall abstractCall, final ConcreteCall concreteCall) {
      InvariantChecks.checkNotNull(abstractCall);
      InvariantChecks.checkNotNull(concreteCall);

      this.abstractCall = abstractCall;
      this.concreteCall = concreteCall;
      this.processingCount = 0;
    }

    public AbstractCall getAbstractCall() {
      return abstractCall;
    }

    public ConcreteCall getConcreteCall() {
      return concreteCall;
    }

    public int getProcessingCount() {
      return processingCount;
    }

    public void setProcessingCount(final int processingCount) {
      this.processingCount = processingCount;
    }

    public void incProcessingCount() {
      processingCount++;
    }
  }

  private final class ConcreteSequenceCreator extends ExecutorListener {
    private final int sequenceIndex;
    private final AbstractSequence abstractSequence;
    private final ConcreteSequence concreteSequence;
    private final Map<ConcreteCall, CallEntry> callMap;
    private final Set<AddressingModeWrapper> initializedModes;
    private final ExecutorListener listenerForInitializers;
    private final ConcreteSequence.Builder testSequenceBuilder;

    private ConcreteSequenceCreator(
        final int sequenceIndex,
        final AbstractSequence abstractSequence,
        final ConcreteSequence concreteSequence) {
      InvariantChecks.checkNotNull(abstractSequence);
      InvariantChecks.checkNotNull(concreteSequence);

      this.sequenceIndex = sequenceIndex;
      this.abstractSequence = abstractSequence;
      this.concreteSequence = concreteSequence;
      this.callMap = new IdentityHashMap<>();
      this.initializedModes = new HashSet<>();
      this.listenerForInitializers = new ExecutorListener();

      final List<AbstractCall> abstractCalls = abstractSequence.getSequence();
      final List<ConcreteCall> concreteCalls = concreteSequence.getAll();
      InvariantChecks.checkTrue(abstractCalls.size() == concreteCalls.size());

      this.testSequenceBuilder = new ConcreteSequence.Builder(abstractSequence.getSection());
      for (int index = 0; index < abstractCalls.size(); ++index) {
        final AbstractCall abstractCall = abstractCalls.get(index);
        InvariantChecks.checkNotNull(abstractCall);

        if (abstractCall.isEmpty()) {
          continue;
        }

        final ConcreteCall concreteCall = concreteCalls.get(index);
        InvariantChecks.checkNotNull(concreteCall);

        if (abstractCall.getAttributes().containsKey("dependsOnIndex")) {
          final int dependencyIndex = (int) abstractCall.getAttributes().get("dependsOnIndex");

          final ConcreteCall dependencyConcreteCall = concreteCalls.get(dependencyIndex);
          InvariantChecks.checkNotNull(dependencyConcreteCall);

          if (callMap.containsKey(dependencyConcreteCall)) {
            final CallEntry callEntry = callMap.get(dependencyConcreteCall);
            InvariantChecks.checkNotNull(callEntry);
            callMap.put(concreteCall, callEntry);
          } else {
            final AbstractCall dependencyAbstractCall = abstractCalls.get(dependencyIndex);
            InvariantChecks.checkNotNull(dependencyAbstractCall);

            final CallEntry callEntry =
                new CallEntry(dependencyAbstractCall, dependencyConcreteCall);

            callMap.put(dependencyConcreteCall, callEntry);
            callMap.put(concreteCall, callEntry);
          }
        } else {
          if (!callMap.containsKey(concreteCall)) {
            callMap.put(concreteCall, new CallEntry(abstractCall, concreteCall));
          }
        }

        this.testSequenceBuilder.add(concreteCall);
      }
    }

    public ConcreteSequence createTestSequence() {
      return testSequenceBuilder.build();
    }

    @Override
    public void onBeforeExecute(
        final EngineContext engineContext,
        final ConcreteCall concreteCall) {
      InvariantChecks.checkNotNull(concreteCall);
      InvariantChecks.checkNotNull(engineContext);

      final CallEntry callEntry = callMap.get(concreteCall);
      InvariantChecks.checkNotNull(callEntry);

      if (callEntry.getProcessingCount() != concreteCall.getExecutionCount()) {
        return; // Already processed
      }

      try {
        processCall(engineContext, callEntry, false);
      } catch (final ConfigurationException e) {
        throw new GenerationAbortedException(
            "Failed to generate test data for " + concreteCall.getText(), e);
      } finally {
        callEntry.incProcessingCount();
      }
    }

    private void processCall(
        final EngineContext engineContext,
        final CallEntry callEntry,
        final boolean terminate) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(callEntry);

      final AbstractCall abstractCall = callEntry.getAbstractCall();
      final ConcreteCall concreteCall = callEntry.getConcreteCall();
      final int processingCount = callEntry.getProcessingCount();

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

      processPrimitive(
          engineContext,
          processingCount,
          terminate,
          abstractCall,
          abstractPrimitive,
          concretePrimitive
          );
    }

    private void processPrimitive(
        final EngineContext engineContext,
        final int processingCount,
        final boolean terminate,
        final AbstractCall abstractCall,
        final Primitive abstractPrimitive,
        final IsaPrimitive concretePrimitive) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(abstractPrimitive);
      InvariantChecks.checkNotNull(concretePrimitive);

      // Unrolls shortcuts to establish correspondence between abstract and concrete primitives.
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

          processPrimitive(
              engineContext,
              processingCount,
              terminate,
              abstractCall,
              abstractArgument,
              concreteArgument
              );
        }
      }

      final List<AbstractCall> initializer = EngineUtils.makeInitializer(
          engineContext,
          processingCount,
          abstractCall,
          abstractSequence,
          abstractPrimitive,
          abstractPrimitive.getSituation(),
          initializedModes,
          fixedConcretePrimitive
          );

      processInitializer(engineContext, initializer);
    }

    private IsaPrimitive findConcretePrimitive(
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
        final List<AbstractCall> abstractCalls) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(abstractCalls);

      final List<ConcreteCall> concreteCalls =
          EngineUtils.makeConcreteCalls(engineContext, abstractCalls);

      final ConcreteSequence.Builder builder =
          new ConcreteSequence.Builder(abstractSequence.getSection());

      builder.add(concreteCalls);
      final ConcreteSequence concreteSequence = builder.build();

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
            concreteSequence,
            sequenceIndex
            );
      } finally {
        programCounter.setValue(programCounterValue);
        setAllocationAddress(listenerForInitializers.getAllocationAddress());
        Logger.debug("");
      }
    }

    public void finishProcessing() throws ConfigurationException {
      for (final ConcreteCall concreteCall : concreteSequence.getAll()) {
        final CallEntry callEntry = callMap.get(concreteCall);
        InvariantChecks.checkNotNull(callEntry);

        if (callEntry.getProcessingCount() != -1) {
          callEntry.setProcessingCount(-1);
          processCall(engineContext, callEntry, true);
        }
      }
    }
  }

  private static List<SelfCheck> createSelfChecks(final List<AbstractCall> abstractSequence) {
    InvariantChecks.checkNotNull(abstractSequence);

    final Set<AddressingModeWrapper> modes = EngineUtils.getOutAddressingModes(abstractSequence);
    final List<SelfCheck> selfChecks = new ArrayList<>(modes.size());

    for (final AddressingModeWrapper mode : modes) {
      selfChecks.add(new SelfCheck(mode));
    }

    return selfChecks;
  }
}
