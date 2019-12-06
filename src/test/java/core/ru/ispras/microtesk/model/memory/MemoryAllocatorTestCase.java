/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.Pair;

import java.math.BigInteger;

public class MemoryAllocatorTestCase {
  private static final int ADDRESSABLE_UNIT_SIZE = 8;
  private static final int REGION_SIZE = 32;
  private static final int REGION_COUNT = 128;

  @Test
  public void test() {
    final MemoryStorage memory =
        new MemoryStorage(REGION_COUNT, REGION_SIZE);

    final MemoryAllocator allocator =
        new MemoryAllocator(memory, ADDRESSABLE_UNIT_SIZE, BigInteger.ZERO);

    System.out.println(allocator);

    // Check correct initialization
    Assert.assertEquals(ADDRESSABLE_UNIT_SIZE, allocator.getAddressableUnitBitSize());
    Assert.assertEquals(REGION_SIZE, allocator.getRegionBitSize());
    Assert.assertEquals(4, allocator.getAddressableUnitsInRegion());

    // Test package-private helper routines
    testBitsToAddressableUnits(allocator);
    testAlignAddress();

    Pair<BigInteger, BigInteger> addressPair = new Pair<>(BigInteger.ZERO, BigInteger.ZERO);

    // Check correct allocation/alignment
    addressPair = allocator.allocate(
        addressPair.second,
        BitVector.valueOf(0xDEADBEEF, 32));
    Assert.assertEquals(BigInteger.valueOf(0), addressPair.first);
    Assert.assertEquals(BigInteger.valueOf(4), addressPair.second);
    Assert.assertEquals(BitVector.valueOf(0xDEADBEEF, 32), memory.read(0));

    addressPair = allocator.allocate(
        addressPair.second,
        BitVector.valueOf(0xF0F0F0F, 32));
    Assert.assertEquals(BigInteger.valueOf(4), addressPair.first);
    Assert.assertEquals(BigInteger.valueOf(8), addressPair.second);
    Assert.assertEquals(BitVector.valueOf(0xF0F0F0F, 32), memory.read(1));

    addressPair = allocator.allocate(
        addressPair.second,
        BitVector.valueOf(0xFF, ADDRESSABLE_UNIT_SIZE), 5);
    Assert.assertEquals(BigInteger.valueOf(8), addressPair.first);
    Assert.assertEquals(BigInteger.valueOf(13), addressPair.second);
    Assert.assertEquals(BitVector.valueOf(0xFFFFFFFF, 32), memory.read(2));
    Assert.assertEquals(BitVector.valueOf(0x000000FF, 32), memory.read(3));

    addressPair = allocator.allocateString(
        addressPair.second,
        "TEST", true);
    Assert.assertEquals(BigInteger.valueOf(13), addressPair.first);
    Assert.assertEquals(BigInteger.valueOf(18), addressPair.second);
    Assert.assertEquals(BitVector.valueOf(0x534554ff, 32), memory.read(3));
    Assert.assertEquals(BitVector.valueOf(0x00000054, 32), memory.read(4));

    addressPair = allocator.allocate(
        addressPair.second,
        BitVector.valueOf(0xDEADBEEF, 32),
        BitVector.valueOf(0xBAADF00D, 32));
    Assert.assertEquals(BigInteger.valueOf(20), addressPair.first);
    Assert.assertEquals(BigInteger.valueOf(28), addressPair.second);
    Assert.assertEquals(BitVector.valueOf(0xDEADBEEF, 32), memory.read(5));
    Assert.assertEquals(BitVector.valueOf(0xBAADF00D, 32), memory.read(6));

    addressPair = allocator.allocate(
        addressPair.second,
        BitVector.valueOf(0xC0DE, 16),
        BitVector.valueOf(0xC001, 16));
    Assert.assertEquals(BigInteger.valueOf(28), addressPair.first);
    Assert.assertEquals(BigInteger.valueOf(32), addressPair.second);
    Assert.assertEquals(BitVector.valueOf(0xC001C0DE, 32), memory.read(7));

    addressPair = allocator.allocate(
        addressPair.second, BitVector.valueOf(0xFF, 8));
    addressPair = allocator.allocate(
        addressPair.second, BitVector.valueOf(0xF00D, 16));

    Assert.assertEquals(BitVector.valueOf(0xF00D00FF, 32), memory.read(8));

    dumpMemory(memory);
  }

  private void testAlignAddress() {
    Assert.assertEquals(BigInteger.valueOf(0),   alignAddress(0, 4));
    Assert.assertEquals(BigInteger.valueOf(4),   alignAddress(1, 4));
    Assert.assertEquals(BigInteger.valueOf(4),   alignAddress(2, 4));
    Assert.assertEquals(BigInteger.valueOf(4),   alignAddress(3, 4));
    Assert.assertEquals(BigInteger.valueOf(4),   alignAddress(4, 4));
    Assert.assertEquals(BigInteger.valueOf(8),   alignAddress(5, 4));
    Assert.assertEquals(BigInteger.valueOf(8),   alignAddress(6, 4));
    Assert.assertEquals(BigInteger.valueOf(256), alignAddress(255, 4));
    Assert.assertEquals(BigInteger.valueOf(256), alignAddress(256, 4));
    Assert.assertEquals(BigInteger.valueOf(260), alignAddress(257, 4));
  }

  private static BigInteger alignAddress(final long address, final int alignment) {
    return MemoryAllocator.alignAddress(BigInteger.valueOf(address), alignment);
  }

  private void testBitsToAddressableUnits(MemoryAllocator allocator) {
    Assert.assertEquals(1, allocator.bitsToAddressableUnits(1));
    Assert.assertEquals(1, allocator.bitsToAddressableUnits(8));
    Assert.assertEquals(2, allocator.bitsToAddressableUnits(9));
    Assert.assertEquals(4, allocator.bitsToAddressableUnits(31));
    Assert.assertEquals(4, allocator.bitsToAddressableUnits(32));
    Assert.assertEquals(5, allocator.bitsToAddressableUnits(35));
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
