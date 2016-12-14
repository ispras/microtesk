/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.Collections;
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
import ru.ispras.microtesk.mmu.basis.BufferEventConstraint;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCalculator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemoryEngineUtils} implements utilities used in the memory engine.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngineUtils {
  private MemoryEngineUtils() {}

  public static boolean isValidTransition(
      final MmuTransition transition,
      final MemoryAccessType type) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(type);

    final MmuGuard guard = transition.getGuard();
    final MemoryOperation operation = guard != null ? guard.getOperation() : null;

    if (type.getOperation() != null && operation != null && operation != type.getOperation()) {
      return false;
    }

    return true;
  }

  public static boolean isDisabledTransition(final MmuTransition transition) {
    InvariantChecks.checkNotNull(transition);

    final MmuGuard guard = transition.getGuard();

    if (guard == null) {
      return false;
    }

    final MemoryAccessStack emptyStack = new MemoryAccessStack();
    final MmuCondition condition = guard.getCondition(emptyStack);

    if (condition == null) {
      return false;
    }

    final Boolean value =
        MmuCalculator.eval(condition, Collections.<IntegerVariable, BigInteger>emptyMap());

    if (value == null) {
      return false;
    }

    return value == false;
  }

  public static boolean isFeasibleEntry(
      final MemoryAccessPath.Entry entry,
      final MemoryAccessStack stack,
      final MemorySymbolicExecutor.Result partialResult /* INOUT */) {
    InvariantChecks.checkNotNull(entry);
    InvariantChecks.checkNotNull(stack);
    InvariantChecks.checkNotNull(partialResult);

    final MemorySymbolicExecutor symbolicExecutor =
        new MemorySymbolicExecutor(entry, partialResult, false);

    final MmuTransition transition = entry.getTransition();
    InvariantChecks.checkNotNull(transition);

    final MmuGuard guard = transition.getGuard();

    final MmuCondition condition = guard != null
        ? guard.getCondition(stack)
        : null;

    final Boolean value = condition != null
        ? MmuCalculator.eval(condition, partialResult.getOriginalConstants())
        : new Boolean(true);

    // False can be return before symbolic execution.
    if (value != null && value == false) {
      return false;
    }

    final MemorySymbolicExecutor.Result symbolicResult = symbolicExecutor.execute();

    // True should be return after symbolic execution.
    if (value != null && value == true) {
      return true;
    }

    if (symbolicResult.hasConflict()) {
      return false;
    }

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(transition, symbolicResult, IntegerVariableInitializer.ZEROS, Solver.Mode.SAT);

    return result.getStatus() == SolverResult.Status.SAT;
  }

  public static boolean isFeasibleTransition(
      final MmuTransition transition,
      final MemoryAccessStack stack,
      final MemorySymbolicExecutor.Result partialResult /* INOUT */) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(stack);
    InvariantChecks.checkNotNull(partialResult);

    return isFeasibleEntry(MemoryAccessPath.Entry.NORMAL(transition, stack), stack, partialResult);
  }

  public static boolean isFeasibleTransition(
      final MmuTransition transition,
      final MemoryAccessType type,
      final MemoryAccessStack stack,
      final MemorySymbolicExecutor.Result partialResult /* INOUT */) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(partialResult);

    return isValidTransition(transition, type)
        && isFeasibleTransition(transition, stack, partialResult);
  }

  public static boolean isValidPath(final MmuSubsystem memory, final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(path);

    if(!memory.getRegions().isEmpty() && path.getRegions().isEmpty()) {
      return false;
    }

    if (memory.getRegions().isEmpty() && path.getSegments().isEmpty()) {
      return false;
    }

    return true;
  }

  public static boolean isEnabledPath(
      final MemoryAccessPath path,
      final Collection<BufferEventConstraint> constraints) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    for (final BufferEventConstraint constraint : constraints) {
      final MmuBuffer buffer = constraint.getBuffer();
      InvariantChecks.checkNotNull(buffer);

      final Set<BufferAccessEvent> events = constraint.getEvents();
      InvariantChecks.checkNotNull(events);

      if (path.contains(buffer) && !events.contains(path.getEvent(buffer))) {
        return false;
      }
    }

    return true;
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

  public static boolean isFeasiblePath(
      final MmuSubsystem memory,
      final MemoryAccessPath path,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    if (!isValidPath(memory, path)) {
      return false;
    }

    final Collection<BufferEventConstraint> bufferEventConstraints = constraints.getBufferEvents();

    if (bufferEventConstraints != null) {
      if (!isEnabledPath(path, bufferEventConstraints)) {
        return false;
      }
    }

    final Collection<IntegerConstraint<IntegerField>> integerConstraints = constraints.getIntegers();

    if (integerConstraints != null) {
      if (!isFeasiblePath(path, integerConstraints)) {
        return false;
      }
    }

    return true;
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
      final MemorySymbolicExecutor.Result symbolicResult /* INOUT */,
      final IntegerVariableInitializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    if (symbolicResult.hasConflict()) {
      return new SolverResult<Map<IntegerVariable, BigInteger>>("Conflict in symbolic execution");
    }

    final Collection<IntegerVariable> variables = symbolicResult.getVariables();
    final IntegerFormula<IntegerField> formula = symbolicResult.getFormula();
    final Map<IntegerVariable, BigInteger> constants = symbolicResult.getConstants();

    final IntegerFieldFormulaSolver solver =
        new IntegerFieldFormulaSolver(variables, formula, constants, initializer);

    final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve(mode);
    if (result.getStatus() != SolverResult.Status.SAT) {
      Logger.debug("Formula: %s", formula);
      Logger.debug("Constants: %s", constants);
      Logger.debug(stringOf(transition));
      for (final String msg : result.getErrors()) {
        Logger.debug("Error: %s", msg);
      }
    }
    return result;
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

    if (symbolicResult.hasConflict()) {
      return new SolverResult<Map<IntegerVariable, BigInteger>>("Conflict in symbolic execution");
    }

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

    final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve(mode);
    if (result.getStatus() != SolverResult.Status.SAT) {
      Logger.debug(stringOf(path));
      for (final String msg : result.getErrors()) {
        Logger.debug("Error: %s", msg);
      }
    }
    return result;
  }

  private static String stringOf(final MemoryAccessPath path) {
    final StringBuilder builder = new StringBuilder();
    for (final MemoryAccessPath.Entry entry : path.getEntries()) {
      final MmuTransition transition = entry.getTransition();

      builder.append(transition.getSource());
      if (transition.getGuard() != null) {
        builder.append(String.format(" -> [%s]", transition.getGuard()));
      }
      builder.append(" -> ");
    }

    final MmuTransition lastTransition = path.getLastEntry().getTransition();
    builder.append(lastTransition.getTarget());

    return builder.toString();
  }

  private static String stringOf(final MmuTransition transition) {
    final StringBuilder builder = new StringBuilder();
    builder.append(transition.getSource());
    if (transition.getGuard() != null) {
      builder.append(String.format(" -> [%s]", transition.getGuard()));
    }
    builder.append(" -> ");
    builder.append(transition.getTarget());

    return builder.toString();
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
