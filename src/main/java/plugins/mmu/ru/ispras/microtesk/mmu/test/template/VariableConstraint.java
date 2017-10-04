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

package ru.ispras.microtesk.mmu.test.template;

import java.math.BigInteger;
import java.util.Set;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.Value;

/**
 * The {@link VariableConstraint} class describes constraints on a memory variable.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class VariableConstraint {
  private final Node variable;
  private final Value variate;
  private final Set<BigInteger> values;

  public VariableConstraint(
      final Node variable,
      final Value variate,
      final Set<BigInteger> values) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(variate);
    InvariantChecks.checkNotNull(values);

    this.variable = variable;
    this.variate = variate;
    this.values = values;
  }

  public Node getVariable() {
    return variable;
  }

  public Value getVariate() {
    return variate;
  }

  public Set<BigInteger> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return String.format("VariableValueConstraint [variable=%s, variate=%s, domain=%s]",
        variable,
        variate,
        values);
  }
}
