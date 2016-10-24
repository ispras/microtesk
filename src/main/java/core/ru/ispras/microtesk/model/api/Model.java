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

package ru.ispras.microtesk.model.api;

import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.instruction.IsaPrimitiveBuilder;
import ru.ispras.microtesk.model.api.metadata.MetaModel;

/**
 * The {@link Model} class implements an ISA model and provides its facilities to external users.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Model {
  private final String name;
  private final MetaModel metaData;
  private final PEState peState;
  private final Map<String, AddressingMode.IInfo> modes;
  private final Map<String, Operation.IInfo> ops;

  protected Model(
      final String name,
      final MetaModel metaData,
      final PEState peState,
      final Map<String, AddressingMode.IInfo> modes,
      final Map<String, Operation.IInfo> ops) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaData);
    InvariantChecks.checkNotNull(peState);

    this.name = name;
    this.metaData = metaData;
    this.peState = peState;
    this.modes = modes;
    this.ops = ops;
  }

  /**
   * Returns the name of the modeled microprocessor design.
   * 
   * @return name Microprocessor design name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns a meta description of the model.
   * 
   * @return An meta data object (provides access to model's meta data).
   */
  public MetaModel getMetaData() {
    return metaData;
  }

  public PEState getPE() {
    return peState;
  }

  public IsaPrimitiveBuilder<AddressingMode> newMode(
      final String name) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final AddressingMode.IInfo modeInfo = modes.get(name);
    if (null == modeInfo) {
      throw new ConfigurationException(
          String.format("The %s addressing mode is not defined.", name));
    }

    return modeInfo.createBuilder();
  }

  public IsaPrimitiveBuilder<Operation> newOp(
      final String name, final String contextName) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final Operation.IInfo opInfo = ops.get(name);
    if (null == opInfo) {
      throw new ConfigurationException(String.format("The %s operation is not defined.", name));
    }

    IsaPrimitiveBuilder<Operation> result = opInfo.createBuilderForShortcut(contextName);
    if (null == result) {
      result = opInfo.createBuilder();
    }

    return result;
  }

  public InstructionCall newCall(final Operation op) {
    InvariantChecks.checkNotNull(op);
    return new InstructionCall(peState, op);
  }

  /**
   * Returns a model state monitor object that allows getting information on the current state of
   * the microprocessor mode (current register values, value in memory locations, etc)
   */
  public ModelStateObserver getStateObserver() {
    return getPE();
  }
}
