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

package ru.ispras.microtesk.model;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.decoder.Decoder;
import ru.ispras.microtesk.model.metadata.MetaModel;

import java.util.HashMap;
import java.util.Map;

public class ModelBuilder {
  private final String name;
  private final MetaModel metaModel;
  private final Decoder decoder;

  private final ProcessingElement.Factory procElemFactory;
  private final TemporaryVariables.Factory tempVarFactory;

  private final Map<String, IsaPrimitiveInfoAnd> modes;
  private final Map<String, IsaPrimitiveInfoAnd> ops;

  protected ModelBuilder(
      final String name,
      final MetaModel metaModel,
      final Decoder decoder,
      final ProcessingElement.Factory procElemFactory,
      final TemporaryVariables.Factory tempVarFactory) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaModel);

    InvariantChecks.checkNotNull(procElemFactory);
    InvariantChecks.checkNotNull(tempVarFactory);

    this.name = name;
    this.metaModel = metaModel;
    this.decoder = decoder;
    this.procElemFactory = procElemFactory;
    this.tempVarFactory = tempVarFactory;
    this.modes = new HashMap<>();
    this.ops = new HashMap<>();
  }

  public final void addMode(final IsaPrimitiveInfoAnd mode) {
    InvariantChecks.checkNotNull(mode);
    modes.put(mode.getName(), mode);
  }

  public final void addOperation(final IsaPrimitiveInfoAnd op) {
    InvariantChecks.checkNotNull(op);
    ops.put(op.getName(), op);
  }

  public final Model build() {
    return new Model(
        name,
        metaModel,
        decoder,
        procElemFactory,
        tempVarFactory,
        modes,
        ops
        );
  }
}
