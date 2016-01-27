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
  OR("||",          2, true,  rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.OR),
                              rule(TypeId.BOOL,              StandardOperation.OR)),

  AND("&&",         2, true,  rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.AND),
                              rule(TypeId.BOOL,              StandardOperation.AND)),

  BIT_OR ("|",      2, false, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVOR),
                              rule(TypeId.CARD,              StandardOperation.BVOR),
                              rule(TypeId.INT,               StandardOperation.BVOR)),

  BIT_XOR("^",      2, false, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVXOR),
                              rule(TypeId.CARD,              StandardOperation.BVXOR),
                              rule(TypeId.INT,               StandardOperation.BVXOR)),

  BIT_AND("&",      2, false, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVAND),
                              rule(TypeId.CARD,              StandardOperation.BVAND),
                              rule(TypeId.INT,               StandardOperation.BVAND)),

  EQ("==",          2, true,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.EQ),
                              rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.EQ),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.EQ),
                              rule(TypeId.CARD,              StandardOperation.EQ),
                              rule(TypeId.INT,               StandardOperation.EQ),
                              rule(TypeId.BOOL,              StandardOperation.EQ),
                              rule(TypeId.FLOAT,             StandardOperation.EQ)),

  NOT_EQ("!=",      2, true,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.NOTEQ),
                              rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.NOTEQ),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.NOTEQ),
                              rule(TypeId.CARD,              StandardOperation.NOTEQ),
                              rule(TypeId.INT,               StandardOperation.NOTEQ),
                              rule(TypeId.BOOL,              StandardOperation.NOTEQ),
                              rule(TypeId.FLOAT,             StandardOperation.NOTEQ)),

  LEQ("<=",         2, true,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.LESSEQ),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVULE),
                              rule(TypeId.CARD,              StandardOperation.BVULE),
                              rule(TypeId.INT,               StandardOperation.BVSLE),
                              rule(TypeId.FLOAT,             StandardOperation.LESSEQ)),

  GEQ(">=",         2, true,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.GREATEREQ),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUGE),
                              rule(TypeId.CARD,              StandardOperation.BVUGE),
                              rule(TypeId.INT,               StandardOperation.BVSGE),
                              rule(TypeId.FLOAT,             StandardOperation.GREATEREQ)),

  LESS("<",         2, true,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.LESS),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVULT),
                              rule(TypeId.CARD,              StandardOperation.BVULT),
                              rule(TypeId.INT,               StandardOperation.BVSLT),
                              rule(TypeId.FLOAT,             StandardOperation.LESS)),

  GREATER(">",      2, true,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.GREATER),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUGT),
                              rule(TypeId.CARD,              StandardOperation.BVUGT),
                              rule(TypeId.INT,               StandardOperation.BVSGT),
                              rule(TypeId.FLOAT,             StandardOperation.GREATER)),

  L_SHIFT("<<",     2, false),
  R_SHIFT(">>",     2, false),
  L_ROTATE("<<<",   2, false),
  R_ROTATE(">>>",   2, false),

  PLUS("+",         2, false, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.ADD),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVADD),
                              rule(TypeId.CARD,              StandardOperation.BVADD),
                              rule(TypeId.INT,               StandardOperation.BVADD),
                              rule(TypeId.FLOAT,             StandardOperation.ADD)),

  MINUS("-",        2, false, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.SUB),
                              rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVSUB),
                              rule(TypeId.CARD,              StandardOperation.BVSUB),
                              rule(TypeId.INT,               StandardOperation.BVSUB),
                              rule(TypeId.FLOAT,             StandardOperation.SUB)),

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

  private final Map<DataTypeId, StandardOperation> dataTypeOps;
  private final Map<TypeId,     StandardOperation> typeOps;

  private static final class Rule {
    public final Enum<?> typeId;
    public final StandardOperation operator;

    private Rule(final Enum<?> typeId, final StandardOperation operator) {
      this.typeId = typeId;
      this.operator = operator;
    }
  }

  private static Rule rule(final Enum<?> type, final StandardOperation op) {
    return new Rule(type, op);
  }

  private Operator(
      final String text,
      final int operandCount,
      final boolean isBoolean,
      final Rule... rules) {
    this.text = text;
    this.operandCount = operandCount;
    this.isBooleanOperator = isBoolean;

    this.dataTypeOps = new EnumMap<>(DataTypeId.class);
    this.typeOps = new EnumMap<>(TypeId.class);

    for (final Rule rule : rules) {
      if (rule.typeId instanceof DataTypeId) {
        this.dataTypeOps.put((DataTypeId) rule.typeId, rule.operator);
      } else if (rule.typeId instanceof TypeId) {
        this.typeOps.put((TypeId) rule.typeId, rule.operator);
      } else {
        throw new IllegalStateException(rule.typeId + " unknown type!");
      }
    }
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
    return dataTypeOps.get(dataTypeId);
  }

  public StandardOperation getFortressOperator(final TypeId typeId) {
    InvariantChecks.checkNotNull(typeId);
    return typeOps.get(typeId);
  }
}
