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

import java.util.ArrayList;
import java.util.List;

/**
 * {@link EvictionPolicyFifo} implements the FIFO (First In - First Out) data replacement policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class EvictionPolicyFifo extends EvictionPolicy {
  /** Keeps line indices in the order of their usage. */
  private final List<Integer> fifo = new ArrayList<>();

  /**
   * Constructs a FIFO data replacement controller.
   *
   * @param associativity the buffer associativity.
   */
  EvictionPolicyFifo(final int associativity) {
    super(associativity);

    for (int i = 0; i < associativity; i++) {
      fifo.add(i);
    }
  }

  @Override
  public void accessLine(final int index) {
    for (int i = 0; i < fifo.size(); i++) {
      if (fifo.get(i) == index) {
        fifo.remove(i);
        fifo.add(index);

        return;
      }
    }

    throw new IllegalStateException(String.format("Index %d cannot be found.", index));
  }

  @Override
  public int chooseVictim() {
    return fifo.get(0);
  }

  @Override
  public void resetState() {
    for (int i = 0; i < associativity; i++) {
      fifo.set(i, i);
    }
  }
}
