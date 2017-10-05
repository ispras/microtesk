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

package ru.ispras.microtesk.mmu.test.engine;

import java.util.Collections;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.engine.memory.Access;
import ru.ispras.microtesk.mmu.test.engine.memory.AccessPath;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Var;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuProgram;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link MmuUnderTest} represents a simplified MMU of a MIPS-compatible microprocessor.
 * 
 * <p>The logic is as follows.</p>
 * 
 * <pre>{@code
 * if (!XUSEG(VA).hit) {
 *   PA = translate(VA)
 *   C = cachePolicy(VA)
 * } else {
 *   if (DTLB(VA).hit) {
 *     entry = DTLB(VA)
 *   } else if (JTLB(VA).hit) {
 *     entry = JTLB(VA)
 *   } else {
 *     exception(TLBMiss)
 *   }
 *   V   = VA[12] == 0 ? entry.V0   : entry.V1;
 *   D   = VA[12] == 0 ? entry.D0   : entry.D1;
 *   C   = VA[12] == 0 ? entry.C0   : entry.C1;
 *   PFN = VA[12] == 0 ? entry.PFN0 : entry.PFN1;
 *   if (V == 1) {
 *     if (D == 1 || op == load) {
 *       PA = PFN::VA[11..0]
 *     } else {
 *       exception(TLBModified)
 *     }
 *   } else {
 *     exception(TLBInvalid)
 *   }
 * }
 * if (isCached(C) == true) {
 *   if (L1(PA).hit) {
 *     L1(PA)
 *   } else {
 *     if (isSecondaryBypass(C) == false) {
 *       if (L2(PA).hit) {
 *         L2(PA)
 *       }
 *     }
 *     MEM(PA)
 *   }
 * }
 * }</pre>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuUnderTest {
  public static final boolean REDUCE = true;

  public static final int VA_BITS = 64;
  public static final int PA_BITS = 36;
  public static final int JTLB_SIZE = 64;
  public static final int MTLB_SIZE = 4;
  public static final int LINE = 32;
  public static final int POS_BITS = Integer.bitCount(LINE - 1);
  public static final int POS_MASK = LINE - 1;
  public static final int L1_WAYS = 4;
  public static final int L1_SETS = 128;
  public static final int L1_ROW_BITS = Integer.bitCount(L1_SETS - 1);
  public static final int L1_TAG_BITS = PA_BITS - (POS_BITS + L1_ROW_BITS);
  public static final int L2_WAYS = 4;
  public static final int L2_SETS = 4096;
  public static final int L2_ROW_BITS = Integer.bitCount(L2_SETS - 1);
  public static final int L2_TAG_BITS = PA_BITS - (POS_BITS + L2_ROW_BITS);

  // The instance should be created in get().
  private static MmuUnderTest instance = null;

  public static MmuUnderTest get() {
    if (instance == null) {
      instance = new MmuUnderTest();
    }
    return instance;
  }


  private static MmuAddressInstance newAddress(final String name, int width) {
    final Type type = new Type(name, Collections.singletonMap("value", new Type(width)));

    return new MmuAddressInstance(
        name,
        new Var(name, type),
        new Variable(name + ".value", DataType.BIT_VECTOR(width)));
  }

  //================================================================================================
  // Variables
  //================================================================================================

  public final MmuAddressInstance vaAddr = newAddress("VA", 64);
  public final MmuAddressInstance paAddr = newAddress("PA", 36);

  public final Variable va = vaAddr.getVariable();
  public final Variable pa = paAddr.getVariable();

  public final Variable kseg0Cp = new Variable("KSEG0_CP", DataType.BIT_VECTOR(3));

  public final Variable vpn2 = new Variable("VPN2", DataType.BIT_VECTOR(27));
  public final Variable v0 = new Variable("V0", DataType.BIT_VECTOR(1));
  public final Variable d0 = new Variable("D0", DataType.BIT_VECTOR(1));
  public final Variable g0 = new Variable("G0", DataType.BIT_VECTOR(1));
  public final Variable c0 = new Variable("C0", DataType.BIT_VECTOR(3));
  public final Variable pfn0 = new Variable("PFN0", DataType.BIT_VECTOR(24));
  public final Variable v1 = new Variable("V1", DataType.BIT_VECTOR(1));
  public final Variable d1 = new Variable("D1", DataType.BIT_VECTOR(1));
  public final Variable g1 = new Variable("G1", DataType.BIT_VECTOR(1));
  public final Variable c1 = new Variable("C1", DataType.BIT_VECTOR(3));
  public final Variable pfn1 = new Variable("PFN1", DataType.BIT_VECTOR(24));
  public final Variable v = new Variable("V", DataType.BIT_VECTOR(1));
  public final Variable d = new Variable("D", DataType.BIT_VECTOR(1));
  public final Variable g = new Variable("G", DataType.BIT_VECTOR(1));
  public final Variable c = new Variable("C", DataType.BIT_VECTOR(3));
  public final Variable pfn = new Variable("PFN", DataType.BIT_VECTOR(24));
  public final Variable l1Tag = new Variable("TAG1", DataType.BIT_VECTOR(24));
  public final Variable l2Tag = new Variable("TAG2", DataType.BIT_VECTOR(19));
  public final Variable l1Data = new Variable("DATA1", DataType.BIT_VECTOR(8 * 32));
  public final Variable l2Data = new Variable("DATA2", DataType.BIT_VECTOR(8 * 32));
  public final Variable data = new Variable("DATA", DataType.BIT_VECTOR(8 * 32));

  //================================================================================================
  // Segments
  //================================================================================================

  public final MmuSegment xuseg = new MmuSegment(
      "XUSEG",
      vaAddr,
      paAddr,
      BitVector.valueOf("0000000080000000", 16, 64),
      BitVector.valueOf("000000ffFFFFffff", 16, 64)
      );

  public final MmuSegment kseg0 = new MmuSegment(
      "KSEG0",
      vaAddr,
      paAddr,
      BitVector.valueOf("ffffFFFF80000000", 16, 64),
      BitVector.valueOf("ffffFFFF9fffFFFF", 16, 64)
      );

  public final MmuSegment kseg1 = new MmuSegment(
      "KSEG1",
      vaAddr,
      paAddr,
      BitVector.valueOf("ffffFFFFa0000000", 16, 64),
      BitVector.valueOf("ffffFFFFbfffFFFF", 16, 64)
      );

  public final MmuSegment xkphys = new MmuSegment(
      "XKPHYS",
      vaAddr,
      paAddr,
      BitVector.valueOf("8000000000000000", 16, 64),
      BitVector.valueOf("bfffFFFFffffFFFF", 16, 64)
      );

  //================================================================================================
  // Buffers
  //================================================================================================

  public final MmuBuffer jtlb = new MmuBuffer(
      "JTLB", MmuBuffer.Kind.UNMAPPED, JTLB_SIZE, 1, vaAddr,
      FortressUtils.makeNodeExtract(va, 13, 39), // Tag
      FortressUtils.makeNodeBitVector(BitVector.newEmpty(1)), // Index
      FortressUtils.makeNodeExtract(va, 0, 12), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(vpn2),
              FortressUtils.makeNodeExtract(va, 13, 39))),
      false, null);

  {
    jtlb.addField(vpn2);

    jtlb.addField(v0);
    jtlb.addField(d0);
    jtlb.addField(g0);
    jtlb.addField(c0);
    jtlb.addField(pfn0);

    jtlb.addField(v1);
    jtlb.addField(d1);
    jtlb.addField(g1);
    jtlb.addField(c1);
    jtlb.addField(pfn1);
  }

  // -----------------------------------------------------------------------------------------------
  public final Predicate<Access> dtlbGuard = new Predicate<Access>() {
    @Override public boolean test(final Access access) {
      final AccessPath path = access.getPath();

      boolean v = false;
      for (final AccessPath.Entry entry : path.getEntries()) {
        final MmuProgram program = entry.getProgram();
        final MmuTransition transition = program.getTransition();

        if (transition == MmuUnderTest.get().ifValid) {
          v = true;
          break;
        }
      }

      return v;
    }
  };

  public final MmuBuffer dtlb = new MmuBuffer(
      "DTLB", MmuBuffer.Kind.UNMAPPED, MTLB_SIZE, 1, vaAddr,
      FortressUtils.makeNodeExtract(va, 13, 39), // Tag
      FortressUtils.makeNodeBitVector(BitVector.newEmpty(1)), // Index
      FortressUtils.makeNodeExtract(va, 0, 12), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(vpn2),
              FortressUtils.makeNodeExtract(va, 13, 39))),
      true, jtlb
      );
  {
    dtlb.addField(new Variable("VPN2", DataType.BIT_VECTOR(27)));

    dtlb.addField(new Variable("V0", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("D0", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("G0", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("C0", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("PFN0", DataType.BIT_VECTOR(24)));

    dtlb.addField(new Variable("V1", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("D1", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("G1", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("C1", DataType.BIT_VECTOR(1)));
    dtlb.addField(new Variable("PFN1", DataType.BIT_VECTOR(24)));
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuBuffer l1 = new MmuBuffer(
      "L1", MmuBuffer.Kind.UNMAPPED, L1_WAYS, L1_SETS, paAddr,
      FortressUtils.makeNodeExtract(pa, POS_BITS + L1_ROW_BITS, PA_BITS - 1), // Tag
      FortressUtils.makeNodeExtract(pa, POS_BITS, POS_BITS + L1_ROW_BITS - 1), // Index
      FortressUtils.makeNodeExtract(pa, 0, POS_BITS - 1), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(l1Tag),
              FortressUtils.makeNodeExtract(pa, POS_BITS + L1_ROW_BITS, PA_BITS - 1))),
      true, null
      );
  {
    l1.addField(l1Tag);
    l1.addField(l1Data);
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuBuffer l2 = new MmuBuffer(
      "L2", MmuBuffer.Kind.UNMAPPED, L1_WAYS, L1_SETS, paAddr,
      FortressUtils.makeNodeExtract(pa, POS_BITS + L2_ROW_BITS, PA_BITS - 1), // Tag
      FortressUtils.makeNodeExtract(pa, POS_BITS, POS_BITS + L2_ROW_BITS - 1), // Index
      FortressUtils.makeNodeExtract(pa, 0, POS_BITS - 1), // Offset
      Collections.singleton(
          new MmuBinding(
              FortressUtils.makeNodeVariable(l2Tag),
              FortressUtils.makeNodeExtract(pa, POS_BITS + L2_ROW_BITS, PA_BITS - 1))),
      true, null
      );
  {
    l2.addField(l2Tag);
    l2.addField(l2Data);
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuBuffer mem = new MmuBuffer(
      "MEM", MmuBuffer.Kind.UNMAPPED, 1, (1L << PA_BITS) / 32, paAddr,
      FortressUtils.makeNodeBitVector(BitVector.newEmpty(1)), // Tag
      FortressUtils.makeNodeExtract(pa, POS_BITS, PA_BITS - 1), // Index
      FortressUtils.makeNodeExtract(pa, 0, POS_BITS - 1), // Offset
      Collections.<MmuBinding>emptySet(),
      false, null
      );
  {
    mem.addField(data);
  }

  // ===============================================================================================
  // Actions
  // ===============================================================================================

  public final MmuAction root = new MmuAction("ROOT",
      new MmuBinding(
          FortressUtils.makeNodeVariable(va)));
  public final MmuAction start = new MmuAction("START");
  public final MmuAction getUpaKseg = new MmuAction("GET_UPA_KSEG",
      new MmuBinding(
          FortressUtils.makeNodeVariable(pa),
          FortressUtils.makeNodeExtract(va, 0, 28)));
  public final MmuAction getUpaXkphys = new MmuAction("GET_UPA_XKPHYS",
      new MmuBinding(
          FortressUtils.makeNodeVariable(pa),
          FortressUtils.makeNodeExtract(va, 0, 35)));
  public final MmuAction startDtlb = new MmuAction("START_DTLB");
  public final MmuAction hitDtlb = new MmuAction(
      "HIT_DTLB",
      defaultAccess(dtlb, BufferAccessEvent.READ),
      new MmuBinding(
          FortressUtils.makeNodeVariable(v0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(d0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(c0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(pfn0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(v1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(d1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(c1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(pfn1)));
  public final MmuAction startJtlb = new MmuAction("START_JTLB");
  public final MmuAction hitJtlb = new MmuAction(
      "HIT_JTLB",
      defaultAccess(jtlb, BufferAccessEvent.READ),
      new MmuBinding(
          FortressUtils.makeNodeVariable(v0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(d0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(c0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(pfn0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(v1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(d1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(c1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(pfn1)));
  public final MmuAction selectVpn = new MmuAction("SELECT_VPN");
  public final MmuAction getLo0 = new MmuAction("GET_LO0",
      new MmuBinding(
          FortressUtils.makeNodeVariable(v),
          FortressUtils.makeNodeVariable(v0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(d),
          FortressUtils.makeNodeVariable(d0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(c),
          FortressUtils.makeNodeVariable(c0)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(pfn),
          FortressUtils.makeNodeVariable(pfn0)));
  public final MmuAction getLo1 = new MmuAction("GET_LO1",
      new MmuBinding(
          FortressUtils.makeNodeVariable(v),
          FortressUtils.makeNodeVariable(v1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(d),
          FortressUtils.makeNodeVariable(d1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(c),
          FortressUtils.makeNodeVariable(c1)),
      new MmuBinding(
          FortressUtils.makeNodeVariable(pfn),
          FortressUtils.makeNodeVariable(pfn1)));
  public final MmuAction checkV = new MmuAction("CHECK_V");
  public final MmuAction checkD = new MmuAction("CHECK_D");
  public final MmuAction checkG = new MmuAction("CHECK_G");
  public final MmuAction local = new MmuAction("LOCAL");
  public final MmuAction global = new MmuAction("GLOBAL");
  public final MmuAction getMpa = new MmuAction("GET_MPA",
      new MmuBinding(
          FortressUtils.makeNodeVariable(pa),
          FortressUtils.makeNodeLittleEndianConcat(
              FortressUtils.makeNodeVariable(pfn),
              FortressUtils.makeNodeExtract(va, 0, 11))));
  public final MmuAction checkSegment = new MmuAction("CHECK_SEGMENT");
  public final MmuAction startKseg0 = new MmuAction("START_KSEG0",
      new MmuBinding(
          FortressUtils.makeNodeVariable(c),
          FortressUtils.makeNodeVariable(kseg0Cp)));
  public final MmuAction startXkphys = new MmuAction("START_XKPHYS",
      new MmuBinding(
          FortressUtils.makeNodeVariable(c),
          FortressUtils.makeNodeExtract(va, 59, 61)));
  public final MmuAction startCache = new MmuAction("START_CACHE");
  public final MmuAction startL1 = new MmuAction("START_L1");
  public final MmuAction hitL1 = new MmuAction(
      "HIT_L1",
      defaultAccess(l1, BufferAccessEvent.READ),
      new MmuBinding(
          FortressUtils.makeNodeVariable(data)));
  public final MmuAction checkL2 = new MmuAction("CHECK_L2");
  public final MmuAction startL2 = new MmuAction("START_L2");
  public final MmuAction hitL2 = new MmuAction(
      "HIT_L2",
      defaultAccess(l2, BufferAccessEvent.READ),
      new MmuBinding(
          FortressUtils.makeNodeVariable(data)));
  public final MmuAction startMem = new MmuAction(
      "START_MEM",
      defaultAccess(mem, BufferAccessEvent.READ),
      new MmuBinding(
          FortressUtils.makeNodeVariable(data)));
  public final MmuAction tlbRefill = new MmuAction("TLB_REFILL");
  public final MmuAction tlbInvalid = new MmuAction("TLB_INVALID");
  public final MmuAction tlbModified = new MmuAction("TLB_MODIFIED");
  public final MmuAction stop = new MmuAction("STOP");

  // ===============================================================================================
  // Transitions
  // ===============================================================================================

  public final MmuTransition ifRead = new MmuTransition(root, start,
      new MmuGuard(MemoryOperation.LOAD));
  public final MmuTransition ifWrite = new MmuTransition(root, start,
      new MmuGuard(MemoryOperation.STORE));
  public final MmuTransition ifUnmappedKseg0 = new MmuTransition(start, getUpaKseg,
      new MmuGuard(kseg0, true));
  public final MmuTransition ifUnmappedKseg1 = new MmuTransition(start, getUpaKseg,
      new MmuGuard(kseg1, true));
  public final MmuTransition ifUnmappedXkphys = new MmuTransition(start, getUpaXkphys,
      new MmuGuard(xkphys, true));
  public final MmuTransition ifMapped = new MmuTransition(start, startDtlb,
      new MmuGuard(xuseg, true));
  public final MmuTransition afterUpaKseg = new MmuTransition(getUpaKseg, checkSegment);
  public final MmuTransition afterUpaXkphys = new MmuTransition(getUpaXkphys, checkSegment);
  public final MmuTransition ifDtlbMiss = new MmuTransition(startDtlb, startJtlb,
      new MmuGuard(defaultAccess(dtlb, BufferAccessEvent.MISS)));
  public final MmuTransition ifDtlbHit = new MmuTransition(startDtlb, hitDtlb,
      new MmuGuard(defaultAccess(dtlb, BufferAccessEvent.HIT)));
  public final MmuTransition afterDtlb = new MmuTransition(hitDtlb, selectVpn);
  public final MmuTransition ifJtlbMiss = new MmuTransition(startJtlb, tlbRefill,
      new MmuGuard(defaultAccess(jtlb, BufferAccessEvent.MISS)));
  public final MmuTransition ifJtlbHit = new MmuTransition(startJtlb, hitJtlb,
      new MmuGuard(defaultAccess(jtlb, BufferAccessEvent.HIT)));
  public final MmuTransition afterJtlb = new MmuTransition(hitJtlb, selectVpn);
  public final MmuTransition ifVpn0 = new MmuTransition(selectVpn, getLo0,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(va, 12),
              FortressUtils.makeNodeBitVector(BitVector.newEmpty(1)))));
  public final MmuTransition ifVpn1 = new MmuTransition(selectVpn, getLo1,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(va, 12),
              FortressUtils.makeNodeInteger(1))));
  public final MmuTransition afterLo0 = new MmuTransition(getLo0, checkG);
  public final MmuTransition afterLo1 = new MmuTransition(getLo1, checkG);
  public final MmuTransition ifLocal = new MmuTransition(checkG, local,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeVariable(g),
              FortressUtils.makeNodeBitVector(BitVector.newEmpty(1)))));
  public final MmuTransition ifGlobal = new MmuTransition(checkG, global,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeVariable(g),
              FortressUtils.makeNodeInteger(1))));
  public final MmuTransition afterLocal = new MmuTransition(local, checkV);
  public final MmuTransition afterGlobal = new MmuTransition(global, checkV);
  public final MmuTransition ifInvalid = new MmuTransition(checkV, tlbInvalid,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeVariable(v),
              FortressUtils.makeNodeBitVector(BitVector.newEmpty(1)))));
  public final MmuTransition ifValid = new MmuTransition(checkV, checkD,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeVariable(v),
              FortressUtils.makeNodeInteger(1))));
  public final MmuTransition ifDirty = new MmuTransition(checkD, tlbModified,
      new MmuGuard(
          MemoryOperation.STORE,
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeVariable(d),
              FortressUtils.makeNodeBitVector(BitVector.newEmpty(1)))));
  public final MmuTransition ifClean = new MmuTransition(checkD, getMpa,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeVariable(d),
              FortressUtils.makeNodeInteger(1))));
  public final MmuTransition afterMpa = new MmuTransition(getMpa, checkSegment);
  public final MmuTransition ifKseg0 = new MmuTransition(checkSegment, startKseg0,
      new MmuGuard(kseg0, true));
  public final MmuTransition ifKseg1 = new MmuTransition(checkSegment, startMem,
      new MmuGuard(kseg1, true));
  public final MmuTransition ifXkphys = new MmuTransition(checkSegment, startXkphys,
      new MmuGuard(xkphys, true));
  public final MmuTransition ifXuseg = new MmuTransition(checkSegment, startCache,
      new MmuGuard(xuseg, true));
  public final MmuTransition afterKseg0 = new MmuTransition(startKseg0, startCache);
  public final MmuTransition afterXkphys = new MmuTransition(startXkphys, startCache);
  public final MmuTransition ifUncached = new MmuTransition(startCache, startMem,
      new MmuGuard(
          FortressUtils.makeNodeEqual(
              FortressUtils.makeNodeExtract(c, 0, 1),
              FortressUtils.makeNodeInteger(0x2))));
  public final MmuTransition ifCached = new MmuTransition(startCache, startL1,
      new MmuGuard(
          FortressUtils.makeNodeNotEqual(
              FortressUtils.makeNodeExtract(c, 0, 1),
              FortressUtils.makeNodeInteger(0x2))));
  public final MmuTransition ifL1Miss = new MmuTransition(startL1, checkL2,
      new MmuGuard(defaultAccess(l1, BufferAccessEvent.MISS)));
  public final MmuTransition ifL1Hit = new MmuTransition(startL1, hitL1,
      new MmuGuard(defaultAccess(l1, BufferAccessEvent.HIT)));
  public final MmuTransition afterL1 = new MmuTransition(hitL1, stop);
  public final MmuTransition ifL2Bypass = new MmuTransition(checkL2, startMem,
      new MmuGuard(
          FortressUtils.makeNodeAnd(
              FortressUtils.makeNodeNotEqual(
                  FortressUtils.makeNodeExtract(c, 0, 1),
                  FortressUtils.makeNodeInteger(0x2)),
              FortressUtils.makeNodeNotEqual(
                  FortressUtils.makeNodeExtract(c, 0, 1),
                  FortressUtils.makeNodeInteger(0x3)))));
  public final MmuTransition ifL2Used = new MmuTransition(checkL2, startL2,
      new MmuGuard(
          FortressUtils.makeNodeAnd(
              FortressUtils.makeNodeNotEqual(
                  FortressUtils.makeNodeExtract(c, 0, 1),
                  FortressUtils.makeNodeInteger(0x0)),
              FortressUtils.makeNodeNotEqual(
                  FortressUtils.makeNodeExtract(c, 0, 1),
                  FortressUtils.makeNodeInteger(0x1)))));
  public final MmuTransition ifL2Miss = new MmuTransition(startL2, startMem,
      new MmuGuard(defaultAccess(l2, BufferAccessEvent.MISS)));
  public final MmuTransition ifL2Hit = new MmuTransition(startL2, hitL2,
      new MmuGuard(defaultAccess(l2, BufferAccessEvent.HIT)));
  public final MmuTransition afterL2 = new MmuTransition(hitL2, stop);
  public final MmuTransition afterMem = new MmuTransition(startMem, stop);

  // ===============================================================================================
  // MMU
  // ===============================================================================================

  public final MmuSubsystem mmu;

  {
    final MmuSubsystem.Builder builder = new MmuSubsystem.Builder();

    builder.registerVariable(va);
    builder.registerVariable(pa);
    builder.registerVariable(kseg0Cp);
    builder.registerVariable(vpn2);
    builder.registerVariable(v0);
    builder.registerVariable(d0);
    builder.registerVariable(g0);
    builder.registerVariable(c0);
    builder.registerVariable(pfn0);
    builder.registerVariable(v1);
    builder.registerVariable(d1);
    builder.registerVariable(g1);
    builder.registerVariable(c1);
    builder.registerVariable(pfn1);
    builder.registerVariable(v);
    builder.registerVariable(d);
    builder.registerVariable(g);
    builder.registerVariable(c);
    builder.registerVariable(pfn);
    builder.registerVariable(l1Tag);
    builder.registerVariable(l2Tag);
    builder.registerVariable(l1Data);
    builder.registerVariable(l2Data);
    builder.registerVariable(data);

    builder.registerAddress(vaAddr);
    builder.registerAddress(paAddr);
    builder.setVirtualAddress(vaAddr);
    builder.setPhysicalAddress(paAddr);

    builder.registerSegment(xuseg);
    builder.registerSegment(kseg0);
    builder.registerSegment(kseg1);
    builder.registerSegment(xkphys);

    builder.registerBuffer(dtlb);
    builder.registerBuffer(jtlb);
    builder.registerBuffer(l1);
    builder.registerBuffer(l2);
    builder.registerBuffer(mem);
    builder.setTargetBuffer(mem);

    builder.registerAction(root);
    builder.registerAction(start);
    builder.registerAction(getUpaKseg);
    builder.registerAction(getUpaXkphys);
    builder.registerAction(startDtlb);
    builder.registerAction(hitDtlb);
    builder.registerAction(startJtlb);
    builder.registerAction(hitJtlb);
    builder.registerAction(selectVpn);
    builder.registerAction(getLo0);
    builder.registerAction(getLo1);
    builder.registerAction(checkG);
    builder.registerAction(checkV);
    builder.registerAction(checkD);
    builder.registerAction(local);
    builder.registerAction(global);
    builder.registerAction(getMpa);
    builder.registerAction(checkSegment);
    builder.registerAction(startKseg0);
    builder.registerAction(startXkphys);
    builder.registerAction(startCache);
    builder.registerAction(startL1);
    builder.registerAction(hitL1);
    builder.registerAction(checkL2);
    builder.registerAction(startL2);
    builder.registerAction(hitL2);
    builder.registerAction(startMem);
    builder.registerAction(tlbRefill);
    builder.registerAction(tlbInvalid);
    builder.registerAction(tlbModified);
    builder.registerAction(stop);
    builder.setStartAction(root);

    builder.registerTransition(ifRead);

    if (!REDUCE) {
      builder.registerTransition(ifWrite);
      builder.registerTransition(ifUnmappedKseg0);
      builder.registerTransition(ifUnmappedKseg1);
      builder.registerTransition(ifUnmappedXkphys);
    }

    builder.registerTransition(ifMapped);
    builder.registerTransition(afterUpaKseg);
    builder.registerTransition(afterUpaXkphys);
    builder.registerTransition(ifDtlbMiss);
    builder.registerTransition(ifDtlbHit);
    builder.registerTransition(afterDtlb);
    builder.registerTransition(ifJtlbMiss);
    builder.registerTransition(ifJtlbHit);
    builder.registerTransition(afterJtlb);
    builder.registerTransition(ifVpn0);
    if (!REDUCE) {
      builder.registerTransition(ifVpn1);
    }
    builder.registerTransition(afterLo0);
    builder.registerTransition(afterLo1);
    if (!REDUCE) {
      builder.registerTransition(ifLocal);
      builder.registerTransition(afterLocal);
    }
    builder.registerTransition(ifGlobal);
    builder.registerTransition(afterGlobal);
    if (!REDUCE) {
      builder.registerTransition(ifInvalid);
    }
    builder.registerTransition(ifValid);
    if (!REDUCE) {
      builder.registerTransition(ifDirty);
    }
    builder.registerTransition(ifClean);
    builder.registerTransition(afterMpa);
    builder.registerTransition(ifKseg0);
    builder.registerTransition(ifKseg1);
    builder.registerTransition(ifXkphys);
    builder.registerTransition(ifXuseg);
    builder.registerTransition(afterKseg0);
    builder.registerTransition(afterXkphys);

    builder.registerTransition(ifUncached);
    builder.registerTransition(ifCached);
    builder.registerTransition(ifL1Miss);
    builder.registerTransition(ifL1Hit);
    builder.registerTransition(afterL1);
    builder.registerTransition(ifL2Bypass);
    if (!REDUCE) {
      builder.registerTransition(ifL2Used);
    }
    builder.registerTransition(ifL2Miss);
    builder.registerTransition(ifL2Hit);
    builder.registerTransition(afterL2);
    builder.registerTransition(afterMem);

    mmu = builder.build();
  }

  private MmuUnderTest() {}

  private static MmuBufferAccess defaultAccess(
      final MmuBuffer buffer,
      final BufferAccessEvent event) {
    return new MmuBufferAccess(
        buffer,
        event,
        buffer.getAddress(),
        buffer,
        buffer.getAddress());
  }
}
