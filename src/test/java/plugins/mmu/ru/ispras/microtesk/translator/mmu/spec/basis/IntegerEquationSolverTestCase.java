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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link IntegerEquationSolver}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IntegerEquationSolverTestCase {
  private final static IntegerVariable a = new IntegerVariable("a", 4);
  private final static IntegerVariable b = new IntegerVariable("b", 4);
  private final static IntegerVariable c = new IntegerVariable("c", 4);
  private final static IntegerVariable d = new IntegerVariable("d", 4);
  private final static IntegerVariable e = new IntegerVariable("e", 4);
  private final static IntegerVariable f = new IntegerVariable("f", 4);
  private final static IntegerVariable z = new IntegerVariable("z", 2);

  private static IntegerEquationSolver getSolver() {
    final IntegerEquationSolver solver = new IntegerEquationSolver();

    solver.addVariable(a);
    solver.addVariable(b);
    solver.addVariable(c);
    solver.addVariable(d);
    solver.addVariable(e);
    solver.addVariable(f);
    solver.addVariable(z);

    return solver;
  }

  /**
   * a == b && a == c && b != c.
   */
  @Test
  public void runTestA() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(a, b, true);
    solver.addEquation(a, c, true);
    solver.addEquation(b, c, false);

    Assert.assertFalse(solver.solve());
  }

  /**
   * a == b && b == c && c == d && d == e && e != a 
   */
  @Test
  public void runTestB() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(a, b, true);
    solver.addEquation(b, c, true);
    solver.addEquation(c, d, true);
    solver.addEquation(d, e, true);
    solver.addEquation(e, a, false);

    Assert.assertFalse(solver.solve());
  }

  /**
   * a == b && a != 0 && a != 1 && b != 10. 
   */
  @Test
  public void runTestC() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(a, b, true);
    solver.addEquation(a, BigInteger.ZERO, false);
    solver.addEquation(a, BigInteger.ONE, false);
    solver.addEquation(b, BigInteger.TEN, false);

    Assert.assertTrue(solver.solve());
  }

  /**
   * a != 0 && ... && a != 14.
   */
  @Test
  public void runTestD() {
    final IntegerEquationSolver solver = getSolver();

    for (int i = 0; i < 15; i++) {
      solver.addEquation(a, BigInteger.valueOf(i), false);
    }

    Assert.assertTrue(solver.solve());
  }

  /**
   * a != 0 && ... && a != 15.
   */
  @Test
  public void runTestE() {
    final IntegerEquationSolver solver = getSolver();

    for (int i = 0; i < 16; i++) {
      solver.addEquation(a, BigInteger.valueOf(i), false);
    }

    Assert.assertFalse(solver.solve());
  }

  /**
   * a != b, where dom(a) and dom(b) have only one common value.
   */
  @Test
  public void runTestF() {
    final IntegerEquationSolver solver = getSolver();

    for (int i = 15; i >= 8; i--) {
      solver.addEquation(a, BigInteger.valueOf(i), false);
    }

    for (int i = 0; i < 8; i++) {
      solver.addEquation(b, BigInteger.valueOf(i), false);
    }

    solver.addEquation(a, b, false);

    Assert.assertTrue(solver.solve());
  }

  /**
   * a != b, where |dom(b)| = 1 and dom(a) and dom(b) are overlapping.
   */
  @Test
  public void runTestG() {
    final IntegerEquationSolver solver = getSolver();

    for (int i = 0; i < 16; i++) {
      if (i != 8) {
        solver.addEquation(b, BigInteger.valueOf(i), false);
      }
    }

    solver.addEquation(a, b, false);

    Assert.assertFalse(solver.solve());
  }

  /**
   * z != a && z != b && z != c && z != d &&
   *           a != b && a != c && a != d &&
   *                     b != c && b != d &&
   *                               c != d.
   */
  @Test
  public void runTestH() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(z, a, false);
    solver.addEquation(z, b, false);
    solver.addEquation(z, c, false);
    solver.addEquation(z, d, false);

    solver.addEquation(a, b, false);
    solver.addEquation(a, c, false);
    solver.addEquation(a, d, false);

    solver.addEquation(b, c, false);
    solver.addEquation(b, d, false);

    solver.addEquation(c, d, false);

    Assert.assertFalse(solver.solve());
  }

  /**
   * a == b && (b == c || b == d) && (a != c || a != d).
   */
  @Test
  public void runTestI() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(a, b, true);

    final IntegerEquationSet set1 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set1.addEquation(b, c, true);
    set1.addEquation(b, d, true);
    solver.addEquationSet(set1);

    final IntegerEquationSet set2 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set2.addEquation(a, c, false);
    set2.addEquation(a, d, false);
    solver.addEquationSet(set2);

    Assert.assertTrue(solver.solve());
  }

  /**
   * a == b && (b == c || b == d) && (a != c && a != d).
   */
  @Test
  public void runTestJ() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(a, b, true);

    final IntegerEquationSet set1 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set1.addEquation(b, c, true);
    set1.addEquation(b, d, true);
    solver.addEquationSet(set1);

    final IntegerEquationSet set2 = new IntegerEquationSet(IntegerEquationSet.Type.AND);
    set2.addEquation(a, c, false);
    set2.addEquation(a, d, false);
    solver.addEquationSet(set2);

    Assert.assertFalse(solver.solve());
  }

  /**
   * a == b && (b == c || b == d) && (c == d || c == e) && (d == e || d == f) && (a != e && a != f).
   */
  @Test
  public void runTestK() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(a, b, true);

    final IntegerEquationSet set1 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set1.addEquation(b, c, true);
    set1.addEquation(b, d, true);
    solver.addEquationSet(set1);

    final IntegerEquationSet set2 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set2.addEquation(c, d, true);
    set2.addEquation(c, e, true);
    solver.addEquationSet(set2);

    final IntegerEquationSet set3 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set3.addEquation(d, e, true);
    set3.addEquation(d, f, true);
    solver.addEquationSet(set3);

    final IntegerEquationSet set4 = new IntegerEquationSet(IntegerEquationSet.Type.AND);
    set4.addEquation(a, e, false);
    set4.addEquation(a, f, false);
    solver.addEquationSet(set4);

    Assert.assertFalse(solver.solve());
  }

  /**
   * a == b && (b == c || b == d) && (c == d || q == e).
   */
  @Test
  public void runTestL() {
    final IntegerEquationSolver solver = getSolver();

    solver.addEquation(a, b, true);

    final IntegerEquationSet set1 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set1.addEquation(b, c, true);
    set1.addEquation(b, d, true);
    solver.addEquationSet(set1);

    final IntegerVariable q = new IntegerVariable("q", 4);
    solver.addVariable(q);

    final IntegerEquationSet set2 = new IntegerEquationSet(IntegerEquationSet.Type.OR);
    set2.addEquation(c, d, true);
    set2.addEquation(q, e, true);
    solver.addEquationSet(set2);

    Assert.assertTrue(solver.solve());
  }
}
