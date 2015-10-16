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

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerVariable} represents a variable, which is a named entity with given width.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IntegerVariable {
  /** The variable name. */
  private final String name;
  /** The variable width. */
  private final int width;
  /** The variable value (optional). */
  private final BigInteger value;

  /**
   * Constructs a fake variable to store a constant value.
   * 
   * @param width variable width in bits.
   * @param value value to be stored.
   * @throws IllegalArgumentException if {@code width <= 0} or if {@code value is null}.
   */
   public IntegerVariable(final int width, final BigInteger value) {
     this(
        String.format("const:%d=%x", width, value),
        width,
        value
        );
  }

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
      InvariantChecks.checkTrue(value.compareTo(BigInteger.ZERO) >= 0);
      InvariantChecks.checkTrue(value.compareTo(BitUtils.getBigIntegerMask(width)) <= 0);
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
  public final String getName() {
    return name;
  }

  /**
   * Returns the width of the variable.
   * 
   * @return the variable width.
   */
  public final int getWidth() {
    return width;
  }

  /**
   * Checks whether the variable is defined, i.e. there is a value associated with the variable.
   * 
   * @return {@code true} if the variable is defined; {@code false} otherwise.
   */
  public final boolean isDefined() {
    return getValue() != null;
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
  public final IntegerField field(final int lo, final int hi) {
    return new IntegerField(this, lo, hi);
  }

  @Override
  public final boolean equals(final Object o) {
    if (o == null || !(o instanceof IntegerVariable)) {
      return false;
    }

    final IntegerVariable r = (IntegerVariable) o;

    if (!name.equals(r.name)) {
      return false;
    }

    return isDefined() ? getValue().compareTo(r.getValue()) == 0 : true;
  }

  @Override
  public final int hashCode() {
    return name.hashCode();
  }

  @Override
  public final String toString() {
    return isDefined() ? String.format("%s=%s", name, getValue()) : name;
  }
}
