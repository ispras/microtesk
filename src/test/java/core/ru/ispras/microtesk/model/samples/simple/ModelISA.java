/*
 * Copyright (c) 2012-2015 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelISA.java, Dec 1, 2012 11:46:09 PM Andrei Tatarnikov
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;

abstract class ModelISA extends CallSimulator {
  public ModelISA(IModel model) {
    super(model);
  }

  public final void mov(AddressingMode op1, AddressingMode op2) throws ConfigurationException {
    final Map<String, AddressingMode> args = new LinkedHashMap<>();

    args.put("op1", op1);
    args.put("op2", op2);

    addCall(newOp("Mov", "#root", args));
  }

  public final void add(AddressingMode op1, AddressingMode op2) throws ConfigurationException {
    final Map<String, AddressingMode> args = new LinkedHashMap<>();

    args.put("op1", op1);
    args.put("op2", op2);

    addCall(newOp("Add", "#root", args));
  }

  public final void sub(AddressingMode op1, AddressingMode op2) throws ConfigurationException {
    final Map<String, AddressingMode> args = new LinkedHashMap<>();

    args.put("op1", op1);
    args.put("op2", op2);

    addCall(newOp("Sub", "#root", args));
  }

  public final AddressingMode reg(int i) throws ConfigurationException {
    return newMode("REG", Collections.singletonMap("i", BigInteger.valueOf(i)));
  }

  public final AddressingMode ireg(int i) throws ConfigurationException {
    return newMode("IREG", Collections.singletonMap("i", BigInteger.valueOf(i)));
  }

  public final AddressingMode mem(int i) throws ConfigurationException {
    return newMode("MEM", Collections.singletonMap("i", BigInteger.valueOf(i)));
  }

  public final AddressingMode imm(int i) throws ConfigurationException {
    return newMode("IMM", Collections.singletonMap("i", BigInteger.valueOf(i)));
  }
}
