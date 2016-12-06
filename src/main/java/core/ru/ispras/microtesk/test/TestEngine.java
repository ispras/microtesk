/*
 * Copyright 2013-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.List;
import java.util.Set;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.solver.Environment;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.Plugin;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.api.Model;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.memory.AddressTranslator;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.Reader;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.DataManager;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.microtesk.utils.StringUtils;

public final class TestEngine {
  private static TestEngine instance = null;

  public static TestEngine getInstance() {
    return instance;
  }

  private final Options options;
  private final Model model;
  private final List<Plugin> plugins;
  private final Statistics statistics;
  private final GeneratorSettings settings; // Architecture-specific settings

  private TestEngine(
      final Model model,
      final Options options,
      final GeneratorSettings settings,
      final List<Plugin> plugins,
      final Statistics statistics) {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(settings);
    InvariantChecks.checkNotNull(plugins);
    InvariantChecks.checkNotNull(statistics);

    this.model = model;
    this.options = options;
    this.settings = settings;
    this.plugins = plugins;
    this.statistics = statistics;

    Reader.setModel(model);
    initSolverPaths(SysUtils.getHomeDir());

    final AllocationSettings allocation = settings.getAllocation();
    if (allocation != null) {
      ModeAllocator.init(allocation);
    }
  }

  public Object getOptionValue(final String optionName) {
    return options.getValue(optionName);
  }

  public void setOptionValue(final String optionName, final Object value) {
    options.setValue(optionName, value);
  }

  public Model getModel() {
    return model;
  }

  public String getModelName() {
    return model.getName();
  }

  public MetaModel getMetaModel() {
    return model.getMetaData();
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public GeneratorSettings getGeneratorSettings() {
    return settings;
  }

  public static Statistics generate(
      final Options options,
      final GeneratorSettings settings,
      final String modelName,
      final String templateFile,
      final List<Plugin> plugins) throws Throwable {
    Logger.debug("Home: " + SysUtils.getHomeDir());
    Logger.debug("Current directory: " + SysUtils.getCurrentDir());
    Logger.debug("Model name: " + modelName);
    Logger.debug("Template file: " + templateFile);

    final Statistics statistics = new Statistics();
    statistics.pushActivity(Statistics.Activity.INITIALIZING);

    final Model model;
    try {
      model = SysUtils.loadModel(modelName);
    } catch (final Exception e) {
      Logger.error(e.getMessage());
      reportAborted("Failed to load the %s model.", modelName);
      return null;
    }

    setRandomSeed(options.getValueAsInteger(Option.RANDOM));
    setSolver(options.getValueAsString(Option.SOLVER));
    Environment.setDebugMode(options.getValueAsBoolean(Option.SOLVER_DEBUG));

    instance = new TestEngine(model, options, settings, plugins, statistics);
    return instance.processTemplate(templateFile);
  }

  private Statistics processTemplate(final String templateFile) throws Throwable {
    final String scriptsPath = String.format(
        "%s/lib/ruby/microtesk.rb", SysUtils.getHomeDir());

    final ScriptingContainer container = new ScriptingContainer();
    container.setArgv(new String[] {templateFile});

    try {
      container.runScriptlet(PathType.ABSOLUTE, scriptsPath);
    } catch(final org.jruby.embed.EvalFailedException e) {
      // JRuby wraps exceptions that occur in Java libraries it calls into
      // EvalFailedException. To handle them correctly, we need to unwrap them.

      try {
        throw e.getCause();
      } catch (final GenerationAbortedException e2) {
        handleGenerationAborted(e2);
      }
    }

    return statistics;
  }

  private void handleGenerationAborted(final GenerationAbortedException e) {
    reportAborted(e.getMessage());

    if (null != Printer.getLastFileName()) {
      new File(Printer.getLastFileName()).delete();
      if (null != statistics) {
        statistics.decPrograms();
      }
    }
  }

  public Template newTemplate() {
    final int instanceNumber = options.getValueAsInteger(Option.INSTANCE_NUMBER);
    Logger.message("Instance number: %d", instanceNumber);

    model.setPENumber(instanceNumber);
    model.setActivePE(0);

    for (final Plugin plugin : plugins) {
      plugin.initializeGenerationEnvironment();
    }

    statistics.popActivity();
    statistics.setProgramLengthLimit(options.getValueAsInteger(Option.CODE_LIMIT));
    statistics.setTraceLengthLimit(options.getValueAsInteger(Option.TRACE_LIMIT));
    statistics.pushActivity(Statistics.Activity.PARSING);

    AddressTranslator.initialize(
        options.getValueAsBigInteger(Option.BASE_VA),
        options.getValueAsBigInteger(Option.BASE_PA)
        );

    final Printer printer = new Printer(options, model.getPE(), statistics);
    final DataManager dataManager = new DataManager(model, options, printer, statistics);
 
    final EngineContext context = new EngineContext(
        options,
        model,
        dataManager,
        settings,
        statistics
        );

    dataManager.setLabelManager(context.getLabelManager());

    if (options.getValueAsBoolean(Option.TARMAC_LOG)) {
      final String outDir = options.hasValue(Option.OUTDIR) ?
          options.getValueAsString(Option.OUTDIR) : SysUtils.getHomeDir(); 
      Tarmac.initialize(outDir, options.getValueAsString(Option.CODE_PRE));
    }

    final TemplateProcessor processor = new TemplateProcessor(context, printer);
    return new Template(context, processor);
  }

  public void process(final Template template) throws ConfigurationException, IOException {
    template.getProcessor().finish();

    final Set<Block> unusedBlocks = template.getUnusedBlocks();
    if (!unusedBlocks.isEmpty()) {
      Logger.warning("Unused blocks have been detected at: %s",
          StringUtils.toString(unusedBlocks, ", ", new StringUtils.Converter<Block>() {
              @Override
              public String toString(final Block o) {return o.getWhere().toString();}
          }));
    }
  }

  private static void initSolverPaths(final String home) {
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

  private static void reportAborted(final String format, final Object... args) {
    Logger.error(format, args);
    Logger.message("Generation Aborted");
  }

  private static void setRandomSeed(int seed) {
    Randomizer.get().setSeed(seed);
  }

  private static void setSolver(final String solverName) {
    if ("z3".equalsIgnoreCase(solverName)) {
      TestBase.setSolverId(SolverId.Z3_TEXT);
    } else if ("cvc4".equalsIgnoreCase(solverName)) {
      TestBase.setSolverId(SolverId.CVC4_TEXT);
    } else {
      Logger.warning("Unknown solver: %s. Default solver will be used.", solverName);
    }
  }
}
