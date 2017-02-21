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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.sequence.GeneratorConfig;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.SelfCheckEngine;
import ru.ispras.microtesk.test.sequence.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link TemplateProcessor} class is responsible for template processing.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TemplateProcessor implements Template.Processor {
  private final EngineContext engineContext;
  private final int instanceNumber;
  private final TestProgram testProgram;
  private final CodeAllocator allocator;
  private final Executor executor;
  private final List<Executor.Status> executorStatuses;
  private boolean isProgramStarted;

  public TemplateProcessor(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkGreaterThanZero(engineContext.getModel().getPENumber());

    this.engineContext = engineContext;
    this.instanceNumber = engineContext.getModel().getPENumber();
    this.testProgram = new TestProgram();
    this.allocator = new CodeAllocator(engineContext);
    this.executor = new Executor(engineContext);
    this.executorStatuses = new ArrayList<>(instanceNumber);
    this.isProgramStarted = false;

    if (engineContext.getOptions().getValueAsBoolean(Option.TARMAC_LOG)) {
      final String outDir = Printer.getOutDir(engineContext.getOptions());
      Tarmac.initialize(outDir, engineContext.getOptions().getValueAsString(Option.CODE_PRE));
    }
  }

  @Override
  public void process(final ExceptionHandler handler) {
    Logger.debugHeader("Processing Exception Handler");
    InvariantChecks.checkNotNull(handler);

    final Pair<List<TestSequence>, Map<String, TestSequence>> concreteHandler;
    try {
      concreteHandler = TestEngineUtils.makeExceptionHandler(engineContext, handler);
      testProgram.addExceptionHandlers(concreteHandler);
      PrinterUtils.printExceptionHandler(engineContext, handler.getId(), concreteHandler.first);
    } catch (final Exception e) {
      rethrowException(e);
    }
  }

  @Override
  public void process(final Section section, final Block block) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(block);

    engineContext.getStatistics().pushActivity(Statistics.Activity.SEQUENCING);

    try {
      if (section == Section.PRE) {
        processPrologue(block);
      } else if (section == Section.POST) {
        processEpilogue(block);
      } else if (block.isExternal()) {
        processExternalBlock(block);
      } else {
        processBlock(block);
      }
    } catch (final Exception e) {
      rethrowException(e);
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
        rethrowException(e);
      }

      if (!data.isGlobal()) {
        return;
      }
    }

    testProgram.addData(data);
  }

  @Override
  public void finish() {
    for (final Executor.Status status : executorStatuses) {
      if (status.isLabelReference()) {
      throw new GenerationAbortedException(String.format(
          "Label '%s' is undefined or unavailable in the current execution scope.",
          status.getLabelReference().getReference().getName()));
      }
    }

    try {
      finishProgram();
      Logger.debugHeader("Ended Processing Template");
    } catch (final Exception e) {
      rethrowException(e);
    } finally {
      engineContext.getStatistics().popActivity(); // PARSING
      engineContext.getStatistics().saveTotalTime();
    }
  }

  private static void rethrowException(final Exception e) {
    if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    } else {
      throw new GenerationAbortedException(e);
    }
  }

  private void processPrologue(final Block block) {
    final TestSequence sequence =
        TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);

    sequence.setTitle("Prologue");
    testProgram.setPrologue(sequence);
  }

  private void processEpilogue(final Block block) {
    testProgram.setEpilogue(block);
  }

  private void processExternalBlock(final Block block) throws ConfigurationException, IOException {
    startProgram();
    final List<Call> abstractSequence = TestEngineUtils.getSingleSequence(block);

    int instanceIndex = findInstanceAtEndOfTestSequence(testProgram.getLastAllocatedEntry());
    if (-1 == instanceIndex) {
      instanceIndex = findInstanceJumpIntoSequence(abstractSequence);
    }

    if (-1 == instanceIndex) {
      Logger.debug("Processing of external code defined at %s is postponed.", block.getWhere());
      testProgram.addPostponedEntry(block);
      return;
    }

    if (testProgram.isPostponedEntry(testProgram.getLastEntry()) &&
        !TestEngineUtils.isOriginFixed(abstractSequence)) {
      throw new GenerationAbortedException(String.format(
          "External code defined at %s must have fixed origin.", block.getWhere()));
    }

    engineContext.getModel().setActivePE(instanceIndex);
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);
    final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);

    final TestSequence sequence = TestEngineUtils.getSingleTestSequence(iterator);
    sequence.setTitle("External Code");

    allocateTestSequence(sequence, Label.NO_SEQUENCE_INDEX);
    executeTestSequence(sequence);

    engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
    if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
      finishProgram();
    }

    processPostponedBlocks();
  }

  private void processBlock(final Block block) throws ConfigurationException, IOException {
    startProgram();

    final int instanceIndex =
        findInstanceAtEndOfTestSequence(testProgram.getLastAllocatedEntry());

    if (-1 == instanceIndex) {
      Logger.debug("Processing of block defined at %s is postponed.", block.getWhere());
      testProgram.addPostponedEntry(block);
      return;
    }

    engineContext.getModel().setActivePE(instanceIndex);
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);

    final Iterator<List<Call>> abstractIt = block.getIterator();
    for (abstractIt.init(); abstractIt.hasValue(); abstractIt.next()) {
      final Iterator<AdapterResult> concreteIt =
          engine.process(engineContext, abstractIt.value());

      for (concreteIt.init(); concreteIt.hasValue(); concreteIt.next()) {
        startProgram();

        final TestSequence sequence = TestEngineUtils.getTestSequence(concreteIt.value());
        final int sequenceIndex = engineContext.getStatistics().getSequences();
        sequence.setTitle(String.format("Test Case %d (%s)", sequenceIndex, block.getWhere()));

        allocateTestSequence(sequence, sequenceIndex);
        executeTestSequence(sequence);

        processSelfChecks(sequence.getChecks(), sequenceIndex);

        engineContext.getStatistics().incSequences();
        Logger.debugHeader("");

        engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
        if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
          finishProgram();
        }
      } // Concrete sequence iterator
    } // Abstract sequence iterator

    processPostponedBlocks();
  }

  private void processSelfChecks(
      final List<SelfCheck> selfChecks,
      final int testCaseIndex) throws ConfigurationException {
    InvariantChecks.checkNotNull(selfChecks);

    if (!engineContext.getOptions().getValueAsBoolean(Option.SELF_CHECKS)) {
      return;
    }

    final String sequenceId = String.format("Self-Checks for Test Case %d", testCaseIndex);
    Logger.debugHeader("Preparing %s", sequenceId);

    final TestSequence sequence = SelfCheckEngine.solve(engineContext, selfChecks);
    sequence.setTitle(sequenceId);
    engineContext.getStatistics().incInstructions(sequence.getInstructionCount());

    allocateTestSequence(sequence, testCaseIndex);
    executeTestSequence(sequence);
  }

  private void processPostponedBlocks() throws ConfigurationException {
    boolean isProcessed = false;
    do {
      isProcessed = false;
      for (final TestSequence entry : testProgram.getEntries()) {
        if (!testProgram.isPostponedEntry(entry)) {
          continue;
        }

        final Block block = testProgram.extractPostponedBlock(entry);
        InvariantChecks.checkNotNull(block);

        if (block.isExternal()) {
          isProcessed = processPostponedExternalBlock(block, entry);
        } else {
          isProcessed = processPostponedBlock(block, entry);
        }

        if (isProcessed) {
          break;
        }
      }
    } while (isProcessed);
  }

  private boolean processPostponedExternalBlock(final Block block, final TestSequence entry) throws ConfigurationException {
    final List<Call> abstractSequence = TestEngineUtils.getSingleSequence(block);

    int instanceIndex = findInstanceAtEndOfTestSequence(testProgram.getPrevAllocatedEntry(entry));
    if (-1 == instanceIndex) {
      instanceIndex = findInstanceJumpIntoSequence(abstractSequence);
    }

    if (-1 == instanceIndex) {
      Logger.debug("Processing of external code defined at %s is postponed again.", block.getWhere());
      testProgram.addPostponedEntry(block);
      return false;
    }

    if (testProgram.isPostponedEntry(testProgram.getLastEntry()) &&
        !TestEngineUtils.isOriginFixed(abstractSequence)) {
      throw new GenerationAbortedException(String.format(
          "External code defined at %s must have fixed origin.", block.getWhere()));
    }

    engineContext.getModel().setActivePE(instanceIndex);
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);
    final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);

    final TestSequence sequence = TestEngineUtils.getSingleTestSequence(iterator);
    sequence.setTitle("External Code");

    allocateData(sequence, Label.NO_SEQUENCE_INDEX);
    allocator.allocateSequence(sequence, Label.NO_SEQUENCE_INDEX);
    testProgram.replaceEntryWith(entry, sequence);
    PrinterUtils.printSequenceToConsole(engineContext, sequence);

    executeTestSequence(sequence);

    engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
    return true;
  }

  private boolean processPostponedBlock(
      final Block block, final TestSequence entry) throws ConfigurationException {
    final int instanceIndex =
        findInstanceAtEndOfTestSequence(testProgram.getPrevAllocatedEntry(entry));

    if (-1 == instanceIndex) {
      Logger.debug("Processing of block defined at %s is postponed again.", block.getWhere());
      return false;
    }

    final Executor.Status status = executorStatuses.get(instanceIndex);
    allocator.setAddress(status.getAddress());

    engineContext.getModel().setActivePE(instanceIndex);
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);

    final Iterator<List<Call>> abstractIt = block.getIterator();
    for (abstractIt.init(); abstractIt.hasValue(); abstractIt.next()) {
      final Iterator<AdapterResult> concreteIt =
          engine.process(engineContext, abstractIt.value());

      for (concreteIt.init(); concreteIt.hasValue(); concreteIt.next()) {
        final TestSequence sequence = TestEngineUtils.getTestSequence(concreteIt.value());
        final int sequenceIndex = engineContext.getStatistics().getSequences();
        sequence.setTitle(String.format("Test Case %d (%s)", sequenceIndex, block.getWhere()));

        allocateData(sequence, sequenceIndex);
        allocator.allocateSequence(sequence, sequenceIndex);
        testProgram.replaceEntryWith(entry, sequence);
        PrinterUtils.printSequenceToConsole(engineContext, sequence);

        executeTestSequence(sequence);
        processSelfChecks(sequence.getChecks(), sequenceIndex);

        engineContext.getStatistics().incSequences();
        Logger.debugHeader("");
        engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
      } // Concrete sequence iterator
    } // Abstract sequence iterator

    return true;
  }

  private void startProgram() throws IOException, ConfigurationException {
    if (isProgramStarted) {
      return;
    }

    isProgramStarted = true;

    Tarmac.createFile();
    allocator.init();

    if (engineContext.getStatistics().getPrograms() > 0) {
      // Allocates global data created during generation of previous test programs
      reallocateGlobalData();
    }

    allocator.allocateHandlers(testProgram.getExceptionHandlers());

    final TestSequence prologue = testProgram.getPrologue();
    allocateTestSequence(prologue, Label.NO_SEQUENCE_INDEX);

    executeTestSequence(prologue);
    engineContext.getStatistics().incInstructions(prologue.getInstructionCount());

    notifyProgramStart();
  }

  private void finishProgram() throws ConfigurationException, IOException {
    try {
      startProgram();
      notifyProgramEnd();

      final TestSequence sequence = TestEngineUtils.makeTestSequenceForExternalBlock(
          engineContext, testProgram.getEpilogue());

      sequence.setTitle("Epilogue");
      engineContext.getStatistics().incInstructions(sequence.getInstructionCount());

      allocateTestSequence(sequence, Label.NO_SEQUENCE_INDEX);
      executeTestSequence(sequence);

      PrinterUtils.printTestProgram(engineContext, testProgram);
   } finally {
      Tarmac.closeFile();

      // Clean up all the state
      engineContext.getModel().resetState();
      engineContext.getLabelManager().reset();
      allocator.reset();
      testProgram.reset();
      executorStatuses.clear();

      isProgramStarted = false;
    }
  }

  private void notifyProgramStart() {
    for (final Engine<?> engine : GeneratorConfig.get().getEngines()) {
      engine.onStartProgram();
    }

    for (final Adapter<?> adapter : GeneratorConfig.get().getAdapters()) {
      adapter.onStartProgram();
    }
  }

  private void notifyProgramEnd() {
    for (final Engine<?> engine : GeneratorConfig.get().getEngines()) {
      engine.onEndProgram();
    }

    for (final Adapter<?> adapter : GeneratorConfig.get().getAdapters()) {
      adapter.onEndProgram();
    }
  }

  private void allocateTestSequence(
      final TestSequence sequence,
      final int sequenceIndex) throws ConfigurationException {
    allocateData(sequence, sequenceIndex);

    final TestSequence lastAllocated = testProgram.getLastAllocatedEntry();
    if (null != lastAllocated) {
     allocator.setAddress(lastAllocated.getEndAddress());
    }

    allocator.allocateSequence(sequence, sequenceIndex);
    testProgram.addEntry(sequence);
    PrinterUtils.printSequenceToConsole(engineContext, sequence);
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
   * @param currentSequence The most recently allocated sequence which is expected to be executed
   *                        by some of the threads.
   */
  private void executeTestSequence(final TestSequence currentSequence) {
    Logger.debugHeader("Executing %s", currentSequence.getTitle());
    if (engineContext.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
      Logger.debug("Simulation is disabled");
      return;
    }

    // Nothing to execute (no new code was allocated).
    if (currentSequence.isEmpty()) {
      return;
    }

    final boolean isNoStatuses = executorStatuses.isEmpty();
    final long currentStartAddress = currentSequence.getStartAddress();
    final TestSequence previousSequence = testProgram.getPrevAllocatedEntry(currentSequence);

    for (int index = 0; index < instanceNumber; index++) {
      Logger.debugHeader("Instance %d", index);

      // Sets initial statuses (address of first sequence in a program).
      if (isNoStatuses) {
        executorStatuses.add(Executor.Status.newAddress(currentStartAddress));
      }

      final Executor.Status oldStatus = executorStatuses.get(index);
      Logger.debug("Execution status: %s%n", oldStatus);

      if (oldStatus.isLabelReference()) {
        final Label label = oldStatus.getLabelReference().getReference();
        final LabelManager.Target target = engineContext.getLabelManager().resolve(label);
        if (null != target) {
          oldStatus.getLabelReference().setTarget(target);
        }
      }

      if (!isAllocatedTarget(oldStatus) && !isEndOfTestSequence(oldStatus, previousSequence)) {
        Logger.debug("Execution cannot be run at the current stage.");
        continue;
      }

      final long address = oldStatus.isAddress() ?
          oldStatus.getAddress() :
          oldStatus.getLabelReference().getTarget().getAddress();

      engineContext.getModel().setActivePE(index);
      final Executor.Status newStatus = executor.execute(allocator.getCode(), address);
      executorStatuses.set(index, newStatus);

      /*
      if (newStatus.isLabelReference()) {
        throw new GenerationAbortedException(String.format(
            "Label '%s' is undefined or unavailable in the current execution scope.",
            newStatus.getLabelReference().getReference().getName()));
      }
      */
    }
  }

  private boolean isAllocatedTarget(final Executor.Status status) {
    if (status.isLabelReference()) {
      return status.getLabelReference().getTarget() != null;
    }

    return allocator.getCode().hasAddress(status.getAddress());
  }

  private boolean isEndOfTestSequence(final Executor.Status status, final TestSequence sequence) {
    if (!status.isAddress()) {
      return false;
    }

    return sequence != null &&  sequence.getEndAddress() == status.getAddress();
  }

  private int findInstanceAtEndOfTestSequence(final TestSequence entry) {
    if (executorStatuses.isEmpty()) {
      // No instances started execution yet - return 0 (can select any)
      return 0; 
    }

    for (int index = 0; index < instanceNumber; index++) {
      final Executor.Status status = executorStatuses.get(index);
      if (isEndOfTestSequence(status, entry)) {
        // Found it!
        return index;
      }
    }

    // Nothing is found.
    return -1;
  }

  private int findInstanceJumpIntoSequence(final List<Call> sequence) {
    if (executorStatuses.isEmpty()) {
      // No instances started execution yet - none jumps to a label
      return -1;
    }

    for (int index = 0; index < instanceNumber; index++) {
      final Executor.Status status = executorStatuses.get(index);
      if (isJumpIntoSequence(status, sequence)) {
        // Found it!
        return index;
      }
    }

    // Nothing is found.
    return -1;
  }

  private static boolean isJumpIntoSequence(
      final Executor.Status status, final List<Call> sequence) {
    if (!status.isLabelReference()) {
      return false;
    }
    return TestEngineUtils.isLabelDefined(sequence, status.getLabelReference().getReference());
  }

  private void allocateData(final TestSequence sequence, final int sequenceIndex) {
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
    memoryAllocator.resetCurrentAddress();

    for (final DataSection data : testProgram.getGlobalData()) {
      data.allocate(memoryAllocator);
      data.registerLabels(engineContext.getLabelManager());
    }

    testProgram.readdGlobalData();
  }
}
