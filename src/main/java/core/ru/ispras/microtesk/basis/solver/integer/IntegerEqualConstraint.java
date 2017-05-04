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
import ru.ispras.microtesk.test.template.Value;

/**
 * {@link IntegerEqualConstraint} class represents constraints of the kind {@code x == value}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerEqualConstraint<V> implements IntegerConstraint<V> {
  private final V variable;
  private final Value variate;

  private BigInteger value = null;

  public IntegerEqualConstraint(
      final V variable,
      Value variate) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(variate);

    this.variable = variable;
    this.variate = variate;

    // Value is randomized.
    this.value = variate.getValue();
  }

  public V getVariable() {
    return variable;
  }

  public Value getVariate() {
    return variate;
  }

  @Override
  public IntegerFormula<V> getFormula() {
    final IntegerFormula.Builder<V> builder = new IntegerFormula.Builder<>();

    builder.addEquation(variable, value, true);
    return builder.build();
  }

  @Override
  public void randomize() {
    // Value is randomized.
    this.value = variate.getValue();
  }

  @Override
  public String toString() {
    return String.format("%s == %s (0x%s)", variable, variate, value.toString(16));
  }
}
