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

package ru.ispras.microtesk.model.api.memory;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;

/**
 * The {@link MemoryDeviceWrapper} class adapts a {@link Memory} object to
 * the {@link MemoryDevice} interface. This might be required to map an
 * external (created by a plugin) object modeling a memory storage to
 * a {@link Memory} object.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MemoryDeviceWrapper implements MemoryDevice {
  private final Memory memory;
  private final int addressBitSize;

  public MemoryDeviceWrapper(final Memory memory) {
    InvariantChecks.checkNotNull(memory);
    this.memory = memory;
    this.addressBitSize = MemoryStorage.calculateAddressSize(memory.getLength());
  }

  public static MemoryDeviceWrapper newWrapperFor(final String name) {
    InvariantChecks.checkNotNull(name);
    final Memory memory = Memory.get(name);
    return new MemoryDeviceWrapper(memory);
  }

  @Override
  public int getAddressBitSize() {
    return addressBitSize;
  }

  @Override
  public int getDataBitSize() {
    return memory.getType().getBitSize();
  }

  @Override
  public BitVector load(final BitVector address) {
    final Location location = memory.access(address.bigIntegerValue(false));
    final Data data = location.load(); 
    return data.getRawData();
  }

  @Override
  public void store(final BitVector address, final BitVector data) {
    final Location location = memory.access(address.bigIntegerValue(false));
    location.store(new Data(data, memory.getType()));
  }

  @Override
  public boolean isInitialized(final BitVector address) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void useTemporaryContext(final boolean value) {
    // Nothing
  }
}
