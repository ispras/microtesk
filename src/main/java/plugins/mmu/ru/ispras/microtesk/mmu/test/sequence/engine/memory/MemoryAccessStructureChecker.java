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
import java.util.Collection;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFieldFormulaSolver;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link MemoryAccessStructureChecker} checks consistency of a {@link MemoryAccessStructure}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessStructureChecker implements Predicate<MemoryAccessStructure> {
  private final Predicate<MemoryAccessStructure> filter;

  public MemoryAccessStructureChecker(
      final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(filter);
    this.filter = filter;
  }

  @Override
  public boolean test(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);

    if (!filter.test(structure)) {
      return false;
    }

    return solve(structure).getStatus() == SolverResult.Status.SAT;
  }

  private SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);

    final MemorySymbolicExecutor symbolicExecutor = new MemorySymbolicExecutor(structure);
    final MemorySymbolicExecutor.Result symbolicResult = symbolicExecutor.execute();

    final Collection<IntegerVariable> variables = symbolicResult.getVariables();
    final IntegerFormula<IntegerField> formula = symbolicResult.getFormula();
System.out.println(formula);
    final IntegerFieldFormulaSolver solver = new IntegerFieldFormulaSolver(variables, formula);

    return solver.solve();
  }
}
