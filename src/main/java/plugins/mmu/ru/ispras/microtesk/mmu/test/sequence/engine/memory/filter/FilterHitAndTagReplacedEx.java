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

import java.util.Map;

import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * Filters off test templates, where {@code TAG_REPLACED} and {@code HIT} are simultaneously set for
 * devices with the same address type.
 * 
 * <p>NOTE: Filtering such test templates off does not mean that they are unsatisfiable; it means
 * that the solver cannot satisfy the {@code HIT} and {@code MISS} constraints if the
 * {@code TAG_REPLACED} hazard is specified, and there by, it is redundant to iterate buffer
 * access events.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterHitAndTagReplacedEx implements BiPredicate<MemoryAccess, MemoryUnitedDependency> {
  @Override
  public boolean test(final MemoryAccess access, MemoryUnitedDependency dependency) {
    final MemoryAccessPath path = access.getPath();
    final Map<MmuBuffer, MemoryUnitedHazard> hazards = dependency.getDeviceHazards();

    for (final Map.Entry<MmuBuffer, MemoryUnitedHazard> entry : hazards.entrySet()) {
      final MmuBuffer buffer = entry.getKey();
      final MemoryUnitedHazard hazard = entry.getValue();

      if (!hazard.getRelation(MemoryHazard.Type.TAG_REPLACED).isEmpty()) {
        for (final MmuBuffer otherDevice : path.getBuffers()) {
          if (otherDevice.isReplaceable() && otherDevice.getAddress() == buffer.getAddress() &&
              path.getEvent(otherDevice) == BufferAccessEvent.HIT)
          // Filter off.
          return false;
        }
      }
    }

    return true;
  }
}
