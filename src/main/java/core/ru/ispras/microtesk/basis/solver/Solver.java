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

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link Solver} defines an interface of solvers.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Solver<T> {
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
  /** Solution decoder. */
  protected final Decoder<T> decoder;

  protected Solver(final Encoder encoder, final Decoder<T> decoder) {
    InvariantChecks.checkNotNull(encoder);
    InvariantChecks.checkNotNull(decoder);

    this.encoder = encoder;
    this.decoder = decoder;
  }

  protected Solver(final Coder<T> coder) {
    this(coder, coder);
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
   * @param problem the encoded constraint (problem) to be solved.
   * @param mode the solver mode.
   * @return {@code SAT} if the constraint is satisfiable; {@code UNSAT} otherwise.
   */
  protected abstract SolverResult<Object> solve(Object problem, Mode mode);

  /**
   * Checks whether the encoded constraint is satisfiable and returns a solution (if required).
   *
   * @param mode the solver mode.
   * @return {@code SAT} if the constraint is satisfiable; {@code UNSAT} otherwise.
   */
  public final SolverResult<T> solve(final Mode mode) {
    final Object problem = encoder.encode();
    final SolverResult<Object> result = solve(problem, mode);

    if (result.getResult() == null) {
      return new SolverResult<>(result.getStatus(), null, result.getErrors());
    }

    final T solution = decoder.decode(result.getResult());
    return new SolverResult<>(result.getStatus(), solution, result.getErrors());
  }
}
