/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.UnsupportedTypeException;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.AddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.OperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;
import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;
import ru.ispras.microtesk.model.api.state.Resetter;
import ru.ispras.microtesk.model.api.state.Status;

/**
 * The ProcessorModel class is base class for all families of microprocessor model classes. It
 * implements all methods of the interfaces that provide services to external users. The
 * responsibility to initialize member data (to create corresponding objects) is delegated to
 * descendant classes.
 * 
 * @author Andrei Tatarnikov
 */

public abstract class ProcessorModel implements IModel, ICallFactory {
  private final String name;

  private final AddressingModeStore modes;
  private final OperationStore ops;

  private final IModelStateObserver observer;
  private final Resetter resetter;
  private final MetaModel metaModel;

  public ProcessorModel(
      final String name,
      final IAddressingMode.IInfo[] modes,
      final IAddressingMode.IInfo[] modeGroups,
      final IOperation.IInfo[] ops,
      final IOperation.IInfo[] opGroups,
      final Memory[] registers,
      final Memory[] memory,
      final Label[] labels,
      final Status[] statuses,
      final Resetter resetter) {

    this.name = name;

    this.modes = new AddressingModeStore(modes);
    this.ops = new OperationStore(ops);

    this.observer = new ModelStateObserver(registers, memory, labels, statuses);
    this.resetter = resetter;

    final List<MetaGroup> modeGroupList = modeGroupsToList(modeGroups);
    final List<MetaGroup> opGroupList = opGroupsToList(opGroups);

    this.metaModel = new MetaModel(
      this.modes.getMetaData(),
      modeGroupList,
      this.ops.getMetaData(),
      opGroupList,
      new MemoryStore(registers).getMetaData(),
      new MemoryStore(memory).getMetaData()
    );
  }

  private static List<MetaGroup> modeGroupsToList(final IAddressingMode.IInfo[] modeGroups) {
    final List<MetaGroup> result = new ArrayList<>();

    for (final IAddressingMode.IInfo i : modeGroups) {
      result.add(((AddressingMode.InfoOrRule) i).getMetaDataGroup());
    }

    return result;
  }
  
  private static List<MetaGroup> opGroupsToList(final IOperation.IInfo[] opGroups) {
    final List<MetaGroup> result = new ArrayList<>();

    for (final IOperation.IInfo i : opGroups) {
      result.add(((Operation.InfoOrRule) i).getMetaDataGroup());
    }

    return result;
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
  public final IModelStateObserver getStateObserver() {
    return observer;
  }

  // IModel
  @Override
  public final ICallFactory getCallFactory() {
    return this;
  }

  // ICallFactory
  @Override
  public final AddressingModeBuilder newMode(final String name) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final String ERROR_FORMAT = "The %s addressing mode is not defined.";

    final IAddressingMode.IInfo modeInfo = modes.getModeInfo(name);
    if (null == modeInfo) {
      throw new UnsupportedTypeException(String.format(ERROR_FORMAT, name));
    }

    final Map<String, AddressingModeBuilder> builders = modeInfo.createBuilders();
    final AddressingModeBuilder result = builders.get(name);

    if (null == result) {
      throw new UnsupportedTypeException(String.format(ERROR_FORMAT, name));
    }

    return result;
  }

  // ICallFactory
  @Override
  public final OperationBuilder newOp(final String name, final String contextName)
      throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final String ERROR_FORMAT = "The %s operation is not defined.";

    final IOperation.IInfo opInfo = ops.getOpInfo(name);
    if (null == opInfo) {
      throw new UnsupportedTypeException(String.format(ERROR_FORMAT, name));
    }

    Map<String, OperationBuilder> builders = null;

    builders = opInfo.createBuildersForShortcut(contextName);
    if (null == builders) {
      builders = opInfo.createBuilders();
    }

    final OperationBuilder result = builders.get(name);
    if (null == result) {
      throw new UnsupportedTypeException(String.format(ERROR_FORMAT, name));
    }

    return result;
  }

  // ICallFactory
  @Override
  public InstructionCall newCall(final IOperation op) {
    InvariantChecks.checkNotNull(op);
    return new InstructionCall(resetter, op);
  }

  private static final class MemoryStore {
    private final Collection<MetaLocationStore> metaData;

    public MemoryStore(final Memory[] memory) {
      this.metaData = new ArrayList<>(memory.length);
      for (final Memory m : memory) {
        this.metaData.add(new MetaLocationStore(m.getName(), m.getType(), m.getLength()));
      }
    }

    public Collection<MetaLocationStore> getMetaData() {
      return metaData;
    }
  }

  private static final class AddressingModeStore {
    private final Map<String, IAddressingMode.IInfo> items;
    private final Collection<MetaAddressingMode> metaData;

    public AddressingModeStore(final IAddressingMode.IInfo[] modes) {
      this.items = new HashMap<>(modes.length);
      this.metaData = new ArrayList<>(modes.length);

      for (IAddressingMode.IInfo i : modes) {
        items.put(i.getName(), i);
        this.metaData.addAll(i.getMetaData());
      }
    }

    public IAddressingMode.IInfo getModeInfo(final String name) {
      return items.get(name);
    }

    public Collection<MetaAddressingMode> getMetaData() {
      return metaData;
    }
  }

  private static final class OperationStore {
    private final Map<String, IOperation.IInfo> items;
    private final Collection<MetaOperation> metaData;

    public OperationStore(final IOperation.IInfo[] ops) {
      this.items = new HashMap<>(ops.length);
      this.metaData = new ArrayList<>(ops.length);

      for (IOperation.IInfo i : ops) {
        items.put(i.getName(), i);
        this.metaData.addAll(i.getMetaData());
      }
    }

    public IOperation.IInfo getOpInfo(final String name) {
      return items.get(name);
    }

    public Collection<MetaOperation> getMetaData() {
      return metaData;
    }
  }
}
