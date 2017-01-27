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
import ru.ispras.microtesk.model.api.memory.LocationAccessor;
import ru.ispras.microtesk.model.api.memory.MemoryDeviceWrapper;

/**
 * The {@link ProcessingElement} class holds information on the state of a PE.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
  */
public abstract class ProcessingElement {
  public interface Factory {
    ProcessingElement create();
  }

  private final Map<String, Memory> storageMap;
  private final Map<String, Label> labelMap;
  private final Map<String, MemoryDevice> deviceMap;

  private MemoryDevice memory;
  private Memory memoryAllocatorStorage;
  private String memoryAllocatorStorageId;

  protected ProcessingElement() {
    this.storageMap = new HashMap<>();
    this.labelMap = new HashMap<>();
    this.deviceMap = new HashMap<>();

    this.memory = null;
    this.memoryAllocatorStorage = null;
    this.memoryAllocatorStorageId = null;
  }

  protected ProcessingElement(final ProcessingElement other) {
    this();
    InvariantChecks.checkNotNull(other);
    this.memoryAllocatorStorageId = other.memoryAllocatorStorageId;
  }

  protected final void addStorage(final Memory storage) {
    InvariantChecks.checkNotNull(storage);
    this.storageMap.put(storage.getName(), storage);
  }

  protected final void addLabel(final Label label) {
    InvariantChecks.checkNotNull(label);
    this.labelMap.put(label.getName(), label);
  }

  protected static List<ProcessingElement> newInstances(final Factory factory, final int number) {
    InvariantChecks.checkNotNull(factory);
    InvariantChecks.checkGreaterThanZero(number);

    final List<ProcessingElement> result = new ArrayList<>(number);

    final ProcessingElement processingElement = factory.create();
    result.add(processingElement);

    for (int index = 1; index < number; ++index) {
      result.add(processingElement.copy(true));
    }

    return result;
  }

  /**
   * Creates a new copy of PE state. Shared resources can be shared or cloned.
   * 
   * @param shared Specifies whether resources marked as shared must be shared or cloned.
   * @return New copy.
   */
  public abstract ProcessingElement copy(boolean shared);

  public final LocationAccessor accessLocation(
      final String storageId) throws ConfigurationException {
    return accessLocation(storageId, BigInteger.ZERO);
  }

  public final LocationAccessor accessLocation(
      final String storageId,
      final BigInteger index) throws ConfigurationException {
    InvariantChecks.checkNotNull(storageId);

    final Label label = labelMap.get(storageId);
    if (null != label) {
      if (null != index && !index.equals(BigInteger.ZERO)) {
        throw new ConfigurationException(
            String.format("The %d index is invalid for the %s storage.", index, storageId));
      }

      return label.access();
    }

    final Memory storage = getStorage(storageId);
    return storage.access(index);
  }

  protected final void resetState() {
    for (final Memory memory : storageMap.values()) {
      memory.reset();
    }
  }

  protected final void initMemoryAllocator(
      final String storageId,
      final int addressableUnitBitSize,
      final BigInteger baseAddress) throws ConfigurationException {
    memoryAllocatorStorage = getStorage(storageId);
    memoryAllocatorStorageId = storageId;
    memoryAllocatorStorage.initAllocator(addressableUnitBitSize, baseAddress);
  }

  protected MemoryAllocator getMemoryAllocator() {
    if (null == memoryAllocatorStorage && null != memoryAllocatorStorageId) {
      memoryAllocatorStorage = storageMap.get(memoryAllocatorStorageId);
    }

    InvariantChecks.checkNotNull(memoryAllocatorStorage, "Allocator is not initialized.");
    return memoryAllocatorStorage.getAllocator();
  }

  protected final void setMemoryHandler(
      final String storageId, final MemoryDevice handler) {
    try {
      final Memory storage = getStorage(storageId);
      memory = storage.setHandler(handler);
    } catch (final ConfigurationException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected MemoryDevice getMemory() {
    return memory;
  }

  public final MemoryDevice getMemoryDevice(final String deviceId) {
    InvariantChecks.checkNotNull(deviceId);

    MemoryDevice device = deviceMap.get(deviceId);
    if (null == device) {
      try {
        final Memory storage = getStorage(deviceId);
        device = new MemoryDeviceWrapper(storage);
        deviceMap.put(deviceId, device);
      } catch (final ConfigurationException e) {
        throw new IllegalArgumentException(e);
      }
    }

    return device;
  }

  private Memory getStorage(final String storageId) throws ConfigurationException {
    final Memory storage = storageMap.get(storageId);
    if (null == storage) {
      throw new ConfigurationException(
          String.format("The %s storage is not defined in the model.", storageId));
    }
    return storage;
  }
}
