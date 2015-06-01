/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.metadata;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The MetaModel class stores information on the model and provides methods to access it. The
 * information includes the list of instructions, the list of memory resources (registers, memory)
 * and the list of test situations (behavioral properties of the instructions).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MetaModel {
  private final Map<String, MetaAddressingMode> modes;
  private final Map<String, MetaOperation> operations;
  private final Map<String, MetaLocationStore> registers;
  private final Map<String, MetaLocationStore> memory;

  public MetaModel(
      final Collection<MetaAddressingMode> modes,
      final Collection<MetaOperation> operations,
      final Collection<MetaLocationStore> registers,
      final Collection<MetaLocationStore> memory) {
    this.modes = toMap(modes);
    this.operations = toMap(operations);
    this.registers = toMap(registers);
    this.memory = toMap(memory);
  }

  private static <T extends MetaData> Map<String, T> toMap(final Collection<T> c) {
    final Map<String, T> map = new LinkedHashMap<>();

    for (final T t : c) {
      map.put(t.getName(), t);
    }

    return map;
  }

  /**
   * Returns an iterator for the collection of addressing modes (excluding modes defined as OR
   * rules).
   * 
   * @return An Iterable object.
   */

  public Iterable<MetaAddressingMode> getAddressingModes() {
    return modes.values();
  }

  /**
   * Returns metadata for the specified addressing mode.
   * 
   * @param name Addressing mode name.
   * @return Addressing mode metadata.
   */

  public MetaAddressingMode getAddressingMode(final String name) {
    return modes.get(name);
  }

  /**
   * Returns an iterator for the collection of operations (excluding operations defined as OR
   * rules).
   * 
   * @return An Iterable object.
   */

  public Iterable<MetaOperation> getOperations() {
    return operations.values();
  }

  /**
   * Returns metadata for the specified operation.
   * 
   * @param name Operation name.
   * @return Operation metadata.
   */

  public MetaOperation getOperation(final String name) {
    return operations.get(name);
  }

  /**
   * Returns an iterator for the collection of registers.
   * 
   * @return An Iterable object.
   */

  public Iterable<MetaLocationStore> getRegisters() {
    return registers.values();
  }

  /**
   * Returns metadata for the specified register file.
   * 
   * @param name Register file name.
   * @return Register file metadata.
   */

  public MetaLocationStore getRegister(final String name) {
    return registers.get(name);
  }

  /**
   * Returns an iterator for the collection of memory store locations.
   * 
   * @return An Iterable object.
   */

  public Iterable<MetaLocationStore> getMemoryStores() {
    return memory.values();
  }

  /**
   * Returns metadata for the specified memory store location.
   * 
   * @param name Memory store location name.
   * @return Memory store location metadata.
   */

  public MetaLocationStore getMemoryStore(final String name) {
    return memory.get(name);
  }
}
