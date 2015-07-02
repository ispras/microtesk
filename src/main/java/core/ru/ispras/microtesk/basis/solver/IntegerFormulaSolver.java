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

package ru.ispras.microtesk.basis.solver;

import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerFormulaSolver} implements a simple constraint solver.
 * 
 * <p>The solver supports equalities and inequalities of variables and constants
 * {@code (x == y, x != y, x == c, x != c)}. A constraint has the following structure:
 * {@code (e[1,1] || ... || e[1,n(1)]) && ... && (e[m,1] || ... || e[m,n(m)])}.
 * It is assumed that the number of non-trivial disjunctions is rather small (i.e., the most of
 * {@code n(i)} are equal to one).</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFormulaSolver implements Solver<Boolean> {
  /** Equation formula (constraint) to be solved. */
  private final IntegerFormula formula;
  /** Variables used in the clause. */
  private final Collection<IntegerVariable> variables;

  /**
   * Constructs a solver.
   * 
   * @param variables the collection of variables.
   * @param formula the constraint to be solved.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public IntegerFormulaSolver(
      final Collection<IntegerVariable> variables, final IntegerFormula formula) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(formula);

    this.variables = variables;
    this.formula = formula;
  }

  /**
   * Checks whether the equation formula is satisfiable.
   * 
   * @return {@code true} if the equation formula is satisfiable; {@code false} otherwise.
   */
  public SolverResult<Boolean> solve() {
    final IntegerClause kernel = new IntegerClause(IntegerClause.Type.AND);
    final IntegerFormula simplifiedFormula = new IntegerFormula();

    int numberOfVariants = 1;

    for (final IntegerClause clause : formula.getEquationClauses()) {
      if (clause.size() == 0) {
        return new SolverResult<>("UNSAT");
      }

      if (clause.size() == 1) {
        kernel.addEquationClause(clause);
      } else {
        simplifiedFormula.addEquationClause(clause);
        numberOfVariants *= clause.size();
      }
    }

    final IntegerClauseSolver kernelSolver =
        new IntegerClauseSolver(variables, kernel);

    final SolverResult<Boolean> kernelResult = kernelSolver.solve();

    if (numberOfVariants == 1 || kernelResult.getStatus() == SolverResult.Status.UNSAT) {
      return kernelResult;
    }

    final List<IntegerClause> clauses = simplifiedFormula.getEquationClauses();

    int equationChooser = numberOfVariants;

    for (int variantId = 0; variantId < numberOfVariants; variantId++) {
      final IntegerClause variant = new IntegerClause(kernel);

      for (int clauseIndex = 0; clauseIndex < clauses.size(); clauseIndex++) {
        final List<IntegerEquation> equations = clauses.get(clauseIndex).getEquations();
        final IntegerEquation equation = equations.get(equationChooser % equations.size());

        variant.addEquation(equation);
        equationChooser /= equations.size();
      }

      final IntegerClauseSolver variantSolver =
          new IntegerClauseSolver(variables, variant);

      if (variantSolver.solve().getStatus() == SolverResult.Status.SAT) {
        return new SolverResult<>(true);
      }
    }

    return new SolverResult<>("UNSAT");
  }
}
