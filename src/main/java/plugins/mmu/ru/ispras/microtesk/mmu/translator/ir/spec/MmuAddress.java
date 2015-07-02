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
  /** The variable that describes the address. */
  private final IntegerVariable address;

  /**
   * Constructs an address.
   * 
   * @param address variable describing the address.
   * @throws NullPointerException if {@code address} is {@code null}.
   */
  public MmuAddress(final IntegerVariable address) {
    InvariantChecks.checkNotNull(address);
    this.address = address;
  }

  /**
   * Returns the variable that describes the address.
   * 
   * @return variable that describes the address.
   */
  public IntegerVariable getAddress() {
    return address;
  }

  /**
   * Returns the address name.
   * 
   * @return address name.
   */
  public String getName() {
    return address.getName();
  }

  /**
   * Returns the address width (in bits).
   * 
   * @return address width (in bits).
   */
  public int getWidth() {
    return address.getWidth();
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
