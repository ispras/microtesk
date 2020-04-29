/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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

import java.util.LinkedHashMap;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * {@link BitVectorFormulaSolverSat4j} is a SAT-based bit-vector constraint solver.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BitVectorFormulaSolverSat4j implements Solver<Map<Variable, BitVector>> {
  /** Problem to be solved. */
  private final BitVectorFormulaProblemSat4j problem;

  /** Initializer used to fill the unused fields of the variables. */
  private final BitVectorVariableInitializer initializer;

  /**
   * Constructs a solver.
   *
   * @param builder the builder of the problem to be solved.
   * @param initializer the initializer to be used to fill the unused fields.
   */
  public BitVectorFormulaSolverSat4j(
      final BitVectorFormulaBuilder builder,
      final BitVectorVariableInitializer initializer) {
    InvariantChecks.checkNotNull(builder);
    InvariantChecks.checkTrue(builder instanceof BitVectorFormulaProblemSat4j);
    InvariantChecks.checkNotNull(initializer);

    this.problem = (BitVectorFormulaProblemSat4j) builder;
    this.initializer = initializer;
  }

  /**
   * Constructs a solver.
   *
   * @param formulae the constraints to be solved.
   * @param initializer the initializer to be used to fill the unused fields.
   */
  public BitVectorFormulaSolverSat4j(
      final Collection<Node> formulae,
      final BitVectorVariableInitializer initializer) {
    InvariantChecks.checkNotNull(formulae);
    InvariantChecks.checkNotNull(initializer);

    this.problem = new BitVectorFormulaProblemSat4j();
    for (final Node formula : formulae) {
      problem.addFormula(formula);
    }

    this.initializer = initializer;
  }

  /**
   * Constructs a solver.
   *
   * @param formula the constraint to be solved.
   * @param initializer the initializer to be used to fill the unused fields.
   */
  public BitVectorFormulaSolverSat4j(
      final Node formula,
      final BitVectorVariableInitializer initializer) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(initializer);

    this.problem = new BitVectorFormulaProblemSat4j();
    problem.addFormula(formula);

    this.initializer = initializer;
  }

  @Override
  public SolverResult<Map<Variable, BitVector>> solve(final Mode mode) {
    InvariantChecks.checkNotNull(mode);

    final ISolver solver = SolverFactory.newDefault();
    final Sat4jFormula formula = problem.getFormula();

    // Construct the problem.
    try {
      for (final IVecInt clause : formula.getClauses()) {
        solver.addClause(clause);
      }
    } catch (final ContradictionException e) {
      return new SolverResult<>(
          SolverResult.Status.UNSAT,
          Collections.<Variable, BitVector>emptyMap(),
          Collections.<String>singletonList(
              String.format("Contradiction: %s", e.getMessage())));
    }

    // Solve the problem.
    try {
      if (!solver.isSatisfiable()) {
        return new SolverResult<>("UNSAT");
      }
    } catch (final TimeoutException e) {
      return new SolverResult<>(
          SolverResult.Status.UNSAT,
          Collections.<Variable, BitVector>emptyMap(),
          Collections.<String>singletonList(
              String.format("Timeout: %s", e.getMessage())));
    }

    if (mode == Solver.Mode.SAT) {
      return new SolverResult<>(Collections.<Variable, BitVector>emptyMap());
    }

    // Assign the variables with values.
    final Map<Variable, BitVector> solution = decode(solver, problem.getIndices());

    // Track unused fields of the variables.
    final Map<Variable, BitVector> masks = problem.getMasks();

    // Initialize unused fields of the variables.
    for (final Map.Entry<Variable, BitVector> entry : solution.entrySet()) {
      final Variable variable = entry.getKey();
      final BitVector mask = masks.get(variable);

      BitVector value = entry.getValue();

      int lowUnusedFieldIndex = -1;

      for (int i = 0; i < mask.getBitSize(); i++) {
        if (!mask.getBit(i)) {
          if (lowUnusedFieldIndex == -1) {
            lowUnusedFieldIndex = i;
          }
        } else {
          if (lowUnusedFieldIndex != -1) {
            final BitVector fieldValue = initializer.getValue(i - lowUnusedFieldIndex);

            value.field(lowUnusedFieldIndex, i - 1).assign(fieldValue);
            lowUnusedFieldIndex = -1;
          }
        }
      }

      if (lowUnusedFieldIndex != -1) {
        final BitVector fieldValue = initializer.getValue(mask.getBitSize() - lowUnusedFieldIndex);
        value.field(lowUnusedFieldIndex, mask.getBitSize() - 1).assign(fieldValue);
      }

      solution.put(variable, value);
    }

    return new SolverResult<>(solution);
  }

  private Map<Variable, BitVector> decode(
      final IProblem problem,
      final Map<Variable, Integer> indices) {
    final Map<Variable, BitVector> solution = new LinkedHashMap<>();

    for (final Map.Entry<Variable, Integer> entry : indices.entrySet()) {
      final Variable variable = entry.getKey();
      final int x = entry.getValue();

      final BitVector value = BitVector.newEmpty(variable.getType().getSize());
      for (int i = 0; i < variable.getType().getSize(); i++) {
        final int xi = x + i;
        value.setBit(i, problem.model(xi));
      }

      solution.put(variable, value);
    }

    return solution;
  }
}
