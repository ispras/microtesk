/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.integer;

import java.math.BigInteger;
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerFormulaBuilder} represents an abstract formula builder.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class IntegerFormulaBuilder<V> {
  /**
   * Adds the constraint to the formula.
   * 
   * @param constraint the constraint to be added.
   * @throws IllegalArgumentException if {@code constraint} is null.
   */
  public final void addConstraint(final IntegerConstraint<V> constraint) {
    InvariantChecks.checkNotNull(constraint);
    addFormula(constraint.getFormula());
  }

  /**
   * Adds the sub-formula to the formula.
   * 
   * @param formula the sub-formula to be added.
   * @throws IllegalArgumentException if {@code formula} is null.
   */
  public final void addFormula(final IntegerFormula<V> formula) {
    InvariantChecks.checkNotNull(formula);
    addClauses(formula.getClauses());
  }

  /**
   * Adds the clauses to the formula.
   * 
   * @param clauses the clauses to be added.
   * @throws IllegalArgumentException if {@code clauses} is null.
   */
  public final void addClauses(final Collection<IntegerClause<V>> clauses) {
    InvariantChecks.checkNotNull(clauses);

    for (final IntegerClause<V> clause : clauses) {
      addClause(clause);
    }
  }

  /**
   * Adds the equation to the formula.
   * 
   * @param equation the equation to be added.
   * @throws IllegalArgumentException if {@code equation} is null.
   */
  public final void addEquation(final IntegerEquation<V> equation) {
    InvariantChecks.checkNotNull(equation);
    addClause(new IntegerClause<V>(equation));
  }

  /**
   * Adds the equality {@code lhs == rhs} or inequality {@code lhs != rhs} to the formula.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code lhs} or {@code rhs} is null.
   */
  public void addEquation(final V lhs, final V rhs, final boolean equal) {
    addEquation(new IntegerEquation<V>(lhs, rhs, equal));
  }

  /**
   * Adds the equality {@code var == val} or inequality {@code var != val} to the formula.
   * 
   * @param var the left-hand-side variable.
   * @param val the right-hand-side value.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code var} or {@code val} is null.
   */
  public void addEquation(final V var, final BigInteger val, final boolean equal) {
    addEquation(new IntegerEquation<V>(var, val, equal));
  }

  /**
   * Adds the clause to the formula.
   * 
   * @param clause the clause to be added.
   * @throws IllegalArgumentException if {@code clause} is null.
   */
  public abstract void addClause(final IntegerClause<V> clause);

  @Override
  public abstract IntegerFormulaBuilder<V> clone();
}
