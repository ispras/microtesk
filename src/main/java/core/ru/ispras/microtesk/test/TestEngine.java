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
import java.util.Collection;
import java.util.Date;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.solver.Environment;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Result;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.tarmac.LogPrinter;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.data.ModeAllocator;
import ru.ispras.microtesk.test.sequence.Configuration;
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
    
    final DataManager dataManager = new DataManager(indentToken);
    final PreparatorStore preparators = new PreparatorStore();

    final Configuration<Call> config = new Configuration<>();
    final TestDataGenerator generator =
        new TestDataGenerator(model, preparators, settings);

    config.registerSolver("default", generator);
    config.registerAdapter("default", new TestSequenceAdapter());

    final TemplateProcessor processor = new TemplateProcessor(
        executor,
        printer, 
        logPrinter,
        dataManager,
        config,
        programLengthLimit,
        traceLengthLimit
        );

    return new Template(
        model.getMetaData(), dataManager, preparators, processor);
  }

  public void process(Template template) throws ConfigurationException, IOException {
    template.getProcessor().finish();
  }

  private static class TemplateProcessor implements Template.Processor {
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
        final Executor executor,
        final Printer printer,
        final LogPrinter logPrinter,
        final DataManager dataManager,
        final Configuration<Call> config,
        final int programLengthLimit,
        final int traceLengthLimit) {

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
      if (null == start)
        start = new Date();

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
      final boolean isSingleSequence = block.isSingle();
      final Iterator<Sequence<Call>> sequenceIt = block.getIterator();
      final DataGenerationEngine engine = getEngine(block);

      int sequenceIndex = 0;
      sequenceIt.init();

      while (sequenceIt.hasValue()) {
        if (isSingleSequence && sequenceIndex > 0) {
          throw new IllegalStateException("Only a single sequence is allowed.");
        }
        
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
            printSectionHeader("Data Declarations");
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

        final String sequenceId = String.format("Test Case %d", sequenceIndex);
        final Sequence<Call> abstractSequence = sequenceIt.value();

        Logger.debugHeader("Generating Data%s", (isSingleSequence ? "" : " for " + sequenceId));

        final Iterator<TestSequence> iterator = engine.process(abstractSequence);
        for (iterator.init(); iterator.hasValue(); iterator.next()) {
          final TestSequence concreteSequence = iterator.value();

          Logger.debugHeader("Executing%s", (isSingleSequence ? "" : " " + sequenceId));
          executor.executeSequence(concreteSequence);

          Logger.debugHeader("Printing%s", (isSingleSequence ? "" : " " + sequenceId));

          if (!isSingleSequence) {
            printer.printToFile("");
            printer.printSubheaderToFile(sequenceId);
          }
          printer.printSequence(concreteSequence);

          STATISTICS.instructionCount += concreteSequence.getPrologue().size();
          STATISTICS.instructionCount += concreteSequence.getBody().size();

          sequenceIt.next();
          ++sequenceIndex;

          Logger.debugHeader("");

          STATISTICS.testCaseNumber++;

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
        }
      }
    }

    private void processPreOrPostBlock(Block block) throws ConfigurationException {
      InvariantChecks.checkTrue(block.isSingle());
      final Iterator<Sequence<Call>> sequenceIt = block.getIterator();
      final DataGenerationEngine engine = getEngine(block);

      sequenceIt.init();
      while (sequenceIt.hasValue()) {
        final Sequence<Call> abstractSequence = sequenceIt.value();

        final Iterator<TestSequence> iterator = engine.process(abstractSequence);

        for (iterator.init(); iterator.hasValue(); iterator.next()) {
          Logger.debugHeader("Generating Data");
          final TestSequence concreteSequence = iterator.value();

          Logger.debugHeader("Executing");
          executor.executeSequence(concreteSequence);

          Logger.debugHeader("Printing");
          printer.printSequence(concreteSequence);

          STATISTICS.instructionCount += concreteSequence.getPrologue().size();
          STATISTICS.instructionCount += concreteSequence.getBody().size();
        }

        sequenceIt.next();
      }
    }

   private DataGenerationEngine getEngine(final Block block) throws ConfigurationException {
      final String solverName = blockAttribute(block, "solver", "default");
      final String adapterName = blockAttribute(block, "adapter", "default");

      final Solver<?> solver = config.getSolver(solverName);
      final Adapter<?> adapter = config.getAdapter(adapterName);

      if (!adapter.getSolutionClass().isAssignableFrom(solver.getSolutionClass())) {
        throw new IllegalStateException("Mismatched solver/adapter pair");
      }
      return new DataGenerationEngine(solver, adapter);
    }

    private static final class DataGenerationEngine {
      private final Solver<?> solver;
      private final Adapter<?> adapter;

      public DataGenerationEngine(final Solver<?> solver, final Adapter<?> adapter) {
        InvariantChecks.checkNotNull(solver);
        InvariantChecks.checkNotNull(adapter);

        this.solver = solver;
        this.adapter = adapter;
      }

      public Iterator<TestSequence> process(final Sequence<Call> abstractSequence) {
        final Iterator<?> solutionIt = solve(solver, abstractSequence);
        return adapt(adapter, abstractSequence, solutionIt);
      }

      private static <T> Iterator<T> solve(final Solver<T> solver,
                                           final Sequence<Call> sequence) {
        final SolverResult<T> result = solver.solve(sequence);
        if (result.getStatus() != SolverResult.Status.OK) {
          final String msg = listErrors("Failed to find a solution for abstract call sequence",
                                        result.getErrors());
          throw new IllegalStateException(msg);
        }
        return result.getResult();
      }

      private static <T> Iterator<TestSequence> adapt(final Adapter<T> adapter,
                                                      final Sequence<Call> sequence,
                                                      final Iterator<?> solutionIterator) {
        return new Iterator<TestSequence>() {
          @Override public void init() {
            solutionIterator.init();
          }

          @Override public boolean hasValue() {
            return solutionIterator.hasValue();
          }

          @Override public TestSequence value() {
            final Object solution = solutionIterator.value(); 
            final Class<T> solutionClass = adapter.getSolutionClass();

            return adapter.adapt(sequence, solutionClass.cast(solution));
          }

          @Override public void next() {
            solutionIterator.next();
          }
        };
      }

      private static String listErrors(final String message, final Collection<String> errors) {
        if (errors.isEmpty()) {
          return message;
        }
        final StringBuilder builder = new StringBuilder(message);
        builder.append(" Errors:");
        for (final String error : errors) {
          builder.append(System.lineSeparator() + "  " + error);
        }
        return builder.toString();
      }
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
