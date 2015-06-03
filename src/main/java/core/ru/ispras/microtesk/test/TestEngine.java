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

import java.io.File;
import java.io.IOException;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.solver.Environment;
import ru.ispras.fortress.solver.Solver;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.DataManager;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

public final class TestEngine {
  public static final class Statistics {
    public long instructionCount;
    public long instructionExecutedCount;
    public int testProgramNumber;
    public int initialTestProgramNumber;
    public int initialTestCaseNumber;
    public int testCaseNumber;

    private Statistics() {
      reset();
    }

    private Statistics(
        long instructionCount,
        long instructionExecutedCount,
        int testProgramNumber,
        int initialTestProgramNumber,
        int initialTestCaseNumber,
        int testCaseNumber) {
      this.instructionCount = instructionCount;
      this.instructionExecutedCount = instructionExecutedCount;
      this.testProgramNumber = testProgramNumber;
      this.initialTestProgramNumber = initialTestProgramNumber;
      this.initialTestCaseNumber = initialTestCaseNumber;
      this.testCaseNumber = testCaseNumber;
    }

    public void reset() {
      instructionCount = 0;
      instructionExecutedCount = 0;
      testProgramNumber = 0;
      initialTestProgramNumber = 0;
      initialTestCaseNumber = 0;
      testCaseNumber = 0;
    }

    public Statistics copy() {
      return new Statistics(
          instructionCount, 
          instructionExecutedCount,
          testProgramNumber,
          initialTestProgramNumber,
          initialTestCaseNumber,
          initialTestCaseNumber
          );
    }
  }

  public static final Statistics STATISTICS = new Statistics();

  public static TestEngine getInstance(IModel model) {
    return new TestEngine(model);
  }

  private final IModel model;

  // Settings passed from a template
  private boolean logExecution = true;
  private boolean printToScreen = true;
  private String commentToken = "// ";

  // Settings from command line and configuration file
  private static int branchExecutionLimit = 100;
  private static String codeFileExtension = ".asm";
  private static String codeFilePrefix = "test";
  private static int programLengthLimit = 1000;
  private static int traceLengthLimit = 1000;

  // Architecture-specific settings
  private static GeneratorSettings settings;

  public static void setRandomSeed(int seed) {
    Randomizer.get().setSeed(seed);
  }

  public static void setSolver(final String solverName) {
    if ("z3".equalsIgnoreCase(solverName)) {
      TestBase.setSolverId(SolverId.Z3_TEXT);
    } else if ("cvc4".equalsIgnoreCase(solverName)) {
      TestBase.setSolverId(SolverId.CVC4_TEXT);
    } else {
      Logger.warning("Unknown solver: %s. Default solver will be used.", solverName);
    }
  }

  public static void setBranchExecutionLimit(final int value) {
    branchExecutionLimit = value;
  }

  public static void setCodeFileExtension(final String value) {
    codeFileExtension = value;
  }

  public static void setCodeFilePrefix(final String value) {
    codeFilePrefix = value;
  }

  public static void setProgramLengthLimit(final int value) {
    programLengthLimit = value;
  }

  public static void setTraceLengthLimit(final int value) {
    traceLengthLimit = value;
  }

  public static GeneratorSettings getGeneratorSettings() {
    return settings;
  }

  public static void setGeneratorSettings(final GeneratorSettings value) {
    settings = value;
  }

