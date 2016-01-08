/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ArgumentKind;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.ReassignmentException;
import ru.ispras.microtesk.model.api.exception.UndeclaredException;
import ru.ispras.microtesk.model.api.exception.UninitializedException;
import ru.ispras.microtesk.model.api.memory.Location;

/**
 * The {@link AddressingModeBuilder} class implements logic responsible for
 * creating and initializing addressing mode objects.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class AddressingModeBuilder {
  private final String modeName;
  private final IAddressingMode.IFactory factory;
  private final Map<String, ArgumentDecls.Argument> decls;
  private final Map<String, Object> args;

  /**
   * Creates a builder for an addressing mode based on the specified configuration parameters.
   * 
   * @param modeName The name of the mode.
   * @param factory The factory for creating the specified mode.
   * @param decls The table of mode argument declarations.
   */

  public AddressingModeBuilder(
      final String modeName,
      final IAddressingMode.IFactory factory,
      final ArgumentDecls decls) {
    this.modeName = modeName;
    this.factory = factory;
    this.decls = decls.getDecls();
    this.args = new HashMap<>();
  }

  /**
   * Initializes the specified addressing mode argument with an integer value.
   * 
   * @param name Mode argument name.
   * @param value Mode argument integer value.
   */

  public AddressingModeBuilder setArgumentValue(
      final String name,
      final BigInteger value) throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final ArgumentDecls.Argument decl = decls.get(name);
    if (decl.getKind() != ArgumentKind.IMM) {
      throw new UndeclaredException(String.format(
          "The %s argument of the %s addressing mode must be an immediate value.", name, modeName));
    }

    final Type type = decl.getType();
    final Data data = Data.valueOf(type, value);

    if (Data.isLossOfSignificantBits(type, value)) {
      Logger.warning(
         "The value of the %s argument (= %d) of the %s addressing mode " +
         "will be truncated to suit %s. This will cause loss of significant bits. Result: %d",
         name, value, modeName, type, data.bigIntegerValue()
         );
    }

    final Location arg = Location.newLocationForConst(data);
    args.put(name, arg);

    return this;
  }

  /**
   * Returns an addressing mode object created by the builder.
   * 
   * @return The addressing mode object.
   * @throws ConfigurationException Exception that informs of an error that occurs on attempt to
   *         build an addressing mode object due to incorrect configuration.
   */

  public IAddressingMode build() throws ConfigurationException {
    checkInitialized();
    return factory.create(args);
  }

  private void checkUndeclaredArgument(final String name) throws UndeclaredException {
    if (!decls.containsKey(name)) {
      throw new UndeclaredException(String.format(
          "The %s argument is not declared for the %s addressing mode.", name, modeName));
    }
  }

  private void checkReassignment(final String name) throws ReassignmentException {
    if (args.containsKey(name)) {
      throw new ReassignmentException(String.format(
          "The value of the %s argument has already been assigned for " +
          "the current instance of the %s addressing mode.", name, modeName));
    }
  }

  private void checkInitialized() throws UninitializedException {
    for (final String name : decls.keySet()) {
      if (!args.containsKey(name)) {
        throw new UninitializedException(String.format(
            "The % argument of the %s mode is not initialized.", name, modeName));
      }
    }
  }
}
