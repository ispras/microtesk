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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.UnsupportedTypeException;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.AddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.OperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaModelBuilder;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;
import ru.ispras.microtesk.model.api.state.Resetter;

/**
 * The {@link ProcessorModel} class is base class for all families of microprocessor model classes.
 * It implements all methods of the interfaces that provide services to external users. The
 * responsibility to initialize member data (to create corresponding objects) is delegated to
 * descendant classes.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class ProcessorModel implements IModel, CallFactory {
  private final String name;

  private final Map<String, AddressingMode.IInfo> modes;
  private final Map<String, Operation.IInfo> ops;

  private final ModelStateObserver observer;
  private final Resetter resetter;
  private final MetaModel metaModel;

  public ProcessorModel(
      final String name,
      final MetaModelBuilder metaModelBuilder,
      final AddressingMode.IInfo[] modes,
      final Operation.IInfo[] ops,
      final Memory[] registers,
      final Memory[] memory,
      final Memory[] variables,
      final Label[] labels) {
    this.name = name;

    this.modes = new HashMap<>(modes.length);
    for (final AddressingMode.IInfo i : modes) {
      this.modes.put(i.getName(), i);
    }

    this.ops = new HashMap<>(ops.length);
    for (final Operation.IInfo i : ops) {
      this.ops.put(i.getName(), i);
    }

    this.observer = new ModelStateObserver(registers, memory, labels);
    this.resetter = new Resetter(variables);
    this.metaModel = metaModelBuilder.build();
  }

  public final String getName() {
    return name;
  }

  // IModel
  @Override
  public final MetaModel getMetaData() {
    return metaModel;
  }

  // IModel
  @Override
  public final ModelStateObserver getStateObserver() {
    return observer;
  }

  // IModel
  @Override
  public final CallFactory getCallFactory() {
    return this;
  }

  // CallFactory
  @Override
  public final AddressingModeBuilder newMode(final String name) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final AddressingMode.IInfo modeInfo = modes.get(name);
    if (null == modeInfo) {
      throw new UnsupportedTypeException(
          String.format("The %s addressing mode is not defined.", name));
    }

    return modeInfo.createBuilder();
  }

  // CallFactory
  @Override
  public final OperationBuilder newOp(final String name, final String contextName)
      throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final Operation.IInfo opInfo = ops.get(name);
    if (null == opInfo) {
      throw new UnsupportedTypeException(String.format("The %s operation is not defined.", name));
    }

    OperationBuilder result = opInfo.createBuilderForShortcut(contextName);
    if (null == result) {
      result = opInfo.createBuilder();
    }

    return result;
  }

  // CallFactory
  @Override
  public InstructionCall newCall(final Operation op) {
    InvariantChecks.checkNotNull(op);
    return new InstructionCall(resetter, op);
  }
}
