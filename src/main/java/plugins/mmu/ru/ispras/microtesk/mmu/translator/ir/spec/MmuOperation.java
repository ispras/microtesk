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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;

public final class MmuOperation {
  private final String name;
  private final MmuAddressType addressType;
  private final Map<IntegerField, MmuBinding> assignments;

  public MmuOperation(
      final String name,
      final MmuAddressType addressType,
      final MmuBinding... assignments) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(addressType);

    this.name = name;
    this.addressType = addressType;

    final Map<IntegerField, MmuBinding> map = new LinkedHashMap<>();
    for (final MmuBinding binding : assignments) {
      map.put(binding.getLhs(), binding);
    }

    this.assignments = Collections.unmodifiableMap(map);
  }

  public String getName() {
    return name;
  }

  public MmuAddressType getAddressType() {
    return addressType;
  }

  public Map<IntegerField, MmuBinding> getAssignments() {
    return assignments;
  }
}
