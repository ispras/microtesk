/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.memory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.MemorySettings;
import ru.ispras.microtesk.test.GenerationAbortedException;

/**
 * The {@link MemoryStorage} implements a memory storage.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class MemoryStorage implements MemoryDevice {
  private String id;
  private boolean isReadOnly;
  private boolean isAddressCheckNeeded;

  private final BigInteger regionCount;
  private final int regionBitSize;
  private final int addressBitSize;

  private static final int REGIONS_IN_BLOCK = 1024 * 4;
  private final int blockBitSize;

  // Default value to be returned when reading an unallocated address.
  private final BitVector defaultRegion;
  private final Map<BitVector, Area> addressMap;

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
      adjusted.assign(bv);

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

  private final class Area {
    private final Map<Integer, Block> blocks;

    public Area() {
      this.blocks = new TreeMap<>();
    }

    public Area(final Area other) {
      this.blocks = new TreeMap<>(other.blocks);
    }

    public MemoryStorage getOwner() {
      return MemoryStorage.this;
    }

    public Block get(final int index) {
      return blocks.get(index);
    }

    public Block put(final int index, final Block block) {
      return blocks.put(index, block);
    }

    public void reset() {
      for (final Block block : blocks.values()) {
        block.reset();
      }
    }
  }

  private final class Block {
    private final BitVector storage;
    private final BitVector initFlags;

    public Block() {
      storage = BitVector.newEmpty(blockBitSize);
      initFlags = BitVector.newEmpty(REGIONS_IN_BLOCK);
    }

    public Block(final Block other) {
      this.storage = other.storage.copy();
      this.initFlags = other.initFlags.copy();
    }

    public MemoryStorage getOwner() {
      return MemoryStorage.this;
    }

    public void reset() {
      storage.reset();
      initFlags.reset();
    }

    public BitVector read(final int index) {
      final BitVector mapping = getRegionMapping(index); 
      return BitVector.unmodifiable(mapping);
    }

    public void write(final int index, final int offset, final BitVector data) {
      final BitVector mapping = getRegionMapping(index);

      final BitVector field = mapping.field(offset, offset + data.getBitSize() - 1);
      field.assign(data);

      initFlags.setBit(index, true);
    }

    public boolean isInitialized(final int index) {
      return initFlags.getBit(index);
    }

    private BitVector getRegionMapping(final int index) {
      InvariantChecks.checkBounds(index, REGIONS_IN_BLOCK);
      final int regionBitPos = index * regionBitSize;
      return BitVector.newMapping(storage, regionBitPos, regionBitSize);
    }
  }

  public MemoryStorage(final long regionCount, final int regionBitSize) {
    this(BigInteger.valueOf(regionCount), regionBitSize);
  }

  public MemoryStorage(final BigInteger regionCount, final int regionBitSize) {
    InvariantChecks.checkNotNull(regionCount);
    InvariantChecks.checkGreaterThan(regionCount, BigInteger.ZERO);
    InvariantChecks.checkGreaterThanZero(regionBitSize);

    this.id = "";
    this.isReadOnly = false;
    this.isAddressCheckNeeded = false;

    this.regionCount = regionCount;
    this.regionBitSize = regionBitSize;
    this.addressBitSize = calculateAddressSize(regionCount);
    this.blockBitSize = regionBitSize * REGIONS_IN_BLOCK;

    this.defaultRegion = BitVector.unmodifiable(BitVector.newEmpty(regionBitSize));
    this.addressMap = new HashMap<>();
  }

  public MemoryStorage(final MemoryStorage other) {
    InvariantChecks.checkNotNull(other);

    this.id = other.id;
    this.isReadOnly = other.isReadOnly;
    this.isAddressCheckNeeded = other.isAddressCheckNeeded;

    this.regionCount = other.regionCount;
    this.regionBitSize = other.regionBitSize;
    this.addressBitSize = other.addressBitSize;
    this.blockBitSize = other.blockBitSize;
    this.defaultRegion = other.defaultRegion;

    this.addressMap = new HashMap<>(other.addressMap);
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

  @Override
  public void store(final BitVector address, final int offset, final BitVector data) {
    write(address, offset, data);
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
    InvariantChecks.checkNotNull(id);
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

  public boolean isAddressCheckNeeded() {
    return isAddressCheckNeeded;
  }

  MemoryStorage setAddressCheckNeeded(final boolean isAddressCheckNeeded) {
    this.isAddressCheckNeeded = isAddressCheckNeeded;
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

  @Override
  public boolean isInitialized(final BitVector address) {
    InvariantChecks.checkNotNull(address);
    final Index index = new Index(address, addressBitSize);

    final Area area = addressMap.get(index.area);
    if (null == area) {
      return false;
    }

    final Block block = area.get(index.block);
    if (null == block) {
      return false;
    }

    return block.isInitialized(index.region);
  }

  public BitVector read(final long address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public BitVector read(final BigInteger address) {
    return read(BitVector.valueOf(address, addressBitSize));
  }

  public BitVector read(final BitVector address) {
    InvariantChecks.checkNotNull(address);
    checkAddress(address, false);

    final Index index = new Index(address, addressBitSize);
    final Area area = addressMap.get(index.area);
    if (null == area) {
      return defaultRegion;
    }

    final Block block = area.get(index.block);
    if (null == block) {
      return defaultRegion;
    }

    return block.read(index.region);
  }

  public void write(final long address, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public void write(final BigInteger address, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), data);
  }

  public void write(final long address, final int offset, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), offset, data);
  }

  public void write(final BigInteger address, final int offset, final BitVector data) {
    write(BitVector.valueOf(address, addressBitSize), offset, data);
  }

  public void write(final BitVector address, final BitVector data) {
    write(address, 0, data);
  }

  public void write(final BitVector address, final int offset, final BitVector data) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(data);

    checkAddress(address, true);

    if (isReadOnly()) {
      return;
    }

    final Index index = new Index(address, addressBitSize);

    Area area = addressMap.get(index.area);
    Block block = null;

    if (null == area || !isOwned(area)) {
      area = null == area ? new Area() : new Area(area);
      addressMap.put(index.area, area);
    } else {
      block = area.get(index.block);
    }

    if (null == block || !isOwned(block)) {
      block = null == block ? new Block() : new Block(block);
      area.put(index.block, block);
    }

    block.write(index.region, offset, data);
  }

  public void reset() {
    for (final Area area : addressMap.values()) {
      area.reset();
    }
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryStorage %s[regionBitSize=%s, regionCount=%s, addressBitSize=%s]",
        id, regionBitSize, regionCount, addressBitSize);
  }

  private void checkAddress(final BitVector address, final boolean isWrite) {
    if (!isAddressCheckNeeded) {
      return;
    }

    final GeneratorSettings settings = GeneratorSettings.get();
    if (null == settings) {
      return;
    }

    final MemorySettings memorySettings = settings.getMemory();
    if (null == memorySettings) {
      return;
    }

    InvariantChecks.checkTrue(regionBitSize % 8 == 0);

    final BigInteger addressValue =
        address.bigIntegerValue(false).multiply(BigInteger.valueOf(regionBitSize / 8));

    if (!memorySettings.checkDataAddress(addressValue)) {
      throw new GenerationAbortedException(String.format(
          "Address 0x%x does not match any data region.", addressValue));
    }
  }

  private boolean isOwned(final Block block) {
    return block.getOwner() == this;
  }

  private boolean isOwned(final Area area) {
    return area.getOwner() == this;
  }
}
