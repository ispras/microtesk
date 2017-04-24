/*
 * Copyright 2012-2017 ISP RAS (http://www.ispras.ru)
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.decoder.Decoder;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.model.api.memory.MemoryDevice;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;

/**
 * The {@link Model} class implements an ISA model and provides its facilities to external users.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Model implements ModelStateManager {
  private final String name;
  private final MetaModel metaData;
  private final Decoder decoder;

  private final ProcessingElement.Factory procElemFactory;
  private final TemporaryVariables tempVars;

  private final Map<String, IsaPrimitiveInfoAnd> modes;
  private final Map<String, IsaPrimitiveInfoAnd> ops;

  private List<ProcessingElement> procElems;
  private int activeProcElemIndex;
  private ProcessingElement activeProcElem;
  private ProcessingElement activeProcElemTemp;

  private final MemoryDevice memoryCallback;
  private Pair<String, MemoryDevice> memoryHandler;
  private final List<ModelStateManager> stateManagers;

  protected Model(
      final String name,
      final MetaModel metaData,
      final Decoder decoder,
      final ProcessingElement.Factory procElemFactory,
      final TemporaryVariables.Factory tempVarFactory,
      final Map<String, IsaPrimitiveInfoAnd> modes,
      final Map<String, IsaPrimitiveInfoAnd> ops) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaData);
    InvariantChecks.checkNotNull(decoder);

    InvariantChecks.checkNotNull(procElemFactory);
    InvariantChecks.checkNotNull(tempVarFactory);

    this.name = name;
    this.metaData = metaData;
    this.decoder = decoder;
    this.procElemFactory = procElemFactory;
    this.tempVars = tempVarFactory.create();
    this.modes = modes;
    this.ops = ops;

    this.procElems = Collections.emptyList();
    this.activeProcElemIndex = -1;
    this.activeProcElem = null;
    this.activeProcElemTemp = null;

    this.memoryCallback = new MemoryCallback();
    this.memoryHandler = null;

    this.stateManagers = new ArrayList<>();
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

  /**
   * Returns decoder to recover instruction calls from binary data.
   *  
   * @return Decoder.
   */
  public Decoder getDecoder() {
    return decoder;
  }

  public TemporaryVariables getTempVars() {
    return tempVars;
  }

  public ProcessingElement getPE() {
    InvariantChecks.checkNotNull(activeProcElem, "No active processing element is set.");
    return null != activeProcElemTemp ? activeProcElemTemp : activeProcElem;
  }

  public void setPENumber(final int number) {
    InvariantChecks.checkGreaterThanZero(number);
    procElems = ProcessingElement.newInstances(procElemFactory, number);
    initializePEs();
  }

  public int getPENumber() {
    return procElems.size();
  }

  public void setActivePE(final int index) {
    InvariantChecks.checkBounds(0, getPENumber());
    activeProcElemIndex = index;
    activeProcElem = procElems.get(index);
    activeProcElemTemp = null;
  }

  public int getActivePE() {
    return activeProcElemIndex;
  }

  @Override
  public void setUseTempState(final boolean value) {
    for(final ModelStateManager stateManager : stateManagers) {
      stateManager.setUseTempState(value);
    }

    if (value) {
      InvariantChecks.checkNotNull(activeProcElem);
      InvariantChecks.checkTrue(null == activeProcElemTemp);
      activeProcElemTemp = activeProcElem.copy(false);
      if (null != memoryHandler) { 
        activeProcElemTemp.setMemoryHandler(memoryHandler.first, memoryHandler.second);
      }
    } else {
      activeProcElemTemp = null;
    }
  }

  @Override
  public void resetState() {
    for(final ModelStateManager stateManager : stateManagers) {
      stateManager.resetState();
    }

    for (final ProcessingElement procElem : procElems) {
      procElem.resetState();
    }

    activeProcElemTemp = null;
  }

  public final MemoryDevice setMemoryHandler(final String id, final MemoryDevice handler) {
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(handler);

    for (final ProcessingElement procElem : procElems) {
      procElem.setMemoryHandler(id, handler);
    }

    memoryHandler = new Pair<>(id, handler);
    return memoryCallback;
  }

  public void addStateManager(final ModelStateManager stateManager) {
    InvariantChecks.checkNotNull(stateManager);
    stateManagers.add(stateManager);
  }

  public void initMemoryAllocator(
      final String storageId,
      final int addressableUnitBitSize,
      final BigInteger basePhysicalAddress) throws ConfigurationException {
    for (final ProcessingElement  procElem : procElems) {
      procElem.initMemoryAllocator(storageId, addressableUnitBitSize, basePhysicalAddress);
    }
  }

  public MemoryAllocator getMemoryAllocator() {
    return getPE().getMemoryAllocator();
  }

  public IsaPrimitiveBuilder newMode(
      final String name) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final IsaPrimitiveInfoAnd modeInfo = modes.get(name);
    if (null == modeInfo) {
      throw new ConfigurationException(
          String.format("The %s addressing mode is not defined.", name));
    }

    return modeInfo.createBuilder();
  }

  public IsaPrimitiveBuilder newOp(
      final String name, final String contextName) throws ConfigurationException {
    InvariantChecks.checkNotNull(name);

    final IsaPrimitiveInfoAnd opInfo = ops.get(name);
    if (null == opInfo) {
      throw new ConfigurationException(String.format("The %s operation is not defined.", name));
    }

    IsaPrimitiveBuilder result = opInfo.createBuilderForShortcut(contextName);
    if (null == result) {
      result = opInfo.createBuilder();
    }

    return result;
  }

  public InstructionCall newCall(final IsaPrimitive op) {
    InvariantChecks.checkNotNull(op);
    return new InstructionCall(tempVars, op);
  }

  private void initializePEs() {
    Logger.debugHeader("Initializing Processing Elements");

    final String instantiate = "instantiate";
    final MetaOperation metaData = getMetaData().getOperation(instantiate);

    if (null == metaData) { // instantiate is undefined
      Logger.debug("The \"instantiate\" operation is undefined.");
      return;
    }

    for (int index = 0; index < procElems.size(); ++index) {
      try {
        final IsaPrimitiveBuilder builder = newOp(instantiate, null);
        int argumentIndex = 0;
        for (final MetaArgument argument : metaData.getArguments()) {
          if (argumentIndex > 0) {
            Logger.warning("Operation '%s' has more than 1 argument.", instantiate);
          }

          builder.setArgument(argument.getName(), BigInteger.valueOf(index));
          argumentIndex++;
        }
        final IsaPrimitive primitive = builder.build();
        primitive.execute(procElems.get(index), tempVars);
      } catch(final ConfigurationException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private final class MemoryCallback implements MemoryDevice {
    private MemoryDevice getMemory() {
      return getPE().getMemory();
    }

    @Override
    public int getAddressBitSize() {
      return getMemory().getAddressBitSize();
    }

    @Override
    public int getDataBitSize() {
      return getMemory().getDataBitSize();
    }

    @Override
    public BitVector load(final BitVector address) {
      return getMemory().load(address);
    }

    @Override
    public void store(final BitVector address, final BitVector data) {
      getMemory().store(address, data);
    }

    @Override
    public void store(final BitVector address, final int offset, final BitVector data) {
      getMemory().store(address, offset, data);
    }

    @Override
    public boolean isInitialized(final BitVector address) {
      return getMemory().isInitialized(address);
    }
  }
}
