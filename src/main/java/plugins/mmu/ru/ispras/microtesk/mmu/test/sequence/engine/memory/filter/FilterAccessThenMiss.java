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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferHazard.Instance;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * Filters off test templates, where a {@code MISS} situation is preceded by a number of accesses
 * that load the data into the buffer.
 * 
 * <p>NOTE: Such test templates are unsatisfiable.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterAccessThenMiss
    implements BiPredicate<MemoryAccess, BufferUnitedDependency> {

  public static boolean test(
      final MmuBufferAccess bufferAccess,
      final BufferUnitedDependency dependency) {
    final Set<Pair<Integer, BufferHazard.Instance>> indexEqualRelation =
        dependency.getIndexEqualRelation(bufferAccess);
    final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
        dependency.getTagEqualRelation(bufferAccess);

    if (!tagEqualRelation.isEmpty()) {
      final List<Pair<Integer, BufferHazard.Instance>> sortedIndices = new ArrayList<>(tagEqualRelation);

      Collections.sort(sortedIndices, new Comparator<Pair<Integer, BufferHazard.Instance>>() {
        @Override
        public int compare(final Pair<Integer, Instance> lhs, final Pair<Integer, Instance> rhs) {
          return lhs.first - rhs.first;
        }
      });

      final Pair<Integer, Instance> latestTagAccess = sortedIndices.get(sortedIndices.size() - 1);

      int setAccessCount = 0;
      for (final Pair<Integer, Instance> currentSetAccess : indexEqualRelation) {
        if (latestTagAccess.first < currentSetAccess.first) {
          setAccessCount++;
        }
      }

      // TODO: There might be exotic replacement policies where stepsToReplaceNewData is
      // TODO: less the buffer associativity (e.g., RANDOM, etc.).
      final MmuBuffer buffer = bufferAccess.getBuffer();
      final int stepsToReplaceNewData = (int) buffer.getWays();

      if (setAccessCount < stepsToReplaceNewData) {
        // Filter off.
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean test(final MemoryAccess access, BufferUnitedDependency dependency) {
    for (final MmuBufferAccess bufferAccess : dependency.getBufferHazards().keySet()) {
      if (bufferAccess.getBuffer().isReplaceable()) {
        if (bufferAccess.getEvent() == BufferAccessEvent.MISS) {
          if (!test(bufferAccess, dependency)) {
            return false;
          }
        }
      }
    }

    return true;
  }
}
