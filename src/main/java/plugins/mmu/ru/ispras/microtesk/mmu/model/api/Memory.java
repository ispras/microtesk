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

public abstract class Memory<D extends Data, A extends Address> 
    implements Buffer<D, A>, BufferObserver {
  private final BigInteger byteSize;
  private MemoryDevice storage;

  /**
   * Proxy class is used to simply code of assignment expressions.
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

  public Memory(final BigInteger byteSize) {
    InvariantChecks.checkNotNull(byteSize);

    this.byteSize = byteSize;
    this.storage = null;
  }

  public void setStorage(final MemoryDevice storage) {
    InvariantChecks.checkNotNull(storage);
    this.storage = storage;
  }

  @Override
  public boolean isHit(final A address) {
    final BigInteger addressValue = address.getValue().bigIntegerValue(false);
    return addressValue.compareTo(byteSize) < 0;
  }

  @Override
  public final boolean isHit(final BitVector value) {
    final A address = newAddress(); 
    address.getValue().assign(value);
    return isHit(address);
  }

  @Override
  public D getData(final A address) {
    InvariantChecks.checkNotNull(storage, "Storage device is not initialized.");

    final int dataBitSize = getDataBitSize();
    final BitVector dataValue = BitVector.newEmpty(dataBitSize);

    BigInteger index = addressToIndex(address.getValue(), dataBitSize);

    int bitsRead = 0;
    while (bitsRead < dataBitSize) {
      final BitVector regionData =
          storage.load(BitVector.valueOf(index, storage.getAddressBitSize()));

      final BitVector mapping =
          BitVector.newMapping(dataValue, bitsRead, regionData.getBitSize());

      mapping.assign(regionData);

      index = index.add(BigInteger.ONE);
      bitsRead += storage.getDataBitSize(); 
    }

    return newData(dataValue);
  }

  @Override
  public D setData(final A address, final D data) {
    InvariantChecks.checkNotNull(storage, "Storage device is not initialized.");

    final BitVector dataValue = data.asBitVector();
    final int dataBitSize = dataValue.getBitSize();

    BigInteger index = addressToIndex(address.getValue(), dataBitSize);

    int bitsWritten = 0;
    while (bitsWritten < dataBitSize) {
      final BitVector mapping =
          BitVector.newMapping(dataValue, bitsWritten, storage.getDataBitSize());

      storage.store(
          BitVector.valueOf(index, storage.getAddressBitSize()),
          mapping
          );

      index = index.add(BigInteger.ONE);
      bitsWritten += storage.getDataBitSize();
    }

    return null;
  }

  public final Proxy setData(final A address) {
    return new Proxy(address);
  }

  protected abstract A newAddress();
  protected abstract D newData(final BitVector value);
  protected abstract int getDataBitSize();

  private BigInteger addressToIndex(final BitVector address, int blockBitSize) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkTrue(blockBitSize >= 8);
    InvariantChecks.checkTrue(((blockBitSize - 1) & blockBitSize) == 0);

    final BigInteger addressInBytes = address.bigIntegerValue(false);
    final BigInteger blockMask = BigInteger.valueOf(blockBitSize / 8 - 1);
    final BigInteger blockAddress = addressInBytes.andNot(blockMask);


    final BigInteger bytesInRegion =
        BigInteger.valueOf(storage.getDataBitSize() / 8);

    return blockAddress.divide(bytesInRegion);
  }
}
