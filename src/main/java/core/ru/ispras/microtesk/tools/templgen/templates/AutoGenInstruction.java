/*
 * Copyright 2018-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.templgen.templates;

/**
 * The {@link AutoGenInstruction} interface for instructions used in the template auto generation.
 *
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public interface AutoGenInstruction {
  /**
   * Returns the command fields.
   *
   * @return command fields.
   */
  public boolean getFields();

  /**
   * Returns the command syntax.
   *
   * @return command syntax.
   */
  public String getCommand();

  /**
   * Returns preparation command set.
   *
   * @return command set.
   */
  public String getPreCommand();

  public String[] getPostCommand();

  public boolean isBranchOperation();

  public boolean isStoreOperation();

  public boolean isLoadOperation();

  public boolean isArithmeticOperation();

  public void printOperation();
}
