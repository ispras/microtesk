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

package ru.ispras.microtesk.translator.mmu.spec;

import java.math.BigInteger;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

/**
 * This class describes an equality/inequality.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuEquality {

  /**
   * This enumeration contains equality/inequality types.
   */
  public static enum Type {
    /** Expression1 == Expression2. */
    EQUAL,
    /** Expression1 != Expression2. */
    NOT_EQUAL,
    /** Expression1 == Constant2. */
    EQUAL_CONST,
    /** Expression1 != Constant2. */
    NOT_EQUAL_CONST,
    /** Expression1 == ReplacedTag2. */
    EQUAL_REPLACED,
    /** Expression1 != ReplacedTag2. */
    NOT_EQUAL_REPLACED
  }

  /**
   * Creates a {@code x[lo..hi] == const} equality.
   * 
   * @param var the variable.
   * @param lo the lower bit index.
   * @param hi the upper bit index.
   * @param val the constant.
   * @return the equality.
   */
  public static MmuEquality EQ(final IntegerVariable var, final int lo, final int hi,
      final BigInteger val) {
    return new MmuEquality(Type.EQUAL_CONST, MmuExpression.VAR(var, lo, hi), val);
  }

  /**
   * Creates a {@code x != const} inequality.
   * 
   * @param var the variable.
   * @param lo the lower bit index.
   * @param hi the upper bit index.
   * @param val the constant.
   * @return the inequality.
   */
  public static MmuEquality NEQ(final IntegerVariable var, final int lo, final int hi,
      final BigInteger val) {
    return new MmuEquality(Type.NOT_EQUAL_CONST, MmuExpression.VAR(var, lo, hi), val);
  }

  /**
   * Creates a {@code x[bit] == const} equality.
   * 
   * @param var the variable.
   * @param bit the bit index.
   * @param val the constant.
   * @return the equality.
   */
  public static MmuEquality EQ(final IntegerVariable var, final int bit, final BigInteger val) {
    return EQ(var, bit, bit, val);
  }

  /**
   * Creates a {@code x[bit] != const} inequality.
   * 
   * @param var the variable.
   * @param bit the bit index.
   * @param val the constant.
   * @return the inequality.
   */
  public static MmuEquality NEQ(final IntegerVariable var, final int bit, final BigInteger val) {
    return NEQ(var, bit, bit, val);
  }

  /**
   * Creates a {@code x == const} equality.
   * 
   * @param var the variable.
   * @param val the constant.
   * @return the equality.
   */
  public static MmuEquality EQ(final IntegerVariable var, final BigInteger val) {
    return EQ(var, 0, (var.getWidth() - 1), val);
  }

  /**
   * Creates a {@code x != const} inequality.
   * 
   * @param var the variable.
   * @param val the constant.
   * @return the inequality.
   */
  public static MmuEquality NEQ(final IntegerVariable var, final BigInteger val) {
    return NEQ(var, 0, var.getWidth() - 1, val);
  }

  /** The equality type. */
  private final Type type;

  /** The expression. */
  private final MmuExpression expression;
  /** The constant. */
  private final BigInteger constant;

  /**
   * Constructs an equality/inequality.
   * 
   * @param type the equality type.
   * @param expression the expression.
   * @param constant the constant.
   * @throws NullPointerException if some parameters are null.
   */
  public MmuEquality(final Type type, final MmuExpression expression, final BigInteger constant) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(expression);
    InvariantChecks.checkNotNull(constant);

    this.type = type;
    this.expression = expression;
    this.constant = constant;
  }

  /**
   * Constructs an equality/inequality.
   * 
   * @param type the equality type.
   * @param expression the lhs/rhs expression.
   */
  public MmuEquality(final Type type, final MmuExpression expression) {
    this(type, expression, BigInteger.ZERO);
  }

  /**
   * Returns the equality type.
   * 
   * @return the equality type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the expression of the equality/inequality.
   * 
   * @return the expression.
   */
  public MmuExpression getExpression() {
    return expression;
  }

  /**
   * Returns the constant of the equality/inequality.
   * 
   * @return the constant.
   */
  public BigInteger getConstant() {
    return constant;
  }

  @Override
  public String toString() {
    switch (type) {
      case EQUAL:
        return String.format("%s=%s", expression, expression);
      case NOT_EQUAL:
        return String.format("%s!=%s", expression, expression);
      case EQUAL_CONST:
        return String.format("%s=%s", expression, constant.toString(16));
      case NOT_EQUAL_CONST:
        return String.format("%s!=%s", expression, constant.toString(16));
      case EQUAL_REPLACED:
        return String.format("%s=REPLACED", expression);
      default:
        return String.format("%s!=REPLACED", expression);
    }
  }
}
