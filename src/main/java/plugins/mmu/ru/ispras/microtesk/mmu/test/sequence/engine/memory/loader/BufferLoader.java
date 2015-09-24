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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * {@link BufferLoader} implements a preparator for a memory buffer.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferLoader implements Loader {
  private final MmuBuffer buffer;
  private final Map<Long, SetLoader> setLoaders = new LinkedHashMap<>();

  public BufferLoader(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);
    this.buffer = buffer;
  }

  private SetLoader getSetLoader(final long address) {
    final long index = buffer.getIndex(address);

    SetLoader setLoader = setLoaders.get(index);
    if (setLoader == null) {
      setLoaders.put(index, setLoader = new SetLoader(buffer, index));
    }

    return setLoader;
  }

  public boolean contains(final BufferAccessEvent targetEvent, final long targetAddress) {
    InvariantChecks.checkNotNull(targetEvent);

    final SetLoader setLoader = getSetLoader(targetAddress);
    return setLoader.contains(targetEvent, targetAddress);
  }

  public void addAddresses(
      final BufferAccessEvent targetEvent, final long targetAddress, final List<Long> addresses) {
    InvariantChecks.checkNotNull(targetEvent);
    InvariantChecks.checkNotNull(addresses);

    final SetLoader setLoader = getSetLoader(targetAddress);
    setLoader.addAddresses(targetEvent, targetAddress, addresses);
  }

  public void addAddressesAndEntries(
      final BufferAccessEvent targetEvent,
      final long targetAddress,
      final List<AddressAndEntry> addressesAndEntries) {
    InvariantChecks.checkNotNull(targetEvent);
    InvariantChecks.checkNotNull(addressesAndEntries);

    final SetLoader setLoader = getSetLoader(targetAddress);
    setLoader.addAddressesAndEntries(targetEvent, targetAddress, addressesAndEntries);
  }

  @Override
  public List<Load> prepareLoads() {
    final List<Load> sequence = new ArrayList<>();

    for (final SetLoader setLoader : setLoaders.values()) {
      sequence.addAll(setLoader.prepareLoads());
    }

    return sequence;
  }
}
