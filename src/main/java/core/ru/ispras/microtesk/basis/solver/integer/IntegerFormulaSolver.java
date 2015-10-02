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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;

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
public final class IntegerFormulaSolver implements Solver<Map<IntegerVariable, BigInteger>> {
  /** Variables used in the formula. */
  private final Collection<Collection<IntegerVariable>> variables;
  /** Formula (constraint) to be solved. */
  private final Collection<IntegerFormula<IntegerVariable>> formulae;

  /**
   * Constructs a solver.
   * 
   * @param variables the collection of variables.
   * @param formulae the constraints to be solved.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public IntegerFormulaSolver(
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
   * @throws IllegalArgumentException if some parameters are null.
   */
  public IntegerFormulaSolver(
      final Collection<IntegerVariable> variables,
      final IntegerFormula<IntegerVariable> formula) {
    this(Collections.singleton(variables), Collections.singleton(formula));
  }

  @Override
  public SolverResult<Map<IntegerVariable, BigInteger>> solve(final Mode mode) {
    InvariantChecks.checkNotNull(mode);

    final IntegerClause.Builder<IntegerVariable> kernelBuilder =
        new IntegerClause.Builder<>(IntegerClause.Type.AND);

    final Collection<IntegerClause<IntegerVariable>> clauses = new ArrayList<>();

    // Construct the formulae kernel (the common AND clause).
    for (final IntegerFormula<IntegerVariable> formula : formulae) {
      for (final IntegerClause<IntegerVariable> clause : formula.getClauses()) {
        if (clause.size() == 0) {
          if (clause.getType() == IntegerClause.Type.OR) {
            return new SolverResult<>("Empty OR clause");
          }
        } else if (clause.getType() == IntegerClause.Type.AND || clause.size() == 1) {
          kernelBuilder.addClause(clause);
        } else {
          clauses.add(clause);
        }
      }
    }

    final IntegerClause<IntegerVariable> kernel = kernelBuilder.build();

    final IntegerClauseSolver kernelSolver = new IntegerClauseSolver(variables, kernel);
    final SolverResult<Map<IntegerVariable, BigInteger>> kernelResult = kernelSolver.solve(mode);

    if (clauses.isEmpty() || kernelResult.getStatus() == SolverResult.Status.UNSAT) {
      return kernelResult;
    }

    // Simplify the remaining OR clauses (if possible).
    final List<IntegerClause<IntegerVariable>> simplifiedClauses = new ArrayList<>();

    for (final IntegerClause<IntegerVariable> clause : clauses) {
      final List<IntegerEquation<IntegerVariable>> equations = new ArrayList<>();

      boolean isFalse = true;
      for (final IntegerEquation<IntegerVariable> equation : clause.getEquations()) {
        if (!kernel.contradictsTo(equation)) {
          if (kernel.strongerThan(equation)) {
            // The clause is redundant: the implication (kernel => clause) holds.
            equations.clear();
            isFalse = false;
            break;
          }

          equations.add(equation);
          isFalse = false;
        }
      }

      if (isFalse) {
        return new SolverResult<>("OR clause contradicts to the kernel");
      }

      if (!equations.isEmpty()) {
        simplifiedClauses.add(new IntegerClause<IntegerVariable>(IntegerClause.Type.OR, equations));
      }
    }

    if (simplifiedClauses.isEmpty()) {
      return kernelResult;
    }

    // Get rid of the redundant OR clauses.
    final List<IntegerClause<IntegerVariable>> redundantClauses = new ArrayList<>();

    for (int i = 0; i < simplifiedClauses.size() - 1; i++) {
      final IntegerClause<IntegerVariable> clause1 = simplifiedClauses.get(i);

      for (int j = i + 1; j < simplifiedClauses.size(); j++) {
        final IntegerClause<IntegerVariable> clause2 = simplifiedClauses.get(j);

        if (clause1.strongerThan(clause2)) {
          redundantClauses.add(clause2);
        } else if (clause2.strongerThan(clause1)) {
          redundantClauses.add(clause1);
          break;
        }
      }
    }

    simplifiedClauses.removeAll(redundantClauses);

    // Initialize the product iterator over variants.
    final ProductIterator<IntegerEquation<IntegerVariable>> variantIterator =
        new ProductIterator<>();

    for (int i = 0; i < simplifiedClauses.size(); i++) {
      final Collection<IntegerEquation<IntegerVariable>> equations =
          simplifiedClauses.get(i).getEquations();

      variantIterator.registerIterator(new CollectionIterator<>(equations));
    }

    for (variantIterator.init(); variantIterator.hasValue(); variantIterator.next()) {
      final IntegerClause.Builder<IntegerVariable> variantBuilder =
          new IntegerClause.Builder<>(IntegerClause.Type.AND);

      for (int i = 0; i < simplifiedClauses.size(); i++) {
        variantBuilder.addEquation(variantIterator.value(i));
      }

      // If the variant is relatively small, check it locally.
      if (variantBuilder.size() < kernel.size() / 3 /* Heuristic */) {
        final IntegerClause<IntegerVariable> localClause = variantBuilder.build();

        final Collection<Collection<IntegerVariable>> localVariables =
            Collections.singleton(localClause.getVariables());

        final IntegerClauseSolver checker = new IntegerClauseSolver(localVariables, localClause);

        if (checker.solve(Mode.SAT).getStatus() != SolverResult.Status.SAT) {
          continue;
        }
      }

      variantBuilder.addClause(kernel);

      final IntegerClause<IntegerVariable> globalVariant = variantBuilder.build();

      final IntegerClauseSolver globalSolver = new IntegerClauseSolver(variables, globalVariant);
      final SolverResult<Map<IntegerVariable, BigInteger>> globalResult = globalSolver.solve(mode);

      if (globalResult.getStatus() == SolverResult.Status.SAT) {
        return globalResult;
      }
    }

    return new SolverResult<>("SAT variant not found");
  }
}
