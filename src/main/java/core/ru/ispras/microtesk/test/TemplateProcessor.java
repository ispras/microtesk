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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;
import ru.ispras.testbase.knowledge.iterator.Iterator;

final class TemplateProcessor implements Template.Processor {
  private final EngineContext engineContext;
  private final Executor executor;

  private Printer printer = null;
  private final List<Map<String, TestSequence>> exceptionHandlers = new ArrayList<>();
  private TestSequence prologue = null;
  private Block epilogueBlock = null;
  private ExecutorCode executorCode = null;

  public TemplateProcessor(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);

    this.engineContext = engineContext;
    this.executor = new Executor(engineContext);
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
    // Code allocation actions
    final List<ConcreteCall> calls = sequence.getAll();

    allocateDataSections(engineContext.getLabelManager(), calls, sequenceIndex);
    registerLabels(engineContext.getLabelManager(), calls, sequenceIndex);
    patchLabels(engineContext.getLabelManager(), calls, sequenceIndex, abortOnUndefinedLabel);

    final int startIndex = executorCode.getCallCount();
    executorCode.addTestSequence(sequence);
    final int endIndex = executorCode.getCallCount() - 1;

    if (engineContext.getOptions().getValueAsBoolean(Option.VERBOSE)) {
      Logger.debugHeader("Constructed %s", sequenceId);
      final Printer consolePrinter =
          Printer.getConsole(engineContext.getOptions(), engineContext.getStatistics());
      consolePrinter.printSequence(engineContext.getModel().getPE(), sequence, "");
    }

    Logger.debugHeader("Executing %s", sequenceId);
    if (startIndex <= endIndex) { // Otherwise it's empty
      executor.execute(
          executorCode,
          sequence.getStartAddress(),
          sequence.getEndAddress(),
          startIndex,
          endIndex
          );
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

    executorCode = new ExecutorCode();
    reallocateGlobalData();

    registerExceptionHandlers(
        executorCode, engineContext.getLabelManager(), exceptionHandlers);

    processTestSequence(prologue, "Prologue", Label.NO_SEQUENCE_INDEX, true);
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

      final TestSequence sequence =
          TestEngineUtils.makeTestSequenceForExternalBlock(engineContext, epilogueBlock);

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
      executorCode = null;

      // Sets the starting address for instruction allocation after the prologue
      engineContext.setAddress(prologue.getEndAddress());
    }
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

    exceptionHandlers.add(concreteHandler.second);

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

  private static void registerExceptionHandlers(
      final Executor.ICode code,
      final LabelManager labelManager,
      final List<Map<String, TestSequence>> exceptionHandlers) {
    for (final Map<String, TestSequence> handler: exceptionHandlers) {
      final Set<Object> handlerSet = new HashSet<>();
      for (final Map.Entry<String, TestSequence> e : handler.entrySet()) {
        final String handlerName = e.getKey();
        final TestSequence handlerSequence = e.getValue();

        if (handlerSequence.isEmpty()) {
          Logger.warning("Empty exception handler: %s", handlerName);
          continue;
        }

        code.addHandlerAddress(handlerName, handlerSequence.getStartAddress());

        if (!handlerSet.contains(handlerSequence)) {
          final List<ConcreteCall> handlerCalls = e.getValue().getAll();
          registerLabels(labelManager, handlerCalls, Label.NO_SEQUENCE_INDEX);
          patchLabels(labelManager, handlerCalls, Label.NO_SEQUENCE_INDEX, true);

          code.addTestSequence(handlerSequence);
          handlerSet.add(handlerSequence);
        }
      }
    }
  }

  private static void registerLabels(
      final LabelManager labelManager,
      final List<ConcreteCall> calls,
      final int sequenceIndex) {
    for (final ConcreteCall call : calls) {
      labelManager.addAllLabels(
          call.getLabels(),
          call.getAddress(),
          sequenceIndex
          );
    }
  }

  private void allocateDataSections(
      final LabelManager labelManager,
      final List<ConcreteCall> calls,
      final int sequenceIndex) {
    for (final ConcreteCall call : calls) {
      if (call.getData() != null) {
        final DataSection data = call.getData();
        data.setSequenceIndex(sequenceIndex);
        engineContext.getDataManager().processData(labelManager, data);
      }
    }
  }

  private static void patchLabels(
      final LabelManager labelManager,
      final List<ConcreteCall> calls,
      final int sequenceIndex,
      final boolean abortOnUndefined) {
    // Resolves all label references and patches the instruction call text accordingly.
    for (final ConcreteCall call : calls) {
      // Resolves all label references and patches the instruction call text accordingly.
      for (final LabelReference labelRef : call.getLabelReferences()) {
        labelRef.resetTarget();

        final Label source = labelRef.getReference();
        source.setSequenceIndex(sequenceIndex);

        final LabelManager.Target target = labelManager.resolve(source);

        final String uniqueName;
        final String searchPattern;
        final String patchedText;

        if (null != target) { // Label is found
          labelRef.setTarget(target);

          uniqueName = target.getLabel().getUniqueName();
          final long address = target.getAddress();

          if (null != labelRef.getArgumentValue()) {
            searchPattern = String.format("<label>%d", labelRef.getArgumentValue());
          } else {
            labelRef.getPatcher().setValue(BigInteger.ZERO);
            searchPattern = "<label>0";
          }

          patchedText = call.getText().replace(searchPattern, uniqueName);
          labelRef.getPatcher().setValue(BigInteger.valueOf(address));
        } else { // Label is not found
          if (abortOnUndefined) {
            throw new GenerationAbortedException(String.format(
                "Label '%s' passed to '%s' (0x%x) is not defined or%n" +
                "is not accessible in the scope of the current test sequence.",
                source.getName(), call.getText(), call.getAddress()));
          }

          uniqueName = source.getName();
          searchPattern = "<label>0";

          patchedText = call.getText().replace(searchPattern, uniqueName);
        }

        call.setText(patchedText);
      }

      // Kill all unused "<label>" markers.
      if (null != call.getText()) {
        call.setText(call.getText().replace("<label>", ""));
      }
    }
  }
}
