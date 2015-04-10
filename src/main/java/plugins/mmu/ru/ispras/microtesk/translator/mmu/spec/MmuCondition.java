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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

/**
 * This class represents a set of equalities/inequalities.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuCondition {

  /** The list of equalities/inequalities. */
  private final List<MmuEquality> equalities = new ArrayList<>();

  /**
   * Creates a condition with equality relation between the variable and its value.
   * 
   * @param variable the variable.
   * @param lo the lower bit index.
   * @param hi the upper bit index.
   * @param value the constant.
   * @return the condition.
   */
  public static MmuCondition EQ(final IntegerVariable variable, final int lo, final int hi,
      final BigInteger value) {
    final MmuCondition condition = new MmuCondition(MmuEquality.EQ(variable, lo, hi, value));
    return condition;
  }

  /**
   * Creates a condition with inequality relation between the variable and its value.
   * 
   * @param variable the variable.
   * @param lo the lower bit index.
   * @param hi the upper bit index.
   * @param value the constant.
   * @return the condition.
   */
  public static MmuCondition NEQ(final IntegerVariable variable, final int lo, final int hi,
      final BigInteger value) {
    final MmuCondition condition = new MmuCondition(MmuEquality.NEQ(variable, lo, hi, value));
    return condition;
  }

  /**
   * Creates a condition with equality relation between the variable and its value.
   * 
   * @param variable the variable.
   * @param bit the bit index.
   * @param value the constant.
   * @return the condition.
   */
  public static MmuCondition EQ(final IntegerVariable variable, final int bit,
      final BigInteger value) {
    return EQ(variable, bit, bit, value);
  }

  /**
   * Creates a condition with inequality relation between the variable and its value.
   * 
   * @param variable the variable.
   * @param bit the bit index.
   * @param value the constant.
   * @return the condition.
   */
  public static MmuCondition NEQ(final IntegerVariable variable, final int bit,
      final BigInteger value) {
    return NEQ(variable, bit, bit, value);
  }

  /**
   * Creates a condition with equality relation between the variable and its value.
   * 
   * @param variable the variable.
   * @param value the constant.
   * @return the condition.
   */
  public static MmuCondition EQ(final IntegerVariable variable, final BigInteger value) {
    return EQ(variable, 0, variable.getWidth() - 1, value);
  }

  /**
   * Creates a condition with inequality relation between the variable and its value.
   * 
   * @param variable the variable.
   * @param value the constant.
   * @return the condition.
   */
  public static MmuCondition NEQ(final IntegerVariable variable, final BigInteger value) {
    return NEQ(variable, 0, variable.getWidth() - 1, value);
  }

  /**
   * Creates a condition consisting of a given equalities/inequalities.
   * 
   * @param equalities the equalities.
   * @return the condition.
   */
  public static MmuCondition AND(final MmuEquality... equalities) {
    final MmuCondition condition = new MmuCondition();

    for (final MmuEquality equality : equalities) {
      condition.addEquality(equality);
    }

    return condition;
  }

  /**
   * Tries to create a condition expressing the constraint {@code var is in [min, max]}.
   * 
   * @param var the variable.
   * @param min the lower bound of the range.
   * @param max the upper bound of the range.
   * @return the condition or {@code null}.
   */
  public static MmuCondition RANGE(final IntegerVariable var,
      final BigInteger min, final BigInteger max) {

    if (min.compareTo(max) == 0) {
      return EQ(var, min);
    }

    int lo1;
    int hi1;

    for (hi1 = var.getWidth() - 1; hi1 >= 0; hi1--) {
      if (min.testBit(hi1) != max.testBit(hi1)) {
        break;
      }
    }

    final List<MmuEquality> equalities = new ArrayList<>();

    if (hi1 + 1 != var.getWidth()) {
      equalities.add(MmuEquality.EQ(var, hi1 + 1, var.getWidth() - 1, min.shiftRight(hi1)));
    }

    for (lo1 = 0; lo1 < var.getWidth(); lo1++) {
      if (min.testBit(lo1) || !max.testBit(lo1)) {
        break;
      }
    }

    if (lo1 <= hi1 && lo1 < var.getWidth()) {
      int lo2;
      int hi2;

      for (hi2 = hi1; hi2 >= lo1; hi2--) {
        if (min.testBit(hi2) || !max.testBit(hi2)) {
          break;
        }
      }

      for (lo2 = lo1; lo2 < hi1; lo2++) {
        if (min.testBit(lo2) != max.testBit(lo2)) {
          break;
        }
      }

      if (lo2 != hi2 + 1 || lo2 == lo1) {
        // Cannot approximate the range constraint.
        return null;
      }

      equalities.add(MmuEquality.EQ(var, lo1, lo2 - 1,
          min.mod(BigInteger.ONE.shiftLeft(lo2)).shiftRight(lo1)));
    }

    return AND(equalities.toArray(new MmuEquality[] {}));
  }

  /**
   * Constructs a condition.
   */
  public MmuCondition() {}

  /**
   * Constructs a copy of the condition.
   * 
   * @param condition the condition to be copied.
   * @throws NullPointerException if {@code condition} is null.
   */
  public MmuCondition(final MmuCondition condition) {
    InvariantChecks.checkNotNull(condition);

    addEqualities(condition.getEqualities());
  }

  /**
   * Constructs a new MmuCondition with a given MmuEquality.
   * 
   * @param equality the equality/inequality.
   * @throws NullPointerException if {@code equality} is null.
   */
  public MmuCondition(final MmuEquality equality) {
    InvariantChecks.checkNotNull(equality);

    equalities.add(equality);
  }

  /**
   * Returns the equalities/inequalities of the condition.
   * 
   * @return the list of equalities/inequalities.
   */
  public List<MmuEquality> getEqualities() {
    return equalities;
  }

  /**
   * Adds the equality/inequality to the condition.
   * 
   * @param equality the equality/inequality.
   * @throws NullPointerException if {@code equality} is null.
   */
  public void addEquality(final MmuEquality equality) {
    InvariantChecks.checkNotNull(equality);

    equalities.add(equality);
  }

  /**
   * Adds the equalities/inequalities to the condition.
   * 
   * @param equalities the equalities/inequalities.
   * @throws NullPointerException if {@code equalities} is null.
   */
  public void addEqualities(final Collection<MmuEquality> equalities) {
    InvariantChecks.checkNotNull(equalities);

    this.equalities.addAll(equalities);
  }

  @Override
  public String toString() {
    final String newLine = System.getProperty("line.separator");

    final StringBuilder string = new StringBuilder("Condition: {");
    string.append(newLine);
    for (final MmuEquality equality : equalities) {
      string.append(equality.toString());
      string.append(",");
      string.append(newLine);
    }
    string.append("}");
    string.append(newLine);

    return string.toString();
  }
}
