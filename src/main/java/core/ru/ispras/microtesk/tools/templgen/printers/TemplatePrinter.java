/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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

/**
 * The {@code TemplatePrinter} interface is used to create data printers for templates.
 *
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public interface TemplatePrinter {
  /**
   * Adds a title to template file.
   */
  public abstract void templateBegin();

  /**
   * Adds a header to template file.
   */
  public abstract void addHeader();

  /**
   * Adds the data region to template file.
   */
  public abstract void addDataRegion();

  /**
   * Returns the data region label of the template file.
   *
   * @return label of the data region.
   */
  public abstract String getDataLabel();

  /**
   * Checks for a match with the keywords and returns not a keyword for the printer with the same
   * value. Example: add - keyword, ADD - not keyword for some languages.
   *
   * @param operationName name of the operation.
   * @return not keyword name of the operation for the printer.
   */
  public String formattingOperation(final String operationName);
  /*
   * TODO: public default String formattingOperation(String operationName) { return operationName; }
   */

  /**
   * Adds the operation to template file.
   *
   * @param opName Operation name.
   * @param opArguments Operation arguments.
   */
  public abstract void addOperation(final String opName, final String opArguments);

  /**
   * Adds the string to template file.
   *
   * @param addString string.
   */
  public abstract void addString(final String addString);

  /**
   * Adds the text to template file.
   *
   * @param addText text.
   */
  public abstract void addText(final String addText);

  /**
   * Adds the aligned (in accordance of the text structure) text to template file.
   *
   * @param addText text.
   */
  public abstract void addAlignedText(final String addText);

  /**
   * Adds the comment to template file.
   *
   * @param addComment text.
   */
  public abstract void addComment(final String addComment);

  /**
   * Opens the sequence in template file.
   *
   * @param sequenceTitle sequence title.
   */
  public abstract void startSequence(final String sequenceTitle);

  /**
   * Closes the sequence in template file.
   *
   * @param sequenceEnd sequence end title.
   */
  public abstract void closeSequence(final String sequenceEnd);

  /**
   * Adds end title for template.
   */
  public abstract void templateEnd();

  /**
   * Closes the template file.
   */
  public abstract void templateClose();

  /**
   * Opens the block in template file.
   */
  public abstract void startBlock();

  /**
   * Closes the block in template file.
   */
  public abstract void closeBlock();
}
