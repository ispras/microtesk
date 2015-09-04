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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class MemoryCoverageExtractor {
  private static final int MAX_RECURSION_DEPTH = 5;

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
    final List<MemoryAccessPath> paths = new ArrayList<>();
    final List<MmuTransition> out = memory.getTransitions(memory.getStartAction());

    if (out != null && !out.isEmpty()) {
      for (final MmuTransition next : out) {
        final MmuGuard guard = next.getGuard();

        if (type == null || guard.getOperation() == type.getOperation()) {
          final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

          builder.add(next);
          paths.add(builder.build());
        }
      }

      final Map<String, Integer> observed = new HashMap<>();

      int i = 0;
      while (i < paths.size()) {
        final MemoryAccessPath path = paths.get(i);
        final List<MemoryAccessPath> pathPrefixes = elongatePath(type, path);
        final int occurences = increment(observed, path.toString());

        if (pathPrefixes != null) {
          paths.remove(i);
          if (occurences < MAX_RECURSION_DEPTH) {
            paths.addAll(pathPrefixes);
          }
        } else {
          i++;
        }
      }
    }

    // TODO: This code needs to be optimized.
    final List<MemoryAccessPath> result = new ArrayList<>(paths.size());
    for (final MemoryAccessPath path : paths) {
      final Collection<MmuSegment> segments = MemoryAccess.getPossibleSegments(path);

      if (segments.isEmpty()) {
        continue;
      }

      result.add(path);
    }

    return result;
  }

  private static int increment(final Map<String, Integer> observed, final String key) {
    Integer n = observed.get(key);
    if (n == null) {
      n = 0;
    }
    observed.put(key, n + 1);

    return n;
  }

  /**
   * Elongates the memory access path.
   * 
   * @param type the memory access type or {@code null}.
   * @param path the memory access path to be elongated.
   * @return the list of all possible elongations of the given memory access path.
   */
  private List<MemoryAccessPath> elongatePath(
      final MemoryAccessType type, final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    final MmuTransition last = path.getLastTransition();
    final MmuAction target = last.getTarget();
    final List<MmuTransition> out = memory.getTransitions(target);

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

    return null;
  }
}
