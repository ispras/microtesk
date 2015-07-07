/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class AddressCoverageExtractor {
  private final List<MemoryHazard> hazards = new ArrayList<>();

  public AddressCoverageExtractor(final MmuAddress address) {
    InvariantChecks.checkNotNull(address);

    // Address1 != Address2.
    hazards.add(getAddrNotEqualHazard(address));
    // Address1 == Address2.
    hazards.add(getAddrEqualHazard(address));
  }

  public List<MemoryHazard> getHazards() {
    return hazards;
  }

  private MemoryHazard getAddrNotEqualHazard(final MmuAddress address) {
    // Address1 != Address2.
    return new MemoryHazard(MemoryHazard.Type.ADDR_NOT_EQUAL, address,
        MmuCondition.neq(address.getVariable()));
  }

  private MemoryHazard getAddrEqualHazard(final MmuAddress address) {
    // Address1 == Address2.
    return new MemoryHazard(MemoryHazard.Type.ADDR_EQUAL, address,
        MmuCondition.eq(address.getVariable()));
  }
}
