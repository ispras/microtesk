/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.samples.simple;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.AddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.instruction.OperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;

public abstract class CallSimulator {
  protected final void addCall(final Operation op) {
    final InstructionCall call = model.getCallFactory().newCall(op);
    calls.add(call);
  }

  protected final AddressingMode newMode(
      final String name,
      final Map<String, BigInteger> args) throws ConfigurationException {
    final AddressingModeBuilder modeBuilder = model.getCallFactory().newMode(name);
    for (final Map.Entry<String, BigInteger> arg : args.entrySet()) {
      modeBuilder.setArgument(arg.getKey(), arg.getValue());
    }

    return modeBuilder.build();
  }

  protected final Operation newOp(
      final String name,
      final String context,
      final Map<String, AddressingMode> args) throws ConfigurationException {
    final OperationBuilder opBuilder = model.getCallFactory().newOp(name, context);

    for (final Map.Entry<String, AddressingMode> arg : args.entrySet()) {
      opBuilder.setArgument(arg.getKey(), arg.getValue());
    }

    return opBuilder.build();
  }

  private final IModel model;
  private final List<InstructionCall> calls;

  protected CallSimulator(final IModel model) {
    InvariantChecks.checkNotNull(model);

    this.model = model;
    this.calls = new ArrayList<>();
  }

  public final void execute() {
    for (final InstructionCall call : calls) {
      call.execute();
    }
  }

  public final void print() {
    Logger.debugBar();
    for (final InstructionCall call : calls) {
      Logger.debug(call.getText());
    }
  }
}
