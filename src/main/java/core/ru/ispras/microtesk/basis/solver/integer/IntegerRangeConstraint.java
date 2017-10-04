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
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * {@link IntegerRangeConstraint} class represents a range constraint.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerRangeConstraint implements IntegerConstraint {
  private static final String NEW_VARIABLE_PREFIX = "new$";
  private static int newVariableId = 0;

  private final Variable variable;
  private final IntegerRange range;

  private final Node formula;

  public IntegerRangeConstraint(
      final Variable variable,
      final IntegerRange range) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(range);

    this.variable = variable;
    this.range = range;

    final List<Node> formulaBuilder = new ArrayList<>();
    encodeGreaterThanOrEqualTo(formulaBuilder, variable, range.getMin());
    encodeLessThanOrEqualTo(formulaBuilder, variable, range.getMax());

    this.formula = FortressUtils.makeNodeAnd(formulaBuilder);
  }

  private static void encodeGreaterThanOrEqualTo(
      final List<Node> formulaBuilder,
      final Variable x,
      final BigInteger a) {
    // Represent x >= a.
    encodeInequality(formulaBuilder, x, a, true);
  }

  private static void encodeLessThanOrEqualTo(
      final List<Node> formulaBuilder,
      final Variable x,
      final BigInteger b) {
    // Represent x <= b.
    encodeInequality(formulaBuilder, x, b, false);
  }

  private static void encodeInequality(
      final List<Node> formulaBuilder,
      final Variable x,
      final BigInteger a,
      final boolean greaterThanOrEqualTo) {

    int lowerBit = 0;
    while (lowerBit < x.getType().getSize() && greaterThanOrEqualTo != a.testBit(lowerBit)) {
      lowerBit++;
    }

    int upperBit = x.getType().getSize() - 1;
    while (upperBit >= 0 && greaterThanOrEqualTo == a.testBit(upperBit)) {
      upperBit--;
    }

    if (upperBit + 1 < x.getType().getSize()) {
      final BigInteger value = greaterThanOrEqualTo
          ? BitUtils.getBigIntegerMask((x.getType().getSize() - upperBit) - 1)
          : BigInteger.ZERO;

      formulaBuilder.add(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(x, upperBit + 1, x.getType().getSize() - 1),
              FortressUtils.makeNodeValueInteger(value)));
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
    final Variable e = new Variable(
        String.format("%s%d", NEW_VARIABLE_PREFIX, newVariableId++),
        DataType.BIT_VECTOR(numberOfBits + 1));

    // (e[0] | ... | e[n-1]) == (e != 0).
    formulaBuilder.add(
        FortressUtils.makeNodeNotEqual(
            FortressUtils.makeNodeExtract(e, 0, e.getType().getSize() - 1),
            FortressUtils.makeNodeInteger(0)));

    // u[0] == (x[upper] = a[upper]).
    // e[0] <=> u[0] == (~u[0] | e[0]) & (u[0] | ~e[0]).
    //                     clause 1         clause 2
    final List<Node> clauseBuilder1 = new ArrayList<>();

    clauseBuilder1.add(
        FortressUtils.makeNodeNotEqual(
            FortressUtils.makeNodeExtract(x, lowerBit, upperBit),
            FortressUtils.makeNodeValueInteger(BitUtils.getField(a, lowerBit, upperBit))));

    clauseBuilder1.add(
        FortressUtils.makeNodeEqual(
            FortressUtils.makeNodeExtract(e, 0, 0),
            FortressUtils.makeNodeInteger(1)));

    formulaBuilder.add(FortressUtils.makeNodeOr(clauseBuilder1));

    final List<Node> clauseBuilder2 = new ArrayList<>();

    clauseBuilder2.add(
        FortressUtils.makeNodeEqual(
            FortressUtils.makeNodeExtract(x, lowerBit, upperBit),
            FortressUtils.makeNodeValueInteger(BitUtils.getField(a, lowerBit, upperBit))));

    clauseBuilder2.add(
        FortressUtils.makeNodeEqual(
            FortressUtils.makeNodeExtract(e, 0, 0),
            FortressUtils.makeNodeInteger(0)));

    formulaBuilder.add(FortressUtils.makeNodeOr(clauseBuilder2));

    int k = 1;
    for (int i = upperBit; i >= lowerBit; i--) {
      if (greaterThanOrEqualTo == a.testBit(i)) {
        continue;
      }

      // u[k] == (x[upper] = a[upper]).
      // v[k] == (x[next] = 1 (for >=) or 0 (for <=)).

      // e[k] <=> u[k] & v[k] == (~u[k] | ~v[k] | e[k]) & (u[k] | ~e[k]) & (v[k] | ~e[k]).
      //                                 clause 3            clause 4         clause 5
      final List<Node> clauseBuilder3 = new ArrayList<>();

      if (i < upperBit) {
        final int j = i + 1;

        clauseBuilder3.add(
            FortressUtils.makeNodeNotEqual(
                FortressUtils.makeNodeExtract(x, j, upperBit),
                FortressUtils.makeNodeValueInteger(BitUtils.getField(a, j, upperBit))));

        final List<Node> clauseBuilder4 = new ArrayList<>();

        clauseBuilder4.add(
            FortressUtils.makeNodeEqual(
                FortressUtils.makeNodeExtract(x, j, upperBit),
                FortressUtils.makeNodeValueInteger(BitUtils.getField(a, j, upperBit))));

        clauseBuilder4.add(
            FortressUtils.makeNodeEqual(
                FortressUtils.makeNodeExtract(e, k, k),
                FortressUtils.makeNodeInteger(0)));

        formulaBuilder.add(FortressUtils.makeNodeOr(clauseBuilder4));
      }

      clauseBuilder3.add(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(x, i, i),
              FortressUtils.makeNodeInteger(greaterThanOrEqualTo ? 0 : 1)));

      clauseBuilder3.add(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(e, k, k),
              FortressUtils.makeNodeInteger(1)));

      formulaBuilder.add(FortressUtils.makeNodeOr(clauseBuilder3));

      final List<Node> clauseBuilder5 = new ArrayList<>();

      clauseBuilder5.add(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(x, i, i),
              FortressUtils.makeNodeInteger(greaterThanOrEqualTo ? 1 : 0)));

      clauseBuilder5.add(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(e, k, k),
              FortressUtils.makeNodeInteger(0)));

      formulaBuilder.add(FortressUtils.makeNodeOr(clauseBuilder5));

      k++;
    }
  }

  public Variable getVariable() {
    return variable;
  }

  public IntegerRange getRange() {
    return range;
  }

  @Override
  public Node getFormula() {
    return formula;
  }

  @Override
  public String toString() {
    return formula.toString();
  }
}
