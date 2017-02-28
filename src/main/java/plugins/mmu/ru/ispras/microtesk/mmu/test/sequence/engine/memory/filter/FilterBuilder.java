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
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferUnitedHazard;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * {@link FilterBuilder} composes all kinds of filters into one template-level filter.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterBuilder {

  private final Collection<TriPredicate<MemoryAccess, MemoryAccess, BufferHazard.Instance>>
    hazardFilters = new ArrayList<>();

  private final Collection<TriPredicate<MemoryAccess, MemoryAccess, BufferDependency>>
    dependencyFilters = new ArrayList<>();

  private final Collection<BiPredicate<MemoryAccess, BufferUnitedHazard>>
    unitedHazardFilters = new ArrayList<>();

  private final Collection<BiPredicate<MemoryAccess, BufferUnitedDependency>>
    unitedDependencyFilters = new ArrayList<>();

  private final Collection<Predicate<MemoryAccessStructure>>
    structureFilters = new ArrayList<>();

  public FilterBuilder() {}

  public FilterBuilder(final FilterBuilder r) {
    InvariantChecks.checkNotNull(r);
    addFilterBuilder(r);
  }

  public Collection<TriPredicate<MemoryAccess, MemoryAccess, BufferHazard.Instance>> getHazardFilters() {
    return hazardFilters;
  }

  public Collection<TriPredicate<MemoryAccess, MemoryAccess, BufferDependency>> getDependencyFilters() {
    return dependencyFilters;
  }

  public Collection<BiPredicate<MemoryAccess, BufferUnitedHazard>> getUnitedHazardFilters() {
    return unitedHazardFilters;
  }

  public Collection<BiPredicate<MemoryAccess, BufferUnitedDependency>> getUnitedDependencyFilters() {
    return unitedDependencyFilters;
  }

  public Collection<Predicate<MemoryAccessStructure>> getStructureFilters() {
    return structureFilters;
  }

  public void addHazardFilter(final TriPredicate<MemoryAccess, MemoryAccess, BufferHazard.Instance> filter) {
    InvariantChecks.checkNotNull(filter);
    hazardFilters.add(filter);
  }

  public void addHazardFilters(
      final Collection<TriPredicate<MemoryAccess, MemoryAccess, BufferHazard.Instance>> filters) {
    InvariantChecks.checkNotNull(filters);
    hazardFilters.addAll(filters);
  }

  public void addDependencyFilter(
      final TriPredicate<MemoryAccess, MemoryAccess, BufferDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    dependencyFilters.add(filter);
  }

  public void addDependencyFilters(
      final Collection<TriPredicate<MemoryAccess, MemoryAccess, BufferDependency>> filters) {
    InvariantChecks.checkNotNull(filters);
    dependencyFilters.addAll(filters);
  }

  public void addUnitedHazardFilter(final BiPredicate<MemoryAccess, BufferUnitedHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    unitedHazardFilters.add(filter);
  }

  public void addUnitedHazardFilters(
      final Collection<BiPredicate<MemoryAccess, BufferUnitedHazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    unitedHazardFilters.addAll(filters);
  }

  public void addUnitedDependencyFilter(final BiPredicate<MemoryAccess, BufferUnitedDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    unitedDependencyFilters.add(filter);
  }

  public void addUnitedDependencyFilters(
      final Collection<BiPredicate<MemoryAccess, BufferUnitedDependency>> filters) {
    InvariantChecks.checkNotNull(filters);
    unitedDependencyFilters.addAll(filters);
  }

  public void addStructureFilter(final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(filter);
    structureFilters.add(filter);
  }

  public void addStructureFilters(final Collection<Predicate<MemoryAccessStructure>> filters) {
    InvariantChecks.checkNotNull(filters);
    structureFilters.addAll(filters);
  }

  public void addFilterBuilder(final FilterBuilder builder) {
    InvariantChecks.checkNotNull(builder);

    hazardFilters.addAll(builder.hazardFilters);
    dependencyFilters.addAll(builder.dependencyFilters);
    unitedHazardFilters.addAll(builder.unitedHazardFilters);
    unitedDependencyFilters.addAll(builder.unitedDependencyFilters);
    structureFilters.addAll(builder.structureFilters);
  }

  public Predicate<MemoryAccessStructure> build() {
    final Collection<TriPredicate<MemoryAccess, MemoryAccess, BufferDependency>>
      newDependencyFilters = new ArrayList<>(dependencyFilters);
    newDependencyFilters.add(new FilterDependency(hazardFilters));

    final Collection<BiPredicate<MemoryAccess, BufferUnitedDependency>>
      newUnitedDependencyFilters = new ArrayList<>(unitedDependencyFilters);
    newUnitedDependencyFilters.add(new FilterUnitedDependency(unitedHazardFilters));

    final Collection<Predicate<MemoryAccessStructure>>
      newTemplateFilters = new ArrayList<>(structureFilters);
    newTemplateFilters.add(new FilterStructure(
        newDependencyFilters, newUnitedDependencyFilters));

    return new FilterComposite(newTemplateFilters);
  }
}
