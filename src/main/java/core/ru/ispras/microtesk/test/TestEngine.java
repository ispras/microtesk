/*
 * Copyright 2013-2017 ISP RAS (http://www.ispras.ru)
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
import java.util.List;

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
import ru.ispras.microtesk.model.api.memory.AddressTranslator;
import ru.ispras.microtesk.model.api.Reader;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.SettingsParser;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

/**
 * The {@link TestEngine} class is responsible for generation of test programs.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
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

  public Statistics getStatistics() {
    return statistics;
  }

  public GeneratorSettings getGeneratorSettings() {
    return settings;
  }

  public static boolean generate(
      final Options options,
      final String modelName,
      final String templateFile,
      final List<Plugin> plugins) throws Throwable {
    Logger.debug("Home: " + SysUtils.getHomeDir());
    Logger.debug("Current directory: " + SysUtils.getCurrentDir());
    Logger.debug("Model name: " + modelName);
    Logger.debug("Template file: " + templateFile);

    if (!new File(templateFile).exists()) {
      reportAborted("The %s file does not exists.", templateFile);
      return false;
    }

    final Statistics statistics = new Statistics();
    statistics.pushActivity(Statistics.Activity.INITIALIZING);

    final Model model;
    try {
      model = SysUtils.loadModel(modelName);
    } catch (final Exception e) {
      Logger.error(e.getMessage());
      reportAborted("Failed to load the %s model.", modelName);
      return false;
    }

    final GeneratorSettings settings = readSettings(options, modelName);
    if (null == settings) {
      reportAborted("Failed to load generation settings for %s.", modelName);
      return false;
    }

    setRandomSeed(options.getValueAsInteger(Option.RANDOM));
    setSolver(options.getValueAsString(Option.SOLVER));
    Environment.setDebugMode(options.getValueAsBoolean(Option.SOLVER_DEBUG));

    instance = new TestEngine(model, options, settings, plugins, statistics);
    if  (!instance.processTemplate(templateFile)) {
      return false;
    }

    final long totalTime = statistics.getTotalTime();
    final long genTime = totalTime - statistics.getTimeMetric(Statistics.Activity.INITIALIZING);
    final long genRate = (1000 * statistics.getInstructions()) / genTime;

    Logger.message("Generation Statistics");
    Logger.message("Generation time: %s", Statistics.timeToString(genTime));
    Logger.message("Generation rate: %d instructions/second", genRate);

    Logger.message("Programs/stimuli/instructions: %d/%d/%d",
        statistics.getPrograms(), statistics.getSequences(), statistics.getInstructions());

    if (options.getValueAsBoolean(Option.TIME_STATISTICS)) {
      Logger.message(System.lineSeparator() + "Time Statistics");

      Logger.message("Total time: %s", Statistics.timeToString(totalTime));
      Logger.message(statistics.getTimeMetricText(Statistics.Activity.INITIALIZING));

      final long genPercentage = (genTime * 10000) / totalTime;
      Logger.message("Generation time: %s (%d.%d%%)",
          Statistics.timeToString(genTime), genPercentage / 100, genPercentage % 100);

      for (final Statistics.Activity activity : Statistics.Activity.values()) {
        if (activity != Statistics.Activity.INITIALIZING) {
          Logger.message("  " + statistics.getTimeMetricText(activity));
        }
      }
    }

    final long rateLimit = options.getValueAsInteger(Option.RATE_LIMIT);
    if (genRate < rateLimit && statistics.getInstructions() >= 1500) { 
      // Makes sense only for sequences of significant length (>= 1000)
      Logger.error("Generation rate is too slow. At least %d is expected.", rateLimit);
      return false;
    }

    return true;
  }

  private boolean processTemplate(final String templateFile) throws Throwable {
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
        reportAborted(e2.getMessage());
      }
    }

    return true;
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

    final EngineContext context = new EngineContext(
        options,
        model,
        settings,
        statistics
        );

    if (options.getValueAsBoolean(Option.TARMAC_LOG)) {
      final String outDir = getOutDir(options); 
      Tarmac.initialize(outDir, options.getValueAsString(Option.CODE_PRE));
    }

    final TemplateProcessor processor = new TemplateProcessor(context);
    return new Template(context, processor);
  }

  private static String getOutDir(final Options options) {
    return options.hasValue(Option.OUTDIR) ?
        options.getValueAsString(Option.OUTDIR) : SysUtils.getHomeDir();
  }

  private static GeneratorSettings readSettings(final Options options, final String modelName) {
    if (!options.hasValue(Option.ARCH_DIRS)) {
      Logger.error("The --%s option is undefined.", Option.ARCH_DIRS.getName());
      return null;
    }

    final String archDirs = options.getValueAsString(Option.ARCH_DIRS);
    final String[] archDirsArray = archDirs.split(":");

    GeneratorSettings settings = null;
    for (final String archDir : archDirsArray) {
      final String[] archDirArray = archDir.split("=");

      if (archDirArray != null && archDirArray.length > 1 && modelName.equals(archDirArray[0])) {
        final File archFile = new File(archDirArray[1]);

        final String archPath = archFile.isAbsolute() ?
            archDirArray[1] :
            String.format("%s%s%s", SysUtils.getHomeDir(), File.separator, archDirArray[1]); 

        settings = SettingsParser.parse(archPath);
        break;
      }
    }

    if (null == settings) {
      Logger.error("The --%s option does not contain path to settings for %s.",
          Option.ARCH_DIRS.getName(), modelName);
    }

    return settings;
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
    Logger.message("Generation was aborted.");
  }

  private static void setRandomSeed(final int seed) {
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
