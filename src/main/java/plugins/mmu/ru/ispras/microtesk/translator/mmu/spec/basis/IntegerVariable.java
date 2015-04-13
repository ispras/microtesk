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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class represents a variable, which is a named entity with given width.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerVariable {
  /** The variable name. */
  private final String name;
  /** The variable width (in bits). */
  private final int width;

  /**
   * Constructs a variable.
   * 
   * @param name the variable name.
   * @param width the variable width.
   * 
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if {@code width} is not positive.
   */
  public IntegerVariable(final String name, int width) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkGreaterThanZero(width);

    this.name = name;
    this.width = width;
  }

  /**
   * Returns the variable name.
   * 
   * @return the variable name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the variable width.
   * 
   * @return the variable width (in bits).
   */
  public int getWidth() {
    return width;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || !(o instanceof IntegerVariable)) {
      return false;
    }

    final IntegerVariable other = (IntegerVariable) o;
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }
}
