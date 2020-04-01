/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.MemoryDevice;

import java.math.BigInteger;

public abstract class Memory<E extends Struct<?>, A extends Address> implements Buffer<E, A> {
  private final Struct<E> entryCreator;
  private final Address<A> addressCreator;

  private final BigInteger byteSize;
  private MemoryDevice storage;

  /**
   * Proxy class is used to simplify code of assignment expressions.
   */
  public final class Proxy {
    private final A address;

    private Proxy(final A address) {
      this.address = address;
    }

    public void assign(final E entry) {
      writeEntry(address, entry.asBitVector());
    }

    public void assign(final BitVector value) {
      writeEntry(address, value);
    }
  }

  public Memory(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final BigInteger byteSize) {
    InvariantChecks.checkNotNull(entryCreator);
    InvariantChecks.checkNotNull(addressCreator);
    InvariantChecks.checkNotNull(byteSize);

    this.entryCreator = entryCreator;
    this.addressCreator = addressCreator;
    this.byteSize = byteSize;
    this.storage = null;
  }

  public final void setStorage(final MemoryDevice storage) {
    InvariantChecks.checkNotNull(storage);
    this.storage = storage;
  }

  @Override
  public boolean isHit(final A address) {
    final BigInteger addressValue = address.getValue().bigIntegerValue(false);
    return addressValue.compareTo(byteSize) < 0;
  }

  @Override
  public final E readEntry(final A address) {
    InvariantChecks.checkNotNull(storage, "Storage device is uninitialized");

    final int entryBitSize = entryCreator.getBitSize();
    final int storageBitSize = storage.getDataBitSize();
    final int addressBitSize = storage.getAddressBitSize();
    final BitVector entry = BitVector.newEmpty(entryBitSize);

    BigInteger index = addressToIndex(address.getValue(), entryBitSize);

    int bitsRead = 0;
    while (bitsRead < entryBitSize) {
      final BitVector regionData = storage.load(BitVector.valueOf(index, addressBitSize));
      InvariantChecks.checkNotNull(regionData, "Uninitialized memory");

      final BitVector mapping = BitVector.newMapping(entry, bitsRead, regionData.getBitSize());
      mapping.assign(regionData);

      index = index.add(BigInteger.ONE);
      bitsRead += storageBitSize;
    }

    return entryCreator.newStruct(entry);
  }

  @Override
  public final void writeEntry(final A address, final BitVector newEntry) {
    InvariantChecks.checkNotNull(storage, "Storage device is uninitialized");

    // Note that the input entry may be of different size.
    final int entryBitSize = entryCreator.getBitSize();
    final int storageBitSize = storage.getDataBitSize();
    final int addressBitSize = storage.getAddressBitSize();
    BigInteger index = addressToIndex(address.getValue(), entryBitSize);

    int bitsWritten = 0;
    while (bitsWritten < entryBitSize) {
      final BitVector mapping = BitVector.newMapping(newEntry, bitsWritten, storageBitSize);
      storage.store(BitVector.valueOf(index, addressBitSize), mapping);

      index = index.add(BigInteger.ONE);
      bitsWritten += storageBitSize;
    }
  }

  @Override
  public final void writeEntry(
      final A address,
      final int lower,
      final int upper,
      final BitVector data) {
    InvariantChecks.checkBounds(lower, entryCreator.getBitSize());

    final int min = lower;
    final int max = upper >= entryCreator.getBitSize() ? entryCreator.getBitSize() - 1 : upper;

    final BitVector memoryEntry = readEntry(address).asBitVector();
    final BitVector coercedData = data.resize((max - min) + 1, false);

    memoryEntry.field(min, max).assign(coercedData);
    writeEntry(address, memoryEntry);
  }

  public final Proxy writeEntry(final A address) {
    return new Proxy(address);
  }

  @Override
  public void resetState() {
    // Do nothing.
  }

  private BigInteger addressToIndex(final BitVector address, final int blockBitSize) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkTrue(blockBitSize >= 8);
    InvariantChecks.checkTrue(((blockBitSize - 1) & blockBitSize) == 0);

    final BigInteger addressInBytes = address.bigIntegerValue(false);
    final BigInteger blockMask = BigInteger.valueOf(blockBitSize / 8 - 1);
    final BigInteger blockAddress = addressInBytes.andNot(blockMask);

    final BigInteger bytesInRegion = BigInteger.valueOf(storage.getDataBitSize() / 8);
    return blockAddress.divide(bytesInRegion);
  }
}
