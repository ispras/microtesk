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
 * {@link Sat4jSolver} is a SAT-based bit-vector constraint solver.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Sat4jSolver implements Solver<Map<Variable, BitVector>> {
  private final Encoder<Sat4jFormula> encoder;
  private final Decoder<IProblem> decoder;

  public Sat4jSolver(final Encoder<Sat4jFormula> encoder, final Decoder<IProblem> decoder) {
    InvariantChecks.checkNotNull(encoder);
    InvariantChecks.checkNotNull(decoder);

    this.encoder = encoder;
    this.decoder = decoder;
  }

  public Sat4jSolver(final Coder<Sat4jFormula, IProblem> coder) {
    this(coder, coder);
  }

  /**
   * Constructs a solver.
   *
   * @param formulae the constraints to be solved.
   * @param initializer the initializer to be used to fill the unused fields.
   */
  public Sat4jSolver(
      final Collection<Node> formulae,
      final VariableInitializer initializer) {
    InvariantChecks.checkNotNull(formulae);
    InvariantChecks.checkNotNull(initializer);

    final CoderSat4j coder = new CoderSat4j(initializer); // FIXME:
    this.encoder = coder;
    this.decoder = coder;

    for (final Node formula : formulae) {
      encoder.addNode(formula);
    }
  }

  /**
   * Constructs a solver.
   *
   * @param formula the constraint to be solved.
   * @param initializer the initializer to be used to fill the unused fields.
   */
  public Sat4jSolver(
      final Node formula,
      final VariableInitializer initializer) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(initializer);

    final CoderSat4j coder = new CoderSat4j(initializer); // FIXME:
    this.encoder = coder;
    this.decoder = coder;

    encoder.addNode(formula);
  }

  @Override
  public SolverResult<Map<Variable, BitVector>> solve(final Mode mode) {
    InvariantChecks.checkNotNull(mode);

    final ISolver solver = SolverFactory.newDefault();
    final Sat4jFormula formula = encoder.encode();

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
    final Map<Variable, BitVector> solution = decoder.decode(solver);
    return new SolverResult<>(solution);
  }
}
