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

import ru.ispras.fortress.data.types.bitvector.BitVector;

import java.math.BigInteger;
import ru.ispras.microtesk.mmu.model.sim.CachePolicy;
import ru.ispras.microtesk.mmu.model.sim.CacheUnit;
import ru.ispras.microtesk.mmu.model.sim.Indexer;
import ru.ispras.microtesk.mmu.model.sim.Matcher;
import ru.ispras.microtesk.mmu.model.sim.Struct;
import ru.ispras.microtesk.mmu.model.sim.StructBase;
import ru.ispras.microtesk.mmu.model.sim.model.L1.Entry;

public final class L1 extends CacheUnit<Entry, PA> {

  public static final class Entry extends StructBase<Entry> implements Struct<Entry> {

    public final BitVector V = BitVector.valueOf(0x0, 1);
    public final BitVector TAG = BitVector.newEmpty(20);
    public final BitVector DATA = BitVector.newEmpty(256);

    public Entry() {
      setEntry(BitVector.newMapping(V, TAG, DATA));
    }

    public Entry(final BitVector value) {
      this();
      assign(value);
    }

    public void assign(final Entry other) {
      this.V.assign(other.V);
      this.TAG.assign(other.TAG);
      this.DATA.assign(other.DATA);
    }

    @Override
    public Entry newStruct(final BitVector value) {
      return new Entry(value);
    }

    @Override
    public String toString() {
      return String.format("Entry [V=0x%s, TAG=0x%s, DATA=0x%s]",
          V.toHexString(),
          TAG.toHexString(),
          DATA.toHexString());
    }
  }

  private static final Indexer<PA> INDEXER = new Indexer<PA>() {
    @Override
    public BitVector getIndex(final PA pa) {
      return pa.value.field(11, 5);
    }
  };

  private static final Matcher<Entry, PA> MATCHER = new Matcher<L1.Entry, PA>() {
    @Override
    public boolean areMatching(final L1.Entry data, final PA pa) {
      return data.V.equals(BitVector.valueOf(0x1, 1))
          && data.TAG.equals(pa.value.field(31, 12));
    }

    @Override
    public L1.Entry assignTag(final L1.Entry data, final PA pa) {
      data.V.assign(BitVector.valueOf(0x1, 1));
      return data;
    }
  };

  public L1(final CachePolicy policy, final L2 next) {
    super(
        new L1.Entry(),
        new PA(),
        new BigInteger("128", 10),
        4,
        policy,
        INDEXER,
        MATCHER,
        next);
  }
}
