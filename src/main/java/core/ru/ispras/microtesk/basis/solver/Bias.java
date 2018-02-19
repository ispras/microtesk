/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link Bias} represents a constraint bias (i.e. measure of obligatoriness).
 *
 * <p>{@code SOFT} constraints are optional, while {@code HARD} constraints are obligatory.</p>
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Bias implements Comparable<Bias> {
  private static final int MIN_BIAS = 0;
  private static final int MAX_BIAS = 100;

  /** Constraint is optional. */
  public static final Bias SOFT = new Bias(MIN_BIAS);

  /** Constraint is obligatory. */
  public static final Bias HARD = new Bias(MAX_BIAS);

  /**
   * Constraint with a given bias.
   *
   * @param bias Bias value.
   * @return Constraint with the specified bias.
   */
  public static final Bias BIAS(final int bias) {
    return new Bias(bias);
  }

  private final int bias;

  private Bias(final int bias) {
    InvariantChecks.checkTrue(MIN_BIAS <= bias && bias <= MAX_BIAS);
    this.bias = bias;
  }

  public int getBias() {
    return bias;
  }

  public boolean isSoft() {
    return bias == MIN_BIAS;
  }

  public boolean isHard() {
    return bias == MAX_BIAS;
  }

  @Override
  public int compareTo(final Bias rhs) {
    InvariantChecks.checkNotNull(rhs);
    return bias - rhs.bias;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof Bias)) {
      return false;
    }

    final Bias r = (Bias) o;
    return bias == r.bias;
  }

  @Override
  public int hashCode() {
    return bias;
  }

  @Override
  public String toString() {
    return String.format("Bias:%d", bias);
  }
}
