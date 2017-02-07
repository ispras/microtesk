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
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
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

  private void rethrowException(final Exception e) {
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
    // Allocation:
    //   Follows after another allocated sequence
    //   Has fixed origin
    // Execution:
    //   At least one thread points start address
    //   At least one thread points a label in this block

    startProgram();

    final TestSequence sequence =
        TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);
    sequence.setTitle("External Code");

    allocateTestSequence(sequence, Label.NO_SEQUENCE_INDEX);
    executeTestSequence(sequence);

    engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
    if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
      finishProgram();
    }
  }

  private void processBlock(final Block block) throws ConfigurationException, IOException {
    // Allocation:
    //   Follows after another allocated sequence
    //   At least one thread points start address
    // Execution:
    //   At least one thread points start address

    final Iterator<List<Call>> abstractIt = block.getIterator();
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);

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
  }

  private void finishProgram() throws ConfigurationException, IOException {
    try {
      startProgram();

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

  private void allocateTestSequence(
      final TestSequence sequence,
      final int sequenceIndex) throws ConfigurationException {
    allocateData(sequence, sequenceIndex);
    allocator.allocateSequence(sequence, sequenceIndex);
    testProgram.getEntries().add(sequence);
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

    final TestSequence previousSequence =
        testProgram.getEntries().getPrevious(currentSequence);

    final long previousEndAddress = null != previousSequence && !previousSequence.isEmpty() ?
        previousSequence.getEndAddress() : currentStartAddress;

    for (int index = 0; index < instanceNumber; index++) {
      Logger.debugHeader("Instance %d", index);

      // Sets initial statuses (address of first sequence in a program).
      if (isNoStatuses) {
        executorStatuses.add(Executor.Status.newAddress(currentStartAddress));
      }

      final Executor.Status previousStatus = executorStatuses.get(index);
      Logger.debug("Execution status: %s%n", previousStatus);

      if (!isReadyForExecution(previousStatus, previousEndAddress)) {
        Logger.debug("Execution cannot be run at the current stage.");
        continue;
      }

      final long address = previousStatus.isAddress() ?
          previousStatus.getAddress() :
          previousStatus.getLabelReference().getTarget().getAddress();

      engineContext.getModel().setActivePE(index);
      final Executor.Status status = executor.execute(allocator.getCode(), address);
      executorStatuses.set(index, status);

      if (status.isLabelReference()) {
        throw new GenerationAbortedException(String.format(
            "Label '%s' is undefined or unavailable in the current execution scope.",
            status.getLabelReference().getReference().getName()));
      }
    }
  }

  /**
   * Checks whether a thread can resume execution.
   * 
   * <p>Conditions:
   * <ol>
   * <li>Thread stopped on an attempt to jump to an undefined label which is now allocated.</li>
   * <li>Thread stopped at an address which is now allocated.</li>
   * <li>Thread stopped at the end of the code block preceding the most recently allocated
   *     code block. In this case, it must resume even if the address has no executable code.</li>
   * </ol>
   * 
   * @param status Thread status to be checked.
   * @param precedingAddress Address of the end of the code block preceding the most recently
   *        allocated code block. 
   * @return {@code true} if the thread can resume execution or {@code false} otherwise.
   */
  private boolean isReadyForExecution(final Executor.Status status, final long precedingAddress) {
    if (status.isLabelReference()) {
      return status.getLabelReference().getTarget() != null;
    }

    InvariantChecks.checkTrue(status.isAddress());
    final long address = status.getAddress();

    if (address == precedingAddress) {
      return true;
    }

    return allocator.getCode().hasAddress(address);
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
  }
}
