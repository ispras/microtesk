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

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

public class MemoryAllocatorTestCase {
  private static final int ADDRESSABLE_UNIT_SIZE = 8;
  private static final int REGION_SIZE = 32;
  private static final int REGION_COUNT = 128;

  @Test
  public void test() {
    final MemoryStorage memory = new MemoryStorage(REGION_COUNT, REGION_SIZE);
    final MemoryAllocator allocator = new MemoryAllocator(memory, ADDRESSABLE_UNIT_SIZE);

    System.out.println(allocator);

    // Check correct initialization
    assertEquals(allocator.getCurrentAddress(), 0);
    assertEquals(allocator.getAddressableUnitBitSize(), ADDRESSABLE_UNIT_SIZE);
    assertEquals(allocator.getRegionBitSize(), REGION_SIZE);
    assertEquals(allocator.getAddressableUnitsInRegion(), 4);

    int address = 0;

    // Check correct allocation/alignment
    address = allocator.allocate(BitVector.valueOf(0xDEADBEEF, 32));
    assertEquals(address, 0);

    int a1 = allocator.allocateData(Type.INT(32), BigInteger.valueOf(1));
    allocator.allocateData(Type.INT(32), BigInteger.valueOf(0xFFFFFFFF));

    System.out.println("Address1: " + a1);

    int a2 = allocator.allocateSpace(Type.CARD(8), 3, 0xFF);
    System.out.println("Address2: " + a2);
    
    allocator.allocateAsciiString("TEST", true);

    dumpMemory(memory);
  }

  private static void dumpMemory(MemoryStorage memory) {
    System.out.print("-------------------------------------------------------");
    for (int index = 0; index < memory.getRegionCount(); ++index) {
      if (index % 4 == 0) {
        System.out.println();
      }
      final BitVector region = memory.read(index);
      System.out.printf("%03d: %08x ", index, region.intValue());
    }
    System.out.println();
  }

}
