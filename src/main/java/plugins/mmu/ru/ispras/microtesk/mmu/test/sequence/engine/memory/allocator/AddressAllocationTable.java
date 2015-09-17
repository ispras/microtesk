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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationStrategyId;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationTable;
import ru.ispras.microtesk.utils.Range;

/**
 * {@link AddressAllocationTable} implements a region-sensitive allocation table. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressAllocationTable {
  private static final int ALLOC_TABLE_SIZE(int width) {
    final int defaultSize;

    if (width < 16) {
      defaultSize = 64; 
    } else if (width < 32) {
      defaultSize = 128;
    } else {
      defaultSize = 256;
    }

    final int maximumSize = width >= Integer.SIZE - 1 ? Integer.MAX_VALUE : (1 << width);
    return maximumSize < defaultSize ? maximumSize : defaultSize;
  }

  /**
   * Returns the ranges specifying the zero fields of the mask.
   * 
   * @param mask the mask to be analyzed.
   * @return the list of ranges.
   */
  private static List<IntegerRange> getZeroFieldRanges(final long mask) {
    final List<IntegerRange> result = new ArrayList<>();

    int j = -1;
    for (int i = 0; i <= Long.SIZE; i++) {
      if (i < Long.SIZE && (mask & (1L << i)) == 0) {
        if (j == -1) {
          j = i;
        }
      } else {
        if (j != -1) {
          result.add(new IntegerRange(j, i - 1));
          j = -1;
        }
      }
    }

    return result;
  }

  /**
   * Transforms the given value so as to assign the zero bits of the mask and applies the mask.
   * 
   * @param value the value to be transformed.
   * @param fieldMask the OR-mask to be applied.
   * @param placeMask the mask whose zero bits indicate places where to put a value.
   * @return the masked value.
   */
  private static long getMaskedValue(final long value, final long fieldMask, final long placeMask) {
    final List<IntegerRange> zeroFieldRanges = getZeroFieldRanges(placeMask);

    long result = 0;
    long remain = value;

    for (final IntegerRange zeroFieldRange : zeroFieldRanges) {
      final int lower = zeroFieldRange.getMin().intValue();
      final int upper = zeroFieldRange.getMax().intValue();
      final int width = (upper - lower) + 1;

      result |= BitUtils.getField(remain, 0, width - 1) << lower;
      remain >>>= width;

      if (remain == 0) {
        break;
      }
    }

    InvariantChecks.checkTrue(remain == 0, "Some data cannot be set into the given places");
    return result | fieldMask;
  }

  /**
   * Returns values to be allocated for the given address field.
   * 
   * @param width the width of the field.
   * @param fieldMask the OR-mask to generate correct values.
   * @param placeMask the mask whose zero bits indicate places where to put a value.
   * @return the set of values.
   */
  private static Set<Long> getAddressFieldValues(
      final int width, final long fieldMask, final long placeMask) {
    InvariantChecks.checkTrue(width > 0);

    final int size = ALLOC_TABLE_SIZE(width);
    InvariantChecks.checkTrue(size > 0, "Incorrect allocation table size");

    final Set<Long> values = new LinkedHashSet<>();
    for (int i = 0; i < size; i++) {
      final long value = getMaskedValue(i, fieldMask, placeMask);
      values.add(value);
    }

    return values;
  }

  private static long allocate(
      final AllocationTable<Long, ?> allocTable, final boolean peek, final Set<Long> exclude) {
    InvariantChecks.checkNotNull(allocTable);

    return peek ?
        (exclude != null ? allocTable.peek(exclude) : allocTable.peek()) :
        (exclude != null ? allocTable.allocate(exclude) : allocTable.allocate());
  }

  /** Joint allocation table for all memory regions. */
  private final AllocationTable<Long, ?> globalAllocTable;
  /** Disjoint allocation tables for individual memory regions. */
  private final Map<Range<Long>, AllocationTable<Long, ?>> regionAllocTables = new HashMap<>();

  /**
   * Creates an allocation table for the given address field.
   * 
   * @param lower the lower bit of the field.
   * @param upper the upper bit of the field.
   * @param addressMask the mask indicating insignificant address bits.
   * @param regions the memory regions or {@code null}.
   */
  public AddressAllocationTable(
      final int lower,
      final int upper,
      final long addressMask,
      final Collection<? extends Range<Long>> regions) {
    final Set<Long> globalValues = new HashSet<>();

    final int width = (upper - lower) + 1;
    final long mask = BitUtils.getLongMask(width);

    final boolean isInsignificant = ((mask << lower) | addressMask) != addressMask;

    final AllocationStrategyId strategy =
        isInsignificant ? AllocationStrategyId.RANDOM : AllocationStrategyId.FREE;

    if (isInsignificant) {
      globalValues.addAll(Collections.singleton(0L));
    } else if (regions == null || regions.isEmpty()) {
      globalValues.addAll(getAddressFieldValues(width, 0, ~mask));
    } else {
      for (final Range<Long> region : regions) {
        final long regionMinField = BitUtils.getField(region.getMin(), lower, upper);
        final long regionMaxField = BitUtils.getField(region.getMax(), lower, upper);
        InvariantChecks.checkTrue(regionMinField <= regionMaxField);

        final Set<Long> regionValues =
            getAddressFieldValues(width, regionMinField, ~(regionMinField ^ regionMaxField));
        InvariantChecks.checkFalse(regionValues.isEmpty(), "Empty set of local values");

        globalValues.addAll(regionValues);

        final AllocationTable<Long, ?> regionAllocTable =
            new AllocationTable<>(strategy, regionValues);

        regionAllocTables.put(region, regionAllocTable);
      }
    }

    InvariantChecks.checkFalse(globalValues.isEmpty(), "Empty set of global values");
    this.globalAllocTable = new AllocationTable<>(strategy, globalValues);
  }

  /**
   * Allocates an address field value for the given region.
   * 
   * @param region the region or {@code null}.
   * @param peek if {@code peek == true}, peek an address without allocation.
   * @param exclude the values to be excluded or {@code null}.
   * @return an allocated address field.
   */
  public long allocate(final Range<Long> region, final boolean peek, final Set<Long> exclude) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);

    final long address = allocate(allocTable, peek, exclude);

    if (!peek) {
      globalAllocTable.use(address);
    }

    return address;
  }

  public void reset() {
    globalAllocTable.reset();
    for (final AllocationTable<Long, ?> allocTable : regionAllocTables.values()) {
      allocTable.reset();
    }
  }

  public Collection<Long> getFreeAddresses(final Range<Long> region) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);
    return allocTable.getFreeObjects();
  }

  public Collection<Long> getUsedAddresses(final Range<Long> region) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);
    return allocTable.getUsedObjects();
  }

  public Collection<Long> getAllAddresses(final Range<Long> region) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);
    return allocTable.getAllObjects();
  }

  private AllocationTable<Long, ?> getAllocTable(final Range<Long> region) {
    if (region != null) {
      final AllocationTable<Long, ?> allocTable = regionAllocTables.get(region);

      if (allocTable != null) {
        return allocTable;
      }
    }

    return globalAllocTable;
  }
}
