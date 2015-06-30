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

package ru.ispras.microtesk.test.mmu.sequence.engine.loader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.mmu.ir.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.ir.spec.basis.BufferAccessEvent;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferLoader implements Loader {
  private final MmuDevice device;
  private final Map<Long, SetLoader> loaders = new LinkedHashMap<>();

  public BufferLoader(final MmuDevice device) {
    this.device = device;
  }

  private SetLoader getLoader(final long address) {
    final long index = device.getIndex(address);

    SetLoader loader = loaders.get(index);
    if (loader == null) {
      loaders.put(index, loader = new SetLoader(device, index));
    }

    return loader;
  }

  public boolean contains(final BufferAccessEvent event, final long address) {
    final SetLoader loader = getLoader(address);

    return loader.contains(event, address);
  }

  public void addLoads(final BufferAccessEvent event, final long address, final List<Long> loads) {
    final SetLoader loader = getLoader(address);

    loader.addLoads(event, address, loads);
  }

  @Override
  public List<Long> prepareLoads() {
    final List<Long> sequence = new ArrayList<>();

    for (final SetLoader loader : loaders.values()) {
      sequence.addAll(loader.prepareLoads());
    }

    return sequence;
  }
}
