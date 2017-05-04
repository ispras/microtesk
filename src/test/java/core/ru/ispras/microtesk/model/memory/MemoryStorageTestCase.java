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

package ru.ispras.microtesk.model.memory;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;

public final class MemoryStorageTestCase {
  @Test
  public void test() {
    BigInteger regionCount = BigInteger.ONE;
    for (int regionCountExp = 0; regionCountExp <= 64; regionCountExp++) {
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
        final int regionBitSize = 1 << regionBitSizeExp; 
        test(regionCount, regionBitSize);
      }

      regionCount = regionCount.shiftLeft(1); 
    }
  }

  @Test
  public void testOffset() {
    final MemoryStorage ms = new MemoryStorage(1024, 64);

    ms.write(256, BitVector.valueOf(0xDEADBEEFBAADF00DL, 64));
    assertEquals(BitVector.valueOf(0xDEADBEEFBAADF00DL, 64), ms.read(256));

    ms.write(256, BitVector.valueOf(0, 32));
    assertEquals(BitVector.valueOf(0xDEADBEEF00000000L, 64), ms.read(256));

    ms.write(256, 32, BitVector.valueOf(-1, 32));
    assertEquals(BitVector.valueOf(0xFFFFFFFF00000000L, 64), ms.read(256));
  }

  @Test
  public void testAddressSizeMismatch() {
    final BigInteger regionCount = BigInteger.valueOf(2).pow(61);
    final int regionBitSize = 64;

    final MemoryStorage ms = new MemoryStorage(regionCount, 64);

    System.out.printf("Test: regions = %d (%x), region size = %d, address size = %d%n",
        regionCount, regionCount, regionBitSize, ms.getAddressBitSize());

    final BitVector address = BitVector.valueOf(0, ms.getAddressBitSize());
    final BitVector data = BitVector.valueOf(0xDEADBEEF, regionBitSize);

    ms.write(address, data);

    assertEquals(data, ms.read(0));
    assertEquals(data, ms.read(address));

    // Smaller than ms.getAddressBitSize()
    final BitVector smallAddress = BitVector.valueOf(0, 48);
    assertEquals(data, ms.read(smallAddress));

    // Larger than ms.getAddressBitSize()
    final BitVector largeAddress = BitVector.valueOf(0, 64);
    assertEquals(data, ms.read(largeAddress));
  }

  private void test(BigInteger regionCount, int regionBitSize) {
    final MemoryStorage ms = new MemoryStorage(regionCount, regionBitSize);

    System.out.printf("Test: regions = %d (%x), region size = %d, address size = %d%n",
        regionCount, regionCount, regionBitSize, ms.getAddressBitSize());

    for (int i = 0; i < 1000; i++) {
      final BitVector address = BitVector.newEmpty(ms.getAddressBitSize());
      Randomizer.get().fill(address);

      final BitVector data = BitVector.newEmpty(ms.getRegionBitSize());
      Randomizer.get().fill(data);

      // System.out.printf("Accessing address 0x%s, data: %s%n", address.toHexString(), data);
      ms.write(address, data);
      assertEquals(data, ms.read(address));
    }
  }

  @Test
  public void testCopy() {
    final int regionBitSize = 64;
    final BigInteger regionCount = BigInteger.valueOf(2).pow(61);

    final MemoryStorage storage1 = new MemoryStorage(regionCount, regionBitSize);

    assertEquals(BitVector.newEmpty(regionBitSize), storage1.read(0xDEADBEEF));
    assertEquals(BitVector.newEmpty(regionBitSize), storage1.read(0xBAADF00D));

    storage1.write(0xDEADBEEF, BitVector.valueOf(0x10L, regionBitSize));
    storage1.write(0xBAADF00D, BitVector.valueOf(0x20L, regionBitSize));

    assertEquals(BitVector.valueOf(0x10L, regionBitSize), storage1.read(0xDEADBEEF));
    assertEquals(BitVector.valueOf(0x20L, regionBitSize), storage1.read(0xBAADF00D));

    final MemoryStorage storage2 = new MemoryStorage(storage1);

    assertEquals(BitVector.valueOf(0x10L, regionBitSize), storage2.read(0xDEADBEEF));
    assertEquals(BitVector.valueOf(0x20L, regionBitSize), storage2.read(0xBAADF00D));

    storage2.write(0xDEADBEEF, BitVector.valueOf(0xFFFF10L, regionBitSize));
    storage2.write(0xBAADF00D, BitVector.valueOf(0xF0F020L, regionBitSize));

    assertEquals(BitVector.valueOf(0xFFFF10L, regionBitSize), storage2.read(0xDEADBEEF));
    assertEquals(BitVector.valueOf(0xF0F020L, regionBitSize), storage2.read(0xBAADF00D));

    assertEquals(BitVector.valueOf(0x10L, regionBitSize), storage1.read(0xDEADBEEF));
    assertEquals(BitVector.valueOf(0x20L, regionBitSize), storage1.read(0xBAADF00D));
  }
}
