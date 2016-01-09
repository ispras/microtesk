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

package ru.ispras.microtesk.model.api.instruction;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ArgumentKind;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.state.LocationAccessor;

/**
 * The {@link PrimitiveBuilder} class is responsible for creating and initializing
 * instances of nML primitives (addressing modes and operations).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public class PrimitiveBuilder<T extends IPrimitive> {
  private final String primitiveName;
  private final IPrimitive.IFactory<T> factory;
  private final Map<String, ArgumentDecls.Argument> decls;
  private final Map<String, Object> args;

  /**
   * Creates a builder for a primitive described with the specified parameters.
   * 
   * @param name The name of the primitive.
   * @param factory The factory for creating the specified primitive.
   * @param decls The table of argument declarations.
   */

  public PrimitiveBuilder(
      final String name,
      final IPrimitive.IFactory<T> factory,
      final ArgumentDecls decls) {
    this.primitiveName = name;
    this.factory = factory;
    this.decls = decls.getDecls();
    this.args = new HashMap<>();
  }

  /**
   * Initializes the specified argument with an integer value.
   * 
   * @param name Argument name.
   * @param value Argument integer value.
   */

  public LocationAccessor setArgument(
      final String name,
      final BigInteger value) throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final ArgumentDecls.Argument decl = decls.get(name);
    if (decl.getKind() != ArgumentKind.IMM) {
      throw new ConfigurationException(
          "The %s argument of %s must be an immediate value.", name, primitiveName);
    }

    final Type type = decl.getType();
    final Data data = Data.valueOf(type, value);

    if (Data.isLossOfSignificantBits(type, value)) {
      Logger.warning(
         "The value of the %s argument (= %d) of %s " +
         "will be truncated to suit %s. This will cause loss of significant bits. Result: %d",
         name, value, primitiveName, type, data.bigIntegerValue()
         );
    }

    final Location arg = Location.newLocationForConst(data);
    args.put(name, arg);

    return arg;
  }

  public void setArgument(
      final String name,
      final IAddressingMode value) throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final ArgumentDecls.Argument decl = decls.get(name);

    if (decl.getKind() != ArgumentKind.MODE) {
      throw new ConfigurationException(
          "The %s argument of %s must be an addressing mode.",
          name,
          primitiveName
          );
    }

    if (!decl.isSupported(value)) {
      throw new ConfigurationException(
          "The %s argument of %s has an incompatible type.",
          name,
          primitiveName
          );
    }

    args.put(name, value);
  }

  public void setArgument(
      final String name,
      final IOperation value) throws ConfigurationException {
    checkUndeclaredArgument(name);
    checkReassignment(name);

    final ArgumentDecls.Argument decl = decls.get(name);

    if (decl.getKind() != ArgumentKind.OP) {
      throw new ConfigurationException(
          "The %s argument of %s must be an operation.",
          name,
          primitiveName
          );
    }

    if (!decl.isSupported(value)) {
      throw new ConfigurationException(
          "The %s argument of %s has an incompatible type.",
          name,
          primitiveName
          );
    }

    args.put(name, value);
  }

  /**
   * Returns an primitive (addressing mode or operation) created by the builder.
   * 
   * @return The created and initialized primitive (addressing mode or operation).
   * @throws ConfigurationException Exception that informs of an error that occurs on attempt to
   *         build an object due to incorrect configuration.
   */

  public T build() throws ConfigurationException {
    checkInitialized();
    return factory.create(args);
  }

  private void checkUndeclaredArgument(final String name) throws ConfigurationException {
    if (!decls.containsKey(name)) {
      throw new ConfigurationException(
          "%s does not have an argument called %s.",
          primitiveName,
          name
          );
    }
  }

  private void checkReassignment(final String name) throws ConfigurationException {
    if (args.containsKey(name)) {
      throw new ConfigurationException(
          "The value of the %s argument has already been assigned for " +
          "the current instance of %s.",
          name,
          primitiveName
          );
    }
  }

  private void checkInitialized() throws ConfigurationException {
    for (final String name : decls.keySet()) {
      if (!args.containsKey(name)) {
        throw new ConfigurationException(
            "The % argument of the current instance of %s is not initialized.",
            name,
            primitiveName
            );
      }
    }
  }
}
