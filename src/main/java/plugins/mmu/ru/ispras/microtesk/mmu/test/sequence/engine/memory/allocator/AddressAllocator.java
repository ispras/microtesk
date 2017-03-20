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
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.utils.Range;

/**
 * {@link AddressAllocator} implements a region-sensitive address (tag, index, etc.) allocator for
 * memory subsystem buffers. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressAllocator {
  private final Map<MmuAddressInstance, AddressAllocationEngine> allocators = new HashMap<>();
  private final Map<MmuAddressInstance, Long> masks = new LinkedHashMap<>();

  public AddressAllocator(
      final Map<MmuAddressInstance, Collection<? extends Range<Long>>> regions) {
    InvariantChecks.checkNotNull(regions);

    final MmuSubsystem memory = MmuPlugin.getSpecification();
    final Map<MmuAddressInstance, Collection<MmuExpression>> expressions = new LinkedHashMap<>();

    for (final MmuAddressInstance addressType : memory.getAddresses()) {
      expressions.put(addressType, new LinkedHashSet<MmuExpression>());
      masks.put(addressType, 0L);
    }

    for (final MmuBuffer buffer : memory.getBuffers()) {
      final MmuAddressInstance addressType = buffer.getAddress();
      final Collection<MmuExpression> addressExpressions = expressions.get(addressType);

      addressExpressions.add(buffer.getTagExpression());
      addressExpressions.add(buffer.getIndexExpression());
      addressExpressions.add(buffer.getOffsetExpression());

      long addressMask = masks.get(addressType);

      addressMask |= buffer.getTagMask();
      addressMask |= buffer.getIndexMask();
      masks.put(addressType, addressMask);
    }

    for (final Map.Entry<MmuAddressInstance, Collection<MmuExpression>> entry : expressions.entrySet()) {
      final MmuAddressInstance addressType = entry.getKey();
      final Collection<MmuExpression> addressExpressions = entry.getValue();
      final long addressMask = masks.get(addressType);
      final Collection<? extends Range<Long>> addressRegions = regions.get(addressType);

      final AddressAllocationEngine allocator = new AddressAllocationEngine(
          addressType, addressExpressions, addressMask, addressRegions);
      allocators.put(addressType, allocator);
    }
  }

  public long allocateTag(
      final MmuBuffer buffer,
      final long partialAddress,
      final Range<Long> region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(buffer);
    // Parameters {@code region} and {@code exclude} can be null.

    final MmuAddressInstance address = buffer.getAddress();
    final MmuExpression tagExpression = buffer.getTagExpression();

    final long result = allocate(address, tagExpression, partialAddress, region, peek, exclude);
    Logger.debug("Allocate tag: %s(%s)[%s] = 0x%x", buffer, address, tagExpression, result);

    return result;
  }

  public long allocateIndex(
      final MmuBuffer buffer,
      final long partialAddress,
      final Range<Long> region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(buffer);
    // Parameters {@code region} and {@code exclude} can be null.

    final MmuAddressInstance address = buffer.getAddress();
    final MmuExpression indexExpression = buffer.getIndexExpression();

    final long result = allocate(address, indexExpression, partialAddress, region, peek, exclude);
    Logger.debug("Allocate index: %s(%s)[%s] = 0x%x", buffer, address, indexExpression, result);

    return result;
  }

  public long allocateAddress(
      final MmuAddressInstance address,
      final long partialAddress,
      final Range<Long> region,
      final boolean peek) {
    InvariantChecks.checkNotNull(address);
    // Parameters {@code region} can be null.

    final long result = allocators.get(address).allocate(partialAddress, region, peek, null);
    Logger.debug("Allocate address: %s = 0x%x", address, result);

    return result;
  }

  private long allocate(
      final MmuAddressInstance address,
      final MmuExpression expression,
      final long partialAddress,
      final Range<Long> region,
      final boolean peek,
      final Set<Long> exclude) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(expression);
    // Parameters {@code region} and {@code exclude} can be null.

    final long result =  allocators.get(address).allocate(
        expression, partialAddress, region, peek, exclude);
    Logger.debug("Allocate address: %s[%s] = 0x%x", address, expression, result);

    return result;
  }

  public long getSignificatBitsMask(final MmuAddressInstance address) {
    return masks.get(address);
  }

  public void reset() {
    for (final AddressAllocationEngine allocator : allocators.values()) {
      allocator.reset();
    }
  }

  public Collection<Long> getAllAddresses(
      final MmuAddressInstance address, final Range<Long> region) {
    InvariantChecks.checkNotNull(address);
    // Parameters {@code region} and {@code exclude} can be null.

    return allocators.get(address).getAllAddresses(region);
  }
}
