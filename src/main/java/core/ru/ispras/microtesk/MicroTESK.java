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

import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.translator.Translator;

public final class MicroTESK {
  private MicroTESK() {}

  public static void main(String[] args) {
    final Parameters params;

    try {
      params = new Parameters(args);
    } catch (ParseException e) {
      Logger.error("Incorrect command line: " + e.getMessage());
      Parameters.help();
      return;
    }

    if (params.hasOption(Parameters.HELP)) {
      Parameters.help();
      return;
    }

    if (params.hasOption(Parameters.VERBOSE)) {
      Logger.setDebug(true);
    }

    try {
      if (params.hasOption(Parameters.GENERATE)) {
        generate(params);
      } else {
        translate(params);
      }
    } catch (ParseException e) {
      Logger.error("Incorrect command line or configuration file: " + e.getMessage());
      Parameters.help();
    } catch (Throwable e) {
      Logger.exception(e);
    }
  }

  private static void translate(final Parameters params) throws RecognitionException {
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

  private static void generate(final Parameters params) throws ParseException, Throwable {
    if (params.hasOption(Parameters.RANDOM)) {
      final int randomSeed = params.getOptionValueAsInt(Parameters.RANDOM);
      TestEngine.setRandomSeed(randomSeed);
    } else {
      reportUndefinedOption(Parameters.RANDOM);
    }
 
    if (params.hasOption(Parameters.LIMIT)) {
      final int branchExecutionLimit = params.getOptionValueAsInt(Parameters.LIMIT);
      TestEngine.setBranchExecutionLimit(branchExecutionLimit);
    } else {
      reportUndefinedOption(Parameters.LIMIT);
    }

    if (params.hasOption(Parameters.SOLVER)) {
      TestEngine.setSolver(params.getOptionValue(Parameters.SOLVER));
    }

    if (params.hasOption(Parameters.CODE_EXT)) {
      TestEngine.setCodeFileExtension(params.getOptionValue(Parameters.CODE_EXT));
    } else {
      reportUndefinedOption(Parameters.CODE_EXT);
    }
 
    if (params.hasOption(Parameters.CODE_PRE)) {
      TestEngine.setCodeFilePrefix(params.getOptionValue(Parameters.CODE_PRE));
    } else {
      reportUndefinedOption(Parameters.CODE_PRE);
    }

    if (params.hasOption(Parameters.CODE_LIMIT)) {
      final int programLengthLimit = params.getOptionValueAsInt(Parameters.CODE_LIMIT);
      TestEngine.setProgramLengthLimit(programLengthLimit);
    } else {
      reportUndefinedOption(Parameters.CODE_LIMIT);
    }

    if (params.hasOption(Parameters.TRACE_LIMIT)) {
      final int traceLengthLimit = params.getOptionValueAsInt(Parameters.TRACE_LIMIT);
      TestEngine.setTraceLengthLimit(traceLengthLimit);
    } else {
      reportUndefinedOption(Parameters.TRACE_LIMIT);
    }

    final String[] args = params.getArgs();
    if (args.length != 2) {
      Logger.error("Wrong number of generator arguments. Two arguments are required.");
      Logger.message("Argument format: <model name>, <template file>");
      return;
    }

    final String modelName = args[0];
    final String templateFile = args[1];

    TestEngine.generate(modelName, templateFile);
  }

  private static void reportUndefinedOption(final Option option) {
    Logger.warning("The --%s option is undefined.", option.getLongOpt());
  }
}
