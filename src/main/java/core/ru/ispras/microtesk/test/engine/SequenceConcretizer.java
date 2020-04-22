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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.Aspectracer;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.InstructionCall;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.memory.LocationAccessor;
import ru.ispras.microtesk.model.memory.LocationManager;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.Code;
import ru.ispras.microtesk.test.CodeAllocator;
import ru.ispras.microtesk.test.ConcreteSequence;
import ru.ispras.microtesk.test.Executor;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.NumericLabelTracker;
import ru.ispras.microtesk.test.Printer;
import ru.ispras.microtesk.test.SelfCheck;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.Stream;
import ru.ispras.microtesk.utils.BigIntegerUtils;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
final class SequenceConcretizer implements Iterator<ConcreteSequence> {
  private final EngineContext engineContext;
  private final boolean isPresimulation;
  private final Iterator<AbstractSequence> sequenceIterator;

  public SequenceConcretizer(
      final EngineContext engineContext,
      final String dataCombinatorName,
      final boolean isPresimulation,
      final Iterator<AbstractSequence> sequenceIterator) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(dataCombinatorName);
    InvariantChecks.checkNotNull(sequenceIterator);

    this.engineContext = engineContext;
    this.isPresimulation = isPresimulation;

    this.sequenceIterator = new AbstractSequenceTestDataIterator(
        engineContext,
        dataCombinatorName,
        new AbstractSequenceIterator(sequenceIterator)
    );
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
    return concretizeSequence(abstractSequence);
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

