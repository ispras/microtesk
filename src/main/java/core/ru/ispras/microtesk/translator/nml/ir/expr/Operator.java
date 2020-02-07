/*
 * Copyright 2013-2020 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.*;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.TypeId;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public enum Operator {
  OR("||",      2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.OR),
                                 rule(TypeId.BOOL,              StandardOperation.OR)),

  AND("&&",     2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.AND),
                                 rule(TypeId.BOOL,              StandardOperation.AND)),

  BIT_OR ("|",  2, Kind.COMMON,  rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVOR),
                                 rule(TypeId.CARD,              StandardOperation.BVOR),
                                 rule(TypeId.INT,               StandardOperation.BVOR)),

  BIT_XOR("^",  2, Kind.COMMON,  rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVXOR),
                                 rule(TypeId.CARD,              StandardOperation.BVXOR),
                                 rule(TypeId.INT,               StandardOperation.BVXOR)),

  BIT_AND("&",  2, Kind.COMMON,  rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVAND),
                                 rule(TypeId.CARD,              StandardOperation.BVAND),
                                 rule(TypeId.INT,               StandardOperation.BVAND)),

  EQ("==",      2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.EQ),
                                 rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.EQ),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.EQ),
                                 rule(TypeId.CARD,              StandardOperation.EQ),
                                 rule(TypeId.INT,               StandardOperation.EQ),
                                 rule(TypeId.BOOL,              StandardOperation.EQ),
                                 rule(TypeId.FLOAT,             StandardOperation.EQ)),

  NOT_EQ("!=",  2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.NOTEQ),
                                 rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.NOTEQ),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.NOTEQ),
                                 rule(TypeId.CARD,              StandardOperation.NOTEQ),
                                 rule(TypeId.INT,               StandardOperation.NOTEQ),
                                 rule(TypeId.BOOL,              StandardOperation.NOTEQ),
                                 rule(TypeId.FLOAT,             StandardOperation.NOTEQ)),

  LEQ("<=",     2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.LESSEQ),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVULE),
                                 rule(TypeId.CARD,              StandardOperation.BVULE),
                                 rule(TypeId.INT,               StandardOperation.BVSLE),
                                 rule(TypeId.FLOAT,             StandardOperation.LESSEQ)),

  GEQ(">=",     2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.GREATEREQ),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUGE),
                                 rule(TypeId.CARD,              StandardOperation.BVUGE),
                                 rule(TypeId.INT,               StandardOperation.BVSGE),
                                 rule(TypeId.FLOAT,             StandardOperation.GREATEREQ)),

  LESS("<",     2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.LESS),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVULT),
                                 rule(TypeId.CARD,              StandardOperation.BVULT),
                                 rule(TypeId.INT,               StandardOperation.BVSLT),
                                 rule(TypeId.FLOAT,             StandardOperation.LESS)),

  GREATER(">",  2, Kind.BOOLEAN, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.GREATER),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUGT),
                                 rule(TypeId.CARD,              StandardOperation.BVUGT),
                                 rule(TypeId.INT,               StandardOperation.BVSGT),
                                 rule(TypeId.FLOAT,             StandardOperation.GREATER)),

  L_SHIFT("<<", 2, Kind.SHIFT,   rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVLSHL),
                                 rule(DataTypeId.LOGIC_INTEGER, StandardOperation.BVLSHL),
                                 rule(TypeId.CARD,              StandardOperation.BVLSHL),
                                 rule(TypeId.INT,               StandardOperation.BVASHL)),

  R_SHIFT(">>", 2, Kind.SHIFT,   rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVLSHR),
                                 rule(DataTypeId.LOGIC_INTEGER, StandardOperation.BVASHR),
                                 rule(TypeId.CARD,              StandardOperation.BVLSHR),
                                 rule(TypeId.INT,               StandardOperation.BVASHR)),

  L_ROTATE("<<<", 2, Kind.SHIFT, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVROL),
                                 rule(TypeId.CARD,              StandardOperation.BVROL),
                                 rule(TypeId.INT,               StandardOperation.BVROL)),

  R_ROTATE(">>>", 2, Kind.SHIFT, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVROR),
                                 rule(TypeId.CARD,              StandardOperation.BVROR),
                                 rule(TypeId.INT,               StandardOperation.BVROR)),

  PLUS("+",    2,  Kind.COMMON,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.ADD),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVADD),
                                 rule(TypeId.CARD,              StandardOperation.BVADD),
                                 rule(TypeId.INT,               StandardOperation.BVADD),
                                 rule(TypeId.FLOAT,             StandardOperation.ADD)),

  MINUS("-",   2,  Kind.COMMON,  rule(DataTypeId.LOGIC_INTEGER, StandardOperation.SUB),
                                 rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVSUB),
                                 rule(TypeId.CARD,              StandardOperation.BVSUB),
                                 rule(TypeId.INT,               StandardOperation.BVSUB),
                                 rule(TypeId.FLOAT,             StandardOperation.SUB)),

  MUL("*",     2,  Kind.COMMON,  rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVMUL),
                                 rule(DataTypeId.LOGIC_INTEGER, StandardOperation.MUL),
                                 rule(TypeId.CARD,              StandardOperation.BVMUL),
                                 rule(TypeId.INT,               StandardOperation.BVMUL),
                                 rule(TypeId.FLOAT,             StandardOperation.MUL)),

  DIV("/",     2,  Kind.COMMON,  rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUDIV),
                                 rule(DataTypeId.LOGIC_INTEGER, StandardOperation.DIV),
                                 rule(TypeId.CARD,              StandardOperation.BVUDIV),
                                 rule(TypeId.INT,               StandardOperation.BVSDIV),
                                 rule(TypeId.FLOAT,             StandardOperation.DIV)),

  MOD("%",     2,   Kind.COMMON, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVUREM),
                                 rule(DataTypeId.LOGIC_INTEGER, StandardOperation.MOD),
                                 rule(TypeId.CARD,              StandardOperation.BVUREM),
                                 rule(TypeId.INT,               StandardOperation.BVSREM),
                                 rule(TypeId.FLOAT,             StandardOperation.MOD)),

  POW("**",    2,   Kind.COMMON, rule(DataTypeId.LOGIC_INTEGER, StandardOperation.POWER),
                                 rule(TypeId.CARD,              StandardOperation.POWER),
                                 rule(TypeId.INT,               StandardOperation.POWER),
                                 rule(TypeId.FLOAT,             StandardOperation.POWER)),

  UPLUS("UPLUS",   1, Kind.COMMON  /* No rules. This operator must be excluded from exprs. */),

  UMINUS("UMINUS", 1, Kind.COMMON, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVNEG),
                                   rule(DataTypeId.LOGIC_INTEGER, StandardOperation.MINUS),
                                   rule(TypeId.CARD,              StandardOperation.BVNEG),
                                   rule(TypeId.INT,               StandardOperation.BVNEG),
                                   rule(TypeId.FLOAT,             StandardOperation.MINUS)),

  BIT_NOT("~",  1,  Kind.COMMON, rule(DataTypeId.BIT_VECTOR,    StandardOperation.BVNOT),
                                 rule(TypeId.CARD,              StandardOperation.BVNOT),
                                 rule(TypeId.INT,               StandardOperation.BVNOT)),

  NOT("!",     1,  Kind.BOOLEAN, rule(DataTypeId.LOGIC_BOOLEAN, StandardOperation.NOT),
                                 rule(TypeId.BOOL,              StandardOperation.NOT)),

  // Synthetic operators
  EXTRACT(null,     3, Kind.COMMON  /* No rules. These operator must be handled separately. */),
  CONCAT(null,      2, Kind.COMMON  /* No rules. These operator must be handled separately. */),
  REPEAT(null,      2, Kind.COMMON  /* No rules. These operator must be handled separately. */),

  ITE(null,         3, Kind.COMMON  /* No rules. These operator must be handled separately. */),
  SQRT(null,        1, Kind.COMMON  /* No rules. These operator must be handled separately. */),
  ROUND(null,       1, Kind.COMMON  /* No rules. These operator must be handled separately. */),

  IS_NAN(null,      1, Kind.BOOLEAN /* No rules. These operator must be handled separately. */),
  IS_SIGN_NAN(null, 1, Kind.BOOLEAN /* No rules. These operator must be handled separately. */),

  SIGN_EXTEND(null, 2, Kind.CAST /* No rules. These operator must be handled separately. */),
  ZERO_EXTEND(null, 2, Kind.CAST /* No rules. These operator must be handled separately. */),
  COERCE(null,      2, Kind.CAST /* No rules. These operator must be handled separately. */),
  CAST(null,        2, Kind.CAST /* No rules. These operator must be handled separately. */),

  INT_TO_FLOAT(null,   2, Kind.CAST /* No rules. These operator must be handled separately. */),
  FLOAT_TO_INT(null,   2, Kind.CAST /* No rules. These operator must be handled separately. */),
  FLOAT_TO_FLOAT(null, 2, Kind.CAST /* No rules. These operator must be handled separately. */);

  private enum Kind {
    COMMON,  // Means operands and operation result have the same type.
    BOOLEAN, // Means result type is boolean
    SHIFT,   // Means result type equals the type of the first argument
    CAST     // Cast operator: first operand is target type, second is source value.
  }

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

  public static DataType getDataType(final Node node) {
    if (ExprUtils.isOperation(node, Operator.CAST)) {
      final NodeOperation operation = (NodeOperation) node;
      final NodeValue value = (NodeValue) operation.getOperand(0);
      return DataType.bitVector(value.getInteger().intValue());
    }

    return node.getDataType();
  }

  private final String text;
  private final int operandCount;
  private final Kind kind;

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
      final Kind kind,
      final Rule... rules) {
    this.text = text;
    this.operandCount = operandCount;
    this.kind = kind;

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
    return kind == Kind.BOOLEAN;
  }

  public boolean isShift() {
    return kind == Kind.SHIFT;
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
