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

import java.util.Map;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Coder;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * {@link SolverSat4j} is a SAT-based bit-vector constraint solver.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SolverSat4j extends Solver<Map<Variable, BitVector>> {

  private static Coder<Map<Variable, BitVector>> newDefaultCoder() {
    return new CoderSat4j();
  }

  public SolverSat4j(final Coder<Map<Variable, BitVector>> coder) {
    super(coder, coder);
  }

  public SolverSat4j() {
    this(newDefaultCoder());
  }

  @Override
  protected SolverResult<Object> solve(final Constraint constraint, final Mode mode) {
    InvariantChecks.checkNotNull(constraint.getInnerRep() instanceof FormulaSat4j);

    final FormulaSat4j formula = (FormulaSat4j) constraint.getInnerRep();
    final ISolver solver = SolverFactory.newDefault();

    // Construct the problem.
    try {
      for (final IVecInt clause : formula.getClauses()) {
        solver.addClause(clause);
      }
    } catch (final ContradictionException e) {
      return SolverResult.newUnsat("Contradiction: " + e.getMessage());
    }

    // Solve the problem.
    try {
      if (!solver.isSatisfiable()) {
        return SolverResult.newUnsat();
      }
    } catch (final TimeoutException e) {
      return SolverResult.newUndef("Timeout: " + e.getMessage());
    }

    if (mode == Solver.Mode.SAT) {
      return SolverResult.newSat();
    }

    // Return the solution.
    return SolverResult.newSat(solver);
  }
}
