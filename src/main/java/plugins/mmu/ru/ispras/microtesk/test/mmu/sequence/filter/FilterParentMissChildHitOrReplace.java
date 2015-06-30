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

package ru.ispras.microtesk.test.mmu.sequence.filter;

import ru.ispras.microtesk.translator.mmu.coverage.ExecutionPath;
import ru.ispras.microtesk.translator.mmu.coverage.Hazard;
import ru.ispras.microtesk.translator.mmu.coverage.UnitedHazard;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.ir.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * Filters off test templates, where there is a hit or a replace in a child device (e.g. DTLB) and
 * a miss in the parent device (e.g. JTLB).
 * 
 * <p>NOTE: Such test templates are unsatisfiable.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterParentMissChildHitOrReplace implements BiPredicate<ExecutionPath, UnitedHazard> {
  @Override
  public boolean test(final ExecutionPath execution, final UnitedHazard hazard) {
    final MmuDevice view = hazard.getDevice();

    if (view != null && view.isView()) {
      final MmuDevice parent = view.getParent();

      final boolean viewAccess = execution.getEvent(view) == BufferAccessEvent.HIT ||
          !hazard.getRelation(Hazard.Type.TAG_REPLACED).isEmpty();

      if (execution.getEvent(parent) == BufferAccessEvent.MISS && viewAccess) {
        // Filter off.
        return false;
      }
    }

    return true;
  }
}

