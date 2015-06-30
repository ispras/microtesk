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

import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.coverage.Dependency;
import ru.ispras.microtesk.translator.mmu.coverage.ExecutionPath;
import ru.ispras.microtesk.translator.mmu.coverage.Hazard;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * {@link FilterDependency} composes hazard-level filters into a dependency-level filter.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterDependency implements TriPredicate<ExecutionPath, ExecutionPath, Dependency> {
  /** The hazard-level filters to be composed. */
  private final Collection<TriPredicate<ExecutionPath, ExecutionPath, Hazard>> filters;

  /**
   * Constructs a dependency-level filter from the collection of hazard-level filters.
   * 
   * @param filters the collection of hazard-level filters to be composed.
   * @throws IllegalArgumentException if {@code filters} is {@code null}.
   */
  public FilterDependency(final Collection<TriPredicate<ExecutionPath, ExecutionPath, Hazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    this.filters = filters;
  }
  
  @Override
  public boolean test(final ExecutionPath execution1, final ExecutionPath execution2,
      final Dependency dependency) {

    if (dependency == null) {
      return true;
    }

    for (final Hazard hazard : dependency.getHazards()) {
      for (final TriPredicate<ExecutionPath, ExecutionPath, Hazard> filter : filters) {
        if (!filter.test(execution1, execution2, hazard)) {
          // Filter off.
          return false;
        }
      }
    }

    return true;
  }
}
