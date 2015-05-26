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

package ru.ispras.microtesk.translator.mmu.coverage;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.MmuAddress;
import ru.ispras.microtesk.translator.mmu.spec.MmuCondition;
import ru.ispras.microtesk.translator.mmu.spec.MmuEquality;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class AddressCoverageExtractor {
  private final MmuAddress address;

  public AddressCoverageExtractor(final MmuAddress address) {
    InvariantChecks.checkNotNull(address);
    this.address = address;
  }

  /**
   * Returns the list of the address hazards.
   * 
   * @return the hazards list.
   */
  public List<Hazard> getHazards() {
    final List<Hazard> hazardList = new ArrayList<>();

    // Address1 != Address2.
    final MmuEquality equalityNoEqual =
        new MmuEquality(MmuEquality.Type.NOT_EQUAL, MmuExpression.VAR(address.getAddress()));
    final MmuCondition conditionNoEqual = new MmuCondition(equalityNoEqual);

    final Hazard hazardNoEqual = new Hazard(Hazard.Type.ADDR_NOT_EQUAL, address, conditionNoEqual);
    hazardList.add(hazardNoEqual);

    // Address1 == Address2.
    final MmuEquality equalityEqual =
        new MmuEquality(MmuEquality.Type.EQUAL, MmuExpression.VAR(address.getAddress()));
    final MmuCondition conditionEqual = new MmuCondition(equalityEqual);

    final Hazard hazardEqual = new Hazard(Hazard.Type.ADDR_EQUAL, address, conditionEqual);
    hazardList.add(hazardEqual);

    return hazardList;
  }
}
