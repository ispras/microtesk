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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.TestProgramGenerator;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.utils.FileUtils;

public final class MicroTESK {
  private MicroTESK() {}

  private static class Parameters {
    private static final Set<String> NAMES = new HashSet<>();
    private static final Set<String> SHORT_NAMES = new HashSet<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Flags

    public static final Option HELP = 
        newOption("help", false, "Shows help message");

    public static final Option VERBOSE =
        newOption("verbose", false, "Enables printing diagnostic messages");

    public static final Option TRANSLATE = 
        newOption("translate", false, "Translates formal specifications");

    public static final Option GENERATE = 
        newOption("generate", false, "Generates test programs");

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Translator Options

    public static final Option INCLUDE =
        newOption("include", true, "Sets include files directories", TRANSLATE);

    public static final Option OUTDIR =
        newOption("output-dir", true, "Sets where to place generated files", TRANSLATE);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Program Generation Options (File Creation)

    public static final Option RANDOM = 
        newOption("random-seed", true, "Sets seed for randomizer", GENERATE);

    public static final Option SOLVER = 
        newOption("solver", true, "Sets constraint solver engine to be used", GENERATE);

    public static final Option LIMIT =
        newOption("branch-exec-limit", true,
            "Sets the limit on control transfers to detect endless loops", GENERATE);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Program Generation Options (File Creation)

    public static final Option CODE_EXT = 
        newOption("code-file-extension", true, "The output file extension", GENERATE);

    public static final Option CODE_PRE =
        newOption("code-file-prefix", true,
            "The output file prefix (file names are as follows prefix_xxxx.ext, " + 
            "where xxxx is a 4-digit decimal number)", GENERATE);
     
    public static final Option DATA_EXT =
        newOption("data-file-extension", true, "The data file extension", GENERATE);

    public static final Option DATA_PRE = 
        newOption("data-file-prefix", true, "The data file prefix", GENERATE);

    public static final Option CODE_LIMIT = 
        newOption("program-length-limit", true,
            "The maximum number of instructions in output programs", GENERATE);
    
    public static final Option TRACE_LIMIT =
        newOption("trace-length-limit", true,
            "The maximum length of execution traces of output programs", GENERATE);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Options

    private static final Options options = newOptions();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Parsed Command Line

    final CommandLine commandLine;

    public Parameters(String[] args) throws ParseException {
      commandLine = parse(args);
    }

    public boolean hasOption(Option option) {
      InvariantChecks.checkNotNull(option);
      return commandLine.hasOption(option.getOpt());
    }

    public String getOptionValue(Option option) {
      InvariantChecks.checkNotNull(option);
      return commandLine.getOptionValue(option.getOpt());
    }

    public String[] getArgs() {
      return commandLine.getArgs();
    }

    private static Option newOption(
        final String name,
        final boolean hasArg,
        final String description) {
      return newOption(name, hasArg, description, null);
    }

    private static Option newOption(
        final String name,
        final boolean hasArg,
        final String description,
        final Option dependency) {
      InvariantChecks.checkNotNull(name);
      InvariantChecks.checkNotNull(description);

      if (NAMES.contains(name)) {
        throw new IllegalArgumentException(name + " is already used!");
      }

      final String shortName = makeUniqueShortName(name);
      final String fullDescription = null == dependency ? description :
          String.format("%s [works with %s]", description, dependency.getLongOpt());

      SHORT_NAMES.add(shortName);
      NAMES.add(name);

      return new Option(shortName, name, hasArg, fullDescription);
    }

    private static String makeUniqueShortName(final String name) {
      final String[] nameTokens = name.split("-");

      final StringBuilder sb = new StringBuilder();
      for (final String token : nameTokens) {
        sb.append(token.charAt(0));
      }

      while (SHORT_NAMES.contains(sb.toString())) {
        final String lastToken = nameTokens[nameTokens.length - 1];
        sb.append(lastToken.charAt(0));
      }

      return sb.toString();
    }

    private static Options newOptions() {
      final Options result = new Options();

      result.addOption(HELP);
      result.addOption(VERBOSE);

      final OptionGroup actions = new OptionGroup();
      actions.addOption(TRANSLATE);
      actions.addOption(GENERATE);
      result.addOptionGroup(actions);

      result.addOption(INCLUDE);
      result.addOption(OUTDIR);

      result.addOption(RANDOM);
      result.addOption(SOLVER);
      result.addOption(LIMIT);

      result.addOption(CODE_EXT);
      result.addOption(CODE_PRE);
      result.addOption(DATA_EXT);
      result.addOption(DATA_PRE);
      result.addOption(CODE_LIMIT);
      result.addOption(TRACE_LIMIT);

      return result;
    }

    private static CommandLine parse(String[] args) throws ParseException {
      final CommandLineParser parser = new GnuParser();
      return parser.parse(options, args);
    }

    public static void help() {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(80, "[options] Files to be processed", "", options, "");
    }
  }

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
