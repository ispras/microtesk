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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFieldFormulaSolver;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariableInitializer;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.classifier.ClassifierEventBased;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemoryEngineUtils} implements utilities used in the memory engine.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngineUtils {
  private MemoryEngineUtils() {}

  public static boolean isFeasibleTransition(
      final MmuTransition transition,
      final MemorySymbolicExecutor.Result partialResult /* INOUT */) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(partialResult);

    final MemorySymbolicExecutor symbolicExecutor =
        new MemorySymbolicExecutor(transition, partialResult, false);
    final MemorySymbolicExecutor.Result symbolicResult = symbolicExecutor.execute();

    if (transition.getGuard() == null) {
      return true;
    }

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(transition, symbolicResult, IntegerVariableInitializer.ZEROS, Solver.Mode.SAT);

    return result.getStatus() == SolverResult.Status.SAT;
  }

  public static boolean isFeasiblePath(
      final MemoryAccessPath path,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(path, constraints, IntegerVariableInitializer.ZEROS, Solver.Mode.SAT);

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

  public static Collection<MemoryAccessPath> getSimilarPaths(
      final MemoryAccessPath path,
      final Collection<MemoryAccessPath> paths) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(paths);

    final Map<MmuBuffer, BufferAccessEvent> pathSkeleton =
        ClassifierEventBased.getBuffersAndEvents(path);
    final Map<Map<MmuBuffer, BufferAccessEvent>, Set<MemoryAccessPath>> pathClasses =
        ClassifierEventBased.getBuffersAndEvents(paths);

    return pathClasses.get(pathSkeleton);
  }

  public static Collection<MemoryAccessPath> getFeasibleSimilarPaths(
      final MemoryAccessPath path,
      final Collection<MemoryAccessPath> paths,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(paths);
    InvariantChecks.checkNotNull(constraints);

    final Collection<MemoryAccessPath> similarPaths = getSimilarPaths(path, paths);
    InvariantChecks.checkNotNull(similarPaths);

    return getFeasiblePaths(similarPaths, constraints);
  }

  public static Map<IntegerVariable, BigInteger> generateData(
      final MemoryAccessPath path,
      final Collection<IntegerConstraint<IntegerField>> constraints,
      final IntegerVariableInitializer initializer) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(initializer);

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(path, constraints, initializer, Solver.Mode.MAP);

    // Solution contains only such variables that are used in the path.
    return result.getStatus() == SolverResult.Status.SAT ? result.getResult() : null;
  }

  public static boolean isFeasibleStructure(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(structure, IntegerVariableInitializer.ZEROS, Solver.Mode.SAT);

    return result.getStatus() == SolverResult.Status.SAT;
  }

  private static Collection<IntegerVariable> getFormulaVariables(
      final IntegerFormula<IntegerField> formula) {
    final Collection<IntegerVariable> variables = new LinkedHashSet<>();

    for (final IntegerField field : formula.getVariables()) {
      variables.add(field.getVariable());
    }

    return variables;
  }

  private static SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MmuTransition transition,
      final MemorySymbolicExecutor.Result symbolicResult/* INOUT */,
      final IntegerVariableInitializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    final Collection<IntegerVariable> variables = symbolicResult.getVariables();
    final IntegerFormula<IntegerField> formula = symbolicResult.getFormula();

    final IntegerFieldFormulaSolver solver =
        new IntegerFieldFormulaSolver(variables, formula, initializer);

    return solver.solve(mode);
  }

  private static SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MemoryAccessPath path,
      final Collection<IntegerConstraint<IntegerField>> constraints,
      final IntegerVariableInitializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    if (!path.hasSymbolicResult()) {
      final MemorySymbolicExecutor symbolicExecutor = new MemorySymbolicExecutor(path, true);
      path.setSymbolicResult(symbolicExecutor.execute());
    }

    final MemorySymbolicExecutor.Result symbolicResult = path.getSymbolicResult();

    final int collectionSize = constraints.size() + 1;

    final Collection<Collection<IntegerVariable>> variables = new ArrayList<>(collectionSize);
    variables.add(symbolicResult.getVariables());

    final Collection<IntegerFormula<IntegerField>> formulae = new ArrayList<>(collectionSize);
    formulae.add(symbolicResult.getFormula());

    // Supplement the formula with the constraints.
    for (final IntegerConstraint<IntegerField> constraint : constraints) {
      final IntegerFormula<IntegerField> formula = constraint.getFormula();
      final Collection<IntegerVariable> collection = getFormulaVariables(formula);

      variables.add(collection);
      formulae.add(formula);
    }

    Logger.debug("Formulae: %s", formulae);

    final IntegerFieldFormulaSolver solver =
        new IntegerFieldFormulaSolver(variables, formulae, initializer);

    return solver.solve(mode);
  }

  private static SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MemoryAccessStructure structure,
      final IntegerVariableInitializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    final MemorySymbolicExecutor symbolicExecutor =
        new MemorySymbolicExecutor(structure, mode == Solver.Mode.MAP);
    final MemorySymbolicExecutor.Result symbolicResult = symbolicExecutor.execute();

    final Collection<IntegerVariable> variables = symbolicResult.getVariables();
    final IntegerFormula<IntegerField> formula = symbolicResult.getFormula();

    final IntegerFieldFormulaSolver solver =
        new IntegerFieldFormulaSolver(variables, formula, initializer);

    return solver.solve(mode);
  }
}
