/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.metadata;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.Type;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MetaModelBuilder {
  private final Map<String, MetaLocationStore> registers;
  private final Map<String, MetaLocationStore> memory;
  private final Map<String, MetaAddressingMode> modes;
  private final Map<String, MetaGroup> modeGroups;
  private final Map<String, MetaOperation> operations;
  private final Map<String, MetaGroup> operationGroups;

  public MetaModelBuilder() {
    this.registers = new LinkedHashMap<>();
    this.memory = new LinkedHashMap<>();
    this.modes = new LinkedHashMap<>();
    this.modeGroups = new LinkedHashMap<>();
    this.operations = new LinkedHashMap<>();
    this.operationGroups = new LinkedHashMap<>();
  }

  public void addRegister(
      final String name,
      final Type dataType,
      final BigInteger count) {
    registers.put(name, new MetaLocationStore(name, dataType, count));
  }

  public void addRegister(
      final String name,
      final Type dataType,
      final long count) {
    addRegister(name, dataType, BigInteger.valueOf(count));
  }

  public void addMemory(
      final String name,
      final Type dataType,
      final BigInteger count) {
    memory.put(name, new MetaLocationStore(name, dataType, count));
  }

  public void addMemory(
      final String name,
      final Type dataType,
      final long count) {
    addMemory(name, dataType, BigInteger.valueOf(count));
  }

  public void addMode(final MetaAddressingMode mode) {
    InvariantChecks.checkNotNull(mode);
    modes.put(mode.getName(), mode);
  }

  public void addMode(final MetaGroup group) {
    InvariantChecks.checkNotNull(group);
    InvariantChecks.checkTrue(group.getKind() == MetaGroup.Kind.MODE);
    modeGroups.put(group.getName(), group);
  }

  public void addOperation(final MetaOperation operation) {
    InvariantChecks.checkNotNull(operation);
    operations.put(operation.getName(), operation);
  }

  public void addOperation(final MetaGroup group) {
    InvariantChecks.checkNotNull(group);
    InvariantChecks.checkTrue(group.getKind() == MetaGroup.Kind.OP);
    operationGroups.put(group.getName(), group);
  }

  public MetaModel build() {
    return new MetaModel(
        modes,
        modeGroups,
        operations,
        operationGroups,
        registers,
        memory
        );
  }
}
