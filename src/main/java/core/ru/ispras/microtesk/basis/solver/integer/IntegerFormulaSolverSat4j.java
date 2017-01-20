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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * {@link IntegerFormulaSolverSat4j} implements a bit-blasting-based constraint solver.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFormulaSolverSat4j implements Solver<Map<IntegerVariable, BigInteger>> {
  /** Variables used in the formulae. */
  private final Collection<Collection<IntegerVariable>> variables;
  /** Formulae (constraints) to be solved. */
  private final Collection<IntegerFormula<IntegerVariable>> formulae;

  /**
   * Constructs a solver.
   * 
   * @param variables the collection of variables.
   * @param formulae the constraints to be solved.
   */
  public IntegerFormulaSolverSat4j(
      final Collection<Collection<IntegerVariable>> variables,
      final Collection<IntegerFormula<IntegerVariable>> formulae) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(formulae);

    this.variables = Collections.unmodifiableCollection(variables);
    this.formulae = Collections.unmodifiableCollection(formulae);
  }

  /**
   * Constructs a solver.
   * 
   * @param variables the collection of variables.
   * @param formula the constraint to be solved.
   */
  public IntegerFormulaSolverSat4j(
      final Collection<IntegerVariable> variables,
      final IntegerFormula<IntegerVariable> formula) {
    this(Collections.singleton(variables), Collections.singleton(formula));
  }

  @Override
  public SolverResult<Map<IntegerVariable, BigInteger>> solve(final Mode mode) {
    InvariantChecks.checkNotNull(mode);

    // Enumerate the variables.
    final Map<IntegerVariable, Integer> indices = new LinkedHashMap<>();

    // The initial value is one, because zero is not a valid variable identifier.
    int index = 1;

    // If a variable x is mapped to n, it means that x[i] is mapped to n + i.
    for (final Collection<IntegerVariable> collection : variables) {
      for (final IntegerVariable variable : collection) {
        if (!indices.containsKey(variable)) {
          indices.put(variable, index);
          index += variable.getWidth();
        }
      }
    }

    // Create a SAT solver instance (it also represents a constraint to be solved).
    final ISolver solver = Sat4jUtils.getSolver();

    // Perform bit blasting.
    try {
      // Handle constants.
      for (final Map.Entry<IntegerVariable, Integer> entry : indices.entrySet()) {
        final IntegerVariable variable = entry.getKey();

        if (variable.isDefined()) {
          final int x = entry.getValue();
          final BigInteger c = variable.getValue();

          // Generate n clauses (c[i] ? x[i] : ~x[i]).
          solver.addAllClauses(Sat4jUtils.encodeVarEqualConst(variable, x, c));
        }
      }

      // Handle formulae.
      for (final IntegerFormula<IntegerVariable> formula : formulae) {
        for (final IntegerClause<IntegerVariable> clause : formula.getClauses()) {
          if (clause.getType() == IntegerClause.Type.AND || clause.size() == 1) {
            // Handle an AND-clause.
            for (final IntegerEquation<IntegerVariable> equation : clause.getEquations()) {
              final int n = equation.lhs.getWidth();
              final int x = indices.get(equation.lhs);
              final int y = equation.rhs != null ? indices.get(equation.rhs) : -1;

              if (equation.equal) {
                if (equation.val != null) {
                  // Equality x == c.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarEqualConst(equation.lhs, x, equation.val));
                } else {
                  // Equality x == y.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarEqualVar(equation.lhs, x, equation.rhs, y));
                }
              } else {
                if (equation.val != null) {
                  // Inequality x != c.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarNotEqualConst(equation.lhs, x, equation.val));
                } else {
                  // Inequality x != y.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarNotEqualVar(equation.lhs, x, equation.rhs, y, index));

                  index += 2 * n;
                }
              }
            } // for equation.
          } else {
            // Handle an OR-clause.
            int ej = index;

            solver.addClause(Sat4jUtils.createClause(index, clause.size()));
            index += clause.size();

            for (final IntegerEquation<IntegerVariable> equation : clause.getEquations()) {
              final int n = equation.lhs.getWidth();
              final int x = indices.get(equation.lhs);
              final int y = equation.rhs != null ? indices.get(equation.rhs) : -1;

              if (equation.equal) {
                if (equation.val != null) {
                  // Equality x == c.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarEqualConst(ej, equation.lhs, x, equation.val));
                } else {
                  // Equality x == y.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarEqualVar(ej, equation.lhs, x, equation.rhs, y, index));

                  index += 2 * n;
                }
              } else {
                if (equation.val != null) {
                  // Inequality x != c.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarNotEqualConst(ej, equation.lhs, x, equation.val));
                } else {
                  // Inequality x != y.
                  solver.addAllClauses(
                      Sat4jUtils.encodeVarNotEqualVar(ej, equation.lhs, x, equation.rhs, y, index));

                  index += 2 * n;
                }
              }

              ej++;
            } // for equation.
          } // if clause type.
        } // for clause.
      } // for formula.
    } catch (final ContradictionException e) {
      return new SolverResult<>(
          SolverResult.Status.UNSAT,
          Collections.<IntegerVariable, BigInteger>emptyMap(),
          Collections.<String>singletonList(String.format("Contradiction: %s", e.getMessage())));
    }

    final IProblem problem = solver;

    try {
      if (!problem.isSatisfiable()) {
        return new SolverResult<>("UNSAT");
      }
    } catch (final TimeoutException e) {
      return new SolverResult<>(
          SolverResult.Status.UNSAT,
          Collections.<IntegerVariable, BigInteger>emptyMap(),
          Collections.<String>singletonList(String.format("Timeout: %s", e.getMessage())));
    }

    if (mode == Solver.Mode.SAT) {
      return new SolverResult<>(Collections.<IntegerVariable, BigInteger>emptyMap());
    }

    // Assign the variables with values.
    final Map<IntegerVariable, BigInteger> solution = Sat4jUtils.decodeSolution(problem, indices);

    return new SolverResult<>(solution);
  }
}
