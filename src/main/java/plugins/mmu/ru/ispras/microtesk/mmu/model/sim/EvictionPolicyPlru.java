/*
 * Copyright 2014-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link EvictionPolicyPlru} implements the PLRU (Pseudo Least Recently Used) eviction policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class EvictionPolicyPlru extends EvictionPolicy {
  /** PLRU bits. */
  private int bits;
  /** Latest access. */
  private int last;

  /**
   * Constructs a PLRU data replacement controller.
   *
   * @param associativity the buffer associativity.
   */
  EvictionPolicyPlru(final int associativity) {
    super(associativity);

    InvariantChecks.checkTrue(associativity <= Integer.SIZE,
        String.format("Illegal associativity %d", associativity));

    resetState();
  }

  @Override
  public void onAccess(final int index) {
    final int mask = (1 << index);

    bits |= mask;
    if (bits == ((1 << associativity) - 1)) {
      bits = mask;
    }

    last = index;
  }

  @Override
  public void onEvict(final int index) {
    final int mask = (1 << index);
    bits &= ~mask;
  }

  @Override
  public int getVictim() {
    for (int i = 0; i < associativity; i++) {
      final int j = (last + i) % associativity;

      if ((bits & (1 << j)) == 0) {
        return j;
      }
    }

    throw new IllegalStateException("All bits are set to 1");
  }

  @Override
  public void resetState() {
    bits = 0;
    last = 0;
  }
}
