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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.memory.handler.MemoryAccessHandlerEngine;
import ru.ispras.microtesk.model.api.type.Type;

public final class MemoryLocationTestCase {

  public static final Type  LONG = Type.INT(32);
  public static final Type  WORD = Type.CARD(32);
  public static final Type SHORT = Type.INT(16);
  public static final Type HWORD = Type.CARD(16);
  public static final Type SBYTE = Type.INT(8);
  public static final Type  BYTE = Type.CARD(8);
  public static final Type   BIT = Type.CARD(1);

  public static final int COUNT = 32;
  
  @Test
  public void testRandom() {
    MemoryAccessHandlerEngine.setHandlingEnabled(false);

    // Allocates memory
    final Memory mem = Memory.def(Memory.Kind.MEM, "MEM", WORD, COUNT);

    // Creates random data
    final List<Location> etalonData = new ArrayList<Location>(COUNT);
    for (int index = 0; index < COUNT; ++index) {
      final BitVector rawData = BitVector.newEmpty(WORD.getBitSize());
      Randomizer.get().fill(rawData);

      final Data data = new Data(rawData, WORD);
      final Location location = Location.newLocationForConst(data);

      etalonData.add(location);
    }

    // Stores random data
    for (int locationIndex = 0; locationIndex < COUNT; ++locationIndex) {
      mem.access(locationIndex).assign(etalonData.get(locationIndex));
    }

    // Loads data and checks its validity
    for (int locationIndex = 0; locationIndex < COUNT; ++locationIndex) {
      assertEquals(etalonData.get(locationIndex).load(), mem.access(locationIndex).load());
    }

    // Reads data using bit fields and checks its validity
    final int locationSize = mem.getType().getBitSize();
    for (int locationIndex = 0; locationIndex < COUNT; ++locationIndex) {
      int fieldSize = locationSize / 2;
      while (fieldSize >= 1) {
        final int fieldCount = locationSize / fieldSize;
        
        final Location[] fields = new Location[fieldCount];
        for (int fieldIndex = 0; fieldIndex < fieldCount; ++fieldIndex) {
          final int start = fieldSize * fieldIndex;
          final int end = start + fieldSize - 1;
          
          final BitVector expected = 
              BitVector.newMapping(etalonData.get(locationIndex).load().getRawData(), start, fieldSize);

          final Location field = mem.access(locationIndex).bitField(start, end);
          final BitVector current = field.load().getRawData();

          assertEquals(expected, current);
          
          fields[fieldIndex] = field;
        }

        final Location concat = Location.concat(fields);
        assertEquals(etalonData.get(locationIndex).load(), concat.load());

        fieldSize = fieldSize / 2;
      }
    }
  }
  
  @Test
  public void test2() {
    /*
    printAll(mem);
    
    mem.access(10).store(new Data(BitVector.valueOf(0xFF0000FF, 32), WORD));
    
    System.out.println(mem.access(10).toBinString());
    System.out.println(mem.access(10).bitField(0, 15).concat(mem.access(10).bitField(16, 31)).toBinString());
    */
    
    /*
    System.out.println(mem.access(10).bitField(0, 15).toBinString());
    System.out.println(mem.access(10).bitField(16, 31).toBinString());
    
    
    System.out.println(mem.access(10).bitField(0, 7).toBinString());
    System.out.println(mem.access(10).bitField(8, 15).toBinString());
    System.out.println(mem.access(10).bitField(16, 23).toBinString());
    System.out.println(mem.access(10).bitField(24, 31).toBinString());
   */
    
    /*

    final Memory alias1 = Memory.MEM("MEM", Type.CARD(16), 2, mem.access(10));
    printAll(alias1);
    
    final Memory alias2 = Memory.MEM("MEM", Type.CARD(8), 4, mem.access(10));
    printAll(alias2);
    
    final Memory alias3 = Memory.MEM("MEM", Type.CARD(4), 8, mem.access(10));
    printAll(alias3);
    
    final Memory alias4 = Memory.MEM("MEM", Type.CARD(2), 16, mem.access(10));
    printAll(alias4);
    
    final Memory alias5 = Memory.MEM("MEM", Type.CARD(1), 32, mem.access(10));
    printAll(alias5);*/

  }
  
  /*
  private void printAll(Memory m) {
    System.out.println("-----------------------------------");
    System.out.println(m);

    for (int index = 0; index < m.getLength(); ++index) {
      System.out.println(m.access(index).toBinString());
    }
    System.out.println();
  }
  */
}
