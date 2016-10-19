/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.metadata.MetaModel;

public class ModelBuilder {
  private final String name;
  private final MetaModel metaModel;
  private final PEState peState;
  private final Map<String, AddressingMode.IInfo> modes;
  private final Map<String, Operation.IInfo> ops;

  protected ModelBuilder(
      final String name,
      final MetaModel metaModel,
      final PEState peState) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(peState);

    this.name = name;
    this.metaModel = metaModel;
    this.peState = peState;
    this.modes = new HashMap<>();
    this.ops = new HashMap<>();
  }

  public final void addMode(final AddressingMode.IInfo mode) {
    InvariantChecks.checkNotNull(mode);
    modes.put(mode.getName(), mode);
  }

  public final void addOperation(final Operation.IInfo op) {
    InvariantChecks.checkNotNull(op);
    ops.put(op.getName(), op);
  }

  public final Model build() {
    return new Model(name, metaModel, peState, modes, ops);
  }
}
