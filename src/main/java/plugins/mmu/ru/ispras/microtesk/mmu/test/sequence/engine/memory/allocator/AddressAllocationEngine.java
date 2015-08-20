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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.utils.Range;

/**
 * {@link AddressAllocationEngine} allocates a part (tag, index, etc.) of an address of a given
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
public final class AddressAllocationEngine {
  /**
   * Maps a known part of the address to the allocation table for the next unknown element.
   * 
   * <p>Provided that {@code address[bit-1:0] == value}, {@code allocators.get(bit).get(value)} is
   * the allocation table for {@code address[next(bit):bit]}.</p>
   */
  private final Map<Integer, Map<Long, AddressAllocationTable>> allocators = new HashMap<>();

  /** Contains all elements. */
  private final List<IntegerRange> allRanges;

  /** Maps an address field into the list of the elements. */
  private final Map<IntegerField, List<IntegerRange>> fieldRanges = new HashMap<>();

  /** Masks insignificant address bits (e.g., offset). */
  private final long mask;

  /** Memory regions. */
  private final Collection<? extends Range<Long>> regions;

  /**
   * Constructs an address allocator.
   * 
   * @param addressType the address type.
   * @param expressions the set of all expressions over addresses used in memory buffers.
   * @param mask the mask to reset insignificant address bits (e.g., offset).
   * @param regions the memory regions or {@code null}.
   */
  public AddressAllocationEngine(
      final MmuAddressType addressType,
      final Collection<MmuExpression> expressions,
      final long mask,
      final Collection<? extends Range<Long>> regions) {
    InvariantChecks.checkNotNull(expressions);

    final Collection<IntegerRange> expressionRanges = new HashSet<>();
    expressionRanges.add(new IntegerRange(0, addressType.getWidth() - 1));

    for (final MmuExpression expression : expressions) {
      for (final IntegerField field : expression.getTerms()) {
        final IntegerRange range = new IntegerRange(field.getLoIndex(), field.getHiIndex());
        expressionRanges.add(range);
      }
    }

    this.allRanges = IntegerRange.divide(expressionRanges);

    for (final MmuExpression expression : expressions) {
      for (final IntegerField field : expression.getTerms()) {
        if (this.fieldRanges.containsKey(field)) {
          continue;
        }

        final List<IntegerRange> ranges = new ArrayList<>();

        for (final IntegerRange range : allRanges) {
          if (range.getMax().intValue() < field.getLoIndex()) {
            continue;
          }
          if (range.getMin().intValue() > field.getHiIndex()) {
            break;
          }

          ranges.add(range);
        }

        this.fieldRanges.put(field, ranges);
      }
    }

    this.mask = mask;
    this.regions = regions;
  }

  /**
   * Allocates an address for the given partial address and the region.
   * 
   * @param partialAddress the partial address.
   * @param region the memory region.
   * @param peek if {@code peek == true}, peek address without allocation.
   * @param exclude the set of addresses whose fields to be excluded.
   * @return an allocated field.
   */
  public long allocate(
      final long partialAddress,
      final Range<Long> region,
      final boolean peek,
      final Set<Long> exclude) {
    return allocate(allRanges, partialAddress, region, peek, exclude);
  }

  /**
   * Allocates an address field for the given partial address and the region.
   * 
   * @param expression the expression defining the field to be allocated.
   * @param partialAddress the partial address.
   * @param region the memory region.
   * @param peek if {@code peek == true}, peek address without allocation.
   * @param exclude the set of addresses whose fields to be excluded.
   * @return an allocated field.
   */
  public long allocate(
      final MmuExpression expression,
      final long partialAddress,
      final Range<Long> region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(expression);

    final List<IntegerRange> ranges = getRanges(expression);
    return allocate(ranges, partialAddress, region, peek, exclude);
  }

  private long allocate(
      final List<IntegerRange> ranges,
      final long partialAddress,
      final Range<Long> region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(ranges);

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
      final long prevFieldsValue =
          lower == 0 ? 0 : (maskedAddress & BitUtils.getLongMask(lower - 1));

      AddressAllocationTable allocTable = fieldAllocator.get(prevFieldsValue);
      if (allocTable == null) {
        fieldAllocator.put(prevFieldsValue,
            allocTable = new AddressAllocationTable(lower, upper, mask, regions));
      }

      final long fieldMask = BitUtils.getLongMask(width);
      final Set<Long> excludeFields = exclude != null ? new HashSet<Long>() : null;

      if (exclude != null) {
        for (final long excludedAddress : exclude) {
          final long excludedField = (excludedAddress >> lower) & fieldMask;
          excludeFields.add(excludedField);
        }
      }

      final long fieldValue = allocTable.allocate(region, peek, excludeFields);

      // Insignificant bits are taken from the partial address.
      if (((fieldMask << lower) | mask) == mask) {
        BitUtils.setField(address, lower, upper, fieldValue);
      }
    }

    return address;
  }

  public void reset() {
    for (final Map<Long, AddressAllocationTable> map : allocators.values()) {
      for (final AddressAllocationTable allocTable : map.values()) {
        allocTable.reset();
      }
    }
  }

  public Collection<Long> getAllAddresses(final Range<Long> region) {
    // Peek an address to initialize allocation tables.
    allocate(allRanges, 0, region, true, null);

    final Map<Integer, Collection<Long>> partialAddresses = new LinkedHashMap<>();

    for (final IntegerRange range : allRanges) {
      final int lower = range.getMin().intValue();

      final Map<Long, AddressAllocationTable> fieldAllocator = allocators.get(lower);
      InvariantChecks.checkNotNull(fieldAllocator);

      final AddressAllocationTable allocTable = fieldAllocator.values().iterator().next();
      InvariantChecks.checkNotNull(allocTable);

      partialAddresses.put(lower, allocTable.getAllAddresses(region));
    }

    // Produce the Cartesian product of the partial address sets.
    Collection<Long> addresses = new ArrayList<>();
    addresses.add(0L);

    for (final Map.Entry<Integer, Collection<Long>> entry : partialAddresses.entrySet()) {
      final int offset = entry.getKey();
      final Collection<Long> fields = entry.getValue();

      addresses = product(addresses, fields, offset);
    }

    return addresses;
  }

  private List<IntegerRange> getRanges(final MmuExpression expression) {
    InvariantChecks.checkNotNull(expression);

    final List<IntegerRange> result = new ArrayList<>();
    for (final IntegerField field : expression.getTerms()) {
      result.addAll(fieldRanges.get(field));
    }

    return result;
  }

  private Collection<Long> product(
      final Collection<Long> addresses, final Collection<Long> fields, final int offset) {
    InvariantChecks.checkNotNull(addresses);
    InvariantChecks.checkNotNull(fields);

    final Collection<Long> result = new ArrayList<>(addresses.size() * fields.size());

    for (final long address : addresses) {
      for (final long field : fields) {
        result.add(address | (field << offset));
      }
    }

    return result;
  }
}
