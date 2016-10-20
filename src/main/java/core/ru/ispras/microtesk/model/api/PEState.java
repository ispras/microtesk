/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.model.api.memory.MemoryDevice;
import ru.ispras.microtesk.model.api.state.LocationAccessor;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;

public abstract class PEState implements ModelStateObserver {
  private final Map<String, Memory> storageMap = new HashMap<>();
  private final Map<String, Label> labelMap = new HashMap<>();
  private final List<Memory> variables = new ArrayList<>();

  protected final void addStorage(final Memory storage) {
    InvariantChecks.checkNotNull(storage);
    this.storageMap.put(storage.getName(), storage);

    if (Memory.Kind.VAR == storage.getKind()) {
      variables.add(storage);
    }
  }

  protected final void addLabel(final Label label) {
    InvariantChecks.checkNotNull(label);
    this.labelMap.put(label.getName(), label);
  }

  public final LocationAccessor accessLocation(final String storageId) {
    return accessLocation(storageId, BigInteger.ZERO);
  }

  public final LocationAccessor accessLocation(
      final String storageId,
      final BigInteger index) {
    InvariantChecks.checkNotNull(storageId);

    final Label label = labelMap.get(storageId);
    if (null != label) {
      if (null != index && !index.equals(BigInteger.ZERO)) {
        throw new IllegalArgumentException(
            String.format("The %d index is invalid for the %s storage.", index, storageId));
      }

      return label.access();
    }

    final Memory storage = getStorage(storageId);
    return storage.access(index);
  }

  @Override
  public final void resetState() {
    for (final Memory memory : storageMap.values()) {
      memory.reset();
    }
  }

  @Override
  public final void setUseTempState(boolean value) {
    for (final Memory memory : storageMap.values()) {
      memory.setUseTempCopy(value);
    }
  }

  public final void resetVariables() {
    for (final Memory variable : variables) {
      variable.reset();
    }
  }

  public final MemoryAllocator newMemoryAllocator(
      final String storageId,
      final int addressableUnitBitSize,
      final BigInteger baseAddress) {
    final Memory storage = getStorage(storageId);
    return storage.newAllocator(addressableUnitBitSize, baseAddress);
  }

  public final MemoryDevice setMemoryHandler(
      final String storageId,
      final MemoryDevice handler) {
    final Memory storage = getStorage(storageId);
    return storage.setHandler(handler);
  }

  private Memory getStorage(final String storageId) {
    final Memory storage = storageMap.get(storageId);
    if (null == storage) {
      throw new IllegalArgumentException(
          String.format("The %s storage is not defined in the model.", storageId));
    }
    return storage;
  }
}
