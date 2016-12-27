/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.engine.TestEngineUtils;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.SelfCheckEngine;
import ru.ispras.microtesk.test.sequence.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;
import ru.ispras.testbase.knowledge.iterator.Iterator;

final class TemplateProcessor implements Template.Processor {
  private final EngineContext engineContext;
  private final Executor executor;

  private Printer printer;
  private boolean needCreateNewFile = true;

  private TestSequence prologue = null;
  private Block epilogueBlock = null;
  private ExecutorCode executorCode = null;

  public TemplateProcessor(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);

    this.engineContext = engineContext;
    this.executor = new Executor(engineContext);
    this.printer = null;
  }

  @Override
  public void process(final Section section, final Block block) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(block);

    engineContext.getStatistics().pushActivity(Statistics.Activity.SEQUENCING);

    try {
      if (section == Section.PRE) {
        prologue = TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);
      } else if (section == Section.POST) {
        epilogueBlock = block;
      } else if (block.isExternal()) {
        processExternalBlock(block);
      } else {
        processBlock(block);
      }
    } catch (final ConfigurationException | IOException e) {
      throw new GenerationAbortedException(e.getMessage());
    } finally {
      engineContext.getStatistics().popActivity(); // SEQUENCING
    }
  }

  @Override
  public void finish() {
    try {
      finishFile();

      if (!needCreateNewFile) {
        //No instructions were added to the newly created file, it must be deleted
        if (engineContext.getStatistics().getProgramLength() == 0) {
          printer.delete();
          engineContext.getStatistics().decPrograms();
        }
      }

      Logger.debugHeader("Ended Processing Template");
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException(e.getMessage());
    } finally {
      engineContext.getStatistics().popActivity(); // PARSING
      engineContext.getStatistics().saveTotalTime();
    }
  }

  private void processExternalBlock(final Block block) throws ConfigurationException, IOException {
    if (needCreateNewFile) {
      startFile();
      needCreateNewFile = false;
    }

    final TestSequence sequence =
        TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, block);

    processTestSequence(sequence, "External Code", true, Label.NO_SEQUENCE_INDEX, true);

    if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
      finishFile();
      needCreateNewFile = true;
    }
  }

  private void processBlock(final Block block) throws ConfigurationException, IOException {
    final Iterator<List<Call>> abstractIt = block.getIterator();
    final TestSequenceEngine engine = TestEngineUtils.getEngine(block);

    for (abstractIt.init(); abstractIt.hasValue(); abstractIt.next()) {
      final Iterator<AdapterResult> concreteIt =
          engine.process(engineContext, abstractIt.value());

      for (concreteIt.init(); concreteIt.hasValue(); concreteIt.next()) {
        if (needCreateNewFile) {
          startFile();
          needCreateNewFile = false;
        }

        final TestSequence sequence = TestEngineUtils.getTestSequence(concreteIt.value());
        final int sequenceIndex = engineContext.getStatistics().getSequences();

        final String sequenceId =
            String.format("Test Case %d (%s)", sequenceIndex, block.getWhere());

        processTestSequence(sequence, sequenceId, false, sequenceIndex, true);
        processSelfChecks(sequence.getChecks(), sequenceIndex);

        engineContext.getStatistics().incSequences();
        Logger.debugHeader("");

        if (engineContext.getStatistics().isFileLengthLimitExceeded()) {
          finishFile();
          needCreateNewFile = true;
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
    processTestSequence(selfCheckSequence, sequenceId, true, testCaseIndex, false);
  }

  private void processTestSequence(
      final TestSequence sequence,
      final String sequenceId,
      final boolean isExternal,
      final int sequenceIndex,
      final boolean abortOnUndefinedLabel) throws ConfigurationException {
    Logger.debugHeader("Constructed %s", sequenceId);
    //printer.printSequence(null, engineContext.getModel().getPE(), sequence);

    Logger.debugHeader("Executing %s", sequenceId);
    executor.execute(
        executorCode,
        sequence.getAll(),
        sequenceIndex,
        abortOnUndefinedLabel
        );

    Logger.debugHeader("Printing %s to %s", sequenceId, printer.getFileName());
    printer.printSequence(engineContext.getModel().getPE(), sequence, sequenceId);
  }

  private void startFile() throws IOException, ConfigurationException {
    printer = Printer.newCodeFile(engineContext.getOptions(), engineContext.getStatistics());
    Tarmac.createFile();

    engineContext.getStatistics().incPrograms();

    // Allocates global data created during generation of previous test programs
    if (engineContext.getStatistics().getPrograms() > 1 &&
        engineContext.getDataManager().containsDecls()) {
      engineContext.getDataManager().reallocateGlobalData();
    }

    executorCode = new ExecutorCode();
    processTestSequence(prologue, "Prologue", true, Label.NO_SEQUENCE_INDEX, true);
  }

  private void finishFile() throws ConfigurationException {
    try {
      final TestSequence sequence =
          TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, epilogueBlock);

      processTestSequence(sequence, "Epilogue", true, Label.NO_SEQUENCE_INDEX, true);

      if (engineContext.getDataManager().containsDecls()) {
        engineContext.getDataManager().printData(printer);
      }
    } finally {
      printer.close();
      Tarmac.closeFile();

      // Clean up all the state
      engineContext.getDataManager().resetLocalData();
      engineContext.getModel().resetState();
      engineContext.getLabelManager().reset();
      executorCode = null;

      // Sets the starting address for instruction allocation after the prologue
      engineContext.setAddress(prologue.getEndAddress());
    }
  }

  @Override
  public void defineExceptionHandler(final ExceptionHandler handler) {
    Printer printer = null; 

    try { 
      printer = Printer.newExcHandlerFile(engineContext.getOptions(), engineContext.getStatistics(), handler.getId());
    } catch (final IOException e) {
      throw new GenerationAbortedException(
          String.format("Failed to generate data file. Reason: %s", e.getMessage()));
    }

    Logger.debugHeader("Processing Exception Handler (%s)", printer.getFileName());
    InvariantChecks.checkNotNull(handler);

    try {
      final Map<String, List<ConcreteCall>> handlers = new LinkedHashMap<>();
      for (final ExceptionHandler.Section section : handler.getSections()) {
        final TestSequence concreteSequence =
            TestEngineUtils.makeTestSequenceForExceptionHandler(engineContext, section);

        final List<ConcreteCall> handlerCalls = concreteSequence.getAll();
        for (final String exception : section.getExceptions()) {
          if (null != handlers.put(exception, handlerCalls)) {
            Logger.warning("Exception handler for %s is redefined.", exception);
          }
        }

        printer.printSequence(engineContext.getModel().getPE(), concreteSequence, "");
      }

      executor.setExceptionHandlers(handlers);
    } catch (final ConfigurationException e) { 
      Logger.error(e.getMessage());
    } finally {
      printer.close();
      Logger.debugBar();
    }
  }
}
