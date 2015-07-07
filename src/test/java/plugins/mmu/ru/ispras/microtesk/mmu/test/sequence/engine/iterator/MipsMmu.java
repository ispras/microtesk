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

package ru.ispras.microtesk.mmu.test.sequence.engine.iterator;

import java.math.BigInteger;

import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAssignment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.MemoryOperation;

/**
 * {@link MipsMmu} represents a simplified MMU of a MIPS-compatible microprocessor.
 * 
 * <p>The logic is as follows.</p>
 * 
 * <pre>{@code
 * if (isMapped(VA) == false) {
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
public final class MipsMmu {
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

  public static final String TEXT_REGION = "code";
  public static final String DATA_LO_REGION = "lo";
  public static final String DATA_HI_REGION = "hi";

  // The instance should be created in get().
  private static MipsMmu instance = null;

  public static MipsMmu get() {
    if (instance == null) {
      instance = new MipsMmu();
    }
    return instance;
  }

  // ===============================================================================================
  // Variables
  // ===============================================================================================

  public final IntegerVariable va = new IntegerVariable("VA", VA_BITS);
  public final IntegerVariable pa = new IntegerVariable("PA", PA_BITS);
  public final IntegerVariable isMapped = new IntegerVariable("isMapped", 1);
  public final IntegerVariable isHiMem = new IntegerVariable("isHiMem", 1);
  public final IntegerVariable cachePolicy = new IntegerVariable("cachePolicy", 3); 
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
  public final IntegerVariable data = new IntegerVariable("DATA", 8 * 32);

  public final MmuAddress vaAddr = new MmuAddress(va);
  public final MmuAddress paAddr = new MmuAddress(pa);

  // ===============================================================================================
  // Devices
  // ===============================================================================================

  public final MmuDevice jtlb = new MmuDevice("JTLB", JTLB_SIZE, 1, vaAddr,
      MmuExpression.var(va, 13, 39), // Tag
      MmuExpression.empty(),         // Index
      MmuExpression.var(va, 0, 12),  // Offset
      false, null);

  {
    jtlb.addField(new IntegerVariable("VPN2", 27));

    jtlb.addField(new IntegerVariable("V0", 1));
    jtlb.addField(new IntegerVariable("D0", 1));
    jtlb.addField(new IntegerVariable("G0", 1));
    jtlb.addField(new IntegerVariable("C0", 1));
    jtlb.addField(new IntegerVariable("PFN0", 24));

    jtlb.addField(new IntegerVariable("V1", 1));
    jtlb.addField(new IntegerVariable("D1", 1));
    jtlb.addField(new IntegerVariable("G1", 1));
    jtlb.addField(new IntegerVariable("C1", 1));
    jtlb.addField(new IntegerVariable("PFN1", 24));
  }

  // -----------------------------------------------------------------------------------------------

  public final MmuDevice dtlb = new MmuDevice("DTLB", MTLB_SIZE, 1, vaAddr,
      MmuExpression.var(va, 13, 39), // Tag
      MmuExpression.empty(),         // Index
      MmuExpression.var(va, 0, 12),  // Offset
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
  public final MmuDevice l1 = new MmuDevice("L1", L1_WAYS, L1_SETS, paAddr,
      MmuExpression.var(pa, POS_BITS + L1_ROW_BITS, PA_BITS - 1),  // Tag
      MmuExpression.var(pa, POS_BITS, POS_BITS + L1_ROW_BITS - 1), // Index
      MmuExpression.var(pa, 0, POS_BITS - 1),                      // Offset
      true, null);

  {
    l1.addField(new IntegerVariable("TAG", 24));
    l1.addField(new IntegerVariable("DATA", 8 * 32));
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuDevice l2 = new MmuDevice("L2", L1_WAYS, L1_SETS, paAddr,
      MmuExpression.var(pa, POS_BITS + L2_ROW_BITS, PA_BITS - 1),  // Tag
      MmuExpression.var(pa, POS_BITS, POS_BITS + L2_ROW_BITS - 1), // Index
      MmuExpression.var(pa, 0, POS_BITS - 1),                      // Offset
      true, null);

  {
    l1.addField(new IntegerVariable("TAG", 19));
    l1.addField(new IntegerVariable("DATA", 8 * 32));
  }

  // -----------------------------------------------------------------------------------------------
  public final MmuDevice mem = new MmuDevice("MEM", 1, (1L << PA_BITS) / 32, paAddr,
      MmuExpression.empty(),                        // Tag
      MmuExpression.var(pa, POS_BITS, PA_BITS - 1), // Index
      MmuExpression.var(pa, 0, POS_BITS - 1),       // Offset
      false, null);

  {
    mem.addField(new IntegerVariable("DATA", 8 * 32));
  }

  // ===============================================================================================
  // Actions
  // ===============================================================================================

  public final MmuAction root = new MmuAction("ROOT",
      new MmuAssignment(va));
  public final MmuAction start = new MmuAction("START");
  public final MmuAction getUpa = new MmuAction("GET_UPA",
      new MmuAssignment(pa, MmuExpression.var(va, 0, 28)));
  public final MmuAction startDtlb = new MmuAction("START_DTLB", dtlb);
  public final MmuAction hitDtlb = new MmuAction("HIT_DTLB", dtlb,
      new MmuAssignment(v0),
      new MmuAssignment(d0),
      new MmuAssignment(c0),
      new MmuAssignment(pfn0),
      new MmuAssignment(v1),
      new MmuAssignment(d1),
      new MmuAssignment(c1),
      new MmuAssignment(pfn1));
  public final MmuAction startJtlb = new MmuAction("START_JTLB", jtlb);
  public final MmuAction hitJtlb = new MmuAction("HIT_JTLB", jtlb,
      new MmuAssignment(v0),
      new MmuAssignment(d0),
      new MmuAssignment(c0),
      new MmuAssignment(pfn0),
      new MmuAssignment(v1),
      new MmuAssignment(d1),
      new MmuAssignment(c1),
      new MmuAssignment(pfn1));
  public final MmuAction selectVpn = new MmuAction("SELECT_VPN");
  public final MmuAction getLo0 = new MmuAction("GET_LO0",
      new MmuAssignment(v, MmuExpression.var(v0)),
      new MmuAssignment(d, MmuExpression.var(d0)),
      new MmuAssignment(c, MmuExpression.var(c0)),
      new MmuAssignment(pfn, MmuExpression.var(pfn0)));
  public final MmuAction getLo1 = new MmuAction("GET_LO1",
      new MmuAssignment(v, MmuExpression.var(v1)),
      new MmuAssignment(d, MmuExpression.var(d1)),
      new MmuAssignment(c, MmuExpression.var(c1)),
      new MmuAssignment(pfn, MmuExpression.var(pfn1)));
  public final MmuAction checkV = new MmuAction("CHECK_V");
  public final MmuAction checkD = new MmuAction("CHECK_D");
  public final MmuAction checkG = new MmuAction("CHECK_G");
  public final MmuAction local = new MmuAction("LOCAL");
  public final MmuAction global = new MmuAction("GLOBAL");
  public final MmuAction getMpa = new MmuAction("GET_MPA",
      new MmuAssignment(pa, MmuExpression.rcat(new IntegerField(pfn), new IntegerField(va, 0, 11))));
  public final MmuAction startCache = new MmuAction("START_CACHE");
  public final MmuAction startL1 = new MmuAction("START_L1", l1);
  public final MmuAction hitL1 = new MmuAction("HIT_L1", l1,
      new MmuAssignment(data));
  public final MmuAction checkL2 = new MmuAction("CHECK_L2");
  public final MmuAction startL2 = new MmuAction("START_L2", l2);
  public final MmuAction hitL2 = new MmuAction("HIT_L2", l2,
      new MmuAssignment(data));
  public final MmuAction startMem = new MmuAction("START_MEM", mem,
          new MmuAssignment(data));
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
  public final MmuTransition ifUnmapped = new MmuTransition(start, getUpa,
      new MmuGuard(MmuCondition.eq(isMapped, BigInteger.ZERO)));
  public final MmuTransition ifMapped = new MmuTransition(start, startDtlb,
      new MmuGuard(MmuCondition.eq(isMapped, BigInteger.ONE)));
  public final MmuTransition afterUpa = new MmuTransition(getUpa, startCache);
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
  public final MmuTransition ifHiMemory = new MmuTransition(getMpa, startCache,
      new MmuGuard(MmuCondition.eq(isHiMem, BigInteger.ONE)));
  public final MmuTransition ifLoMemory = new MmuTransition(getMpa, startCache,
      new MmuGuard(MmuCondition.eq(isHiMem, BigInteger.ZERO)));
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

  public final MmuSubsystem mmu = new MmuSubsystem();

  {
    mmu.registerAddress(vaAddr);
    mmu.registerAddress(paAddr);

    mmu.registerDevice(dtlb);
    mmu.registerDevice(jtlb);
    mmu.registerDevice(l1);
    mmu.registerDevice(l2);
    mmu.registerDevice(mem);

    mmu.registerAction(root);
    mmu.registerAction(start);
    mmu.registerAction(getUpa);
    mmu.registerAction(startDtlb);
    mmu.registerAction(hitDtlb);
    mmu.registerAction(startJtlb);
    mmu.registerAction(hitJtlb);
    mmu.registerAction(selectVpn);
    mmu.registerAction(getLo0);
    mmu.registerAction(getLo1);
    mmu.registerAction(checkG);
    mmu.registerAction(checkV);
    mmu.registerAction(checkD);
    mmu.registerAction(local);
    mmu.registerAction(global);
    mmu.registerAction(getMpa);
    mmu.registerAction(startCache);
    mmu.registerAction(startL1);
    mmu.registerAction(hitL1);
    mmu.registerAction(checkL2);
    mmu.registerAction(startL2);
    mmu.registerAction(hitL2);
    mmu.registerAction(startMem);
    mmu.registerAction(tlbRefill);
    mmu.registerAction(tlbInvalid);
    mmu.registerAction(tlbModified);
    mmu.registerAction(stop);

    mmu.registerTransition(ifRead);
    mmu.registerTransition(ifWrite);
    mmu.registerTransition(ifUnmapped);
    mmu.registerTransition(ifMapped);
    mmu.registerTransition(afterUpa);
    mmu.registerTransition(ifDtlbMiss);
    mmu.registerTransition(ifDtlbHit);
    mmu.registerTransition(afterDtlb);

    mmu.registerTransition(ifJtlbMiss);
    mmu.registerTransition(ifJtlbHit);

    mmu.registerTransition(afterJtlb);
    mmu.registerTransition(ifVpn0);
    mmu.registerTransition(ifVpn1);
    mmu.registerTransition(afterLo0);
    mmu.registerTransition(afterLo1);

    mmu.registerTransition(ifLocal);
    mmu.registerTransition(afterLocal);

    mmu.registerTransition(ifGlobal);
    mmu.registerTransition(afterGlobal);

    mmu.registerTransition(ifInvalid);
    mmu.registerTransition(ifValid);

    mmu.registerTransition(ifDirty);
    mmu.registerTransition(ifClean);

    mmu.registerTransition(ifLoMemory);
    mmu.registerTransition(ifHiMemory);

    mmu.registerTransition(ifUncached);
    mmu.registerTransition(ifCached);
    mmu.registerTransition(ifL1Miss);
    mmu.registerTransition(ifL1Hit);
    mmu.registerTransition(afterL1);
    mmu.registerTransition(ifL2Bypass);
    mmu.registerTransition(ifL2Used);
    mmu.registerTransition(ifL2Miss);
    mmu.registerTransition(ifL2Hit);
    mmu.registerTransition(afterL2);
    mmu.registerTransition(afterMem);

    mmu.setStartAddress(vaAddr);
    mmu.setStartAction(root);
  }

  private MipsMmu() {}
}