  private ConcreteSequence concretizeSequence(final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(abstractSequence);

    final boolean isDebug = Logger.isDebug();
    Logger.setDebug(engineContext.getOptions().getValueAsBoolean(Option.DEBUG_PRINT));

    try {
      engineContext.getModel().setUseTempState(true);
      return processSequence(engineContext, abstractSequence);
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

    final ConcreteSequence concreteSequence =
        ConcreteSequence.newConcreteSequence(abstractSequence.getSection(), concreteCalls);

    if (Logger.isDebug()) {
      Logger.debugHeader("Abstract Sequence");
      Printer.getConsole(engineContext.getOptions(), engineContext.getStatistics())
          .printSequence(engineContext.getModel(), concreteSequence);
    }

    final ConcreteSequenceCreator creator =
        new ConcreteSequenceCreator(sequenceIndex, abstractSequence, concreteSequence);

    creator.setAllocationAddress(engineContext.getCodeAllocationAddress());

    if (isPresimulation) {
      creator.startProcessing();
    }

    execute(
        engineContext,
        creator,
        creator.getAllocationAddress(),
        concreteSequence,
        sequenceIndex
    );

    if (isPresimulation) {
      creator.finishProcessing();
    }

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

    final LabelManager labelManager =
        new LabelManager(engineContext.getLabelManager());

    final NumericLabelTracker numericLabelTracker =
        new NumericLabelTracker(engineContext.getNumericLabelTracker());

    allocateData(engineContext, labelManager, sequence, sequenceIndex);

    final CodeAllocator codeAllocator = new CodeAllocator(
        engineContext.getModel(), labelManager, numericLabelTracker);

    codeAllocator.init();
    codeAllocator.setAddress(
        concreteSequence.getSection(), BigIntegerUtils.asUnsigned(allocationAddress));
    codeAllocator.allocateSequence(concreteSequence, sequenceIndex);

    final ConcreteCall first = sequence.get(0);
    final ConcreteCall last = sequence.get(sequence.size() - 1);

    final long startAddress = first.getAddress().longValue();
    final long endAddress = last.getAddress().longValue() + last.getByteSize();

    listener.setAllocationAddress(endAddress);

    if (!isPresimulation || engineContext.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
      // Presimulation and processing of test situations are disabled.
      return;
    }

    final Code code = codeAllocator.getCode();

    final Executor executor = new Executor(engineContext, labelManager, true);

    // Copies exception handler addresses from the global context.
    // Needed to favorably handle exceptions.
    code.getHandlerAddresses().putAll(
        engineContext.getCodeAllocator().getCode().getHandlerAddresses());

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
        final long nextAddress = lastExecutedCall.getAddress().longValue() + lastExecutedCall.getByteSize();

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

    final MemoryAllocator memoryAllocator = engineContext.getModel().getMemoryAllocator();

    for (final ConcreteCall call : sequence) {
      if (call.getData() != null) {
        final DataSection data = call.getData();
        data.setSequenceIndex(sequenceIndex);
        data.allocateDataAndRegisterLabels(memoryAllocator, labelManager);
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

    public void incProcessingCount() {
      processingCount++;
    }
  }

  private final class ConcreteSequenceCreator extends ExecutorListener {
    private final int sequenceIndex;
    private final AbstractSequence abstractSequence;
    private final ConcreteSequence concreteSequence;
    private final Map<ConcreteCall, CallEntry> callMap;
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

        Logger.debug(
            "%3d (%10d) %s %s",
            index,
            System.identityHashCode(concreteCall),
            abstractCall.getAttributes().containsKey("dependsOnIndex")
                ? String.format("Ref=%3d", abstractCall.getAttributes().get("dependsOnIndex"))
                : "       ",
            concreteCall.getText()
        );

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

      Logger.debug(
          "%s (%d)->(%d): processing count = %d, execution count = %d",
          callEntry.getConcreteCall().getText(),
          System.identityHashCode(concreteCall),
          System.identityHashCode(callEntry.getConcreteCall()),
          callEntry.getProcessingCount(),
          callEntry.getConcreteCall().getExecutionCount());

      if (callEntry.getProcessingCount() != callEntry.getConcreteCall().getExecutionCount()) {
        Logger.debug("NO PROCESSING");
        return; // Already processed
      }

      try {
        processCall(engineContext, callEntry, InitializerMaker.Stage.MAIN);
      } catch (final ConfigurationException e) {
        throw new GenerationAbortedException(
            "Failed to generate test data for " + concreteCall.getText(), e);
      } finally {
        Logger.debug(
          "%s Before increment: processing count = %d, execution count = %d",
          callEntry.getConcreteCall().getText(),
          callEntry.getProcessingCount(),
          callEntry.getConcreteCall().getExecutionCount());
        callEntry.incProcessingCount();
      }
    }

    private void processCall(
        final EngineContext engineContext,
        final CallEntry callEntry,
        final InitializerMaker.Stage stage) throws ConfigurationException {
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
        Logger.debug("%nGenerating test data for %s (processing count is %d)...",
            concreteCall.getText(), processingCount);
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
          stage,
          abstractCall,
          abstractPrimitive,
          concretePrimitive
      );
    }

    private void processPrimitive(
        final EngineContext engineContext,
        final int processingCount,
        final InitializerMaker.Stage stage,
        final AbstractCall abstractCall,
        final Primitive abstractPrimitive,
        final IsaPrimitive concretePrimitive) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(abstractPrimitive);
      InvariantChecks.checkNotNull(concretePrimitive);

      // Unrolls shortcuts to establish correspondence between abstract and concrete primitives.
      final IsaPrimitive fixedConcretePrimitive =
          findConcretePrimitive(abstractPrimitive.getName(), concretePrimitive);

      InvariantChecks.checkNotNull(
          fixedConcretePrimitive, abstractPrimitive.getName() + " not found.");

      for (final Argument argument : abstractPrimitive.getArguments().values()) {
        if (argument.isPrimitive()) {
          final String argumentName = argument.getName();
          final Primitive abstractArgument = (Primitive) argument.getValue();

          IsaPrimitive concreteArgument = fixedConcretePrimitive.getArguments().get(argumentName);
          if (null == concreteArgument) {
            concreteArgument =
                findConcreteArgument(abstractArgument.getName(), argumentName, concretePrimitive);
          }

          InvariantChecks.checkNotNull(concreteArgument);
          processPrimitive(
              engineContext,
              processingCount,
              stage,
              abstractCall,
              abstractArgument,
              concreteArgument
          );
        }
      }

      final Situation situation = abstractPrimitive.getSituation();
      if (null == situation && Primitive.Kind.MODE == abstractPrimitive.getKind()) {
        // No default data generation for addressing modes.
        return;
      }

      final List<AbstractCall> initializer = EngineUtils.makeInitializer(
          engineContext,
          processingCount,
          stage,
          abstractCall,
          abstractSequence,
          abstractPrimitive,
          situation,
          fixedConcretePrimitive
          );

      final LocationManager locationsToBeRestored =
          getLocationsToBeRestored(engineContext, stage, situation);

      processInitializer(engineContext, initializer, locationsToBeRestored);
    }

    private void processInitializer(
        final EngineContext engineContext,
        final List<AbstractCall> abstractCalls,
        final LocationManager locationsToBeRestored) throws ConfigurationException {
      InvariantChecks.checkNotNull(engineContext);
      InvariantChecks.checkNotNull(abstractCalls);

      final List<ConcreteCall> concreteCalls =
          EngineUtils.makeConcreteCalls(engineContext, abstractCalls);

      final ConcreteSequence concreteSequence =
          ConcreteSequence.newConcreteSequence(abstractSequence.getSection(), concreteCalls);

      testSequenceBuilder.addToPrologue(concreteCalls);

      if (!concreteCalls.isEmpty()) {
        Logger.debug("Executing initializing code...");
      }

      locationsToBeRestored.save();
      try {
        execute(
            engineContext,
            listenerForInitializers,
            getAllocationAddress(),
            concreteSequence,
            sequenceIndex
        );
      } finally {
        locationsToBeRestored.restore();
        setAllocationAddress(listenerForInitializers.getAllocationAddress());
        Logger.debug("");
      }
    }

    public void startProcessing() throws ConfigurationException {
      for (final ConcreteCall concreteCall : concreteSequence.getAll()) {
        final CallEntry callEntry = callMap.get(concreteCall);

        // Dependencies are ignored. Calls are processed according to their order in the collection.
        if (null != callEntry && callEntry.getConcreteCall() == concreteCall) {
          processCall(engineContext, callEntry, InitializerMaker.Stage.PRE);
        }
      }
    }

    public void finishProcessing() throws ConfigurationException {
      for (final ConcreteCall concreteCall : concreteSequence.getAll()) {
        final CallEntry callEntry = callMap.get(concreteCall);

        // Dependencies are ignored. Calls are processed according to their order in the collection.
        if (null != callEntry && callEntry.getConcreteCall() == concreteCall) {
          processCall(engineContext, callEntry, InitializerMaker.Stage.POST);
        }
      }
    }
  }

