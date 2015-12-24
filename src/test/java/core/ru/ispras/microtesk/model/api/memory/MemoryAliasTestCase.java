/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;

public class MemoryAliasTestCase {

  public static final Type WORD = Type.def("WORD", Type.INT(32));
  public static final Type HALF = Type.def("HALF", Type.INT(16));

  public static final Data DEAD = new Data(BitVector.valueOf(0xDEAD, HALF.getBitSize()), HALF);
  public static final Data BEEF = new Data(BitVector.valueOf(0xBEEF, HALF.getBitSize()), HALF);

  @Test
  public void test() {
    final Memory src  = Memory.def(Memory.Kind.REG, "src",  WORD, 32);
    final Memory view = Memory.def(Memory.Kind.REG, "view", HALF, 32, src, 16, 31);

    src.access(20).store(new Data(BitVector.valueOf(0xDEADBEEF, WORD.getBitSize()), WORD));
    print(src.access(20));

    assertEquals(BEEF, view.access(8).load());
    assertEquals(DEAD, view.access(9).load());

    assertEquals(BEEF, view.access(8L).load());
    assertEquals(DEAD, view.access(9L).load());

    assertEquals(BEEF, view.access(addressFor(8)).load());
    assertEquals(DEAD, view.access(addressFor(9)).load());
  }

  private Data addressFor(final int address) {
    return new Data(BitVector.valueOf(address, 5), Type.CARD(5));
  }
  
  private static void print(final Location location) {
    System.out.printf("%X%n", location.getValue());
  }
}
