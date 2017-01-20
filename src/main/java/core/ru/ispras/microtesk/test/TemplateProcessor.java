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
  private Printer printer;

  public TemplateProcessor(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);

    this.engineContext = engineContext;
    this.testProgram = new TestProgram();
    this.allocator = new CodeAllocator(engineContext);
    this.executor = new Executor(engineContext);
    this.printer = null;
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

    testProgram.addExceptionHandlers(concreteHandler.second);

    Printer printer = null;
    try { 
      printer = Printer.newExcHandlerFile(engineContext.getOptions(), engineContext.getStatistics(), handler.getId());
      for (final TestSequence sequence : concreteHandler.first) {
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
        testProgram.setPrologue(
            TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block));
      } else if (section == Section.POST) {
        testProgram.setEpilogue(block);
      } else if (block.isExternal()) {
        processExternalBlock(block);
      } else {
        processBlock(block);
      }
    } catch (final Exception e) {
      if (null != printer) {
        printer.close();
        printer.delete();
        printer = null;
      }

      throw e instanceof GenerationAbortedException ? (GenerationAbortedException) e :
                                                      new GenerationAbortedException(e);
    } finally {
      engineContext.getStatistics().popActivity(); // SEQUENCING
    }
  }

  @Override
  public void finish() {
    try {
      finishFile();
      Logger.debugHeader("Ended Processing Template");
    } catch (final Exception e) {
      if (null != printer) {
        printer.close();
        printer.delete();
        printer = null;
      }

      throw e instanceof GenerationAbortedException ? (GenerationAbortedException) e :
                                                      new GenerationAbortedException(e);
    } finally {
      engineContext.getStatistics().popActivity(); // PARSING
      engineContext.getStatistics().saveTotalTime();
    }
  }

  private void processExternalBlock(final Block block) throws ConfigurationException, IOException {
    startFile();

    final TestSequence sequence =
        TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);

    processTestSequence(sequence, "External Code", Label.NO_SEQUENCE_INDEX, true);

    if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
      finishFile();
    }
  }

  private void processBlock(final Block block) throws ConfigurationException, IOException {
    final Iterator<List<Call>> abstractIt = block.getIterator();
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);

    for (abstractIt.init(); abstractIt.hasValue(); abstractIt.next()) {
      final Iterator<AdapterResult> concreteIt =
          engine.process(engineContext, abstractIt.value());

      for (concreteIt.init(); concreteIt.hasValue(); concreteIt.next()) {
        startFile();

        final TestSequence sequence = TestEngineUtils.getTestSequence(concreteIt.value());
        final int sequenceIndex = engineContext.getStatistics().getSequences();

        final String sequenceId =
            String.format("Test Case %d (%s)", sequenceIndex, block.getWhere());

        processTestSequence(sequence, sequenceId, sequenceIndex, true);
        processSelfChecks(sequence.getChecks(), sequenceIndex);

        engineContext.getStatistics().incSequences();
        Logger.debugHeader("");

        if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
          finishFile();
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

    final TestSequence selfCheckSequence = SelfCheckEngine.solve(engineContext, selfChecks);
    processTestSequence(selfCheckSequence, sequenceId, testCaseIndex, false);
  }

  private void processTestSequence(
      final TestSequence sequence,
      final String sequenceId,
      final int sequenceIndex,
      final boolean abortOnUndefinedLabel) throws ConfigurationException {
    printSequenceToConsole(sequence, sequenceId);

    Logger.debugHeader("Executing %s", sequenceId);
    if (!sequence.isEmpty()) {
      allocator.allocateSequence(sequence, sequenceIndex);

      final long startAddress = sequence.getAll().get(0).getAddress();
      final long endAddress = engineContext.getAddress();

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

    Logger.debugHeader("Printing %s to %s", sequenceId, printer.getFileName());
    printer.printSequence(engineContext.getModel().getPE(), sequence, sequenceId);
  }

  private void startFile() throws IOException, ConfigurationException {
    if (null != printer) {
      return;
    }

    printer = Printer.newCodeFile(engineContext.getOptions(), engineContext.getStatistics());
    Tarmac.createFile();

    allocator.init();
    reallocateGlobalData();

    allocator.allocateHandlers(testProgram.getExceptionHandlers());
    processTestSequence(testProgram.getPrologue(), "Prologue", Label.NO_SEQUENCE_INDEX, true);
  }

  private void reallocateGlobalData() {
    // Allocates global data created during generation of previous test programs
    if (engineContext.getStatistics().getPrograms() > 1 &&
        engineContext.getDataManager().containsDecls()) {
      engineContext.getDataManager().reallocateGlobalData();
    }
  }

  private void finishFile() throws ConfigurationException, IOException {
    try {
      startFile();

      final TestSequence sequence = TestEngineUtils.makeTestSequenceForExternalBlock(
          engineContext, testProgram.getEpilogue());

      processTestSequence(sequence, "Epilogue", Label.NO_SEQUENCE_INDEX, true);

      if (engineContext.getDataManager().containsDecls()) {
        engineContext.getDataManager().printData(printer);
      }

      if (null != printer) {
        printer.close();
        // If no instructions were added to the newly created file, it must be deleted
        if (engineContext.getStatistics().getProgramLength() == 0) {
          printer.delete();
        }
        printer = null;
      }
    } finally {
      Tarmac.closeFile();

      // Clean up all the state
      engineContext.getDataManager().resetLocalData();
      engineContext.getModel().resetState();
      engineContext.getLabelManager().reset();
      allocator.reset();

      engineContext.setAddress(
          engineContext.getOptions().getValueAsBigInteger(Option.BASE_VA).longValue());
    }
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
