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

package ru.ispras.microtesk.model.api.memory;

import ru.ispras.fortress.util.InvariantChecks;

public final class MemoryAccessMode {
  public final boolean r;
  public final boolean w;
  public final boolean x;

  public MemoryAccessMode(
      final boolean r,
      final boolean w,
      final boolean x) {
    this.r = r;
    this.w = w;
    this.x = x;
  }

  public MemoryAccessMode(final String rwx) {
    InvariantChecks.checkNotNull(rwx);
    InvariantChecks.checkTrue(rwx.length() == 3);

    final String mode = rwx.toLowerCase();

    this.r = mode.charAt(0) == 'r';
    this.w = mode.charAt(1) == 'w';
    this.x = mode.charAt(2) == 'x';
  }

  @Override
  public String toString() {
    return String.format(
        "%s%s%s", (r ? "r" : "-"), (w ? "w" : "-"), (x ? "x" : "-"));
  }
}
