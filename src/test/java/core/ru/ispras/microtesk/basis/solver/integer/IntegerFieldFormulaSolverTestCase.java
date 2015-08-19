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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * Test for {@link IntegerFieldFormulaSolver}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFieldFormulaSolverTestCase {
  public static final IntegerVariable va = new IntegerVariable("VA", 64);
  public static final IntegerVariable pa = new IntegerVariable("PA", 36);
  public static final IntegerVariable vpn2 = new IntegerVariable("VPN2", 27);
  public static final IntegerVariable v0 = new IntegerVariable("V0", 1);
  public static final IntegerVariable d0 = new IntegerVariable("D0", 1);
  public static final IntegerVariable g0 = new IntegerVariable("G0", 1);
  public static final IntegerVariable c0 = new IntegerVariable("C0", 3);
  public static final IntegerVariable pfn0 = new IntegerVariable("PFN0", 24);
  public static final IntegerVariable v1 = new IntegerVariable("V1", 1);
  public static final IntegerVariable d1 = new IntegerVariable("D1", 1);
  public static final IntegerVariable g1 = new IntegerVariable("G1", 1);
  public static final IntegerVariable c1 = new IntegerVariable("C1", 3);
  public static final IntegerVariable pfn1 = new IntegerVariable("PFN1", 24);
  public static final IntegerVariable v = new IntegerVariable("V", 1);
  public static final IntegerVariable d = new IntegerVariable("D", 1);
  public static final IntegerVariable g = new IntegerVariable("G", 1);
  public static final IntegerVariable c = new IntegerVariable("C", 3);
  public static final IntegerVariable pfn = new IntegerVariable("PFN", 24);
  public static final IntegerVariable l1Tag = new IntegerVariable("TAG1", 24);
  public static final IntegerVariable l2Tag = new IntegerVariable("TAG2", 19);
  public static final IntegerVariable l1Data = new IntegerVariable("DATA1", 8 * 32);
  public static final IntegerVariable l2Data = new IntegerVariable("DATA2", 8 * 32);
  public static final IntegerVariable data = new IntegerVariable("DATA", 8 * 32);

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

    variables.add(va);
    variables.add(pa);
    variables.add(vpn2);
    variables.add(v0);
    variables.add(d0);
    variables.add(g0);
    variables.add(c0);
    variables.add(pfn0);
    variables.add(v1);
    variables.add(d1);
    variables.add(g1);
    variables.add(c1);
    variables.add(pfn1);
    variables.add(v);
    variables.add(d);
    variables.add(g);
    variables.add(c);
    variables.add(pfn);
    variables.add(l1Tag);
    variables.add(l2Tag);
    variables.add(l1Data);
    variables.add(l2Data);
    variables.add(data);

    final IntegerFormula<IntegerField> formula = new IntegerFormula<>();

    formula.addEquation(new IntegerField(vpn2, 0, 26), new IntegerField(va, 13, 39), true);
    formula.addEquation(new IntegerField(va, 12), BigInteger.ZERO, true);
    formula.addEquation(new IntegerField(d), new IntegerField(d0), true);
    formula.addEquation(new IntegerField(v), new IntegerField(v0), true);
    formula.addEquation(new IntegerField(pfn, 0, 4), new IntegerField(pfn0, 0, 4), true);
    formula.addEquation(new IntegerField(pfn, 5, 23), new IntegerField(pfn0, 5, 23), true);
    formula.addEquation(new IntegerField(c, 0, 1), new IntegerField(c0, 0, 1), true);
    formula.addEquation(new IntegerField(c, 2), new IntegerField(c0, 2), true);
    formula.addEquation(new IntegerField(g, 0), BigInteger.ONE, true);
    formula.addEquation(new IntegerField(v, 0), BigInteger.ONE, true);
    formula.addEquation(new IntegerField(d, 0), BigInteger.ONE, true);
    formula.addEquation(new IntegerField(pa, 0, 11), new IntegerField(va, 0, 11), true);
    formula.addEquation(new IntegerField(pa, 12, 16), new IntegerField(pfn, 0, 4), true);
    formula.addEquation(new IntegerField(pa, 17, 35), new IntegerField(pfn, 5, 23), true);
    formula.addEquation(new IntegerField(c, 0, 1), BigInteger.valueOf(0x2), false);
    formula.addEquation(new IntegerField(l1Tag, 0, 4), new IntegerField(pa, 12, 16), true);
    formula.addEquation(new IntegerField(l1Tag, 5, 23), new IntegerField(pa, 17, 35), true);
    formula.addEquation(new IntegerField(c, 0, 1), BigInteger.ZERO, false);
    formula.addEquation(new IntegerField(c, 0, 1), BigInteger.ONE, false);
    formula.addEquation(new IntegerField(l2Tag, 0, 18), new IntegerField(pa, 17, 35), true);
    formula.addEquation(new IntegerField(pa, 0, 11), BigInteger.valueOf(0x148), true);
    formula.addEquation(new IntegerField(pa, 12, 16), BigInteger.valueOf(0xe), true);
    formula.addEquation(new IntegerField(pa, 17, 35), BigInteger.valueOf(0x16), true);
    formula.addEquation(new IntegerField(va, 0, 11), BigInteger.valueOf(0x148), true);
    formula.addEquation(new IntegerField(va, 12, 12), BigInteger.ZERO, true);
    formula.addEquation(new IntegerField(va, 13, 39), BigInteger.valueOf(0x40032), true);
    formula.addEquation(new IntegerField(va, 40, 63), BigInteger.ZERO, true);

    final IntegerFieldFormulaSolver solver = new IntegerFieldFormulaSolver(variables, formula);
    final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve();
    final Map<IntegerVariable, BigInteger> values = result.getResult();

    System.out.println(values);

    final BigInteger paValue = values.get(pa);
    Assert.assertTrue(paValue.longValue() == 0x2ce148);
  }
}
