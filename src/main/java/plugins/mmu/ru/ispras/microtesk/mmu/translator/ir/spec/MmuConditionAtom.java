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
import ru.ispras.microtesk.basis.solver.IntegerRange;
import ru.ispras.microtesk.basis.solver.IntegerVariable;

/**
 * {@link MmuConditionAtom} represents an atomic condition.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuConditionAtom {
  public static enum Type {
    /** Constraint: {@code expression1 == expression2}. */
    EQUAL,
    /** Constraint: {@code expression == constant}. */
    EQUAL_CONST,
    /** Constraint: {@code lowerBound <= variable <= upperBound}. */
    RANGE,
    /** Constraint: {@code expression == replacedTag}. */
    EQUAL_REPLACED,
  }

  //------------------------------------------------------------------------------------------------
  // Positive Atomic Conditions
  //------------------------------------------------------------------------------------------------

  public static MmuConditionAtom eq(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQUAL, false, expression);
  }

  public static MmuConditionAtom eq(final MmuExpression expression, final BigInteger value) {
    return new MmuConditionAtom(Type.EQUAL_CONST, false, expression, value);
  }

  public static MmuConditionAtom eq(final IntegerField field) {
    return eq(MmuExpression.field(field));
  }

  public static MmuConditionAtom eq(final IntegerVariable variable) {
    return eq(MmuExpression.var(variable));
  }

  public static MmuConditionAtom eq(final IntegerField field, final BigInteger value) {
    return eq(MmuExpression.field(field), value);
  }

  public static MmuConditionAtom eq(final IntegerVariable variable, final BigInteger value) {
    return eq(MmuExpression.var(variable), value);
  }

  public static MmuConditionAtom range(
      final MmuExpression expression, final BigInteger min, final BigInteger max) {
    return new MmuConditionAtom(Type.RANGE, false, expression, new IntegerRange(min, max));
  }

  public static MmuConditionAtom range(
      final IntegerField field, final BigInteger min, final BigInteger max) {
    return range(MmuExpression.field(field), min, max);
  }

  public static MmuConditionAtom range(
      final IntegerVariable variable, final BigInteger min, final BigInteger max) {
    return range(MmuExpression.var(variable), min, max);
  }

  public static MmuConditionAtom eqReplaced(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQUAL_REPLACED, false, expression);
  }

  public static MmuConditionAtom eqReplaced(final IntegerField field) {
    return eqReplaced(MmuExpression.field(field));
  }

  public static MmuConditionAtom eqReplaced(final IntegerVariable variable) {
    return eqReplaced(MmuExpression.var(variable));
  }

  //------------------------------------------------------------------------------------------------
  // Negative Atomic Conditions
  //------------------------------------------------------------------------------------------------

  public static MmuConditionAtom not(final MmuConditionAtom equality) {
    return new MmuConditionAtom(
        equality.type, !equality.negation, equality.expression, equality.range);
  }

  public static MmuConditionAtom neq(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQUAL, true, expression);
  }

  public static MmuConditionAtom neq(final MmuExpression expression, final BigInteger value) {
    return new MmuConditionAtom(Type.EQUAL_CONST, true, expression, value);
  }

  public static MmuConditionAtom neq(final IntegerField field) {
    return neq(MmuExpression.field(field));
  }

  public static MmuConditionAtom neq(final IntegerVariable variable) {
    return neq(MmuExpression.var(variable));
  }

  public static MmuConditionAtom neq(final IntegerField field, final BigInteger value) {
    return neq(MmuExpression.field(field), value);
  }

  public static MmuConditionAtom neq(final IntegerVariable variable, final BigInteger value) {
    return neq(MmuExpression.var(variable), value);
  }

  public static MmuConditionAtom nrange(
      final MmuExpression expression, final BigInteger min, final BigInteger max) {
    return new MmuConditionAtom(Type.RANGE, true, expression, new IntegerRange(min, max));
  }

  public static MmuConditionAtom nrange(
      final IntegerField field, final BigInteger min, final BigInteger max) {
    return nrange(MmuExpression.field(field), min, max);
  }

  public static MmuConditionAtom nrange(
      final IntegerVariable variable, final BigInteger min, final BigInteger max) {
    return nrange(MmuExpression.var(variable), min, max);
  }

  public static MmuConditionAtom neqReplaced(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQUAL_REPLACED, true, expression);
  }

  public static MmuConditionAtom neqReplaced(final IntegerField field) {
    return neqReplaced(MmuExpression.field(field));
  }

  public static MmuConditionAtom neqReplaced(final IntegerVariable variable) {
    return neqReplaced(MmuExpression.var(variable));
  }

  //------------------------------------------------------------------------------------------------
  // Internals
  //------------------------------------------------------------------------------------------------

  /** Equality type. */
  private final Type type;
  /** Negation flag. */
  private final boolean negation;

  /** Expression. */
  private final MmuExpression expression;
  /** Constant or range. */
  private final IntegerRange range;

  private MmuConditionAtom(
      final Type type,
      final boolean negation,
      final MmuExpression expression,
      final IntegerRange range) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(expression);
    InvariantChecks.checkNotNull(range);
    InvariantChecks.checkTrue(range.isSingular() || expression.size() == 1);
    InvariantChecks.checkTrue(range.isSingular() != (type == Type.RANGE));

    this.type = type;
    this.negation = negation;
    this.expression = expression;
    this.range = range;
  }

  private MmuConditionAtom(
      final Type type,
      final boolean negation,
      final MmuExpression expression,
      final BigInteger value) {
    this(type, negation, expression, new IntegerRange(value));
  }

  private MmuConditionAtom(
      final Type type,
      final boolean negation,
      final MmuExpression expression) {
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

  public IntegerRange getRange() {
    return range;
  }

  public BigInteger getConstant() {
    return range.getMin();
  }

  @Override
  public String toString() {
    final String equalSign = (negation ? "!="  : "=");
    final String rangeSign = (negation ? "!in" : "in");

    switch (type) {
      case EQUAL:
        return String.format("%s%s%s", expression, equalSign, expression);
      case EQUAL_CONST:
        return String.format("%s%s%s", expression, equalSign, range.getMin().toString(16));
      case RANGE:
        return String.format("%s%s%s", expression, rangeSign, range);
      default:
        return String.format("%s%sREPLACED", expression, equalSign);
    }
  }
}
