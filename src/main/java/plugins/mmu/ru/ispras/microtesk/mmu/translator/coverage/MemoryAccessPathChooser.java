/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

public final class MemoryAccessPathChooser {
  private final Collection<Iterator<MemoryAccessPath>> iterators = new ArrayList<>();
  private final Collection<MemoryAccessPath> paths = new ArrayList<>(); 

  public MemoryAccessPathChooser(
      final MmuSubsystem memory,
      final Collection<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(trajectory);
    InvariantChecks.checkNotNull(graph);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    iterators.add(new MemoryAccessPathIterator(memory, trajectory, graph, type, constraints));
  }

  public MemoryAccessPathChooser(
      final MmuSubsystem memory,
      final Collection<Collection<Object>> trajectories,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final boolean discardEmptyTrajectories) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(trajectories);
    InvariantChecks.checkNotEmpty(trajectories);
    InvariantChecks.checkNotNull(graph);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    for (final Collection<Object> trajectory : trajectories) {
      if (discardEmptyTrajectories && trajectory.isEmpty()) {
        continue;
      }

      iterators.add(new MemoryAccessPathIterator(memory, trajectory, graph, type, constraints));
    }
  }

  public MemoryAccessPath get() {
    while (!iterators.isEmpty()) {
      final Iterator<MemoryAccessPath> iterator = Randomizer.get().choose(iterators);

      if (iterator.hasNext()) {
        final MemoryAccessPath path = iterator.next();
        InvariantChecks.checkNotNull(path);
  
        paths.add(path);
        return path;
      }

      iterators.remove(iterator);
    }

    if (!paths.isEmpty()) {
      return Randomizer.get().choose(paths);
    }

    return null;
  }
}
