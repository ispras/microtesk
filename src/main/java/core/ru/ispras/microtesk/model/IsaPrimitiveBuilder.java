/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.model.memory.LocationAccessor;
import ru.ispras.microtesk.test.template.LabelReference;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link IsaPrimitiveBuilder} class is responsible for creating and initializing
 * instances of nML primitives (addressing modes and operations).
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class IsaPrimitiveBuilder {
  private final IsaPrimitiveInfoAnd info;
  private final Map<String, IsaPrimitive> args;
  private LabelReference labelReference = null;

  /**
   * Creates a builder for a primitive described with the specified parameters.
   *
   * @param info Information on the primitive to be built.
   */
  public IsaPrimitiveBuilder(final IsaPrimitiveInfoAnd info) {
    InvariantChecks.checkNotNull(info);

    this.info = info;
    this.args = new HashMap<>();
  }

  /**
   * Initializes the specified immediate argument with an integer value.
   *
   * @param name Argument name.
   * @param value Argument integer value.
   * @return Accessor for the argument value.
   *
   * @throws ConfigurationException if the specified argument is not an immediate value.
   */
  public LocationAccessor setArgument(
      final String name,
      final BigInteger value) throws ConfigurationException {
    checkReassignment(name);

    final IsaPrimitiveInfo argInfo = getArgumentInfo(name);
    if (argInfo.getKind() != IsaPrimitiveKind.IMM) {
      throw new ConfigurationException(
          "The %s argument of %s must be an immediate value.", name, info.getName());
    }

    final Type type = argInfo.getType();
    final Data data = Data.valueOf(type, value);

    if (Data.isLossOfSignificantBits(type, value)) {
      Logger.warning(
          "The value of the %s argument (= %d) of %s will be truncated to suit %s."
              + " This will cause loss of significant bits. Result: %d",
          name, value, info.getName(), type, data.bigIntegerValue()
      );
    }

    final Location location = Location.newLocationForConst(data);
    final Immediate arg = new Immediate(location);

    args.put(name, arg);
    return location;
  }

  public void setArgument(
      final String name,
      final IsaPrimitive value) throws ConfigurationException {
    checkReassignment(name);

    final IsaPrimitiveInfo argInfo = getArgumentInfo(name);
    if (!argInfo.isSupported(value)) {
      throw new ConfigurationException(
          "The %s argument of %s has an incompatible type.", name, info.getName());
    }

    args.put(name, value);
  }

  public void setLabelReference(final LabelReference labelReference) throws ConfigurationException {
    if (null != this.labelReference) {
      throw new ConfigurationException(
          "The label reference for %s is already assigned.", info.getName());
    }
    this.labelReference = labelReference;
  }

  /**
   * Returns an primitive (addressing mode or operation) created by the builder.
   *
   * @return The created and initialized primitive (addressing mode or operation).
   * @throws ConfigurationException Exception that informs of an error that occurs on attempt to
   *         build an object due to incorrect configuration.
   */
  public IsaPrimitive build() throws ConfigurationException {
    checkInitialized();

    final IsaPrimitive result = info.create(args);
    if (null != labelReference) {
      result.setLabelReference(labelReference);
    }

    return result;
  }

  private IsaPrimitiveInfo getArgumentInfo(final String name) throws ConfigurationException {
    final IsaPrimitiveInfo argInfo = info.getArgument(name);
    if (null == argInfo) {
      throw new ConfigurationException(
          "%s does not have an argument called %s.", info.getName(), name);
    }
    return argInfo;
  }

  private void checkReassignment(final String name) throws ConfigurationException {
    if (args.containsKey(name)) {
      throw new ConfigurationException(
          "The value of the %s argument of %s has already been assigned.", name, info.getName());
    }
  }

  private void checkInitialized() throws ConfigurationException {
    for (final String name : info.getArgumentNames()) {
      if (!args.containsKey(name)) {
        throw new ConfigurationException(
            "The %s argument of %s is not initialized.", name, info.getName());
      }
    }
  }
}
