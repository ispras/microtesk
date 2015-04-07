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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

/**
 * The Field class stores information on location of a field in
 * a data structure (e.g. address). This is needed to extract fields 
 * that serve a common purpose.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class Field implements Comparable<Field>{
  public final int min;
  public final int max;

  public Field(int min, int max) {
    this.min = Math.min(min, max);
    this.max = Math.max(min, max);
  }

  public int getWidth() {
    return max - min + 1;
  }

  public boolean inField(int index) {
    return min <= index && index <= max;
  }

  @Override
  public String toString() {
    return String.format("<%d..%d>", min, max);
  }

  @Override
  public int hashCode() {
    return 31 * min + max;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final Field other = (Field) obj;
    return min == other.min && max == other.max;
  }

  @Override
  public int compareTo(Field other) {
    checkNotNull(other);

    final int deltaMin = this.min - other.min;
    if (deltaMin != 0) {
      return deltaMin;
    }

    final int deltaMax = this.max - other.max;
    if (deltaMax != 0) {
      return deltaMax;
    }

    return 0;
  }
}
