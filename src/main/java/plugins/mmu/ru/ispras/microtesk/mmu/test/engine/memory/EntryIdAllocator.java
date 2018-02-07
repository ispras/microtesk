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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.engine.allocator.AllocationStrategyId;
import ru.ispras.microtesk.test.engine.allocator.AllocationTable;
import ru.ispras.microtesk.utils.function.Supplier;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        allocator = new AllocationTable<>(AllocationStrategyId.TRY_FREE, entryIds);
      } else {
        allocator = new AllocationTable<>(
            AllocationStrategyId.TRY_FREE,
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
    // Parameter exclude can be null.

    final AllocationTable<BitVector, ?> allocator = allocators.get(buffer);
    return peek ?
        (exclude != null ? allocator.peek(exclude) : allocator.peek()) :
        (exclude != null ? allocator.allocate(exclude) : allocator.allocate());
  }

  public void reset() {
    for (final AllocationTable<BitVector, ?> allocator : allocators.values()) {
      allocator.reset();
    }
  }
}
