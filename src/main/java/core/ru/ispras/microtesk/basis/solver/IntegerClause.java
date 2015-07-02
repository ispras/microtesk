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

package ru.ispras.microtesk.basis.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerClause} represents a clause, which is a set of OR- or AND-connected
 * equations (objects of {@link IntegerEquation}).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerClause {

  /**
   * This enumeration contains equation clause types.
   */
  public static enum Type {
    /** Conjunction. */
    AND,
    /** Disjunction. */
    OR
  };

  /** The equation clause type: {@code AND} or {@code OR}. */
  private final Type type;
  /** The equations. */
  private final List<IntegerEquation> equations = new ArrayList<>();

  /**
   * Constructs an equation clause of the given type
   * 
   * @param type the equation clause type.
   * @throws IllegalArgumentException if {@code type} is null.
   */
  public IntegerClause(final Type type) {
    InvariantChecks.checkNotNull(type);

    this.type = type;
  }

  /**
   * Constructs a copy of the equation clause.
   * 
   * @param rhs the equation clause to be copied.
   * @throws IllegalArgumentException if {@code rhs} is null.
   */
  public IntegerClause(final IntegerClause rhs) {
    InvariantChecks.checkNotNull(rhs);

    this.type = rhs.type;
    this.equations.addAll(rhs.equations);
  }

  /**
   * Returns the type of the equation clause.
   * 
   * @return the equation clause type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the number of equations in the equation clause.
   * 
   * @return the size of the equation clause.
   */
  public int size() {
    return equations.size();
  }

  /**
   * Adds the equation to the equation clause.
   * 
   * @param equation the equation to be added.
   * @throws IllegalArgumentException if {@code equation} is null.
   */
  public void addEquation(final IntegerEquation equation) {
    InvariantChecks.checkNotNull(equation);

    equations.add(equation);
  }

  /**
   * Adds the equality {@code lhs == rhs} or inequality {@code lhs != rhs} to the equation clause.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code lhs} or {@code rhs} is null.
   */
  public void addEquation(
      final IntegerVariable lhs, final IntegerVariable rhs, final boolean equal) {
    addEquation(new IntegerEquation(lhs, rhs, equal));
  }

  /**
   * Adds the equality {@code var == val} or inequality {@code var != val} to the equation clause.
   * 
   * @param var the left-hand-side variable.
   * @param val the right-hand-side value.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code var} or {@code val} is null.
   */
  public void addEquation(final IntegerVariable var, final BigInteger val, final boolean equal) {
    addEquation(new IntegerEquation(var, val, equal));
  }

  /**
   * Adds the equations of the given equation clause to this clause.
   * 
   * @param clause the clause whose equations to be added.
   * @throws IllegalArgumentException if {@code clause} is null.
   */
  public void addEquationClause(final IntegerClause clause) {
    InvariantChecks.checkNotNull(clause);

    equations.addAll(clause.getEquations());
  }

  /**
   * Returns the equations of the set.
   * 
   * @return the equations.
   */
  public List<IntegerEquation> getEquations() {
    return equations;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof IntegerClause)) {
      return false;
    }

    final IntegerClause r = (IntegerClause) o;

    return equations.equals(r.equations);
  }

  @Override
  public int hashCode() {
    return equations.hashCode();
  }

  @Override
  public String toString() {
    return equations.toString();
  }
}
