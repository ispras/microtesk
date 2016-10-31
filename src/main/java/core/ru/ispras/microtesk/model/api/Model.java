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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;

/**
 * The {@link Model} class implements an ISA model and provides its facilities to external users.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Model {
  private final String name;
  private final MetaModel metaData;

  private final ProcessingElement.Factory procElemFactory;
  private final TemporaryVariables tempVars;

  private final Map<String, IsaPrimitiveInfoAnd> modes;
  private final Map<String, IsaPrimitiveInfoAnd> ops;

  private List<ProcessingElement> procElems;
  private ProcessingElement activeProcElem;
  private ProcessingElement activeProcElemTemp;

  protected Model(
      final String name,
      final MetaModel metaData,
      final ProcessingElement.Factory procElemFactory,
      final TemporaryVariables.Factory tempVarFactory,
      final Map<String, IsaPrimitiveInfoAnd> modes,
      final Map<String, IsaPrimitiveInfoAnd> ops) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaData);

    InvariantChecks.checkNotNull(procElemFactory);
    InvariantChecks.checkNotNull(tempVarFactory);

    this.name = name;
    this.metaData = metaData;
    this.procElemFactory = procElemFactory;
    this.tempVars = tempVarFactory.create();
    this.modes = modes;
    this.ops = ops;

    this.procElems = Collections.emptyList();
    this.activeProcElem = null;
    this.activeProcElemTemp = null;
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

  public ProcessingElement getPE() {
    InvariantChecks.checkNotNull(activeProcElem);
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
    activeProcElem = procElems.get(index);
    activeProcElemTemp = null;
  }

  public void setUseTempState(final boolean useTempState) {
    if (useTempState) {
      InvariantChecks.checkNotNull(activeProcElem);
      InvariantChecks.checkTrue(null == activeProcElemTemp);
      activeProcElemTemp = activeProcElem.copy(false);
    } else {
      activeProcElemTemp = null;
    }
  }

  public TemporaryVariables getTempVars() {
    return tempVars;
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
}
