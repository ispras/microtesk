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
