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

package ru.ispras.microtesk.model.api.memory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;

public final class MemoryStorageTestCase {
  @Test
  public void test() {
    for (int regionCountExp = 0; regionCountExp < 32; regionCountExp++) {
      final int regionCount = 1 << regionCountExp; // 2 ** regionCount

      test(regionCount, 3);
      test(regionCount, 5);
      test(regionCount, 7);
      test(regionCount, 10);
      test(regionCount, 12);
      test(regionCount, 14);
      test(regionCount, 18);
      test(regionCount, 24);
      test(regionCount, 26);
      test(regionCount, 28);

      for (int regionBitSizeExp = 0; regionBitSizeExp <= 8; regionBitSizeExp++) {
        final int regionBitSize = 1 << regionBitSizeExp; // 2 ** regionBitSizeExp, 
        test(regionCount, regionBitSize);
      }
    }
  }

  private void test(int regionCount, int regionBitSize) {
    System.out.printf("%nTest: region count = %d, region bit size = %d%n", regionCount, regionBitSize);

    final MemoryStorage ms = new MemoryStorage(regionCount, regionBitSize);
    
    final int maxBlockBitSize = 
        MemoryStorage.MAX_BLOCK_BIT_SIZE - (MemoryStorage.MAX_BLOCK_BIT_SIZE % regionBitSize);

    final int maxRegionsInBlock = 
        maxBlockBitSize / regionBitSize;
    
    final int blockCount = 
        regionCount / maxRegionsInBlock + (0 == regionCount % maxRegionsInBlock ? 0 : 1);
    
    System.out.printf(
        "Max block size = %d, max block size (adjusted) = %d%n",
        MemoryStorage.MAX_BLOCK_BIT_SIZE, maxBlockBitSize, maxRegionsInBlock);
    
    System.out.printf(
        "Max regions in block = %d, block count = %d%n", blockCount, maxRegionsInBlock);

    assertEquals(regionCount, ms.getRegionCount());
    assertEquals(regionBitSize, ms.getRegionBitSize());
    assertEquals(blockCount, ms.getBlockCount());
    
    for (int i = 0; i < 1000; i++) {
      randomAccessTest(ms, Randomizer.get().nextIntRange(0, ms.getRegionCount()-1));
    }
  }

  private void randomAccessTest(MemoryStorage ms, int regionIndex) {
    final BitVector data = BitVector.newEmpty(ms.getRegionBitSize());
    Randomizer.get().fill(data);

    //System.out.printf("Accessing region %d, data: %s%n", regionIndex, data);

    ms.write(regionIndex, data);
    assertEquals(data, ms.read(regionIndex));
  }
}
