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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * {@link BitVectorEqualConstraint} class represents a constraint of the kind {@code x == value}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BitVectorEqualConstraint implements BitVectorConstraint {
  private final Node variable;
  private final BitVector value;
  private final Node formula;

  public BitVectorEqualConstraint(
      final Node variable,
      final BitVector value) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);

    this.variable = variable;
    this.value = value;

    this.formula = new NodeOperation(
        StandardOperation.EQ,
        variable,
        FortressUtils.makeNodeBitVector(value));
  }

  public Node getVariable() {
    return variable;
  }

  public BitVector getValue() {
    return value;
  }

  @Override
  public Node getFormula() {
    return formula;
  }

  @Override
  public String toString() {
    return String.format("%s == %s", variable, value.toHexString());
  }
}
