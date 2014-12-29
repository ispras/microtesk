/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterOrEqZero;
import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;

import java.util.HashMap;
import java.util.Map;

public final class MemoryMap {
  private static class Pointer {
    final int address;
    final int sizeInAddresableUnits;

    Pointer(int address, int sizeInAddresableUnits) {
      this.address = address;
      this.sizeInAddresableUnits = sizeInAddresableUnits;
    }
  }

  private final Map<String, Pointer> labels;

  MemoryMap() {
    this.labels = new HashMap<String, Pointer>(); 
  }

  public void addLabel(String label, int address, int sizeInAddresableUnits) {
    checkNotNull(label);
    checkGreaterOrEqZero(address);
    checkGreaterThanZero(sizeInAddresableUnits);

    labels.put(label, new Pointer(address, sizeInAddresableUnits));
  }

  public int resolve(String label) {
    return resolve(label, 0);
  }

  public int resolve(String label, int index) {
    checkNotNull(label);
    checkGreaterOrEqZero(index);

    final Pointer pointer = labels.get(label);
    if (null == pointer) {
      throw new IllegalArgumentException(String.format("The %s label is not defined.", label));
    }

    return pointer.address + pointer.sizeInAddresableUnits * index;
  }
}
