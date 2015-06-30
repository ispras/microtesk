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

package ru.ispras.microtesk.translator.mmu.ir.spec.basis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.test.sequence.solver.IntegerClause;
import ru.ispras.microtesk.test.sequence.solver.IntegerFormula;
import ru.ispras.microtesk.test.sequence.solver.IntegerFormulaSolver;
import ru.ispras.microtesk.test.sequence.solver.IntegerVariable;
import ru.ispras.microtesk.test.sequence.solver.SolverResult;

/**
 * Test for {@link IntegerFormulaSolver}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IntegerFormulaSolverTestCase {
  private final static IntegerVariable a = new IntegerVariable("a", 4);
  private final static IntegerVariable b = new IntegerVariable("b", 4);
  private final static IntegerVariable c = new IntegerVariable("c", 4);
  private final static IntegerVariable d = new IntegerVariable("d", 4);
  private final static IntegerVariable e = new IntegerVariable("e", 4);
  private final static IntegerVariable f = new IntegerVariable("f", 4);
  private final static IntegerVariable z = new IntegerVariable("z", 2);

  private final static List<IntegerVariable> VARS = new ArrayList<>();
  static {
    VARS.add(a);
    VARS.add(b);
    VARS.add(c);
    VARS.add(d);
    VARS.add(e);
    VARS.add(f);
    VARS.add(z);
  }

  private static IntegerFormulaSolver getSolver(
      final List<IntegerVariable> vars, final IntegerFormula formula) {
    final IntegerFormulaSolver solver = new IntegerFormulaSolver(vars, formula);
    return solver;
  }

  private static IntegerFormulaSolver getSolver(final IntegerFormula formula) {
    return getSolver(VARS, formula);
  }

  /**
   * a == b && a == c && b != c.
   */
  @Test
  public void runTestA() {
    final IntegerFormula formula = new IntegerFormula();

    formula.addEquation(a, b, true);
    formula.addEquation(a, c, true);
    formula.addEquation(b, c, false);

    Assert.assertFalse(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a == b && b == c && c == d && d == e && e != a 
   */
  @Test
  public void runTestB() {
    final IntegerFormula formula = new IntegerFormula();

    formula.addEquation(a, b, true);
    formula.addEquation(b, c, true);
    formula.addEquation(c, d, true);
    formula.addEquation(d, e, true);
    formula.addEquation(e, a, false);

    Assert.assertFalse(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a == b && a != 0 && a != 1 && b != 10. 
   */
  @Test
  public void runTestC() {
    final IntegerFormula formula = new IntegerFormula();

    formula.addEquation(a, b, true);
    formula.addEquation(a, BigInteger.ZERO, false);
    formula.addEquation(a, BigInteger.ONE, false);
    formula.addEquation(b, BigInteger.TEN, false);

    Assert.assertTrue(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a != 0 && ... && a != 14.
   */
  @Test
  public void runTestD() {
    final IntegerFormula formula = new IntegerFormula();

    for (int i = 0; i < 15; i++) {
      formula.addEquation(a, BigInteger.valueOf(i), false);
    }

    Assert.assertTrue(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a != 0 && ... && a != 15.
   */
  @Test
  public void runTestE() {
    final IntegerFormula formula = new IntegerFormula();

    for (int i = 0; i < 16; i++) {
      formula.addEquation(a, BigInteger.valueOf(i), false);
    }

    Assert.assertFalse(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a != b, where dom(a) and dom(b) have only one common value.
   */
  @Test
  public void runTestF() {
    final IntegerFormula formula = new IntegerFormula();

    for (int i = 15; i >= 8; i--) {
      formula.addEquation(a, BigInteger.valueOf(i), false);
    }

    for (int i = 0; i < 8; i++) {
      formula.addEquation(b, BigInteger.valueOf(i), false);
    }

    formula.addEquation(a, b, false);

    Assert.assertTrue(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a != b, where |dom(b)| = 1 and dom(a) and dom(b) are overlapping.
   */
  @Test
  public void runTestG() {
    final IntegerFormula formula = new IntegerFormula();

    for (int i = 0; i < 16; i++) {
      if (i != 8) {
        formula.addEquation(b, BigInteger.valueOf(i), false);
      }
    }

    formula.addEquation(a, b, false);

    Assert.assertFalse(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * z != a && z != b && z != c && z != d &&
   *           a != b && a != c && a != d &&
   *                     b != c && b != d &&
   *                               c != d.
   */
  @Test
  public void runTestH() {
    final IntegerFormula formula = new IntegerFormula();

    formula.addEquation(z, a, false);
    formula.addEquation(z, b, false);
    formula.addEquation(z, c, false);
    formula.addEquation(z, d, false);

    formula.addEquation(a, b, false);
    formula.addEquation(a, c, false);
    formula.addEquation(a, d, false);

    formula.addEquation(b, c, false);
    formula.addEquation(b, d, false);

    formula.addEquation(c, d, false);

    Assert.assertFalse(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a == b && (b == c || b == d) && (a != c || a != d).
   */
  @Test
  public void runTestI() {
    final IntegerFormula formula = new IntegerFormula();

    formula.addEquation(a, b, true);

    final IntegerClause set1 = new IntegerClause(IntegerClause.Type.OR);
    set1.addEquation(b, c, true);
    set1.addEquation(b, d, true);
    formula.addEquationClause(set1);

    final IntegerClause set2 = new IntegerClause(IntegerClause.Type.OR);
    set2.addEquation(a, c, false);
    set2.addEquation(a, d, false);
    formula.addEquationClause(set2);

    Assert.assertTrue(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a == b && (b == c || b == d) && (a != c && a != d).
   */
  @Test
  public void runTestJ() {
    final IntegerFormula formula = new IntegerFormula();

    formula.addEquation(a, b, true);

    final IntegerClause set1 = new IntegerClause(IntegerClause.Type.OR);
    set1.addEquation(b, c, true);
    set1.addEquation(b, d, true);
    formula.addEquationClause(set1);

    final IntegerClause set2 = new IntegerClause(IntegerClause.Type.AND);
    set2.addEquation(a, c, false);
    set2.addEquation(a, d, false);
    formula.addEquationClause(set2);

    Assert.assertFalse(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * a == b && (b == c || b == d) && (c == d || c == e) && (d == e || d == f) && (a != e && a != f).
   */
  @Test
  public void runTestK() {
    final IntegerFormula formula = new IntegerFormula();

    formula.addEquation(a, b, true);

    final IntegerClause set1 = new IntegerClause(IntegerClause.Type.OR);
    set1.addEquation(b, c, true);
    set1.addEquation(b, d, true);
    formula.addEquationClause(set1);

    final IntegerClause set2 = new IntegerClause(IntegerClause.Type.OR);
    set2.addEquation(c, d, true);
    set2.addEquation(c, e, true);
    formula.addEquationClause(set2);

    final IntegerClause set3 = new IntegerClause(IntegerClause.Type.OR);
    set3.addEquation(d, e, true);
    set3.addEquation(d, f, true);
    formula.addEquationClause(set3);

    final IntegerClause set4 = new IntegerClause(IntegerClause.Type.AND);
    set4.addEquation(a, e, false);
    set4.addEquation(a, f, false);
    formula.addEquationClause(set4);

    Assert.assertFalse(getSolver(formula).solve().getStatus() == SolverResult.Status.SAT);
  }

  /**
   * OutOfMemoryError test.
   */
  @Test
  public void runTestN() {
    final List<IntegerVariable> vars = new ArrayList<>();
    final IntegerFormula formula = new IntegerFormula();

    final int numberOfDisjunctions = 10;
    final int numberOfEqualitiesInDisjuntion = 5; 

    final IntegerVariable[][] arrayVariable =
        new IntegerVariable[numberOfDisjunctions][numberOfEqualitiesInDisjuntion];

    for (int i = 0; i < numberOfDisjunctions; i++) {
      for (int j = 0; j < numberOfEqualitiesInDisjuntion; j++) {
        arrayVariable[i][j] = new IntegerVariable("a_" + i + "_"+ j, 4);
        vars.add(arrayVariable[i][j]);
      }
    }

    final IntegerClause[] equationSet = new IntegerClause[numberOfDisjunctions];

    for (int i = 1; i < numberOfDisjunctions; i++) {
      equationSet[i] = new IntegerClause(IntegerClause.Type.OR);

      for (int j = 0; j < numberOfEqualitiesInDisjuntion; j++) {
        equationSet[i].addEquation(arrayVariable[i][j], arrayVariable[i - 1][j], true);
      }

      formula.addEquationClause(equationSet[i]);
    }

    Assert.assertTrue(getSolver(vars, formula).solve().getStatus() == SolverResult.Status.SAT);
  }
}
