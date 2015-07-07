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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.IntegerVariable;

/**
 * {@link MmuAddress} describes an address, i.e. a parameter used to access a buffer.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MmuAddress {
  /** Address description (the variable contains the name and the bit length). */
  private final IntegerVariable addrVariable;

  public MmuAddress(final IntegerVariable address) {
    InvariantChecks.checkNotNull(address);
    this.addrVariable = address;
  }

  public IntegerVariable getVariable() {
    return addrVariable;
  }

  public String getName() {
    return addrVariable.getName();
  }

  public int getWidth() {
    return addrVariable.getWidth();
  }

  @Override
  public String toString() {
    return String.format("%s[%d]", getName(), getWidth());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || !(obj instanceof MmuAddress)) {
      return false;
    }

    final MmuAddress other = (MmuAddress) obj;
    return getName().equals(other.getName());
  }
}
