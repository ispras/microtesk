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

package ru.ispras.microtesk.test.mmu.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ru.ispras.microtesk.translator.mmu.coverage.ExecutionPath;
import ru.ispras.microtesk.translator.mmu.coverage.UnitedDependency;
import ru.ispras.microtesk.translator.mmu.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * Filters off test templates, where a {@code MISS} situation is preceded by a number of accesses
 * that load the data into the buffer.
 * 
 * <p>NOTE: Such test templates are unsatisfiable.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterAccessThenMiss implements BiPredicate<ExecutionPath, UnitedDependency> {
  public static boolean test(final MmuDevice device, final UnitedDependency dependency) {
    final Set<Integer> indexEqualRelation = dependency.getIndexEqualRelation(device);
    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(device);

    if (!tagEqualRelation.isEmpty()) {
      final List<Integer> sortedIndices = new ArrayList<>(tagEqualRelation);
      Collections.sort(sortedIndices);

      final int latestTagAccess = sortedIndices.get(sortedIndices.size() - 1);

      int setAccessCount = 0;
      for (final int currentSetAccess : indexEqualRelation) {
        if (latestTagAccess < currentSetAccess) {
          setAccessCount++;
        }
      }

      // TODO: There might be exotic replacement policies where stepsToReplaceNewData is
      // TODO: less the buffer associativity (e.g., RANDOM, etc.).
      final int stepsToReplaceNewData = (int) device.getWays();

      if (setAccessCount < stepsToReplaceNewData) {
        // Filter off.
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean test(final ExecutionPath execution, UnitedDependency dependency) {
    for (final MmuDevice device : dependency.getDeviceHazards().keySet()) {
      if (device.isReplaceable() && execution.getEvent(device) == BufferAccessEvent.MISS) {
        if (!test(device, dependency)) {
          return false;
        }
      }
    }

    return true;
  }
}
