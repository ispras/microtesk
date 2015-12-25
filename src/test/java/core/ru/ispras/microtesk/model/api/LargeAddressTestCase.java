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

package ru.ispras.microtesk.model.api;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.memory.Memory;

public class LargeAddressTestCase {
  public static final Type WORD48 = Type.def("WORD48", Type.CARD(0x30));
  public static final Type WORD64 = Type.def("WORD64", Type.CARD(0x40));

  public static final Memory GPR48 = Memory.def(Memory.Kind.REG, "GPR48", WORD48, 0x30);
  public static final Memory GPR64 = Memory.def(Memory.Kind.REG, "GPR64", WORD64, 0x40);
  public static final Memory M48 = Memory.def(Memory.Kind.MEM, "M48", WORD48, 0x1000000000000L);
  public static final Memory M64 = Memory.def(Memory.Kind.MEM, "M64", WORD64, new BigInteger("10000000000000000", 16));

  public static final Memory[] __REGISTERS = {GPR48, GPR64};
  public static final Memory[] __MEMORY = {M48, M64};

  @Test
  public void test() {
    M48.access(0x800000000000L).store(Data.valueOf(WORD48, 0xffffffffffffL));
    assertEquals(new Data(BitVector.valueOf(-1L, WORD48.getBitSize()), WORD48), M48.access(0x800000000000L).load());

    M48.access(0x800000000000L).store(Data.valueOf(WORD48, 0xdeadbeefbaadL));
    assertEquals(new Data(BitVector.valueOf(0xdeadbeefbaadL, WORD48.getBitSize()), WORD48), M48.access(0x800000000000L).load());

    M64.access(new BigInteger("8000000000000000", 16)).store(Data.valueOf(WORD64, new BigInteger("ffffffffffffffff", 16)));
    assertEquals(new Data(BitVector.valueOf(-1L, WORD64.getBitSize()), WORD64), M64.access(new BigInteger("8000000000000000", 16)).load());

    M64.access(new BigInteger("8000000000000001", 16)).store(Data.valueOf(WORD64, new BigInteger("deadfeedbaadf00d", 16)));
    assertEquals(new Data(BitVector.valueOf(0xdeadfeedbaadf00dL, WORD64.getBitSize()), WORD64), M64.access(new BigInteger("8000000000000001", 16)).load());

    GPR64.access(0).store(Data.valueOf(WORD64, new BigInteger("8000000000000002", 16)));
    M64.access(GPR64.access(0).load()).store(Data.valueOf(WORD64, new BigInteger("deadfeedbaadf00d", 16)));
    assertEquals(new Data(BitVector.valueOf(0xdeadfeedbaadf00dL, WORD64.getBitSize()), WORD64), M64.access(new BigInteger("8000000000000002", 16)).load());
  }
}
