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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryLoader implements Loader {
  private final MmuSubsystem memory;
  private final Map<MmuDevice, BufferLoader> loaders = new LinkedHashMap<>();

  public MemoryLoader(final MmuSubsystem memory) {
    this.memory = memory;
  }

  private BufferLoader getLoader(final MmuDevice device) {
    BufferLoader loader = loaders.get(device);
    if (loader == null) {
      loaders.put(device, loader = new BufferLoader(device));
    }

    return loader;
  }

  public boolean contains(final MmuDevice device, final BufferAccessEvent event,
      final long address) {
    final BufferLoader loader = getLoader(device);

    return loader.contains(event, address);
  }

  public void addLoads(final MmuDevice device, final BufferAccessEvent event,
      final long address, final List<Long> loads) {
    final BufferLoader loader = getLoader(device);

    loader.addLoads(event, address, loads);
  }

  public List<Long> prepareLoads(final MmuAddress addressType) {
    final List<Long> sequence = new ArrayList<>();
    final List<MmuDevice> devices = memory.getSortedListOfDevices();

    // Reverse order: large buffers come first.
    for (int i = devices.size() - 1; i >= 0; i--) {
      final MmuDevice device = devices.get(i);

      if (device.getAddress() == addressType && device.isReplaceable()) {
        final BufferLoader loader = getLoader(device);
        sequence.addAll(loader.prepareLoads());
      }
    }

    return sequence;
  }
  
  @Override
  public List<Long> prepareLoads() {
    final List<Long> sequence = new ArrayList<>();
    final List<MmuAddress> addresses = memory.getSortedListOfAddresses();

    for (final MmuAddress address : addresses) {
      sequence.addAll(prepareLoads(address));
    }

    return sequence;
  }
}
