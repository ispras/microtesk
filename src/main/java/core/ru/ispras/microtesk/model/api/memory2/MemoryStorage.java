/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.memory2;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import static ru.ispras.microtesk.utils.InvariantChecks.*;

/**
 * Serves as a memory storage organized as a sequence of fixed-size regions grouped into blocks.
 * Blocks are allocated when data is being written to them for the first time. When a region
 * in an unallocated block is read, a bit vector filled with zeros is returned.
 * 
 * @author Andrei Tatarnikov
 */

public final class MemoryStorage {
  /** Maximal size of memory block in bits */
  public static final int MAX_BLOCK_BIT_SIZE = 4096 * 8;

  private final String id;

  private final int regionCount;
  private final int regionBitSize;

  private final int maxBlockBitSize;
  private final int maxRegionsInBlock;

  private final BitVector zeroRegion;
  private final List<Block> blocks;

  final class Block {
    private final int regionsInBlock;
    private BitVector storage;

    public Block(int regionsInBlock) {
      checkGreaterThanZero(regionsInBlock);

      this.regionsInBlock = regionsInBlock;
      this.storage = null;
    }

    public void reset() {
      if (null != storage) {
        storage.reset();
      }
    }

    public BitVector readRegion(int index) {
      checkRange(index);

      if (null == storage) {
        return zeroRegion;
      }

      final BitVector target = getRegionMapping(index);
      return BitVector.unmodifiable(target);
    }

    public void writeRegion(int index, BitVector data) {
      checkRange(index);
      checkNotNull(data);

      if (data.getBitSize() != regionBitSize) {
        throw new IllegalArgumentException();
      }

      if (null == storage) {
        storage = allocateBlock(); 
      }

      final BitVector target = getRegionMapping(index);
      target.assign(data);
    }

    private BitVector allocateBlock() {
      final int blockBitSize = regionsInBlock * regionBitSize;
      return BitVector.newEmpty(blockBitSize);
    }

    private BitVector getRegionMapping(int index) {
      final int regionBitPos = index * regionBitSize;
      return BitVector.newMapping(storage, regionBitPos, regionBitSize);
    }

    private void checkRange(int index) {
      if (! (0 <= index && index < regionsInBlock)) {
        throw new IndexOutOfBoundsException(String.format(
            "%s is out of bounds [%d..%d)", index, 0, regionsInBlock));
      }
    }
  }

  int getBlockCount() {
    return blocks.size();
  }

  Block getBlock(int blockIndex) {
    return blocks.get(blockIndex);
  }

  public MemoryStorage(int regionCount, int regionBitSize) {
    this (null, regionCount, regionBitSize);
  }

  public MemoryStorage(String id, final int regionCount, final int regionBitSize) {
    checkGreaterThanZero(regionCount);
    checkGreaterThanZero(regionBitSize);

    this.id = id; 

    this.regionCount = regionCount;
    this.regionBitSize = regionBitSize;

    this.maxBlockBitSize = MAX_BLOCK_BIT_SIZE - (MAX_BLOCK_BIT_SIZE % regionBitSize);
    this.maxRegionsInBlock = maxBlockBitSize / regionBitSize;

    this.zeroRegion = BitVector.unmodifiable(BitVector.newEmpty(regionBitSize));
    this.blocks = reserveBlocks(regionCount, regionBitSize);
  }

  private List<Block> reserveBlocks(final int regionCount, final int regionBitSize) {
    final int remainder = regionCount % maxRegionsInBlock;

    final int blockCount = 
        regionCount / maxRegionsInBlock + (0 == remainder ? 0 : 1);

    final int regionsInLastBlock = 
        0 == remainder ? maxRegionsInBlock : remainder;

    final List<Block> blocks = new ArrayList<Block>(blockCount); 
    for (int index = 0; index < blockCount - 1; index++) {
      blocks.add(new Block(maxRegionsInBlock));  
    }

    blocks.add(new Block(regionsInLastBlock));
    return blocks;
  }

  public String getId() {
    return id;
  }

  public int getRegionCount() {
    return regionCount;
  }

  public int getRegionBitSize() {
    return regionBitSize;
  }

  public BitVector read(int regionIndex) {
    checkBounds(regionIndex, regionCount);

    final Block block = getBlockForRegion(regionIndex);
    final int regionInBlockIndex = getRegionInBlockIndex(regionIndex);

    return block.readRegion(regionInBlockIndex);
  }

  public void write(int regionIndex, BitVector data) {
    checkBounds(regionIndex, regionCount);

    checkNotNull(data);
    if (data.getBitSize() != regionBitSize) {
      throw new IllegalArgumentException();
    }

    final Block block = getBlockForRegion(regionIndex);
    final int regionInBlockIndex = getRegionInBlockIndex(regionIndex);

    block.writeRegion(regionInBlockIndex, data);
  }

  public void reset() {
    for (Block block : blocks) {
      block.reset();
    }
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryStorage [id=%s, regions=%s, region bit size=%s, blocks=%s]",
        id, getRegionCount(), getRegionBitSize(), getBlockCount());
  }

  private Block getBlockForRegion(int regionIndex) {
    final int blockIndex = regionIndex / maxRegionsInBlock;
    return blocks.get(blockIndex);
  }

  private int getRegionInBlockIndex(int regionIndex) {
    return regionIndex % maxRegionsInBlock;
  }
}
