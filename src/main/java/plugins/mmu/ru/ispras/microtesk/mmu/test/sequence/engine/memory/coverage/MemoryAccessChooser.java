/*
 * Copyright 2016-2017 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.BiasedConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

public final class MemoryAccessChooser {
  private final MmuSubsystem memory;
  private final Collection<List<Object>> trajectories;
  private final MemoryGraph graph;
  private final MemoryAccessType type;
  private final MemoryAccessConstraints constraints;
  private final int recursionLimit;
  private final boolean discardEmptyTrajectories;

  private final Collection<Iterator<MemoryAccessIterator.Result>> iterators = new ArrayList<>();
  private final Collection<MemoryAccess> accesses = new ArrayList<>(); 

  public MemoryAccessChooser(
      final MmuSubsystem memory,
      final Collection<List<Object>> trajectories,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final int recursionLimit,
      final boolean discardEmptyTrajectories) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(trajectories);
    InvariantChecks.checkNotEmpty(trajectories);
    InvariantChecks.checkNotNull(graph);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    Logger.debug("Creating memory access chooser: %s", constraints);

    this.memory = memory;
    this.trajectories = trajectories;
    this.graph = graph;
    this.type = type;
    this.constraints = constraints;
    this.recursionLimit = recursionLimit;
    this.discardEmptyTrajectories = discardEmptyTrajectories;

    for (final List<Object> trajectory : trajectories) {
      if (discardEmptyTrajectories && trajectory.isEmpty()) {
        continue;
      }

      Logger.debug("Add iterator for the trajectory: %s", trajectory);
      iterators.add(
        new MemoryAccessIterator(memory, trajectory, graph, type, constraints, recursionLimit)
      );
    }
  }

  public MemoryAccessChooser(
      final MmuSubsystem memory,
      final List<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final int recursionLimit) {
    this(
        memory,
        Collections.<List<Object>>singleton(trajectory),
        graph,
        type,
        constraints,
        recursionLimit,
        false);
  }

  public MemoryAccess get() {
    while (!iterators.isEmpty()) {
      final Iterator<MemoryAccessIterator.Result> iterator = Randomizer.get().choose(iterators);

      if (iterator.hasNext()) {
        final MemoryAccessIterator.Result result = iterator.next();
        final MemoryAccess access = result.getAccess();

        accesses.add(access);
        return access;
      }

      iterators.remove(iterator);
    }

    if (!accesses.isEmpty()) {
      return Randomizer.get().choose(accesses);
    }

    return null;
  }

  public MemoryAccess get(final BiasedConstraints<MemoryAccessConstraints> constraints) {
    InvariantChecks.checkNotNull(constraints);

    if (constraints.isEmpty()) {
      return get();
    }

    // New hard constraints & new soft constraints.
    final Collection<MemoryAccessConstraints> strongestConstraints = constraints.getAll();
    // Existing constraints & new hard constraints & new soft constraints.
    strongestConstraints.add(this.constraints);

    final MemoryAccessChooser strongestChooser =
        new MemoryAccessChooser(
            this.memory,
            this.trajectories,
            this.graph,
            this.type,
            MemoryAccessConstraints.compose(strongestConstraints),
            this.recursionLimit,
            this.discardEmptyTrajectories);

    final MemoryAccess strongestAccess = strongestChooser.get();

    if (strongestAccess != null) {
      return strongestAccess;
    }

    // New hard constraints.
    final Collection<MemoryAccessConstraints> weakestConstraints = constraints.getHard();

    if (weakestConstraints.isEmpty()) {
      return get();
    }

    // Existing constraints & new hard constraints.
    weakestConstraints.add(this.constraints);

    final MemoryAccessChooser weakestChooser =
        new MemoryAccessChooser(
            this.memory,
            this.trajectories,
            this.graph,
            this.type,
            MemoryAccessConstraints.compose(weakestConstraints),
            this.recursionLimit,
            this.discardEmptyTrajectories);

    return weakestChooser.get();
  }
}
