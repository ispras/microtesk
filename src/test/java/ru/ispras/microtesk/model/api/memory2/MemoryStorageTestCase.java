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

import static org.junit.Assert.*;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;

public class MemoryStorageTestCase {
  @Test
  public void test() {
    final MemoryStorage ms = new MemoryStorage((int) Math.pow(2, 30), 8);
    final BitVector regionData = BitVector.valueOf("11001010");

    assertEquals(BitVector.newEmpty(8), ms.read(11));
    ms.write(11, regionData);
    assertEquals(regionData, ms.read(11));

    System.out.println(MemoryStorage.MAX_BLOCK_BIT_SIZE);
    System.out.println(ms.getRegionBitSize());
    System.out.println(ms.getRegionCount());
    System.out.println(ms.getBlockCount());
  }
}