  private static LocationManager getLocationsToBeRestored(
      final EngineContext engineContext,
      final InitializerMaker.Stage stage,
      final Situation situation) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(stage);

    final LocationAccessor programCounter = engineContext.getModel().getPE().accessLocation("PC");
    final LocationManager locationsToBeRestored;

    if (stage == InitializerMaker.Stage.MAIN) {
      final String streamId =
          null != situation ? (String) situation.getAttributes().get("stream") : null;

      if (streamId != null) {
        final Stream stream = engineContext.getStreams().getStream(streamId);

        final IsaPrimitive indexSource =
            EngineUtils.makeConcretePrimitive(engineContext, stream.getIndexSource());

        final LocationAccessor streamIndex = indexSource.access(
            engineContext.getModel().getPE(), engineContext.getModel().getTempVars());

        locationsToBeRestored = new LocationManager(programCounter, streamIndex);
      } else {
        locationsToBeRestored = new LocationManager(programCounter);
      }
    } else {
      locationsToBeRestored = new LocationManager(programCounter);
    }

    return locationsToBeRestored;
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

  private static IsaPrimitive findConcretePrimitive(
      final String primitiveName,
      final IsaPrimitive rootPrimitive) {
    InvariantChecks.checkNotNull(primitiveName);
    InvariantChecks.checkNotNull(rootPrimitive);

    if (primitiveName.equals(rootPrimitive.getName())) {
      return rootPrimitive;
    }

    for (final IsaPrimitive argument : rootPrimitive.getArguments().values()) {
      final IsaPrimitive result = findConcretePrimitive(primitiveName, argument);
      if (null != result) {
        return result;
      }
    }

    return null;
  }

  private static IsaPrimitive findConcreteArgument(
      final String primitiveName,
      final String argumentName,
      final IsaPrimitive rootPrimitive) {
    InvariantChecks.checkNotNull(primitiveName);
    InvariantChecks.checkNotNull(argumentName);
    InvariantChecks.checkNotNull(rootPrimitive);

    final IsaPrimitive namedArgument = rootPrimitive.getArguments().get(argumentName);
    if (null != namedArgument && primitiveName.equals(namedArgument.getName())) {
      return namedArgument;
    }

    for (final IsaPrimitive argument : rootPrimitive.getArguments().values()) {
      final IsaPrimitive result = findConcreteArgument(primitiveName, argumentName, argument);
      if (null != result) {
        return result;
      }
    }

    return null;
  }
}
