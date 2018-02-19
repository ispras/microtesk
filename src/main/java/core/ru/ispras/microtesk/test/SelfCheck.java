/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.engine.AddressingModeWrapper;

public final class SelfCheck {
  private final AddressingModeWrapper mode;

  public SelfCheck(final AddressingModeWrapper mode) {
    InvariantChecks.checkNotNull(mode);
    this.mode = mode;
  }

  public AddressingModeWrapper getMode() {
    return mode;
  }

  @Override
  public String toString() {
    return String.format("Check: %s", mode);
  }
}
