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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerVariable} represents a variable, which is a named entity with given width.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerVariable {
  /** The variable name. */
  private final String name;
  /** The variable width. */
  private final int width;
  /** The variable value (optional). */
  private final BigInteger value;

  /**
   * Constructs a variable.
   * 
   * @param name the variable name.
   * @param width the variable width.
   * @param value the variable value or null.
   * @throws IllegalArgumentException if {@code width} is not positive.
   */
  public IntegerVariable(final String name, final int width, final BigInteger value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkGreaterThanZero(width);

    if (value != null) {
      InvariantChecks.checkTrue(value.compareTo(BigInteger.valueOf(0)) >= 0);
      InvariantChecks.checkTrue(value.compareTo(BigInteger.ONE.shiftLeft(width - 1)) <= 0);
    }

    this.name = name;
    this.width = width;
    this.value = value;
  }

  /**
   * Constructs a variable.
   * 
   * @param name the variable name.
   * @param width the variable width.
   * @throws IllegalArgumentException if {@code name == null} or {@code width} is not positive.
   */
  public IntegerVariable(final String name, final int width) {
    this(name, width, null);
  }

  /**
   * Returns the name of the variable.
   * 
   * @return the variable name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the width of the variable.
   * 
   * @return the variable width.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Checks whether the variable is defined, i.e. there is a value associated with the variable.
   * 
   * @return {@code true} if the variable is defined; {@code false} otherwise.
   */
  public boolean isDefined() {
    return value != null;
  }

  /**
   * Returns the value of the variable.
   * 
   * @return the variable value.
   */
  public BigInteger getValue() {
    return value;
  }

  /**
   * Creates an integer field for the current variable.
   * 
   * @param lo the lower bit index.
   * @param hi the upper bit index.
   * @return An integer field.
   */
  public IntegerField field(final int lo, final int hi) {
    return new IntegerField(this, lo, hi);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || !(o instanceof IntegerVariable)) {
      return false;
    }

    final IntegerVariable r = (IntegerVariable) o;

    if (!name.equals(r.name)) {
      return false;
    }

    return value != null ? value.compareTo(r.value) == 0 : true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return value != null ? String.format("%s=%s", name, value) : name;
  }
}
