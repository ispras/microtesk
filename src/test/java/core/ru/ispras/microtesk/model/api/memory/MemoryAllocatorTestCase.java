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
import java.util.Arrays;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

public class MemoryAllocatorTestCase {

  private static final int BITS_IN_BYTE = 8;

  @Test
  public void test() {
    assertTrue(true);
    
    final MemoryStorage memory = new MemoryStorage(128, 32);
    final MemoryAllocator allocator = new MemoryAllocator(memory, BITS_IN_BYTE);

    dumpMemory(memory);
    
    int a1 = allocator.allocateData(Type.INT(32), Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(0xFFFFFFFF)));
    System.out.println("Address1: " + a1);

    int a2 = allocator.allocateSpace(Type.CARD(8), 3, 0xFF);
    System.out.println("Address2: " + a2);
    

    dumpMemory(memory);
  }

  private static void dumpMemory(MemoryStorage memory) {
    System.out.print("-------------------------------------------------------");
    for (int index = 0; index < memory.getRegionCount(); ++index) {
      if (index % 4 == 0) {
        System.out.println();
      }
      final BitVector region = memory.read(index);
      System.out.printf("%03d: %08x ", index, new BigInteger(region.toByteArray()));
    }
    System.out.println();
  }

}
