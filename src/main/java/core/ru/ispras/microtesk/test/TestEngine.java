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

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.solver.Environment;
import ru.ispras.fortress.solver.Solver;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.DataManager;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;

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

  private static int executionLimit = 200;
  public static void setExecutionLimit(int value) {
    executionLimit = value;
  }

  private TestEngine(IModel model) {
    checkNotNull(model);
    this.model = model;

    final String HOME = System.getenv("MICROTESK_HOME");

    final String z3Path = (HOME != null ? HOME : ".") + "/tools/z3/";
    final Solver z3Solver = SolverId.Z3_TEXT.getSolver(); 

    if (Environment.isUnix()) {
      z3Solver.setSolverPath(z3Path + "unix/z3/bin/z3");
    } else if (Environment.isWindows()) {
      z3Solver.setSolverPath(z3Path + "windows/z3/bin/z3.exe");
    } else if (Environment.isOSX()) {
      z3Solver.setSolverPath(z3Path + "osx/z3/bin/z3");
    } else {
      throw new UnsupportedOperationException(String.format(
        "Unsupported platform: %s.", Environment.getOSName()));
    }

    final String cvc4Path = (HOME != null ? HOME : ".") + "/tools/cvc4/";
    final Solver cvc4Solver = SolverId.CVC4_TEXT.getSolver();

    if (Environment.isUnix()) {
      cvc4Solver.setSolverPath(cvc4Path + "unix/cvc4");
    } else if (Environment.isWindows()) {
      cvc4Solver.setSolverPath(cvc4Path + "windows/cvc4.exe");
    } else if (Environment.isOSX()) {
      cvc4Solver.setSolverPath(cvc4Path + "osx/cvc4");
    } else {
      throw new UnsupportedOperationException(String.format(
        "Unsupported platform: %s.", Environment.getOSName()));
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

  public Template newTemplate() throws IOException {
    final IModelStateObserver observer = model.getStateObserver();
    final Executor executor = new Executor(observer, logExecution, executionLimit);
    final Printer printer = new Printer(fileName, observer, commentToken, printToScreen);

    final DataManager dataManager = new DataManager();
    final PreparatorStore preparators = new PreparatorStore();
    final DataGenerator dataGenerator = new DataGenerator(model, preparators);

    final TemplateProcessor processor = 
        new TemplateProcessor(executor, printer, dataManager, dataGenerator);

    return new Template(
        model.getMetaData(), dataManager, preparators, processor);
  }

  public void process(Template template) throws ConfigurationException, IOException {
    template.getProcessor().finish();
  }

  private static class TemplateProcessor implements Template.Processor {
    private final Executor executor;
    private final Printer printer;
    private final DataManager dataManager;
    private final DataGenerator dataGenerator;

    private boolean isDataPrinted = false;
    private boolean isMainStarted = false;
    private int testIndex = 0; 

    private TemplateProcessor(
        Executor executor, Printer printer,
        DataManager dataManager, DataGenerator dataGenerator) {

      this.executor = executor;
      this.printer = printer;
      this.dataManager = dataManager;
      this.dataGenerator = dataGenerator;

      printer.printToolInfoToFile();
    }

    @Override
    public void process(Section section, Block block) {
      checkNotNull(section);
      checkNotNull(block);

      if (!isDataPrinted && dataManager.containsDecls()) {
        printSectionHeader("Data Declarations");
        printer.printText(dataManager.getDeclText());
        isDataPrinted = true;
      }

      if (block.isEmpty()) {
        return;
      }

      switch(section) {
        case PRE:
          printer.printHeaderToFile("Initialization Section");
          break;

        case POST:
          printer.printHeaderToFile("Finalization Section");
          break;

        case MAIN:
          if (!isMainStarted) {
            printer.printHeaderToFile("Main Section (Tests)");
            isMainStarted = true;
          } else {
            printer.printNewLineToFile();
          }
          printer.printSeparatorToFile(String.format("Test %s", ++testIndex));
          break;

        default:
          throw new IllegalArgumentException("Unknon section: " + section);
      }

      try {
        processBlock(block);
      } catch (ConfigurationException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void finish() {
      printHeader("Ended Processing Template");
      printer.close();
    }

    private void processBlock(Block block) throws ConfigurationException {
      final boolean isSingleSequence = block.isSingle();
      final Iterator<Sequence<Call>> sequenceIt = block.getIterator();

      int sequenceIndex = 1;
      sequenceIt.init();

      while (sequenceIt.hasValue()) {
        if (isSingleSequence && sequenceIndex > 1) {
          throw new IllegalStateException("Only a single sequence is allowed.");
        }

        final String sequenceId = String.format("Test Case %d", sequenceIndex);
        final Sequence<Call> abstractSequence = sequenceIt.value();

        printHeader("Generating Data%s", (isSingleSequence ? "" : " for " + sequenceId));
        final TestSequence concreteSequence = dataGenerator.process(abstractSequence);

        printHeader("Executing%s", (isSingleSequence ? "" : " " + sequenceId));
        executor.executeSequence(concreteSequence);

        printHeader("Printing%s", (isSingleSequence ? "" : " " + sequenceId));
        if (!isSingleSequence) {
          printer.printSubheaderToFile(String.format("%s:", sequenceId));
        }
        printer.printSequence(concreteSequence);

        sequenceIt.next();
        ++sequenceIndex;

        printHeader("");
      }
    }

    private void printSectionHeader(String title) {
      printHeader(title);
      printer.printHeaderToFile(title);
    }
  }
}
