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
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * Test for {@link IntegerFormulaSolver}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFormulaSolverTestCase {
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
      final List<IntegerVariable> vars, final IntegerFormula<IntegerVariable> formula) {
    final IntegerFormulaSolver solver = new IntegerFormulaSolver(vars, formula);
    return solver;
  }

  private static IntegerFormulaSolver getSolver(final IntegerFormula<IntegerVariable> formula) {
    return getSolver(VARS, formula);
  }

  private static Map<IntegerVariable, BigInteger> check(
      final String id, final IntegerFormula<IntegerVariable> formula, final boolean expected) {
    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        getSolver(formula).solve(Solver.Mode.MAP);

    System.out.println(id);
    System.out.println(result.getResult());

    Assert.assertTrue((result.getStatus() == SolverResult.Status.SAT) == expected);
    return result.getResult();
  }

  private static Map<IntegerVariable, BigInteger> check(
      final String id,
      final List<IntegerVariable> vars,
      final IntegerFormula<IntegerVariable> formula,
      final boolean expected) {
    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        getSolver(vars, formula).solve(Solver.Mode.MAP);

    System.out.println(id);
    System.out.println(result.getResult());

    Assert.assertTrue((result.getStatus() == SolverResult.Status.SAT) == expected);
    return result.getResult();
  }

  /**
   * a == b && a == c && b != c.
   */
  @Test
  public void runTestA() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(a, b, true);
    formulaBuilder.addEquation(a, c, true);
    formulaBuilder.addEquation(b, c, false);

    check("A: a == b && a == c && b != c", formulaBuilder.build(), false);
  }

  /**
   * a == b && b == c && c == d && d == e && e != a.
   */
  @Test
  public void runTestB() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(a, b, true);
    formulaBuilder.addEquation(b, c, true);
    formulaBuilder.addEquation(c, d, true);
    formulaBuilder.addEquation(d, e, true);
    formulaBuilder.addEquation(e, a, false);

    check("B: a == b && b == c && c == d && d == e && e != a", formulaBuilder.build(), false);
  }

  /**
   * a == b && a != 0 && a != 1 && b != 10. 
   */
  @Test
  public void runTestC() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(a, b, true);
    formulaBuilder.addEquation(a, BigInteger.ZERO, false);
    formulaBuilder.addEquation(a, BigInteger.ONE, false);
    formulaBuilder.addEquation(b, BigInteger.TEN, false);

    check("C: a == b && a != 0 && a != 1 && b != 10", formulaBuilder.build(), true);
  }

  /**
   * a != 0 && ... && a != 14.
   */
  @Test
  public void runTestD() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    for (int i = 0; i < 15; i++) {
      formulaBuilder.addEquation(a, BigInteger.valueOf(i), false);
    }

    check("D: a != 0 && ... && a != 14", formulaBuilder.build(), true);
  }

  /**
   * a != 0 && ... && a != 15.
   */
  @Test
  public void runTestE() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    for (int i = 0; i < 16; i++) {
      formulaBuilder.addEquation(a, BigInteger.valueOf(i), false);
    }

    check("E: a != 0 && ... && a != 15", formulaBuilder.build(), false);
  }

  /**
   * a != b, where dom(a) and dom(b) have only one common value.
   */
  @Test
  public void runTestF() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    for (int i = 15; i >= 8; i--) {
      formulaBuilder.addEquation(a, BigInteger.valueOf(i), false);
    }

    for (int i = 0; i < 8; i++) {
      formulaBuilder.addEquation(b, BigInteger.valueOf(i), false);
    }

    formulaBuilder.addEquation(a, b, false);

    check("F: a != b, where dom(a) and dom(b) have only one common value", formulaBuilder.build(), true);
  }

  /**
   * a != b, where |dom(b)| = 1 and dom(a) and dom(b) are overlapping.
   */
  @Test
  public void runTestG() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    for (int i = 0; i < 16; i++) {
      if (i != 8) {
        formulaBuilder.addEquation(b, BigInteger.valueOf(i), false);
      }
    }

    formulaBuilder.addEquation(a, b, false);

    check("G: a != b, where |dom(b)| = 1 and dom(a) and dom(b) are overlapping", formulaBuilder.build(), false);
  }

  /**
   * z != a && z != b && z != c && z != d &&
   *           a != b && a != c && a != d &&
   *                     b != c && b != d &&
   *                               c != d.
   */
  @Test
  public void runTestH() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(z, a, false);
    formulaBuilder.addEquation(z, b, false);
    formulaBuilder.addEquation(z, c, false);
    formulaBuilder.addEquation(z, d, false);

    formulaBuilder.addEquation(a, b, false);
    formulaBuilder.addEquation(a, c, false);
    formulaBuilder.addEquation(a, d, false);

    formulaBuilder.addEquation(b, c, false);
    formulaBuilder.addEquation(b, d, false);

    formulaBuilder.addEquation(c, d, false);

    check("H: a, b, c, d and z are mutually different", formulaBuilder.build(), false);
  }

  /**
   * a == b && (b == c || b == d) && (a != c || a != d).
   */
  @Test
  public void runTestI() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(a, b, true);

    final IntegerClause.Builder<IntegerVariable> clauseBuilder1 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder1.addEquation(b, c, true);
    clauseBuilder1.addEquation(b, d, true);
    formulaBuilder.addClause(clauseBuilder1.build());

    final IntegerClause.Builder<IntegerVariable> clauseBuilder2 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder2.addEquation(a, c, false);
    clauseBuilder2.addEquation(a, d, false);
    formulaBuilder.addClause(clauseBuilder2.build());

    check("I: a == b && (b == c || b == d) && (a != c || a != d)", formulaBuilder.build(), true);
  }

  /**
   * a == b && (b == c || b == d) && (a != c && a != d).
   */
  @Test
  public void runTestJ() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(a, b, true);

    final IntegerClause.Builder<IntegerVariable> clauseBuilder1 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder1.addEquation(b, c, true);
    clauseBuilder1.addEquation(b, d, true);
    formulaBuilder.addClause(clauseBuilder1.build());

    final IntegerClause.Builder<IntegerVariable> clauseBuilder2 =
        new IntegerClause.Builder<>(IntegerClause.Type.AND);

    clauseBuilder2.addEquation(a, c, false);
    clauseBuilder2.addEquation(a, d, false);
    formulaBuilder.addClause(clauseBuilder2.build());

    check("J: a == b && (b == c || b == d) && (a != c && a != d)", formulaBuilder.build(), false);
  }

  /**
   * a == b && (b == c || b == d) && (c == d || c == e) && (d == e || d == f) && (a != e && a != f).
   */
  @Test
  public void runTestK() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(a, b, true);

    final IntegerClause.Builder<IntegerVariable> clauseBuilder1 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder1.addEquation(b, c, true);
    clauseBuilder1.addEquation(b, d, true);
    formulaBuilder.addClause(clauseBuilder1.build());

    final IntegerClause.Builder<IntegerVariable> clauseBuilder2 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder2.addEquation(c, d, true);
    clauseBuilder2.addEquation(c, e, true);
    formulaBuilder.addClause(clauseBuilder2.build());

    final IntegerClause.Builder<IntegerVariable> clauseBuilder3 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder3.addEquation(d, e, true);
    clauseBuilder3.addEquation(d, f, true);
    formulaBuilder.addClause(clauseBuilder3.build());

    final IntegerClause.Builder<IntegerVariable> clauseBuilder4 =
        new IntegerClause.Builder<>(IntegerClause.Type.OR);

    clauseBuilder4.addEquation(a, e, false);
    clauseBuilder4.addEquation(a, f, false);
    formulaBuilder.addClause(clauseBuilder4.build());

    check("K: a == b && (b == c || b == d) && (c == d || c == e) && (d == e || d == f) && (a != e && a != f)",
        formulaBuilder.build(), false);
  }

  /**
   * OutOfMemoryError test.
   */
  @Test
  public void runTestN() {
    final List<IntegerVariable> vars = new ArrayList<>();
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

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

    for (int i = 1; i < numberOfDisjunctions; i++) {
      final IntegerClause.Builder<IntegerVariable> clauseBuilder =
          new IntegerClause.Builder<>(IntegerClause.Type.OR);

      for (int j = 0; j < numberOfEqualitiesInDisjuntion; j++) {
        clauseBuilder.addEquation(arrayVariable[i][j], arrayVariable[i - 1][j], true);
      }

      formulaBuilder.addClause(clauseBuilder.build());
    }

    check("N: OutOfMemoryError", vars, formulaBuilder.build(), true);
  }

  /**
   * a == b && b == c && c == d && d == 10 => a == 10.
   */
  @Test
  public void runTestO() {
    final IntegerFormula.Builder<IntegerVariable> formulaBuilder = new IntegerFormula.Builder<>();

    formulaBuilder.addEquation(a, b, true);
    formulaBuilder.addEquation(b, c, true);
    formulaBuilder.addEquation(c, d, true);
    formulaBuilder.addEquation(d, BigInteger.TEN, true);

    final Map<IntegerVariable, BigInteger> solution =
        check("O: a == b && b == c && c == d && d == 10", formulaBuilder.build(), true);

    for (final Map.Entry<IntegerVariable, BigInteger> entry : solution.entrySet()) {
      final IntegerVariable variable = entry.getKey();
      final BigInteger value = entry.getValue();

      if (variable == a || variable == b || variable == c || variable == d) {
        Assert.assertTrue(value.equals(BigInteger.TEN));
      }
    }
  }
}
