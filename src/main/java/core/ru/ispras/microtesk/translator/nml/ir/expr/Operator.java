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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.TypeId;

public enum Operator {
  OR("||",          2, true),

  AND("&&",         2, true),

  BIT_OR ("|",      2, false),
  BIT_XOR("^",      2, false),
  BIT_AND("&",      2, false),

  EQ("==",          2, true),
  NOT_EQ("!=",      2, true),

  LEQ("<=",         2, true),
  GEQ(">=",         2, true),
  LESS("<",         2, true),
  GREATER(">",      2, true),

  L_SHIFT("<<",     2, false),
  R_SHIFT(">>",     2, false),
  L_ROTATE("<<<",   2, false),
  R_ROTATE(">>>",   2, false),

  PLUS("+",         2, false),
  MINUS("-",        2, false),

  MUL("*",          2, false),
  DIV("/",          2, false),
  MOD("%",          2, false),

  POW("**",         2, false),

  UPLUS("UPLUS",    1, false),
  UMINUS("UMINUS",  1, false),
  BIT_NOT("~",      1, false),
  NOT("!",          1, true),

  // Synthetic operators
  ITE(null,         3, false),
  SQRT(null,        1, false),
  IS_NAN(null,      1, true),
  IS_SIGN_NAN(null, 1, true);

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
  private final int operandCount;
  private final boolean isBooleanOperator;

  private final Map<DataTypeId, StandardOperation> dataTypeOp;
  private final Map<TypeId,     StandardOperation> typeOps;


  private Operator(
      final String text,
      final int operandCount,
      final boolean isBoolean) {
    this.text = text;
    this.operandCount = operandCount;

    this.isBooleanOperator = isBoolean;
    this.dataTypeOp = new EnumMap<>(DataTypeId.class);
    this.typeOps = new EnumMap<>(TypeId.class);
  }

  public String getText() {
    return text;
  }

  public int getOperandCount() {
    return operandCount;
  }

  public boolean isBoolean() {
    return isBooleanOperator;
  }

  public StandardOperation getFortressOperator(final DataTypeId dataTypeId) {
    InvariantChecks.checkNotNull(dataTypeId);
    return dataTypeOp.get(dataTypeId);
  }

  public StandardOperation getFortressOperator(final TypeId typeId) {
    InvariantChecks.checkNotNull(typeId);
    return typeOps.get(typeId);
  }
}
