/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.spec;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.Range;

/**
 * {@link MmuSegment} represents a virtual memory segment (address space).
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuSegment implements Range<BitVector> {
  private final String name;
  private final MmuAddressInstance vaType;
  private final MmuAddressInstance paType;
  private final BitVector startAddress;
  private final BitVector endAddress;

  public MmuSegment(
      final String name,
      final MmuAddressInstance vaType,
      final MmuAddressInstance paType,
      final BitVector startAddress,
      final BitVector endAddress) {
    InvariantChecks.checkNotNull(name);

    this.name = name;
    this.vaType = vaType;
    this.paType = paType;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
  }

  public final String getName() {
    return name;
  }

  public final MmuAddressInstance getVaType() {
    return vaType;
  }

  public final MmuAddressInstance getPaType() {
    return paType;
  }

  public final BitVector getStartAddress() {
    return startAddress;
  }

  public final BitVector getEndAddress() {
    return endAddress;
  }

  @Override
  public final BitVector getMin() {
    return startAddress;
  }

  @Override
  public final BitVector getMax() {
    return endAddress;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if ((o == null) || !(o instanceof MmuSegment)) {
      return false;
    }

    final MmuSegment r = (MmuSegment) o;
    return name.equals(r.name);
  }
}
