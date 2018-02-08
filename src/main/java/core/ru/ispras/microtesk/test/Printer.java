/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.ProcessingElement;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataDirective;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Output;
import ru.ispras.microtesk.utils.FileUtils;
import ru.ispras.microtesk.utils.BinaryWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
  private final static int LINE_WIDTH = 100;
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

  public static Printer newCodeFile(
      final Options options,
      final int codeFileIndex) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkGreaterOrEqZero(codeFileIndex);

    final String outDir = getOutDir(options);
    final String fileName = String.format(
        "%s_%04d", options.getValueAsString(Option.CODE_PRE), codeFileIndex);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.CODE_EXT));

    final File binaryFile = options.getValueAsBoolean(Option.GENERATE_BINARY) ?
        FileUtils.newFile(outDir, fileName, options.getValueAsString(Option.BIN_EXT)) : null;

    return new Printer(options, false, file, binaryFile);
  }

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

  public static Printer newDataFile(
      final Options options,
      final int dataFileIndex) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkGreaterOrEqZero(dataFileIndex);

    final String outDir = getOutDir(options);
    final String fileName = String.format(
        "%s_%04d", options.getValueAsString(Option.DATA_PRE), dataFileIndex);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.DATA_EXT));

    return new Printer(options, false, file, null);
  }

  public static Printer newExcHandlerFile(
      final Options options,
      final String id) throws IOException {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(id);

    final String outDir = getOutDir(options);
    final String fileName =
        options.getValueAsString(Option.EXCEPT_PRE) + (id.isEmpty() ? "" : "_" + id);

    final File file = FileUtils.newFile(
        outDir, fileName, options.getValueAsString(Option.CODE_EXT));

    final File binaryFile = options.getValueAsBoolean(Option.GENERATE_BINARY) ?
        FileUtils.newFile(outDir, fileName, options.getValueAsString(Option.BIN_EXT)) : null;

    return new Printer(options, false, file, binaryFile);
  }

  public static String getOutDir(final Options options) {
    InvariantChecks.checkNotNull(options);
    return options.hasValue(Option.OUTDIR) ?
        options.getValueAsString(Option.OUTDIR) : SysUtils.getHomeDir();
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
    final boolean bigEndian = options.getValueAsBoolean(Option.BIN_USE_BIG_ENDIAN);
    this.binaryWriter = null != binaryFile ? new BinaryWriter(binaryFile, bigEndian) : null;

    this.commentToken = options.getValueAsString(Option.COMMENT_TOKEN);
    this.indentToken = options.getValueAsString(Option.INDENT_TOKEN);
    this.separatorToken = options.getValueAsString(Option.SEPARATOR_TOKEN);
    this.separator = commentToken +
        newSeparator(LINE_WIDTH - indentToken.length() - commentToken.length(), separatorToken);

    printFileHeader();
  }

  public String getFileName() {
    return null != file ? file.getName() : null;
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
   * @param observer Information on the processing element state.
   * @param sequence Instruction call sequence.
   *
   * @throws NullPointerException if the parameter is null.
   * @throws ConfigurationException if failed to evaluate one of the output objects associated with
   *         an instruction call in the sequence.
   */
  public void printSequence(
      final ProcessingElement observer,
      final ConcreteSequence sequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(observer);
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
      printText(section.getAsmText());
    }

    final List<ConcreteCall> prologue = sequence.getPrologue();
    if (!prologue.isEmpty()) {
      printNote("Preparation");
      printCalls(observer, prologue);

      printText("");
      printNote("Stimulus");
    }

    printCalls(observer, sequence.getBody());
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
      if (printToScreen) {
        Logger.debug(text);
      }
      printToFileNoIndent(text);
    }
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

  private void printToScreen(final String text) {
    if (printToScreen && Logger.isDebug()) {
      Logger.debug(indentToken + text);
    }
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

  public void printData(final DataSection dataSection) {
    for (final DataDirective directive : dataSection.getDirectives()) {
      final String text = directive.getText();
      if (directive.needsIndent()) {
        printText(text);
      } else {
        printTextNoIndent(text);
      }
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
        printText(section.getAsmText());
      }

      printToFile("");
      final int index = dataSection.getSequenceIndex();
      if (index != currentTestCaseIndex) {
        printSeparatorToFile(index == Label.NO_SEQUENCE_INDEX ?
            "Global Data" : String.format("Test Case %d", index));
        currentTestCaseIndex = index;
      }

      printData(dataSection);
    }
  }
}
