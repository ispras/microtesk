/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.options;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link OptionReader} class reads options from command line or
 * from table of cached values.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class OptionReader {
  private final Map<String, String> configuration;
  private final String[] commandLineArgs;
  private final org.apache.commons.cli.Options commandLineOptions;

  private org.apache.commons.cli.CommandLine commandLine;
  private Options options;

  public OptionReader(
      final Map<String, String> configuration,
      final String[] commandLineArgs) {
    InvariantChecks.checkNotNull(commandLineArgs);
    InvariantChecks.checkNotNull(configuration);

    this.configuration = configuration;
    this.commandLineArgs = commandLineArgs;
    this.commandLineOptions = newCommandLineOptions(Arrays.asList(Option.values()));

    this.commandLine = null;
    this.options = null;
  }

  public void read() throws Exception {
    this.commandLine = parseCommandLine(commandLineArgs);
    this.options = new Options();

    for (final Option option : Option.values()) {
      final Object value = getOptionValue(option);
      if (null != value) {
        options.setValue(option, value);
      }
    }
  }

  public Options getOptions() {
    return options;
  }

  public String[] getArguments() {
    return null != commandLine ? commandLine.getArgs() : null;
  }

  public String getHelpText() {
    final org.apache.commons.cli.HelpFormatter formatter =
        new org.apache.commons.cli.HelpFormatter();

    final java.io.StringWriter writer =
        new java.io.StringWriter();

    formatter.printHelp(
        new java.io.PrintWriter(writer),
        100,
        "[options] Files to be processed",
        "",
        commandLineOptions,
        0,
        0,
        ""
        );
    return writer.toString();
  }

  private Object getOptionValue(final Option option) {
    InvariantChecks.checkNotNull(option);

    if (commandLine.hasOption(option.getShortName())) {
      return option.isFlag() ?
          true :
          parseValue(option.getValueClass(), commandLine.getOptionValue(option.getShortName()));
    }

    if (configuration.containsKey(option.getName())) {
      return parseValue(option.getValueClass(), configuration.get(option.getName()));
    }

    return null;
  }

  private static Object parseValue(final Class<?> valueClass, final String value) {
    InvariantChecks.checkNotNull(valueClass);
    InvariantChecks.checkNotNull(value);

    if (valueClass.equals(String.class)) {
      return value;
    }

    if (valueClass.equals(Integer.class)) {
      return Integer.valueOf(value);
    }

    if (valueClass.equals(BigInteger.class)) {
      return new BigInteger(value);
    }

    if (valueClass.equals(Boolean.class)) {
      return value.isEmpty() ? true : Boolean.valueOf(value);
    }

    throw new IllegalArgumentException(String.format(
        "Failed to parse the %s value as %s.", value, valueClass.getSimpleName()));
  }

  private org.apache.commons.cli.CommandLine parseCommandLine(
      final String[] args) throws org.apache.commons.cli.ParseException {
    InvariantChecks.checkNotNull(args);
    final org.apache.commons.cli.CommandLineParser parser = new org.apache.commons.cli.GnuParser();
    return parser.parse(commandLineOptions, args);
  }

  private static org.apache.commons.cli.Options newCommandLineOptions(final List<Option> options) {
    InvariantChecks.checkNotNull(options);
    final org.apache.commons.cli.Options result = new org.apache.commons.cli.Options();

    final Map<String, List<Option>> groups = new LinkedHashMap<>();
    for (final Option option : options) {
      final String groupName = option.getGroupName();
      if (null == groupName) {
        result.addOption(newCommandLineOption(option));
      } else {
        List<Option> groupOptions = groups.get(groupName);
        if (null == groupOptions) {
          groupOptions = new ArrayList<>();
          groups.put(groupName, groupOptions);
        }
        groupOptions.add(option);
      }
    }

    for (final List<Option> groupOptions : groups.values()) {
      result.addOptionGroup(newCommandLineOptionGroup(groupOptions));
    }

    return result;
  }

  private static org.apache.commons.cli.Option newCommandLineOption(
      final Option option) {
    InvariantChecks.checkNotNull(option);
    return new org.apache.commons.cli.Option(
        option.getShortName(), option.getName(), !option.isFlag(), option.getDescription());
  }

  private static org.apache.commons.cli.OptionGroup newCommandLineOptionGroup(
      final List<Option> options) {
    InvariantChecks.checkNotNull(options);
    final org.apache.commons.cli.OptionGroup result = new org.apache.commons.cli.OptionGroup();

    for (final Option option : options) {
      result.addOption(newCommandLineOption(option));
    }

    return result;
  }
}
