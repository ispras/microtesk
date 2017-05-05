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

    final IntegerFormula.Builder<IntegerField> formulaBuilder = new IntegerFormula.Builder<>();
    encodeGreaterThanOrEqualTo(formulaBuilder, variable, range.getMin());
    encodeLessThanOrEqualTo(formulaBuilder, variable, range.getMax());

    this.formula = formulaBuilder.build();
  }

  private static void encodeGreaterThanOrEqualTo(
      final IntegerFormula.Builder<IntegerField> formulaBuilder,
      final IntegerVariable x,
      final BigInteger a) {
    // Represent x >= a.
    encodeInequality(formulaBuilder, x, a, true);
  }

  private static void encodeLessThanOrEqualTo(
      final IntegerFormula.Builder<IntegerField> formulaBuilder,
      final IntegerVariable x,
      final BigInteger b) {
    // Represent x <= b.
    encodeInequality(formulaBuilder, x, b, false);
  }

  private static void encodeInequality(
      final IntegerFormula.Builder<IntegerField> formulaBuilder,
      final IntegerVariable x,
      final BigInteger a,
      final boolean greaterThanOrEqualTo) {

    int lowerBit = 0;
    while (lowerBit < x.getWidth() && greaterThanOrEqualTo != a.testBit(lowerBit)) {
      lowerBit++;
    }

    int upperBit = x.getWidth() - 1;
    while (upperBit >= 0 && greaterThanOrEqualTo == a.testBit(upperBit)) {
      upperBit--;
    }

    if (upperBit + 1 < x.getWidth()) {
      final BigInteger value = greaterThanOrEqualTo
          ? BitUtils.getBigIntegerMask((x.getWidth() - upperBit) - 1)
          : BigInteger.ZERO;

      formulaBuilder.addEquation(x.field(upperBit + 1, x.getWidth() - 1), value, true);
    }

    if (upperBit <= lowerBit) {
      return;
    }

    int numberOfBits = 0;
    for (int i = lowerBit; i <= upperBit; i++) {
      if (greaterThanOrEqualTo != a.testBit(i)) {
        numberOfBits++;
      }
    }

    // Introduce a new variable to encode OR.
    final IntegerVariable e = new IntegerVariable(
        String.format("%s%d", NEW_VARIABLE_PREFIX, newVariableId++), numberOfBits + 1);

    // (e[0] | ... | e[n-1]) == (e != 0).
    formulaBuilder.addEquation(e.field(0, e.getWidth() - 1), BigInteger.ZERO, false);

    // u[0] == (x[upper] = a[upper]).
    // e[0] <=> u[0] == (~u[0] | e[0]) & (u[0] | ~e[0]).
    //                     clause 1         clause 2
    final IntegerClause.Builder<IntegerField> clauseBuilder1 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder1.addEquation(
        x.field(lowerBit, upperBit), BitUtils.getField(a, lowerBit, upperBit), false);
    clauseBuilder1.addEquation(e.field(0, 0), BigInteger.ONE, true);
    formulaBuilder.addClause(clauseBuilder1.build());

    final IntegerClause.Builder<IntegerField> clauseBuilder2 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder2.addEquation(
        x.field(lowerBit, upperBit), BitUtils.getField(a, lowerBit, upperBit), true);
    clauseBuilder2.addEquation(e.field(0, 0), BigInteger.ONE, false);
    formulaBuilder.addClause(clauseBuilder2.build());

    int k = 1;
    for (int i = upperBit; i >= lowerBit; i--) {
      if (greaterThanOrEqualTo == a.testBit(i)) {
        continue;
      }

      // u[k] == (x[upper] = a[upper]).
      // v[k] == (x[next] = 1 (for >=) or 0 (for <=)).

      // e[k] <=> u[k] & v[k] == (~u[k] | ~v[k] | e[k]) & (u[k] | ~e[k]) & (v[k] | ~e[k]).
      //                                 clause 3            clause 4         clause 5
      final IntegerClause.Builder<IntegerField> clauseBuilder3 =
          new IntegerClause.Builder<>(IntegerClause.Type.OR);

      if (i < upperBit) {
        final int j = i + 1;

        clauseBuilder3.addEquation(
            x.field(j, upperBit), BitUtils.getField(a, j, upperBit), false);

        final IntegerClause.Builder<IntegerField> clauseBuilder4 =
            new IntegerClause.Builder<>(IntegerClause.Type.OR);

        clauseBuilder4.addEquation(
            x.field(j, upperBit), BitUtils.getField(a, j, upperBit), true);
        clauseBuilder4.addEquation(e.field(k, k), BigInteger.ONE, false);
        formulaBuilder.addClause(clauseBuilder4.build());
      }

      clauseBuilder3.addEquation(x.field(i, i), BigInteger.ONE, !greaterThanOrEqualTo);
      clauseBuilder3.addEquation(e.field(k, k), BigInteger.ONE, true);
      formulaBuilder.addClause(clauseBuilder3.build());

      final IntegerClause.Builder<IntegerField> clauseBuilder5 =
          new IntegerClause.Builder<>(IntegerClause.Type.OR);

      clauseBuilder5.addEquation(x.field(i, i), BigInteger.ONE, greaterThanOrEqualTo);
      clauseBuilder5.addEquation(e.field(k, k), BigInteger.ONE, false);
      formulaBuilder.addClause(clauseBuilder5.build());

      k++;
    }
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
