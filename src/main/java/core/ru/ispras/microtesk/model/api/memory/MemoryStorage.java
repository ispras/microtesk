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
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThan;
import static ru.ispras.fortress.util.InvariantChecks.checkBounds;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

  private static final int REGIONS_IN_BLOCK = 1024 * 4;
  private final int blockBitSize;

  // Default value to be returned when reading an unallocated address.
  private final BitVector defaultRegion;

  private final Map<BitVector, Map<Integer, Block>> addressMap;

  private final static class Index {
    private static final BitVector ZERO_FIELD = BitVector.valueOf(0, 1);

    public final BitVector address;
    public final int region;
    public final int block;
    public final BitVector area;

    public Index(BitVector address) {
      this.address = address;
      this.region  = getField(address, 0, 11).intValue();
      this.block = getField(address, 12, 43).intValue();
      this.area  = getField(address, 44, address.getBitSize());
    }

    private static BitVector getField(final BitVector bv, final int min, final int max) {
      if (min >= bv.getBitSize()) {
        return ZERO_FIELD;
      }

      final int bitSize = Math.min(max + 1, bv.getBitSize()) - min;
      return BitVector.newMapping(bv, min, bitSize);
    }

    @Override
    public String toString() {
      return String.format("0x%s[area=0x%s, block=0x%X, region=0x%X]",
          address.toHexString(), area.toHexString(), block, region);
    }
  }

  private final class Block {
    private final BitVector storage;

    public Block() {
      storage = BitVector.newEmpty(blockBitSize);
    }

    public void reset() {
      storage.reset();
    }

    public BitVector read(final int index) {
      final BitVector mapping = getRegionMapping(index); 
      return BitVector.unmodifiable(mapping);
    }

    public void write(final int index, final BitVector data) {
      final BitVector mapping = getRegionMapping(index);
      mapping.assign(data);
    }

    private BitVector getRegionMapping(final int index) {
      checkBounds(index, REGIONS_IN_BLOCK);
      final int regionBitPos = index * regionBitSize;
      return BitVector.newMapping(storage, regionBitPos, regionBitSize);
    }
  }

  public MemoryStorage(final long regionCount, final int regionBitSize) {
    this(BigInteger.valueOf(regionCount), regionBitSize);
  }

  public MemoryStorage(final BigInteger regionCount, final int regionBitSize) {
    checkNotNull(regionCount);
    checkGreaterThan(regionCount, BigInteger.ZERO);
    checkGreaterThanZero(regionBitSize);

    this.id = "";
    this.isReadOnly = false;

    this.regionCount = regionCount;
    this.regionBitSize = regionBitSize;
    this.addressBitSize = calculateAddressSize(regionBitSize, regionCount);
    this.blockBitSize = regionBitSize * REGIONS_IN_BLOCK;

    this.defaultRegion = BitVector.unmodifiable(BitVector.newEmpty(regionBitSize));
    this.addressMap = new HashMap<>();
  }

  public static int calculateAddressSize(
      final int regionBitSize,
      final BigInteger regionCount) {
    int result = 0;

    BigInteger value = regionCount.subtract(BigInteger.ONE);
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

  MemoryStorage setId(final String id) {
    checkNotNull(id);
    this.id = id;
    return this;
  }

  public boolean isReadOnly() {
    return isReadOnly;
  }

  MemoryStorage setReadOnly(final boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
    return this;
  }

  public BigInteger getRegionCount() {
    return regionCount;
  }

  public int getRegionBitSize() {
    return regionBitSize;
  }

  public int getAddressBitSize() {
    return addressBitSize;
  }

  public BitVector read(final int address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public void write(final int address, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public BitVector read(final long address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public void write(final long address, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public BitVector read(final BitVector address) {
    checkNotNull(address);
    final Index index = new Index(address);

    final Map<Integer, Block> area = addressMap.get(index.area);
    if (null == area) {
      return defaultRegion;
    }

    final Block block = area.get(index.block);
    if (null == block) {
      return defaultRegion;
    }

    return block.read(index.region);
  }

  public void write(final BitVector address, final BitVector data) {
    checkNotNull(address);
    checkNotNull(data);

    if (isReadOnly()) {
      return;
    }

    final Index index = new Index(address);

    Map<Integer, Block> area = addressMap.get(index.area);
    Block block = null;

    if (null == area) {
      area = new TreeMap<>();
      addressMap.put(index.area, area);
    } else {
      block = area.get(index.block);
    }

    if (null == block) {
      block = new Block();
      area.put(index.block, block);
    }

    block.write(index.region, data);
  }

  public void reset() {
    for (final Map<Integer, Block> area : addressMap.values()) {
      for (final Block block : area.values()) {
        block.reset();
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryStorage %s[regionBitSize=%s, regionCount=%s, addressBitSize=%s]",
        id, regionBitSize, regionCount, addressBitSize);
  }
}
