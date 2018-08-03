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

import ru.ispras.fortress.randomizer.Randomizer;

/**
 * The random data replacement policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class PolicyRandom extends Policy {
  /**
   * Constructs a random data replacement controller.
   *
   * @param associativity the buffer associativity.
   */
  PolicyRandom(final int associativity) {
    super(associativity);
  }

  @Override
  public void accessLine(final int index) {
    // Do nothing.
  }

  @Override
  public int chooseVictim() {
    return Randomizer.get().nextIntRange(0, associativity - 1);
  }
}
