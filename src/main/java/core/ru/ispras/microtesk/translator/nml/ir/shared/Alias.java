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

package ru.ispras.microtesk.translator.nml.ir.shared;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.memory.Location;

public final class Alias {
  public enum Kind {
    LOCATION,
    MEMORY
  }

  private final Kind kind;
  private final Location location;
  private final Memory memory;
  private final int min;
  private final int max;

  public static Alias forLocation(
      final Location location) {
    InvariantChecks.checkNotNull(location);
    return new Alias(Kind.LOCATION, location, null, 0, 0);
  }

  public static Alias forMemory(
      final Memory memory, final int min, final int max) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkBounds(min, memory.getLength());
    InvariantChecks.checkBounds(max, memory.getLength());
    return new Alias(Kind.MEMORY, null, memory, min, max);
  }

  private Alias(
      final Kind kind,
      final Location location,
      final Memory memory,
      final int min,
      final int max) {
    this.kind = kind;
    this.location = location;
    this.memory = memory;
    this.min = min;
    this.max = max;
  }

  public Kind getKind() {
    return kind;
  }

  public Location getLocation() {
    checkKind(Kind.LOCATION);
    return location;
  }

  public Memory getMemory() {
    checkKind(Kind.MEMORY);
    return memory;
  }

  public int getMin() {
    checkKind(Kind.MEMORY);
    return min;
  }

  public int getMax() {
    checkKind(Kind.MEMORY);
    return max;
  }

  private void checkKind(final Kind expectedKind) {
    if (expectedKind != kind) {
      throw new UnsupportedOperationException(
          "Operation is not support for kind " + kind);
    }
  }
}
