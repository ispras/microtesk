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
import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerRange;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationTable;

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
  private static final int ALLOC_TABLE_SIZE = 64;

  /**
   * Returns a set of values for a field of the given bit width.
   * 
   * @param width the bit width.
   * @return the set of values.
   */
  private static Set<Long> getFieldValues(final int width) {
    final int size = (1 << width) < ALLOC_TABLE_SIZE ? (1 << width) : ALLOC_TABLE_SIZE;

    final Set<Long> values = new LinkedHashSet<>();
    for (int i = 0; i < size; i++) {
      final long value = i;
      values.add(value);
    }

    return values;
  }

  /**
   * Maps a known part of the address to the allocation table for the next unknown element.
   * 
   * <p>Provided that {@code address[bit-1:0] == value}, {@code allocators.get(bit).get(value)} is
   * the allocation table for {@code address[next(bit):bit]}.</p>
   */
  private final Map<Integer, Map<Long, AllocationTable<Long, ?>>> allocators = new HashMap<>();

  /** Maps an address field into the list of the elements. */
  private final Map<IntegerField, List<IntegerRange>> fieldRanges = new HashMap<>();

  /** Masks insignificant address bits (e.g., offset). */
  private final long mask;

  /**
   * Constructs an address allocator.
   * 
   * @param addressType the address type.
   * @param expressions the set of all expressions over addresses used in memory buffers.
   * @param mask the mask to reset insignificant address bits (e.g., offset).
   */
  public SingleAddressTypeAllocator(
      final MmuAddressType addressType,
      final Collection<MmuExpression> expressions,
      final long mask) {
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
  }

  public long allocate(final MmuExpression expression, final long partialAddress) {
    InvariantChecks.checkNotNull(expression);

    final List<IntegerRange> ranges = getRanges(expression);

    long address = partialAddress;

    for (final IntegerRange range : ranges) {
      final int lower = range.getMin().intValue();
      final int upper = range.getMax().intValue();
      final int width = (upper - lower) + 1;

      Map<Long, AllocationTable<Long, ?>> fieldAllocator = allocators.get(lower);
      if (fieldAllocator == null) {
        allocators.put(lower, fieldAllocator = new HashMap<>());
      }

      final long maskedAddress = (address & mask);
      final long prevFieldsValue = lower == 0 ? 0 : (maskedAddress & ((1L << (lower - 1)) - 1));

      AllocationTable<Long, ?> allocationTable = fieldAllocator.get(prevFieldsValue);
      if (allocationTable == null) {
        final Set<Long> values = getFieldValues(width);
        fieldAllocator.put(prevFieldsValue, allocationTable = new AllocationTable<>(values));
      }

      final long fieldValue = allocationTable.allocate();

      address &= ~(((1L << width) - 1) << lower);
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
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class AddressAllocator {
  private final Map<MmuAddressType, SingleAddressTypeAllocator> allocators = new HashMap<>();

  public AddressAllocator(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);

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

      final SingleAddressTypeAllocator allocator =
          new SingleAddressTypeAllocator(addressType, addressExpressions, addressMask);

      allocators.put(addressType, allocator);
    }
  }

  public long allocateTag(final MmuBuffer buffer, final long partialAddress) {
    InvariantChecks.checkNotNull(buffer);
    return allocate(buffer.getAddress(), buffer.getTagExpression(), partialAddress);
  }

  public long allocateIndex(final MmuBuffer buffer, final long partialAddress) {
    InvariantChecks.checkNotNull(buffer);
    return allocate(buffer.getAddress(), buffer.getIndexExpression(), partialAddress);
  }

  public long allocate(
      final MmuAddressType address, final MmuExpression expression, final long partialAddress) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(expression);

    return allocators.get(address).allocate(expression, partialAddress);
  }
}
