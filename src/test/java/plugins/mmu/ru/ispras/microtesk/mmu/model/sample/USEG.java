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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.mmu.model.api.Data;
import ru.ispras.microtesk.mmu.model.api.Segment;

/**
 * <pre><code>
 * segment USEG (va: VA) = (pa : PA)
 * range = (0x0000000000000000, 0x000000007fffffff)
 *
 * var tlbEntry: JTLB.entry;
 *
 * var evenOddBit: 5;
 *
 * var g: 1;
 * var v: 1;
 * var d: 1;
 * var c: 3;
 * var pfn: 24;
 *
 * read = {
 *  // The address hits the DTLB.
 *  if DTLB(va).hit then
 *    tlbEntry = DTLB(va);
 *  // The address hits the JTLB.
 *  elif JTLB(va).hit then
 *    tlbEntry = JTLB(va);
 *  // The address does not hit the TLB.
 *  else
 *    exception("TLBMiss");
 *  endif; // If the address hits the DTLB.
 *
 *  // Only 4KB pages are supported.
 *  evenOddBit = 12;
 *
 *  // The VPN is even.
 *  if va.value<evenOddBit> == 0 then
 *    g   = tlbEntry.G0;
 *    v   = tlbEntry.V0;
 *    d   = tlbEntry.D0;
 *    c   = tlbEntry.C0;
 *    pfn = tlbEntry.PFN0;
 *  // The VPN is odd.
 *  else
 *    g   = tlbEntry.G1;
 *    v   = tlbEntry.V1;
 *    d   = tlbEntry.D1;
 *    c   = tlbEntry.C1;
 *    pfn = tlbEntry.PFN1;
 *  endif; // If the VPN is even.
 *
 *  // The EntryLo is valid.
 *  if v == 1 then
 *    pa.value = pfn<24..(evenOddBit - 12)>::va.value<(evenOddBit - 1)..0>;
 *  // The EntryLo is invalid.
 *  else
 *    exception("TLBInvalid");
 *  endif; // If the EntryLo is valid.
 * }
 * </code></pre>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class USEG extends Segment<PA, VA> {

  public USEG() {
    super(
        BitVector.valueOf("0000000000000000", 16, /*VA.size*/ 64),
        BitVector.valueOf("000000007fffffff", 16, /*VA.size*/ 64)
    );
  }

  @SuppressWarnings("unused")
  @Override
  public PA getData(final VA va) {
    PA pa = new PA();

    Data tlbEntry = new JTLB.Entry();
    BitVector evenOddBit = BitVector.newEmpty(5);
    BitVector g = BitVector.newEmpty(1);
    BitVector v = BitVector.newEmpty(1);
    BitVector d = BitVector.newEmpty(1);
    BitVector c = BitVector.newEmpty(3);
    BitVector pfn = BitVector.newEmpty(24);

    if (DTLB.get().isHit(va)) {
      tlbEntry = DTLB.get().getData(va);
    } else if (JTLB.get().isHit(va)) {
      tlbEntry = JTLB.get().getData(va);
    } else {
      exception("TLBMiss");
    }

    evenOddBit = BitVector.valueOf(12, 5);

    if (!va.getValue().getBit(evenOddBit.intValue())) {
      g   = tlbEntry.getField("G0");
      v   = tlbEntry.getField("V0");
      d   = tlbEntry.getField("D0");
      c   = tlbEntry.getField("C0");
      pfn = tlbEntry.getField("PFN0");
    } else {
      g   = tlbEntry.getField("G1");
      v   = tlbEntry.getField("V1");
      d   = tlbEntry.getField("D1");
      c   = tlbEntry.getField("C1");
      pfn = tlbEntry.getField("PFN1");
    }

    if (v.intValue() == 1) {
      pa.setField(
          "value",
          BitVector.newMapping(
              pfn.field(24, evenOddBit.intValue() - 12),
              va.getField("value").field(evenOddBit.intValue() - 1, 0)
          )
      );
    } else {
      exception("TLBInvalid");
    }

    return pa;
  }
}
