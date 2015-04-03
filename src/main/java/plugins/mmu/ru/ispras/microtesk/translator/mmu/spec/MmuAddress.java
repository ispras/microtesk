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

package ru.ispras.microtesk.translator.mmu.spec;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

/**
 * This class describes an address, i.e. a parameter used to access some MMU device.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MmuAddress {
  /** The address. */
  private final IntegerVariable address;

  /**
   * Constructs an address.
   * 
   * @param address the address variable.
   * @throws NullPointerException if {@code address} is null.
   */
  public MmuAddress(final IntegerVariable address) {
    InvariantChecks.checkNotNull(address);
    this.address = address;
  }

  /**
   * Returns the address variable.
   * 
   * @return the address variable.
   */
  public IntegerVariable getAddress() {
    return address;
  }

  /**
   * Returns the list of the address conflicts.
   * 
   * @return the conflicts list.
   */
  public List<MmuConflict> getConflicts() {
    final List<MmuConflict> conflicts = new ArrayList<>();

    // Address1 != Address2.
    final MmuEquality equalityNoEqual =
        new MmuEquality(MmuEquality.Type.NOT_EQUAL, MmuExpression.VAR(address));
    final MmuCondition conditionNoEqual = new MmuCondition(equalityNoEqual);

    final MmuConflict conflictNoEqual =
        new MmuConflict(MmuConflict.Type.ADDR_NOT_EQUAL, this, conditionNoEqual);
    conflicts.add(conflictNoEqual);

    // Address1 == Address2.
    final MmuEquality equalityEqual =
        new MmuEquality(MmuEquality.Type.EQUAL, MmuExpression.VAR(address));
    final MmuCondition conditionEqual = new MmuCondition(equalityEqual);

    final MmuConflict conflictEqual =
        new MmuConflict(MmuConflict.Type.ADDR_EQUAL, this, conditionEqual);
    conflicts.add(conflictEqual);

    return conflicts;
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", address.getName(), address.getWidth());
  }
}
