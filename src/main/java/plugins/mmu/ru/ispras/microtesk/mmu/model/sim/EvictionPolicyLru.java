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

/**
 * {@link EvictionPolicyLru} implements the LRU (Least Recently Used) eviction policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class EvictionPolicyLru extends EvictionPolicy {
  /** Maps index to time. */
  private int[] times;
  /** Current time. */
  private int time;

  EvictionPolicyLru(final int associativity) {
    super(associativity);

    this.times = new int[associativity];
    resetState();
  }

  @Override
  public void onAccess(final int index) {
    times[index] = ++time;
  }

  @Override
  public void onEvict(final int index) {
    times[index] = 0;
  }

  @Override
  public int getVictim() {
    int victim = 0;
    int minTime = times[0];

    for (int i = 1; i < times.length; i++) {
      if (times[i] < minTime) {
        victim = i;
        minTime = times[i];
      }
    }

    return victim;
  }

  @Override
  public void resetState() {
    time = 0;
    for (int i = 0; i < times.length; i++) {
      times[i] = 0;
    }
  }
}
