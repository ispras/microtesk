/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.TestProgramGenerator;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.utils.FileUtils;

public final class MicroTESK {
  private MicroTESK() {}

  public static void main(String[] args) {
    final Parameters params;

    try {
      params = new Parameters(args);
    } catch (ParseException e) {
      Logger.error("Wrong command line: " + e.getMessage());
      Parameters.help();
      return;
    }

    if (params.hasOption(Parameters.HELP)) {
      Parameters.help();
      return;
    }

    if (params.hasOption(Parameters.VERBOSE)) {
      Logger.setDebug(true);
      return;
    }

    try {
      final Map<String, String> settings = Config.loadSettings();
      InvariantChecks.checkNotNull(settings);

      if (params.hasOption(Parameters.GENERATE)) {
        generate(params, settings);
      } else {
        translate(params, settings);
      }
    } catch (Throwable e) {
      Logger.exception(e);
    }
  }

  private static void translate(
      final Parameters params,
      final Map<String, String> settings)
      throws RecognitionException {

    final List<Translator<?>> translators = Config.loadTranslators();
    for (Translator<?> translator : translators) {
      if (params.hasOption(Parameters.INCLUDE)) {
        translator.addPath(params.getOptionValue(Parameters.INCLUDE));
      }

      if (params.hasOption(Parameters.OUTDIR)) {
        translator.setOutDir(params.getOptionValue(Parameters.OUTDIR));
      }

      translator.start(params.getArgs());
    }
  }

  private static void generate(
      final Parameters params,
      final Map<String, String> settings)
      throws Throwable {

    final TestProgramGenerator generator = new TestProgramGenerator();

    final String random = getSetting(Parameters.RANDOM, params, settings);
    if (null != random) {
      try {
        final int seed = Integer.parseInt(random);
        generator.setRandomSeed(seed);
      } catch (NumberFormatException e) {
        Logger.warning("Failed to parse the value of the -r parameter: " + random);
      }
    }

    final String limitStr = getSetting(Parameters.LIMIT, params, settings);
    if (null != limitStr) {
      try {
        final int limitVal = Integer.parseInt(limitStr);
        generator.setBranchExecutionLimit(limitVal);
      } catch (NumberFormatException e) {
        Logger.warning("Failed to parse the value of the -l parameter: " + limitStr);
      }
    }

    if (params.hasOption(Parameters.SOLVER)) {
      generator.setSolver(params.getOptionValue(Parameters.SOLVER));
    }

    final String[] args = params.getArgs();
    if (args.length < 2) {
      Logger.error("Wrong number of generator arguments. At least two are required.");
      Logger.message("Argument format: <model name>, <template files>[, <output file>]");
      return;
    }

    final String modelName = args[0];
    generator.setModelName(modelName);

    final List<String> templateFiles = new ArrayList<>();
    for (int index = 1; index < args.length; ++index) {
      final String fileName = args[index];
      if (".rb".equals(FileUtils.getFileExtension(fileName))) {
        templateFiles.add(fileName);
      } else if (index == args.length - 1) {
        generator.setFileName(fileName);
      }
    }

    generator.generate(templateFiles);
  }

  private static String getSetting(
      final Option option,
      final Parameters params,
      final Map<String, String> settings) {

    if (params.hasOption(option)) {
      return params.getOptionValue(option);
    }

    final String optionName = option.getLongOpt();
    return settings.get(optionName);
  }
}
