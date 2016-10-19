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

import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.instruction.PrimitiveBuilder;
import ru.ispras.microtesk.model.api.metadata.MetaModel;

public final class Model {
  private final String name;
  private final MetaModel metaModel;
  private final PEState peState;
  private final Map<String, AddressingMode.IInfo> modes;
  private final Map<String, Operation.IInfo> ops;

  protected Model(
      final String name,
      final MetaModel metaModel,
      final PEState peState,
      final Map<String, AddressingMode.IInfo> modes,
      final Map<String, Operation.IInfo> ops) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(peState);

    this.name = name;
    this.metaModel = metaModel;
    this.peState = peState;
    this.modes = modes;
    this.ops = ops;
  }

  public String getName() {
    return name;
  }

  public MetaModel getMetaModel() {
    return metaModel;
  }

  public PEState getPE() {
    return peState;
  }

  public PrimitiveBuilder<AddressingMode> newMode(final String name) {
    InvariantChecks.checkNotNull(name);

    final AddressingMode.IInfo modeInfo = modes.get(name);
    InvariantChecks.checkNotNull(modeInfo, name);

    return modeInfo.createBuilder();
  }

  public PrimitiveBuilder<Operation> newOp(final String name, final String contextName) {
    InvariantChecks.checkNotNull(name);

    final Operation.IInfo opInfo = ops.get(name);
    InvariantChecks.checkNotNull(opInfo, name);

    PrimitiveBuilder<Operation> result = opInfo.createBuilderForShortcut(contextName);
    if (null == result) {
      result = opInfo.createBuilder();
    }

    return result;
  }

  public InstructionCall newCall(final Operation op) {
    InvariantChecks.checkNotNull(op);
    return new InstructionCall(null, op);
  }
}
