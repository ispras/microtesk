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

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryEngineUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemorySymbolicExecutor;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class MemoryCoverageExtractor {
  private static final class MemoryAccessPathEntry {
    final MemoryAccessPath path;
    final MemorySymbolicExecutor.Result result;

    MemoryAccessPathEntry(final MemoryAccessPath path, final MemorySymbolicExecutor.Result result) {
      InvariantChecks.checkNotNull(path);
      InvariantChecks.checkNotNull(result);

      this.path = path;
      this.result = result;
    }
  }

  private final MmuSubsystem memory;

  public MemoryCoverageExtractor(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);
    this.memory = memory;
  }

  /**
   * Returns all memory access paths for the given memory access type.
   * 
   * @param type the memory access type or {@code null}.
   * @return the memory access paths
   */
  public List<MemoryAccessPath> getPaths(final MemoryAccessType type) {
    final ArrayList<MemoryAccessPath> paths = new ArrayList<>();
    final ArrayList<MemoryAccessPathEntry> queue = new ArrayList<>();

    final Collection<MmuTransition> outTransitions = memory.getTransitions(memory.getStartAction());
    InvariantChecks.checkNotNull(outTransitions);

    for (final MmuTransition transition : outTransitions) {
      final MmuGuard guard = transition.getGuard();

      if (type == null /* Any operation */ || guard.getOperation() == type.getOperation()) {
        final MemorySymbolicExecutor.Result result = new MemorySymbolicExecutor.Result();

        if (MemoryEngineUtils.isFeasibleTransition(transition, result /* INOUT */)) {
          final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

          builder.add(transition);
          queue.add(new MemoryAccessPathEntry(builder.build(), result));
        }
      }
    }

    while (!queue.isEmpty()) {
      final MemoryAccessPathEntry entry = queue.remove(queue.size() - 1);

      final Collection<MemoryAccessPathEntry> continuations = elongatePath(type, entry);

      if (continuations == null) {
        continue;
      } else if (continuations.isEmpty()) {
        paths.add(entry.path);
      } else {
        queue.addAll(continuations);
      }
    }

    // TODO: This code needs to be optimized?
    for (int i = paths.size() - 1; i >= 0; --i) {
      final MemoryAccessPath path = paths.get(i);
      final Collection<MmuSegment> segments = path.getSegments();

      if (segments.isEmpty()) {
        final int lastIndex = paths.size() - 1;
        paths.set(i, paths.get(lastIndex));
        paths.remove(lastIndex);
      }
    }
    paths.trimToSize();

    return paths;
  }

  /**
   * Elongates the memory access path.
   * 
   * @param type the memory access type or {@code null}.
   * @param entry the information about the memory access path to be elongated.
   * @return the collection of all possible continuations of the given memory access path
   *         (in particular, the empty collection if the path is completed).
   */
  private Collection<MemoryAccessPathEntry> elongatePath(
      final MemoryAccessType type, final MemoryAccessPathEntry entry) {
    InvariantChecks.checkNotNull(entry);

    final MemoryAccessPath path = entry.path;
    final MemorySymbolicExecutor.Result result = entry.result;

    final MmuTransition lastTransition = path.getLastTransition();
    final MmuAction targetAction = lastTransition.getTarget();

    final Collection<MmuTransition> outTransitions = memory.getTransitions(targetAction);
    InvariantChecks.checkNotNull(outTransitions);

    if (outTransitions.isEmpty()) {
      return Collections.emptyList();
    }

    final Collection<MemoryAccessPathEntry> elongatedEntries = new ArrayList<>();

    for (final MmuTransition transition : outTransitions) {
      final MmuGuard guard = transition.getGuard();
      final MemoryOperation operation = guard != null ? guard.getOperation() : null;

      if (type == null || operation == null || operation == type.getOperation()) {
        final MemorySymbolicExecutor.Result resultClone =
            outTransitions.size() == 1 ? result : new MemorySymbolicExecutor.Result(result);

        if (MemoryEngineUtils.isFeasibleTransition(transition, resultClone)) {
          final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

          builder.addAll(path.getTransitions());
          builder.add(transition);
  
          elongatedEntries.add(new MemoryAccessPathEntry(builder.build(), resultClone));
        }
      }
    }

    return elongatedEntries.isEmpty() ? elongatedEntries : null;
  }
}
