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
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.SelfCheckEngine;
import ru.ispras.microtesk.test.sequence.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;
import ru.ispras.testbase.knowledge.iterator.Iterator;

final class TemplateProcessor implements Template.Processor {
  private final EngineContext engineContext;
  private final TestProgram testProgram;
  private final CodeAllocator allocator;
  private final Executor executor;
  private boolean isProgramStarted;

  public TemplateProcessor(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);

    this.engineContext = engineContext;
    this.testProgram = new TestProgram();
    this.allocator = new CodeAllocator(engineContext);
    this.executor = new Executor(engineContext);
    this.isProgramStarted = false;
  }

  @Override
  public void defineExceptionHandler(final ExceptionHandler handler) {
    Logger.debugHeader("Processing Exception Handler");
    InvariantChecks.checkNotNull(handler);

    final Pair<List<TestSequence>, Map<String, TestSequence>> concreteHandler;
    try {
      concreteHandler = TestEngineUtils.makeExceptionHandler(engineContext, handler);
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException(e);
    }

    testProgram.addExceptionHandlers(concreteHandler);

    Printer printer = null;
    try { 
      printer = Printer.newExcHandlerFile(engineContext.getOptions(), handler.getId());
      for (final TestSequence sequence : concreteHandler.first) {
        engineContext.getStatistics().incInstructions(sequence.getInstructionCount());
        printer.printSequence(engineContext.getModel().getPE(), sequence, "");
      }
    } catch (final ConfigurationException | IOException e) {
      throw new GenerationAbortedException(e);
    } finally {
      if (null != printer) {
        printer.close();
      }
      Logger.debugBar();
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
    engineContext.getDataManager().processData(engineContext.getLabelManager(), data);
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
    printSequenceToConsole(sequence, sequenceId);
    testProgram.addEntry(new TestProgramEntry(sequenceId, sequence));

    Logger.debugHeader("Executing %s", sequenceId);
    if (!sequence.isEmpty()) {
      allocator.allocateSequence(sequence, sequenceIndex);

      final long startAddress = sequence.getAll().get(0).getAddress();
      final long endAddress = allocator.getAddress();

      if (!engineContext.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
        for (int index = 0; index < engineContext.getModel().getPENumber(); index++) {
          Logger.debugHeader("Instance %d", index);
          engineContext.getModel().setActivePE(index);

          final Executor.Status status =
              executor.execute(allocator.getCode(), startAddress, endAddress);

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
      engineContext.getDataManager().reallocateGlobalData();
      // Adds the prologue instruction count to overall statistics.
      engineContext.getStatistics().incInstructions(testProgram.getPrologue().getInstructionCount());
    }

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

      printTestProgram();
      testProgram.clearEntries();
   } finally {
      Tarmac.closeFile();

      // Clean up all the state
      engineContext.getDataManager().resetLocalData();
      engineContext.getModel().resetState();
      engineContext.getLabelManager().reset();
      allocator.reset();

      isProgramStarted = false;
    }
  }

  private void printTestProgram() throws ConfigurationException, IOException {
    final Statistics statistics = engineContext.getStatistics(); 
    if (statistics.getProgramLength() == 0) {
      return;
    }

    statistics.pushActivity(Statistics.Activity.PRINTING);

    final int programIndex = statistics.getPrograms();
    final Printer printer = Printer.newCodeFile(engineContext.getOptions(), programIndex);

    try {
      statistics.incPrograms();

      for (int index = 0; index < testProgram.getEntryCount(); ++index) {
        final TestProgramEntry entry = testProgram.getEntry(index);
        final String sequenceId = entry.getSequenceId();
        Logger.debugHeader("Printing %s to %s", sequenceId, printer.getFileName());
        printer.printSequence(engineContext.getModel().getPE(), entry.getSequence(), sequenceId);
      }

      engineContext.getDataManager().printData(printer);
    } finally {
      printer.close();
    }

    engineContext.getStatistics().popActivity();
  }

  private void printSequenceToConsole(
      final TestSequence sequence,
      final String sequenceId) throws ConfigurationException {
    InvariantChecks.checkNotNull(sequence);
    InvariantChecks.checkNotNull(sequenceId);

    if (engineContext.getOptions().getValueAsBoolean(Option.VERBOSE)) {
      Logger.debugHeader("Constructed %s", sequenceId);

      final Printer consolePrinter =
          Printer.getConsole(engineContext.getOptions(), engineContext.getStatistics());

      consolePrinter.printSequence(
          engineContext.getModel().getPE(), sequence, "");
    }
  }
}
