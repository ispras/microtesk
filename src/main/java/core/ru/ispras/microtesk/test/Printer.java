/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.decoder.BinaryWriter;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.ProcessingElement;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataDirective;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Output;
import ru.ispras.microtesk.utils.FileUtils;

/**
 * The Printer class is responsible for printing generated symbolic test programs (sequences of
 * concrete calls to a file and to the screen).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Printer {
  private final static int LINE_WIDTH = 100;

  private final Options options;
  private final Statistics statistics;

  private final File file;
  private final PrintWriter fileWritter;

  private final File binaryFile;
  private final BinaryWriter binaryWriter;

  private final String commentToken;
  private final String indentToken;
  private final String separatorToken;
  private final String separator;

  public static Printer newCodeFile(
      final Options options,
      final Statistics statistics) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(statistics);

    final String outDir = getOutDir(options);
    final String fileName = String.format(
        "%s_%04d", options.getValueAsString(Option.CODE_PRE), statistics.getPrograms());

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.CODE_EXT));

    final File binaryFile = options.getValueAsBoolean(Option.GENERATE_BINARY) ?
        FileUtils.newFile(outDir, fileName, options.getValueAsString(Option.BIN_EXT)) : null;

    final Printer printer = new Printer(options, statistics, file, binaryFile);
    return printer;
  }

  public static Printer newDataFile(
      final Options options,
      final Statistics statistics,
      final int dataFileIndex) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(statistics);
    InvariantChecks.checkGreaterOrEqZero(dataFileIndex);

    final String outDir = getOutDir(options);
    final String fileName = String.format(
        "%s_%04d", options.getValueAsString(Option.DATA_PRE), dataFileIndex);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.DATA_EXT));

    return new Printer(options, statistics, file, null);
  }

  public static Printer newExcHandlerFile(
      final Options options,
      final Statistics statistics,
      final String id) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(statistics);
    InvariantChecks.checkNotNull(id);

    final String outDir = getOutDir(options);
    final String fileName =
        options.getValueAsString(Option.EXCEPT_PRE) + (id.isEmpty() ? "" : "_" + id);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.CODE_EXT));

    final File binaryFile = options.getValueAsBoolean(Option.GENERATE_BINARY) ?
        FileUtils.newFile(outDir, fileName, options.getValueAsString(Option.BIN_EXT)) : null;

    final Printer printer = new Printer(options, statistics, file, binaryFile);
    return printer;
  }

  private static String getOutDir(final Options options) {
    return options.hasValue(Option.OUTDIR) ?
        options.getValueAsString(Option.OUTDIR) : SysUtils.getHomeDir();
  }

  private Printer(
      final Options options,
      final Statistics statistics,
      final File file,
      final File binaryFile) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(statistics);
    InvariantChecks.checkNotNull(file);

    this.options = options;
    this.statistics = statistics;

    this.file = file;
    this.binaryFile = binaryFile;

    this.fileWritter = new PrintWriter(file);
    this.binaryWriter = null != binaryFile ? new BinaryWriter(binaryFile) : null;

    this.commentToken = options.getValueAsString(Option.COMMENT_TOKEN);
    this.indentToken = options.getValueAsString(Option.INDENT_TOKEN);
    this.separatorToken = options.getValueAsString(Option.SEPARATOR_TOKEN);
    this.separator = commentToken +
        newSeparator(LINE_WIDTH - commentToken.length() - commentToken.length(), separatorToken);

    printFileHeader();
  }

  public String getFileName() {
    return file.getName();
  }

  public void close() {
    if (null != fileWritter) {
      fileWritter.close();
    }

    if (null != binaryWriter) {
      binaryWriter.close();
    }
  }

  public void delete() {
    file.delete();
    if (null != binaryFile) {
      binaryFile.delete();
    }
  }

  private void printFileHeader() {
    if (options.getValueAsBoolean(Option.COMMENTS_ENABLED)) {
      // Prints MicroTESK information to the file (as the top file header).
      printToFile(separator);
      printCommentToFile("");
      printCommentToFile("This test program was automatically generated by the MicroTESK tool");
      printCommentToFile(String.format("Generation started: %s", new Date()));
      printCommentToFile("");
      printCommentToFile("Institute for System Programming of the Russian Academy of Sciences (ISP RAS)");
      printCommentToFile("25 Alexander Solzhenitsyn st., Moscow, 109004, Russia");
      printCommentToFile("http://forge.ispras.ru/projects/microtesk");
      printCommentToFile("");
      printToFile(separator);
    }
  }

  private static String newSeparator(final int length, final String token) {
    final StringBuilder sb = new StringBuilder();
    while (sb.length() < length / token.length()) {
      sb.append(token);
    }
    return sb.toString();
  }

  /**
   * Prints the specified instruction call sequence.
   * 
   * @param sequence Instruction call sequence.
   * @throws NullPointerException if the parameter is null.
   * @throws ConfigurationException if failed to evaluate one of the output objects associated with
   *         an instruction call in the sequence.
   */
  public void printSequence(
      final ProcessingElement observer,
      final TestSequence sequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(observer);
    InvariantChecks.checkNotNull(sequence);

    statistics.pushActivity(Statistics.Activity.PRINTING);

    try {
      final List<ConcreteCall> prologue = sequence.getPrologue();
      if (!prologue.isEmpty()) {
        printNote("Preparation");
        printCalls(observer, prologue);

        printText("");
        printNote("Stimulus");
      }

      printCalls(observer, sequence.getBody());
    } finally {
      statistics.popActivity(); // PRINTING
    }
  }

  /**
   * Prints the specified list of calls (all attributes applicable at generation time).
   * 
   * @param calls List of calls.
   * @throws ConfigurationException if failed to evaluate one of the output objects
   *         associated with an instruction call.
   */
  private void printCalls(
      final ProcessingElement observer,
      final List<ConcreteCall> calls) throws ConfigurationException {
    if (calls.isEmpty()) {
      printNote("Empty");
      return;
    }

    for (final ConcreteCall call : calls) {
      if (call.getOrigin() != null) {
        printText(String.format(
            options.getValueAsString(Option.ORIGIN_FORMAT), call.getOrigin()));
      }

      if (call.getAlignment() != null) {
        printText(String.format(
            options.getValueAsString(Option.ALIGN_FORMAT), call.getAlignment()));
      }

      printOutputs(observer, call.getOutputs());
      printLabels(call.getLabels());

      final boolean writeToFile = null != fileWritter;
      final String text = call.getText();

      if (null != text) {
        printText(text);
        if (writeToFile) {
          statistics.incInstructions();
        }
      }

      if (writeToFile && null != binaryWriter) {
        final String image = call.getImage();
        if (null != image && !image.isEmpty()) {
          binaryWriter.write(image);
        }
      }
    }
  }


  private void printOutputs(
      final ProcessingElement observer,
      final List<Output> outputs) throws ConfigurationException {
    InvariantChecks.checkNotNull(outputs);

    for (final Output output : outputs) {
      if (output.isRuntime()) {
        continue;
      }

      final boolean printComment =
          options.getValueAsBoolean(Option.COMMENTS_ENABLED) &&
          options.getValueAsBoolean(Option.COMMENTS_DEBUG);

      String text = output.evaluate(observer);
      switch (output.getKind()) {
        case COMMENT:
          text = commentToken + " " + text;
          break;

        case COMMENT_ML_START:
          text = options.getValueAsString(Option.COMMENT_TOKEN_START) + text;
          break;

        case COMMENT_ML_END:
          text = text + options.getValueAsString(Option.COMMENT_TOKEN_END);
          break;
      }

      if (output.isComment() && !printComment) {
        printToScreen(text);
      } else {
        printToScreen(text);
        printToFile(text);
      }
    }
  }

  private void printLabels(final List<Label> labels) {
    InvariantChecks.checkNotNull(labels);

    for (final Label label : labels) {
      printTextNoIndent(label.getUniqueName() + ":");
    }
  }

  /**
   * Prints the specified text to the screen (as is) and to the file (a comment).
   * The text is followed by an empty line. Note specify parts of code that need
   * a comment on their purpose. 
   * 
   * @param text Text to be printed.
   */
  private void printNote(final String text) {
    printToScreen(text);
    if (options.getValueAsBoolean(Option.COMMENTS_ENABLED)) {
      printCommentToFile(text);
    }
  }

  /**
   * Prints text both to the file and to the screen (if corresponding options are enabled).
   * @param text Text to be printed.
   */
  private void printText(final String text) {
    if (text != null) {
      printToScreen(text);
      printToFile(text);
    }
  }

  /**
   * Prints text with no indent both to the file and to the screen (if corresponding options are
   * enabled).
   * 
   * @param text Text to be printed.
   */
  private void printTextNoIndent(final String text) {
    if (text != null) {
      printToScreen(text);
      printToFileNoIndent(text);
    }
  }

  /**
   * Prints a special header comment that specifies the start of a code section
   * (sections include: data definitions, initialization, finalization and main code). 
   * 
   * @param text Text of the header.
   */
  private void printHeaderToFile(final String text) {
    if (options.getValueAsBoolean(Option.COMMENTS_ENABLED)) {
      printToFile("");
      printSeparatorToFile();
      printCommentToFile(text);
      printSeparatorToFile();
      printToFile("");
    }
  }

  /**
   * Prints a special header comment that specifies the start of a logically separate
   * part of code. 
   * 
   * @param text Text of the header.
   */
  public void printSubheaderToFile(final String text) {
    if (options.getValueAsBoolean(Option.COMMENTS_ENABLED)) {
      printToFile("");
      printSeparatorToFile();
      printCommentToFile(text);
      printToFile("");
    }
  }

  /**
   * Prints a comment to the file.
   * 
   * @param text Text of the comment to be printed.
   */
  private void printCommentToFile(final String text) {
    if (text != null) {
      printToFile(
          String.format("%s%s%s", commentToken, commentToken.endsWith(" ") ? "" : " ", text));
    }
  }

  /**
   * Prints a special comment (a line of '*' characters) to the file to
   * separate different parts of the code.
   */
  private void printSeparatorToFile() {
    if (options.getValueAsBoolean(Option.COMMENTS_ENABLED)) {
      printToFile(separator);
    }
  }

  /**
   * Prints a special comment to the file to separate different parts of the code.
   * 
   * @param text Text of the separator.
   */
  private void printSeparatorToFile(final String text) {
    if (!options.getValueAsBoolean(Option.COMMENTS_ENABLED)) {
      return;
    }

    final int prefixLength = (LINE_WIDTH - text.length()) / 2;
    final int postfixLength = LINE_WIDTH - prefixLength - text.length();
    final StringBuilder sb = new StringBuilder();

    sb.append(commentToken);
    sb.append(newSeparator(prefixLength - commentToken.length() - 1, separatorToken));
    sb.append(' ');
    sb.append(text);
    sb.append(' ');
    sb.append(newSeparator(postfixLength - 1, separatorToken));

    printToFile(sb.toString());
  }

  private void printToScreen(final String text) {
    Logger.debug(text);
  }

  private void printToFile(final String text) {
    if (null != fileWritter && null != text) {
      if (text.isEmpty()) {
        fileWritter.println();
      } else {
        fileWritter.print(indentToken);
        fileWritter.println(text);
      }
    }
  }

  private void printToFileNoIndent(final String text) {
    if (null != fileWritter) {
      fileWritter.println(text);
    }
  }

  public void printDataDirectives(final List<DataDirective> directives) {
    for (final DataDirective directive : directives) {
      final String text = directive.getText();
      if (directive.needsIndent()) {
        printToScreen(indentToken + text);
        printToFile(text);
      } else {
        printTextNoIndent(text);
      }
    }
  }

  public void printData(
      final String headerText,
      final List<DataSection> globalData,
      final List<DataSection> localData) {
    InvariantChecks.checkNotNull(globalData);
    InvariantChecks.checkNotNull(localData);

    statistics.pushActivity(Statistics.Activity.PRINTING);

    Logger.debugHeader("Printing Data to %s", getFileName());
    printHeaderToFile("Data");

    printToScreen(indentToken + headerText);
    printToFile(headerText);

    if (!globalData.isEmpty()) {
      printToFile("");
      printSeparatorToFile("Global Data");
    }

    for (final DataSection item : globalData) {
      printDataDirectives(item.getDirectives());
    }

    if (!localData.isEmpty()) {
      printToFile("");
      printSeparatorToFile("Test Case Data");
    }

    int currentTestCaseIndex = -1;
    for (final DataSection data : localData) {
      final List<DataDirective> directives = data.getDirectives();
      final int index = data.getSequenceIndex();

      if (index != currentTestCaseIndex) {
        currentTestCaseIndex = index;
        printSubheaderToFile(String.format("Test Case %d", currentTestCaseIndex));
      }

      printDataDirectives(directives);
    }

    statistics.popActivity();
  }
}
