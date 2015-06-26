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

import java.math.BigInteger;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;

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
    assertEquals(0, allocator.getCurrentAddress());
    assertEquals(ADDRESSABLE_UNIT_SIZE, allocator.getAddressableUnitBitSize());
    assertEquals(REGION_SIZE, allocator.getRegionBitSize());
    assertEquals(4, allocator.getAddressableUnitsInRegion());

    // Test package-private helper routines
    testBitsToAddressableUnits(allocator);
    testAlignAddress();

    BigInteger address = BigInteger.ZERO;

    // Check correct allocation/alignment
    address = allocator.allocate(BitVector.valueOf(0xDEADBEEF, 32));
    assertEquals(0, address);
    assertEquals(BitVector.valueOf(0xDEADBEEF, 32), memory.read(0));

    address = allocator.allocate(BitVector.valueOf(0xF0F0F0F, 32));
    assertEquals(4, address);
    assertEquals(BitVector.valueOf(0xF0F0F0F, 32), memory.read(1));

    address = allocator.allocate(BitVector.valueOf(0xFF, ADDRESSABLE_UNIT_SIZE), 5);
    assertEquals(8, address);
    assertEquals(BitVector.valueOf(0xFFFFFFFF, 32), memory.read(2));
    assertEquals(BitVector.valueOf(0x000000FF, 32), memory.read(3));
    
    address = allocator.allocateAsciiString("TEST", true);
    assertEquals(13, address);
    assertEquals(BitVector.valueOf(0x534554ff, 32), memory.read(3));
    assertEquals(BitVector.valueOf(0x00000054, 32), memory.read(4));
    
    address = allocator.allocate(BitVector.valueOf(0xDEADBEEF, 32), BitVector.valueOf(0xBAADF00D, 32));
    assertEquals(20, address);
    assertEquals(BitVector.valueOf(0xDEADBEEF, 32), memory.read(5));
    assertEquals(BitVector.valueOf(0xBAADF00D, 32), memory.read(6));
    
    address = allocator.allocate(BitVector.valueOf(0xC0DE, 16), BitVector.valueOf(0xC001, 16));
    assertEquals(28, address);
    assertEquals(BitVector.valueOf(0xC001C0DE, 32), memory.read(7));
    
    allocator.allocate(BitVector.valueOf(0xFF, 8));
    allocator.allocate(BitVector.valueOf(0xF00D, 16));

    assertEquals(BitVector.valueOf(0xF00D00FF, 32), memory.read(8));

    dumpMemory(memory);
  }

  private void testAlignAddress() {
    assertEquals(BigInteger.valueOf(0),   alignAddress(0, 4));
    assertEquals(BigInteger.valueOf(4),   alignAddress(1, 4));
    assertEquals(BigInteger.valueOf(4),   alignAddress(2, 4));
    assertEquals(BigInteger.valueOf(4),   alignAddress(3, 4));
    assertEquals(BigInteger.valueOf(4),   alignAddress(4, 4));
    assertEquals(BigInteger.valueOf(8),   alignAddress(5, 4));
    assertEquals(BigInteger.valueOf(8),   alignAddress(6, 4));
    assertEquals(BigInteger.valueOf(256), alignAddress(255, 4));
    assertEquals(BigInteger.valueOf(256), alignAddress(256, 4));
    assertEquals(BigInteger.valueOf(260), alignAddress(257, 4));
  }

  private static BigInteger alignAddress(final long address, final int alignment) {
    return MemoryAllocator.alignAddress(BigInteger.valueOf(address), alignment);
  }

  private void testBitsToAddressableUnits(MemoryAllocator allocator) {
    assertEquals(1, allocator.bitsToAddressableUnits(1));
    assertEquals(1, allocator.bitsToAddressableUnits(8));
    assertEquals(2, allocator.bitsToAddressableUnits(9));
    assertEquals(4, allocator.bitsToAddressableUnits(31));
    assertEquals(4, allocator.bitsToAddressableUnits(32));
    assertEquals(5, allocator.bitsToAddressableUnits(35));
  }

  private static void dumpMemory(MemoryStorage memory) {
    System.out.print("-------------------------------------------------------");
    for (int index = 0; index < memory.getRegionCount().intValue(); ++index) {
      if (index % 4 == 0) {
        System.out.println();
      }
      final BitVector region = memory.read(index);
      System.out.printf("%03d: %08x ", index, region.intValue());
    }
    System.out.println();
  }

}
