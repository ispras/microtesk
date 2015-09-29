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
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class AddressCoverageExtractor {
  private static enum Hazard {
    ADDR_NOT_EQUAL {
      @Override
      public MemoryHazard getHazard(final MmuAddressType address) {
        // Address1 != Address2.
        return new MemoryHazard(
            MemoryHazard.Type.ADDR_NOT_EQUAL,
            address,
            MmuCondition.neq(address.getVariable()));
      }
    },

    ADDR_EQUAL {
      @Override
      public MemoryHazard getHazard(final MmuAddressType address) {
        // Address1 == Address2.
        return new MemoryHazard(
            MemoryHazard.Type.ADDR_EQUAL,
            address,
            MmuCondition.eq(address.getVariable()));
      }
    };

    public abstract MemoryHazard getHazard(MmuAddressType address);
  }

  private final Collection<MemoryHazard> hazards = new ArrayList<>();

  public AddressCoverageExtractor(final MmuAddressType address) {
    InvariantChecks.checkNotNull(address);

    // Address1 != Address2.
    hazards.add(Hazard.ADDR_NOT_EQUAL.getHazard(address));
    // Address1 == Address2.
    hazards.add(Hazard.ADDR_EQUAL.getHazard(address));
  }

  public Collection<MemoryHazard> getHazards() {
    return hazards;
  }
}
