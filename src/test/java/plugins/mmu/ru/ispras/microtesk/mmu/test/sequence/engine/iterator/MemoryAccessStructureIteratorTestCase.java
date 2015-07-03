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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.classifier.ClassifierTrivial;
import ru.ispras.microtesk.mmu.translator.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryDependency;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.DataType;

/**
 * Test for {@link MemoryAccessStructureMmuIterator}.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructureIteratorTestCase {
  /** Contains of devices (buffers) of the memory management unit. */
  private static Collection<MmuDevice> devices = new LinkedHashSet<>();

  /** Contains of addresses (buffers) of the memory management unit. */
  private static Collection<MmuAddress> addresses = new LinkedHashSet<>();

  private static final boolean PRINT_LOGS = false;
  private final static int N = 2;

  private static int countPaVaL1Equal = 0;
  private static int countPaVaEqual = 0;

  @Test
  public void runTest() {
    final MmuSubsystem mipsMmu = MipsMmu.get().mmu;

    devices = mipsMmu.getDevices();
    addresses = mipsMmu.getAddresses();

    final MemoryAccessStructureIterator mmuIterator =
        new MemoryAccessStructureIterator(
            mipsMmu,
            new DataType[] {DataType.BYTE},
            true,
            N,
            new ClassifierTrivial());

    final Map<MemoryHazard.Type, Integer> conflictsType = new HashMap<>();
    for (final MemoryHazard.Type type : MemoryHazard.Type.values()) {
      conflictsType.put(type, 0);
    }

    final Map<MmuDevice, Map<MemoryHazard.Type, Integer>> devicesConflicts = new HashMap<>();
    for (final MmuDevice device : devices) {
      final Map<MemoryHazard.Type, Integer> conflicts = new HashMap<>();
      for (final MemoryHazard conflict : CoverageExtractor.get().getCoverage(device)) {
        conflicts.put(conflict.getType(), 0);
      }

      devicesConflicts.put(device, conflicts);
    }

    final Map<MmuAddress, Map<MemoryHazard.Type, Integer>> addressesConflicts = new HashMap<>();
    for (final MmuAddress address : addresses) {
      final Map<MemoryHazard.Type, Integer> conflicts = new HashMap<>();

      for (final MemoryHazard conflict : CoverageExtractor.get().getCoverage(address)) {
        conflicts.put(conflict.getType(), 0);
      }

      addressesConflicts.put(address, conflicts);
    }

    int k = 0;
    for (mmuIterator.init(); mmuIterator.hasValue(); mmuIterator.next()) {
      k++;
      if (PRINT_LOGS)
        System.out.println("");
      if (PRINT_LOGS)
        System.out.println("Template: " + k);

      checkSituationsDependency((MemoryAccessStructure) mmuIterator.value(), conflictsType,
          devicesConflicts, addressesConflicts);

      if (PRINT_LOGS)
        System.out.println("");
      boolean testEnd = true;
      for (final Map.Entry<MemoryHazard.Type, Integer> conflicts : conflictsType.entrySet()) {
        if (conflicts.getValue().equals(0)) {
          testEnd = false;
          break;
        }
      }
      if (testEnd) {
        // break;
      }
    }

    System.out.println("All: " + k);
    System.out.println(conflictsType);

    System.out.println(devicesConflicts);
    System.out.println(addressesConflicts);

    if (countPaVaL1Equal == 0) {
      Assert.fail("Not found: PAEqual, VAEqual, L1TagEqual");
    } else {
      if (PRINT_LOGS)
        System.out.println("Found conflict: PAEqual, VAEqual, L1TagEqual: " + countPaVaL1Equal);
    }

    if (countPaVaEqual == 0) {
      Assert.fail("Not found: PAEqual, VAEqual");
    } else {
      if (PRINT_LOGS)
        System.out.println("Found: PAEqual, VAEqual: " + countPaVaEqual);
    }

    for (final Map.Entry<MemoryHazard.Type, Integer> conflicts : conflictsType.entrySet()) {
      if (conflicts.getValue() == 0) {
        Assert.fail("Not found: " + conflicts.getKey());
      }
    }

    for (final Map.Entry<MmuDevice, Map<MemoryHazard.Type, Integer>> deviceConflicts :
      devicesConflicts.entrySet()) {
      for (final Map.Entry<MemoryHazard.Type, Integer> conflicts :
        deviceConflicts.getValue().entrySet()) {
        if (conflicts.getValue() == 0) {
          if (!(deviceConflicts.getKey().equals(MipsMmu.get().mem) && conflicts.getKey().equals(
              MemoryHazard.Type.TAG_NOT_EQUAL))) {
            Assert.fail(
                "Not found: " + conflicts.getKey() + " of device " + deviceConflicts.getKey());
          }
        }
      }
    }

    for (final Map.Entry<MmuAddress, Map<MemoryHazard.Type, Integer>> addressConflicts :
      addressesConflicts.entrySet()) {
      for (final Map.Entry<MemoryHazard.Type, Integer> conflicts :
        addressConflicts.getValue().entrySet()) {
        if (conflicts.getValue() == 0) {
          Assert.fail(
              "Not found: " + conflicts.getKey() + " of address " + addressConflicts.getKey());
        }
      }
    }

  }

  private static void checkSituationsDependency(final MemoryAccessStructure template,
      final Map<MemoryHazard.Type, Integer> conflictsType,
      final Map<MmuDevice, Map<MemoryHazard.Type, Integer>> devicesConflicts,
      final Map<MmuAddress, Map<MemoryHazard.Type, Integer>> addressesConflicts) {
    InvariantChecks.checkNotNull(template);
    InvariantChecks.checkNotNull(conflictsType);
    InvariantChecks.checkNotNull(devicesConflicts);

    for (int i = 0; i < N; i++) {
      for (int j = i + 1; j < N; j++) {
        final MemoryDependency dependency = template.getDependency(i, j);

        if (dependency != null) {
          boolean addressConflict = false;
          boolean paEqual = false;
          boolean vaEqual = false;
          boolean l1TagEqual = false;
          boolean memTagNotEqual = false;

          for (final MemoryHazard conflict : dependency.getHazards()) {
            final MmuDevice device = conflict.getDevice();
            if (device != null) {
              final Map<MemoryHazard.Type, Integer> conflicts = devicesConflicts.get(device);
              final int numberOfConflicts = conflicts.get(conflict.getType());
              conflicts.put(conflict.getType(), numberOfConflicts + 1);
            }

            final MmuAddress address = conflict.getAddress();
            if (address != null) {
              final Map<MemoryHazard.Type, Integer> conflicts = addressesConflicts.get(address);
              final int numberOfConflicts = conflicts.get(conflict.getType());
              conflicts.put(conflict.getType(), numberOfConflicts + 1);
              addressConflict = true;
              if (MipsMmu.get().pa.equals(conflict.getAddress().getAddress())) {
                addressConflict = true;
              }
              if (MipsMmu.get().va.equals(conflict.getAddress().getAddress())) {
                addressConflict = true;
              }
            }

            if (PRINT_LOGS) {
              System.out.println("conflict name: " + conflict.getFullName());
            }

            conflictsType.put(conflict.getType(), conflictsType.get(conflict.getType()) + 1);

            if (conflict.getAddress() != null
                && MipsMmu.get().pa.equals(conflict.getAddress().getAddress())
                && MemoryHazard.Type.ADDR_EQUAL.equals(conflict.getType())) {
              paEqual = true;
            }

            if (conflict.getAddress() != null
                && MipsMmu.get().va.equals(conflict.getAddress().getAddress())
                && MemoryHazard.Type.ADDR_EQUAL.equals(conflict.getType())) {
              vaEqual = true;
            }

            if (conflict.getDevice() != null && MipsMmu.get().l1.equals(conflict.getDevice())
                && MemoryHazard.Type.TAG_EQUAL.equals(conflict.getType())) {
              l1TagEqual = true;
            }

            if (conflict.getDevice() != null && MipsMmu.get().mem.equals(conflict.getDevice())
                && MemoryHazard.Type.TAG_NOT_EQUAL.equals(conflict.getType())) {
              memTagNotEqual = true;
            }

            if (paEqual && vaEqual) {
              countPaVaEqual++;
            }

            if (paEqual && memTagNotEqual) {
              Assert.fail("Found: PAEqual, MemTagNotEqual");
            }

            if (paEqual && vaEqual && l1TagEqual) {
              countPaVaL1Equal++;
            }
          }
          if (!addressConflict) {
            Assert.fail("Not found: Address Equal/NotEqual.");
          }
        }
      }
    }
  }
}
