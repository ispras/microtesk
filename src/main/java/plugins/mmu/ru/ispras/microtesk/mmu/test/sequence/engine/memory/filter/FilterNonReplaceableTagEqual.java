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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * Filters off test templates, where {@code TAG_EQUAL} is specified for a non-replaceable device
 * {@code D} and {@code execution1.getEvent(D) != execution2.getEvent(D)}.
 * 
 * <p>NOTE: Such test templates are unsatisfiable.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterNonReplaceableTagEqual implements TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard> {
  @Override
  public boolean test(final MemoryAccess execution1, final MemoryAccess execution2,
      final MemoryHazard hazard) {

    if (hazard.getType() == MemoryHazard.Type.TAG_EQUAL) {
      final MmuDevice hazardDevice = hazard.getDevice();
      final List<MmuDevice> devices = new ArrayList<>();

      if (hazardDevice != null) {
        devices.add(hazardDevice);
        if (hazardDevice.isView()) {
          devices.add(hazardDevice.getParent());
        }
      }

      for (final MmuDevice device : devices) {
        if (!device.isReplaceable()) {
          if (execution1.getEvent(device) != execution2.getEvent(device)) {
            // Filter off.
            return false;
          }
        }
      }
    }

    return true;
  }
}
