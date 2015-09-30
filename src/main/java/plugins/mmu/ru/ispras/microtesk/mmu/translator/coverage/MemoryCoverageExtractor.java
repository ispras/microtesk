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
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class MemoryCoverageExtractor {
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
    final List<MmuTransition> out = memory.getTransitions(memory.getStartAction());

    if (out != null && !out.isEmpty()) {
      final List<MemoryAccessPath> queue = new ArrayList<>();

      for (final MmuTransition next : out) {
        final MmuGuard guard = next.getGuard();

        if (type == null || guard.getOperation() == type.getOperation()) {
          final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

          builder.add(next);
          queue.add(builder.build());
        }
      }

      while (!queue.isEmpty()) {
        final MemoryAccessPath path = queue.remove(queue.size() - 1);
        final List<MemoryAccessPath> prefixes = elongatePath(type, path);
        if (prefixes == null) {
          continue;
        } else if (!prefixes.isEmpty()) {
          queue.addAll(prefixes);
        } else {
          paths.add(path);
        }
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
   * @param path the memory access path to be elongated.
   * @return the list of all possible elongations of the given memory access path,
             empty list for terminal paths, null for infeasible paths
   */
  private List<MemoryAccessPath> elongatePath(
      final MemoryAccessType type, final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    final MmuTransition last = path.getLastTransition();
    final MmuAction target = last.getTarget();
    final List<MmuTransition> out = memory.getTransitions(target);

    if (!MemoryEngineUtils.isFeasiblePath(path)) {
      return null;
    }

    if (out != null && !out.isEmpty()) {
      final List<MemoryAccessPath> elongatedPaths = new ArrayList<>();

      for (final MmuTransition next : out) {
        final MmuGuard guard = next.getGuard();
        final MemoryOperation operation = guard != null ? guard.getOperation() : null;

        if (operation == null || type == null || operation == type.getOperation()) {
          final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

          builder.addAll(path.getTransitions());
          builder.add(next);

          elongatedPaths.add(builder.build());
        }
      }

      return elongatedPaths;
    }
    return Collections.emptyList();
  }
}
