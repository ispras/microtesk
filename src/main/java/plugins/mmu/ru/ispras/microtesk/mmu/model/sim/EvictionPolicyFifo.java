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
 * {@link EvictionPolicyFifo} implements the FIFO (First In - First Out) eviction policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class EvictionPolicyFifo extends EvictionPolicy {
  /** Unallocated lines: sorted in order of eviction. */
  private final List<Integer> free = new ArrayList<>();
  /** Allocated lines: sorted from LRU to MRU. */
  private final List<Integer> used = new ArrayList<>();

  EvictionPolicyFifo(final int associativity) {
    super(associativity);
    resetState();
  }

  @Override
  public void onAccess(final int index) {
    if (remove(used, index) || remove(free, index)) {
      used.add(index);
    }
  }

  @Override
  public void onEvict(final int index) {
    if (remove(used, index)) {
      free.add(index);
    }
  }

  private static boolean remove(final List<Integer> fifo, final int index) {
    for (int i = 0; i < fifo.size(); i++) {
      if (fifo.get(i) == index) {
        fifo.remove(i);
        fifo.add(index);
        return true;
      }
    }
    return false;
  }

  @Override
  public int getVictim() {
    return !free.isEmpty() ? free.get(0) : used.get(0);
  }

  @Override
  public void resetState() {
    free.clear();
    used.clear();

    for (int i = 0; i < associativity; i++) {
      free.add(i);
    }
  }
}
