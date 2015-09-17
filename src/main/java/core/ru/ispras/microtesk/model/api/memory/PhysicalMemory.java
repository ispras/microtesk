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

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.test.TestSettings;

final class PhysicalMemory extends Memory {
  private final MemoryDevice storage;
  private MemoryDevice handler;

  private final boolean isLogical;
  private AddressTranslator addressTranslator;
  private final BigInteger addressableUnitsInData;

  public PhysicalMemory(
      final String name,
      final Type type,
      final BigInteger length) {
    super(Kind.MEM, name, type, length, false);

    this.storage = new MemoryStorage(length, type.getBitSize()).setId(name);
    this.handler = null;
    this.addressTranslator = null;

    // A memory array that corresponds to a real physical memory must satisfy the following
    // precondition: (1) element size is a multiple of 8 bits (byte), (2) element count is
    // greater than one. This is required for MMU handlers and for simple translation logic
    // to work correctly.
    //
    // There are situations when memory is used as a variable (logical memory) and MMU-related
    // logic is not used. When the preconditions are violated, memory is considered logical.
    //
    if (storage.getDataBitSize() % 8 == 0 && length.compareTo(BigInteger.ONE) >= 0) {
      this.addressableUnitsInData = BigInteger.valueOf(storage.getDataBitSize() / 8);
      this.isLogical = false;
    }
    else {
      this.addressableUnitsInData = null;
      this.isLogical = true;
    }
  }

  public MemoryDevice getStorage() {
    return storage;
  }

  public MemoryDevice getHandler() {
    return handler;
  }

  public void setHandler(final MemoryDevice handler) {
    InvariantChecks.checkNotNull(handler);
    this.handler = handler;
  }

  @Override
  public Location access(final int index) {
    return access(index & 0x00000000FFFFFFFFL);
  }

  @Override
  public Location access(final long index) {
    final BitVector address = BitVector.valueOf(index, storage.getAddressBitSize());
    return newLocationForRegion(address);
  }

  @Override
  public Location access(final BigInteger index) {
    final BitVector address = BitVector.valueOf(index, storage.getAddressBitSize());
    return newLocationForRegion(address);
  }

  @Override
  public Location access(final Data address) {
    InvariantChecks.checkNotNull(address);
    return newLocationForRegion(address.getRawData());
  }

  @Override
  public void reset() {
    // Do nothing. Memory is not reset.
  }

  @Override
  public void setUseTempCopy(boolean value) {
    storage.useTemporaryContext(value);
  }

  private Location newLocationForRegion(final BitVector index) {
    InvariantChecks.checkNotNull(index);

    final Location.Atom atom = isLogical ?
        new LogicalMemoryAtom(index, getType().getBitSize(), 0) :
        new PhysicalMemoryAtom(index, getType().getBitSize(), 0);

    return new Location(getType(), atom);
  }

  private AddressTranslator getAddressTranslator() {
    if (null == addressTranslator) {
      addressTranslator = new AddressTranslator(
          TestSettings.getBaseVirtualAddress(),
          TestSettings.getBasePhysicalAddress()
          );
    }

    return addressTranslator;
  }

  private final class PhysicalMemoryAtom implements Location.Atom {
    private final BitVector index;
    private final int bitSize;
    private final int startBitPos;

    private PhysicalMemoryAtom(
        final BitVector index,
        final int bitSize,
        final int startBitPos) {
      this.index = index;
      this.bitSize = bitSize;
      this.startBitPos = startBitPos;
    }

    @Override
    public boolean isInitialized() {
      return storage.isInitialized(virtualIndexToPhysicalIndex(index));
    }

    @Override
    public PhysicalMemoryAtom resize(
        final int newBitSize,
        final int newStartBitPos) {
      return new PhysicalMemoryAtom(index, newBitSize, newStartBitPos);
    }

    @Override
    public int getBitSize() {
      return bitSize;
    }

    @Override
    public int getStartBitPos() {
      return startBitPos;
    }

