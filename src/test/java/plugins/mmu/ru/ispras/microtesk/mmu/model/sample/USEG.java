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
import ru.ispras.fortress.solver.function.StandardFunction;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.model.api.Buffer;
import ru.ispras.microtesk.mmu.model.api.Data;
import ru.ispras.microtesk.model.api.instruction.StandardFunctions;

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

public final class USEG implements Buffer<PA, VA> {
  private final DTLB dtlb;
  private final JTLB jtlb;
  private final Pair<BitVector, BitVector> range;

  public USEG(final DTLB dtlb, final JTLB jtlb) {
    InvariantChecks.checkNotNull(dtlb);
    InvariantChecks.checkNotNull(jtlb);

    this.dtlb = dtlb;
    this.jtlb = jtlb;

    this.range = new Pair<>(
      BitVector.valueOf("0000000000000000", 16, /*VA.size*/ 64),
      BitVector.valueOf("000000007fffffff", 16, /*VA.size*/ 64));
  }

  @Override
  public boolean isHit(final VA address) {
    final BitVector value = address.getField("value");
    return range.first.compareTo(value)  <= 0 &&
           range.second.compareTo(value) >= 0;
  }

  @Override
  public PA getData(final VA va) {

    final Data tlbEntry;        // var tlbEntry: JTLB.entry;
    final BitVector evenOddBit; // var evenOddBit: 5;
    final BitVector g;          // var g: 1;
    final BitVector v;          // var v: 1;
    final BitVector d;          // var d: 1;
    final BitVector c;          // var c: 3;
    final BitVector pfn;        // var pfn: 24;

    if (dtlb.isHit(va)) {
      tlbEntry = dtlb.getData(va);
    } else if (jtlb.isHit(va)) {
      tlbEntry = jtlb.getData(va);
    } else {
      StandardFunctions.exception("TLBMiss");
    }

    evenOddBit = BitVector.valueOf(12, 5);

    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PA setData(final VA address, final PA data) {
    // NOT SUPPORTED
    throw new UnsupportedOperationException();
  }
}
