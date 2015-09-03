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

import static ru.ispras.fortress.util.InvariantChecks.checkBounds;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThan;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm;

/**
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MemoryStorage implements MemoryDevice {
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
  private Map<BitVector, Map<Integer, Block>> tempAddressMap = null;

  private final static class Index {
    private static final BitVector ZERO_FIELD = BitVector.valueOf(0, 1);

    public final BitVector address;
    public final int region;
    public final int block;
    public final BitVector area;

    public Index(final BitVector address, final int addressBitSize) {
      this.address = adjustSize(address, addressBitSize);
      this.region  = getField(this.address, 0, 11).intValue();
      this.block = getField(this.address, 12, 43).intValue();
      this.area  = getField(this.address, 44, addressBitSize - 1);
    }

    private static BitVector getField(final BitVector bv, final int min, final int max) {
      if (min >= bv.getBitSize()) {
        return ZERO_FIELD;
      }

      final int bitSize = Math.min(max + 1, bv.getBitSize()) - min;
      return BitVector.newMapping(bv, min, bitSize);
    }

    private static BitVector adjustSize(final BitVector bv, final int bitSize) {
      if (bv.getBitSize() == bitSize) {
        return bv; 
      }

      if (bv.getBitSize() > bitSize) {
        return BitVector.newMapping(bv, 0, bitSize);
      }

      final BitVector adjusted = BitVector.newEmpty(bitSize);
      BitVectorAlgorithm.copy(bv, adjusted);
      return adjusted;
    }

    @Override
    public String toString() {
      return String.format(
          "0x%s(%d bits)[area=0x%s(%d bits), block=0x%X, region=0x%X]",
          address.toHexString(),
          address.getBitSize(),
          area.toHexString(),
          area.getBitSize(),
          block,
          region
          );
    }
  }

  private final class Block {
    private final BitVector storage;
    private final BitSet initFlags;

    public Block() {
      storage = BitVector.newEmpty(blockBitSize);
      initFlags = new BitSet();
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

      initFlags.set(index, true);
    }

    public boolean isInitialized(final int index) {
      return initFlags.get(index);
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
    this.addressBitSize = calculateAddressSize(regionCount);
    this.blockBitSize = regionBitSize * REGIONS_IN_BLOCK;

    this.defaultRegion = BitVector.unmodifiable(BitVector.newEmpty(regionBitSize));
    this.addressMap = new HashMap<>();
  }

  @Override
  public int getAddressBitSize() {
    return addressBitSize;
  }

  @Override
  public int getDataBitSize() {
    return getRegionBitSize();
  }

  @Override
  public BitVector load(final BitVector address) {
    return read(address);
  }

  @Override
  public void store(final BitVector address, final BitVector data) {
    write(address, data);
  }

  public void setUseTempCopy(final boolean value) {
    if (isReadOnly()) {
      return; // Makes not sense for read-only stores
    }

    if (value) {
      tempAddressMap = new HashMap<>();
    } else {
      tempAddressMap = null;
    }
  }

  private Map<BitVector, Map<Integer, Block>> getAddressMap() {
    return null != tempAddressMap ? tempAddressMap : addressMap;
  }

  public static int calculateAddressSize(final BigInteger regionCount) {
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

  public boolean isInitialized(final int address) {
    return isInitialized(BitVector.valueOf(address, addressBitSize));
  }

  public boolean isInitialized(final long address) {
    return isInitialized(BitVector.valueOf(address, addressBitSize));
  }

  public boolean isInitialized(final BitVector address) {
    checkNotNull(address);
    final Index index = new Index(address, addressBitSize);

    final Map<Integer, Block> area = getAddressMap().get(index.area);
    if (null == area) {
      return false;
    }

    final Block block = area.get(index.block);
    if (null == block) {
      return false;
    }

    return block.isInitialized(index.region);
  }

  public BitVector read(final int address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public BitVector read(final long address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public BitVector read(final BigInteger address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public BitVector read(final BitVector address) {
    checkNotNull(address);
    final Index index = new Index(address, addressBitSize);

    final Map<Integer, Block> area = getAddressMap().get(index.area);
    if (null == area) {
      return defaultRegion;
    }

    final Block block = area.get(index.block);
    if (null == block) {
      return defaultRegion;
    }

    return block.read(index.region);
  }

  public void write(final int address, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public void write(final long address, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public void write(final BigInteger address, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public void write(final BitVector address, final BitVector data) {
    checkNotNull(address);
    checkNotNull(data);

    if (isReadOnly()) {
      return;
    }

    final Index index = new Index(address, addressBitSize);

    Map<Integer, Block> area = getAddressMap().get(index.area);
    Block block = null;

    if (null == area) {
      area = new TreeMap<>();
      getAddressMap().put(index.area, area);
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
    for (final Map<Integer, Block> area : getAddressMap().values()) {
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
