/*
 * Copyright 2016-2017 ISP RAS (http://www.ispras.ru)
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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.model.tracer.Tracer;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.engine.AdapterResult;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.SelfCheckEngine;
import ru.ispras.microtesk.test.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.engine.TestSequenceEngineResult;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.AbstractSequence;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.SectionKind;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link TemplateProcessor2} class is responsible for template processing.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TemplateProcessor2 implements Template.Processor {
  private static final class BlockEntry {
    private final ConcreteSequence entry;
    private final Block block;
    private final int times;
    private boolean beingProcessed;

    private BlockEntry(final Block block) {
      this(block, 1);
    }

    private BlockEntry(final Block block, final int times) {
      InvariantChecks.checkNotNull(block);
      InvariantChecks.checkGreaterThanZero(times);

      this.entry = new ConcreteSequence.Builder().build();
      this.block = block;
      this.times = times;
      this.beingProcessed = false;
    }
  }

  private final EngineContext engineContext;
  private final int instanceNumber;
  private final TestProgram testProgram;
  private final Set<BlockEntry> postponedBlocks;
  private final CodeAllocator allocator;
  private final Executor executor;
  private final List<Executor.Status> executorStatuses;
  private final Deque<ConcreteSequence> interruptedSequences;
  private boolean isProgramStarted;
  private boolean hasDispatchingCode;

  public TemplateProcessor2(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkGreaterThanZero(engineContext.getModel().getPENumber());

    final Model model = engineContext.getModel();
    final LabelManager labelManager = engineContext.getLabelManager();

    final long baseAddress =
        engineContext.getOptions().getValueAsBigInteger(Option.BASE_VA).longValue();

    final boolean isFetchDecodeEnabled =
        engineContext.getOptions().getValueAsBoolean(Option.FETCH_DECODE_ENABLED);

    this.engineContext = engineContext;
    this.instanceNumber = model.getPENumber();
    this.testProgram = new TestProgram();
    this.postponedBlocks = new LinkedHashSet<>();
    this.allocator = new CodeAllocator(model, labelManager, baseAddress, isFetchDecodeEnabled);
    this.executor = new Executor(engineContext);
    this.executorStatuses = new ArrayList<>(instanceNumber);
    this.interruptedSequences = new ArrayDeque<>();
    this.isProgramStarted = false;
    this.hasDispatchingCode = false;

    if (engineContext.getOptions().getValueAsBoolean(Option.TRACER_LOG)) {
      final String outDir = Printer.getOutDir(engineContext.getOptions());
      Tracer.initialize(outDir, engineContext.getOptions().getValueAsString(Option.CODE_PRE));
    }
  }

  @Override
  public void process(final ExceptionHandler handler) {
    Logger.debugHeader("Processing Exception Handler");
    InvariantChecks.checkNotNull(handler);

    final Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>> concreteHandler;
    try {
      concreteHandler = TestEngineUtils.makeExceptionHandler(engineContext, handler);
      testProgram.addExceptionHandlers(concreteHandler);
      PrinterUtils.printExceptionHandler(engineContext, handler.getId(), concreteHandler.first);
    } catch (final Exception e) {
      TestEngineUtils.rethrowException(e);
    }
  }

  @Override
  public void process(final SectionKind section, final Block block) {
    process(section, block, 1);
  }

  @Override
  public void process(final SectionKind section, final Block block, final int times) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(block);
    InvariantChecks.checkTrue(block.isExternal() ? times == 1 : true);

    engineContext.getStatistics().pushActivity(Statistics.Activity.SEQUENCING);
    try {
      if (section == SectionKind.PRE) {
        processPrologue(block);
      } else if (section == SectionKind.POST) {
        processEpilogue(block);
      } else if (block.isExternal()) {
        processExternalBlock(block);
      } else {
        processBlock(block, times);
      }
    } catch (final Exception e) {
      TestEngineUtils.rethrowException(e);
    } finally {
      engineContext.getStatistics().popActivity(); // SEQUENCING
    }
  }

  @Override
  public void process(final DataSection data) {
    InvariantChecks.checkNotNull(data);

    data.allocate(engineContext.getModel().getMemoryAllocator());
    data.registerLabels(engineContext.getLabelManager());

    if (data.isSeparateFile()) {
      try {
        PrinterUtils.printDataSection(engineContext, data);
      } catch (final Exception e) {
        TestEngineUtils.rethrowException(e);
      }

      if (!data.isGlobal()) {
        return;
      }
    }

    testProgram.addData(data);
  }

  @Override
  public void finish() {
    try {
      startProgram();
      runExecutionFromStart();

      processPostponedBlocks();
      processPostponedBlocksNoSimulation();

      finishProgram();
      Logger.debugHeader("Ended Processing Template");
    } catch (final Exception e) {
      TestEngineUtils.rethrowException(e);
    } finally {
      engineContext.getStatistics().popActivity(); // PARSING
      engineContext.getStatistics().saveTotalTime();
    }
  }

  private void processPrologue(final Block block) throws ConfigurationException {
    testProgram.setPrologue(
        TestEngineUtils.makeExternalTestSequence(engineContext, block, "Prologue"));
  }

  private void processEpilogue(final Block block) throws ConfigurationException {
    testProgram.setEpilogue(
        TestEngineUtils.makeExternalTestSequence(engineContext, block, "Epilogue"));
  }

  private void processExternalBlock(final Block block) throws ConfigurationException, IOException {
    startProgram();

    hasDispatchingCode = true;
    final ConcreteSequence prevEntry = testProgram.getLastEntry();
    if (!TestEngineUtils.canBeAllocatedAfter(prevEntry, block)) {
      Logger.debug("Processing of external code defined at %s is postponed.", block.getWhere());
      postpone(new BlockEntry(block));
      return;
    }

    final ConcreteSequence sequence = TestEngineUtils.makeExternalTestSequence(engineContext, block);
    allocateTestSequence(sequence, Label.NO_SEQUENCE_INDEX);
  }

  private void processBlock(
      final Block block,
      final int times) throws ConfigurationException, IOException {
    startProgram();

    Logger.debug("Processing of block defined at %s is postponed.", block.getWhere());
    postpone(new BlockEntry(block, times));
  }

  private ConcreteSequence processSelfChecks(
      final ConcreteSequence previous,
      final List<SelfCheck> selfChecks,
      final int testCaseIndex) throws ConfigurationException {
    InvariantChecks.checkNotNull(previous);

    if (null == selfChecks) {
      return previous;
    }

    final String sequenceId = String.format("Self-Checks for Test Case %d", testCaseIndex);
    final Executor.Status status = executorStatuses.get(engineContext.getModel().getActivePE());

    if (!TestEngineUtils.isAtEndOf(status, previous)) {
      Logger.warning("%s will not be created because execution does not reach them.", sequenceId);
      return previous;
    }

    Logger.debugHeader("Preparing %s", sequenceId);
    final ConcreteSequence sequence = SelfCheckEngine.solve(engineContext, selfChecks);
    sequence.setTitle(sequenceId);

    allocateTestSequenceAfter(previous, sequence, testCaseIndex);

    try {
      executor.setPauseOnUndefinedLabel(false);
      executor.setSelfCheckMode(true);
      runExecution(sequence);
    } finally {
      executor.setPauseOnUndefinedLabel(true);
      executor.setSelfCheckMode(false);
    }

    return sequence;
  }

  private void postpone(final BlockEntry blockEntry) {
    InvariantChecks.checkNotNull(blockEntry);
    testProgram.addEntry(blockEntry.entry);
    postponedBlocks.add(blockEntry);
  }

  private void processPostponedBlocks() throws ConfigurationException, IOException {
    boolean isProcessed = false;
    do {
      isProcessed = false;
      for (final BlockEntry blockEntry : postponedBlocks) {
        if (blockEntry.beingProcessed) {
          continue;
        }
        blockEntry.beingProcessed = true;

        final ConcreteSequence entry = blockEntry.entry;
        final Block block = blockEntry.block;
        final int times = blockEntry.times;

        if (block.isExternal()) {
          isProcessed = processPostponedExternalBlock(block, entry);
        } else {
          isProcessed = processPostponedBlock(block, times, entry);
        }

        if (isProcessed) {
          postponedBlocks.remove(blockEntry);
          break;
        }

        blockEntry.beingProcessed = false;
      }
    } while (isProcessed);
  }

  private boolean processPostponedExternalBlock(
      final Block block,
      final ConcreteSequence entry) throws ConfigurationException, IOException {
    if (!isProgramStarted) {
      startProgram();
      runExecutionFromStart();
    }

    final ConcreteSequence prevEntry = testProgram.getPrevEntry(entry);
    if (!TestEngineUtils.canBeAllocatedAfter(prevEntry, block)) {
      Logger.debug("Processing of external code defined at %s is postponed again.", block.getWhere());
      return false;
    }

    // This is needed to prevent allocation of postponed sequences in middle
    // of sequences constructed by a block (interrupting a block).
    for (final Executor.Status status: executorStatuses) { 
      if (TestEngineUtils.isAtEndOfAny(status, interruptedSequences)) {
        Logger.debug("Processing of block defined at %s is skipped.", block.getWhere());
        return false;
      }
    }

    final int instanceIndex = TestEngineUtils.findAtEndOf(executorStatuses, prevEntry);
    if (-1 != instanceIndex) {
      engineContext.getModel().setActivePE(instanceIndex);
    }

    final ConcreteSequence sequence = TestEngineUtils.makeExternalTestSequence(engineContext, block);
    allocateTestSequenceWithReplace(entry, sequence, Label.NO_SEQUENCE_INDEX);

    runExecution(sequence);
    return true;
  }

  private boolean processPostponedBlock(
      final Block block,
      final int times,
      final ConcreteSequence entry) throws ConfigurationException, IOException {
    if (!isProgramStarted) {
      startProgram();
      runExecutionFromStart();
    }

    final ConcreteSequence prevEntry = testProgram.hasEntry(entry) ?
                                       testProgram.getPrevEntry(entry) :
                                       testProgram.getLastEntry();

    final int instanceIndex = TestEngineUtils.findAtEndOf(executorStatuses, prevEntry);
    if (-1 == instanceIndex) {
      Logger.debug("Processing of block defined at %s is postponed again.", block.getWhere());
      return false;
    }

    // This is needed to prevent allocation of postponed sequences in middle
    // of sequences constructed by a block (interrupting a block).
    if (!executorStatuses.isEmpty() &&
        TestEngineUtils.isAtEndOfAny(executorStatuses.get(instanceIndex), interruptedSequences)) {
      Logger.debug("Processing of block defined at %s is skipped.", block.getWhere());
      return false;
    }

    engineContext.getModel().setActivePE(instanceIndex);

    long allocationAddress =
        null != prevEntry ? prevEntry.getEndAddress() : allocator.getAddress();

    // The placeholder entry is marked allocated at allocationAddress in case no sequences are
    // produced. In this situation, the placeholder sequences will be printed as empty.
    InvariantChecks.checkTrue(entry.isEmpty());
    entry.setAllocationAddresses(allocationAddress, allocationAddress);

    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);
    ConcreteSequence previous = entry;

    for (int index = 0; index < times; index++) {
      final Iterator<List<AbstractCall>> abstractIt = block.getIterator();
      for (abstractIt.init(); abstractIt.hasValue(); abstractIt.next()) {
        engineContext.setCodeAllocationAddress(allocationAddress);

        final TestSequenceEngineResult engineResult =
            engine.process(engineContext, new AbstractSequence(abstractIt.value()));
        final Iterator<AdapterResult> concreteIt = engineResult.getResult();

        for (concreteIt.init(); concreteIt.hasValue(); concreteIt.next()) {
          if (!isProgramStarted) {
            startProgram();
            runExecutionFromStart();
          }

          final ConcreteSequence sequence = TestEngineUtils.getTestSequence(concreteIt.value());
          final int sequenceIndex = engineContext.getStatistics().getSequences();
          sequence.setTitle(String.format("Test Case %d (%s)", sequenceIndex, block.getWhere()));

          if (!testProgram.hasEntry(previous)) {
            allocateTestSequence(sequence, sequenceIndex);
          } else if (previous == entry) {
            allocateTestSequenceWithReplace(previous, sequence, sequenceIndex);
          } else {
            allocateTestSequenceAfter(previous, sequence, sequenceIndex);
          }

          engineContext.getStatistics().incSequences();
          runExecution(sequence);

          final Executor.Status status = executorStatuses.get(instanceIndex);
          if (!TestEngineUtils.isAtEndOf(status, sequence) &&
              !TestEngineUtils.isAtEndOfAny(status, interruptedSequences)) {
            interruptedSequences.push(sequence);
            processPostponedBlocks();
            interruptedSequences.pop();
          }

          previous = processSelfChecks(sequence, engineResult.getSelfChecks(), sequenceIndex);
          allocationAddress = previous.getEndAddress();

          if (!hasDispatchingCode && engineContext.getStatistics().isFileLengthLimitExceeded()) {
            finishProgram();
          }
        } // Concrete sequence iterator
      } // Abstract sequence iterator
    } // For times

    return true;
  }

  private void processPostponedBlocksNoSimulation() throws ConfigurationException {
    boolean isFirst = true;
    for (final BlockEntry blockEntry : postponedBlocks) {
      if (isFirst) {
        Logger.debugHeader("Processing All Postponed Blocks Without Simulation");
        isFirst = false;
      }

      final ConcreteSequence entry = blockEntry.entry;
      final Block block = blockEntry.block;
      final int times = blockEntry.times;

      if (block.isExternal()) {
        processPostponedExternalBlockNoSimulation(block, entry);
      } else {
        processPostponedBlockNoSimulation(block, times, entry);
      }
    }
  }

  private void processPostponedExternalBlockNoSimulation(
      final Block block,
      final ConcreteSequence entry) throws ConfigurationException {
    final ConcreteSequence sequence = TestEngineUtils.makeExternalTestSequence(engineContext, block);
    allocateTestSequenceWithReplace(entry, sequence, Label.NO_SEQUENCE_INDEX);
  }

  private void processPostponedBlockNoSimulation(
      final Block block,
      final int times,
      final ConcreteSequence entry) throws ConfigurationException {
    final ConcreteSequence prevEntry = testProgram.getPrevEntry(entry);

    long allocationAddress =
        null != prevEntry ? prevEntry.getEndAddress() : allocator.getAddress();

    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);
    ConcreteSequence previous = entry;

    for (int index = 0; index < times; index++) {
    final Iterator<List<AbstractCall>> abstractIt = block.getIterator();
      for (abstractIt.init(); abstractIt.hasValue(); abstractIt.next()) {
        engineContext.setCodeAllocationAddress(allocationAddress);

        final TestSequenceEngineResult engineResult =
            engine.process(engineContext, new AbstractSequence(abstractIt.value()));
        final Iterator<AdapterResult> concreteIt = engineResult.getResult();

        for (concreteIt.init(); concreteIt.hasValue(); concreteIt.next()) {
          final ConcreteSequence sequence = TestEngineUtils.getTestSequence(concreteIt.value());

          final int sequenceIndex = engineContext.getStatistics().getSequences();
          sequence.setTitle(String.format("Test Case %d (%s)", sequenceIndex, block.getWhere()));

          if (previous == entry) {
            allocateTestSequenceWithReplace(previous, sequence, sequenceIndex);
          } else {
            allocateTestSequenceAfter(previous, sequence, sequenceIndex);
          }

          previous = sequence;
          allocationAddress = previous.getEndAddress();

          engineContext.getStatistics().incSequences();
        } // Concrete sequence iterator
      } // Abstract sequence iterator
    } // For times
  }

  private void startProgram() throws IOException, ConfigurationException {
    if (isProgramStarted) {
      return;
    }

    isProgramStarted = true;
    TestEngineUtils.notifyProgramStart();

    Tracer.createFile();
    allocator.init();

    if (engineContext.getStatistics().getPrograms() > 0) {
      // Allocates global data created during generation of previous test programs
      reallocateGlobalData();
    }

    allocator.allocateHandlers(testProgram.getExceptionHandlers());

    final ConcreteSequence prologue = testProgram.getPrologue();
    allocateTestSequence(prologue, Label.NO_SEQUENCE_INDEX);
  }

  private void finishProgram() throws ConfigurationException, IOException {
    if (!isProgramStarted) {
      return;
    }

    try {
      final ConcreteSequence epilogue = testProgram.getEpilogue();
      allocateTestSequence(epilogue, Label.NO_SEQUENCE_INDEX);
      runExecution(epilogue);

      TestEngineUtils.checkAllAtEndOf(executorStatuses, epilogue);
    } finally {
      TestEngineUtils.notifyProgramEnd();

      PrinterUtils.printTestProgram(engineContext, testProgram);
      Tracer.closeFile();

      // Clean up all the state
      engineContext.getModel().resetState();
      engineContext.getLabelManager().reset();
      allocator.reset();
      testProgram.reset();
      executorStatuses.clear();

      isProgramStarted = false;
    }
  }

  private void allocateTestSequenceWithReplace(
      final ConcreteSequence old,
      final ConcreteSequence sequence,
      final int sequenceIndex) throws ConfigurationException {
    final ConcreteSequence previous = testProgram.getPrevEntry(old);
    testProgram.replaceEntryWith(old, sequence);
    allocate(previous, sequence, sequenceIndex);
  }

  private void allocateTestSequenceAfter(
      final ConcreteSequence previous,
      final ConcreteSequence sequence,
      final int sequenceIndex) throws ConfigurationException {
    testProgram.addEntryAfter(previous, sequence);
    allocate(previous, sequence, sequenceIndex);
  }

  private void allocateTestSequence(
      final ConcreteSequence sequence,
      final int sequenceIndex) throws ConfigurationException {
    final ConcreteSequence previous = testProgram.getLastEntry();
    testProgram.addEntry(sequence);
    allocate(previous, sequence, sequenceIndex);
  }

  private void allocate(
      final ConcreteSequence previous,
      final ConcreteSequence sequence,
      final int sequenceIndex) throws ConfigurationException {
    PrinterUtils.printSequenceToConsole(engineContext, sequence);

    if (null != previous && previous.isAllocated()) {
      allocator.setAddress(previous.getEndAddress());
    }

    allocateData(sequence, sequenceIndex);
    allocator.allocateSequence(sequence, sequenceIndex);

    engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
  }

  private void allocateData(final ConcreteSequence sequence, final int sequenceIndex) {
    for (final ConcreteCall call : sequence.getAll()) {
      if (call.getData() != null) {
        final DataSection data = call.getData();
        data.setSequenceIndex(sequenceIndex);
        process(data);
      }
    }
  }

  private void reallocateGlobalData() {
    final MemoryAllocator memoryAllocator = engineContext.getModel().getMemoryAllocator();
    memoryAllocator.reset();

    for (final DataSection data : testProgram.getGlobalData()) {
      data.allocate(memoryAllocator);
      data.registerLabels(engineContext.getLabelManager());
    }

    testProgram.readdGlobalData();
  }

  /**
   * Executes all threads that can resume execution after the current sequence was allocated.
   * 
   * <p>A thread can resume execution if the address where execution was stopped is now allocated.
   * Special case: If a thread points for the end of previous code block and it does not match
   * the beginning of the current code block, the thread will be run causing an illegal address
   * error.
   * <p>Threads are executed until they reach the end of allocated code or a jump to an undefined
   * label.
   * 
   * @param sequence The most recently allocated sequence which is expected to be executed
   *                 by some of the threads.
   */
  private boolean runExecution(final ConcreteSequence sequence) {
    InvariantChecks.checkNotNull(sequence);
    Logger.debugHeader("Running Execution from " + sequence.getTitle());

    if (engineContext.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
      Logger.debug("Simulation is disabled");
      return false;
    }

    // Nothing to execute (no new code was allocated).
    if (sequence.isEmpty()) {
      return false;
    }

    final boolean isNoStatuses = executorStatuses.isEmpty();
    final Code code = allocator.getCode();

    boolean isExecuted = false;
    for (int index = 0; index < instanceNumber; index++) {
      // Sets initial statuses (address of first sequence in a program).
      if (isNoStatuses) {
        executorStatuses.add(Executor.Status.newAddress(sequence.getStartAddress()));
      }

      final Executor.Status status = executorStatuses.get(index);
      final long address = status.getAddress();

      Logger.debugHeader("Instance %d", index);
      Logger.debug("Execution status: %s%n", status);

      final boolean isUndefinedLabel =
          status.isLabelReference() &&
          engineContext.getLabelManager().resolve(
              status.getLabelReference().getReference()) == null;

      if (!code.hasAddress(address) || isUndefinedLabel) {
        Logger.debug("Execution cannot continue at the current stage.");
        continue;
      }

      engineContext.getModel().setActivePE(index);
      final Executor.Status newStatus = executor.execute(code, address);
      executorStatuses.set(index, newStatus);
      isExecuted = true;
    }

    return isExecuted;
  }

  private boolean runExecutionFromStart() {
    // Run from the first allocated non-empty entry.
    for (final ConcreteSequence entry : testProgram.getEntries()) {
      if (!entry.isAllocated()) {
        break;
      }

      if (!entry.isEmpty()) {
        return runExecution(entry);
      }
    }

    return false;
  }
}
