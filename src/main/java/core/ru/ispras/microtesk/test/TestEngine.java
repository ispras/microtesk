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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.solver.Environment;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.tarmac.LogPrinter;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.sequence.Configuration;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.sequence.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataManager;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.StreamStore;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

public final class TestEngine {
  public static final class Statistics {
    public long instructionCount;
    public long instructionExecutedCount;
    public int testProgramNumber;
    public int testCaseNumber;

    private Statistics() {
      reset();
    }

    private Statistics(
        long instructionCount,
        long instructionExecutedCount,
        int testProgramNumber,
        int testCaseNumber) {
      this.instructionCount = instructionCount;
      this.instructionExecutedCount = instructionExecutedCount;
      this.testProgramNumber = testProgramNumber;
      this.testCaseNumber = testCaseNumber;
    }

    public void reset() {
      instructionCount = 0;
      instructionExecutedCount = 0;
      testProgramNumber = 0;
      testCaseNumber = 0;
    }

    public Statistics copy() {
      return new Statistics(
          instructionCount, 
          instructionExecutedCount,
          testProgramNumber,
          testCaseNumber
          );
    }
  }

  public static final Statistics STATISTICS = new Statistics();

  private static TestEngine instance = null;
  public static TestEngine getInstance() {
    return instance;
  }

  private final IModel model;

  // Settings passed from a template
  private String indentToken = "\t";
  private String commentToken = "//";
  private String separatorToken = "=";

  // Settings from command line and configuration file
  private static int branchExecutionLimit = 100;
  private static String codeFileExtension = ".asm";
  private static String codeFilePrefix = "test";
  private static String dataFileExtension = ".dat";
  private static String dataFilePrefix = codeFilePrefix;
  private static int programLengthLimit = 1000;
  private static int traceLengthLimit = 1000;
  private static boolean commentsDebug = false;
  private static boolean commentsEnabled = false;
  private static boolean tarmacLog = false;

  // Architecture-specific settings
  private static GeneratorSettings settings;

  public IModel getModel() {
    return model;
  }

  public MetaModel getMetaModel() {
    return model.getMetaData();
  }

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

  public static void setDataFileExtension(final String value) {
    dataFileExtension = value;
  }

  public static void setDataFilePrefix(final String value) {
    dataFilePrefix = value;
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
    InvariantChecks.checkNotNull(value);

    settings = value;

    final AllocationSettings allocation = value.getAllocation();
    if (allocation != null) {
      ModeAllocator.init(allocation);
    }
  }

  public static void setCommentsDebug(boolean value) {
    commentsDebug = value;
  }

  public static void setCommentsEnabled(boolean value) {
    commentsEnabled = value;
  }

  public static void setTarmacLog(boolean value) {
    tarmacLog  = value;
  }

  public static Date generate(final String modelName, final String templateFile) throws Throwable {
    Logger.debug("Home: " + SysUtils.getHomeDir());
    Logger.debug("Current directory: " + SysUtils.getCurrentDir());
    Logger.debug("Model name: " + modelName);
    Logger.debug("Template file: " + templateFile);

    try {
      final IModel model = SysUtils.loadModel(modelName);
      if (null == model) {
        throw new GenerationAbortedException(
            String.format("Failed to load the %s model.", modelName));
      }

      instance = new TestEngine(model);

      final ScriptingContainer container = new ScriptingContainer();
      container.setArgv(new String[] {templateFile});

      final String scriptsPath = String.format(
          "%s/lib/ruby/microtesk.rb", SysUtils.getHomeDir());

        try {
          container.runScriptlet(PathType.ABSOLUTE, scriptsPath);
        } catch(org.jruby.embed.EvalFailedException e) {
          // JRuby wraps exceptions that occur in Java libraries it calls into
          // EvalFailedException. To handle them correctly, we need to unwrap them.
          throw e.getCause();
        }
    } catch (final GenerationAbortedException e) {
      Logger.message("Generation Aborted");
      Logger.error(e.getMessage());

      if (null != Printer.getLastFileName()) {
        new File(Printer.getLastFileName()).delete();
      }
      STATISTICS.testProgramNumber--;
    }

    return TemplateProcessor.start;
  }

