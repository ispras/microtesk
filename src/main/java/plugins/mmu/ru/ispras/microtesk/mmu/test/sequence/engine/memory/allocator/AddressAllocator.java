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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.RegionSettings;

/**
 * {@link AddressAllocator} implements a region-sensitive address (tag, index, etc.) allocator for
 * memory subsystem buffers. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressAllocator {
  private final Map<MmuAddressType, AddressAllocationEngine> allocators = new HashMap<>();

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

      final AddressAllocationEngine allocator = new AddressAllocationEngine(
          addressType, addressExpressions, addressMask, addressRegions);
      allocators.put(addressType, allocator);
    }
  }

  public long allocateTag(
      final MmuBuffer buffer,
      final long partialAddress,
      final RegionSettings region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(buffer);
    // Parameters {@code region} and {@code exclude} can be null.

    return allocate(
        buffer.getAddress(), buffer.getTagExpression(), partialAddress, region, peek, exclude);
  }

  public long allocateIndex(
      final MmuBuffer buffer,
      final long partialAddress,
      final RegionSettings region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(buffer);
    // Parameters {@code region} and {@code exclude} can be null.

    return allocate(
        buffer.getAddress(), buffer.getIndexExpression(), partialAddress, region, peek, exclude);
  }

  public long allocateAddress(
      final MmuAddressType address,
      final long partialAddress,
      final RegionSettings region,
      final boolean peek) {
    InvariantChecks.checkNotNull(address);
    // Parameters {@code region} can be null.

    return allocators.get(address).allocate(partialAddress, region, peek, null);
  }

  private long allocate(
      final MmuAddressType address,
      final MmuExpression expression,
      final long partialAddress,
      final RegionSettings region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(expression);
    // Parameters {@code region} and {@code exclude} can be null.

    return allocators.get(address).allocate(expression, partialAddress, region, peek, exclude);
  }

  public void reset() {
    for (final AddressAllocationEngine allocator : allocators.values()) {
      allocator.reset();
    }
  }

  public Collection<Long> getAllAddresses(
      final MmuAddressType address, final RegionSettings region) {
    InvariantChecks.checkNotNull(address);
    // Parameters {@code region} and {@code exclude} can be null.

    return allocators.get(address).getAllAddresses(region);
  }
}
