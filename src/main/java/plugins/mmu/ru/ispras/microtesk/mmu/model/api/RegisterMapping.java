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

package ru.ispras.microtesk.mmu.model.api;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.MemoryDevice;
import ru.ispras.microtesk.model.api.memory.MemoryDeviceWrapper;

public abstract class RegisterMapping<D extends Data, A extends Address>
    implements Buffer<D, A> {

  private final MemoryDevice storage;

  /**
   * Proxy class is used to simplify code of assignment expressions.
   */
  public final class Proxy {
    private final A address;

    private Proxy(final A address) {
      this.address = address;
    }

    public D assign(final D data) {
      return setData(address, data);
    }

    public D assign(final BitVector value) {
      final D data = newData(value);
      return setData(address, data);
    }
  }

  public RegisterMapping(final String name) {
    this.storage = MemoryDeviceWrapper.newWrapperFor(name);
    InvariantChecks.checkTrue(getDataBitSize() == storage.getDataBitSize());
  }

  @Override
  public final boolean isHit(final A address) {
    throw new UnsupportedOperationException(
        "isHit is unsupported for mapped buffers.");
  }

  @Override
  public final D getData(final A address) {
    final BitVector value = storage.load(address.getValue());
    return newData(value);
  }

  @Override
  public final D setData(final A address, final D data) {
    storage.store(address.getValue(), data.asBitVector());
    return null;
  }

  public final Proxy setData(final A address) {
    return new Proxy(address);
  }

  protected abstract D newData(final BitVector value);
  protected abstract int getDataBitSize();
}
