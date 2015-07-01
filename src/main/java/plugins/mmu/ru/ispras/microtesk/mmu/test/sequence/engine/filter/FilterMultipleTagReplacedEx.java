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

package ru.ispras.microtesk.mmu.test.sequence.engine.filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.mmu.translator.coverage.ExecutionPath;
import ru.ispras.microtesk.mmu.translator.coverage.Hazard;
import ru.ispras.microtesk.mmu.translator.coverage.UnitedDependency;
import ru.ispras.microtesk.mmu.translator.coverage.UnitedHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * Filters off test templates with multiple {@code TAG_REPLACED} hazards over devices with the
 * same address type for a single execution.
 * 
 * <p>NOTE: Such constraints do not have a well-defined semantics.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterMultipleTagReplacedEx implements BiPredicate<ExecutionPath, UnitedDependency> {
  @Override
  public boolean test(final ExecutionPath execution, final UnitedDependency dependency) {
    final Set<MmuAddress> addresses = new HashSet<>();
    final Map<MmuDevice, UnitedHazard> hazards = dependency.getDeviceHazards();

    for (final Map.Entry<MmuDevice, UnitedHazard> entry : hazards.entrySet()) {
      final MmuDevice device = entry.getKey();
      final UnitedHazard hazard = entry.getValue();

      if (!hazard.getRelation(Hazard.Type.TAG_REPLACED).isEmpty()) {
        if(!addresses.add(device.getAddress())) {
          // Filter off.
          return false;
        }
      }
    }

    return true;
  }
}
