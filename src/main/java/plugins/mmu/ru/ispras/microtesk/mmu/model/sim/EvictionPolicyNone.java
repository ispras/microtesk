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
 * {@link EvictionPolicyNone} implements the trivial eviction policy.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class EvictionPolicyNone extends EvictionPolicy {

  EvictionPolicyNone(final int associativity) {
    super(associativity);
  }

  @Override
  public void onAccess(final int index) {
    // Do nothing.
  }

  @Override
  public void onEvict(final int index) {
    // Do nothing.
  }

  @Override
  public int getVictim() {
    return 0;
  }

  @Override
  public void resetState() {
    // Do nothing.
  }
}
