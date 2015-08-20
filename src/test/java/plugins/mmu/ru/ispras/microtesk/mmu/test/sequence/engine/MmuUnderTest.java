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

package ru.ispras.microtesk.mmu.test.sequence.engine;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
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
  public static final String DATA_LO_REGION = "lo";
  public static final String DATA_HI_REGION = "hi";

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


  private static MmuAddressType newAddress(final String name, int width) {
    final Type type = new Type(name, Collections.singletonMap("value", new Type(width)));

    return new MmuAddressType(new Variable(name, type),
                              new IntegerVariable(name + ".value", width));
  }

  // ===============================================================================================
  // Variables
  // ===============================================================================================

  public final MmuAddressType vaAddr = newAddress("VA", 64);
  public final MmuAddressType paAddr = newAddress("PA", 36);

  public final IntegerVariable va = vaAddr.getVariable();
  public final IntegerVariable pa = paAddr.getVariable();

  public final IntegerVariable kseg0Cp = new IntegerVariable("KSEG0_CP", 3);

  public final IntegerVariable vpn2 = new IntegerVariable("VPN2", 27);
  public final IntegerVariable v0 = new IntegerVariable("V0", 1);
  public final IntegerVariable d0 = new IntegerVariable("D0", 1);
  public final IntegerVariable g0 = new IntegerVariable("G0", 1);
  public final IntegerVariable c0 = new IntegerVariable("C0", 3);
  public final IntegerVariable pfn0 = new IntegerVariable("PFN0", 24);
  public final IntegerVariable v1 = new IntegerVariable("V1", 1);
  public final IntegerVariable d1 = new IntegerVariable("D1", 1);
  public final IntegerVariable g1 = new IntegerVariable("G1", 1);
  public final IntegerVariable c1 = new IntegerVariable("C1", 3);
  public final IntegerVariable pfn1 = new IntegerVariable("PFN1", 24);
  public final IntegerVariable v = new IntegerVariable("V", 1);
  public final IntegerVariable d = new IntegerVariable("D", 1);
  public final IntegerVariable g = new IntegerVariable("G", 1);
  public final IntegerVariable c = new IntegerVariable("C", 3);
  public final IntegerVariable pfn = new IntegerVariable("PFN", 24);
  public final IntegerVariable l1Tag = new IntegerVariable("TAG1", 24);
  public final IntegerVariable l2Tag = new IntegerVariable("TAG2", 19);
  public final IntegerVariable l1Data = new IntegerVariable("DATA1", 8 * 32);
  public final IntegerVariable l2Data = new IntegerVariable("DATA2", 8 * 32);
  public final IntegerVariable data = new IntegerVariable("DATA", 8 * 32);

  // ===============================================================================================
  // Segments
  // ===============================================================================================

  public final MmuSegment xuseg = new MmuSegment("XUSEG", vaAddr, paAddr,
      0x0000000080000000L, 0x000000ffFFFFffffL, true, null, null);
  public final MmuSegment kseg0 = new MmuSegment("KSEG0", vaAddr, paAddr,
      0xffffFFFF80000000L, 0xffffFFFF9fffFFFFL, false,
      MmuExpression.var(va, 0, 28), 
      MmuExpression.var(va, 29, 63));
  public final MmuSegment kseg1 = new MmuSegment("KSEG1", vaAddr, paAddr,
      0xffffFFFFa0000000L, 0xffffFFFFbfffFFFFL, false,
      MmuExpression.var(va, 0, 28), 
      MmuExpression.var(va, 29, 63));
  public final MmuSegment xkphys = new MmuSegment("XKPHYS", vaAddr, paAddr,
      0x8000000000000000L, 0xbfffFFFFffffFFFFL, false,
      MmuExpression.var(va, 0, 35), 
      MmuExpression.var(va, 36, 63));

  // ===============================================================================================
  // Buffers
  // ===============================================================================================

  public final MmuBuffer jtlb = new MmuBuffer("JTLB", JTLB_SIZE, 1, vaAddr,
      MmuExpression.var(va, 13, 39), // Tag
      MmuExpression.empty(),         // Index
      MmuExpression.var(va, 0, 12),  // Offset
      Collections.singleton(new MmuBinding(new IntegerField(vpn2), MmuExpression.var(va, 13, 39))),
      null, null,                    // Guard
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
  public final Predicate<MemoryAccess> dtlbGuard = new Predicate<MemoryAccess>() {
    @Override public boolean test(final MemoryAccess access) {
      final MemoryAccessPath path = access.getPath();

      final boolean dtlbHit = path.getEvent(MmuUnderTest.get().dtlb) == BufferAccessEvent.HIT; // TODO: remove
      final boolean jtlbHit = path.getEvent(MmuUnderTest.get().jtlb) == BufferAccessEvent.HIT; // TODO: remove
      final boolean v = path.contains(MmuUnderTest.get().ifValid);

      return (dtlbHit || jtlbHit) && v;
    }
  };

  public final MmuBuffer dtlb = new MmuBuffer("DTLB", MTLB_SIZE, 1, vaAddr,
      MmuExpression.var(va, 13, 39),                 // Tag
      MmuExpression.empty(),                         // Index
      MmuExpression.var(va, 0, 12),                  // Offset
      Collections.singleton(new MmuBinding(new IntegerField(vpn2), MmuExpression.var(va, 13, 39))),
      MmuCondition.eq(v, BigInteger.ONE), dtlbGuard, // Guard
      true, jtlb);

  {
    dtlb.addField(new IntegerVariable("VPN2", 27));

    dtlb.addField(new IntegerVariable("V0", 1));
    dtlb.addField(new IntegerVariable("D0", 1));
    dtlb.addField(new IntegerVariable("G0", 1));
    dtlb.addField(new IntegerVariable("C0", 1));
    dtlb.addField(new IntegerVariable("PFN0", 24));

    dtlb.addField(new IntegerVariable("V1", 1));
    dtlb.addField(new IntegerVariable("D1", 1));
    dtlb.addField(new IntegerVariable("G1", 1));
    dtlb.addField(new IntegerVariable("C1", 1));
    dtlb.addField(new IntegerVariable("PFN1", 24));
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuBuffer l1 = new MmuBuffer("L1", L1_WAYS, L1_SETS, paAddr,
      MmuExpression.var(pa, POS_BITS + L1_ROW_BITS, PA_BITS - 1),  // Tag
      MmuExpression.var(pa, POS_BITS, POS_BITS + L1_ROW_BITS - 1), // Index
      MmuExpression.var(pa, 0, POS_BITS - 1),                      // Offset
      Collections.singleton(
          new MmuBinding(new IntegerField(l1Tag),
              MmuExpression.var(pa, POS_BITS + L1_ROW_BITS, PA_BITS - 1))),
      null, null,                                                  // Guard
      true, null);

  {
    l1.addField(l1Tag);
    l1.addField(l1Data);
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuBuffer l2 = new MmuBuffer("L2", L1_WAYS, L1_SETS, paAddr,
      MmuExpression.var(pa, POS_BITS + L2_ROW_BITS, PA_BITS - 1),  // Tag
      MmuExpression.var(pa, POS_BITS, POS_BITS + L2_ROW_BITS - 1), // Index
      MmuExpression.var(pa, 0, POS_BITS - 1),                      // Offset
      Collections.singleton(
          new MmuBinding(new IntegerField(l2Tag),
              MmuExpression.var(pa, POS_BITS + L2_ROW_BITS, PA_BITS - 1))),
      null, null,                                                  // Guard
      true, null);

  {
    l2.addField(l2Tag);
    l2.addField(l2Data);
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuBuffer mem = new MmuBuffer("MEM", 1, (1L << PA_BITS) / 32, paAddr,
      MmuExpression.empty(),                        // Tag
      MmuExpression.var(pa, POS_BITS, PA_BITS - 1), // Index
      MmuExpression.var(pa, 0, POS_BITS - 1),       // Offset
      Collections.<MmuBinding>emptySet(),
      null, null,                                   // Guard
      false, null);

  {
    mem.addField(data);
  }

  // ===============================================================================================
  // Actions
  // ===============================================================================================

  public final MmuAction root = new MmuAction("ROOT",
      new MmuBinding(va));
  public final MmuAction start = new MmuAction("START");
  public final MmuAction getUpaKseg = new MmuAction("GET_UPA_KSEG",
      new MmuBinding(pa, MmuExpression.var(va, 0, 28)));
  public final MmuAction getUpaXkphys = new MmuAction("GET_UPA_XKPHYS",
      new MmuBinding(pa, MmuExpression.var(va, 0, 35)));
  public final MmuAction startDtlb = new MmuAction("START_DTLB", dtlb);
  public final MmuAction hitDtlb = new MmuAction("HIT_DTLB", dtlb,
      new MmuBinding(v0),
      new MmuBinding(d0),
      new MmuBinding(c0),
      new MmuBinding(pfn0),
      new MmuBinding(v1),
      new MmuBinding(d1),
      new MmuBinding(c1),
      new MmuBinding(pfn1));
  public final MmuAction startJtlb = new MmuAction("START_JTLB", jtlb);
  public final MmuAction hitJtlb = new MmuAction("HIT_JTLB", jtlb,
      new MmuBinding(v0),
      new MmuBinding(d0),
      new MmuBinding(c0),
      new MmuBinding(pfn0),
      new MmuBinding(v1),
      new MmuBinding(d1),
      new MmuBinding(c1),
      new MmuBinding(pfn1));
  public final MmuAction selectVpn = new MmuAction("SELECT_VPN");
  public final MmuAction getLo0 = new MmuAction("GET_LO0",
      new MmuBinding(v, MmuExpression.var(v0)),
      new MmuBinding(d, MmuExpression.var(d0)),
      new MmuBinding(c, MmuExpression.var(c0)),
      new MmuBinding(pfn, MmuExpression.var(pfn0)));
  public final MmuAction getLo1 = new MmuAction("GET_LO1",
      new MmuBinding(v, MmuExpression.var(v1)),
      new MmuBinding(d, MmuExpression.var(d1)),
      new MmuBinding(c, MmuExpression.var(c1)),
      new MmuBinding(pfn, MmuExpression.var(pfn1)));
  public final MmuAction checkV = new MmuAction("CHECK_V");
  public final MmuAction checkD = new MmuAction("CHECK_D");
  public final MmuAction checkG = new MmuAction("CHECK_G");
  public final MmuAction local = new MmuAction("LOCAL");
  public final MmuAction global = new MmuAction("GLOBAL");
  public final MmuAction getMpa = new MmuAction("GET_MPA",
      new MmuBinding(pa, MmuExpression.rcat(new IntegerField(pfn), new IntegerField(va, 0, 11))));
  public final MmuAction checkSegment = new MmuAction("CHECK_SEGMENT");
  public final MmuAction startKseg0 = new MmuAction("START_KSEG0",
      new MmuBinding(c, MmuExpression.var(kseg0Cp)));
  public final MmuAction startXkphys = new MmuAction("START_XKPHYS",
      new MmuBinding(c, MmuExpression.var(va, 59, 61)));
  public final MmuAction startCache = new MmuAction("START_CACHE");
  public final MmuAction startL1 = new MmuAction("START_L1", l1);
  public final MmuAction hitL1 = new MmuAction("HIT_L1", l1,
      new MmuBinding(data));
  public final MmuAction checkL2 = new MmuAction("CHECK_L2");
  public final MmuAction startL2 = new MmuAction("START_L2", l2);
  public final MmuAction hitL2 = new MmuAction("HIT_L2", l2,
      new MmuBinding(data));
  public final MmuAction startMem = new MmuAction("START_MEM", mem,
      new MmuBinding(data));
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
  public final MmuTransition ifUnmappedKseg = new MmuTransition(start, getUpaKseg,
      new MmuGuard(null, Arrays.asList(new MmuSegment[] {kseg0, kseg1})));
  public final MmuTransition ifUnmappedXkphys = new MmuTransition(start, getUpaXkphys,
      new MmuGuard(null, Collections.singleton(xkphys)));
  public final MmuTransition ifMapped = new MmuTransition(start, startDtlb,
      new MmuGuard(null, Collections.singleton(xuseg)));
  public final MmuTransition afterUpaKseg = new MmuTransition(getUpaKseg, checkSegment);
  public final MmuTransition afterUpaXkphys = new MmuTransition(getUpaXkphys, checkSegment);
  public final MmuTransition ifDtlbMiss = new MmuTransition(startDtlb, startJtlb,
      new MmuGuard(dtlb, BufferAccessEvent.MISS));
  public final MmuTransition ifDtlbHit = new MmuTransition(startDtlb, hitDtlb,
      new MmuGuard(dtlb, BufferAccessEvent.HIT));
  public final MmuTransition afterDtlb = new MmuTransition(hitDtlb, selectVpn);
  public final MmuTransition ifJtlbMiss = new MmuTransition(startJtlb, tlbRefill,
      new MmuGuard(jtlb, BufferAccessEvent.MISS));
  public final MmuTransition ifJtlbHit = new MmuTransition(startJtlb, hitJtlb,
      new MmuGuard(jtlb, BufferAccessEvent.HIT));
  public final MmuTransition afterJtlb = new MmuTransition(hitJtlb, selectVpn);
  public final MmuTransition ifVpn0 = new MmuTransition(selectVpn, getLo0,
      new MmuGuard(MmuCondition.eq(new IntegerField(va, 12), BigInteger.ZERO)));
  public final MmuTransition ifVpn1 = new MmuTransition(selectVpn, getLo1,
      new MmuGuard(MmuCondition.eq(new IntegerField(va, 12), BigInteger.ONE)));
  public final MmuTransition afterLo0 = new MmuTransition(getLo0, checkG);
  public final MmuTransition afterLo1 = new MmuTransition(getLo1, checkG);
  public final MmuTransition ifLocal = new MmuTransition(checkG, local,
      new MmuGuard(MmuCondition.eq(g, BigInteger.ZERO)));
  public final MmuTransition ifGlobal = new MmuTransition(checkG, global,
      new MmuGuard(MmuCondition.eq(g, BigInteger.ONE)));
  public final MmuTransition afterLocal = new MmuTransition(local, checkV);
  public final MmuTransition afterGlobal = new MmuTransition(global, checkV);
  public final MmuTransition ifInvalid = new MmuTransition(checkV, tlbInvalid,
      new MmuGuard(
          MmuCondition.eq(v, BigInteger.ZERO)));
  public final MmuTransition ifValid = new MmuTransition(checkV, checkD,
      new MmuGuard(
          MmuCondition.eq(v, BigInteger.ONE)));
  public final MmuTransition ifDirty = new MmuTransition(checkD, tlbModified,
      new MmuGuard(MemoryOperation.STORE, MmuCondition.eq(d, BigInteger.ZERO)));
  public final MmuTransition ifClean = new MmuTransition(checkD, getMpa,
      new MmuGuard(
          MmuCondition.eq(d, BigInteger.ONE)));
  public final MmuTransition ifHiMemory = new MmuTransition(getMpa, checkSegment,
      new MmuGuard(Collections.singleton(DATA_HI_REGION), null));
  public final MmuTransition ifLoMemory = new MmuTransition(getMpa, checkSegment,
      new MmuGuard(Collections.singleton(DATA_LO_REGION), null));
  public final MmuTransition ifKseg0 = new MmuTransition(checkSegment, startKseg0,
      new MmuGuard(null, Collections.singleton(kseg0)));
  public final MmuTransition ifKseg1 = new MmuTransition(checkSegment, startMem,
      new MmuGuard(null, Collections.singleton(kseg1)));
  public final MmuTransition ifXkphys = new MmuTransition(checkSegment, startXkphys,
      new MmuGuard(null, Collections.singleton(xkphys)));
  public final MmuTransition ifXuseg = new MmuTransition(checkSegment, startCache,
      new MmuGuard(null, Collections.singleton(xuseg)));
  public final MmuTransition afterKseg0 = new MmuTransition(startKseg0, startCache);
  public final MmuTransition afterXkphys = new MmuTransition(startXkphys, startCache);
  public final MmuTransition ifUncached = new MmuTransition(startCache, startMem,
      new MmuGuard(
          MmuCondition.eq(new IntegerField(c, 0, 1), BigInteger.valueOf(0x2))));
  public final MmuTransition ifCached = new MmuTransition(startCache, startL1,
      new MmuGuard(
          MmuCondition.neq(new IntegerField(c, 0, 1), BigInteger.valueOf(0x2))));
  public final MmuTransition ifL1Miss = new MmuTransition(startL1, checkL2,
      new MmuGuard(l1, BufferAccessEvent.MISS));
  public final MmuTransition ifL1Hit = new MmuTransition(startL1, hitL1,
      new MmuGuard(l1, BufferAccessEvent.HIT));
  public final MmuTransition afterL1 = new MmuTransition(hitL1, stop);
  public final MmuTransition ifL2Bypass = new MmuTransition(checkL2, startMem,
      new MmuGuard(
          MmuCondition.and(
              MmuConditionAtom.neq(new IntegerField(c, 0, 1), BigInteger.valueOf(0x2)),
              MmuConditionAtom.neq(new IntegerField(c, 0, 1), BigInteger.valueOf(0x3)))));
  public final MmuTransition ifL2Used = new MmuTransition(checkL2, startL2,
      new MmuGuard(
          MmuCondition.and(
              MmuConditionAtom.neq(new IntegerField(c, 0, 1), BigInteger.valueOf(0x0)),
              MmuConditionAtom.neq(new IntegerField(c, 0, 1), BigInteger.valueOf(0x1)))));
  public final MmuTransition ifL2Miss = new MmuTransition(startL2, startMem,
      new MmuGuard(l2, BufferAccessEvent.MISS));
  public final MmuTransition ifL2Hit = new MmuTransition(startL2, hitL2,
      new MmuGuard(l2, BufferAccessEvent.HIT));
  public final MmuTransition afterL2 = new MmuTransition(hitL2, stop);
  public final MmuTransition afterMem = new MmuTransition(startMem, stop);

  // ===============================================================================================
  // MMU
  // ===============================================================================================

  public final MmuSubsystem mmu;

  {
    final ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.Builder builder = new MmuSubsystem.Builder();

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
    builder.registerTransition(ifWrite);
    builder.registerTransition(ifUnmappedKseg);
    builder.registerTransition(ifUnmappedXkphys);
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
    builder.registerTransition(ifVpn1);
    builder.registerTransition(afterLo0);
    builder.registerTransition(afterLo1);

    builder.registerTransition(ifLocal);
    builder.registerTransition(afterLocal);

    builder.registerTransition(ifGlobal);
    builder.registerTransition(afterGlobal);

    builder.registerTransition(ifInvalid);
    builder.registerTransition(ifValid);

    builder.registerTransition(ifDirty);
    builder.registerTransition(ifClean);

    builder.registerTransition(ifLoMemory);
    builder.registerTransition(ifHiMemory);

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
    builder.registerTransition(ifL2Used);
    builder.registerTransition(ifL2Miss);
    builder.registerTransition(ifL2Hit);
    builder.registerTransition(afterL2);
    builder.registerTransition(afterMem);

    // Disable some of the transitions to reduce testing time.
    if (REDUCE) {
      ifWrite.setEnabled(false);
      ifUnmappedKseg.setEnabled(false);
      ifUnmappedXkphys.setEnabled(false);
      ifVpn1.setEnabled(false);
      ifInvalid.setEnabled(false);
      ifDirty.setEnabled(false);
      ifLocal.setEnabled(false);
      ifHiMemory.setEnabled(false);
      ifL2Used.setEnabled(false);
    }

    mmu = builder.build();
  }

  private MmuUnderTest() {}
}
