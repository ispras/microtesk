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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter;

import java.util.Map;

import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferUnitedHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
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
public final class FilterHitAndTagReplacedEx
    implements BiPredicate<MemoryAccess, BufferUnitedDependency> {

  @Override
  public boolean test(final MemoryAccess access, BufferUnitedDependency dependency) {
    final MemoryAccessPath path = access.getPath();
    final Map<MmuBufferAccess, BufferUnitedHazard> hazards = dependency.getBufferHazards();

    for (final Map.Entry<MmuBufferAccess, BufferUnitedHazard> entry : hazards.entrySet()) {
      final MmuBufferAccess bufferAccess = entry.getKey();
      final BufferUnitedHazard hazard = entry.getValue();

      if (!hazard.getRelation(BufferHazard.Type.TAG_REPLACED).isEmpty()) {
        for (final MmuBufferAccess otherAccess : path.getBufferAccesses()) {
          if (otherAccess.getBuffer().isReplaceable()
              && otherAccess.getAddress().equals(bufferAccess.getAddress()) &&
              path.getEvent(otherAccess) == BufferAccessEvent.HIT)
          // Filter off.
          return false;
        }
      }
    }

    return true;
  }
}
