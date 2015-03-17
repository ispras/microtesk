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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ru.ispras.microtesk.translator.simnml.SimnMLAnalyzer;

public final class MicroTESK {
  private MicroTESK() {}

  private static class Parameters {
    public static final String INCLUDE = "i";
    public static final String HELP = "h";
    public static final String OUTDIR = "d";
    public static final String TESTSIT = "s";

    private static final Options options = new Options();

    static {
      options.addOption(HELP, "help", false, "Shows this message");
      options.addOption(INCLUDE, "include", true, "Sets include files directories");
      options.addOption(OUTDIR, "dir", true, "Sets where to place generated Java files");
      options.addOption(TESTSIT, "sit", true, "Sets the location of user-defined test situations");
    };

    private Parameters() {}

    public static CommandLine parse(String[] args) throws ParseException {
      final CommandLineParser parser = new GnuParser();
      return parser.parse(options, args);
    }

    public static void help() {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(80, "[options] nML-files", "", options, "");
    }
  }

  public static void main(String[] args) {
    final SimnMLAnalyzer analyzer = new SimnMLAnalyzer();

    try {
      final CommandLine params = Parameters.parse(args);

      if (params.hasOption(Parameters.HELP)) {
        Parameters.help();
        return;
      }

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
    } catch (ParseException e) {
      System.out.println("Error: " + e.getMessage());
      Parameters.help();
    }
  }
}
