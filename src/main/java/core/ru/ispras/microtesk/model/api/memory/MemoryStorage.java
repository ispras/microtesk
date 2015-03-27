/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkBounds;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MemoryStorage {
  private String id;
  private boolean isReadOnly;

  private final BigInteger regionCount;
  private final int regionBitSize;
  private final int addressBitSize;

  private static final int BLOCK_SIZE_IN_UNITS = 1024 * 4;
  private final int blockSizeInBits;

  // Default value to be returned when reading an unallocated address.
  private final BitVector defaultUnitData;

  private final Map<BitVector, Map<Integer, Block>> addressSpace;

  private final static class Address {
    static final BitVector ZERO_FIELD = BitVector.valueOf(0, 1);

    final BitVector address;
    final int unit;
    final int block;
    final BitVector area;

    Address(BitVector address) {
      this.address = address;
      this.unit  = getField(address, 0, 11).intValue();
      this.block = getField(address, 12, 43).intValue();
      this.area  = getField(address, 34, address.getBitSize());
    }

    @Override
    public String toString() {
      return String.format("address 0x%s[area=0x%X, block=0x%X, unit=0x%X]",
          address.toHexString(), area.bigIntegerValue(), block, unit);
    }

    static BitVector getField(BitVector bv, int min, int max) {
      if (min >= bv.getBitSize()) {
        return ZERO_FIELD;
      }

      final int bitSize = Math.min(max + 1, bv.getBitSize()) - min;
      return BitVector.newMapping(bv, min, bitSize);
    }
  }

  final class Block {
    private final BitVector storage;

    public Block() {
      storage = BitVector.newEmpty(blockSizeInBits);
    }

    public void reset() {
      storage.reset();
    }

    public BitVector read(int unitIndex) {
      final BitVector mapping = getUnitMapping(unitIndex); 
      return BitVector.unmodifiable(mapping);
    }

    public void write(int unitIndex, BitVector data) {
      final BitVector mapping = getUnitMapping(unitIndex);
      mapping.assign(data);
    }

    private BitVector getUnitMapping(int index) {
      checkBounds(index, BLOCK_SIZE_IN_UNITS);
      final int bitPos = index * regionBitSize;
      return BitVector.newMapping(storage, bitPos, regionBitSize);
    }
  }

  public MemoryStorage(long storageSizeInUnits, int addressableUnitBitSize) {
    this(BigInteger.valueOf(storageSizeInUnits), addressableUnitBitSize);
  }

  public MemoryStorage(BigInteger storageSizeInUnits, int addressableUnitBitSize) {
    checkGreaterThanZero(addressableUnitBitSize);
    checkNotNull(storageSizeInUnits);

    if (storageSizeInUnits.compareTo(BigInteger.ZERO) <= 0) {
      throw new IllegalArgumentException("Illegal storage size: " + storageSizeInUnits);
    }

    this.id = "";
    this.isReadOnly = false;

    this.regionCount = storageSizeInUnits;
    this.regionBitSize = addressableUnitBitSize;
    this.addressBitSize = calculateAddressSize(addressableUnitBitSize, storageSizeInUnits);
    this.blockSizeInBits = addressableUnitBitSize * BLOCK_SIZE_IN_UNITS;

    this.defaultUnitData = BitVector.unmodifiable(BitVector.newEmpty(addressableUnitBitSize));
    this.addressSpace = new HashMap<>();
  }

  private static int calculateAddressSize(
      final int addressableUnitSize, final BigInteger storageSize) {

    int result = 0;

    BigInteger value = storageSize.subtract(BigInteger.ONE);
    while (!value.equals(BigInteger.ZERO)) {
      value = value.shiftRight(1);
      ++result;
    }

    if (result == 0) {
      return 1;
    }

    return result;
  }

  public String getId() {
    return id;
  }

  MemoryStorage setId(String id) {
    checkNotNull(id);
    this.id = id;
    return this;
  }

  public boolean isReadOnly() {
    return isReadOnly;
  }

  MemoryStorage setReadOnly(boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
    return this;
  }

  public BigInteger getStorageSizeInUnits() {
    return regionCount;
  }

  public int getRegionBitSize() {
    return regionBitSize;
  }

  public int getAddressBitSize() {
    return addressBitSize;
  }

  public BitVector read(int address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public void write(int address, BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public BitVector read(BitVector address) {
    checkNotNull(address);

    final Address addr = new Address(address);

    final Map<Integer, Block> area = addressSpace.get(addr.area);
    if (null == area) {
      return defaultUnitData;
    }

    final Block block = area.get(addr.block);
    if (null == block) {
      return defaultUnitData;
    }

    return block.read(addr.unit);
  }

  public void write(BitVector address, BitVector data) {
    checkNotNull(address);
    checkNotNull(data);

    if (isReadOnly()) {
      return;
    }

    final Address addr = new Address(address);

    Map<Integer, Block> area = addressSpace.get(addr.area);
    Block block = null;

    if (null == area) {
      area = new HashMap<>();
      addressSpace.put(addr.area, area);
    } else {
      block = area.get(addr.block);
    }

    if (null == block) {
      block = new Block();
      area.put(addr.block, block);
    }

    block.write(addr.unit, data);
  }

  public void reset() {
    for(Map<Integer, Block> area : addressSpace.values()) {
      for (Block block : area.values()) {
        block.reset();
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryStorage %s[addressableUnitSize=%s bits, storageSize=%s units, addressSize=%s bits]",
        id, regionBitSize, regionCount, addressBitSize);
  }

  public int getRegionCount() {
    return getStorageSizeInUnits().intValue();
  }
}
