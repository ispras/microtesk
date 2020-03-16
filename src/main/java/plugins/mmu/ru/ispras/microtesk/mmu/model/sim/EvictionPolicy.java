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
 * {@link EvictionPolicy} is a base class for a data replacement policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
abstract class EvictionPolicy {
  /** Associativity. */
  protected final int associativity;

  /**
   * Constructs a data replacement controller.
   *
   * @param associativity the buffer associativity.
   */
  protected EvictionPolicy(final int associativity) {
    InvariantChecks.checkGreaterThanZero(associativity);
    this.associativity = associativity;
  }

  /**
   * Handles a line access.
   *
   * @param index the line being accessed.
   */
  public abstract void onAccess(int index);

  /**
   * Handles a line eviction.
   *
   * @param index the line being evicted.
   */
  public abstract void onEvict(int index);

  /**
   * Chooses a line to be replaced.
   *
   * @return the line to be replaced.
   */
  public abstract int getVictim();

  /**
   * Resets the state of the policy object.
   */
  public abstract void resetState();
}
