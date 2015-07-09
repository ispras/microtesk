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

import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * {@link FilterDependency} composes hazard-level filters into a dependency-level filter.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterDependency implements TriPredicate<MemoryAccess, MemoryAccess, MemoryDependency> {
  /** The hazard-level filters to be composed. */
  private final Collection<TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard>> filters;

  /**
   * Constructs a dependency-level filter from the collection of hazard-level filters.
   * 
   * @param filters the collection of hazard-level filters to be composed.
   * @throws IllegalArgumentException if {@code filters} is {@code null}.
   */
  public FilterDependency(
      final Collection<TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    this.filters = filters;
  }
  
  @Override
  public boolean test(
      final MemoryAccess access1, final MemoryAccess access2, final MemoryDependency dependency) {

    if (dependency == null) {
      return true;
    }

    for (final MemoryHazard hazard : dependency.getHazards()) {
      for (final TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard> filter : filters) {
        if (!filter.test(access1, access2, hazard)) {
          // Filter off.
          return false;
        }
      }
    }

    return true;
  }
}
