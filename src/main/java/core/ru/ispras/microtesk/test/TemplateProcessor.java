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
      return; // FIXME: must be saved to
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
    if (e instanceof GenerationAbortedException) {
      throw (GenerationAbortedException) e;
    }

    if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    }

    throw new GenerationAbortedException(e);
  }

  private void processPrologue(final Block block) {
    final TestSequence sequence =
        TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);
    testProgram.setPrologue(sequence);
  }

  private void processEpilogue(final Block block) {
    testProgram.setEpilogue(block);
  }

  private void processExternalBlock(final Block block) throws ConfigurationException, IOException {
    startProgram();

    final TestSequence sequence =
        TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);

    processTestSequence(sequence, "External Code", Label.NO_SEQUENCE_INDEX, true);

    engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
    if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
      finishProgram();
    }
  }

  private void processBlock(final Block block) throws ConfigurationException, IOException {
    final Iterator<List<Call>> abstractIt = block.getIterator();
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);

    for (abstractIt.init(); abstractIt.hasValue(); abstractIt.next()) {
      final Iterator<AdapterResult> concreteIt =
          engine.process(engineContext, abstractIt.value());

      for (concreteIt.init(); concreteIt.hasValue(); concreteIt.next()) {
        startProgram();

        final TestSequence sequence = TestEngineUtils.getTestSequence(concreteIt.value());
        final int sequenceIndex = engineContext.getStatistics().getSequences();

        final String sequenceId =
            String.format("Test Case %d (%s)", sequenceIndex, block.getWhere());

        processTestSequence(sequence, sequenceId, sequenceIndex, true);
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
    engineContext.getStatistics().incInstructions(sequence.getInstructionCount());

    processTestSequence(sequence, sequenceId, testCaseIndex, false);
  }

  private void processTestSequence(
      final TestSequence sequence,
      final String sequenceId,
      final int sequenceIndex,
      final boolean abortOnUndefinedLabel) throws ConfigurationException {
    PrinterUtils.printSequenceToConsole(engineContext, sequence, sequenceId);
    testProgram.addEntry(new TestProgramEntry(sequenceId, sequence));

    Logger.debugHeader("Executing %s", sequenceId);
    if (!sequence.isEmpty()) {
      allocateData(sequence, sequenceIndex);
      allocator.allocateSequence(sequence, sequenceIndex);

      final long startAddress = sequence.getAll().get(0).getAddress();
      final long endAddress = allocator.getAddress();

      if (!engineContext.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
        for (int index = 0; index < instanceNumber; index++) {
          Logger.debugHeader("Instance %d", index);
          engineContext.getModel().setActivePE(index);

          final Code code = allocator.getCode();
          final Executor.Status status = executor.execute(code, startAddress, endAddress);
          executorStatuses.set(index, status);

          if (status.isLabelReference()) {
            throw new GenerationAbortedException(String.format(
                "Label '%s' is undefined or unavailable in the current execution scope.",
                status.getLabelReference().getReference().getName()));
          }
        }
      } else {
        Logger.debug("Simulation is disabled");
      }
    }
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

    // Resets execution statuses
    final boolean emptyExecutorStatuses = executorStatuses.isEmpty(); 
    for (int index = 0; index < instanceNumber; index++) {
      if (emptyExecutorStatuses) {
        executorStatuses.add(null);
      } else {
        executorStatuses.set(index, null);
      }
    }

    // Adds the prologue instruction count to overall statistics.
    engineContext.getStatistics().incInstructions(testProgram.getPrologue().getInstructionCount());

    allocator.allocateHandlers(testProgram.getExceptionHandlers());
    processTestSequence(testProgram.getPrologue(), "Prologue", Label.NO_SEQUENCE_INDEX, true);
  }

  private void finishProgram() throws ConfigurationException, IOException {
    try {
      startProgram();

      final TestSequence sequence = TestEngineUtils.makeTestSequenceForExternalBlock(
          engineContext, testProgram.getEpilogue());
      engineContext.getStatistics().incInstructions(sequence.getInstructionCount());

      processTestSequence(sequence, "Epilogue", Label.NO_SEQUENCE_INDEX, true);
      PrinterUtils.printTestProgram(engineContext, testProgram);
   } finally {
      Tarmac.closeFile();

      // Clean up all the state
      engineContext.getModel().resetState();
      engineContext.getLabelManager().reset();
      allocator.reset();
      testProgram.reset();

      isProgramStarted = false;
    }
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
