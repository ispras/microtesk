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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.solver.IntegerField;
import ru.ispras.microtesk.test.sequence.solver.IntegerVariable;

/**
 * {@link MmuExpression} class represents an expression, which is a sequence of terms.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MmuExpression {
  /** The terms. */
  private final List<IntegerField> terms = new ArrayList<>();

  /**
   * Creates the zero expression (which consists of the empty list of fields).
   * 
   * @return the expression.
   */
  public static MmuExpression ZERO() {
    return new MmuExpression();
  }

  /**
   * Creates a single-variable expression.
   * 
   * @param variable the variable.
   * @return the expression.
   */
  public static MmuExpression VAR(final IntegerVariable variable) {
    final MmuExpression expression = new MmuExpression();
    expression.addLoTerm(new IntegerField(variable));

    return expression;
  }

  /**
   * Creates a bits-selection expression.
   * 
   * @param variable the variable.
   * @param lo the lower bit index.
   * @param hi the upper bit index.
   * @return the expression.
   */
  public static MmuExpression VAR(final IntegerVariable variable, final int lo, final int hi) {
    final MmuExpression expression = new MmuExpression();
    expression.addLoTerm(new IntegerField(variable, lo, hi));

    return expression;
  }

  /**
   * Creates an expression from the variables (UPPER bits come first).
   * RCAT stands for Reversed Concatenation.
   * 
   * @param variables the expression variables
   * @return the expression.
   */
  public static MmuExpression RCAT(final IntegerVariable... variables) {
    return RCATX(Arrays.<IntegerVariable>asList(variables));
  }

  /**
   * Makes Reversed Concatenation of variables the specified collection
   * (higher bits come first).
   * 
   * @param terms the collection of variables.
   * @return the concatenated expression.
   */

  public static MmuExpression RCATX(final Collection<IntegerVariable> variables) {
    final List<IntegerField> fields = new ArrayList<>(variables.size());
    for (final IntegerVariable variable : variables) {
      fields.add(new IntegerField(variable));
    }
    return RCAT(fields);
  }

  /**
   * Creates an expression from the terms (UPPER bits come first). RCAT stands for Reversed
   * Concatenation.
   * 
   * @param terms the expression terms
   * @return the expression.
   */
  public static MmuExpression RCAT(final IntegerField... terms) {
    return RCAT(Arrays.<IntegerField>asList(terms));
  }

  /**
  * Creates an expression from the specified collections terms (UPPER bits come first).
  * RCAT stands for Reversed Concatenation.
  * 
  * @param terms the collection of expression terms
  * @return the concatenated expression.
  */
  public static MmuExpression RCAT(final Collection<IntegerField> terms) {
    final MmuExpression expression = new MmuExpression();

    for (final IntegerField term : terms) {
      expression.addLoTerm(term);
    }

    return expression;
  }

  /**
   * Returns the expression terms (LOWER bits come first).
   * 
   * @return the terms.
   */
  public List<IntegerField> getTerms() {
    return terms;
  }

  /**
   * Adds the term to the expression (LOWER bits).
   * 
   * @param term the term to be added.
   * @throws NullPointerException if {@code term} is null.
   */
  public void addLoTerm(final IntegerField term) {
    InvariantChecks.checkNotNull(term);

    terms.add(0, term);
  }

  /**
   * Adds the terms to the expression (LOWER bits).
   * 
   * @param terms the terms to be added.
   * @throws NullPointerException if {@code terms} is null.
   */
  public void addLoTerms(final List<IntegerField> terms) {
    InvariantChecks.checkNotNull(terms);

    this.terms.addAll(0, terms);
  }

  /**
   * Adds the term to the expression (UPPER bits).
   * 
   * @param term the term to be added.
   * @throws NullPointerException if {@code term} is null.
   */
  public void addHiTerm(final IntegerField term) {
    InvariantChecks.checkNotNull(term);

    terms.add(term);
  }

  /**
   * Adds the terms to the expression (UPPER bits).
   * 
   * @param terms the terms to be added.
   * @throws NullPointerException if {@code terms} is null.
   */
  public void addHiTerms(final List<IntegerField> terms) {
    InvariantChecks.checkNotNull(terms);

    this.terms.addAll(terms);
  }

  /**
   * Returns the size (bit width) of the expression.
   * 
   * @return the size of the expression.
   */
  public int getWidth() {
    int width = 0;

    for (final IntegerField field : terms) {
      width += field.getWidth();
    }

    return width;
  }

  @Override
  public String toString() {
    if (terms.isEmpty()) {
      return "0";
    }

    if (terms.size() == 1) {
      return terms.get(0).toString();
    }

    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    builder.append("{");
    for (final IntegerField field : terms) {
      builder.append(comma ? separator : "");
      builder.append(field);
      comma = true;
    }
    builder.append("}");

    return builder.toString();
  }
}
