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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.solver.Environment;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.Plugin;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.state.Reader;
import ru.ispras.microtesk.model.api.tarmac.LogPrinter;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.sequence.GeneratorConfig;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.SelfCheckEngine;
import ru.ispras.microtesk.test.sequence.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.sequence.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.BufferPreparatorStore;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataManager;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.StreamStore;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.test.template.Template.Section;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class TestEngine {
  public static TestStatistics STATISTICS = new TestStatistics();

  private static TestEngine instance = null;
  public static TestEngine getInstance() {
    return instance;
  }

  private final IModel model;

  // Architecture-specific settings
  private static GeneratorSettings settings;

  public IModel getModel() {
    return model;
  }

  public String getModelName() {
    return model.getName();
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

  public static TestStatistics generate(
      final String modelName,
      final String templateFile,
      final List<Plugin> plugins) throws Throwable {
    Logger.debug("Home: " + SysUtils.getHomeDir());
    Logger.debug("Current directory: " + SysUtils.getCurrentDir());
    Logger.debug("Model name: " + modelName);
    Logger.debug("Template file: " + templateFile);

    STATISTICS = null;

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

      for (final Plugin plugin : plugins) {
        plugin.initializeGenerationEnvironment();
      }

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
        STATISTICS.testProgramNumber--;
      }
    }

    return STATISTICS;
  }

  private TestEngine(final IModel model) {
    InvariantChecks.checkNotNull(model);

    this.model = model;
    Reader.setModel(model);

    final String home = SysUtils.getHomeDir();

    final ru.ispras.fortress.solver.Solver z3Solver = SolverId.Z3_TEXT.getSolver(); 
    if (null == z3Solver.getSolverPath()) {
      // If the Z3_PATH environment variable is not set, we set up default solver path
      // in hope to find the the tool there.
      final String z3Path = (home != null ? home : ".") + "/tools/z3/";
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
    }

    final ru.ispras.fortress.solver.Solver cvc4Solver = SolverId.CVC4_TEXT.getSolver();
    if (null == cvc4Solver.getSolverPath()) {
      // If the CVC4_PATH environment variable is not set, we set up default solver path
      // in hope to find the the tool there.
      final String cvc4Path = (home != null ? home : ".") + "/tools/cvc4/";
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
  }

  public Template newTemplate() {
    final PreparatorStore preparators = new PreparatorStore();
    final BufferPreparatorStore bufferPreparators = new BufferPreparatorStore();
    final StreamStore streams = new StreamStore();

    final IModelStateObserver observer = model.getStateObserver();
    final Printer printer = new Printer(observer);

    final DataManager dataManager = new DataManager(printer);
 
    final EngineContext context = new EngineContext(
        model,
        dataManager,
        preparators,
        bufferPreparators,
        streams,
        settings
        );

    final LogPrinter logPrinter = TestSettings.isTarmacLog() ?
        new LogPrinter(TestSettings.getCodeFilePrefix()) : null;

    final Executor executor = new Executor(context, observer, logPrinter);

    final TemplateProcessor processor = new TemplateProcessor(
        context,
        executor,
        printer, 
        logPrinter,
        dataManager
        );

    STATISTICS = new TestStatistics();

    return new Template(
        context,
        model.getMetaData(),
        dataManager,
        preparators,
        bufferPreparators,
        streams,
        processor
        );
  }

  public void process(final Template template) throws ConfigurationException, IOException {
    template.getProcessor().finish();
  }

  private static class TemplateProcessor implements Template.Processor {
    private final EngineContext engineContext;
    private final Executor executor;
    private final Printer printer;
    private final LogPrinter logPrinter;
    private final DataManager dataManager;
    private final GeneratorConfig<Call> config;

    private int testIndex = 0; 

    private boolean needCreateNewFile;

    private TemplateProcessor(
        final EngineContext engineContext,
        final Executor executor,
        final Printer printer,
        final LogPrinter logPrinter,
        final DataManager dataManager) {

      this.engineContext = engineContext;
      this.executor = executor;
      this.printer = printer;
      this.logPrinter = logPrinter;
      this.dataManager = dataManager;
      this.config = GeneratorConfig.<Call>get();

      this.needCreateNewFile = true;
    }

    private List<TestSequence> preBlockTestSequences = null;
    private Block postBlock = null;
    private TestStatistics before;
    private String fileName;

    @Override
    public void process(final Section section, final Block block) {
      InvariantChecks.checkNotNull(section);
      InvariantChecks.checkNotNull(block);

      if (section == Section.PRE) {
        try {
          preBlockTestSequences = buildTestSequencesForPreOrPost(block);
        } catch (ConfigurationException e) {
          Logger.error(e.getMessage());
        }
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
            processPreOrPostBlock(postBlock, "Epilogue");
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

    private void processBlock(final Block block) throws ConfigurationException {
      final Iterator<List<Call>> sequenceIt = block.getIterator();
      final TestSequenceEngine engine = getEngine(block);

      int sequenceIndex = 0;
      sequenceIt.init();

      while (sequenceIt.hasValue()) {
        final List<Call> abstractSequence = sequenceIt.value();
        InvariantChecks.checkNotNull(abstractSequence);

        final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);
        InvariantChecks.checkNotNull(iterator);

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

            if (dataManager.containsDecls()) {
              printer.printToFile("");
              printSectionHeader("Data Declaration");
              printer.printToFile("");
              printer.printText(dataManager.getDeclText());
            }

            printer.printToFile("");
            printer.printHeaderToFile("Prologue");
            printer.printToFile("");

            if (!preBlockTestSequences.isEmpty()) {
              try {
                executeAndPrintTestSequencesOfPreOrPostBlock(preBlockTestSequences, "Prologue");
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
          InvariantChecks.checkNotNull(adapterResult);

          if (adapterResult.getStatus() != AdapterResult.Status.OK) {
            Logger.debug("%nAdapter Error: %s", adapterResult.getErrors());
            continue;
          }

          final TestSequence concreteSequence = adapterResult.getResult();
          InvariantChecks.checkNotNull(concreteSequence);

          Logger.debugHeader("Initialization");
          for (final ConcreteCall concreteCall : concreteSequence.getPrologue()) {
            Logger.debug(concreteCall.getText());
          }
          Logger.debugHeader("Test Case");
          for (final ConcreteCall concreteCall : concreteSequence.getBody()) {
            Logger.debug(concreteCall.getText());
          }

          final int testCaseIndex = STATISTICS.testCaseNumber;
          final String sequenceId = String.format("Test Case %d", testCaseIndex);
          Logger.debugHeader("Generating Data for %s", sequenceId);

          Logger.debugHeader("Executing %s", sequenceId);
          sandboxExecution(executor, concreteSequence, testCaseIndex);

          final TestSequence selfCheckSequence;
          if (TestSettings.isSelfChecks()) {
            Logger.debugHeader("Preparing Self-Checks for %s", sequenceId);
            selfCheckSequence = SelfCheckEngine.solve(engineContext, concreteSequence.getChecks());

            Logger.debugHeader("Executing Self-Checks for %s", sequenceId);
            sandboxExecution(executor, selfCheckSequence, testCaseIndex);
          } else {
            selfCheckSequence = null;
          }

          Logger.debugHeader("Printing %s to %s", sequenceId, fileName);

          printer.printToFile("");
          printer.printSubheaderToFile(sequenceId);
          printer.printSequence(concreteSequence);
          STATISTICS.instructionCount += concreteSequence.getInstructionCount();

          if (null != selfCheckSequence) {
            printer.printText("");
            printer.printNote("Self Checks");

            final List<ConcreteCall> selfCheckCalls = selfCheckSequence.getAll();
            if (selfCheckCalls.isEmpty()) {
              printer.printNote("Empty");
            } else {
              printer.printCalls(selfCheckCalls);
            }

            STATISTICS.instructionCount += selfCheckSequence.getInstructionCount();
          }

          STATISTICS.testCaseNumber++;
          ++sequenceIndex;
          Logger.debugHeader("");

          final TestStatistics after = STATISTICS.copy();

          final boolean isProgramLengthLimitExceeded =
              (after.instructionCount - before.instructionCount) >= TestSettings.getProgramLengthLimit();

          final boolean isTraceLengthLimitExceeded =
              (after.instructionExecutedCount - before.instructionExecutedCount) >= TestSettings.getTraceLengthLimit();

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
                processPreOrPostBlock(postBlock, "Epilogue");
              } catch (ConfigurationException e) {
                Logger.error(e.getMessage());
              }
            } else {
              printer.printCommentToFile("Empty");
            }

            printer.close();

            if (!preBlockTestSequences.isEmpty()) {
              final TestSequence sequences =
                  preBlockTestSequences.get(preBlockTestSequences.size() - 1);
              engineContext.setAddress(sequences.getEndAddress());
            } else {
              engineContext.setAddress(0);
            }
          }
        } // Concrete sequence iterator

        sequenceIt.next();
      } // Abstract sequence iterator
    }

    private void sandboxExecution(final Executor exe, final TestSequence s, final int index) {
      try {
        exe.executeSequence(s, index);
      } catch (final Throwable e) {
        final java.io.StringWriter writer = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(writer));
        Logger.warning("Simulation failed: %s%n%s", e.getMessage(), writer);
      }
    }

    private void processPreOrPostBlock(
        final Block block,
        final String headerText) throws ConfigurationException {
      final List<TestSequence> concreteSequences = buildTestSequencesForPreOrPost(block);
      executeAndPrintTestSequencesOfPreOrPostBlock(concreteSequences, headerText);
    }

    /**
     * This method creates a list of test sequences (sequences of concrete calls)
     * for a PRE or a POST block.
     * 
     * <p>NOTE: It is assumed that PRE and POST blocks can return only one <b>SINGLE</b>
     * sequence of abstract calls. Otherwise, this is an incorrect test template. Using 
     * constructs like 'block' that produce multiple sequence is forbidden in
     * 'pre' and 'post'.
     * 
     * @param block PRE or POST block to be processed. 
     * @return List of test sequences.
     */

    public List<TestSequence> buildTestSequencesForPreOrPost(final Block block) throws ConfigurationException {
      InvariantChecks.checkNotNull(block);
      InvariantChecks.checkTrue(block.isSingle());

      final TestSequenceEngine engine = getEngine(block);
      final Iterator<List<Call>> sequenceIt = block.getIterator();

      sequenceIt.init();
      if (!sequenceIt.hasValue()) {
        return Collections.emptyList();
      }

      final List<Call> abstractSequence = sequenceIt.value();
      final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);

      final List<TestSequence> result = new ArrayList<>();
      for (iterator.init(); iterator.hasValue(); iterator.next()) {
        final AdapterResult adapterResult = iterator.value();
        InvariantChecks.checkNotNull(adapterResult);

        if (adapterResult.getStatus() != AdapterResult.Status.OK) {
          Logger.debug("%nAdapter Error: %s", adapterResult.getErrors());
          continue;
        }

        final TestSequence concreteSequence = adapterResult.getResult();
        InvariantChecks.checkNotNull(concreteSequence);

        result.add(concreteSequence);
      }

      return result;
    }

    private TestSequenceEngine getEngine(final Block block) throws ConfigurationException {
      InvariantChecks.checkNotNull(block);

      final String engineName = blockAttribute(block, "engine", "default");
      final String adapterName = blockAttribute(block, "adapter", engineName);

      final Engine<?> engine = config.getEngine(engineName);
      InvariantChecks.checkNotNull(engine);

      final Adapter<?> adapter = config.getAdapter(adapterName);
      InvariantChecks.checkNotNull(adapter);

      if (!adapter.getSolutionClass().isAssignableFrom(engine.getSolutionClass())) {
        throw new IllegalStateException("Mismatched solver/adapter pair");
      }

      final TestSequenceEngine testSequenceEngine = new TestSequenceEngine(engine, adapter);
      testSequenceEngine.configure(block.getAttributes());

      return testSequenceEngine;
    }

    private void executeAndPrintTestSequencesOfPreOrPostBlock(
        final List<TestSequence> concreteSequences,
        final String headerText) throws ConfigurationException {

      for (final TestSequence concreteSequence : concreteSequences) {
        Logger.debugHeader("Executing %s", headerText);
        executor.executeSequence(concreteSequence, Label.NO_SEQUENCE_INDEX);

        Logger.debugHeader("Printing %s to %s", headerText, fileName);
        printer.printSequence(concreteSequence);

        STATISTICS.instructionCount += concreteSequence.getInstructionCount();
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

    @Override
    public void defineExceptionHandler(final ExceptionHandler handler) {
      final String exceptionFileName = String.format(
          "%s.%s", TestSettings.getExceptionFilePrefix(), TestSettings.getCodeFileExtension());

      Logger.debugHeader("Processing Exception Handler (%s)", exceptionFileName);
      InvariantChecks.checkNotNull(handler);

      final Engine<?> engine = config.getEngine("default");
      InvariantChecks.checkNotNull(engine);

      final Adapter<?> adapter = config.getAdapter("default");
      InvariantChecks.checkNotNull(adapter);

      if (!adapter.getSolutionClass().isAssignableFrom(engine.getSolutionClass())) {
        throw new IllegalStateException("Mismatched solver/adapter pair");
      }

      final PrintWriter fileWriter;
      try {
        fileWriter = printer.newFileWriter(exceptionFileName);
      } catch (final IOException e) {
        throw new GenerationAbortedException(String.format(
            "Failed to create the %s file. Reason: %s", exceptionFileName, e.getMessage()));
      }

      try {
        final Map<String, List<ConcreteCall>> handlers = new LinkedHashMap<>();

        final TestSequenceEngine testSequenceEngine = 
            new TestSequenceEngine(engine, adapter);

        for (final ExceptionHandler.Section section : handler.getSections()) {
          final Iterator<AdapterResult> iterator;
          final boolean tempIsGenerateData = engineContext.isGenerateData();
          try {
            engineContext.setGenerateData(false);
            iterator = testSequenceEngine.process(engineContext, section.getCalls());
          } finally {
            engineContext.setGenerateData(tempIsGenerateData);
          }

          // At least one sequence is expected 
          iterator.init(); 
          InvariantChecks.checkTrue(iterator.hasValue());

          final AdapterResult adapterResult = iterator.value();

          // Only one sequence is expected
          iterator.next();
          InvariantChecks.checkFalse(iterator.hasValue());

          if (adapterResult.getStatus() != AdapterResult.Status.OK) {
            throw new GenerationAbortedException(
                String.format("%nAdapter Error: %s", adapterResult.getErrors()));
          }

          final TestSequence concreteSequence = adapterResult.getResult();
          InvariantChecks.checkNotNull(concreteSequence);

          final long address = section.getAddress().longValue();
          concreteSequence.setAddress(address);

          final List<ConcreteCall> handlerSequence = concreteSequence.getAll();
          for (final String exception : section.getExceptions()) {
            if (null != handlers.put(exception, handlerSequence)) {
              Logger.warning("Exception handler for %s is redefined.", exception);
            }
          }

          try {
            fileWriter.println();
            Logger.debug("");

            printer.printCommentToFile(fileWriter,
                String.format("Exceptions: %s", section.getExceptions()));

            final String org = String.format(".org 0x%x", address);
            Logger.debug(org);
            printer.printToFile(fileWriter, org);
            printer.printSequence(fileWriter, concreteSequence);
          } catch (ConfigurationException e) {
            e.printStackTrace();
          }

          STATISTICS.instructionCount += concreteSequence.getInstructionCount();
        }

        executor.setExceptionHandlers(handlers);
      } finally {
        fileWriter.close();
        Logger.debugBar();
      }
    }
  }
}
