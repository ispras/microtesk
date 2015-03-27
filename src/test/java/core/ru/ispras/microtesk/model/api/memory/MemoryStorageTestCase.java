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
}
