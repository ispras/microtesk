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

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
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
      printDataDeclarations();

      printHeader("INITIALIZATION SECTION");
      processSequences(sequences.getPre().getIterator());

      printHeader("MAIN SECTION");
      for (Block mainBlock : sequences.getMain()) {
        processSequences(mainBlock.getIterator());
      }

      printHeader("FINALIZATION SECTION");
      processSequences(sequences.getPost().getIterator());
    }

    private void printDataDeclarations() {
      printHeader("DATA DECLARATIONS");
      if (dataManager.containsDecls()) {
        final String declText = dataManager.getDeclText();
        printer.printText(declText);
        if (!printer.isPrintToScreenEnabled()) {
          executor.logText(declText);
        }
      } else {
        executor.logText("<none>");
      }
    }
    
    private void processSequences(final IIterator<Sequence<Call>> sequenceIt)
        throws ConfigurationException {

      int sequenceNumber = 1;
      sequenceIt.init();
      while (sequenceIt.hasValue()) {
        final Sequence<Call> abstractSequence = sequenceIt.value();

        printHeader("Generating data for sequence %d", sequenceNumber);
        final Sequence<ConcreteCall> concreteSequence = dataGenerator.process(abstractSequence);

        printHeader("Executing sequence %d", sequenceNumber);
        executor.executeSequence(concreteSequence);

        printHeader("Printing sequence %d", sequenceNumber);
        printer.printSequence(concreteSequence);

        sequenceIt.next();
        sequenceNumber++;
      }
    }
  }
}
