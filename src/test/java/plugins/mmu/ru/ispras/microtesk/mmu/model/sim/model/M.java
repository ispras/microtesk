/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim.model;

import java.math.BigInteger;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.mmu.model.sim.Memory;
import ru.ispras.microtesk.mmu.model.sim.Struct;
import ru.ispras.microtesk.mmu.model.sim.StructBase;
import ru.ispras.microtesk.mmu.model.sim.model.M.Entry;

public final class M extends Memory<Entry, PA> {

  public static final class Entry extends StructBase<Entry> implements Struct<Entry> {
    public final BitVector DATA = BitVector.newEmpty(256);

    public Entry() {
      setEntry(BitVector.newMapping(DATA));
    }

    public Entry(final BitVector value) {
      this();
      assign(value);
    }

    public void assign(final Entry other) {
      this.DATA.assign(other.DATA);
    }

    @Override
    public Entry newStruct(final BitVector value) {
      return new Entry(value);
    }

    @Override
    public String toString() {
      return String.format(
          "Entry [DATA=0x%s]",
          DATA.toHexString()
      );
    }
  }

  public M() {
    super(
        new M.Entry(),
        new PA(),
        new BigInteger("100000000", 16)
    );
  }
}
