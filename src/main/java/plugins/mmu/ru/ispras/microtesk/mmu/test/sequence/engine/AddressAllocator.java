/*
 * Copyright 2006-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationTable;

/**
 * {@link AddressAllocationTable} implements a region-sensitive allocation table. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class AddressAllocationTable {
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

  private static long allocate(final AllocationTable<Long, ?> allocTable, final Set<Long> exclude) {
    InvariantChecks.checkNotNull(allocTable);
    return exclude != null ? allocTable.allocate(exclude) : allocTable.allocate();
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
   * @param partialAddress the value of the previous fields ({@code address[lo-1:0]}).
   * @param regions the memory regions or {@code null}.
   */
  public AddressAllocationTable(
      final int lo,
      final int hi,
      final long partialAddress,
      final Collection<RegionSettings> regions) {

    final Set<Long> globalValues = new HashSet<>();

    final int width = (hi - lo) + 1;
    final long mask = width == Long.SIZE ? -1L : (1L << width) - 1;

    if (regions == null || regions.isEmpty()) {
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
   * @param exclude the values to be excluded or {@code null}.
   * @return an allocated address field.
   */
  public long allocate(final RegionSettings region, final Set<Long> exclude) {
    if (region != null) {
      final AllocationTable<Long, ?> allocationTable = regionAllocTables.get(region.getName());

      if (allocationTable != null) {
        final long address = allocate(allocationTable, exclude);
        globalAllocTable.use(address);
      }
    }

    return allocate(globalAllocTable, exclude);
  }
}

/**
 * {@link SingleAddressTypeAllocator} allocates a part (tag, index, etc.) of an address of a given
 * type (the rest of the address is assumed to be known).
 * 
 * <p>Address is represented as a set of disjoint fields (elements). For each element, a finite
 * domain is provided.</p>
 * 
 * <p>An address part is a subset of the address elements. Given the values of the rest elements,
 * allocation is a construction of a unique combination of values for the part elements.</p>
 * 
 * <p>To avoid possible conflicts, the allocator works as follows. The elements are ordered.
 * Provided that elements {@code E[1], ..., E[k]} are defined, it chooses a new value for the
 * the element {@code E[k+1]}.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class SingleAddressTypeAllocator {
  /**
   * Maps a known part of the address to the allocation table for the next unknown element.
   * 
   * <p>Provided that {@code address[bit-1:0] == value}, {@code allocators.get(bit).get(value)} is
   * the allocation table for {@code address[next(bit):bit]}.</p>
   */
  private final Map<Integer, Map<Long, AddressAllocationTable>> allocators = new HashMap<>();

  /** Maps an address field into the list of the elements. */
  private final Map<IntegerField, List<IntegerRange>> fieldRanges = new HashMap<>();

  /** Masks insignificant address bits (e.g., offset). */
  private final long mask;

  /** Memory regions. */
  private final Collection<RegionSettings> regions;

  /**
   * Constructs an address allocator.
   * 
   * @param addressType the address type.
   * @param expressions the set of all expressions over addresses used in memory buffers.
   * @param mask the mask to reset insignificant address bits (e.g., offset).
   * @param regions the memory regions or {@code null}.
   */
  public SingleAddressTypeAllocator(
      final MmuAddressType addressType,
      final Collection<MmuExpression> expressions,
      final long mask,
      final Collection<RegionSettings> regions) {
    InvariantChecks.checkNotNull(expressions);

    final Collection<IntegerRange> expressionRanges = new HashSet<>();
    expressionRanges.add(new IntegerRange(0, addressType.getWidth() - 1));

    for (final MmuExpression expression : expressions) {
      for (final IntegerField field : expression.getTerms()) {
        final IntegerRange range = new IntegerRange(field.getLoIndex(), field.getHiIndex());
        expressionRanges.add(range);
      }
    }

    final List<IntegerRange> disjointRanges = IntegerRange.divide(expressionRanges);

    for (final MmuExpression expression : expressions) {
      for (final IntegerField field : expression.getTerms()) {
        if (fieldRanges.containsKey(field)) {
          continue;
        }

        final List<IntegerRange> ranges = new ArrayList<>();

        for (final IntegerRange range : disjointRanges) {
          if (range.getMax().intValue() < field.getLoIndex()) {
            continue;
          }
          if (range.getMin().intValue() > field.getHiIndex()) {
            break;
          }

          ranges.add(range);
        }

        fieldRanges.put(field, ranges);
      }
    }

    this.mask = mask;
    this.regions = regions;
  }

  /**
   * Allocates an address field for the given partial address and the region.
   * 
   * @param expression the expression defining the field to be allocated.
   * @param partialAddress the partial address.
   * @param region the memory region.
   * @param exclude the set of addresses whose fields to be excluded.
   * @return an allocated field.
   */
  public long allocate(
      final MmuExpression expression,
      final long partialAddress,
      final RegionSettings region,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(expression);

    final List<IntegerRange> ranges = getRanges(expression);

    long address = partialAddress;

    for (final IntegerRange range : ranges) {
      final int lower = range.getMin().intValue();
      final int upper = range.getMax().intValue();
      final int width = (upper - lower) + 1;

      Map<Long, AddressAllocationTable> fieldAllocator = allocators.get(lower);
      if (fieldAllocator == null) {
        allocators.put(lower, fieldAllocator = new HashMap<>());
      }

      final long maskedAddress = (address & mask);
      final long prevFieldsValue = lower == 0 ? 0 : (maskedAddress & ((1L << (lower - 1)) - 1));

      AddressAllocationTable allocationTable = fieldAllocator.get(prevFieldsValue);
      if (allocationTable == null) {
        fieldAllocator.put(prevFieldsValue,
            allocationTable = new AddressAllocationTable(lower, upper, maskedAddress, regions));
      }

      final long fieldMask = width == Long.SIZE ? -1L : (1L << width) - 1;
      final Set<Long> excludeFields = exclude != null ? new HashSet<Long>() : null;

      if (exclude != null) {
        for (final long excludedAddress : exclude) {
          excludeFields.add((excludedAddress >> lower) & fieldMask);
        }
      }

      final long fieldValue = allocationTable.allocate(region, excludeFields);

      address &= ~(fieldMask << lower);
      address |= (fieldValue << lower);
    }

    return address;
  }

  private List<IntegerRange> getRanges(final MmuExpression expression) {
    InvariantChecks.checkNotNull(expression);

    final List<IntegerRange> result = new ArrayList<>();
    for (final IntegerField field : expression.getTerms()) {
      result.addAll(fieldRanges.get(field));
    }

    return result;
  }
}

