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
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
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
   * @param accessType the memory access type or {@code null}.
   * @return all memory access paths
   */
  public List<MemoryAccessPath> getAccesses(final MemoryAccessType accessType) {
    final List<MemoryAccessPath> paths = new ArrayList<>();
    final List<MmuTransition> out = memory.getTransitions(memory.getStartAction());

    if (out != null && !out.isEmpty()) {
      for (final MmuTransition next : out) {
        final MmuGuard guard = next.getGuard();

        if (!next.isEnabled()) {
          continue;
        }

        if (accessType == null || guard.getOperation() == accessType.getOperation()) {
          final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

          builder.add(next);
          paths.add(builder.build());
        }
      }

      int i = 0;
      while (i < paths.size()) {
        final List<MemoryAccessPath> pathPrefixes = elongatePath(accessType, paths.get(i));

        if (pathPrefixes != null) {
          paths.remove(i);
          paths.addAll(pathPrefixes);
        } else {
          i++;
        }
      }
    }

    return paths;
  }

  /**
   * Elongates the memory access path.
   * 
   * @param accessType the memory access type.
   * @param accessPath the memory access path to be elongated.
   * @return the list of all possible elongations of the given memory access path.
   */
  private List<MemoryAccessPath> elongatePath(
      final MemoryAccessType accessType, final MemoryAccessPath accessPath) {
    InvariantChecks.checkNotNull(accessType);
    InvariantChecks.checkNotNull(accessPath);

    final MmuTransition last = accessPath.getLastTransition();
    final MmuAction target = last.getTarget();
    final List<MmuTransition> out = memory.getTransitions(target);

    if (out != null && !out.isEmpty()) {
      final List<MemoryAccessPath> elongatedPaths = new ArrayList<>();

      for (final MmuTransition next : out) {
        if (!next.isEnabled()) {
          continue;
        }

        final MmuGuard guard = next.getGuard();
        final MemoryOperation operation = guard != null ? guard.getOperation() : null;

        if (operation == null || accessType == null || operation == accessType.getOperation()) {
          final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

          builder.addAll(accessPath.getTransitions());
          builder.add(next);

          elongatedPaths.add(builder.build());
        }
      }

      return elongatedPaths;
    }

    return null;
  }
}
