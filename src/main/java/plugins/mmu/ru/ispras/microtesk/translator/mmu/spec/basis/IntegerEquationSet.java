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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class implements a simple constraint solver.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerEquationSet {

  /**
   * This enumeration contains equation set types.
   */
  public static enum Type {
    /** Conjunction. */
    AND,
    /** Disjunction. */
    OR
  };

  /** The equation set type: {@code AND} or {@code OR}. */
  private Type type;
  /** The equations. */
  private List<IntegerEquation> equations = new ArrayList<>();

  /**
   * Constructs an equation set of the given type
   * 
   * @param type the equation set type.
   * @throws NullPointerException if {@code type} is null.
   */
  public IntegerEquationSet(final Type type) {
    InvariantChecks.checkNotNull(type);

    this.type = type;
  }

  /**
   * Returns the type of the equation set.
   * 
   * @return the equation set type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the number of equations in the equation set.
   * 
   * @return the size of the equation set.
   */
  public int size() {
    return equations.size();
  }

  /**
   * Adds the equation to the equation set.
   * 
   * @param equation the equation to be added.
   * @throws NullPointerException if {@code equation} is null.
   */
  public void addEquation(final IntegerEquation equation) {
    InvariantChecks.checkNotNull(equation);

    equations.add(equation);
  }

  /**
   * Adds the equality {@code lhs == rhs} or inequality {@code lhs != rhs} to the equation set.
   *  
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @param equal the equality/inequality flag.
   * @throws NullPointerException if {@code lhs} or {@code rhs} is null.
   */
  public void addEquation(final IntegerVariable lhs, final IntegerVariable rhs, final boolean equal) {
    addEquation(new IntegerEquation(lhs, rhs, equal));
  }

  /**
   * Adds the equality {@code var == val} or inequality {@code var != val} to the equation set.
   *  
   * @param var the left-hand-side variable.
   * @param val the right-hand-side value.
   * @param equal the equality/inequality flag.
   * @throws NullPointerException if {@code var} or {@code val} is null.
   */
  public void addEquation(final IntegerVariable var, final BigInteger val, final boolean equal) {
    addEquation(new IntegerEquation(var, val, equal));
  }

  /**
   * Returns the equations of the set.
   * 
   * @return the equations.
   */
  public List<IntegerEquation> getEquations() {
    return equations;
  }
}
