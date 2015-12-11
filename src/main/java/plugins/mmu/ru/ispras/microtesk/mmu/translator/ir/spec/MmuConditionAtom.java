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
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * {@link MmuConditionAtom} represents an atomic condition.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuConditionAtom {
  public static enum Type {
    /** Constraint: {@code lhsExpr == rhsExpr}. */
    EQ_EXPR_EXPR,
    /** Constraint: {@code lhsExpr[1] == rhsExpr[2]}. */
    EQ_SAME_EXPR,
    /** Constraint: {@code lhsExpr == rhsConst}. */
    EQ_EXPR_CONST,
    /** Constraint: {@code lhsVar in rhsRange}. */
    IN_EXPR_RANGE,
    /** Constraint: {@code expression == replacedTag}. */
    EQ_REPLACED
  }

  //------------------------------------------------------------------------------------------------
  // Positive Atomic Conditions
  //------------------------------------------------------------------------------------------------

  public static MmuConditionAtom eq(final MmuExpression lhsExpr, final MmuExpression rhsExpr) {
    return new MmuConditionAtom(Type.EQ_EXPR_EXPR, false, lhsExpr, rhsExpr);
  }

  public static MmuConditionAtom eq(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQ_SAME_EXPR, false, expression);
  }

  public static MmuConditionAtom eq(final MmuExpression lhsExpr, final BigInteger rhsConst) {
    return new MmuConditionAtom(Type.EQ_EXPR_CONST, false, lhsExpr, rhsConst);
  }

  public static MmuConditionAtom eq(final IntegerField field) {
    return eq(MmuExpression.field(field));
  }

  public static MmuConditionAtom eq(final IntegerVariable variable) {
    return eq(MmuExpression.var(variable));
  }

  public static MmuConditionAtom eq(final IntegerField lhsField, final BigInteger rhsConst) {
    return eq(MmuExpression.field(lhsField), rhsConst);
  }

  public static MmuConditionAtom eq(final IntegerField lhsField, final IntegerField rhsField) {
    return eq(MmuExpression.field(lhsField), MmuExpression.field(rhsField));
  }

  public static MmuConditionAtom eq(final IntegerField lhsField, final IntegerVariable rhsVar) {
    return eq(MmuExpression.field(lhsField), MmuExpression.var(rhsVar));
  }

  public static MmuConditionAtom eq(final IntegerVariable lhsVar, final BigInteger rhsConst) {
    return eq(MmuExpression.var(lhsVar), rhsConst);
  }

  public static MmuConditionAtom eq(final IntegerVariable lhsVar, final IntegerVariable rhsVar) {
    return eq(MmuExpression.var(lhsVar), MmuExpression.var(rhsVar));
  }

  public static MmuConditionAtom eq(final IntegerVariable lhsVar, final IntegerField rhsField) {
    return eq(MmuExpression.var(lhsVar), MmuExpression.field(rhsField));
  }

  public static MmuConditionAtom range(
      final MmuExpression lhsExpr, final BigInteger rhsMin, final BigInteger rhsMax) {
    return new MmuConditionAtom(
        Type.IN_EXPR_RANGE, false, lhsExpr, new IntegerRange(rhsMin, rhsMax));
  }

  public static MmuConditionAtom range(
      final IntegerField lhsField, final BigInteger rhsMin, final BigInteger rhsMax) {
    return range(MmuExpression.field(lhsField), rhsMin, rhsMax);
  }

  public static MmuConditionAtom range(
      final IntegerVariable lhsVar, final BigInteger rhsMin, final BigInteger rhsMax) {
    return range(MmuExpression.var(lhsVar), rhsMin, rhsMax);
  }

  public static MmuConditionAtom eqReplaced(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQ_REPLACED, false, expression);
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

  public static MmuConditionAtom not(final MmuConditionAtom atom) {
    return new MmuConditionAtom(
        atom.type, !atom.negation, atom.lhsExpr, atom.rhsRange);
  }

  public static MmuConditionAtom neq(final MmuExpression lhsExpr, final MmuExpression rhsExpr) {
    return new MmuConditionAtom(Type.EQ_EXPR_EXPR, true, lhsExpr, rhsExpr);
  }

  public static MmuConditionAtom neq(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQ_SAME_EXPR, true, expression);
  }

  public static MmuConditionAtom neq(final MmuExpression lhsExpr, final BigInteger rhsConst) {
    return new MmuConditionAtom(Type.EQ_EXPR_CONST, true, lhsExpr, rhsConst);
  }

  public static MmuConditionAtom neq(final IntegerField field) {
    return neq(MmuExpression.field(field));
  }

  public static MmuConditionAtom neq(final IntegerVariable variable) {
    return neq(MmuExpression.var(variable));
  }

  public static MmuConditionAtom neq(final IntegerField lhsField, final BigInteger rhsConst) {
    return neq(MmuExpression.field(lhsField), rhsConst);
  }

  public static MmuConditionAtom neq(final IntegerField lhsField, final IntegerField rhsField) {
    return neq(MmuExpression.field(lhsField), MmuExpression.field(rhsField));
  }

  public static MmuConditionAtom neq(final IntegerField lhsField, final IntegerVariable rhsVar) {
    return neq(MmuExpression.field(lhsField), MmuExpression.var(rhsVar));
  }

  public static MmuConditionAtom neq(final IntegerVariable lhsVar, final BigInteger rhsConst) {
    return neq(MmuExpression.var(lhsVar), rhsConst);
  }

  public static MmuConditionAtom neq(final IntegerVariable lhsVar, final IntegerVariable rhsVar) {
    return neq(MmuExpression.var(lhsVar), MmuExpression.var(rhsVar));
  }

  public static MmuConditionAtom neq(final IntegerVariable lhsVar, final IntegerField rhsField) {
    return neq(MmuExpression.var(lhsVar), MmuExpression.field(rhsField));
  }

  public static MmuConditionAtom nrange(
      final MmuExpression lhsExpr, final BigInteger rhsMin, final BigInteger rhsMax) {
    return new MmuConditionAtom(
        Type.IN_EXPR_RANGE, true, lhsExpr, new IntegerRange(rhsMin, rhsMax));
  }

  public static MmuConditionAtom nrange(
      final IntegerField lhsField, final BigInteger rhsMin, final BigInteger rhsMax) {
    return nrange(MmuExpression.field(lhsField), rhsMin, rhsMax);
  }

  public static MmuConditionAtom nrange(
      final IntegerVariable lhsVar, final BigInteger rhsMin, final BigInteger rhsMax) {
    return nrange(MmuExpression.var(lhsVar), rhsMin, rhsMax);
  }

  public static MmuConditionAtom neqReplaced(final MmuExpression expression) {
    return new MmuConditionAtom(Type.EQ_REPLACED, true, expression);
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

  /** Left-hand-side expression. */
  private final MmuExpression lhsExpr;
  /** Right-hand-side expression. */
  private final MmuExpression rhsExpr;
  /** Constant or range. */
  private final IntegerRange rhsRange;

  private MmuConditionAtom(
      final Type type,
      final boolean negation,
      final MmuExpression lhsExpr,
      final MmuExpression rhsExpr,
      final IntegerRange rhsRange) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(lhsExpr);
    InvariantChecks.checkTrue(rhsExpr != null || rhsRange != null);
    InvariantChecks.checkTrue(rhsExpr == null || lhsExpr.getWidth() == rhsExpr.getWidth());
    InvariantChecks.checkTrue(rhsRange == null || rhsRange.isSingular() || lhsExpr.size() == 1);
    InvariantChecks.checkTrue(rhsRange == null || rhsRange.isSingular() != (type == Type.IN_EXPR_RANGE));

    this.type = type;
    this.negation = negation;
    this.lhsExpr = lhsExpr;
    this.rhsExpr = rhsExpr;
    this.rhsRange = rhsRange;
  }

  private MmuConditionAtom(
      final Type type,
      final boolean negation,
      final MmuExpression lhsExpr,
      final MmuExpression rhsExpr) {
    this(type, negation, lhsExpr, rhsExpr, null);
  }

  private MmuConditionAtom(
      final Type type,
      final boolean negation,
      final MmuExpression lhsExpr,
      final IntegerRange rhsRange) {
    this(type, negation, lhsExpr, null, rhsRange);
  }

  private MmuConditionAtom(
      final Type type,
      final boolean negation,
      final MmuExpression lhsExpr,
      final BigInteger rhsValue) {
    this(type, negation, lhsExpr, null, new IntegerRange(rhsValue));
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

  public MmuExpression getLhsExpr() {
    return lhsExpr;
  }

  public MmuExpression getRhsExpr() {
    return rhsExpr;
  }

  public IntegerRange getRhsRange() {
    return rhsRange;
  }

  public BigInteger getRhsConst() {
    return rhsRange.getMin();
  }

  @Override
  public String toString() {
    final String equalSign = (negation ? "!="  : "=");
    final String rangeSign = (negation ? "!in" : "in");

    switch (type) {
      case EQ_EXPR_EXPR:
        return String.format("%s%s%s", lhsExpr, equalSign, rhsExpr);
      case EQ_SAME_EXPR:
        return String.format("%s%s%s", lhsExpr, equalSign, lhsExpr);
      case EQ_EXPR_CONST:
        return String.format("%s%s%s", lhsExpr, equalSign, rhsRange.getMin().toString(16));
      case IN_EXPR_RANGE:
        return String.format("%s%s%s", lhsExpr, rangeSign, rhsRange);
      default:
        return String.format("%s%sREPLACED", lhsExpr, equalSign);
    }
  }
}
