/*
 * Copyright 2014-2019 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.FileUtils;
import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.directive.Directive;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Output;
import ru.ispras.microtesk.utils.BinaryWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The {@link Printer} class is responsible for printing generated symbolic test programs
 * (sequences of concrete calls to a file and to the screen).
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Printer {
  private static final List<String> CUSTOM_HEADER = new ArrayList<>();
  private static final List<String> CUSTOM_FOOTER = new ArrayList<>();

  public static void addToHeader(final String text) {
    InvariantChecks.checkNotNull(text);
    CUSTOM_HEADER.add(text);
  }

  public static void addToFooter(final String text) {
    InvariantChecks.checkNotNull(text);
    CUSTOM_FOOTER.add(text);
  }

  private static final int LINE_WIDTH = 100;
  private static Printer console = null;

  private final Options options;
  private final boolean printToScreen;

  private final File file;
  private final PrintWriter fileWritter;

  private final File binaryFile;
  private final BinaryWriter binaryWriter;

  private final String commentToken;
  private final String indentToken;
  private final String separatorToken;
  private final String separator;

  private Section section = null;

  public static Printer getConsole(
      final Options options, final Statistics statistics) {
    if (null != console) {
      return console;
    }

    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(statistics);

    try {
      console = new Printer(options, true, null, null);
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }

    return console;
  }

  public static Printer newCodeFile(
      final Options options,
      final int fileIndex) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkGreaterOrEqZero(fileIndex);

    final String outDir = getOutDir(options);
    final String fileName = String.format(
        "%s_%04d", options.getValueAsString(Option.CODE_FILE_PREFIX), fileIndex);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.CODE_FILE_EXTENSION));

    final File binaryFile = newBinaryFile(outDir, fileName, options);
    return new Printer(options, false, file, binaryFile);
  }

  public static Printer newDataFile(
      final Options options,
      final int fileIndex) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkGreaterOrEqZero(fileIndex);

    final String outDir = getOutDir(options);
    final String fileName = String.format(
        "%s_%04d", options.getValueAsString(Option.DATA_FILE_PREFIX), fileIndex);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.DATA_FILE_EXTENSION));

    return new Printer(options, false, file, null);
  }

  public static Printer newSectionFile(
      final String name,
      final Options options,
      final int fileIndex) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkGreaterOrEqZero(fileIndex);

    final String outDir = getOutDir(options);
    final String fileName = String.format("%s_%04d", name, fileIndex);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.CODE_FILE_EXTENSION));

    return new Printer(options, false, file, null);
  }

  public static Printer newExceptionHandlerFile(
      final Options options,
      final String id) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(id);

    final String outDir = getOutDir(options);
    final String fileName =
        options.getValueAsString(Option.EXCEPT_FILE_PREFIX) + (id.isEmpty() ? "" : "_" + id);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.CODE_FILE_EXTENSION));

    final File binaryFile = newBinaryFile(outDir, fileName, options);
    return new Printer(options, false, file, binaryFile);
  }

  private static File newBinaryFile(
      final String outDir,
      final String fileName,
      final Options options) {
    if (!options.getValueAsBoolean(Option.GENERATE_BINARY)) {
      return null;
    }

    final String fileExtension = options.getValueAsString(Option.BINARY_FILE_EXTENSION);
    return FileUtils.newFile(outDir, fileName, fileExtension);
  }

  public static String getOutDir(final Options options) {
    InvariantChecks.checkNotNull(options);
    return options.hasValue(Option.OUTPUT_DIR)
        ? options.getValueAsString(Option.OUTPUT_DIR) : SysUtils.getHomeDir();
  }

  private Printer(
      final Options options,
      final boolean printToScreen,
      final File file,
      final File binaryFile) throws IOException {
    InvariantChecks.checkNotNull(options);

    this.options = options;
    this.printToScreen = printToScreen;
    this.file = file;
    this.binaryFile = binaryFile;

    this.fileWritter = null != file ? new PrintWriter(file) : null;
    final boolean bigEndian = options.getValueAsBoolean(Option.BINARY_FILE_BIG_ENDIAN);
    this.binaryWriter = null != binaryFile ? new BinaryWriter(binaryFile, bigEndian) : null;

    this.commentToken = options.getValueAsString(Option.COMMENT_TOKEN);
    this.indentToken = options.getValueAsString(Option.INDENT_TOKEN);
    this.separatorToken = options.getValueAsString(Option.SEPARATOR_TOKEN);
    this.separator = commentToken
        + newSeparator(LINE_WIDTH - indentToken.length() - commentToken.length(), separatorToken);

    printFileHeader();
  }

  public String getFileName() {
    return null != file ? file.getName() : null;
  }

  public void close() {
    printFileFooter();

    if (null != fileWritter) {
      fileWritter.close();
    }

    if (null != binaryWriter) {
      binaryWriter.close();
    }
  }

  public void delete() {
    if (null != file) {
      file.delete();
    }

    if (null != binaryFile) {
      binaryFile.delete();
    }
  }

  private void printFileHeader() {
    if (null != fileWritter && options.getValueAsBoolean(Option.COMMENTS_ENABLED)) {
      // Prints MicroTESK information to the file (as the top file header).
      printToFile(separator);
      printCommentToFile("");
      printCommentToFile("This test program was automatically generated by the MicroTESK tool");
      printCommentToFile(String.format("Generation started: %s", new Date()));
      printCommentToFile("");
      printCommentToFile(
          "Ivannikov Institute for System Programming"
           + " of the Russian Academy of Sciences (ISP RAS)");
      printCommentToFile("25 Alexander Solzhenitsyn st., Moscow, 109004, Russia");
      printCommentToFile("");
      printCommentToFile("http://www.microtesk.org");
      printCommentToFile("http://forge.ispras.ru/projects/microtesk");
      printCommentToFile("");
      printToFile(separator);

      for (final String text : CUSTOM_HEADER) {
        printToFile(text);
      }
      CUSTOM_HEADER.clear();
    }
  }

  private void printFileFooter() {
    for (final String text : CUSTOM_FOOTER) {
      printToFile(text);
    }
    CUSTOM_FOOTER.clear();
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
   * @param model Microprocessor model.
   * @param sequence Instruction call sequence.
   *
   * @throws NullPointerException if the parameter is null.
   * @throws ConfigurationException if failed to evaluate one of the output objects associated with
   *         an instruction call in the sequence.
   */
  public void printSequence(
      final Model model,
      final ConcreteSequence sequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(sequence);

    if (!sequence.getTitle().isEmpty()) {
      printHeaderToFile(sequence.getTitle());
    } else {
      printToFile("");
    }

    if (sequence.isEmpty()) {
      printNote("Empty");
      return;
    }

    if (sequence.getSection() != section) {
      section = sequence.getSection();
      printSeparatorToFile(section.getAsmText());

      if (!section.isSeparateFile()) {
        printText(section.getAsmText());
      } else {
        printNote(section.toString());
      }
    }

    final List<ConcreteCall> prologue = sequence.getPrologue();
    if (!prologue.isEmpty()) {
      printNote("Preparation");
      printCalls(model, prologue);

      printText("");
      printNote("Stimulus");
    }

    printCalls(model, sequence.getBody());
  }

  /**
   * Prints the specified list of calls (all attributes applicable at generation time).
   *
   * @param calls List of calls.
   * @param model Microprocessor model,.
   * @throws ConfigurationException if failed to evaluate one of the output objects
   *         associated with an instruction call.
   */
  private void printCalls(
      final Model model,
      final List<ConcreteCall> calls) throws ConfigurationException {
    if (calls.isEmpty()) {
      printNote("Empty");
      return;
    }

    for (final ConcreteCall call : calls) {
      for (final Directive directive : call.getDirectives()) {
        printText(directive.getText(), directive.needsIndent());
      }

      printOutputs(model, call.getOutputs());

      final boolean writeToFile = null != fileWritter;
      final String text = call.getText();

      if (null != text) {
        printText(text);
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
      final Model model,
      final List<Output> outputs) throws ConfigurationException {
    InvariantChecks.checkNotNull(outputs);

    for (final Output output : outputs) {
      if (output.isRuntime()) {
        continue;
      }

      final boolean printComment =
          options.getValueAsBoolean(Option.COMMENTS_ENABLED)
              && options.getValueAsBoolean(Option.COMMENTS_DEBUG);

      String text = output.evaluate(model);
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

        default:
          // Anything else is ignored.
          break;
      }

      if (output.isComment() && !printComment) {
        printToScreen(text);
      } else {
        printText(text);
      }
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
   *
   * @param text Text to be printed.
   * @param needsIndent Indentation flag.
   */
  private void printText(final String text, final boolean needsIndent) {
    printToScreen(text, needsIndent);
    printToFile(text, needsIndent);
  }

  private void printText(final String text) {
    printText(text, true);
  }

  /**
   * Prints a special header comment that specifies the start of a logically separate
   * part of code.
   *
   * @param text Text of the header.
   */
  private void printHeaderToFile(final String text) {
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
      printToFile(String.format(
          "%s%s%s", commentToken, text.isEmpty() || commentToken.endsWith(" ") ? "" : " ", text));
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

    final int prefixLength = (LINE_WIDTH - indentToken.length() - text.length()) / 2;
    final int postfixLength = LINE_WIDTH - indentToken.length() - prefixLength - text.length();
    final StringBuilder sb = new StringBuilder();

    sb.append(commentToken);
    sb.append(newSeparator(prefixLength - commentToken.length() - 1, separatorToken));
    sb.append(' ');
    sb.append(text);
    sb.append(' ');
    sb.append(newSeparator(postfixLength - 1, separatorToken));

    printToFile(sb.toString());
  }

  private void printToScreen(final String text, final boolean needsIndent) {
    if (null != text && printToScreen && Logger.isDebug()) {
      Logger.debug((needsIndent ? indentToken : "") + text);
    }
  }

  private void printToScreen(final String text) {
    printToScreen(text, true);
  }

  private void printToFile(final String text, final boolean needsIndent) {
    if (null != fileWritter && null != text) {
      if (text.isEmpty()) {
        fileWritter.println();
      } else {
        if (needsIndent) {
          fileWritter.print(indentToken);
        }
        fileWritter.println(text);
      }
    }
  }

  private void printToFile(final String text) {
    printToFile(text, true);
  }

  public void printData(final DataSection dataSection) {
    for (final Directive directive : dataSection.getDirectives()) {
      printText(directive.getText(), directive.needsIndent());
    }
  }

  public void printData(final Collection<DataSection> dataSections) {
    InvariantChecks.checkNotNull(dataSections);

    if (dataSections.isEmpty()) {
      return;
    }

    printHeaderToFile("Data");

    int currentTestCaseIndex = Integer.MIN_VALUE;
    for (final DataSection dataSection : dataSections) {
      if (dataSection.isSeparateFile()) {
        continue;
      }

      if (dataSection.getSection() != section) {
        section = dataSection.getSection();
        printSeparatorToFile(section.getAsmText());
        printText(section.getAsmText());
      }

      printToFile("");
      final int index = dataSection.getSequenceIndex();
      if (index != currentTestCaseIndex) {
        printSeparatorToFile(index == Label.NO_SEQUENCE_INDEX
            ? "Global Data" : String.format("Test Case %d", index));
        currentTestCaseIndex = index;
      }

      printData(dataSection);
    }
  }
}
