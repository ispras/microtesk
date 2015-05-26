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
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.mmu.Template;
import ru.ispras.microtesk.translator.mmu.coverage.Dependency;
import ru.ispras.microtesk.translator.mmu.coverage.ExecutionPath;
import ru.ispras.microtesk.translator.mmu.coverage.Hazard;
import ru.ispras.microtesk.translator.mmu.coverage.UnitedDependency;
import ru.ispras.microtesk.translator.mmu.coverage.UnitedHazard;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * {@link FilterBuilder} composes all kinds of filters into one template-level filter.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterBuilder {

  private final Collection<Predicate<ExecutionPath>>
    executionFilters = new ArrayList<>();

  private final Collection<TriPredicate<ExecutionPath, ExecutionPath, Hazard>>
    hazardFilters = new ArrayList<>();

  private final Collection<TriPredicate<ExecutionPath, ExecutionPath, Dependency>>
    dependencyFilters = new ArrayList<>();

  private final Collection<BiPredicate<ExecutionPath, UnitedHazard>>
    unitedHazardFilters = new ArrayList<>();

  private final Collection<BiPredicate<ExecutionPath, UnitedDependency>>
    unitedDependencyFilters = new ArrayList<>();

  private final Collection<Predicate<Template>>
    templateFilters = new ArrayList<>();

  public FilterBuilder() {
  }

  public FilterBuilder(final FilterBuilder r) {
    InvariantChecks.checkNotNull(r);
    addFilterBuilder(r);
  }

  public void addExecutionFilter(final Predicate<ExecutionPath> filter) {
    InvariantChecks.checkNotNull(filter);
    executionFilters.add(filter);
  }

  public void addExecutionFilters(final Collection<Predicate<ExecutionPath>> filters) {
    InvariantChecks.checkNotNull(filters);
    executionFilters.addAll(filters);
  }

  public void addHazardFilter(final TriPredicate<ExecutionPath, ExecutionPath, Hazard> filter) {
    InvariantChecks.checkNotNull(filter);
    hazardFilters.add(filter);
  }

  public void addHazardFilters(
      final Collection<TriPredicate<ExecutionPath, ExecutionPath, Hazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    hazardFilters.addAll(filters);
  }

  public void addDependencyFilter(
      final TriPredicate<ExecutionPath, ExecutionPath, Dependency> filter) {
    InvariantChecks.checkNotNull(filter);
    dependencyFilters.add(filter);
  }

  public void addDependencyFilters(
      final Collection<TriPredicate<ExecutionPath, ExecutionPath, Dependency>> filters) {
    InvariantChecks.checkNotNull(filters);
    dependencyFilters.addAll(filters);
  }

  public void addUnitedHazardFilter(final BiPredicate<ExecutionPath, UnitedHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    unitedHazardFilters.add(filter);
  }

  public void addUnitedHazardFilters(
      final Collection<BiPredicate<ExecutionPath, UnitedHazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    unitedHazardFilters.addAll(filters);
  }

  public void addUnitedDependencyFilter(final BiPredicate<ExecutionPath, UnitedDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    unitedDependencyFilters.add(filter);
  }

  public void addUnitedDependencyFilters(
      final Collection<BiPredicate<ExecutionPath, UnitedDependency>> filters) {
    InvariantChecks.checkNotNull(filters);
    unitedDependencyFilters.addAll(filters);
  }

  public void addTemplateFilter(final Predicate<Template> filter) {
    InvariantChecks.checkNotNull(filter);
    templateFilters.add(filter);
  }

  public void addTemplateFilters(final Collection<Predicate<Template>> filters) {
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

  public Predicate<Template> build() {
    final Collection<TriPredicate<ExecutionPath, ExecutionPath, Dependency>>
      newDependencyFilters = new ArrayList<>(dependencyFilters);
    newDependencyFilters.add(new FilterDependency(hazardFilters));

    final Collection<BiPredicate<ExecutionPath, UnitedDependency>>
      newUnitedDependencyFilters = new ArrayList<>(unitedDependencyFilters);
    newUnitedDependencyFilters.add(new FilterUnitedDependency(unitedHazardFilters));

    final Collection<Predicate<Template>>
      newTemplateFilters = new ArrayList<>(templateFilters);
    newTemplateFilters.add(new FilterTemplate(
        executionFilters, newDependencyFilters, newUnitedDependencyFilters));

    return new FilterComposite(newTemplateFilters);
  }
}
