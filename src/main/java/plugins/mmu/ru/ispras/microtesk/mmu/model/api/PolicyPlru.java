/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.api;

/**
 * The PLRU (Pseudo Least Recently Used) data replacement policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class PolicyPlru extends Policy {
  /** The PLRU bits. */
  private int bits;
  /** The last access. */
  private int last;

  /**
   * Constructs a PLRU data replacement controller.
   *
   * @param associativity the buffer associativity.
   */
  PolicyPlru(final int associativity) {
    super(associativity);

    if (associativity > 32) {
      throw new IllegalArgumentException(String.format("Illegal associativity %d", associativity));
    }
  }

  @Override
  public void accessLine(final int index) {
    setBit(index);
  }

  @Override
  public int chooseVictim() {
    for (int i = 0; i < associativity; i++) {
      final int j = (last + i) % associativity;

      if ((bits & (1 << j)) == 0) {
        return j;
      }
    }

    throw new IllegalStateException("All bits are set to 1");
  }

  private void setBit(final int i) {
    final int mask = (1 << (last = i));

    bits |= mask;
    if (bits == ((1 << associativity) - 1)) {
      bits = mask;
    }
  }
}
