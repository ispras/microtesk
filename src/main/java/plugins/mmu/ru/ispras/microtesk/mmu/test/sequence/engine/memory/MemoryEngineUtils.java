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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFieldFormulaSolver;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * {@link MemoryEngineUtils} implements utilities used in the memory engine.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngineUtils {
  private MemoryEngineUtils() {}

  public static boolean isFeasiblePath(
      final MemoryAccessPath path,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    final SolverResult<Map<IntegerVariable, BigInteger>> result = solve(path, constraints);
    return result.getStatus() == SolverResult.Status.SAT;
  }

  public static Collection<MemoryAccessPath> getFeasiblePaths(
      final Collection<MemoryAccessPath> paths,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(paths);
    InvariantChecks.checkNotNull(constraints);

    final Collection<MemoryAccessPath> result = new ArrayList<>(paths.size());

    for (final MemoryAccessPath path : paths) {
      if (isFeasiblePath(path, constraints)) {
        result.add(path);
      }
    }

    return result;
  }

  public static Map<IntegerVariable, BigInteger> generateData(
      final MemoryAccessPath path,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    final SolverResult<Map<IntegerVariable, BigInteger>> result = solve(path, constraints);

    InvariantChecks.checkTrue(result.getStatus() == SolverResult.Status.SAT,
        String.format("Infeasible path=%s", path));

    // Solution contains only such variables that are used in the path.
    return result.getResult();
  }

  private static SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MemoryAccessPath path,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    final MemorySymbolicExecutor symbolicExecutor = new MemorySymbolicExecutor(path);
    final MemorySymbolicExecutor.Result symbolicResult = symbolicExecutor.execute();

    final Collection<IntegerVariable> variables = symbolicResult.getVariables();
    final IntegerFormula<IntegerField> formula = symbolicResult.getFormula();

    // Supplement the formula with the constraints.
    for (final IntegerConstraint<IntegerField> constraint : constraints) {
      formula.addConstraint(constraint);
    }

    final IntegerFieldFormulaSolver solver = new IntegerFieldFormulaSolver(variables, formula);

    return solver.solve();
  }
}
