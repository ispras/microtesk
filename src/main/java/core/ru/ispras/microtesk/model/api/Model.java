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
import ru.ispras.microtesk.model.api.metadata.MetaModel;

/**
 * The {@link Model} class implements an ISA model and provides its facilities to external users.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Model {
  private final String name;
  private final MetaModel metaData;

  private final ProcessingElement.Factory procElemFactory;
  private final TemporaryVariables.Factory tempVarFactory;

  private final Map<String, IsaPrimitiveInfoAnd> modes;
  private final Map<String, IsaPrimitiveInfoAnd> ops;

  private final ProcessingElement processingElement;

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
    this.tempVarFactory = tempVarFactory;
    this.modes = modes;
    this.ops = ops;
    this.processingElement = procElemFactory.create();
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
    return processingElement;
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
    return new InstructionCall(
        processingElement, tempVarFactory, op);
  }
}
