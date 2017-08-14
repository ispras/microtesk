/*
 * Copyright 2012-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.OptionReader;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.EngineConfig;
import ru.ispras.microtesk.test.engine.InitializerMaker;
import ru.ispras.microtesk.tools.Disassembler;
import ru.ispras.microtesk.tools.symexec.SymbolicExecutor;
import ru.ispras.microtesk.tools.templgen.TemplateGenerator;
import ru.ispras.microtesk.tools.transform.TraceTransformer;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorContext;
import ru.ispras.microtesk.utils.FileUtils;
import ru.ispras.testbase.TestBaseRegistry;
import ru.ispras.testbase.generator.DataGenerator;
import ru.ispras.testbase.stub.TestBase;

public final class MicroTESK {
  private MicroTESK() {}

  private static final List<Translator<?>> translators = new ArrayList<>();

  public static void main(final String[] args) {
    try {
      final OptionReader optionReader = new OptionReader(Config.loadSettings(), args);

      try {
        optionReader.read();
      } catch (final Exception e) {
        Logger.error("Incorrect command line: " + e.getMessage());
        Logger.message(optionReader.getHelpText());
        exitWithError();
      }

      final Options options = optionReader.getOptions();
      final String[] arguments = optionReader.getArguments();

      if (options.getValueAsBoolean(Option.HELP)) {
        Logger.message(optionReader.getHelpText());
        return;
      }

      Logger.setDebug(options.getValueAsBoolean(Option.VERBOSE));

      final List<Plugin> plugins = Config.loadPlugins();
      registerPlugins(plugins);

      if (!runTask(options, arguments, plugins)) {
        exitWithError();
      }
    } catch (final Throwable e) {
      Logger.exception(e);
      exitWithError();
    }
  }

  private static void exitWithError() {
    System.exit(-1);
  }

  private static void registerPlugins(final List<Plugin> plugins) {
    for (final Plugin plugin : plugins) {
      // Register the translator.
      final Translator<?> translator = plugin.getTranslator();
      if (translator != null) {
        translators.add(translator);
      }

      final EngineConfig generatorConfig = EngineConfig.get();

      // Register the engines.
      final Map<String, Engine> engines = plugin.getEngines();
      for (final Map.Entry<String, Engine> entry : engines.entrySet()) {
        generatorConfig.registerEngine(entry.getKey(), entry.getValue());
      }

      // Register the adapters.
      final Map<String, InitializerMaker> initializerMakers  = plugin.getInitializerMakers();
      for (final Map.Entry<String, InitializerMaker> entry : initializerMakers.entrySet()) {
        generatorConfig.registerInitializerMaker(entry.getKey(), entry.getValue());
      }

      // Register the data generators.
      final TestBaseRegistry testBaseRegistry = TestBase.get().getRegistry();
      final Map<String, DataGenerator> dataGenerators = plugin.getDataGenerators();

      if (dataGenerators != null) {
        for (final Map.Entry<String, DataGenerator> entry : dataGenerators.entrySet()) {
          testBaseRegistry.registerGenerator(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  private static boolean runTask(
      final Options options,
      final String[] arguments,
      final List<Plugin> plugins) throws Throwable {
    if (options.getValueAsBoolean(Option.GENERATE)) {
      return generate(options, arguments, plugins);
    } else if (options.getValueAsBoolean(Option.GENERATE_TEMPLATE)) {
      return generateTemplate(options, arguments);
    } else if (options.getValueAsBoolean(Option.DISASSEMBLE)) {
      return disassemble(options, arguments);
    } else if (options.getValueAsBoolean(Option.SYMBOLIC_EXECUTE)) {
      return symbolicExecute(options, arguments);
    } else if (options.getValueAsBoolean(Option.TRANSFORM_TRACE)) {
      return transformTrace(options, arguments);
    } else {
      return translate(options, arguments);
    }
  }

  private static boolean translate(final Options options, final String[] arguments) {
    final String revision = options.getValueAsString(Option.REVID);
    final Set<String> revisions = revision.isEmpty() ? Collections.<String>emptySet() :
                                                       Collections.<String>singleton(revision);

    final TranslatorContext context = new TranslatorContext();
    for (final Translator<?> translator : translators) {
      if (!translator.translate(options, context, revisions, arguments)) {
        Logger.message("Translation was aborted.");
        return false;
      }
    }

    // Copies user-defined Java code to the output folder.
    if(!copyExtensions(options)) {
      Logger.error("Failed to copy extensions.");
      return false;
    }

    return true;
  }

  private static boolean copyExtensions(final Options options) {
    if (options.hasValue(Option.EXTDIR)) {
      final String extensionDir = options.getValueAsString(Option.EXTDIR);
      final File extensionDirFile = new File(extensionDir);

      if (!extensionDirFile.exists() || !extensionDirFile.isDirectory()) {
        Logger.error("The extension folder %s does not exists or is not a folder.", extensionDir);
        return false;
      }

      final String outDir = options.getValueAsString(Option.OUTDIR) + "/src/java";
      final File outDirFile = new File(outDir);

      try {
        FileUtils.copyDirectory(extensionDirFile, outDirFile);
        Logger.message("Copied %s to %s", extensionDir, outDir);
      } catch (final IOException e) {
        Logger.error("Failed to copy %s to %s", extensionDir, outDir);
        return false;
      }
    }

    return true;
  }

  private static boolean generateTemplate(final Options options, final String[] arguments) {
    if (arguments.length != 1) {
      Logger.error("Wrong number of command-line arguments. One argument is required.");
      Logger.message("Argument format: <model name>");
      return false;
    }

    final String modelName = arguments[0];
    if (!TemplateGenerator.generate(options, modelName)) {
      Logger.message("Template generation was aborted.");
      return false;
    }

    return true;
  }

  private static boolean disassemble(final Options options, final String[] arguments) {
    if (!checkTwoArguments(arguments)) {
      return false;
    }

    final String modelName = arguments[0];
    final String inputFile = arguments[1];

    if (!Disassembler.disassemble(options, modelName, inputFile)) {
      Logger.message("Disassembling was aborted.");
      return false;
    }

    return true;
  }

  private static boolean symbolicExecute(final Options options, final String[] arguments) {
    if (!checkTwoArguments(arguments)) {
      return false;
    }

    final String modelName = arguments[0];
    final String inputFile = arguments[1];

    if (!SymbolicExecutor.execute(options, modelName, inputFile)) {
      Logger.message("Symbolic execution was aborted.");
      return false;
    }

    return true;
  }

  private static boolean transformTrace(final Options options, final String[] arguments) {
    if (!checkThreeArguments(arguments)) {
      return false;
    }

    final String modelName = arguments[0];
    final String templateName = arguments[1];
    final String traceName = arguments[2];

    if (!TraceTransformer.execute(options, modelName, templateName, traceName)) {
      Logger.message("Trace transformation was aborted.");
      return false;
    }

    return true;
  }

  private static boolean generate(
      final Options options,
      final String[] arguments,
      final List<Plugin> plugins) throws Throwable {
    if (!checkTwoArguments(arguments)) {
      return false;
    }

    final String modelName = arguments[0];
    final String templateFile = arguments[1];

    if (!TestEngine.generate(options, modelName, templateFile, plugins)) {
      return false;
    }

    return true;
  }

  private static boolean checkTwoArguments(final String[] arguments) {
    if (arguments.length == 2) {
      return true;
    }

    Logger.error("Wrong number of command-line arguments. Two are required.");
    Logger.message("Argument format: <model name>, <input file>");

    return false;
  }

  private static boolean checkThreeArguments(final String[] arguments) {
    if (arguments.length == 3) {
      return true;
    }

    Logger.error("Wrong number of command-line arguments. Two are required.");
    Logger.message("Argument format: <model name>, <input file 1>, <input file 2>");

    return false;
  }
}
