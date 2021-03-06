/*
 * Copyright 2017-2021 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.fortress.expression.Node;

import ru.ispras.fortress.solver.SolverResult;
import ru.ispras.fortress.solver.SolverResult.Status;
import ru.ispras.fortress.solver.constraint.Sat4jFormulaEncoder;
import ru.ispras.fortress.solver.engine.sat.Sat4jSolver;
import ru.ispras.microtesk.basis.solver.Restriction;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link Restriction}.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class RestrictionTestCase {
  private void runTest(final Variable x, final BitVector a, final BitVector b) {
    System.out.format("Range: [%s, %s]\n", a.toHexString(), b.toHexString());

    final Node constraint = Restriction.range(x, a, b);

    final Sat4jSolver solver = new Sat4jSolver();
    final Sat4jFormulaEncoder encoder = new Sat4jFormulaEncoder();
    encoder.addNode(constraint);

    final SolverResult result = solver.solve(encoder.encode());
    Assert.assertTrue(result.getErrors().toString(),
        result.getStatus() == Status.SAT);

    final List<Variable> values = result.getVariables();
    Assert.assertTrue(values != null);

    final BitVector value = values.iterator().next().getData().getBitVector();
    System.out.format("Value: %s\n", value);

    Assert.assertTrue(BitVectorMath.ule(a, value) && BitVectorMath.ule(value, b));
  }

  @Test
  public void runTest1() {
    final int bitSize = 64;
    final Variable x = new Variable("x", DataType.bitVector(bitSize));

    runTest(x, BitVector.valueOf(0x00000L, bitSize), BitVector.valueOf(0x0ffffL, bitSize));
    runTest(x, BitVector.valueOf(0x10000L, bitSize), BitVector.valueOf(0x1ffffL, bitSize));
  }

  @Test
  public void runTest2() {
    final int N = 1000;
    final int bitSize = 64;
    final Variable x = new Variable("x", DataType.bitVector(bitSize));

    for (int i = 0; i < N; i++) {
      runTest(x, BitVector.valueOf(i, bitSize), BitVector.valueOf(0xffff0000L + 2 * i, bitSize));
    }
  }
}
