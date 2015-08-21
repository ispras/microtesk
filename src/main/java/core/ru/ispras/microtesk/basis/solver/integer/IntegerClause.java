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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerClause} represents a clause, which is a set of OR- or AND-connected
 * equations (objects of {@link IntegerEquation}).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerClause<V> {
  /**
   * {@link Type} contains clause types.
   */
  public static enum Type {
    /** Conjunction. */
    AND,
    /** Disjunction. */
    OR
  };

  /** The clause type: {@code AND} or {@code OR}. */
  private final Type type;
  /** The equations. */
  private final List<IntegerEquation<V>> equations = new ArrayList<>();

  /**
   * Constructs an clause of the given type
   * 
   * @param type the clause type.
   * @throws IllegalArgumentException if {@code type} is null.
   */
  public IntegerClause(final Type type) {
    InvariantChecks.checkNotNull(type);

    this.type = type;
  }

  /**
   * Constructs an clause of the given type with the given set of equations.
   * 
   * @param type the clause type.
   * @param equations the equations.
   * @throws IllegalArgumentException if {@code type} or {@code equations} is null.
   */
  public IntegerClause(final Type type, final Collection<IntegerEquation<V>> equations) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(equations);

    this.type = type;
    this.equations.addAll(equations);
  }

  /**
   * Constructs a copy of the clause.
   * 
   * @param rhs the clause to be copied.
   * @throws IllegalArgumentException if {@code rhs} is null.
   */
  public IntegerClause(final IntegerClause<V> rhs) {
    InvariantChecks.checkNotNull(rhs);

    this.type = rhs.type;
    this.equations.addAll(rhs.equations);
  }

  /**
   * Returns the type of the clause.
   * 
   * @return the clause type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the number of equations in the clause.
   * 
   * @return the size of the clause.
   */
  public int size() {
    return equations.size();
  }

  /**
   * Adds the equation to the clause.
   * 
   * @param equation the equation to be added.
   * @throws IllegalArgumentException if {@code equation} is null.
   */
  public void addEquation(final IntegerEquation<V> equation) {
    InvariantChecks.checkNotNull(equation);
    equations.add(equation);
  }

  /**
   * Adds the equality {@code lhs == rhs} or inequality {@code lhs != rhs} to the clause.
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
   * Adds the equality {@code var == val} or inequality {@code var != val} to the clause.
   * 
   * @param var the left-hand-side variable.
   * @param val the right-hand-side value.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code var} or {@code val} is null.
   */
  public void addEquation(
      final V var, final BigInteger val, final boolean equal) {
    addEquation(new IntegerEquation<V>(var, val, equal));
  }

  /**
   * Adds the equations to this clause.
   * 
   * @param equations the equations to be added.
   * @throws IllegalArgumentException if {@code equations} is null.
   */
  public void addEquations(final Collection<IntegerEquation<V>> equations) {
    InvariantChecks.checkNotNull(equations);
    equations.addAll(equations);
  }

  /**
   * Adds the equations of the given clause to this clause.
   * 
   * @param clause the clause whose equations to be added.
   * @throws IllegalArgumentException if {@code clause} is null.
   */
  public void addClause(final IntegerClause<V> clause) {
    InvariantChecks.checkNotNull(clause);
    equations.addAll(clause.getEquations());
  }

  /**
   * Adds the equations of the given clauses to this clause.
   * 
   * @param clauses the clauses whose equations to be added.
   * @throws IllegalArgumentException if {@code clauses} is null.
   */
  public void addClauses(final Collection<IntegerClause<V>> clauses) {
    InvariantChecks.checkNotNull(clauses);

    for (final IntegerClause<V> clause : clauses) {
      addClause(clause);
    }
  }

  /**
   * Returns the equations of the set.
   * 
   * @return the equations.
   */
  public List<IntegerEquation<V>> getEquations() {
    return equations;
  }

  /**
   * Checks whether this clause contradicts to the given equation.
   * 
   * @param equation the equation to be matched with this one.
   * @return {@code true} if this clause definitely contradicts to the given equation;
   *         {@code false} if this clause seems to be consistent to the given equation. 
   */
  public boolean contradictsTo(final IntegerEquation<V> equation) {
    InvariantChecks.checkNotNull(equation);

    for (final IntegerEquation<V> clauseEquation : equations) {
      if (clauseEquation.contradictsTo(equation)) {
        if (type == Type.AND) {
          return true;
        }
      } else {
        if (type == Type.OR) {
          return false;
        }
      }
    }

    return type == Type.AND ? false : true;
  }

  /**
   * Checks whether this clause ({@code A}) is stronger than the given equation ({@code B}), i.e.
   * the property {@code A => B} holds.
   * 
   * @param equation the equation to be matched with this one.
   * @return {@code true} if this clause is definitely stronger than the given equation;
   *         {@code false} if this clause does not seem to be stronger than the given equation. 
   */
  public boolean strongerThan(final IntegerEquation<V> equation) {
    InvariantChecks.checkNotNull(equation);

    for (final IntegerEquation<V> clauseEquation : equations) {
      if (clauseEquation.strongerThan(equation)) {
        if (type == Type.AND) {
          return true;
        }
      } else {
        if (type == Type.OR) {
          return false;
        }
      }
    }

    return type == Type.AND ? false : true;
  }

  /**
   * Checks whether this clause ({@code A}) is stronger than the given one ({@code B}), i.e.
   * the property {@code A => B} holds.
   * 
   * @param clause the clause to be matched with this one.
   * @return {@code true} if this clause is definitely stronger than the given one;
   *         {@code false} if this clause does not seem to be stronger than the given one. 
   */
  public boolean strongerThan(final IntegerClause<V> clause) {
    InvariantChecks.checkNotNull(clause);
    InvariantChecks.checkTrue(type == clause.type);

    final Set<IntegerEquation<V>> lhs =
        new HashSet<>(type == Type.AND ? equations : clause.equations);
    final List<IntegerEquation<V>> rhs = (type == Type.AND ? clause.equations : equations);

    return lhs.containsAll(rhs);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof IntegerClause)) {
      return false;
    }

    final IntegerClause<V> r = (IntegerClause<V>) o;

    return equations.equals(r.equations);
  }

  @Override
  public int hashCode() {
    return equations.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s %s", type, equations.toString());
  }
}
