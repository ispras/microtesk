/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.FileUtils;
import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.solver.Environment;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.Config;
import ru.ispras.microtesk.Plugin;
import ru.ispras.microtesk.Revisions;
import ru.ispras.microtesk.ScriptRunner;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.Execution;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.Reader;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.ExtensionSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.SettingsParser;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.allocator.AllocatorEngine;
import ru.ispras.microtesk.test.template.Template;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

import ru.ispras.testbase.TestBaseRegistry;
import ru.ispras.testbase.generator.DataGenerator;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@link TestEngine} is responsible for test program generation.
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
  private final Set<String> revisionIds;
  private final List<Plugin> plugins;
  private final Statistics statistics;

  private TestEngine(
      final Model model,
      final Set<String> revisionIds,
      final Options options,
      final List<Plugin> plugins,
      final Statistics statistics) {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(revisionIds);
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(plugins);
    InvariantChecks.checkNotNull(statistics);

    this.model = model;
    this.revisionIds = revisionIds;
    this.options = options;
    this.plugins = plugins;
    this.statistics = statistics;

    Reader.setModel(model);
    initSolverPaths(SysUtils.getHomeDir());

    final GeneratorSettings settings = GeneratorSettings.get();
    final AllocationSettings allocation = settings.getAllocation();

    if (allocation != null) {
      AllocatorEngine.init(allocation);
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

  public boolean isRevision(final String revisionId) {
    return revisionIds.contains(revisionId);
  }

  public Statistics getStatistics() {
    return statistics;
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

    final Set<String> revisionIds = readRevisionIds(options, modelName, model.getRevisionId());
    if (null == revisionIds) {
      reportAborted("Failed to load revision IDs for %s.", modelName);
      return false;
    }

    GeneratorSettings.set(settings);
    try {
      installDataGeneratorsIntoTestBase(settings);
    } catch (final Exception e) {
      reportAborted("Failed to load custom data generators for %s.", modelName);
      return false;
    }

    setRandomSeed(options.getValueAsInteger(Option.RANDOM_SEED));
    setSolver(options.getValueAsString(Option.SOLVER));
    Environment.setDebugMode(options.getValueAsBoolean(Option.SOLVER_DEBUG));

    instance = new TestEngine(model, revisionIds, options, plugins, statistics);

    try {
      ScriptRunner.run(options, templateFile);
    } catch (final GenerationAbortedException e) {
      reportAborted(e.getMessage());
    }

    final long totalTime = statistics.getTotalTime();
    final long genTime = totalTime - statistics.getTimeMetric(Statistics.Activity.INITIALIZING);
    final long genRate = genTime > 0 ? (1000 * statistics.getInstructions()) / genTime : -1;

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

  public Template newTemplate() {
    Execution.setAssertionsEnabled(options.getValueAsBoolean(Option.ASSERTS_ENABLED));

    final int instanceNumber = options.getValueAsInteger(Option.INSTANCE_NUMBER);
    Logger.message("Instance number: %d", instanceNumber);

    model.setPENumber(instanceNumber);
    model.setActivePE(0);

    for (final Plugin plugin : plugins) {
      plugin.initializeGenerationEnvironment();
    }

    statistics.popActivity();
    statistics.setProgramLengthLimit(options.getValueAsInteger(Option.PROGRAM_LENGTH_LIMIT));
    statistics.setTraceLengthLimit(options.getValueAsInteger(Option.TRACE_LENGTH_LIMIT));
    statistics.pushActivity(Statistics.Activity.PARSING);

    final EngineContext context = new EngineContext(options, model, statistics);
    final Template.Processor processor = new TemplateProcessor(context);

    return new Template(context, processor);
  }

  private static GeneratorSettings readSettings(final Options options, final String modelName) {
    if (!options.hasValue(Option.ARCH_DIRS)) {
      Logger.error("The --%s option is undefined.", Option.ARCH_DIRS.getName());
      return null;
    }

    final String archDirs = options.getValueAsString(Option.ARCH_DIRS);
    final String archPath = SysUtils.getArchDir(archDirs, modelName);

    if (null == archPath) {
      Logger.error("The --%s option does not contain path to settings for %s.",
          Option.ARCH_DIRS.getName(), modelName);
    }

    return SettingsParser.parse(archPath);
  }

  private static Set<String> readRevisionIds(
      final Options options,
      final String modelName,
      final String revisionId) {
    if (revisionId.isEmpty()) {
      return Collections.emptySet();
    }

    if (!options.hasValue(Option.ARCH_DIRS)) {
      Logger.error("The --%s option is undefined.", Option.ARCH_DIRS.getName());
      return null;
    }

    final String archDirs = options.getValueAsString(Option.ARCH_DIRS);
    final String archPath = SysUtils.getArchDir(archDirs, modelName);

    if (null == archPath) {
      Logger.error("The --%s option does not contain path to settings for %s.",
          Option.ARCH_DIRS.getName(), modelName);
    }

    final String path = new File(FileUtils.getFileDir(archPath), "revisions.xml").getPath();
    final Revisions revisions = Config.loadRevisions(path);

    return revisions.getRevision(revisionId);
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
      } else if (Environment.isOsX()) {
        z3Solver.setSolverPath(z3Path + "osx/z3/bin/z3");
      } else {
        throw new UnsupportedOperationException(String.format(
            "Unsupported platform: %s.", Environment.getOsName()));
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
      } else if (Environment.isOsX()) {
        cvc4Solver.setSolverPath(cvc4Path + "osx/cvc4");
      } else {
        throw new UnsupportedOperationException(String.format(
            "Unsupported platform: %s.", Environment.getOsName()));
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

  // Register the user-defined test data generators.
  private static void installDataGeneratorsIntoTestBase(final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(settings);

    final TestBase testBase = TestBase.get();
    final TestBaseRegistry registry = testBase.getRegistry();

    if (null == settings.getExtensions()) {
      return;
    }

    for (final ExtensionSettings ext : settings.getExtensions().getExtensions()) {
      final Object object = SysUtils.loadFromModel(ext.getPath());
      final DataGenerator generator = DataGenerator.class.cast(object);
      registry.registerGenerator(ext.getName(), generator);
    }
  }
}
