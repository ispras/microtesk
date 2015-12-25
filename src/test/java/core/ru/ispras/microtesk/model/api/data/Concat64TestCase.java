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

package ru.ispras.microtesk.model.api.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.memory.Memory.Kind;

public class Concat64TestCase {

  @Test
  public void test() {
    final Memory temp = Memory.def(Kind.VAR, "temp", Type.INT(64), 1);

    temp.access().bitField(31, 0).store(Data.valueOf(Type.INT(32), 0xffffffffL));
    temp.access().bitField(63, 32).store(Data.valueOf(Type.INT(32), 0x77777777));

    trace("temp =%s", temp.access().load().getRawData().toHexString());
    assertEquals(
        BitVector.valueOf("77777777ffffffff", 16, 64),
        temp.access().load().getRawData()
        );

    final Location rtemp = Location.concat(
        temp.access().bitField(63, 32), temp.access().bitField(31, 0));

    trace("rtemp=%s", rtemp.load().getRawData().toHexString());
    assertEquals(
        BitVector.valueOf("ffffffff77777777", 16, 64),
        rtemp.load().getRawData()
        );

    temp.access().store(Location.concat(temp.access().bitField(63, 32), temp.access().bitField(31, 0)).load());

    trace("temp =%s", temp.access().load().getRawData().toHexString());
    assertEquals(
        BitVector.valueOf("ffffffff77777777", 16, 64),
        temp.access().load().getRawData()
        );
  }

  public static void trace(String format, Object... args) {
    System.out.println(String.format(format, args));
  }
}
