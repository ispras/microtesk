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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerEquation} represents an equality or inequality of two integer variables
 * (objects of {@link IntegerVariable}).
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerEquation {
  /** The equality/inequality flag. */
  public boolean equal;
  /** The variable-value/variable-variable flag. */
  public boolean value;
  /** The left-hand-side variable. */
  public IntegerVariable lhs;
  /** The right-hand-side variable. */
  public IntegerVariable rhs;
  /** The right-hand-side value. */
  public BigInteger val;

  /**
   * Constructs an equality/inequality.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code lhs} or {@code rhs} is null.
   */
  public IntegerEquation(
      final IntegerVariable lhs, final IntegerVariable rhs, final boolean equal) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    this.lhs = lhs;
    this.rhs = rhs;
    this.equal = equal;
    this.value = false;
  }

  /**
   * Constructs an equality/inequality.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side value.
   * @param equal the equality/inequality flag.
   * @throws IllegalArgumentException if {@code lhs} or {@code rhs} is null.
   */
  public IntegerEquation(
      final IntegerVariable lhs, final BigInteger rhs, final boolean equal) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    this.lhs = lhs;
    this.val = rhs;
    this.equal = equal;
    this.value = true;
  }

  /**
   * Checks whether the equation contradicts to this one.
   * 
   * @param equation the equation to be matched with this one.
   * @return {@code true} if the equation definitely contradicts with this one;
   *         {@code false} if the equation seems to be consistent with this one. 
   */
  public boolean contradicts(final IntegerEquation equation) {
    InvariantChecks.checkNotNull(equation);

    if (!lhs.equals(equation.lhs)) {
      return false;
    }

    if (value && equation.value) {
      return ((val.compareTo(equation.val) == 0) != (equal == equation.equal));
    }

    if (!value && !equation.value) {
      return rhs.equals(equation.rhs) && equal != equation.equal;
    }

    return false;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof IntegerEquation)) {
      return false;
    }

    final IntegerEquation r = (IntegerEquation) o;

    return equal == r.equal && value == r.value && lhs.equals(r.lhs) &&
        (value && rhs.equals(r.rhs) || !value && val.equals(r.val));
  }

  @Override
  public int hashCode() {
    return ((31 * lhs.hashCode() + (value ? val.hashCode() : rhs.hashCode()) << 1)) +
        (equal ? 0 : 1);
  }

  @Override
  public String toString() {
    return String.format("%s %s %s", lhs, (equal ? "==" : "!="),
        (value ? val.toString() : rhs.toString()));
  }
}
