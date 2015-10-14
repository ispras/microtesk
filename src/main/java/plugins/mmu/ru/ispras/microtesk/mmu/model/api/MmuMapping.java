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

/**
 * The {@link MmuMapping} class describes a buffer mapped to memory.
 * An access to such a buffer causes a access to memory by virtual 
 * address using MMU (address translation, caches, physical memory).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 * 
 * @param <D> Data type.
 * @param <A> Address type.
 */

public abstract class MmuMapping <D extends Data, A extends Address>
    implements Buffer<D, A>, BufferObserver {

  @Override
  public boolean isHit(final BitVector address) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isHit(final A address) {
    throw new UnsupportedOperationException();
  }

  @Override
  public D getData(final A address) {
    final BitVector value = getMmu().getData(address);
    return newData(value); 
  }

  @Override
  public D setData(final A address, final D data) {
    getMmu().setData(address, data.asBitVector());
    return null;
  }

  protected abstract Mmu<A> getMmu();
  protected abstract D newData(final BitVector value);
}
