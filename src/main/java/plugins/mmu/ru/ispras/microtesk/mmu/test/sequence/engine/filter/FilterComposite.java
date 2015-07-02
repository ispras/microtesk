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

import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.iterator.MemoryAccessStructure;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link FilterComposite} composes template-level filters.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterComposite implements Predicate<MemoryAccessStructure> {
  private final Collection<Predicate<MemoryAccessStructure>> filters;

  /**
   * Constructs a template-level filter from the collection of template-level filters.
   * 
   * @param filters the collection of template-level filters to be composed.
   * @throws IllegalArgumentException if {@code filters} is {@code null}.
   */
  public FilterComposite(final Collection<Predicate<MemoryAccessStructure>> filters) {
    InvariantChecks.checkNotNull(filters);
    this.filters = filters;
  }
  
  @Override
  public boolean test(final MemoryAccessStructure template) {
    for (final Predicate<MemoryAccessStructure> filter : filters) {
      if (!filter.test(template)) {
        // Filter off.
        return false;
      }
    }

    return true;
  }
}
