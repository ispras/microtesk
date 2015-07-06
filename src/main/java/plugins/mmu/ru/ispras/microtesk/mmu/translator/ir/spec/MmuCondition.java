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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerVariable;

/**
 * {@link MmuCondition} represents a set of {@code AND}- or {@code OR}-connected equalities or
 * inequalities ({@link MmuEquality}).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MmuCondition {
  public static enum Type {
    AND,
    OR
  }

  public static MmuCondition EQ(final MmuExpression expression) {
    return new MmuCondition(MmuEquality.EQ(expression));
  }

  public static MmuCondition NEQ(final MmuExpression expression) {
    return new MmuCondition(MmuEquality.EQ(expression));
  }

  public static MmuCondition EQ(final IntegerField field) {
    return new MmuCondition(MmuEquality.EQ(field));
  }

  public static MmuCondition NEQ(final IntegerField field) {
    return new MmuCondition(MmuEquality.NEQ(field));
  }

  public static MmuCondition EQ(final IntegerVariable variable) {
    return new MmuCondition(MmuEquality.EQ(variable));
  }

  public static MmuCondition NEQ(final IntegerVariable variable) {
    return new MmuCondition(MmuEquality.NEQ(variable));
  }

  public static MmuCondition EQ(final MmuExpression expression, final BigInteger value) {
    return new MmuCondition(MmuEquality.EQ(expression, value));
  }

  public static MmuCondition NEQ(final MmuExpression expression, final BigInteger value) {
    return new MmuCondition(MmuEquality.EQ(expression, value));
  }

  public static MmuCondition EQ(final IntegerField field, final BigInteger value) {
    return new MmuCondition(MmuEquality.EQ(field, value));
  }

  public static MmuCondition NEQ(final IntegerField field, final BigInteger value) {
    return new MmuCondition(MmuEquality.NEQ(field, value));
  }

  public static MmuCondition EQ(final IntegerVariable variable, final BigInteger value) {
    return new MmuCondition(MmuEquality.EQ(variable, value));
  }

  public static MmuCondition NEQ(final IntegerVariable variable, final BigInteger value) {
    return new MmuCondition(MmuEquality.NEQ(variable, value));
  }

  public static MmuCondition AND(final List<MmuEquality> equalities) {
    return new MmuCondition(Type.AND, equalities);
  }

  public static MmuCondition OR(final List<MmuEquality> equalities) {
    return new MmuCondition(Type.OR, equalities);
  }

  public static MmuCondition AND(final MmuEquality... equalities) {
    return AND(Arrays.asList(equalities));
  }

  public static MmuCondition OR(final MmuEquality... equalities) {
    return OR(Arrays.asList(equalities));
  }

  /**
   * Tries to create a condition expressing the constraint {@code var is in [min, max]}.
   * 
   * @param var the variable.
   * @param min the lower bound of the range.
   * @param max the upper bound of the range.
   * @return the condition or {@code null}.
   */
  public static MmuCondition RANGE(
      final IntegerVariable var, final BigInteger min, final BigInteger max) {

    if (min.compareTo(max) == 0) {
      return EQ(var, min);
    }

    int lo, hi;

    // MIN = DEAD BEEF XXXX XXXX
    // MAX = DEAD BEEF YYYY YYYY
    for (hi = var.getWidth() - 1; hi >= 0; hi--) {
      if (min.testBit(hi) != max.testBit(hi)) {
        break;
      }
    }

    final List<MmuEquality> equalities = new ArrayList<>();

    if (hi + 1 != var.getWidth()) {
      // Equality: VAR[HI:32] = DEAD BEEF
      equalities.add(
          MmuEquality.EQ(new IntegerField(var, hi + 1, var.getWidth() - 1), min.shiftRight(hi)));
    }

    // MIN = DEAD BEEF XXXX 0000
    // MAX = DEAD BEEF YYYY FFFF
    for (lo = 0; lo < var.getWidth(); lo++) {
      if (min.testBit(lo) || !max.testBit(lo)) {
        break;
      }
    }

    if (lo <= hi && lo < var.getWidth()) {
      // XXXX
      final BigInteger min1 = min.mod(BigInteger.ONE.shiftLeft(hi + 1)).shiftRight(lo);
      // YYYY
      final BigInteger max1 = max.mod(BigInteger.ONE.shiftLeft(hi + 1)).shiftRight(lo);

      // YYYY != FFFF
      if (max1.compareTo(BigInteger.ONE.shiftLeft((hi - lo) + 1).subtract(BigInteger.ONE)) != 0) {
        return null;
      }

      // XXXX > 10 (rather big)
      if (min1.compareTo(BigInteger.TEN) > 0) {
        return null;
      }

      for (int i = 0; i < min1.longValue(); i++) {
        // Inequality: XXXX != i
        equalities.add(MmuEquality.NEQ(new IntegerField(var, lo, hi), BigInteger.valueOf(i)));
      }
    }

    return AND(equalities.toArray(new MmuEquality[] {}));
  }

  /** Condition connective. */
  private final Type type;

  /** Set of equalities and inequalities. */
  private final List<MmuEquality> equalities = new ArrayList<>();

  /**
   * Constructs a condition.
   * 
   * @param type the connective.
   * @param equalities the equalities/inequalities.
   */
  public MmuCondition(final Type type, final List<MmuEquality> equalities) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(equalities);

    this.type = type;
    this.equalities.addAll(equalities);
  }

  /**
   * Constructs a copy of the condition.
   * 
   * @param condition the condition to be copied.
   */
  public MmuCondition(final MmuCondition condition) {
    InvariantChecks.checkNotNull(condition);

    this.type = condition.type;
    this.equalities.addAll(condition.getEqualities());
  }

  /**
   * Constructs a condition consisting of a given equality/inequality.
   * 
   * @param equality the equality/inequality.
   */
  public MmuCondition(final MmuEquality equality) {
    InvariantChecks.checkNotNull(equality);

    this.type = Type.AND;
    this.equalities.add(equality);
  }

  /**
   * Returns the equalities/inequalities of the condition.
   * 
   * @return the list of equalities/inequalities.
   */
  public List<MmuEquality> getEqualities() {
    return equalities;
  }

  @Override
  public String toString() {
    return String.format("%s %s", type, equalities);
  }
}
