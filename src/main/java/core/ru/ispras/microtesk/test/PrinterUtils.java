/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.template.DataSection;

import java.io.IOException;
import java.util.List;

/**
 * The {@link PrinterUtils} class provides utility methods for printing test programs and their
 * parts to files and to console.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class PrinterUtils {
  public static void printSequenceToConsole(
      final EngineContext engineContext,
      final ConcreteSequence sequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(sequence);

    if (engineContext.getOptions().getValueAsBoolean(Option.VERBOSE)) {
      Logger.debugHeader("Constructed %s", sequence.getTitle());

      final Printer consolePrinter =
          Printer.getConsole(engineContext.getOptions(), engineContext.getStatistics());

      consolePrinter.printSequence(engineContext.getModel(), sequence);
    }
  }

  public static void printDataSection(
      final EngineContext engineContext,
      final DataSection data) throws IOException, ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(data);

    final Statistics statistics = engineContext.getStatistics();
    statistics.pushActivity(Statistics.Activity.PRINTING);

    Printer printer = null;
    try {
      printer = Printer.newDataFile(engineContext.getOptions(), statistics.getDataFiles());
      Logger.debugHeader("Printing data to %s", printer.getFileName());
      printer.printData(data);
      statistics.incDataFiles();
    } finally {
      if (null != printer) {
        printer.close();
      }
      statistics.popActivity();
    }
  }

  public static void printExceptionHandler(
      final EngineContext engineContext,
      final String id,
      final List<ConcreteSequence> sequences) throws IOException, ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(sequences);

    final Statistics statistics = engineContext.getStatistics();
    statistics.pushActivity(Statistics.Activity.PRINTING);

    Printer printer = null;
    try {
      printer = Printer.newExcHandlerFile(engineContext.getOptions(), id);
      Logger.debugHeader("Printing exception handler to %s", printer.getFileName());
      for (final ConcreteSequence sequence : sequences) {
        statistics.incInstructions(sequence.getInstructionCount());
        printer.printSequence(engineContext.getModel(), sequence);
      }
    } finally {
      if (null != printer) {
        printer.close();
      }
      statistics.popActivity();
    }
  }

  public static void printTestProgram(
      final EngineContext engineContext,
      final TestProgram testProgram) throws ConfigurationException, IOException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testProgram);

    final Statistics statistics = engineContext.getStatistics();
    statistics.pushActivity(Statistics.Activity.PRINTING);

    Printer printer = null;
    try {
      printer = Printer.newCodeFile(engineContext.getOptions(), statistics.getPrograms());
      Logger.debugHeader("Printing test program to %s", printer.getFileName());

      for (final ConcreteSequence sequence : testProgram.getEntries()) {
        printer.printSequence(engineContext.getModel(), sequence);
      }

      printer.printData(testProgram.getAllData());
      statistics.incPrograms();
    } finally {
      if (null != printer) {
        printer.close();
      }
      statistics.popActivity();
    }
  }

  public static void printLinkerScript(final EngineContext engineContext) throws IOException {
    InvariantChecks.checkNotNull(engineContext);

    final Statistics statistics = engineContext.getStatistics();
    statistics.pushActivity(Statistics.Activity.PRINTING);

    try {
      final LinkerScriptPrinter printer = new LinkerScriptPrinter(engineContext.getOptions());
      Logger.debugHeader("Printing linker script to %s", printer.getFileName());
      printer.print();
    } finally {
      statistics.popActivity();
    }
  }
}
