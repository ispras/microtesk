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
import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;

import java.util.HashMap;
import java.util.Map;

public final class MemoryMap {
  private final Map<String, Integer> labels;

  MemoryMap() {
    this.labels = new HashMap<String, Integer>(); 
  }

  public void addLabel(String label, int address) {
    checkNotNull(label);
    checkGreaterOrEqZero(address);

    labels.put(label, address);
  }

  public int resolve(String label) {
    checkNotNull(label);

    if (!labels.containsKey(label)) {
      throw new IllegalArgumentException(String.format("The %s label is not defined.", label));
    }

    return labels.get(label);
  }

  public int resolveWithDefault(String label, int defaultValue) {
    checkNotNull(label);

    if (!labels.containsKey(label)) {
      return defaultValue;
    }

    return labels.get(label);
  }
}