    @Override
    public BitVector load(final boolean callHandler) {
      final MemoryDevice targetDevice;
      final BitVector targetAddress;

      if (!callHandler || handler == null) {
        targetDevice = storage;
        targetAddress = virtualIndexToPhysicalIndex(index);
      } else {
        targetDevice = handler;

        final BigInteger virtualAddress = indexToAddress(index.bigIntegerValue(false));
        targetAddress = BitVector.valueOf(virtualAddress, handler.getAddressBitSize());

        // TODO: temporary. To make sure it is not called yet.
        throw new UnsupportedOperationException("Functionality is untested and it not enabled yet.");
      }

      final BitVector region = targetDevice.load(targetAddress);
      return BitVector.newMapping(region, startBitPos, bitSize);
    }

    @Override
    public void store(final BitVector data, final boolean callHandler) {
      InvariantChecks.checkNotNull(data);

      final MemoryDevice targetDevice;
      final BitVector targetAddress;

      if (!callHandler || handler == null) {
        targetDevice = storage;
        targetAddress = virtualIndexToPhysicalIndex(index);
      } else {
        targetDevice = handler;

        final BigInteger virtualAddress = indexToAddress(index.bigIntegerValue(false));
        targetAddress = BitVector.valueOf(virtualAddress, handler.getAddressBitSize());

        // TODO: temporary. To make sure it is not called yet.
        throw new UnsupportedOperationException("Functionality is untested and it not enabled yet.");
      }

      final BitVector region;
      if (bitSize == targetDevice.getDataBitSize()) {
        region = data;
      } else {
        region = targetDevice.load(targetAddress).copy();
        final BitVector mapping = BitVector.newMapping(region, startBitPos, bitSize);
        mapping.assign(data);
      }

      targetDevice.store(targetAddress, region);
    }

    @Override
    public String toString() {
      return String.format("%s[%d]<%d..%d>",
          getName(),
          index.bigIntegerValue(false),
          startBitPos,
          startBitPos + bitSize - 1
          );
    }

    private BigInteger indexToAddress(final BigInteger index) {
      if (addressableUnitsInData.equals(BigInteger.ONE)) {
        return index;
      }

      return index.multiply(addressableUnitsInData);
    }

    private BigInteger addressToIndex(final BigInteger address) {
      if (addressableUnitsInData.equals(BigInteger.ONE)) {
        return address;
      }

      return address.divide(addressableUnitsInData);
    }

    private BitVector virtualIndexToPhysicalIndex(final BitVector index) {
      final BigInteger virtualAddress = indexToAddress(index.bigIntegerValue(false));
      final BigInteger physicalAddress = getAddressTranslator().virtualToPhysical(virtualAddress);

      final BigInteger physicalIndex = addressToIndex(physicalAddress);
      return BitVector.valueOf(physicalIndex, storage.getAddressBitSize());
    }
  }

  private final class LogicalMemoryAtom implements Location.Atom {
    private final BitVector index;
    private final int bitSize;
    private final int startBitPos;

    private LogicalMemoryAtom(
        final BitVector index,
        final int bitSize,
        final int startBitPos) {
      this.index = index;
      this.bitSize = bitSize;
      this.startBitPos = startBitPos;
    }

    @Override
    public boolean isInitialized() {
      return storage.isInitialized(index);
    }

    @Override
    public LogicalMemoryAtom resize(
        final int newBitSize,
        final int newStartBitPos) {
      return new LogicalMemoryAtom(index, newBitSize, newStartBitPos);
    }

    @Override
    public int getBitSize() {
      return bitSize;
    }

    @Override
    public int getStartBitPos() {
      return startBitPos;
    }

    @Override
    public BitVector load(final boolean callHandler) {
      final BitVector region = storage.load(index);
      return BitVector.newMapping(region, startBitPos, bitSize);
    }

    @Override
    public void store(final BitVector data, final boolean callHandler) {
      InvariantChecks.checkNotNull(data);

      final BitVector region;
      if (bitSize == storage.getDataBitSize()) {
        region = data;
      } else {
        region = storage.load(index).copy();
        final BitVector mapping = BitVector.newMapping(region, startBitPos, bitSize);
        mapping.assign(data);
      }

      storage.store(index, region);
    }

    @Override
    public String toString() {
      return String.format("%s[%d]<%d..%d>",
          getName(),
          index.bigIntegerValue(false),
          startBitPos,
          startBitPos + bitSize - 1
          );
    }
  }
}