  public static void generate(final String modelName, final String templateFile) throws Throwable {
    final ScriptingContainer container = new ScriptingContainer();
    container.setArgv(new String[] {modelName, templateFile});

    final String scriptsPath = String.format(
        "%s/lib/ruby/microtesk.rb", System.getenv("MICROTESK_HOME"));

    try {
        try {
          container.runScriptlet(PathType.ABSOLUTE, scriptsPath);
        } catch(org.jruby.embed.EvalFailedException e) {
          // JRuby wraps exceptions that occur in Java libraries it calls into
          // EvalFailedException. To handle them correctly, we need to unwrap them.
          throw e.getCause();
        }
    } catch (GenerationAbortedException e) {
      Logger.error(e.getMessage());
    }
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

  public void setLogExecution(boolean logExecution) {
    this.logExecution = logExecution;
  }

  public void setPrintToScreen(boolean printToScreen) {
    this.printToScreen = printToScreen;
  }

  public void setCommentToken(String commentToken) {
    this.commentToken = commentToken;
  }

  public Template newTemplate() {
    final IModelStateObserver observer = model.getStateObserver();

    final Executor executor = new Executor(
        observer, logExecution, branchExecutionLimit);

    final Printer printer = new Printer(
        codeFilePrefix, codeFileExtension, observer, commentToken, printToScreen);

    final DataManager dataManager = new DataManager();
    final PreparatorStore preparators = new PreparatorStore();
    final DataGenerator dataGenerator = new DataGenerator(model, preparators);

    final TemplateProcessor processor = new TemplateProcessor(
        executor, printer, dataManager, dataGenerator, programLengthLimit, traceLengthLimit);

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

    private final int programLengthLimit;
    private final int traceLengthLimit;

    private boolean isDataPrinted = false;
    private int testIndex = 0; 

    private boolean needCreateNewFile;

    private TemplateProcessor(
        final Executor executor,
        final Printer printer,
        final DataManager dataManager,
        final DataGenerator dataGenerator,
        final int programLengthLimit,
        final int traceLengthLimit) {

      this.executor = executor;
      this.printer = printer;
      this.dataManager = dataManager;
      this.dataGenerator = dataGenerator;

      this.programLengthLimit = programLengthLimit;
      this.traceLengthLimit = traceLengthLimit;
      
      this.needCreateNewFile = true;
    }

    private Block preBlock = null;
    private Block postBlock = null;
    private Statistics before;
    private String fileName;

    @Override
    public void process(final Section section, final Block block) {
      final boolean isNewFile = needCreateNewFile;
      if (needCreateNewFile) {
        try {
          before = STATISTICS.copy();
          printer.close();
          fileName = printer.createNewFile();
          STATISTICS.testProgramNumber++;
        } catch (IOException e) {
          Logger.error(e.getMessage());
        }
        needCreateNewFile = false;
      }

      checkNotNull(section);
      checkNotNull(block);

      if (section == Section.PRE) {
        preBlock = block;
        return;
      }

      if (section == Section.POST) {
        postBlock = block;
        return;
      }

      if (!isDataPrinted && dataManager.containsDecls()) {
        printSectionHeader("Data Declarations");
        printer.printText(dataManager.getDeclText());
        isDataPrinted = true;
      }
      
      if (isNewFile) {
        if (!preBlock.isEmpty()) {
          try {
            printer.printHeaderToFile("Initialization Section");
            processBlock(preBlock);
          } catch (ConfigurationException e) {
            Logger.error(e.getMessage());
          }
        }
        printer.printHeaderToFile("Main Section (Tests)");
      }

      printer.printSeparatorToFile(String.format("Test %s", ++testIndex));
      STATISTICS.testCaseNumber++;

      try {
        processBlock(block);
      } catch (ConfigurationException e) {
        Logger.error(e.getMessage());
      }
      final Statistics after = STATISTICS.copy();

      final boolean isProgramLengthLimitExceeded =
          (after.instructionCount - before.instructionCount) >= programLengthLimit;
      final boolean isTraceLengthLimitExceeded =
          (after.instructionExecutedCount - before.instructionExecutedCount) >= traceLengthLimit;

       /*
       System.out.println(String.format("INSTRS: %d, %d, %d, %b%nEXECS: %d, %d, %d, %b",
              before.instructionCount, after.instructionCount,
              (after.instructionCount - before.instructionCount),
              (after.instructionCount - before.instructionCount) >= programLengthLimit,
              
              before.instructionExecutedCount,
              after.instructionExecutedCount,
              (after.instructionCount - before.instructionCount),
              (after.instructionCount - before.instructionCount) >= programLengthLimit
              ));
       */

      needCreateNewFile = isProgramLengthLimitExceeded || isTraceLengthLimitExceeded;
      if (needCreateNewFile) {
        if (!postBlock.isEmpty()) {
          try {
            printer.printHeaderToFile("Finalization Section");
            processBlock(postBlock);
          } catch (ConfigurationException e) {
            Logger.error(e.getMessage());
          }
        }
        printer.close();
      }
    }

    @Override
    public void finish() {
      if (!needCreateNewFile) {
        if (!postBlock.isEmpty()) {
          try {
            printer.printHeaderToFile("Finalization Section");
            processBlock(postBlock);
          } catch (ConfigurationException e) {
            Logger.error(e.getMessage());
          }
        }
        printer.close();

        // No instruction was added to the newly created file, it must be deleted
        if (STATISTICS.instructionCount == before.instructionCount) {
          new File(fileName).delete();
        }
      }

      printHeader("Ended Processing Template");
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

        STATISTICS.instructionCount += concreteSequence.getPrologue().size();
        STATISTICS.instructionCount += concreteSequence.getBody().size();

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
