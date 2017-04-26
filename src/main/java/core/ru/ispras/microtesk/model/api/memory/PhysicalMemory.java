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
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.tracer.Record;
import ru.ispras.microtesk.model.api.tracer.Tracer;

final class PhysicalMemory extends Memory {
  private final MemoryStorage storage;
  private MemoryDevice handler;
  private MemoryAllocator allocator;

  private final boolean isLogical;
  private final BigInteger addressableUnitsInData;

  public PhysicalMemory(
      final String name,
      final Type type,
      final BigInteger length) {
    super(Kind.MEM, name, type, length, false);

    this.storage = new MemoryStorage(length, type.getBitSize()).setId(name);
    this.handler = null;
    this.allocator = null;
  
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
    } else {
      this.addressableUnitsInData = null;
      this.isLogical = true;
    }

    storage.setAddressCheckNeeded(!isLogical);
  }

  private PhysicalMemory(final PhysicalMemory other) {
    super(other);

    this.storage = new MemoryStorage(other.storage);
    this.handler = other.handler;

    if (null != other.allocator) {
      initAllocator(other.allocator.getAddressableUnitBitSize(), other.allocator.getBaseAddress());
      this.allocator.setCurrentAddress(other.allocator.getCurrentAddress());
    }

    this.addressableUnitsInData = other.addressableUnitsInData;
    this.isLogical = other.isLogical;
  }

  @Override
  public void initAllocator(final int addressableUnitBitSize, final BigInteger baseAddress) {
    InvariantChecks.checkGreaterThanZero(addressableUnitBitSize);
    InvariantChecks.checkNotNull(baseAddress);

    if (null == allocator) {
      allocator = new MemoryAllocator(storage, addressableUnitBitSize, baseAddress);
    } else {
      InvariantChecks.checkTrue(allocator.getAddressableUnitBitSize() == addressableUnitBitSize);
      InvariantChecks.checkTrue(allocator.getBaseAddress() == baseAddress);
    }
  }

  @Override
  public MemoryAllocator getAllocator() {
    InvariantChecks.checkNotNull(allocator, "Allocator is not initialized.");
    return allocator;
  }

  @Override
  public MemoryDevice setHandler(final MemoryDevice handler) {
    InvariantChecks.checkNotNull(handler);
    InvariantChecks.checkFalse(isLogical);

    this.handler = handler;
    return storage;
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
  public Memory copy() {
    return new PhysicalMemory(this);
  }

  @Override
  public void reset() {
    storage.reset();
    if (null != allocator) {
      allocator.reset();
    }
  }

  private Location newLocationForRegion(final BitVector index) {
    InvariantChecks.checkNotNull(index);

    final LocationAtom atom = isLogical ?
        new LogicalMemoryAtom(index, getType().getBitSize(), 0) :
        new PhysicalMemoryAtom(index, getType().getBitSize(), 0);

    return Location.newLocationForAtom(getType(), atom);
  }

  private final class PhysicalMemoryAtom extends LocationAtom {
    private PhysicalMemoryAtom(
        final BitVector index,
        final int bitSize,
        final int startBitPos) {
      super(getName(), index, bitSize, startBitPos);
      InvariantChecks.checkNotNull(index);
    }

    @Override
    public int getStorageBitSize() {
      return getType().getBitSize();
    }

    @Override
    public boolean isInitialized() {
      return storage.isInitialized(virtualIndexToPhysicalIndex(getIndex()));
    }

    @Override
    public PhysicalMemoryAtom resize(
        final int newBitSize,
        final int newStartBitPos) {
      return new PhysicalMemoryAtom(getIndex(), newBitSize, newStartBitPos);
    }

    @Override
    public BitVector load(final boolean callHandler) {
      final BitVector index = getIndex();
      final MemoryDevice targetDevice;
      final BitVector targetAddress;

      if (!callHandler || handler == null) {
        targetDevice = storage;
        targetAddress = virtualIndexToPhysicalIndex(index);
      } else {
        targetDevice = handler;

        final BigInteger virtualAddress = indexToAddress(index.bigIntegerValue(false));
        targetAddress = BitVector.valueOf(virtualAddress, handler.getAddressBitSize());
      }

      final BitVector region = targetDevice.load(targetAddress);
      final BitVector data = BitVector.newMapping(region, getBitFieldStart(), getBitFieldSize());

      if (Tracer.isEnabled()) {
        final BigInteger virtualAddress = indexToAddress(index.bigIntegerValue(false));
        final Record record = Record.newMemoryAccess(
            virtualAddress.longValue(),
            data,
            false
            );
        Tracer.addRecord(record);
      }

      return data;
    }

    @Override
    public void store(final BitVector data, final boolean callHandler) {
      InvariantChecks.checkNotNull(data);

      final BitVector index = getIndex();
      final MemoryDevice targetDevice;
      final BitVector targetAddress;

      if (!callHandler || handler == null) {
        targetDevice = storage;
        targetAddress = virtualIndexToPhysicalIndex(index);
      } else {
        targetDevice = handler;

        final BigInteger virtualAddress = indexToAddress(index.bigIntegerValue(false));
        targetAddress = BitVector.valueOf(virtualAddress, handler.getAddressBitSize());
      }

      targetDevice.store(targetAddress, getBitFieldStart(), data);

      if (Tracer.isEnabled()) {
        final BigInteger virtualAddress = indexToAddress(index.bigIntegerValue(false));
        final Record record = Record.newMemoryAccess(
            virtualAddress.longValue(),
            data,
            true
            );
        Tracer.addRecord(record);
      }
    }

    @Override
    public String toString() {
      return String.format("%s[%d]<%d..%d>",
          getName(),
          getIndex().bigIntegerValue(false),
          getBitFieldStart(),
          getBitFieldStart() + getBitFieldSize() - 1
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
      final BigInteger physicalAddress = AddressTranslator.get().virtualToPhysical(virtualAddress);

      final BigInteger physicalIndex = addressToIndex(physicalAddress);
      return BitVector.valueOf(physicalIndex, storage.getAddressBitSize());
    }
  }

  private final class LogicalMemoryAtom extends LocationAtom {
    private LogicalMemoryAtom(
        final BitVector index,
        final int bitSize,
        final int startBitPos) {
      super(getName(), index, bitSize, startBitPos);
      InvariantChecks.checkNotNull(index);
    }

    @Override
    public int getStorageBitSize() {
      return getType().getBitSize();
    }

    @Override
    public boolean isInitialized() {
      return storage.isInitialized(getIndex());
    }

    @Override
    public LogicalMemoryAtom resize(
        final int newBitSize,
        final int newStartBitPos) {
      return new LogicalMemoryAtom(getIndex(), newBitSize, newStartBitPos);
    }

    @Override
    public BitVector load(final boolean callHandler) {
      final BitVector region = storage.load(getIndex());
      return BitVector.newMapping(region, getBitFieldStart(), getBitFieldSize());
    }

    @Override
    public void store(final BitVector data, final boolean callHandler) {
      InvariantChecks.checkNotNull(data);
      storage.store(getIndex(), getBitFieldStart(), data);
    }

    @Override
    public String toString() {
      return String.format("%s[%d]<%d..%d>",
          getName(),
          getIndex().bigIntegerValue(false),
          getBitFieldStart(),
          getBitFieldStart() + getBitFieldSize() - 1
          );
    }
  }
}
