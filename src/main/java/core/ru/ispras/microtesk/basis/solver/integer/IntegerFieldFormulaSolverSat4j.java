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
import java.util.Map;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * {@link IntegerFieldFormulaSolverSat4j} implements an integer-field-constraints solver.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFieldFormulaSolverSat4j implements Solver<Map<IntegerVariable, BigInteger>> {
  /** Problem to be solved. */
  private final IntegerFieldFormulaProblemSat4j problem;

  /** Initializer used to fill the unused fields of the variables. */
  private final IntegerVariableInitializer initializer;

  /**
   * Constructs a solver.
   * 
   * @param problem the problem to be solved.
   * @param initializer the initializer to be used to fill the unused fields. 
   */
  public IntegerFieldFormulaSolverSat4j(
    final IntegerFormulaBuilder<IntegerField> builder,
    final IntegerVariableInitializer initializer) {
    InvariantChecks.checkNotNull(builder);
    InvariantChecks.checkTrue(builder instanceof IntegerFieldFormulaProblemSat4j);
    InvariantChecks.checkNotNull(initializer);

    this.problem = (IntegerFieldFormulaProblemSat4j) builder;
    this.initializer = initializer;
  }

  /**
   * Constructs a solver.
   * 
   * @param formulae the constraints to be solved.
   * @param initializer the initializer to be used to fill the unused fields. 
   */
  public IntegerFieldFormulaSolverSat4j(
    final Collection<IntegerFormula<IntegerField>> formulae,
    final IntegerVariableInitializer initializer) {
    InvariantChecks.checkNotNull(formulae);
    InvariantChecks.checkNotNull(initializer);

    this.problem = new IntegerFieldFormulaProblemSat4j();
    for (final IntegerFormula<IntegerField> formula : formulae) {
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
  public IntegerFieldFormulaSolverSat4j(
    final IntegerFormula<IntegerField> formula,
    final IntegerVariableInitializer initializer) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(initializer);

    this.problem = new IntegerFieldFormulaProblemSat4j();
    problem.addFormula(formula);

    this.initializer = initializer;
  }

  @Override
  public SolverResult<Map<IntegerVariable, BigInteger>> solve(final Mode mode) {
    InvariantChecks.checkNotNull(mode);

    final ISolver solver = Sat4jUtils.getSolver();
    final Sat4jFormula formula = problem.getFormula();

    // Construct the problem.
    try {
      for (final IVec<IVecInt> clauses : formula.getClauses()) {
        solver.addAllClauses(clauses);
      }
    } catch (final ContradictionException e) {
      return new SolverResult<>(
          SolverResult.Status.UNSAT,
          Collections.<IntegerVariable, BigInteger>emptyMap(),
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
          Collections.<IntegerVariable, BigInteger>emptyMap(),
          Collections.<String>singletonList(
              String.format("Timeout: %s", e.getMessage())));
    }

    if (mode == Solver.Mode.SAT) {
      return new SolverResult<>(Collections.<IntegerVariable, BigInteger>emptyMap());
    }

    // Assign the variables with values.
    final Map<IntegerVariable, BigInteger> solution =
        Sat4jUtils.decodeSolution(solver, problem.getIndices());

    // Track unused fields of the variables.
    final Map<IntegerVariable, BitVector> masks = problem.getMasks();

    // Initialize unused fields of the variables.
    for (final Map.Entry<IntegerVariable, BigInteger> entry : solution.entrySet()) {
      final IntegerVariable variable = entry.getKey();
      final BitVector mask = masks.get(variable);

      BigInteger value = entry.getValue();

      int lowUnusedFieldIndex = -1;

      for (int i = 0; i < mask.getBitSize(); i++) {
        if (!mask.getBit(i)) {
          if (lowUnusedFieldIndex == -1) {
            lowUnusedFieldIndex = i;
          }
        } else {
          if (lowUnusedFieldIndex != -1) {
            value = BitUtils.setField(value, lowUnusedFieldIndex, i - 1,
                initializer.getValue(i - lowUnusedFieldIndex));
            lowUnusedFieldIndex = -1;
          }
        }
      }

      if (lowUnusedFieldIndex != -1) {
        value = BitUtils.setField(value, lowUnusedFieldIndex, mask.getBitSize() - 1,
            initializer.getValue(mask.getBitSize() - lowUnusedFieldIndex));
      }

      solution.put(variable, value);
    }

    return new SolverResult<>(solution);
  }
}
