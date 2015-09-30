/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * Test for {@link IntegerFieldFormulaSolver}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFieldFormulaSolverTestCase {
 /**
  * There was a bug when solving the following constraint:
  * 
  * <pre>{@code
  *   VPN2[0, 26] == VA[13, 39]
  *   VA[12] == 0
  *   D[0] == D0[0]
  *   V[0] == V0[0]
  *   PFN[0, 4] == PFN0[0, 4]
  *   PFN[5, 23] == PFN0[5, 23]
  *   C[0, 1] == C0[0, 1]
  *   C[2] == C0[2]
  *   G[0] == 1
  *   V[0] == 1
  *   D[0] == 1
  *   PA[0, 11] == VA[0, 11]
  *   PA[12, 16] == PFN[0, 4]
  *   PA[17, 35] == PFN[5, 23]
  *   C[0, 1] != 2
  *   TAG1[0, 4] == PA[12, 16]
  *   TAG1[5, 23] == PA[17, 35]
  *   C[0, 1] != 0
  *   C[0, 1] != 1
  *   TAG2[0, 18] == PA[17, 35]
  *   PA[0, 11] == 148
  *   PA[12, 16] == e
  *   PA[17, 35] == 16
  *   VA[0, 11] == 148
  *   VA[12, 12] == 0
  *   VA[13, 39] == 40032
  *   VA[40, 63] == 0
  * }</pre>
  */
  @Test
  public void runTest() {
    final Collection<IntegerVariable> variables = new ArrayList<>();

    final IntegerFormula<IntegerField> formula = new IntegerFormula<>();

    final int K = 10;

    final IntegerVariable[] va = new IntegerVariable[K];
    final IntegerVariable[] pa = new IntegerVariable[K];
    final IntegerVariable[] vpn2 = new IntegerVariable[K];
    final IntegerVariable[] v0 = new IntegerVariable[K];
    final IntegerVariable[] d0 = new IntegerVariable[K];
    final IntegerVariable[] g0 = new IntegerVariable[K];
    final IntegerVariable[] c0 = new IntegerVariable[K];
    final IntegerVariable[] pfn0 = new IntegerVariable[K];
    final IntegerVariable[] v1 = new IntegerVariable[K];
    final IntegerVariable[] d1 = new IntegerVariable[K];
    final IntegerVariable[] g1 = new IntegerVariable[K];
    final IntegerVariable[] c1 = new IntegerVariable[K];
    final IntegerVariable[] pfn1 = new IntegerVariable[K];
    final IntegerVariable[] v = new IntegerVariable[K];
    final IntegerVariable[] d = new IntegerVariable[K];
    final IntegerVariable[] g = new IntegerVariable[K];
    final IntegerVariable[] c = new IntegerVariable[K];
    final IntegerVariable[] pfn = new IntegerVariable[K];
    final IntegerVariable[] l1Tag = new IntegerVariable[K];
    final IntegerVariable[] l2Tag = new IntegerVariable[K];
    final IntegerVariable[] l1Data = new IntegerVariable[K];
    final IntegerVariable[] l2Data = new IntegerVariable[K];
    final IntegerVariable[] data = new IntegerVariable[K];

    for (int i = 0; i < K; i++) {
      va[i] = new IntegerVariable("VA" + i, 64);
      pa[i] = new IntegerVariable("PA" + i, 36);
      vpn2[i] = new IntegerVariable("VPN2" + i, 27);
      v0[i] = new IntegerVariable("V0" + i, 1);
      d0[i] = new IntegerVariable("D0" + i, 1);
      g0[i] = new IntegerVariable("G0" + i, 1);
      c0[i] = new IntegerVariable("C0" + i, 3);
      pfn0[i] = new IntegerVariable("PFN0" + i, 24);
      v1[i] = new IntegerVariable("V1" + i, 1);
      d1[i] = new IntegerVariable("D1" + i, 1);
      g1[i] = new IntegerVariable("G1" + i, 1);
      c1[i] = new IntegerVariable("C1" + i, 3);
      pfn1[i] = new IntegerVariable("PFN1" + i, 24);
      v[i] = new IntegerVariable("V" + i, 1);
      d[i] = new IntegerVariable("D" + i, 1);
      g[i] = new IntegerVariable("G" + i, 1);
      c[i] = new IntegerVariable("C" + i, 3);
      pfn[i] = new IntegerVariable("PFN" + i, 24);
      l1Tag[i] = new IntegerVariable("TAG1" + i, 24);
      l2Tag[i] = new IntegerVariable("TAG2" + i, 19);
      l1Data[i] = new IntegerVariable("DATA1" + i, 8 * 32);
      l2Data[i] = new IntegerVariable("DATA2" + i, 8 * 32);
      data[i] = new IntegerVariable("DATA" + i, 8 * 32);

      variables.add(va[i]);
      variables.add(pa[i]);
      variables.add(vpn2[i]);
      variables.add(v0[i]);
      variables.add(d0[i]);
      variables.add(g0[i]);
      variables.add(c0[i]);
      variables.add(pfn0[i]);
      variables.add(v1[i]);
      variables.add(d1[i]);
      variables.add(g1[i]);
      variables.add(c1[i]);
      variables.add(pfn1[i]);
      variables.add(v[i]);
      variables.add(d[i]);
      variables.add(g[i]);
      variables.add(c[i]);
      variables.add(pfn[i]);
      variables.add(l1Tag[i]);
      variables.add(l2Tag[i]);
      variables.add(l1Data[i]);
      variables.add(l2Data[i]);
      variables.add(data[i]);

      formula.addEquation(new IntegerField(vpn2[i], 0, 26), new IntegerField(va[i], 13, 39), true);
      formula.addEquation(new IntegerField(va[i], 12), BigInteger.ZERO, true);
      formula.addEquation(new IntegerField(d[i]), new IntegerField(d0[i]), true);
      formula.addEquation(new IntegerField(v[i]), new IntegerField(v0[i]), true);
      formula.addEquation(new IntegerField(pfn[i], 0, 4), new IntegerField(pfn0[i], 0, 4), true);
      formula.addEquation(new IntegerField(pfn[i], 5, 23), new IntegerField(pfn0[i], 5, 23), true);
      formula.addEquation(new IntegerField(c[i], 0, 1), new IntegerField(c0[i], 0, 1), true);
      formula.addEquation(new IntegerField(c[i], 2), new IntegerField(c0[i], 2), true);
      formula.addEquation(new IntegerField(g[i], 0), BigInteger.ONE, true);
      formula.addEquation(new IntegerField(v[i], 0), BigInteger.ONE, true);
      formula.addEquation(new IntegerField(d[i], 0), BigInteger.ONE, true);
      formula.addEquation(new IntegerField(pa[i], 0, 11), new IntegerField(va[i], 0, 11), true);
      formula.addEquation(new IntegerField(pa[i], 12, 16), new IntegerField(pfn[i], 0, 4), true);
      formula.addEquation(new IntegerField(pa[i], 17, 35), new IntegerField(pfn[i], 5, 23), true);
      formula.addEquation(new IntegerField(c[i], 0, 1), BigInteger.valueOf(0x2), false);
      formula.addEquation(new IntegerField(l1Tag[i], 0, 4), new IntegerField(pa[i], 12, 16), true);
      formula.addEquation(new IntegerField(l1Tag[i], 5, 23), new IntegerField(pa[i], 17, 35), true);
      formula.addEquation(new IntegerField(c[i], 0, 1), BigInteger.ZERO, false);
      formula.addEquation(new IntegerField(c[i], 0, 1), BigInteger.ONE, false);
      formula.addEquation(new IntegerField(l2Tag[i], 0, 18), new IntegerField(pa[i], 17, 35), true);
      formula.addEquation(new IntegerField(pa[i], 0, 11), BigInteger.valueOf(0x148), true);
      formula.addEquation(new IntegerField(pa[i], 12, 16), BigInteger.valueOf(0xe), true);
      formula.addEquation(new IntegerField(pa[i], 17, 35), BigInteger.valueOf(0x16), true);
      formula.addEquation(new IntegerField(va[i], 0, 11), BigInteger.valueOf(0x148), true);
      formula.addEquation(new IntegerField(va[i], 12, 12), BigInteger.ZERO, true);
      formula.addEquation(new IntegerField(va[i], 13, 39), BigInteger.valueOf(0x40032), true);
      formula.addEquation(new IntegerField(va[i], 40, 63), BigInteger.ZERO, true);

      // To increase complexity.
      final IntegerClause<IntegerField> clause = new IntegerClause<>(IntegerClause.Type.OR);
      for (int k = 0; k < 16; k++) {
        clause.addEquation(new IntegerField(pa[i], 0, 3), BigInteger.valueOf(k), true);
      }
      formula.addClause(clause);

      // Links between copies.
      if (i > 0) {
        final int j = i - 1;

        formula.addEquation(new IntegerField(va[i]), new IntegerField(va[j]), true);
        formula.addEquation(new IntegerField(pa[i]), new IntegerField(pa[j]), true);
      }
    }

    System.out.println(formula);

    final int N = 1000;
    final Date startTime = new Date();

    for (int i = 0; i < N; i++) {
      final IntegerFieldFormulaSolver solver =
          new IntegerFieldFormulaSolver(variables, formula, IntegerVariableInitializer.RANDOM);

      final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve(Solver.Mode.MAP);
      Assert.assertTrue(result.getErrors().toString(),
          result.getStatus() == SolverResult.Status.SAT);

      final Map<IntegerVariable, BigInteger> values = result.getResult();
      Assert.assertTrue(values != null);

      final BigInteger paValue = values.get(pa[i % K]);
      Assert.assertTrue(paValue.longValue() == 0x2ce148);
    }

    final Date endTime = new Date();

    System.out.format("Solving time: %d milliseconds%n", (endTime.getTime() - startTime.getTime()));
  }
}
