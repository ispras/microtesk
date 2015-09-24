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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * {@link SetLoader} implements a preparator for a single buffer set.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SetLoader implements Loader {
  private final MmuBuffer buffer;
  private final long index;

  private final EnumMap<BufferAccessEvent, Map<Long, EventLoader>> eventLoaders =
      new EnumMap<>(BufferAccessEvent.class);

  /**
   * Constructs a set loader.
   * 
   * @param buffer the target memory buffer.
   * @param index the index of the set.
   */
  public SetLoader(final MmuBuffer buffer, final long index) {
    InvariantChecks.checkNotNull(buffer);

    this.buffer = buffer;
    this.index = index;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }
  
  public long getIndex() {
    return index;
  }

  public boolean contains(final BufferAccessEvent targetEvent, final long targetAddress) {
    final Map<Long, EventLoader> tagLoaders = eventLoaders.get(targetEvent);
    if (tagLoaders == null) {
      return false;
    }

    final long tag = buffer.getTag(targetAddress);
    return tagLoaders.containsKey(tag);
  }

  public void addAddresses(
      final BufferAccessEvent targetEvent,
      final long targetAddress,
      final List<Long> addresses) {
    InvariantChecks.checkNotNull(targetEvent);
    InvariantChecks.checkNotNull(addresses);

    Map<Long, EventLoader> tagLoaders = eventLoaders.get(targetEvent);
    if (tagLoaders == null) {
      eventLoaders.put(targetEvent, tagLoaders = new LinkedHashMap<>());
    }

    final long tag = buffer.getTag(targetAddress);

    EventLoader eventLoader = tagLoaders.get(tag);
    if (eventLoader == null) {
      tagLoaders.put(tag, eventLoader = new EventLoader(buffer, targetEvent, targetAddress));
    }

    eventLoader.addAddresses(addresses);
  }

  public void addAddressesAndEntries(
      final BufferAccessEvent targetEvent,
      final long targetAddress,
      final List<AddressAndEntry> addressesAndEntries) {
    InvariantChecks.checkNotNull(targetEvent);
    InvariantChecks.checkNotNull(addressesAndEntries);

    Map<Long, EventLoader> tagLoaders = eventLoaders.get(targetEvent);
    if (tagLoaders == null) {
      eventLoaders.put(targetEvent, tagLoaders = new LinkedHashMap<>());
    }

    final long tag = buffer.getTag(targetAddress);

    EventLoader eventLoader = tagLoaders.get(tag);
    if (eventLoader == null) {
      tagLoaders.put(tag, eventLoader = new EventLoader(buffer, targetEvent, targetAddress));
    }

    eventLoader.addAddressesAndEntries(addressesAndEntries);
  }

  private List<Load> prepareLoads(final Collection<EventLoader> eventLoaders) {
    InvariantChecks.checkNotNull(eventLoaders);

    final List<Load> sequence = new ArrayList<>();

    int length = 0;
    for (final EventLoader eventLoader : eventLoaders) {
      final List<Load> loads = eventLoader.prepareLoads();

      sequence.addAll(loads);
      length += loads.size();

      // Optimization.
      if (length >= buffer.getWays()) {
        break;
      }
    }

    return sequence;
  }

  @Override
  public List<Load> prepareLoads() {
    final List<Load> sequence = new ArrayList<>();

    // Evict data.
    final Map<Long, EventLoader> missLoaders = eventLoaders.get(BufferAccessEvent.MISS);
    if (missLoaders != null) {
      sequence.addAll(prepareLoads(missLoaders.values()));
    }

    // Load data.
    final Map<Long, EventLoader> hitLoaders = eventLoaders.get(BufferAccessEvent.HIT);
    if (hitLoaders != null) {
      sequence.addAll(prepareLoads(hitLoaders.values()));
    }

    return sequence;
  }
}
