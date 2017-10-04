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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * Test for {@link IntegerRangeConstraint}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerRangeConstraintTestCase {
  private void runTest(final Variable x, final BigInteger a, final BigInteger b) {
    final IntegerRange range = new IntegerRange(a, b);
    System.out.format("Range: %s\n", range);

    final IntegerRangeConstraint constraint = new IntegerRangeConstraint(x, range);
    System.out.format("Formula: %s\n", constraint);

    final IntegerFormulaSolverSat4j solver = new IntegerFormulaSolverSat4j(
        constraint.getFormula(), VariableInitializer.RANDOM);

    final SolverResult<Map<Variable, BigInteger>> result = solver.solve(Solver.Mode.MAP);
    Assert.assertTrue(result.getErrors().toString(),
        result.getStatus() == SolverResult.Status.SAT);

    final Map<Variable, BigInteger> values = result.getResult();
    Assert.assertTrue(values != null);

    final BigInteger value = values.get(x);
    System.out.format("Value: %s\n", value);

    Assert.assertTrue(range.contains(value));
  }

  @Test
  public void runTest1() {
    final Variable x = new Variable("x", DataType.BIT_VECTOR(64));

    runTest(x, BigInteger.valueOf(0x00000L), BigInteger.valueOf(0x0ffffL));
    runTest(x, BigInteger.valueOf(0x10000L), BigInteger.valueOf(0x1ffffL));
  }

  @Test
  public void runTest2() {
    final int N = 1000;
    final Variable x = new Variable("x", DataType.BIT_VECTOR(64));

    for (int i = 0; i < N; i++) {
      runTest(x, BigInteger.valueOf(i), BigInteger.valueOf(0xffff0000L + 2*i));
    }
  }
}
