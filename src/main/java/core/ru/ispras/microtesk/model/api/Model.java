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
import ru.ispras.microtesk.model.api.exception.UnsupportedTypeException;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.instruction.PrimitiveBuilder;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;

/**
 * The {@link Model} class implements an ISA model and provides its facilities to external users.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Model implements IModel, CallFactory {
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

  public String getName() {
    return name;
  }

  public MetaModel getMetaData() {
    return metaData;
  }

  public PEState getPE() {
    return peState;
  }

  public PrimitiveBuilder<AddressingMode> newMode(
      final String name) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final AddressingMode.IInfo modeInfo = modes.get(name);
    if (null == modeInfo) {
      throw new UnsupportedTypeException(
          String.format("The %s addressing mode is not defined.", name));
    }

    return modeInfo.createBuilder();
  }

  public PrimitiveBuilder<Operation> newOp(
      final String name, final String contextName) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final Operation.IInfo opInfo = ops.get(name);
    if (null == opInfo) {
      throw new UnsupportedTypeException(String.format("The %s operation is not defined.", name));
    }

    PrimitiveBuilder<Operation> result = opInfo.createBuilderForShortcut(contextName);
    if (null == result) {
      result = opInfo.createBuilder();
    }

    return result;
  }

  public InstructionCall newCall(final Operation op) {
    InvariantChecks.checkNotNull(op);
    return new InstructionCall(peState, op);
  }

  @Override
  public ModelStateObserver getStateObserver() {
    return getPE();
  }

  @Override
  public CallFactory getCallFactory() {
    return this;
  }
}
