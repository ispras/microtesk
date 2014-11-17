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

public final class MemoryStorage {
  private static final int MAX_BLOCK_BIT_SIZE = 4096 * 8; 

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

      final int regionBitPos = index * regionBitSize;
      return BitVector.unmodifiable(BitVector.newMapping(storage, regionBitPos, regionBitSize));
    }

    public void writeRegion(int index, BitVector data) {
      checkRange(index);
      checkNotNull(data);

      if (data.getBitSize() != regionBitSize) {
        throw new IllegalArgumentException();
      }

      if (null == storage) {
        final int blockBitSize = regionsInBlock * regionBitSize;
        storage = BitVector.newEmpty(blockBitSize); 
      }
      
      final int regionBitPos = index * regionBitSize;
      final BitVector target = BitVector.newMapping(storage, regionBitPos, regionBitSize);

      target.assign(data);
    }

    private void checkRange(int index) {
      if (index <= 0 || index > regionsInBlock) {
        throw new IndexOutOfBoundsException();
      }
    }
  }

  int getBlockCount() {
    return blocks.size();
  }

  Block getBlock(int blockIndex) {
    return blocks.get(blockIndex);
  }

  public MemoryStorage(final int regionCount, final int regionBitSize) {
    checkGreaterThanZero(regionCount);
    checkGreaterThanZero(regionBitSize);

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
    for (int index = 0; index < blocks.size() - 1; index++) {
      blocks.add(new Block(maxRegionsInBlock));  
    }

    blocks.add(new Block(regionsInLastBlock));
    return blocks;
  }

  public int getRegionCount() {
    return regionCount;
  }

  public int getRegionBitSize() {
    return regionBitSize;
  }

  public BitVector read(int regionIndex) {
    if (!(0 <= regionIndex && regionIndex < regionCount)) { 
      throw new IndexOutOfBoundsException();
    }

    final int blockIndex = regionIndex / maxRegionsInBlock;
    final int regionInBlockIndex = regionIndex % maxRegionsInBlock;

    final Block block = blocks.get(blockIndex);
    return block.readRegion(regionInBlockIndex);
  }

  public void write(int regionIndex, BitVector data) {
    checkNotNull(data);
    if (data.getBitSize() != regionBitSize) {
      throw new IllegalArgumentException();
    }

    if (!(0 <= regionIndex && regionIndex < regionCount)) { 
      throw new IndexOutOfBoundsException();
    }

    final int blockIndex = regionIndex / maxRegionsInBlock;
    final int regionInBlockIndex = regionIndex % maxRegionsInBlock;

    final Block block = blocks.get(blockIndex);
    block.writeRegion(regionInBlockIndex, data);
  }

  public void reset() {
    for (Block block : blocks) {
      block.reset();
    }
  }

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }

  private static void checkGreaterThanZero(int n ) {
    if (n <= 0) {
      throw new IllegalArgumentException();      
    }
  }
}
