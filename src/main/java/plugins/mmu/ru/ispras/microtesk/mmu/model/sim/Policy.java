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
 * Base interface to be implemented by all data replacement policies.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
abstract class Policy {
  /**
   * The associativity.
   */
  protected final int associativity;

  /**
   * Constructs a data replacement controller.
   *
   * @param associativity the buffer associativity.
   */
  protected Policy(final int associativity) {
    if (associativity <= 0) {
      throw new IllegalArgumentException(String.format("Illegal associativity %d", associativity));
    }

    this.associativity = associativity;
  }

  /**
   * Handles a buffer hit.
   *
   * @param index the line being hit.
   */
  public abstract void accessLine(int index);

  /**
   * Handles a buffer miss.
   *
   * @return the line to be replaced.
   */
  public abstract int chooseVictim();

  /**
   * Resets the state of the policy object.
   */
  public abstract void resetState();
}
