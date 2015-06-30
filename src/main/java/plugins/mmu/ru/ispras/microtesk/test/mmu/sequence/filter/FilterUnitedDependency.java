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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.coverage.ExecutionPath;
import ru.ispras.microtesk.translator.mmu.coverage.UnitedDependency;
import ru.ispras.microtesk.translator.mmu.coverage.UnitedHazard;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuAddress;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuDevice;
import ru.ispras.microtesk.utils.function.BiPredicate;

/**
 * {@link FilterUnitedDependency} composes device-level filters into an execution-level filter.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class FilterUnitedDependency implements BiPredicate<ExecutionPath, UnitedDependency> {
  /** The device-level filters to be composed. */
  private final Collection<BiPredicate<ExecutionPath, UnitedHazard>> filters;

  /**
   * Constructs an execution-level filter from the collection of device-level filters.
   * 
   * @param filters the collection of device-level filters to be composed.
   * @throws IllegalArgumentException if {@code filters} is {@code null}.
   */
  public FilterUnitedDependency(final Collection<BiPredicate<ExecutionPath, UnitedHazard>> filters) {
    InvariantChecks.checkNotNull(filters);
    this.filters = filters;
  }

  @Override
  public boolean test(final ExecutionPath execution, final UnitedDependency dependency) {
    final Set<UnitedHazard> hazards = new LinkedHashSet<>();

    final Map<MmuAddress, UnitedHazard> addrHazards = dependency.getAddrHazards();
    final Map<MmuDevice, UnitedHazard> deviceHazards = dependency.getDeviceHazards();

    hazards.addAll(addrHazards.values());
    hazards.addAll(deviceHazards.values());

    for (final UnitedHazard hazard : hazards) {
      for (final BiPredicate<ExecutionPath, UnitedHazard> filter : filters) {
        if (!filter.test(execution, hazard)) {
          return false;
        }
      }
    }

    return true;
  }
}
