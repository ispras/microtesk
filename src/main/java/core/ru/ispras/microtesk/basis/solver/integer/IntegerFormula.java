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

package ru.ispras.microtesk.basis.solver.integer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerFormula} represents a formula, which is a set of clauses
 * (objects of {@link IntegerClause}).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFormula<V> {
  /** AND-connected clauses of the OR type. */
  private final List<IntegerClause<V>> clauses = new ArrayList<>();

  /**
   * Constructs the equation formula.
   */
  public IntegerFormula() {
    // Do nothing.
  }

  /**
   * Constructs a copy of the equation formula.
   * 
   * @param rhs the equation formula to be copied.
   * @throws IllegalArgumentException if {@code rhs} is null.
   */
  public IntegerFormula(final IntegerFormula<V> rhs) {
    InvariantChecks.checkNotNull(rhs);
    this.clauses.addAll(rhs.clauses);
  }

  /**
   * Returns the number of clauses in the equation formula.
   * 
   * @return the size of the equation formula.
   */
  public int size() {
    return clauses.size();
  }

  /**
   * Adds the equation clause to the equation formula.
   * 
   * @param clause the equation clause to be added.
   * @throws IllegalArgumentException if {@code clause} is null.
   */
  public void addEquationClause(final IntegerClause<V> clause) {
    InvariantChecks.checkNotNull(clause);

    if (clause.getType() == IntegerClause.Type.OR) {
      clauses.add(clause);
    } else {
      for (final IntegerEquation<V> equation : clause.getEquations()) {
        addEquation(equation);
      }
    }
  }

  /**
   * Adds the equation to the equation formula.
   * 
   * @param equation the equation to be added.
   * @throws IllegalArgumentException if {@code equation} is null.
   */
  public void addEquation(final IntegerEquation<V> equation) {
    InvariantChecks.checkNotNull(equation);

    final IntegerClause<V> clause = new IntegerClause<V>(IntegerClause.Type.AND);
    clause.addEquation(equation);

    clauses.add(clause);
  }

  /**
   * Adds the equality {@code lhs == rhs} or inequality {@code lhs != rhs} to the equation formula.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code lhs} or {@code rhs} is null.
   */
  public void addEquation(
      final V lhs, final V rhs, final boolean equal) {
    addEquation(new IntegerEquation<V>(lhs, rhs, equal));
  }

  /**
   * Adds the equality {@code var == val} or inequality {@code var != val} to the equation formula.
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
   * Returns the equation clauses of the set.
   * 
   * @return the equation clauses.
   */
  public List<IntegerClause<V>> getEquationClauses() {
    return clauses;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof IntegerFormula)) {
      return false;
    }

    final IntegerFormula<V> r = (IntegerFormula<V>) o;

    return clauses.equals(r.clauses);
  }

  @Override
  public int hashCode() {
    return clauses.hashCode();
  }

  @Override
  public String toString() {
    return clauses.toString();
  }
}
