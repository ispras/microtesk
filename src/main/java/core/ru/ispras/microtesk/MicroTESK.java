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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import ru.ispras.fortress.solver.Environment;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.SettingsParser;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.utils.FileUtils;

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
      System.exit(-1);
    } catch (Throwable e) {
      Logger.exception(e);
      System.exit(-1);
    }
  }

  private static void translate(final Parameters params) throws RecognitionException {
    final List<Translator<?>> translators = Config.loadTranslators();
    for (final Translator<?> translator : translators) {
      if (params.hasOption(Parameters.INCLUDE)) {
        translator.addPath(params.getOptionValue(Parameters.INCLUDE));
      }

      if (params.hasOption(Parameters.OUTDIR)) {
        translator.setOutDir(params.getOptionValue(Parameters.OUTDIR));
      }

      for (final String fileName : params.getArgs()) {
        final String fileDir = FileUtils.getFileDir(fileName);
        if (null != fileDir) {
          translator.addPath(fileDir);
        }
      }

      translator.start(params.getArgs());
    }

    // Copy user-defined Java code is copied to the output folder.
    if (params.hasOption(Parameters.EXTDIR)) {
      final String extensionDir = params.getOptionValue(Parameters.EXTDIR);
      final File extensionDirFile = new File(extensionDir);

      if (!extensionDirFile.exists() || !extensionDirFile.isDirectory()) {
        Logger.error("The extension folder %s does not exists or is not a folder.", extensionDir);
        return;
      }

      final String outDir = (params.hasOption(Parameters.OUTDIR) ?
          params.getOptionValue(Parameters.OUTDIR) : PackageInfo.DEFAULT_OUTDIR) + "/src/java";

      final File outDirFile = new File(outDir);

      try {
        FileUtils.copyDirectory(extensionDirFile, outDirFile);
        Logger.message("Copied %s to %s", extensionDir, outDir);
      } catch (IOException e) {
        Logger.error("Failed to copy %s to %s", extensionDir, outDir);
      }
    }
  }

  private static void generate(final Parameters params) throws ParseException, Throwable {
    final String[] args = params.getArgs();
    if (args.length != 2) {
      Logger.error("Wrong number of generator arguments. Two arguments are required.");
      Logger.message("Argument format: <model name>, <template file>");
      return;
    }

    final String modelName = args[0];
    final String templateFile = args[1];

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

    if (params.hasOption(Parameters.DATA_EXT)) {
      TestEngine.setDataFileExtension(params.getOptionValue(Parameters.DATA_EXT));
    } else {
      reportUndefinedOption(Parameters.DATA_EXT);
    }
 
    if (params.hasOption(Parameters.DATA_PRE)) {
      TestEngine.setDataFilePrefix(params.getOptionValue(Parameters.DATA_PRE));
    } else {
      reportUndefinedOption(Parameters.DATA_PRE);
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

    TestEngine.setCommentsEnabled(params.hasOption(Parameters.COMMENTS_ENABLED));
    TestEngine.setCommentsDebug(params.hasOption(Parameters.COMMENTS_DEBUG));

    if (params.hasOption(Parameters.SOLVER_DEBUG)) {
      Environment.setDebugMode(true);
    }

    if (params.hasOption(Parameters.TARMAC_LOG)) {
      TestEngine.setTarmacLog(true);
    }

    if (params.hasOption(Parameters.ARCH_DIRS)) {
      final String archDirs = params.getOptionValue(Parameters.ARCH_DIRS);
      final String[] archDirsArray = archDirs.split(":");

      for (final String archDir : archDirsArray) {
        final String[] archDirArray = archDir.split("=");

        if (archDirArray != null && archDirArray.length > 1 && modelName.equals(archDirArray[0])) {
          final File archFile = new File(archDirArray[1]);

          final String archPath = archFile.isAbsolute() ? archDirArray[1] : String.format("%s%s%s",
              SysUtils.getHomeDir(), File.separator, archDirArray[1]); 

          final GeneratorSettings settings = SettingsParser.parse(archPath);
          TestEngine.setGeneratorSettings(settings);
        }
      }
    }

    final TestEngine.Statistics statistics = TestEngine.STATISTICS;
    statistics.reset();

    final Date startTime = TestEngine.generate(modelName, templateFile);
    if (null == startTime) {
      return;
    }
    
    final Date endTime = new Date();

    long time = endTime.getTime() - startTime.getTime();
    final long useconds = time % 1000;
    final long seconds = (time /= 1000) % 60; 
    final long minutes = (time /= 60) % 60;
    final long hours = time / 60;

    final StringBuilder sb = new StringBuilder();
    if (hours != 0) {
      sb.append(String.format("%d hours ", hours));
    }
    if (hours != 0 || minutes != 0) {
      sb.append(String.format("%d minutes ", minutes));
    }
    sb.append(String.format("%d.%03d seconds ", seconds, useconds));

    Logger.message("Generation Statistics");
    Logger.message("Generation time: %s", sb.toString());

    final long rate = (1000 * statistics.instructionCount) /
                       (endTime.getTime() - startTime.getTime());
    Logger.message("Generation rate: %d instructions/second", rate);

    Logger.message("Programs/stimuli/instructions: %d/%d/%d",
        statistics.testProgramNumber,
        statistics.testCaseNumber,
        statistics.instructionCount
        );

    if (params.hasOption(Parameters.RATE_LIMIT)) {
      final long rateLimit = params.getOptionValueAsInt(Parameters.RATE_LIMIT);
      if (rate < rateLimit && statistics.instructionCount >= 1000) { 
        // Makes sense only for sequences of significant length (>= 1000)
        Logger.error("Generation rate is too slow. At least %d is expected.", rateLimit);
        System.exit(-1);
      }
    }
  }

  private static void reportUndefinedOption(final Option option) {
    Logger.warning("The --%s option is undefined.", option.getLongOpt());
  }
}
