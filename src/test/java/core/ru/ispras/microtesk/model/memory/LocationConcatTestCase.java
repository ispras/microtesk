/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.model.memory.Memory.Kind;

public class LocationConcatTestCase {
  public static final Type WORD = Type.def("WORD", Type.INT(32));
  public static final Type DWORD = Type.def("DWORD", Type.INT(64));
  public static final Data DWORD_DATA = Data.valueOf(DWORD, 0xDEADBEEFBAADF00Dl);

  @Test
  public void test() {
    final Memory memory = Memory.def(Kind.MEM, "memory",  WORD, 32);
    final Memory temp = Memory.def(Kind.VAR, "temp", DWORD, 1);

    temp.access().assign(Location.newLocationForConst(DWORD_DATA));

    final Location concatenation = Location.concat(memory.access(1), memory.access(0));
    concatenation.assign(temp.access());

    final Data data = concatenation.load();
    Assert.assertEquals(DWORD_DATA.toHexString(), data.toHexString());
  }
}
