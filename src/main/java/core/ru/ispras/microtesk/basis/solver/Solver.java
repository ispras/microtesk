/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

import java.util.Map;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link Solver} defines an interface of solvers.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Solver {
  /**
   * {@link Mode} represents a solver mode.
   */
  public static enum Mode {
    /** SAT/UNSAT. */
    SAT,
    /** (SAT, MAP)/UNSAT. */
    MAP
  }

  /** Constraint encoder. */
  protected final Encoder encoder;

  protected Solver(final Encoder encoder) {
    InvariantChecks.checkNotNull(encoder);
    this.encoder = encoder;
  }

  /**
   * Adds the node to the current constraint.
   *
   * @param node the node to be added.
   */
  public final void addNode(final Node node) {
    encoder.addNode(node);
  }

  /**
   * Checks whether the constraint is satisfiable and returns a solution (if required).
   *
   * @param constraint the encoded constraint to be solved.
   * @param mode the solver mode.
   * @return {@code SAT} if the constraint is satisfiable; {@code UNSAT} otherwise.
   */
  protected abstract SolverResult<Map<Variable, BitVector>> solve(Constraint constraint, Mode mode);

  /**
   * Checks whether the encoded constraint is satisfiable and returns a solution (if required).
   *
   * @param mode the solver mode.
   * @return {@code SAT} if the constraint is satisfiable; {@code UNSAT} otherwise.
   */
  public final SolverResult<Map<Variable, BitVector>> solve(final Mode mode) {
    final Constraint constraint = encoder.encode();
    return solve(constraint, mode);
  }
}