  private TestEngine(IModel model) {
    checkNotNull(model);
    this.model = model;

    final String home = SysUtils.getHomeDir();

    final String z3Path = (home != null ? home : ".") + "/tools/z3/";
    final ru.ispras.fortress.solver.Solver z3Solver = SolverId.Z3_TEXT.getSolver(); 

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

    final String cvc4Path = (home != null ? home : ".") + "/tools/cvc4/";
    final ru.ispras.fortress.solver.Solver cvc4Solver = SolverId.CVC4_TEXT.getSolver();

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

  public void setIndentToken(String indentToken) {
    this.indentToken = indentToken;
  }

  public void setCommentToken(String commentToken) {
    this.commentToken = commentToken;
  }

  public void setSeparatorToken(String separatorToken) {
    this.separatorToken = separatorToken;
  }

  public Template newTemplate() {
    final IModelStateObserver observer = model.getStateObserver();

    final LogPrinter logPrinter = tarmacLog ?
        new LogPrinter(codeFilePrefix) : null;

    final Executor executor = new Executor(
        observer, branchExecutionLimit, logPrinter);

    final Printer printer = new Printer(
        codeFilePrefix,
        codeFileExtension,
        observer,
        indentToken,
        commentToken,
        separatorToken,
        commentsEnabled,
        commentsDebug
        );

    final DataManager dataManager = new DataManager(
        indentToken, dataFilePrefix, dataFileExtension);

    final PreparatorStore preparators = new PreparatorStore();
    final StreamStore streams = new StreamStore();

    final Configuration<Call> config = new Configuration<>();
    final EngineContext context = new EngineContext(
        model, preparators, streams, settings);

    final TemplateProcessor processor = new TemplateProcessor(
        context,
        executor,
        printer, 
        logPrinter,
        dataManager,
        config,
        programLengthLimit,
        traceLengthLimit
        );

    return new Template(
        model.getMetaData(),
        dataManager,
        preparators,
        streams,
        processor
        );
  }

  public void process(Template template) throws ConfigurationException, IOException {
    template.getProcessor().finish();
  }

  private static class TemplateProcessor implements Template.Processor {
    private final EngineContext engineContext;
    private final Executor executor;
    private final Printer printer;
    private final LogPrinter logPrinter;
    private final DataManager dataManager;
    private final Configuration<Call> config;

    private final int programLengthLimit;
    private final int traceLengthLimit;

    private boolean isDataPrinted = false;
    private int testIndex = 0; 

    private boolean needCreateNewFile;

    private TemplateProcessor(
        final EngineContext engineContext,
        final Executor executor,
        final Printer printer,
        final LogPrinter logPrinter,
        final DataManager dataManager,
        final Configuration<Call> config,
        final int programLengthLimit,
        final int traceLengthLimit) {

      this.engineContext = engineContext;
      this.executor = executor;
      this.printer = printer;
      this.logPrinter = logPrinter;
      this.dataManager = dataManager;
      this.config = config;

      this.programLengthLimit = programLengthLimit;
      this.traceLengthLimit = traceLengthLimit;
      
      this.needCreateNewFile = true;
    }

    private Block preBlock = null;
    private Block postBlock = null;
    private Statistics before;
    private String fileName;
    public static Date start = null;

    @Override
    public void process(final Section section, final Block block) {
      if (null == start) {
        start = new Date();
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

      try {
        processBlock(block);
      } catch (ConfigurationException e) {
        Logger.error(e.getMessage());
      }
    }

    @Override
    public void finish() {
      if (!needCreateNewFile) {
        printer.printToFile("");
        printer.printHeaderToFile("Epilogue");
        printer.printToFile("");

        if (!postBlock.isEmpty()) {
          try {
            processPreOrPostBlock(postBlock);
          } catch (ConfigurationException e) {
            Logger.error(e.getMessage());
          }
        } else {
          printer.printCommentToFile("Empty");
        }

        printer.close();

        if (null != logPrinter) {
          logPrinter.close();
        }

        // No instruction was added to the newly created file, it must be deleted
        if (STATISTICS.instructionCount == before.instructionCount) {
          new File(fileName).delete();
          STATISTICS.testProgramNumber--;
        }
      }

      Logger.debugHeader("Ended Processing Template");
    }

    private void processBlock(Block block) throws ConfigurationException {
      final Iterator<List<Call>> sequenceIt = block.getIterator();
      final TestSequenceEngine engine = getEngine(block);

      int sequenceIndex = 0;
      sequenceIt.init();

      while (sequenceIt.hasValue()) {
        final List<Call> abstractSequence = sequenceIt.value();
        checkNotNull(abstractSequence);

        final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);
        checkNotNull(iterator);

        for (iterator.init(); iterator.hasValue(); iterator.next()) {
          if (needCreateNewFile) {
            try {
              before = STATISTICS.copy();
              fileName = printer.createNewFile();
              STATISTICS.testProgramNumber++;

              if (logPrinter != null) {
                logPrinter.createNewFile();
              }
            } catch (IOException e) {
              Logger.error(e.getMessage());
            }

            if (!isDataPrinted && dataManager.containsDecls()) {
              printer.printToFile("");
              printSectionHeader("Data Declaration");
              printer.printToFile("");

              printer.printText(dataManager.getDeclText());
              isDataPrinted = true;
            }

            printer.printToFile("");
            printer.printHeaderToFile("Prologue");
            printer.printToFile("");

            if (!preBlock.isEmpty()) {
              try {
                processPreOrPostBlock(preBlock);
              } catch (ConfigurationException e) {
                Logger.error(e.getMessage());
              }
            } else {
              printer.printCommentToFile("Empty");
            }

            needCreateNewFile = false;
          }

          if (sequenceIndex == 0) {
            printer.printText("");
            printer.printSeparatorToFile(String.format("Test %d", testIndex++));
          }

          final AdapterResult adapterResult = iterator.value();
          checkNotNull(adapterResult);

          if (adapterResult.getStatus() != AdapterResult.Status.OK) {
            Logger.debug("%nAdapter Error: %s", adapterResult.getErrors());
            continue;
          }

          final TestSequence concreteSequence = adapterResult.getResult();
          checkNotNull(concreteSequence);

          Logger.debugHeader("Initialization");
          for (final ConcreteCall concreteCall : concreteSequence.getPrologue()) {
            Logger.debug(concreteCall.getText());
          }
          Logger.debugHeader("Test Case");
          for (final ConcreteCall concreteCall : concreteSequence.getBody()) {
            Logger.debug(concreteCall.getText());
          }

          final String sequenceId = String.format("Test Case %d", sequenceIndex);
          Logger.debugHeader("Generating Data for %s", sequenceId);

          Logger.debugHeader("Executing %s", sequenceId);
          executor.executeSequence(concreteSequence);

          Logger.debugHeader("Printing %s to %s", sequenceId, fileName);

          printer.printToFile("");
          printer.printSubheaderToFile(sequenceId);
          printer.printSequence(concreteSequence);

          STATISTICS.instructionCount += concreteSequence.getInstructionCount();
          STATISTICS.testCaseNumber++;

          ++sequenceIndex;

          Logger.debugHeader("");

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
            printer.printToFile("");
            printer.printHeaderToFile("Epilogue");
            printer.printToFile("");

            if (!postBlock.isEmpty()) {
              try {
                processPreOrPostBlock(postBlock);
              } catch (ConfigurationException e) {
                Logger.error(e.getMessage());
              }
            } else {
              printer.printCommentToFile("Empty");
            }

            printer.close();
          }
        } // Concrete sequence iterator

        sequenceIt.next();
      } // Abstract sequence iterator
    }

    private void processPreOrPostBlock(Block block) throws ConfigurationException {
      InvariantChecks.checkTrue(block.isSingle());
      final Iterator<List<Call>> sequenceIt = block.getIterator();
      final TestSequenceEngine engine = getEngine(block);

      sequenceIt.init();
      while (sequenceIt.hasValue()) {
        final List<Call> abstractSequence = sequenceIt.value();

        final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);

        for (iterator.init(); iterator.hasValue(); iterator.next()) {
          final AdapterResult adapterResult = iterator.value();
          checkNotNull(adapterResult);

          if (adapterResult.getStatus() != AdapterResult.Status.OK) {
            Logger.debug("%nAdapter Error: %s", adapterResult.getErrors());
            continue;
          }

          Logger.debugHeader("Generating Data");
          final TestSequence concreteSequence = adapterResult.getResult();;
          checkNotNull(concreteSequence);

          Logger.debugHeader("Executing");
          executor.executeSequence(concreteSequence);

          Logger.debugHeader("Printing to %s", fileName);
          printer.printSequence(concreteSequence);

          STATISTICS.instructionCount += concreteSequence.getInstructionCount();
        }

        sequenceIt.next();
      }
    }

    private TestSequenceEngine getEngine(final Block block) throws ConfigurationException {
      checkNotNull(block);

      final String engineName = blockAttribute(block, "engine", "default");
      final String adapterName = blockAttribute(block, "adapter", engineName);

      final Engine<?> engine = config.getEngine(engineName);
      checkNotNull(engine);

      final Adapter<?> adapter = config.getAdapter(adapterName);
      checkNotNull(adapter);

      if (!adapter.getSolutionClass().isAssignableFrom(engine.getSolutionClass())) {
        throw new IllegalStateException("Mismatched solver/adapter pair");
      }

      final TestSequenceEngine testSequenceEngine = new TestSequenceEngine(engine, adapter);
      testSequenceEngine.configure(block.getAttributes());

      return testSequenceEngine;
    }

    private static String blockAttribute(final Block block,
                                         final String name,
                                         final String fallback) {
      final Object value = block.getAttribute(name);
      if (value == null) {
        return fallback;
      }
      return value.toString();
    }

    private void printSectionHeader(String title) {
      Logger.debugHeader(title);
      printer.printHeaderToFile(title);
    }
  }
}
