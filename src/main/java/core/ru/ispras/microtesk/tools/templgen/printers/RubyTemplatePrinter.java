/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@code RubyTemplatePrinter} class prints data of template into a ruby file.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public class RubyTemplatePrinter implements TemplatePrinter {
  public static final String[] RUBY_KEYWORDS = {"and", "or"};
  public static final String RUBY_TAB = "  ";

  private int NowLevel;

  public String formattingOperation(String operationName) {
    for (String rubyKeywords : RUBY_KEYWORDS) {
      if (rubyKeywords == operationName)
        return operationName.toUpperCase();
    }
    return operationName;
  }

  static final String TEMPLATE_NAME = "_autogentemplate.rb";

  private PrintWriter printWriter;

  public RubyTemplatePrinter(String templateName) {
    final File templateFile = new File(templateName + TEMPLATE_NAME);

    printWriter = newPrintWriter(templateFile);
  }

  public BufferedWriter newBufferedWriter(final File file) {
    InvariantChecks.checkNotNull(file);
    try {
      return new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset()));
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public PrintWriter newPrintWriter(final File file) {
    InvariantChecks.checkNotNull(file);
    return new PrintWriter(newBufferedWriter(file));
  }

  public void templateBegin() {
    // Adds xml data
    this.printWriter.print("require_relative 'minimips_base'\r\n\n"
        + "class MiniMipsGenTemplate < MiniMipsBaseTemplate\r\n");
    NowLevel++;
    this.addTab(NowLevel);
    this.printWriter.print("def run\r\n");
    NowLevel++;
    this.addTab(NowLevel);
    this.printWriter.print("org 0x00020000\n");
  }

  private void addTab(int tabCount) {
    for (int i = 0; i < tabCount; i++) {
      this.printWriter.print(RUBY_TAB);
    }
  }

  public void addOperation(String operationName) {
    InvariantChecks.checkNotNull(operationName);
    this.addTab(NowLevel);
    this.printWriter.format("%s ", formattingOperation(operationName));
  }

  @Override
  public void addOperation(String opName, String opArguments) {
    InvariantChecks.checkNotNull(opName);
    this.addTab(NowLevel);
    this.printWriter.format("%s %s", formattingOperation(opName), opArguments);
  }

  @Override
  public void addString(String addString) {
    InvariantChecks.checkNotNull(addString);
    this.addTab(NowLevel);
    this.printWriter.format("%s\n", addString);
  }

  @Override
  public void addText(String addText) {
    InvariantChecks.checkNotNull(addText);
    this.printWriter.format("%s", addText);
  }

  @Override
  public void addComment(String addText) {
    InvariantChecks.checkNotNull(addText);
    this.addTab(NowLevel);
    this.printWriter.format("# %s\n", addText);
  }

  @Override
  public void templateEnd() {
    NowLevel--;
    this.addTab(NowLevel);
    this.printWriter.println("end");
    NowLevel--;
    this.addTab(NowLevel);
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
    NowLevel++;
  }

  @Override
  public void closeSequence(String addText) {
    InvariantChecks.checkNotNull(addText);
    NowLevel--;
    this.addString(addText);
  }

  public void startBlock() {
    this.addString("block(:combinator => 'product', :compositor => 'random') {");
    NowLevel++;
  }

  public void closeBlock() {
    NowLevel--;
    this.addString("}.run");
  }
}
