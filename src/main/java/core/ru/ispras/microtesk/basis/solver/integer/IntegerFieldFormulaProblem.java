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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerFormulaProblem} represents an integer problem.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFieldFormulaProblem extends IntegerFormulaBuilder<IntegerField> {
  private final IntegerFormula.Builder<IntegerField> builder;

  public IntegerFieldFormulaProblem() {
    this.builder = new IntegerFormula.Builder<>();
  }

  public IntegerFieldFormulaProblem(final IntegerFieldFormulaProblem r) {
    InvariantChecks.checkNotNull(r);
    this.builder = new IntegerFormula.Builder<>(r.builder);
  }

  public IntegerFormula<IntegerField> getFormula() {
    return builder.build();
  }

  @Override
  public void addClause(final IntegerClause<IntegerField> clause) {
    builder.addClause(clause);
  }

  @Override
  public IntegerFieldFormulaProblem clone() {
    return new IntegerFieldFormulaProblem(this);
  }
}