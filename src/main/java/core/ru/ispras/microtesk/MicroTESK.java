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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ru.ispras.microtesk.translator.simnml.SimnMLAnalyzer;

public final class MicroTESK {
  private MicroTESK() {}

  private static class Parameters {
    public static final String INCLUDE = "i";
    public static final String HELP = "h";
    public static final String OUTDIR = "d";
    public static final String MODEL = "m";
    public static final String GENERATE = "g";
    public static final String TRANSLATE = "t";
    public static final String VERBOSE = "v";
    public static final String FILE = "f";

    private static final Options options = newOptions();

    private static Options newOptions() {
      final String TOPT = " [works with -t]";
      final String GOPT = " [works with -g]";

      final Options result = new Options();
      result.addOption(HELP, "help", false, "Shows this message");

      final OptionGroup actions = new OptionGroup(); 
      actions.addOption(new Option(GENERATE, "generate", false, "Generates test programs"));
      actions.addOption(new Option(TRANSLATE, "translate", false, "Translates formal specifications"));
      result.addOptionGroup(actions);

      result.addOption(INCLUDE, "include", true, "Sets include files directories" + TOPT);
      result.addOption(OUTDIR, "dir", true, "Sets where to place generated files" + TOPT);
      result.addOption(MODEL, "model", true, "Sets model used to generate test programs" + GOPT);
      result.addOption(FILE, "file", true, "Sets file name of generated test program" + GOPT);

      result.addOption(VERBOSE, "verbose", false, "Enables printing diagnostic messages");
      return result;
    }

    private Parameters() {}

    public static CommandLine parse(String[] args) throws ParseException {
      final CommandLineParser parser = new GnuParser();
      return parser.parse(options, args);
    }

    public static void help() {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(80, "[options] Files to be processed", "", options, "");
    }
  }

  public static void main(String[] args) {
    final CommandLine params;

    try {
      params = Parameters.parse(args);
    } catch (ParseException e) {
      System.out.println("Wrong command line: " + e.getMessage());
      Parameters.help();
      return;
    }

    if (params.hasOption(Parameters.HELP)) {
      Parameters.help();
      return;
    }

    if (params.hasOption(Parameters.GENERATE)) {
      generate(params);
      return;
    }

    translate(params);
  }

  private static void translate(CommandLine params) {
    final SimnMLAnalyzer analyzer = new SimnMLAnalyzer();

    if (params.hasOption(Parameters.INCLUDE)) {
      analyzer.addPath(params.getOptionValue(Parameters.INCLUDE));
    }

    if (params.hasOption(Parameters.OUTDIR)) {
      analyzer.setOutDir(params.getOptionValue(Parameters.OUTDIR));
    }

    try {
      analyzer.start(params.getArgs());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void generate(CommandLine params) {
    // TODO
  }
}
