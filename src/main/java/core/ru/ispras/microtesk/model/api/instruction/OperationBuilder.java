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

package ru.ispras.microtesk.model.api.instruction;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.ReassignmentException;
import ru.ispras.microtesk.model.api.exception.UndeclaredException;
import ru.ispras.microtesk.model.api.exception.UninitializedException;
import ru.ispras.microtesk.model.api.memory.Location;

public final class OperationBuilder implements IOperationBuilder {
  private final String opName;
  private final IOperation.IFactory factory;
  private final Map<String, Operation.Param> decls;
  private final Map<String, Object> args;

  public OperationBuilder(String opName, IOperation.IFactory factory, Operation.ParamDecls decls) {
    this.opName = opName;
    this.factory = factory;
    this.decls = decls.getDecls();
    this.args = new HashMap<String, Object>();
  }

  @Override
  public IOperationBuilder setArgument(String name, String value) throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final Operation.Param decl = decls.get(name);

    if (decl.getKind() != Operation.Param.Kind.IMM) {
      throw new UndeclaredException(String.format(
        "The %s argument of the %s operation must be an immediate value.", name, opName));
    }

    final Location arg = Location.newLocationForConst(DataEngine.valueOf(decl.getType(), value));
    args.put(name, arg);

    return this;
  }

  @Override
  public IOperationBuilder setArgument(String name, int value) throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final Operation.Param decl = decls.get(name);
    if (decl.getKind() != Operation.Param.Kind.IMM) {
      throw new UndeclaredException(String.format(
        "The %s argument of the %s operation must be an immediate value.", name, opName));
    }

    final Data data = DataEngine.valueOf(decl.getType(), value);
    if (DataEngine.isLossOfSignificantBits(decl.getType(), value)) {
      System.out.printf("Warning: The value of the %s argument (= %d) of the %s operation " +
         "will be truncated to suit %s. This will cause loss of significant bits. Result: %d%n",
         name, value, opName, decl.getType(), data.getRawData().intValue());
    }

    final Location arg = Location.newLocationForConst(data);
    args.put(name, arg);

    return this;
  }

  @Override
  public IOperationBuilder setArgument(String name, IAddressingMode value)
      throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final Operation.Param decl = decls.get(name);

    if (decl.getKind() != Operation.Param.Kind.MODE) {
      throw new UndeclaredException(String.format(
        "The %s argument of the %s operation must be an addressing mode.", name, opName));
    }

    if (!decl.isSupported(value)) {
      throw new UndeclaredException(String.format(
        "The %s argument of the %s operation has an incompatible type.", name, opName));
    }

    args.put(name, value);
    return this;
  }

  @Override
  public IOperationBuilder setArgument(String name, IOperation value) throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final Operation.Param decl = decls.get(name);

    if (decl.getKind() != Operation.Param.Kind.OP) {
      throw new UndeclaredException(String.format(
        "The %s argument of the %s operation must be an operation.", name, opName));
    }

    if (!decl.isSupported(value)) {
      throw new UndeclaredException(String.format(
        "The %s argument of the %s operation has an incompatible type.", name, opName));
    }

    args.put(name, value);
    return this;
  }

  @Override
  public IOperation build() throws ConfigurationException {
    checkInitialized();
    return factory.create(args);
  }

  private void checkUndeclaredArgument(String name) throws UndeclaredException {
    final String ERROR_FORMAT = "The %s argument is not declared for the %s operation.";

    if (!decls.containsKey(name)) {
      throw new UndeclaredException(String.format(ERROR_FORMAT, name, opName));
    }
  }

  private void checkReassignment(String name) throws ReassignmentException {
    final String ERROR_FORMAT =
      "The value of the %s argument has already been assigned for " +
      "the current instance of the %s operation.";

    if (args.containsKey(name)) {
      throw new ReassignmentException(String.format(ERROR_FORMAT, name, opName));
    }
  }

  private void checkInitialized() throws UninitializedException {
    final String ERROR_FORMAT = "The % argument of the %s operation is not initialized.";

    for (String name : decls.keySet()) {
      if (!args.containsKey(name)) {
        throw new UninitializedException(String.format(ERROR_FORMAT, name, opName));
      }
    }
  }
}
