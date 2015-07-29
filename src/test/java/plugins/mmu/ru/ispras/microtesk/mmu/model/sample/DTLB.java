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
 * buffer DTLB (va: VA) viewof JTLB
 * ways   = 4
 * sets   = 1
 * entry  = (ASID: 8, VPN2: 27, R: 2,               // EntryHi
 *           G0: 1, V0: 1, D0: 1, C0: 3, PFN0: 24,  // EntryLo0
 *           G1: 1, V1: 1, D1: 1, C1: 3, PFN1: 24)  // EntryLo1
 * index  = 0
 * match  = VPN2 == va.value<39..13> // ASID, G and non-4KB pages are unsupported
 * policy = PLRU
 * </pre></code>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class DTLB implements Buffer<DTLB.Entry, VA>,
                                   Indexer<VA>, 
                                   Matcher<DTLB.Entry, VA> {

  public static final class Entry extends Data {
    public Entry() {
      // EntryHi
      setField("ASID", BitVector.newEmpty(8));
      setField("VPN2", BitVector.newEmpty(27));
      setField("R",    BitVector.newEmpty(2));

      // EntryLo0
      setField("G0",   BitVector.newEmpty(1));
      setField("V0",   BitVector.newEmpty(1));
      setField("D0",   BitVector.newEmpty(1));
      setField("C0",   BitVector.newEmpty(3));
      setField("PFN0", BitVector.newEmpty(24));

      // EntryLo1
      setField("G1",   BitVector.newEmpty(1));
      setField("V1",   BitVector.newEmpty(1));
      setField("D1",   BitVector.newEmpty(1));
      setField("C1",   BitVector.newEmpty(3));
      setField("PFN1", BitVector.newEmpty(24));
    }
  }

  private final List<Set<Entry, VA>> sets;

  public DTLB() {
    this.sets = new ArrayList<>(1);
    this.sets.add(new Set<Entry, VA>(4, PolicyId.PLRU, this));
  }

  @Override
  public boolean isHit(final VA address) {
    final int index = getIndex(address);
    final Set<Entry, VA> set = sets.get(index);
    return set.isHit(address);
  }

  @Override
  public Entry getData(final VA address) {
    final int index = getIndex(address);
    final Set<Entry, VA> set = sets.get(index);
    return set.getData(address);
  }

  @Override
  public Entry setData(final VA address, final Entry data) {
    final int index = getIndex(address);
    final Set<Entry, VA> set = sets.get(index);
    return set.setData(address, data);
  }

  @Override
  public int getIndex(final VA address) {
    return 0;
  }

  @Override
  public boolean areMatching(final Entry data, final VA address) {
    // match  = VPN2 == va.value<39..13>
    final BitVector value = address.getValue();
    final BitVector field = BitVector.newMapping(value, 13, 27);

    return data.getField("VPN2").equals(field);
  }
}
