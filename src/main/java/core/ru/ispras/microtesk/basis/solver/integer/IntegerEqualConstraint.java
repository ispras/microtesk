/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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
 * {@link IntegerEqualConstraint} class represents constraints of the kind {@code x == value}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerEqualConstraint<V> implements IntegerConstraint<V> {
  private final V variable;
  private BigInteger value;

  public IntegerEqualConstraint(
      final V variable,
      final BigInteger value) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);

    this.variable = variable;
    this.value = value;
  }

  public V getVariable() {
    return variable;
  }

  public BigInteger getValue() {
    return value;
  }

  @Override
  public IntegerFormula<V> getFormula() {
    final IntegerFormula.Builder<V> builder = new IntegerFormula.Builder<>();

    builder.addEquation(variable, value, true);
    return builder.build();
  }

  @Override
  public String toString() {
    return String.format("%s == %s", variable, value.toString(16));
  }
}
