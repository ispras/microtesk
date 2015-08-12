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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationStrategyId;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationTable;

/**
 * {@link EntryIdAllocator} implements an  allocator of entry identifiers (indices).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EntryIdAllocator {
  private final Map<MmuBuffer, AllocationTable<Long, ?>> allocators = new HashMap<>();

  public EntryIdAllocator(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);

    for (final MmuBuffer buffer : memory.getBuffers()) {
      if (buffer.isReplaceable()) {
        continue;
      }

      // Construct the set of possible entry identifiers.
      final Set<Long> entryIds = new LinkedHashSet<>();
      for (long i = 0; i < buffer.getWays(); i++) {
        entryIds.add(i);
      }

      // Construct the allocation table.
      final AllocationTable<Long, ?> allocator = new AllocationTable<>(
          entryIds.size() > 1 ? AllocationStrategyId.FREE : AllocationStrategyId.RANDOM, entryIds);
      allocators.put(buffer, allocator);
    }
  }

  public long allocate(
      final MmuBuffer buffer,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkTrue(!buffer.isReplaceable());
    // Parameter {@code exclude} can be null.

    final AllocationTable<Long, ?> allocator = allocators.get(buffer);
    return peek ?
        (exclude != null ? allocator.peek(exclude) : allocator.peek()) :
        (exclude != null ? allocator.allocate(exclude) : allocator.allocate());
  }

  public void reset() {
    for (final AllocationTable<Long, ?> allocator : allocators.values()) {
      allocator.reset();
    }
  }
}
