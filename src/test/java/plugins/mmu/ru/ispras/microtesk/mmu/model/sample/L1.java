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

package ru.ispras.microtesk.mmu.model.sample;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.mmu.model.api.Cache;
import ru.ispras.microtesk.mmu.model.api.Data;
import ru.ispras.microtesk.mmu.model.api.Indexer;
import ru.ispras.microtesk.mmu.model.api.Matcher;
import ru.ispras.microtesk.mmu.model.api.PolicyId;

/**
 * <pre><code>
 * buffer L1 (pa: PA)
 *   ways   = 4
 *   sets   = 128
 *   entry  = (V: 1 = 0, TAG: 24, DATA: 256)
 *   index  = pa.value<11..5>
 *   match  = V == 1 && TAG == pa.value<35..12>
 *   policy = PLRU
 * </pre></code>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class L1 extends Cache<L1.Entry, PA> {

  public static final class Entry extends Data {
    public Entry() {
      defineField("V", 1);
      defineField("TAG", 24);
      defineField("DATA", 256);
    }
  }

  private static final Indexer<PA> INDEXER = new Indexer<PA>() {
    @Override
    public BitVector getIndex(final PA address) {
      return address.getValue().field(11, 5);
    }
  };

  private static final Matcher<Entry, PA> MATCHER = new Matcher<Entry, PA>() {
    @Override
    public boolean areMatching(final Entry data, final PA address) {
      final BitVector value = address.getValue();
      final BitVector field = BitVector.newMapping(value, 35, 12);

      return data.getField("V").intValue() == 1 &&
             data.getField("TAG").equals(field);
    }
  };

  public L1() {
    super(
        BigInteger.valueOf(128),
        4,
        PolicyId.PLRU,
        INDEXER,
        MATCHER
        );
   }
}
