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

package ru.ispras.microtesk.mmu.model.sim;

/**
 * The LRU (Least Recently Used) data replacement policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class PolicyLru extends Policy {
  /** Maps index to time. */
  private int[] times;
  /** Current time. */
  private int time = 0;

  /**
   * Constructs an LRU data replacement controller.
   *
   * @param associativity the buffer associativity.
   */
  PolicyLru(final int associativity) {
    super(associativity);

    times = new int[associativity];
    for (int i = 0; i < associativity; i++) {
      times[i] = time++;
    }
  }

  @Override
  public void accessLine(final int index) {
    times[index] = time++;
  }

  @Override
  public int chooseVictim() {
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
}
