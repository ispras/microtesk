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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFieldFormulaProblemSat4j;
import ru.ispras.microtesk.basis.solver.integer.IntegerFieldFormulaSolverSat4j;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormulaBuilder;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariableInitializer;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.BufferEventConstraint;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicExecutor;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicRestrictor;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicResult;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCalculator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuProgram;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.utils.function.Function;

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

    final MmuCondition condition = guard.getCondition(null, MemoryAccessContext.EMPTY);

    if (condition == null) {
      return false;
    }

    final Boolean value =
        MmuCalculator.eval(condition,
            new Function<IntegerVariable, BigInteger>() {
              @Override
              public BigInteger apply(final IntegerVariable variable) {
                return null;
              }
            });

    if (value == null) {
      return false;
    }

    return value == false;
  }

  private static boolean checkBufferConstraints(
      final MemoryAccessPath.Entry entry,
      final Collection<BufferEventConstraint> bufferConstraints) {

    final MmuProgram program = entry.getProgram();
    final MemoryAccessContext context = entry.getContext();

    // Recursive memory accesses are ignored.
    if (!context.getMemoryAccessStack().isEmpty()) {
      return true;
    }

    for (final MmuTransition transition : program.getTransitions()) {
      // Empty context is enough for checking buffer access constraints.
      final Collection<MmuBufferAccess> bufferAccesses =
          transition.getBufferAccesses(MemoryAccessContext.EMPTY);

      if (bufferAccesses.isEmpty()) {
        continue;
      }

      for (final MmuBufferAccess bufferAccess : bufferAccesses) {
        for (final BufferEventConstraint bufferConstraint : bufferConstraints) {
          final MmuBuffer buffer = bufferConstraint.getBuffer();
          final Set<BufferAccessEvent> events = bufferConstraint.getEvents();

          if (buffer.equals(bufferAccess.getBuffer()) && !events.contains(bufferAccess.getEvent())) {
            return false;
          }
        }
      }
    }

    return true;
  }

  public static boolean isFeasibleEntry(
      final MemoryAccessPath.Entry entry,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemorySymbolicResult partialResult /* INOUT */) {
    InvariantChecks.checkNotNull(entry);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(partialResult);

    final MmuProgram program = entry.getProgram();

    if (program.isAtomic() && !isValidTransition(program.getTransition(), type)) {
      return false;
    }

    final Collection<BufferEventConstraint> bufferConstraints = constraints.getBufferEvents();
    if (!checkBufferConstraints(entry, bufferConstraints)) {
      return false;
    }

    final MemorySymbolicExecutor symbolicExecutor = newSymbolicExecutor(partialResult);
    final Boolean status = symbolicExecutor.execute(entry);

    if (status != null) {
      return status.booleanValue();
    }

    // Integer constraints are not applied, because they are relevant only for
    // latest assignments and latest buffer accesses.

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(partialResult, IntegerVariableInitializer.ZEROS, Solver.Mode.SAT);

    return result.getStatus() == SolverResult.Status.SAT;
  }

  private static boolean checkBufferConstraints(
      final MemoryAccess access,
      final Collection<BufferEventConstraint> bufferConstraints) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(bufferConstraints);

    final MemoryAccessPath path = access.getPath();

    for (final BufferEventConstraint bufferConstraint : bufferConstraints) {
      final MmuBuffer buffer = bufferConstraint.getBuffer();
      final Collection<BufferAccessEvent> allowedEvents = bufferConstraint.getEvents();
      final Collection<BufferAccessEvent> inducedEvents = path.getEvents(buffer);

      // If there is a constraint on a buffer, the buffer should be accessed.
      if (inducedEvents.isEmpty()) {
        Logger.debug("Path does not contain %s", buffer);
        return false;
      }

      // There should not be events of other types than ones specified in the constraint.
      if (!allowedEvents.containsAll(inducedEvents)) {
        Logger.debug("Event mismatch: induced=%s, allowed=%s", inducedEvents, allowedEvents);
        return false;
      }
    }

    return true;
  }

  public static boolean isFeasibleAccess(
      final MemoryAccess access,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(constraints);

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(
            access,
            Collections.<MmuCondition>emptyList(),
            constraints,
            IntegerVariableInitializer.ZEROS,
            Solver.Mode.SAT
        );

    return result.getStatus() == SolverResult.Status.SAT;
  }

  public static boolean isFeasibleAccess(
      final MemoryAccess access,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(constraints);

    final Collection<BufferEventConstraint> bufferEventConstraints = constraints.getBufferEvents();
    if (!checkBufferConstraints(access, bufferEventConstraints)) {
      Logger.debug("Checking buffer constraints failed");
      return false;
    }

    final Collection<IntegerConstraint<IntegerField>> integerConstraints = constraints.getIntegers();
    if (!isFeasibleAccess(access, integerConstraints)) {
      Logger.debug("Checking integer constraints failed");
      return false;
    }

    return true;
  }

  public static Map<IntegerVariable, BigInteger> generateData(
      final MemoryAccess access,
      final Collection<MmuCondition> conditions,
      final Collection<IntegerConstraint<IntegerField>> constraints,
      final IntegerVariableInitializer initializer) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(initializer);

    Logger.debug("Start generating data");

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(access, conditions, constraints, initializer, Solver.Mode.MAP);

    Logger.debug("Stop generating data: %s", result.getResult());

    // Solution contains only such variables that are used in the path.
    return result.getStatus() == SolverResult.Status.SAT ? result.getResult() : null;
  }

  public static boolean isFeasibleStructure(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);

    final SolverResult<Map<IntegerVariable, BigInteger>> result =
        solve(structure, IntegerVariableInitializer.ZEROS, Solver.Mode.SAT);

    return result.getStatus() == SolverResult.Status.SAT;
  }

  private static SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MemorySymbolicResult symbolicResult /* INOUT */,
      final IntegerVariableInitializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    if (symbolicResult.hasConflict()) {
      return new SolverResult<Map<IntegerVariable, BigInteger>>("Conflict in symbolic execution");
    }

    final IntegerFormulaBuilder<IntegerField> builder = symbolicResult.getBuilder();

    final Solver<Map<IntegerVariable, BigInteger>> solver = newSolver(builder, initializer);
    final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve(mode);

    if (result.getStatus() != SolverResult.Status.SAT && mode == Solver.Mode.MAP) {
      for (final String error : result.getErrors()) {
        Logger.debug("Error: %s", error);
      }
    }

    return result;
  }

  private static SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MemoryAccess access,
      final Collection<MmuCondition> conditions,
      final Collection<IntegerConstraint<IntegerField>> constraints,
      final IntegerVariableInitializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(conditions);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    Logger.debug("Solving path constraints");

    // FIXME: Move from access.getPath() to access.
    if (!access.getPath().hasSymbolicResult()) {
      final MemorySymbolicExecutor symbolicExecutor = newSymbolicExecutor();

      symbolicExecutor.execute(access, true);
      access.getPath().setSymbolicResult(symbolicExecutor.getResult());
    }

    final MemorySymbolicResult symbolicResult = access.getPath().getSymbolicResult();
    final MemorySymbolicExecutor symbolicExecutor = newSymbolicExecutor(symbolicResult);

    for (final MmuCondition condition : conditions) {
      symbolicExecutor.execute(condition);
    }

    if (symbolicResult.hasConflict()) {
      Logger.debug("Conflict in symbolic execution");
      return new SolverResult<Map<IntegerVariable, BigInteger>>("Conflict in symbolic execution");
    }

    final IntegerFormulaBuilder<IntegerField> builder = symbolicResult.getBuilder().clone();

    // Supplement the formula with the constraints.
    for (final IntegerConstraint<IntegerField> constraint : constraints) {
      builder.addFormula(constraint.getFormula());
    }

    final Solver<Map<IntegerVariable, BigInteger>> solver = newSolver(builder, initializer);
    final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve(mode);

    if (result.getStatus() != SolverResult.Status.SAT && mode == Solver.Mode.MAP) {
      Logger.debug("Access: %s", access);
      for (final String msg : result.getErrors()) {
        Logger.debug("Error: %s", msg);
      }
    }

    Logger.debug("Solving result: %s", result.getResult());
    return result;
  }

  private static SolverResult<Map<IntegerVariable, BigInteger>> solve(
      final MemoryAccessStructure structure,
      final IntegerVariableInitializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    final MemorySymbolicExecutor symbolicExecutor = newSymbolicExecutor();
    symbolicExecutor.execute(structure, mode == Solver.Mode.MAP);

    final MemorySymbolicResult symbolicResult = symbolicExecutor.getResult();
    final IntegerFormulaBuilder<IntegerField> builder = symbolicResult.getBuilder();
    final Solver<Map<IntegerVariable, BigInteger>> solver = newSolver(builder, initializer);

    return solver.solve(mode);
  }

  public static IntegerFormulaBuilder<IntegerField> newFormulaBuilder() {
    return new IntegerFieldFormulaProblemSat4j();
  }

  public static MemorySymbolicResult newSymbolicResult() {
    return new MemorySymbolicResult(newFormulaBuilder());
  }

  public static MemorySymbolicRestrictor newSymbolicRestrictor() {
    return new MemorySymbolicRestrictor(null);
  }

  public static MemorySymbolicExecutor newSymbolicExecutor() {
    return new MemorySymbolicExecutor(newSymbolicRestrictor(), newSymbolicResult());
  }

  public static MemorySymbolicExecutor newSymbolicExecutor(final MemorySymbolicResult result) {
    return new MemorySymbolicExecutor(newSymbolicRestrictor(), result);
  }

  public static Solver<Map<IntegerVariable, BigInteger>> newSolver(
      final IntegerFormulaBuilder<IntegerField> builder,
      final IntegerVariableInitializer initializer) {
    InvariantChecks.checkNotNull(builder);
    InvariantChecks.checkNotNull(initializer);

    return new IntegerFieldFormulaSolverSat4j(builder, initializer);
  }
}
