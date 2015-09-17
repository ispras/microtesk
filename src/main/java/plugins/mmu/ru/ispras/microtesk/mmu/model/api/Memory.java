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

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.MemoryDevice;

public abstract class Memory<D extends Data, A extends Address> implements Buffer<D, A>{
  private final BigInteger length;
  private MemoryDevice storage;

  public Memory(final BigInteger length) {
    InvariantChecks.checkNotNull(length);

    this.length = length;
    this.storage = null;
  }

  public void setStorage(final MemoryDevice storage) {
    InvariantChecks.checkNotNull(storage);
    this.storage = storage;
  }

  @Override
  public boolean isHit(final A address) {
    final BigInteger addressValue = address.getValue().bigIntegerValue(false);
    return addressValue.compareTo(length) < 0;
  }

  @Override
  public D getData(final A address) {
    InvariantChecks.checkNotNull(storage, "Storage device is not initialized.");
 
    BitVector addressValue = address.getValue();
    
    final int dataBitSize = getDataBitSize();
    final BitVector dataValue = BitVector.newEmpty(dataBitSize);

    int bitsRead = 0;
    while (bitsRead < dataBitSize) {
      
    }

    return newData(dataValue);
  }

  @Override
  public D setData(final A address, final D data) {
    InvariantChecks.checkNotNull(storage, "Storage device is not initialized.");

    final BitVector addressValue = address.getValue();
    final BitVector dataValue = data.asBitVector();
    final int dataBitSize = dataValue.getBitSize();

    int bitsWritten = 0;
    while (bitsWritten < dataBitSize) {
      
    }

    return null;
  }

  protected abstract int getDataBitSize();
  protected abstract D newData(final BitVector value); 
}
