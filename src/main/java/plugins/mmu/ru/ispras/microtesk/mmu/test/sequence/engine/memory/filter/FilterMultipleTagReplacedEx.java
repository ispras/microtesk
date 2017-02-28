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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferUnitedHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * Filters off test templates with multiple {@code TAG_REPLACED} hazards over devices with the
 * same address type for a single execution.
 * 
 * <p>NOTE: Such constraints do not have a well-defined semantics.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterMultipleTagReplacedEx
    implements BiPredicate<MemoryAccess, BufferUnitedDependency> {

  @Override
  public boolean test(final MemoryAccess access, final BufferUnitedDependency dependency) {
    final Set<MmuAddressInstance> addresses = new HashSet<>();
    final Map<MmuBufferAccess, BufferUnitedHazard> hazards = dependency.getBufferHazards();

    for (final Map.Entry<MmuBufferAccess, BufferUnitedHazard> entry : hazards.entrySet()) {
      final MmuBufferAccess bufferAccess = entry.getKey();
      final BufferUnitedHazard hazard = entry.getValue();

      if (!hazard.getRelation(BufferHazard.Type.TAG_REPLACED).isEmpty()) {
        if(!addresses.add(bufferAccess.getAddress())) {
          // Filter off.
          return false;
        }
      }
    }

    return true;
  }
}
