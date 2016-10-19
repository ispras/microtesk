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

import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
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
public abstract class ProcessorModel implements IModel {
  private final String name;
  private final CallFactory callFactory;
  private final ModelStateObserver observer;
  private final MetaModel metaModel;

  public ProcessorModel(
      final String name,
      final MetaModel metaModel,
      final AddressingMode.IInfo[] modes,
      final Operation.IInfo[] ops,
      final Memory[] registers,
      final Memory[] memory,
      final Memory[] variables,
      final Label[] labels) {
    this.name = name;

    final Map<String, AddressingMode.IInfo> modeMap = new HashMap<>(modes.length);
    for (final AddressingMode.IInfo mode : modes) {
      modeMap.put(mode.getName(), mode);
    }

    final Map<String, Operation.IInfo> opMap = new HashMap<>(ops.length);
    for (final Operation.IInfo op : ops) {
      opMap.put(op.getName(), op);
    }

    this.callFactory = new CallFactory(new Resetter(variables), modeMap, opMap);
    this.observer = new ModelStateObserver(registers, memory, labels);
    this.metaModel = metaModel;
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
    return callFactory;
  }

  protected abstract Core newCore();
}
