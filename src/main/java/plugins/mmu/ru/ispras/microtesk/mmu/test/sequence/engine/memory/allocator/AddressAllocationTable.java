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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationTable;

/**
 * {@link AddressAllocationTable} implements a region-sensitive allocation table. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressAllocationTable {
  private static final int ALLOC_TABLE_SIZE = 64;

  /**
   * Returns the ranges specifying the zero fields of the mask.
   * 
   * @param mask the mask to be analyzed.
   * @return the list of ranges.
   */
  private static List<IntegerRange> getZeroFields(final long mask) {
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
    final List<IntegerRange> zeroFields = getZeroFields(placeMask);

    long result = 0;
    long remain = value;

    for (final IntegerRange range : zeroFields) {
      result |= remain << range.getMin().intValue();
      remain >>>= range.getMax().intValue() + 1;

      if (remain == 0) {
        break;
      }
    }

    InvariantChecks.checkTrue(remain == 0);
    return (result & ~placeMask) | fieldMask;
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

    final int count = (1 << width) < ALLOC_TABLE_SIZE ? (1 << width) : ALLOC_TABLE_SIZE;

    final Set<Long> values = new LinkedHashSet<>();
    for (int i = 0; i < count; i++) {
      final long value = getMaskedValue(i, fieldMask, placeMask);
      values.add(value);
    }

    return values;
  }

  /**
   * Returns the mask whose zero bits indicates the fields' bits that can be modified.
   * 
   * @param width the width of the field.
   * @param fields the set of field values.
   * 
   * @return the place mask.
   */
  private static long getPlaceMask(final int width, final Set<Long> fields) {
    InvariantChecks.checkTrue(width > 0);
    InvariantChecks.checkNotNull(fields);

    int i = 0;
    for (; i < width; i++) {
      for (final long field : fields) {
        if ((field & (1L << i)) != 0) {
          break;
        }
      }
    }

    return i == Long.SIZE ? -1L : ~((1L << i) - 1);
  }

  private static long allocate(
      final AllocationTable<Long, ?> allocTable, boolean peek, final Set<Long> exclude) {
    InvariantChecks.checkNotNull(allocTable);

      return peek ?
          (exclude != null ? allocTable.peek(exclude) : allocTable.peek()) :
          (exclude != null ? allocTable.allocate(exclude) : allocTable.allocate());
  }

  /** Joint allocation table for all memory regions. */
  private final AllocationTable<Long, ?> globalAllocTable;
  /** Disjoint allocation tables for individual memory regions. */
  private final Map<String, AllocationTable<Long, ?>> regionAllocTables = new HashMap<>();

  /**
   * Creates an allocation table for the given address field.
   * 
   * @param lo the lower bit of the field.
   * @param hi the upper bit of the field.
   * @param addressMask the mask indicating insignificant address bits.
   * @param regions the memory regions or {@code null}.
   */
  public AddressAllocationTable(
      final int lo,
      final int hi,
      final long addressMask,
      final Collection<RegionSettings> regions) {
    final Set<Long> globalValues = new HashSet<>();

    final int width = (hi - lo) + 1;
    final long mask = width == Long.SIZE ? -1L : (1L << width) - 1;

    if (((mask << lo) | addressMask) != addressMask) {
      globalValues.addAll(Collections.singleton(0L));
    } else if (regions == null || regions.isEmpty()) {
      globalValues.addAll(getAddressFieldValues(width, 0, ~mask));
    } else {
      final Set<Long> regionFields = new HashSet<>();

      for (final RegionSettings region : regions) {
        final long regionField = (region.getStartAddress() >> lo) & mask;
        regionFields.add(regionField);
      }

      // Two possibilities: all regions have the same field (region-insensitive field allocation);
      // each region has a unique field (region-sensitive field allocation).
      InvariantChecks.checkTrue(regionFields.size() == 1 || regionFields.size() == regions.size());

      for (final RegionSettings region : regions) {
        final long regionField = (region.getStartAddress() >> lo) & mask;
        final Set<Long> regionValues =
            getAddressFieldValues(width, regionField, getPlaceMask(width, regionFields));

        globalValues.addAll(regionValues);
        if (regionFields.size() == regions.size()) {
          final AllocationTable<Long, ?> regionAllocTable = new AllocationTable<>(regionValues);
          regionAllocTables.put(region.getName(), regionAllocTable);
        }
      }
    }

    this.globalAllocTable = new AllocationTable<>(globalValues);
  }

  /**
   * Allocates an address field value for the given region.
   * 
   * @param region the region or {@code null}.
   * @param peek if {@code peek == true}, peek an address without allocation.
   * @param exclude the values to be excluded or {@code null}.
   * @return an allocated address field.
   */
  public long allocate(final RegionSettings region, final boolean peek, final Set<Long> exclude) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);

    final long address = allocate(allocTable, peek, exclude);
    globalAllocTable.use(address);

    return address;
  }

  public Collection<Long> getFreeAddresses(final RegionSettings region) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);
    return allocTable.getFreeObjects();
  }

  public Collection<Long> getUsedAddresses(final RegionSettings region) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);
    return allocTable.getUsedObjects();
  }

  public Collection<Long> getAllAddresses(final RegionSettings region) {
    final AllocationTable<Long, ?> allocTable = getAllocTable(region);
    return allocTable.getAllObjects();
  }

  private AllocationTable<Long, ?> getAllocTable(final RegionSettings region) {
    if (region != null) {
      final AllocationTable<Long, ?> allocTable = regionAllocTables.get(region.getName());

      if (allocTable != null) {
        return allocTable;
      }
    }

    return globalAllocTable;
  }
}
