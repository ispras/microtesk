/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.utils.PrintingUtils.printHeader;

import java.io.IOException;
import java.util.List;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.DataManager;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.TemplateProduct;

public final class TestEngine {
  public static TestEngine getInstance(IModel model) {
    return new TestEngine(model);
  }

  private final IModel model;

  // Settings
  private String fileName = null;
  private boolean logExecution = true;
  private boolean printToScreen = true;
  private String commentToken = "// ";

  private TestEngine(IModel model) {
    checkNotNull(model);
    this.model = model;
  }

  public Template newTemplate() {
    return new Template(model.getMetaData());
  }

  public void process(Template template) throws ConfigurationException, IOException {
    checkNotNull(template);

    final IModelStateObserver observer = model.getStateObserver();

    final Executor executor = new Executor(model.getStateObserver(), logExecution);
    final Printer printer = new Printer(fileName, observer, commentToken, printToScreen);

    final DataManager dataManager = template.getDataManager();
    final DataGenerator dataGenerator = new DataGenerator(model, template.getPreparators());

    final TemplateProcessor processor = new TemplateProcessor(
        template.getProduct(), executor, printer, dataManager, dataGenerator);

    try {
      processor.process();
    } finally {
      printer.close();
    }
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setLogExecution(boolean logExecution) {
    this.logExecution = logExecution;
  }

  public void setPrintToScreen(boolean printToScreen) {
    this.printToScreen = printToScreen;
  }

  public void setCommentToken(String commentToken) {
    this.commentToken = commentToken;
  }

  public void setRandomSeed(int seed) {
    Randomizer.get().setSeed(seed);
  }

  private static class TemplateProcessor {
    private final TemplateProduct sequences;
    private final Executor executor;
    private final Printer printer;
    private final DataManager dataManager;
    private final DataGenerator dataGenerator;

    private TemplateProcessor(
        TemplateProduct sequences, Executor executor, Printer printer,
        DataManager dataManager, DataGenerator dataGenerator) {

      this.sequences = sequences;
      this.executor = executor;
      this.printer = printer;
      this.dataManager = dataManager;
      this.dataGenerator = dataGenerator;
    }

    private void process() throws ConfigurationException {
      printer.printToolInfoToFile();
      printDataDeclarations();

      processOptionalSection("INITIALIZATION SECTION", sequences.getPre());
      processMainSection("MAIN SECTION (TEST CASES)", sequences.getMain());
      processOptionalSection("FINALIZATION SECTION", sequences.getPost());

      printHeader("GENERATION DONE");
    }

    private void printDataDeclarations() {
      final String title = "DATA DECLARATIONS";

      printHeader(title);
      if (dataManager.containsDecls()) {
        printer.printHeaderToFile(title);

        final String declText = dataManager.getDeclText();
        printer.printText(declText);
        if (!printer.isPrintToScreenEnabled()) {
          executor.logText(declText);
        }
      } else {
        executor.logText("<none>");
      }
    }

    private void processOptionalSection(
        String title, Block block) throws ConfigurationException {
      
      if (!block.isEmpty()) {
        printHeader(title);
        printer.printHeaderToFile(title);
        processSequences(block);
      }
    }

    private void processMainSection(
        String title, List<Block> blocks) throws ConfigurationException {

      printHeader(title);
      printer.printHeaderToFile(title);

      final List<Block> testCases = sequences.getMain();
      int testCaseIndex = 1;

      for (Block testCase : testCases) {
        final String testCaseTitle = String.format("Test Case %s", testCaseIndex);

        printHeader(testCaseTitle);
        printer.printSeparatorToFile(testCaseTitle);

        processSequences(testCase);
        ++testCaseIndex;
      }
    }

    private void processSequences(Block sequences) throws ConfigurationException {
      final boolean isSingleSequence = sequences.isSingle();
      final IIterator<Sequence<Call>> sequenceIt = sequences.getIterator();

      int sequenceIndex = 1;
      sequenceIt.init();

      while (sequenceIt.hasValue()) {
        if (isSingleSequence && sequenceIndex > 1) {
          throw new IllegalStateException("Only a single sequence is allowed.");
        }

        final String sequenceId = String.format("Sequence %d", sequenceIndex);
        final Sequence<Call> abstractSequence = sequenceIt.value();

        printHeader("Generating Data%s", (isSingleSequence ? "" : " for " + sequenceId));
        final TestSequence concreteSequence = dataGenerator.process(abstractSequence);

        printHeader("Executing%s", (isSingleSequence ? "" : " " + sequenceId));
        executor.executeSequence(concreteSequence);

        printHeader("Printing%s", (isSingleSequence ? "" : " " + sequenceId));
        if (!isSingleSequence) {
          printer.printNewLineToFile();
          printer.printSeparatorToFile();
          printer.printCommentToFile(String.format("%s:", sequenceId));
          printer.printNewLineToFile();
        }
        printer.printSequence(concreteSequence);

        sequenceIt.next();
        ++sequenceIndex;
      }
    }
  }
}
