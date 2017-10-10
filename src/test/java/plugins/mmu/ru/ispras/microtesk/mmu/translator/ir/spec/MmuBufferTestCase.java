/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.mmu.basis.AddressView;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Var;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.microtesk.utils.function.Function;

/**
 * Test for {@link MmuBuffer}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuBufferTestCase {
  private static MmuAddressInstance newAddress(final String name, int width) {
    final Type type = new Type(name, Collections.singletonMap("value", new Type(width)));

    return new MmuAddressInstance(
        name,
        new Var(name, type),
        new Variable(name + ".value", DataType.BIT_VECTOR(width)));
  }

  public static final MmuAddressInstance VA_ADDR = newAddress("VA", 64);
  public static final MmuAddressInstance PA_ADDR = newAddress("PA", 36);

  public static final Variable VA = VA_ADDR.getVariable();
  public static final Variable PA = PA_ADDR.getVariable();

  public static final Variable isMapped = new Variable("isMapped", DataType.BIT_VECTOR(1));
  public static final Variable isCached = new Variable("isCached", DataType.BIT_VECTOR(1));
  public static final Variable VPN2 = new Variable("VPN2", DataType.BIT_VECTOR(27));
  public static final Variable V0 = new Variable("V0", DataType.BIT_VECTOR(1));
  public static final Variable D0 = new Variable("D0", DataType.BIT_VECTOR(1));
  public static final Variable G0 = new Variable("G0", DataType.BIT_VECTOR(1));
  public static final Variable C0 = new Variable("C0", DataType.BIT_VECTOR(3));
  public static final Variable PFN0 = new Variable("PFN0", DataType.BIT_VECTOR(24));
  public static final Variable V1 = new Variable("V1", DataType.BIT_VECTOR(1));
  public static final Variable D1 = new Variable("D1", DataType.BIT_VECTOR(1));
  public static final Variable G1 = new Variable("G1", DataType.BIT_VECTOR(1));
  public static final Variable C1 = new Variable("C1", DataType.BIT_VECTOR(3));
  public static final Variable PFN1 = new Variable("PFN1", DataType.BIT_VECTOR(24));
  public static final Variable V = new Variable("V", DataType.BIT_VECTOR(1));
  public static final Variable D = new Variable("D", DataType.BIT_VECTOR(1));
  public static final Variable G = new Variable("G", DataType.BIT_VECTOR(1));
  public static final Variable C = new Variable("C", DataType.BIT_VECTOR(3));
  public static final Variable PFN = new Variable("PFN", DataType.BIT_VECTOR(24));
  public static final Variable L1_TAG = new Variable("TAG1", DataType.BIT_VECTOR(24));
  public static final Variable L2_TAG = new Variable("TAG2", DataType.BIT_VECTOR(24));
  public static final Variable L1_DATA = new Variable("DATA1", DataType.BIT_VECTOR(8 * 32));
  public static final Variable L2_DATA = new Variable("DATA2", DataType.BIT_VECTOR(8 * 32));
  public static final Variable DATA = new Variable("DATA", DataType.BIT_VECTOR(8 * 32));

  public static final MmuBuffer JTLB = new MmuBuffer(
      "JTLB", MmuBuffer.Kind.UNMAPPED, 64, 1, VA_ADDR,
      FortressUtils.makeNodeExtract(VA, 13, 39), // Tag
      NodeValue.newBitVector(BitVector.newEmpty(1)), // Index
      FortressUtils.makeNodeExtract(VA, 0, 12), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(VPN2),
              FortressUtils.makeNodeExtract(VA, 13, 39))),
      false, null
      );

  static {
    JTLB.addField(VPN2);

    JTLB.addField(V0);
    JTLB.addField(D0);
    JTLB.addField(G0);
    JTLB.addField(C0);
    JTLB.addField(PFN0);

    JTLB.addField(V1);
    JTLB.addField(D1);
    JTLB.addField(G1);
    JTLB.addField(C1);
    JTLB.addField(PFN1);
  }

  public static final MmuBuffer DTLB = new MmuBuffer(
      "DTLB", MmuBuffer.Kind.UNMAPPED, 4, 1, VA_ADDR,
      FortressUtils.makeNodeExtract(VA, 13, 39), // Tag
      NodeValue.newBitVector(BitVector.newEmpty(1)), // Index
      FortressUtils.makeNodeExtract(VA, 0, 12), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(VPN2),
              FortressUtils.makeNodeExtract(VA, 13, 39))),
      true, JTLB
      );

  static {
    DTLB.addField(VPN2);

    DTLB.addField(V0);
    DTLB.addField(D0);
    DTLB.addField(G0);
    DTLB.addField(C0);
    DTLB.addField(PFN0);

    DTLB.addField(V1);
    DTLB.addField(D1);
    DTLB.addField(G1);
    DTLB.addField(C1);
    DTLB.addField(PFN1);
  }

  public static final AddressView<BitVector> DTLB_ADDR_VIEW = new AddressView<>(
      new Function<BitVector, List<BitVector>>() {
        @Override
        public List<BitVector> apply(final BitVector address) {
          final List<BitVector> fields = new ArrayList<>();
          // Tag = VPN2.
          fields.add(BitVector.valueOf((address.longValue() >>> 13) & 0x7FFffffL, 27));
          // Index = 0.
          fields.add(BitVector.newEmpty(1));
          // Offset = Select | Offset.
          fields.add(BitVector.valueOf(address.longValue() & 0x1fffL, 13));
          return fields;
        }
      },
      new Function<List<BitVector>, BitVector>() {
        @Override
        public BitVector apply(final List<BitVector> fields) {
          final long tag = fields.get(0).longValue() & 0x7FFffffL;
          final long offset = fields.get(2).longValue() & 0x1fffL;

          return BitVector.valueOf((tag << 13) | offset, 64);
        }
      });

  public static final MmuBuffer L1 = new MmuBuffer(
      "L1", MmuBuffer.Kind.UNMAPPED, 4, 128, PA_ADDR,
      FortressUtils.makeNodeExtract(PA, 12, 35), // Tag
      FortressUtils.makeNodeExtract(PA, 5, 11), // Index
      FortressUtils.makeNodeExtract(PA, 0, 4), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(L1_TAG),
              FortressUtils.makeNodeExtract(PA, 12, 35))),
      true, null
      );

  static {
    L1.addField(L1_TAG);
    L1.addField(L1_DATA);
  }

  public static final AddressView<BitVector> L1_ADDR_VIEW = new AddressView<BitVector>(
      new Function<BitVector, List<BitVector>>() {
        @Override
        public List<BitVector> apply(final BitVector address) {
          final List<BitVector> fields = new ArrayList<>();
          fields.add(BitVector.valueOf(((address.longValue() >>> 12) & 0xFFffffL), 24));
          fields.add(BitVector.valueOf((address.longValue() >>> 5) & 0x7fL, 7));
          fields.add(BitVector.valueOf(address.longValue() & 0x1fL, 5));
          return fields;
        }
      },
      new Function<List<BitVector>, BitVector>() {
        @Override
        public BitVector apply(final List<BitVector> fields) {
          final long tag = fields.get(0).longValue() & 0xFFffffL;
          final long index = fields.get(1).longValue() & 0x7fL;
          final long offset = fields.get(2).longValue() & 0x1fL;

          return BitVector.valueOf((tag << 12) | (index << 5) | offset, 36);
        }
      });

  // -----------------------------------------------------------------------------------------------
  public static final MmuBuffer L2 = new MmuBuffer(
      "L2", MmuBuffer.Kind.UNMAPPED, 4, 4096, PA_ADDR,
      FortressUtils.makeNodeExtract(PA, 17, 35), // Tag
      FortressUtils.makeNodeExtract(PA, 5, 16), // Index
      FortressUtils.makeNodeExtract(PA, 0, 4), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(L2_TAG),
              FortressUtils.makeNodeExtract(PA, 17, 35))),
      true, null
      );

  static {
    L2.addField(L2_TAG);
    L2.addField(L2_DATA);
  }

  public static final AddressView<BitVector> L2_ADDR_VIEW = new AddressView<BitVector>(
      new Function<BitVector, List<BitVector>>() {
        @Override
        public List<BitVector> apply(final BitVector address) {
          final List<BitVector> fields = new ArrayList<BitVector>();
          fields.add(BitVector.valueOf((address.longValue() >>> 17) & 0x7ffffL, 19));
          fields.add(BitVector.valueOf((address.longValue() >>> 5) & 0xfffL, 12));
          fields.add(BitVector.valueOf(address.longValue() & 0x1fL, 5));
          return fields;
        }
      },
      new Function<List<BitVector>, BitVector>() {
        @Override
        public BitVector apply(final List<BitVector> fields) {
          final long tag = fields.get(0).longValue() & 0x7ffffL;
          final long index = fields.get(1).longValue() & 0xfffL;
          final long offset = fields.get(2).longValue() & 0x1fL;

          return BitVector.valueOf((tag << 17) | (index << 5) | offset, 36);
        }
      });

  public static final MmuBuffer MEM = new MmuBuffer(
      "MMU", MmuBuffer.Kind.UNMAPPED, 1, (1L << 36) / 32, PA_ADDR,
      NodeValue.newBitVector(BitVector.newEmpty(1)), // Tag
      FortressUtils.makeNodeExtract(PA, 5, 35), // Index
      FortressUtils.makeNodeExtract(PA, 0, 4), // Offset
      Collections.<MmuBinding>emptySet(),
      false, null
      );

  static {
    MEM.addField(DATA);
  }

  private void runTest(
      final MmuBuffer device,
      final AddressView<BitVector> addressView,
      final BitVector address) {
    System.out.format("Test: %s, %s\n", device.getName(), address.toHexString());

    final BitVector tagA = addressView.getTag(address);
    final BitVector indexA = addressView.getIndex(address);
    final BitVector offsetA = addressView.getOffset(address);

    final BitVector tagD = device.getTag(address);
    final BitVector indexD = device.getIndex(address);
    final BitVector offsetD = device.getOffset(address);

    System.out.format("Spec: tag=%s, index=%s, offset=%s%n",
        tagA.toHexString(), indexA.toHexString(), offsetA.toHexString());
    System.out.format("Impl: tag=%s, index=%s, offset=%s%n",
        tagD.toHexString(), indexD.toHexString(), offsetD.toHexString());

    Assert.assertEquals(tagA.toHexString(), tagD.toHexString());
    Assert.assertEquals(indexA.toHexString(), indexD.toHexString());
    Assert.assertEquals(offsetA.toHexString(), offsetD.toHexString());

    final BitVector addressA = addressView.getAddress(tagA, indexA, offsetA);
    final BitVector addressD = device.getAddress(tagD, indexD, offsetD);

    System.out.format("Spec: address=%s%n", addressA.toHexString());
    System.out.format("Impl: address=%s%n", addressD.toHexString());

    Assert.assertEquals(addressA.toHexString(), addressD.toHexString());
  }

  @Test
  public void runTest() {
    final int testCount = 1000;
    for (int i = 0; i < testCount; i++) {
      runTest(DTLB, DTLB_ADDR_VIEW, BitVector.valueOf(Randomizer.get().nextLong(), 64));
      runTest(L1, L1_ADDR_VIEW, BitVector.valueOf(Randomizer.get().nextLong(), 36));
      runTest(L2, L2_ADDR_VIEW, BitVector.valueOf(Randomizer.get().nextLong(), 36));
    }
  }
}
