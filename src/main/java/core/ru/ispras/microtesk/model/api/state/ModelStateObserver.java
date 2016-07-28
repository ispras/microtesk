/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.state;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.UndeclaredException;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;

public final class ModelStateObserver {
  private final static String ALREADY_ADDED_ERR_FRMT =
      "The %s item has already been added to the table.";

  private final static String UNDEFINED_ERR_FRMT =
      "The %s resource is not defined in the current model.";

  private final static String BOUNDS_ERR_FRMT =
      "The %d index is invalid for the %s resource.";

  private final Map<String, Memory> memoryMap;
  private final Map<String, Label> labelMap;

  public ModelStateObserver(
      final Memory[] registers,
      final Memory[] memory,
      final Label[] labels) {
    checkNotNull(registers);
    checkNotNull(memory);
    checkNotNull(labels);

    memoryMap = new HashMap<String, Memory>();
    addToMemoryMap(memoryMap, registers);
    addToMemoryMap(memoryMap, memory);

    labelMap = new HashMap<String, Label>();
    addToLabelMap(labelMap, labels);
  }

  private static void addToMemoryMap(final Map<String, Memory> map, final Memory[] items) {
    for (final Memory m : items) {
      final Memory prev = map.put(m.getName(), m);
      if (null != prev) {
        throw new IllegalStateException(String.format(ALREADY_ADDED_ERR_FRMT, m.getName()));
      }
    }
  }

  private static void addToLabelMap(final Map<String, Label> map, final Label[] items) {
    for (final Label l : items) {
      final Label prev = map.put(l.getName(), l);
      if (null != prev) {
        throw new IllegalStateException(String.format(ALREADY_ADDED_ERR_FRMT, l.getName()));
      }
    }
  }

  public LocationAccessor accessLocation(final String name) throws ConfigurationException {
    return accessLocation(name, BigInteger.ZERO);
  }

  public LocationAccessor accessLocation(
      final String name, final BigInteger index) throws ConfigurationException {
    if (labelMap.containsKey(name)) {
      if (null != index && !index.equals(BigInteger.ZERO)) {
        throw new UndeclaredException(String.format(BOUNDS_ERR_FRMT, index, name));
      }

      return labelMap.get(name).access();
    }

    if (!memoryMap.containsKey(name)) {
      throw new UndeclaredException(String.format(UNDEFINED_ERR_FRMT, name));
    }

    final Memory current = memoryMap.get(name);
    return current.access(index);
  }

  public void resetState() {
    for (final Memory memory : memoryMap.values()) {
      memory.reset();
    }
  }

  public void setUseTempState(final boolean value) {
    for (final Memory memory : memoryMap.values()) {
      memory.setUseTempCopy(value);
    }
  }
}
