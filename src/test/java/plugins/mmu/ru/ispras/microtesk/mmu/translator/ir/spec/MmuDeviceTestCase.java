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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.basis.solver.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.AddressView;
import ru.ispras.microtesk.utils.function.Function;

/**
 * Test for {@link MmuDevice}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuDeviceTestCase {
  public static final IntegerVariable VA = new IntegerVariable("VA", 64);
  public static final IntegerVariable PA = new IntegerVariable("PA", 36);
  public static final IntegerVariable isMapped = new IntegerVariable("isMapped", 1);
  public static final IntegerVariable isCached = new IntegerVariable("isCached", 1);
  public static final IntegerVariable V0 = new IntegerVariable("V0", 1);
  public static final IntegerVariable D0 = new IntegerVariable("D0", 1);
  public static final IntegerVariable G0 = new IntegerVariable("G0", 1);
  public static final IntegerVariable C0 = new IntegerVariable("C0", 3);
  public static final IntegerVariable PFN0 = new IntegerVariable("PFN0", 24);
  public static final IntegerVariable V1 = new IntegerVariable("V1", 1);
  public static final IntegerVariable D1 = new IntegerVariable("D1", 1);
  public static final IntegerVariable G1 = new IntegerVariable("G1", 1);
  public static final IntegerVariable C1 = new IntegerVariable("C1", 3);
  public static final IntegerVariable PFN1 = new IntegerVariable("PFN1", 24);
  public static final IntegerVariable V = new IntegerVariable("V", 1);
  public static final IntegerVariable D = new IntegerVariable("D", 1);
  public static final IntegerVariable G = new IntegerVariable("G", 1);
  public static final IntegerVariable C = new IntegerVariable("C", 3);
  public static final IntegerVariable PFN = new IntegerVariable("PFN", 24);
  public static final IntegerVariable DATA = new IntegerVariable("DATA", 8 * 32);

  public static final MmuAddress VA_ADDR = new MmuAddress(VA);
  public static final MmuAddress PA_ADDR = new MmuAddress(PA);

  public static final MmuDevice JTLB = new MmuDevice("JTLB", 64, 1, VA_ADDR,
      MmuExpression.var(VA, 13, 39), // Tag
      MmuExpression.empty(),         // Index
      MmuExpression.var(VA, 0, 12),  // Offset
      null, null, false, null);

  static {
    JTLB.addField(new IntegerVariable("VPN2", 27));

    JTLB.addField(new IntegerVariable("V0", 1));
    JTLB.addField(new IntegerVariable("D0", 1));
    JTLB.addField(new IntegerVariable("G0", 1));
    JTLB.addField(new IntegerVariable("C0", 1));
    JTLB.addField(new IntegerVariable("PFN0", 24));

    JTLB.addField(new IntegerVariable("V1", 1));
    JTLB.addField(new IntegerVariable("D1", 1));
    JTLB.addField(new IntegerVariable("G1", 1));
    JTLB.addField(new IntegerVariable("C1", 1));
    JTLB.addField(new IntegerVariable("PFN1", 24));
  }

  public static final MmuDevice DTLB = new MmuDevice("DTLB", 4, 1, VA_ADDR,
      MmuExpression.var(VA, 13, 39), // Tag
      MmuExpression.empty(),         // Index
      MmuExpression.var(VA, 0, 12),  // Offset
      null, null, true, JTLB);

  static {
    DTLB.addField(new IntegerVariable("VPN2", 27));

    DTLB.addField(new IntegerVariable("V0", 1));
    DTLB.addField(new IntegerVariable("D0", 1));
    DTLB.addField(new IntegerVariable("G0", 1));
    DTLB.addField(new IntegerVariable("C0", 1));
    DTLB.addField(new IntegerVariable("PFN0", 24));

    DTLB.addField(new IntegerVariable("V1", 1));
    DTLB.addField(new IntegerVariable("D1", 1));
    DTLB.addField(new IntegerVariable("G1", 1));
    DTLB.addField(new IntegerVariable("C1", 1));
    DTLB.addField(new IntegerVariable("PFN1", 24));
  }

  public static final AddressView<Long> DTLB_ADDR_VIEW = new AddressView<Long>(
      new Function<Long, List<Long>>() {
        @Override
        public List<Long> apply(final Long address) {
          final List<Long> fields = new ArrayList<>();
          // Tag = VPN2.
          fields.add((address >>> 13) & 0x7FFffffL);
          // Index = 0.
          fields.add(0L);
          // Offset = Select | Offset.
          fields.add(address & 0x1fffL);
          return fields;
        }
      }, new Function<List<Long>, Long>() {
        @Override
        public Long apply(final List<Long> fields) {
          final long tag = fields.get(0) & 0x7FFffffL;
          final long offset = fields.get(2) & 0x1fffL;

          return (tag << 13) | offset;
        }
      });

  public static final MmuDevice L1 = new MmuDevice("L1", 4, 128, PA_ADDR,
      MmuExpression.var(PA, 12, 35), // Tag
      MmuExpression.var(PA, 5, 11), // Index
      MmuExpression.var(PA, 0, 4), // Offset
      null, null, true, null);

  static {
    L1.addField(new IntegerVariable("TAG", 24));
    L1.addField(new IntegerVariable("DATA", 8 * 32));
  }

  public static final AddressView<Long> L1_ADDR_VIEW = new AddressView<Long>(
      new Function<Long, List<Long>>() {
        @Override
        public List<Long> apply(final Long address) {
          final List<Long> fields = new ArrayList<Long>();
          fields.add(((address >>> 12) & 0xFFffffL));
          fields.add((address >>> 5) & 0x7fL);
          fields.add(address & 0x1fL);
          return fields;
        }
      }, new Function<List<Long>, Long>() {
        @Override
        public Long apply(final List<Long> fields) {
          final long tag = fields.get(0) & 0xFFffffL;
          final long index = fields.get(1) & 0x7fL;
          final long offset = fields.get(2) & 0x1fL;

          return (tag << 12) | (index << 5) | offset;
        }
      });

  // -----------------------------------------------------------------------------------------------
  public static final MmuDevice L2 = new MmuDevice("L2", 4, 4096, PA_ADDR,
      MmuExpression.var(PA, 17, 35), // Tag
      MmuExpression.var(PA, 5, 16), // Index
      MmuExpression.var(PA, 0, 4), // Offset
      null, null, true, null);

  static {
    L1.addField(new IntegerVariable("TAG", 19));
    L1.addField(new IntegerVariable("DATA", 8 * 32));
  }

  public static final AddressView<Long> L2_ADDR_VIEW = new AddressView<Long>(
      new Function<Long, List<Long>>() {
        @Override
        public List<Long> apply(final Long address) {
          final List<Long> fields = new ArrayList<Long>();
          fields.add((address >>> 17) & 0x7ffffL);
          fields.add((address >>> 5) & 0xfffL);
          fields.add(address & 0x1fL);
          return fields;
        }
      }, new Function<List<Long>, Long>() {
        @Override
        public Long apply(final List<Long> fields) {
          final long tag = fields.get(0) & 0x7ffffL;
          final long index = fields.get(1) & 0xfffL;
          final long offset = fields.get(2) & 0x1fL;

          return (tag << 17) | (index << 5) | offset;
        }
      });

  public static final MmuDevice MEM = new MmuDevice("MMU", 1, (1L << 36) / 32, PA_ADDR,
      MmuExpression.empty(),        // Tag
      MmuExpression.var(PA, 5, 35), // Index
      MmuExpression.var(PA, 0, 4),  // Offset
      null, null, false, null);

  static {
    MEM.addField(new IntegerVariable("DATA", 8 * 32));
  }

  private void runTest(final MmuDevice device, final AddressView<Long> addressView,
      final long address) {

    System.out.format("Test: %s, %x\n", device.getName(), address);

    final long tagA = addressView.getTag(address);
    final long indexA = addressView.getIndex(address);
    final long offsetA = addressView.getOffset(address);

    final long tagD = device.getTag(address);
    final long indexD = device.getIndex(address);
    final long offsetD = device.getOffset(address);

    System.out.format("tag=%x, index=%x, offset=%x\n", tagA, indexA, offsetA);

    Assert.assertEquals(Long.toHexString(tagA), Long.toHexString(tagD));
    Assert.assertEquals(Long.toHexString(indexA), Long.toHexString(indexD));
    Assert.assertEquals(Long.toHexString(offsetA), Long.toHexString(offsetD));

    final long addressA = addressView.getAddress(tagA, indexA, offsetA);
    final long addressD = device.getAddress(tagD, indexD, offsetD);

    Assert.assertEquals(Long.toHexString(addressA), Long.toHexString(addressD));
  }

  @Test
  public void runTest() {
    final int testCount = 1000;
    for (int i = 0; i < testCount; i++) {
      runTest(DTLB, DTLB_ADDR_VIEW, Randomizer.get().nextLong());
      runTest(L1, L1_ADDR_VIEW, Randomizer.get().nextLong());
      runTest(L2, L2_ADDR_VIEW, Randomizer.get().nextLong());
    }
  }
}
