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

package ru.ispras.microtesk.mmu.test.engine.memory;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.Coder;
import ru.ispras.microtesk.basis.solver.bitvector.CoderSat4j;
import ru.ispras.microtesk.basis.solver.bitvector.SolverSat4j;
import ru.ispras.microtesk.basis.solver.bitvector.Initializer;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.template.AccessConstraints;
import ru.ispras.microtesk.mmu.test.template.BufferEventConstraint;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.model.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.model.spec.MmuGuard;
import ru.ispras.microtesk.mmu.model.spec.MmuProgram;
import ru.ispras.microtesk.mmu.model.spec.MmuTransition;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.utils.FortressUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    return type.getOperation() == null || operation == null || operation == type.getOperation();
  }

  public static boolean isDisabledTransition(final MmuTransition transition) {
    InvariantChecks.checkNotNull(transition);

    final MmuGuard guard = transition.getGuard();
    if (guard == null) {
      return false;
    }

    final Node condition = guard.getCondition(null, MemoryAccessContext.EMPTY);
    if (condition == null) {
      return false;
    }

    final Boolean value = FortressUtils.evaluateBoolean(condition);
    if (value == null) {
      return false;
    }

    return value == false;
  }

  private static boolean checkBufferConstraints(
      final AccessPath.Entry entry,
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

          if (buffer.equals(bufferAccess.getBuffer())
              && !events.contains(bufferAccess.getEvent())) {
            return false;
          }
        }
      }
    }

    return true;
  }

  private static boolean checkBufferConstraints(
      final Access access,
      final Collection<BufferEventConstraint> bufferConstraints) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(bufferConstraints);

    final AccessPath path = access.getPath();

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

  public static boolean isFeasibleEntry(
      final AccessPath.Entry entry,
      final MemoryAccessType type,
      final AccessConstraints constraints,
      final SymbolicResult partialResult /* INOUT */) {
    InvariantChecks.checkNotNull(entry);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(partialResult);

    final MmuProgram program = entry.getProgram();

    if (program.isAtomic() && !isValidTransition(program.getTransition(), type)) {
      return false;
    }

    if (!checkBufferConstraints(entry, constraints.getBufferEventConstraints())) {
      return false;
    }

    final RegionSettings region = constraints.getRegion();
    final SymbolicExecutor symbolicExecutor = newSymbolicExecutor(region, partialResult);
    final Boolean status = symbolicExecutor.execute(entry);

    if (status != null) {
      return status;
    }

    // Integer constraints are not applied, because they are relevant only for
    // latest assignments and latest buffer accesses.

    final SolverResult<Map<Variable, BitVector>> result =
        solve(partialResult, Solver.Mode.SAT);

    return result.getStatus() == SolverResult.Status.SAT;
  }

  public static boolean isFeasibleAccess(final Access access) {
    InvariantChecks.checkNotNull(access);

    final AccessConstraints constraints = access.getConstraints();

    if (!checkBufferConstraints(access, constraints.getBufferEventConstraints())) {
      return false;
    }

    final SolverResult<Map<Variable, BitVector>> result =
        solve(
            access,
            Collections.emptyList(),
            constraints.getGeneralConstraints(),
            Initializer.ZEROS,
            Solver.Mode.SAT
        );

    return result.getStatus() == SolverResult.Status.SAT;
  }

  public static Map<Variable, BitVector> generateData(
      final Access access,
      final Collection<Node> conditions,
      final Collection<Node> constraints,
      final Initializer initializer) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(initializer);

    Logger.debug("Start generating data");

    final SolverResult<Map<Variable, BitVector>> result =
        solve(access, conditions, constraints, initializer, Solver.Mode.MAP);

    Logger.debug("Stop generating data: %s", result.getResult());

    // Solution contains only such variables that are used in the path.
    return result.getStatus() == SolverResult.Status.SAT ? result.getResult() : null;
  }

  public static boolean isFeasibleStructure(final List<Access> structure) {
    InvariantChecks.checkNotNull(structure);

    final SolverResult<Map<Variable, BitVector>> result =
        solve(structure, Solver.Mode.SAT);

    return result.getStatus() == SolverResult.Status.SAT;
  }

  private static SolverResult<Map<Variable, BitVector>> solve(
      final SymbolicResult symbolicResult /* INOUT */,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(mode);

    if (symbolicResult.hasConflict()) {
      return SolverResult.newUnsat("Conflict in symbolic execution");
    }

    final Coder<Map<Variable, BitVector>> coder = symbolicResult.getCoder();

    final Solver<Map<Variable, BitVector>> solver = newSolver(coder);
    final SolverResult<Map<Variable, BitVector>> result = solver.solve(mode);

    if (result.getStatus() != SolverResult.Status.SAT && mode == Solver.Mode.MAP) {
      for (final String error : result.getErrors()) {
        Logger.debug("Error: %s", error);
      }
    }

    return result;
  }

  private static SolverResult<Map<Variable, BitVector>> solve(
      final Access access,
      final Collection<Node> conditions,
      final Collection<Node> constraints,
      final Initializer initializer,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkNotNull(conditions);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(initializer);
    InvariantChecks.checkNotNull(mode);

    Logger.debug("Solving path constraints");

    final RegionSettings region = access.getConstraints().getRegion();

    if (!access.hasSymbolicResult()) {
      final SymbolicExecutor symbolicExecutor = newSymbolicExecutor(region);

      symbolicExecutor.execute(access, true);
      access.setSymbolicResult(symbolicExecutor.getResult());
    }

    final SymbolicResult symbolicResult = access.getSymbolicResult();
    final SymbolicExecutor symbolicExecutor = newSymbolicExecutor(region, symbolicResult);

    for (final Node condition : conditions) {
      symbolicExecutor.execute(condition);
    }

    if (symbolicResult.hasConflict()) {
      Logger.debug("Conflict in symbolic execution");
      return SolverResult.newUnsat("Conflict in symbolic execution");
    }

    final Coder<Map<Variable, BitVector>> coder = symbolicResult.getCoder().clone();

    // Supplement the formula with the constraints.
    for (final Node constraint : constraints) {
      coder.addNode(constraint);
    }

    final Solver<Map<Variable, BitVector>> solver = newSolver(coder);
    final SolverResult<Map<Variable, BitVector>> result = solver.solve(mode);

    if (result.getStatus() != SolverResult.Status.SAT && mode == Solver.Mode.MAP) {
      Logger.debug("Access: %s", access);
      for (final String msg : result.getErrors()) {
        Logger.debug("Error: %s", msg);
      }
    }

    Logger.debug("Solving result: %s", result.getResult());
    return result;
  }

  private static SolverResult<Map<Variable, BitVector>> solve(
      final List<Access> structure,
      final Solver.Mode mode) {
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkNotNull(mode);

    final SymbolicExecutor symbolicExecutor = newSymbolicExecutor(null);
    symbolicExecutor.execute(structure, mode == Solver.Mode.MAP);

    final SymbolicResult symbolicResult = symbolicExecutor.getResult();
    final Coder<Map<Variable, BitVector>> coder = symbolicResult.getCoder();
    final Solver<Map<Variable, BitVector>> solver = newSolver(coder);

    return solver.solve(mode);
  }

  public static Coder<Map<Variable, BitVector>> newCoder() {
    return new CoderSat4j();
  }

  public static SymbolicResult newSymbolicResult() {
    return new SymbolicResult(newCoder());
  }

  public static SymbolicRestrictor newSymbolicRestrictor(final RegionSettings region) {
    // The region parameter can be null.
    return new SymbolicRestrictor(region);
  }

  public static SymbolicExecutor newSymbolicExecutor(final RegionSettings region) {
    // The region parameter can be null.
    return new SymbolicExecutor(newSymbolicRestrictor(region), newSymbolicResult());
  }

  public static SymbolicExecutor newSymbolicExecutor(
      final RegionSettings region,
      final SymbolicResult result) {
    // The region parameter can be null.
    return new SymbolicExecutor(newSymbolicRestrictor(region), result);
  }

  public static Solver<Map<Variable, BitVector>> newSolver(
      final Coder<Map<Variable, BitVector>> coder) {
    return new SolverSat4j(coder);
  }
}
