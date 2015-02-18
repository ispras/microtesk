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
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataManager;
import ru.ispras.microtesk.test.template.Template;

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

  /**
   * Processes sequence by sequence:
   * <ol>
   * <li>Generate data (create concrete calls).</li>
   * <li>Execute (simulate).</li>
   * <li>Print.</li>
   * </ol>
   */

  public void process(Template template) throws ConfigurationException, IOException {
    checkNotNull(template);

    final IIterator<Sequence<Call>> sequenceIt = template.getSequences();
    final DataGenerator dataGenerator = new DataGenerator(model, template.getPreparators());

    final IModelStateObserver observer = model.getStateObserver();
    final Executor executor = new Executor(observer, logExecution);
    final Printer printer = new Printer(fileName, observer, commentToken, printToScreen);
    final DataManager dataManager = template.getDataManager();

    try {
      printHeader("Printing Data Declarations");
      if (dataManager.containsDecls()) {
        final String declText = dataManager.getDeclText();
        printer.printText(declText);
        if (!printToScreen) {
          executor.logText(declText);
        }
      } else {
        executor.logText("<none>");
      }

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
}
