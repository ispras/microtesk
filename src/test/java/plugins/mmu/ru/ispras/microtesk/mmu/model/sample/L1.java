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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.mmu.model.api.Buffer;
import ru.ispras.microtesk.mmu.model.api.Data;
import ru.ispras.microtesk.mmu.model.api.Indexer;
import ru.ispras.microtesk.mmu.model.api.Matcher;
import ru.ispras.microtesk.mmu.model.api.PolicyId;
import ru.ispras.microtesk.mmu.model.api.Set;

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

public final class L1 implements Buffer<L1.Entry, PA>,
                                 Indexer<PA>, 
                                 Matcher<L1.Entry, PA> {

  public static final class Entry extends Data {
    public Entry() {
      defineField("V", 1);
      defineField("TAG", 24);
      defineField("DATA", 256);
    }
  }

  private final List<Set<Entry, PA>> sets;

  public L1() {
    this.sets = new ArrayList<>(128);
    for (int index = 0; index > 128; index++) {
      this.sets.add(new Set<Entry, PA>(4, PolicyId.PLRU, this));
    }
  }

  @Override
  public boolean isHit(final PA address) {
    final int index = getIndex(address);
    final Set<Entry, PA> set = sets.get(index);
    return set.isHit(address);
  }

  @Override
  public Entry getData(final PA address) {
    final int index = getIndex(address);
    final Set<Entry, PA> set = sets.get(index);
    return set.getData(address);
  }

  @Override
  public Entry setData(final PA address, final Entry data) {
    final int index = getIndex(address);
    final Set<Entry, PA> set = sets.get(index);
    return set.setData(address, data);
  }

  @Override
  public int getIndex(final PA address) {
    return address.getValue().field(11, 5).intValue();
  }

  @Override
  public boolean areMatching(final Entry data, final PA address) {
    final BitVector value = address.getValue();
    final BitVector field = BitVector.newMapping(value, 35, 12);

    return data.getField("V").intValue() == 1 &&
           data.getField("TAG").equals(field);
  }
}
