/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@code Parameters} class is responsible for parsing the command line
 * and extracting command-line parameter values. All properties of the MicroTESK
 * command line are defined here.
 * 
 * @author Andrei Tatarnikov
 */

public final class Parameters {
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
  // Parsed Command Line and Settings from Configuration Files

  private final CommandLine commandLine;
  private final Map<String, String> settings;

  public Parameters(String[] args) throws ParseException {
    commandLine = parse(args);
    settings = Config.loadSettings();
  }

  public boolean hasOption(final Option option) {
    InvariantChecks.checkNotNull(option);

    if (commandLine.hasOption(option.getOpt())) {
      return true;
    }

    if (settings.containsKey(option.getOpt())) {
      return true;
    }

    return false;
  }

  public String getOptionValue(final Option option) {
    InvariantChecks.checkNotNull(option);

    if (commandLine.hasOption(option.getOpt())) {
      return commandLine.getOptionValue(option.getOpt());
    }

    if (settings.containsKey(option.getOpt())) {
      return settings.get(option.getOpt());
    }

    throw new IllegalStateException(String.format(
        "Failed to read the value of the %s option.", option.getLongOpt()));
  }
  
  public int getOptionValueAsInt(final Option option) throws ParseException {
    final String valueText = getOptionValue(option);
    final int value;

    try {
      value = Integer.parseInt(valueText);
    } catch (NumberFormatException e) {
      throw new ParseException(String.format(
          "Failed to parse the value of the --%s option as integer: %s",
          option.getLongOpt(), e.getMessage()));
    }

    return value;
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
      throw new IllegalArgumentException(String.format("--%s is already used!", name));
    }

    final String shortName = makeUniqueShortName(name);
    final String fullDescription = null == dependency ? description :
        String.format("%s [works with the --%s key]", description, dependency.getLongOpt());

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

  private static CommandLine parse(final String[] args) throws ParseException {
    final CommandLineParser parser = new GnuParser();
    return parser.parse(options, args);
  }

  public static void help() {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(80, "[options] Files to be processed", "", options, "");
  }
}
