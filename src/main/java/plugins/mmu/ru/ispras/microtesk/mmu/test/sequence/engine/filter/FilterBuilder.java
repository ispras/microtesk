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

import java.util.ArrayList;
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.iterator.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryAccess;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryDependency;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryUnitedDependency;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryUnitedHazard;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * {@link FilterBuilder} composes all kinds of filters into one template-level filter.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterBuilder {

  private final Collection<Predicate<MemoryAccess>>
    executionFilters = new ArrayList<>();

  private final Collection<TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard>>
    hazardFilters = new ArrayList<>();

  private final Collection<TriPredicate<MemoryAccess, MemoryAccess, MemoryDependency>>
    dependencyFilters = new ArrayList<>();

  private final Collection<BiPredicate<MemoryAccess, MemoryUnitedHazard>>
    unitedHazardFilters = new ArrayList<>();

  private final Collection<BiPredicate<MemoryAccess, MemoryUnitedDependency>>
    unitedDependencyFilters = new ArrayList<>();

  private final Collection<Predicate<MemoryAccessStructure>>
    templateFilters = new ArrayList<>();

  public FilterBuilder() {
  }

  public FilterBuilder(final FilterBuilder r) {
    InvariantChecks.checkNotNull(r);
    addFilterBuilder(r);
  }

  public void addExecutionFilter(final Predicate<MemoryAccess> filter) {
    InvariantChecks.checkNotNull(filter);
    executionFilters.add(filter);
  }

  public void addExecutionFilters(final Collection<Predicate<MemoryAccess>> filters) {
    InvariantChecks.checkNotNull(filters);
    executionFilters.addAll(filters);
  }

  public void addHazardFilter(final TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    hazardFilters.add(filter);
  }

  public void addHazardFilters(
      final Collection<TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    hazardFilters.addAll(filters);
  }

  public void addDependencyFilter(
      final TriPredicate<MemoryAccess, MemoryAccess, MemoryDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    dependencyFilters.add(filter);
  }

  public void addDependencyFilters(
      final Collection<TriPredicate<MemoryAccess, MemoryAccess, MemoryDependency>> filters) {
    InvariantChecks.checkNotNull(filters);
    dependencyFilters.addAll(filters);
  }

  public void addUnitedHazardFilter(final BiPredicate<MemoryAccess, MemoryUnitedHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    unitedHazardFilters.add(filter);
  }

  public void addUnitedHazardFilters(
      final Collection<BiPredicate<MemoryAccess, MemoryUnitedHazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    unitedHazardFilters.addAll(filters);
  }

  public void addUnitedDependencyFilter(final BiPredicate<MemoryAccess, MemoryUnitedDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    unitedDependencyFilters.add(filter);
  }

  public void addUnitedDependencyFilters(
      final Collection<BiPredicate<MemoryAccess, MemoryUnitedDependency>> filters) {
    InvariantChecks.checkNotNull(filters);
    unitedDependencyFilters.addAll(filters);
  }

  public void addTemplateFilter(final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(filter);
    templateFilters.add(filter);
  }

  public void addTemplateFilters(final Collection<Predicate<MemoryAccessStructure>> filters) {
    InvariantChecks.checkNotNull(filters);
    templateFilters.addAll(filters);
  }

  public void addFilterBuilder(final FilterBuilder builder) {
    InvariantChecks.checkNotNull(builder);

    executionFilters.addAll(builder.executionFilters);
    hazardFilters.addAll(builder.hazardFilters);
    dependencyFilters.addAll(builder.dependencyFilters);
    unitedHazardFilters.addAll(builder.unitedHazardFilters);
    unitedDependencyFilters.addAll(builder.unitedDependencyFilters);
    templateFilters.addAll(builder.templateFilters);
  }

  public Predicate<MemoryAccessStructure> build() {
    final Collection<TriPredicate<MemoryAccess, MemoryAccess, MemoryDependency>>
      newDependencyFilters = new ArrayList<>(dependencyFilters);
    newDependencyFilters.add(new FilterDependency(hazardFilters));

    final Collection<BiPredicate<MemoryAccess, MemoryUnitedDependency>>
      newUnitedDependencyFilters = new ArrayList<>(unitedDependencyFilters);
    newUnitedDependencyFilters.add(new FilterUnitedDependency(unitedHazardFilters));

    final Collection<Predicate<MemoryAccessStructure>>
      newTemplateFilters = new ArrayList<>(templateFilters);
    newTemplateFilters.add(new FilterTemplate(
        executionFilters, newDependencyFilters, newUnitedDependencyFilters));

    return new FilterComposite(newTemplateFilters);
  }
}
