/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.bitvector;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BitVectorFormulaBuilder} represents an abstract formula builder.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class BitVectorFormulaBuilder {
  /**
   * Adds the constraint to the formula.
   * 
   * @param constraint the constraint to be added.
   * @throws IllegalArgumentException if {@code constraint} is null.
   */
  public final void addConstraint(final BitVectorConstraint constraint) {
    InvariantChecks.checkNotNull(constraint);
    addFormula(constraint.getFormula());
  }

  /**
   * Adds the sub-formula to the formula.
   * 
   * @param formula the sub-formula to be added.
   * @throws IllegalArgumentException if {@code formula} is null.
   */
  public abstract void addFormula(final Node formula);

  @Override
  public abstract BitVectorFormulaBuilder clone();
}
