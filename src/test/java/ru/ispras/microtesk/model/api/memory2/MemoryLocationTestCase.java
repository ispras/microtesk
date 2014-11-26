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
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

public class MemoryLocationTestCase {

  @Test
  public void test() {
    assertTrue(true);
    
    Memory.setHandlingEnabled(false);
    final Memory mem = Memory.MEM("MEM", Type.CARD(32), 32);
    //printAll(mem);
    
    mem.access(10).store(new Data(BitVector.valueOf(0xFF0000FF, 32), Type.CARD(32)));
    
    System.out.println(mem.access(10).toBinString());
    
    /*
    System.out.println(mem.access(10).bitField(0, 15).toBinString());
    System.out.println(mem.access(10).bitField(16, 31).toBinString());
    
    
    System.out.println(mem.access(10).bitField(0, 7).toBinString());
    System.out.println(mem.access(10).bitField(8, 15).toBinString());
    System.out.println(mem.access(10).bitField(16, 23).toBinString());
    System.out.println(mem.access(10).bitField(24, 31).toBinString());
   */

    final Memory alias1 = Memory.MEM("MEM", Type.CARD(16), 2, mem.access(10));
    printAll(alias1);
    
    final Memory alias2 = Memory.MEM("MEM", Type.CARD(8), 4, mem.access(10));
    printAll(alias2);
    
    final Memory alias3 = Memory.MEM("MEM", Type.CARD(4), 8, mem.access(10));
    printAll(alias3);
    
    final Memory alias4 = Memory.MEM("MEM", Type.CARD(2), 16, mem.access(10));
    printAll(alias4);
    
    final Memory alias5 = Memory.MEM("MEM", Type.CARD(1), 32, mem.access(10));
    printAll(alias5);

  }
  
  private void printAll(Memory m) {
    for (int index = 0; index < m.getLength(); ++index) {
      System.out.println(m.access(index).toBinString());
    }
  }

}
