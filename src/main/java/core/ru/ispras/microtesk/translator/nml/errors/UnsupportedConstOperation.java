/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class UnsupportedConstOperation implements ISemanticError {
  private static final String FORMAT =
    "The %s operation is not supported for the following operand types: %s (\"%s\").";

  private final String op;
  private final String text;
  private final Class<?> type1;
  private final Class<?> type2;

  public UnsupportedConstOperation(String op, String text, Class<?> type1, Class<?> type2) {
    this.op = op;
    this.text = text;
    this.type1 = type1;
    this.type2 = type2;
  }

  public UnsupportedConstOperation(String op, String text, Class<?> type) {
    this(op, text, type, null);
  }

  @Override
  public String getMessage() {
    final String types = (null == type2) ?
      type1.getSimpleName() : type1.getSimpleName() + " and " + type2.getSimpleName();

    return String.format(FORMAT, op, types, text);
  }
}