/**
 * {@link AddressAllocator} implements an address (tag, index, etc.) allocator for memory buffers. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressAllocator {
  private final Map<MmuAddressType, SingleAddressTypeAllocator> allocators = new HashMap<>();

  public AddressAllocator(
      final MmuSubsystem memory, final Map<MmuAddressType, Collection<RegionSettings>> regions) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(regions);

    final Map<MmuAddressType, Collection<MmuExpression>> expressions = new LinkedHashMap<>();
    final Map<MmuAddressType, Long> mask = new LinkedHashMap<>();

    for (final MmuAddressType addressType : memory.getAddresses()) {
      expressions.put(addressType, new LinkedHashSet<MmuExpression>());
      mask.put(addressType, 0L);
    }

    for (final MmuBuffer buffer : memory.getDevices()) {
      final MmuAddressType addressType = buffer.getAddress();
      final Collection<MmuExpression> addressExpressions = expressions.get(addressType);

      addressExpressions.add(buffer.getTagExpression());
      addressExpressions.add(buffer.getIndexExpression());
      addressExpressions.add(buffer.getOffsetExpression());

      long addressMask = mask.get(addressType);

      addressMask |= buffer.getTagMask();
      addressMask |= buffer.getIndexMask();
      mask.put(addressType, addressMask);
    }

    for (final Map.Entry<MmuAddressType, Collection<MmuExpression>> entry : expressions.entrySet()) {
      final MmuAddressType addressType = entry.getKey();
      final Collection<MmuExpression> addressExpressions = entry.getValue();
      final long addressMask = mask.get(addressType);
      final Collection<RegionSettings> addressRegions = regions.get(addressType);

      final SingleAddressTypeAllocator allocator = new SingleAddressTypeAllocator(
          addressType, addressExpressions, addressMask, addressRegions);

      allocators.put(addressType, allocator);
    }
  }

  public long allocateTag(
      final MmuBuffer buffer,
      final long partialAddress,
      final RegionSettings region,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(buffer);
    return allocate(
        buffer.getAddress(), buffer.getTagExpression(), partialAddress, region, exclude);
  }

  public long allocateIndex(
      final MmuBuffer buffer,
      final long partialAddress,
      final RegionSettings region,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(buffer);
    return allocate(
        buffer.getAddress(), buffer.getIndexExpression(), partialAddress, region, exclude);
  }

  public long allocate(
      final MmuAddressType address,
      final MmuExpression expression,
      final long partialAddress,
      final RegionSettings region,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(expression);

    return allocators.get(address).allocate(expression, partialAddress, region, exclude);
  }
}
