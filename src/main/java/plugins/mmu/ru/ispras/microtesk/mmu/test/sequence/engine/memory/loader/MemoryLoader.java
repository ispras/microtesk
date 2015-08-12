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
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * {@link MemoryLoader} implements a preparator for memory buffers.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryLoader implements Loader {
  private final MmuSubsystem memory = MmuTranslator.getSpecification();
  private final Map<MmuBuffer, BufferLoader> bufferLoaders = new LinkedHashMap<>();

  public MemoryLoader() {
    InvariantChecks.checkNotNull(memory);
  }

  private BufferLoader getLoader(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    BufferLoader bufferLoader = bufferLoaders.get(buffer);
    if (bufferLoader == null) {
      bufferLoaders.put(buffer, bufferLoader = new BufferLoader(buffer));
    }

    return bufferLoader;
  }

  public boolean contains(
      final MmuBuffer buffer, final BufferAccessEvent targetEvent, final long targetAddress) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(targetEvent);

    final BufferLoader bufferLoader = getLoader(buffer);
    return bufferLoader.contains(targetEvent, targetAddress);
  }

  public void addLoads(
      final MmuBuffer buffer,
      final BufferAccessEvent targetEvent,
      final long targetAddress,
      final List<Long> addresses) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(targetEvent);
    InvariantChecks.checkNotNull(addresses);

    final BufferLoader bufferLoader = getLoader(buffer);
    bufferLoader.addLoads(targetEvent, targetAddress, addresses);
  }

  public List<Load> prepareLoads(final MmuAddressType addressType) {
    InvariantChecks.checkNotNull(addressType);

    final List<Load> sequence = new ArrayList<>();
    final List<MmuBuffer> buffers = memory.getSortedListOfBuffers();

    // Reverse order: large buffers come first.
    for (int i = buffers.size() - 1; i >= 0; i--) {
      final MmuBuffer buffer = buffers.get(i);

      if (buffer.getAddress() == addressType && buffer.isReplaceable()) {
        final BufferLoader bufferLoader = getLoader(buffer);
        sequence.addAll(bufferLoader.prepareLoads());
      }
    }

    return sequence;
  }

  @Override
  public List<Load> prepareLoads() {
    final List<Load> sequence = new ArrayList<>();
    final List<MmuAddressType> addressTypes = memory.getSortedListOfAddresses();

    for (final MmuAddressType addressType : addressTypes) {
      sequence.addAll(prepareLoads(addressType));
    }

    return sequence;
  }
}
