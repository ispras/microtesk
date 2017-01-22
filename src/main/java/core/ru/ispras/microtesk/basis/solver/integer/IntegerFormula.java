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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerFormula} represents a formula, which is a set of {@link IntegerClause}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFormula<V> {
  /**
   * {@link Builder} is a {@link IntegerFormula} builder.
   */
  public static final class Builder<V> extends IntegerFormulaBuilder<V> {
    /** AND-connected clauses of the OR type. */
    private final Collection<IntegerClause<V>> clauses = new LinkedHashSet<>();

    /**
     * Constructs a formula builder.
     */
    public Builder() {}

    /**
     * Constructs a copy of the formula builder.
     * 
     * @param rhs the formula builder to be copied.
     */
    public Builder(final Builder<V> rhs) {
      clauses.addAll(rhs.clauses);
    }

    /**
     * Returns the current number of clauses in the formula being built.
     * 
     * @return the size of the formula.
     */
    public int size() {
      return clauses.size();
    }

    /**
     * Return the current clauses of the formula being built.
     * 
     * @return the current clauses.
     */
    public Collection<IntegerClause<V>> getClauses() {
      return clauses;
    }

    @Override
    public void addClause(final IntegerClause<V> clause) {
      InvariantChecks.checkNotNull(clause);

      if (clause.getType() == IntegerClause.Type.OR && clause.size() != 1) {
        clauses.add(clause);
      } else {
        for (final IntegerEquation<V> equation : clause.getEquations()) {
          clauses.add(new IntegerClause<V>(equation));
        }
      }
    }

    /**
     * Builds a formula.
     * 
     * @return the reference to the built formula.
     */
    public IntegerFormula<V> build() {
      return new IntegerFormula<>(clauses);
    }
  }

  /** AND-connected clauses of the OR type. */
  private final Collection<IntegerClause<V>> clauses;

  /** The variables. */
  private Collection<V> variables;

  /**
   * Constructs a formula.
   * 
   * @param clauses the formula clauses.
   * @throws IllegalArgumentException if {@code clauses} is null.
   */
  public IntegerFormula(final Collection<IntegerClause<V>> clauses) {
    InvariantChecks.checkNotNull(clauses);
    this.clauses = Collections.unmodifiableCollection(clauses);
  }

  /**
   * Constructs a copy of the formula.
   * 
   * @param rhs the formula to be copied.
   * @throws IllegalArgumentException if {@code rhs} is null.
   */
  public IntegerFormula(final IntegerFormula<V> rhs) {
    this(rhs.clauses);
  }

  /**
   * Checks whether there are no clauses.
   * 
   * @return {@code true} if there are no clauses; {@code false} otherwise.
   */
  public boolean isEmpty() {
    return clauses.isEmpty();
  }

  /**
   * Returns the number of clauses in the formula.
   * 
   * @return the size of the formula.
   */
  public int size() {
    return clauses.size();
  }

  /**
   * Returns the equation clauses of the formula.
   * 
   * @return the equation clauses.
   */
  public Collection<IntegerClause<V>> getClauses() {
    return clauses;
  }

  /**
   * Returns the variables used in the clause.
   * 
   * @return the variables.
   */
  public Collection<V> getVariables() {
    if (variables != null) {
      return variables;
    }

    variables = new LinkedHashSet<>();

    for (final IntegerClause<V> clause : clauses) {
      variables.addAll(clause.getVariables());
    }

    return variables;
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
