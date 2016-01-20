/*
 * Copyright 2013-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.expr;

import java.util.HashMap;
import java.util.Map;

public enum Operator {
  OR("||",          2),

  AND("&&",         2),

  BIT_OR ("|",      2),
  BIT_XOR("^",      2),
  BIT_AND("&",      2),

  EQ("==",          2),
  NOT_EQ("!=",      2),

  LEQ("<=",         2),
  GEQ(">=",         2),
  LESS("<",         2),
  GREATER(">",      2),

  L_SHIFT("<<",     2),
  R_SHIFT(">>",     2),
  L_ROTATE("<<<",   2),
  R_ROTATE(">>>",   2),

  PLUS("+",         2),
  MINUS("-",        2),

  MUL("*",          2),
  DIV("/",          2),
  MOD("%",          2),

  POW("**",         2),

  UPLUS("UPLUS",    1),
  UMINUS("UMINUS",  1),
  BIT_NOT("~",      1),
  NOT("!",          1),

  // Synthetic operators
  ITE(null,         3),
  SQRT(null,        1),
  IS_NAN(null,      1),
  IS_SIGN_NAN(null, 1);

  private static final Map<String, Operator> operators;
  static {
    final Operator[] ops = Operator.values();
    operators = new HashMap<>(ops.length);

    for (final Operator o : ops) {
      if (o.text != null) {
        operators.put(o.text, o);
      }
    }
  }

  public static Operator forText(final String text) {
    return operators.get(text);
  }

  private final String text;
  private final int operands;

  private Operator(final String text, final int operands) {
    this.text = text;
    this.operands = operands;
  }

  public String text() {
    return text;
  }

  public int operands() {
    return operands;
  }
}
