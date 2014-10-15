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

package ru.ispras.microtesk.model.api.instruction;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.ReassignmentException;
import ru.ispras.microtesk.model.api.exception.UndeclaredException;
import ru.ispras.microtesk.model.api.exception.UninitializedException;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The AddressingModeBuilder class implements logic responsible for creating and initializing
 * addressing mode objects.
 * 
 * @author Andrei Tatarnikov
 */

public final class AddressingModeBuilder implements IAddressingModeBuilder {
  private final String modeName;
  private final IAddressingMode.IFactory factory;
  private final Map<String, Type> decls;
  private final Map<String, Data> args;

  /**
   * Creates a builder for an addressing mode based on the specified configuration parameters.
   * 
   * @param modeName The name of the mode.
   * @param factory The factory for creating the specified mode.
   * @param decls The table of mode argument declarations.
   */

  public AddressingModeBuilder(
      String modeName, IAddressingMode.IFactory factory, Map<String, Type> decls) {
    this.modeName = modeName;
    this.factory = factory;
    this.decls = decls;
    this.args = new HashMap<String, Data>();
  }

  @Override
  public IAddressingModeBuilder setArgumentValue(String name, String value)
      throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final Data data = DataEngine.valueOf(decls.get(name), value);
    args.put(name, data);

    return this;
  }

  @Override
  public IAddressingModeBuilder setArgumentValue(String name, Integer value)
      throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final Data data = DataEngine.valueOf(decls.get(name), value);
    args.put(name, data);

    return this;
  }

  @Override
  public IAddressingMode getProduct() throws ConfigurationException {
    checkInitialized();
    return factory.create(args);
  }

  private void checkUndeclaredArgument(String name) throws UndeclaredException {
    final String ERROR_FORMAT = "The %s argument is not declared for the %s addressing mode.";

    if (!decls.containsKey(name)) {
      throw new UndeclaredException(String.format(ERROR_FORMAT, name, modeName));
    }
  }

  private void checkReassignment(String name) throws ReassignmentException {
    final String ERROR_FORMAT =
      "The value of the %s argument has already been assigned for " +
      "the current instance of the %s addressing mode.";

    if (args.containsKey(name)) {
      throw new ReassignmentException(String.format(ERROR_FORMAT, name, modeName));
    }
  }

  private void checkInitialized() throws UninitializedException {
    final String ERROR_FORMAT = "The % argument of the %s mode is not initialized.";

    for (String name : decls.keySet()) {
      if (!args.containsKey(name)) {
        throw new UninitializedException(String.format(ERROR_FORMAT, name, modeName));
      }
    }
  }
}
