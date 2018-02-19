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

package ru.ispras.microtesk.tools.templgen.printers;

import ru.ispras.fortress.util.InvariantChecks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * The {@code RubyTemplatePrinter} class prints data of template into a ruby file.
 *
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class RubyTemplatePrinter implements TemplatePrinter {
  public static final String[] RUBY_KEYWORDS = {"and", "or", "not"};
  public static final String RUBY_TAB = "  ";

  private int nowLevel;
  private final String modelName;
  private final String templateName;

  static final String TEMPLATE_FILE_NAME = "_autogentemplate.rb";

  private PrintWriter printWriter;

  /**
   * Constructs a ruby printer with the specified template name.
   *
   * @param templateName the template name.
   */
  public RubyTemplatePrinter(final String templateName, final String modelName) {
    this.modelName = modelName;
    this.templateName = templateName;

    final File templateFile = new File(templateName + TEMPLATE_FILE_NAME);
    printWriter = newPrintWriter(templateFile);
  }

  @Override
  public String formattingOperation(String operationName) {
    for (String rubyKeywords : RUBY_KEYWORDS) {
      if (rubyKeywords == operationName) {
        return operationName.toUpperCase();
      }
    }
    return operationName;
  }

  private BufferedWriter newBufferedWriter(final File file) {
    InvariantChecks.checkNotNull(file);
    try {
      return new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset()));
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * Creates new {@code PrintWriter}.
   *
   * @param file filename of new {@code PrintWriter}.
   *
   * @return new {@code PrintWriter}.
   */
  private PrintWriter newPrintWriter(final File file) {
    InvariantChecks.checkNotNull(file);
    return new PrintWriter(newBufferedWriter(file));
  }

  @Override
  public void templateBegin() {
    // Adds xml data
    this.printWriter.print(
        "require_relative '" + modelName + "_base'\r\n\n" + "class " + templateName.toUpperCase()
            + "GenTemplate < " + modelName.toUpperCase() + "BaseTemplate\r\n");
    nowLevel++;
    this.addTab(nowLevel);
    this.printWriter.print("def run\r\n");
    nowLevel++;
    this.addTab(nowLevel);
    this.printWriter.print("org 0x00020000\n");
  }

  private void addTab(int tabCount) {
    for (int i = 0; i < tabCount; i++) {
      this.printWriter.print(RUBY_TAB);
    }
  }

  /**
   * Adds the operation to template file.
   *
   * @param operation Operation syntax.
   */
  public void addOperation(String operation) {
    InvariantChecks.checkNotNull(operation);
    this.addTab(nowLevel);
    this.printWriter.format("%s ", formattingOperation(operation));
  }

  @Override
  public void addOperation(String opName, String opArguments) {
    InvariantChecks.checkNotNull(opName);
    this.addTab(nowLevel);
    this.printWriter.format("%s %s", formattingOperation(opName), opArguments);
  }

  @Override
  public void addString(String addString) {
    InvariantChecks.checkNotNull(addString);
    this.addTab(nowLevel);
    this.printWriter.format("%s\n", addString);
  }

  @Override
  public void addText(String addText) {
    InvariantChecks.checkNotNull(addText);
    this.printWriter.format("%s", addText);
  }

  @Override
  public void addAlignedText(String addText) {
    InvariantChecks.checkNotNull(addText);
    this.addTab(nowLevel);
    this.printWriter.format("%s", addText);
  }

  @Override
  public void addComment(String addText) {
    InvariantChecks.checkNotNull(addText);
    this.addTab(nowLevel);
    this.printWriter.format("# %s\n", addText);
  }

  @Override
  public void templateEnd() {
    nowLevel--;
    this.addTab(nowLevel);
    this.printWriter.println("end");
    nowLevel--;
    this.addTab(nowLevel);
    this.printWriter.println("end");
  }

  @Override
  public void templateClose() {
    this.printWriter.close();
  }

  @Override
  public void startSequence(String addText) {
    InvariantChecks.checkNotNull(addText);
    this.addString(addText);
    nowLevel++;
  }

  @Override
  public void closeSequence(String addText) {
    InvariantChecks.checkNotNull(addText);
    nowLevel--;
    this.addString(addText);
  }

  @Override
  public void startBlock() {
    this.addString("block(:combinator => 'product', :compositor => 'random') {");
    nowLevel++;
  }

  @Override
  public void closeBlock() {
    nowLevel--;
    this.addString("}.run");
  }
}
