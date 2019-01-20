/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.model.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.engine.allocator.AllocationData;
import ru.ispras.microtesk.test.engine.allocator.AllocationTable;
import ru.ispras.microtesk.test.engine.allocator.Allocator;
import ru.ispras.microtesk.test.engine.allocator.ResourceOperation;
import ru.ispras.microtesk.utils.function.Supplier;

/**
 * {@link EntryIdAllocator} implements an allocator of entry identifiers (indices) for
 * non-replaceable (non-transparent) buffers.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EntryIdAllocator {
  private static final int MAX_EXPLICIT_DOMAIN = 1024;

  private final Map<MmuBuffer, AllocationTable<BitVector, ?>> allocators = new LinkedHashMap<>();

  public EntryIdAllocator(final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(settings);

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    for (final MmuBuffer buffer : memory.getBuffers()) {
      if (buffer.isReplaceable()) {
        continue;
      }

      final BigInteger min;
      final BigInteger max;

      if (buffer.getKind() == MmuBuffer.Kind.MEMORY) {
        final RegionSettings region = settings.getMemory().getRegion(buffer.getName());
        InvariantChecks.checkNotNull(region);

        min = region.getStartAddress();
        max = region.getEndAddress();
      } else {
        min = BigInteger.ZERO;
        max = BigInteger.valueOf(buffer.getSets() * buffer.getWays() - 1);
      }

      final BigInteger size = max.subtract(min).add(BigInteger.ONE);
      final AllocationTable<BitVector, ?> allocator;

      if (size.compareTo(BigInteger.valueOf(MAX_EXPLICIT_DOMAIN)) < 0) {
        // Construct the set of possible entry identifiers.
        final Set<BitVector> entryIds = new LinkedHashSet<>();

        for (long i = 0; i < size.longValue(); i++) {
          final BitVector value =
              BitVector.valueOf(
                  min.add(BigInteger.valueOf(i)),
                  buffer.getAddress().getWidth());
          entryIds.add(value);
        }

        // Construct the allocation table.
        allocator = new AllocationTable<>(
            new AllocationData<BitVector>(Allocator.TRY_FREE, entryIds),
            entryIds);
      } else {
        allocator = new AllocationTable<>(
            new AllocationData<BitVector>(Allocator.TRY_FREE),
            new Supplier<BitVector>() {
              @Override
              public BitVector get() {
                return BitVector.valueOf(
                    Randomizer.get().nextBigIntegerRange(min, max),
                    buffer.getAddress().getWidth());
              }
            });
      }

      allocators.put(buffer, allocator);
    }
  }

  public BitVector allocate(
      final MmuBuffer buffer,
      final boolean peek,
      final Set<BitVector> exclude) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkTrue(!buffer.isReplaceable());
    InvariantChecks.checkNotNull(exclude);

    final AllocationTable<BitVector, ?> allocator = allocators.get(buffer);

    if (peek) {
      return allocator.peek(
          exclude,
          Collections.<BitVector>emptySet(),
          Collections.<ResourceOperation, Integer>emptyMap());
    } else {
      return allocator.allocate(ResourceOperation.WRITE,
          exclude,
          Collections.<BitVector>emptySet(),
          Collections.<ResourceOperation, Integer>emptyMap());
    }
  }

  public void reset() {
    for (final AllocationTable<BitVector, ?> allocator : allocators.values()) {
      allocator.reset();
    }
  }
}
