/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.math.BigInteger;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerVariable;

/**
 * {@link MmuEquality} class describes an equality/inequality.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuEquality {
  /**
   * This enumeration contains equality/inequality types.
   */
  public static enum Type {
    /** Expression1 == Expression2. */
    EQUAL,
    /** Expression1 == Constant2. */
    EQUAL_CONST,
    /** Expression1 == ReplacedTag2. */
    EQUAL_REPLACED,
  }

  public static MmuEquality NOT(final MmuEquality equality) {
    return new MmuEquality(equality.getType(), !equality.isNegated(),
        equality.getExpression(), equality.getConstant());
  }

  public static MmuEquality EQ(final MmuExpression expression) {
    return new MmuEquality(Type.EQUAL, false, expression);
  }

  public static MmuEquality NEQ(final MmuExpression expression) {
    return new MmuEquality(Type.EQUAL, true, expression);
  }

  public static MmuEquality EQ(final MmuExpression expression, final BigInteger value) {
    return new MmuEquality(Type.EQUAL_CONST, false, expression, value);
  }

  public static MmuEquality NEQ(final MmuExpression expression, final BigInteger value) {
    return new MmuEquality(Type.EQUAL_CONST, true, expression, value);
  }

  public static MmuEquality EQ(final IntegerField field) {
    return EQ(MmuExpression.FIELD(field));
  }

  public static MmuEquality NEQ(final IntegerField field) {
    return NEQ(MmuExpression.FIELD(field));
  }

  public static MmuEquality EQ(final IntegerVariable variable) {
    return EQ(MmuExpression.VAR(variable));
  }

  public static MmuEquality NEQ(final IntegerVariable variable) {
    return NEQ(MmuExpression.VAR(variable));
  }

  public static MmuEquality EQ(final IntegerField field, final BigInteger value) {
    return EQ(MmuExpression.FIELD(field), value);
  }

  public static MmuEquality NEQ(final IntegerField field, final BigInteger value) {
    return NEQ(MmuExpression.FIELD(field), value);
  }

  public static MmuEquality EQ(final IntegerVariable variable, final BigInteger value) {
    return EQ(MmuExpression.VAR(variable), value);
  }

  public static MmuEquality NEQ(final IntegerVariable variable, final BigInteger value) {
    return NEQ(MmuExpression.VAR(variable), value);
  }

  /** Equality type. */
  private final Type type;

  /** Negation flag. */
  private final boolean negation;

  /** Expression. */
  private final MmuExpression expression;
  /** Constant. */
  private final BigInteger constant;

  public MmuEquality(
      final Type type,
      final boolean negation,
      final MmuExpression expression,
      final BigInteger constant) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(expression);
    InvariantChecks.checkNotNull(constant);

    this.type = type;
    this.negation = negation;
    this.expression = expression;
    this.constant = constant;
  }

  public MmuEquality(final Type type, final boolean negation, final MmuExpression expression) {
    this(type, negation, expression, BigInteger.ZERO);
  }

  public Type getType() {
    return type;
  }

  public boolean isNegated() {
    return negation;
  }

  public MmuExpression getExpression() {
    return expression;
  }

  public BigInteger getConstant() {
    return constant;
  }

  @Override
  public String toString() {
    final String sign = (negation ? "!=" : "=");

    switch (type) {
      case EQUAL:
        return String.format("%s%s%s", expression, sign, expression);
      case EQUAL_CONST:
        return String.format("%s%s%s", expression, sign, constant.toString(16));
      default:
        return String.format("%s%sREPLACED", expression, sign);
    }
  }
}
