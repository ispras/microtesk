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

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerRangeConstraint} class represents a range constraint.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerRangeConstraint implements IntegerConstraint<IntegerField> {
  private static final String NEW_VARIABLE_PREFIX = "new$";
  private static int newVariableId = 0;

  private final IntegerVariable variable;
  private final IntegerRange range;

  private final IntegerFormula<IntegerField> formula;

  public IntegerRangeConstraint(
      final IntegerVariable variable,
      final IntegerRange range) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(range);

    this.variable = variable;
    this.range = range;

    final IntegerVariable x = variable;
    //final BigInteger a = range.getMin();
    final BigInteger b = range.getMax();

    // Represents the constraint a <= x <= b.
    final IntegerFormula.Builder<IntegerField> formulaBuilder = new IntegerFormula.Builder<>();

    // The lower bound: x >= a.
    // TODO:

    // The upper bound: x <= b.
    int lower = 0;
    while (lower < x.getWidth() && b.testBit(lower)) {
      lower++;
    }

    int upper = x.getWidth() - 1;
    while (upper >= 0 && !b.testBit(upper)) {
      upper--;
    }

    if (upper + 1 < x.getWidth()) {
      formulaBuilder.addEquation(x.field(upper + 1, x.getWidth() - 1), BigInteger.ZERO, true);
    }

    if (upper > lower) {
      final int n = (upper - lower) + 1;

      // Introduce a new variable to encode OR.
      final IntegerVariable e = new IntegerVariable(
          String.format("%s%d", NEW_VARIABLE_PREFIX, newVariableId++), n);

      // (e[0] | ... | e[n-1]) == (e != 0).
      formulaBuilder.addEquation(e.field(0, e.getWidth() - 1), BigInteger.ZERO, false);

      for (int i = lower; i <= upper; i++) {
        if (b.testBit(i)) {
          final int k = i - lower;

          // e[k] <=> u[k] & v[k] == (~u[k] | ~v[k] | e[k]) & (u[k] | ~e[k]) & (v[k] | ~e[k]).
          //                                 clause 1            clause 2         clause 3
          final IntegerClause.Builder<IntegerField> clauseBuilder1 =
              new IntegerClause.Builder<>(IntegerClause.Type.OR);

          final IntegerClause.Builder<IntegerField> clauseBuilder2 =
              new IntegerClause.Builder<>(IntegerClause.Type.OR);

          clauseBuilder1.addEquation(x.field(i, upper), BitUtils.getField(b, i, upper), false);

          clauseBuilder2.addEquation(x.field(i, upper), BitUtils.getField(b, i, upper), true);
          clauseBuilder2.addEquation(e.field(k, k), BigInteger.ONE, false);
          formulaBuilder.addClause(clauseBuilder2.build());

          if (i > lower) {
            final IntegerClause.Builder<IntegerField> clauseBuilder3 =
                new IntegerClause.Builder<>(IntegerClause.Type.OR);

            clauseBuilder1.addEquation(x.field(i - 1, i - 1), BigInteger.ZERO, false);

            clauseBuilder3.addEquation(x.field(i - 1, i - 1), BigInteger.ZERO, true);
            clauseBuilder3.addEquation(e.field(k, k), BigInteger.ONE, false);
            formulaBuilder.addClause(clauseBuilder3.build());
          }

          clauseBuilder1.addEquation(e.field(k, k), BigInteger.ONE, true);
          formulaBuilder.addClause(clauseBuilder1.build());
        }
      }
    }

    this.formula = null;
  }

  public IntegerVariable getVariable() {
    return variable;
  }

  public IntegerRange getRange() {
    return range;
  }

  @Override
  public IntegerFormula<IntegerField> getFormula() {
    return formula;
  }

  @Override
  public String toString() {
    return formula.toString();
  }
}
