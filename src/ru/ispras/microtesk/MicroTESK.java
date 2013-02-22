/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MicroTESK.java, Oct 19, 2012 3:50:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk;

import org.apache.commons.cli.*;
import ru.ispras.microtesk.translator.simnml.SimnMLAnalyzer;

public class MicroTESK
{
    private static class Parameters
    {
        public static final String INCLUDE = "i";
        public static final String HELP    = "h";

        private static Options options = new Options();

        static
        {
            options.addOption(INCLUDE, "include", true,  "Sets include files directories");
            options.addOption(HELP,    "help",    false, "Shows this message");
        };

        private Parameters() {}

        public static CommandLine parse(String[] args) throws ParseException
        {
            CommandLineParser parser = new GnuParser();
            return parser.parse(options, args);
        }

        public static void help()
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(80, "[options] verilog-files", "", options, "");
        }
    }

    public static void main(String[] args)
    {
        SimnMLAnalyzer analyzer = new SimnMLAnalyzer();

        try
        {
            CommandLine params = Parameters.parse(args);

            if(params.hasOption(Parameters.INCLUDE))
            {
                analyzer.addPath(params.getOptionValue(Parameters.INCLUDE));
            }

            if(params.hasOption(Parameters.HELP))
            {
                Parameters.help();
            }

            try
            {
                analyzer.start(params.getArgs());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        catch(ParseException e)
        {
            Parameters.help();
        }
    }
}
