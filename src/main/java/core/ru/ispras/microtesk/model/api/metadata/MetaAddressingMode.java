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

package ru.ispras.microtesk.model.api.metadata;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Map;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The {@code MetaAddressingMode} class holds information on the specified
 * addressing mode.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MetaAddressingMode implements MetaData {
  private final String name;
  private final Type dataType;
  private final Map<String, MetaArgument> args;
  private final boolean exception;
  private final boolean memoryAccess;

  /**
   * Constructs a metadata object for an addressing mode.
   * 
   * @param name Addressing mode name.
   * @param dataType the type of data accessed via the addressing mode.
   * @param argumentNames Argument names.
   * @param exception {@code true} if the addressing mode can throw an
   *        exception or {@code false} otherwise.
   * 
   * @throws IllegalArgumentException if any of the parameters is {@code null}.
   */

  public MetaAddressingMode(
      final String name,
      final Type dataType,
      final Map<String, MetaArgument> args,
      final boolean exception,
      final boolean memoryAccess) {
    checkNotNull(name);
    checkNotNull(args);

    this.name = name;
    this.dataType = dataType;
    this.args = args;
    this.exception = exception;
    this.memoryAccess = memoryAccess;
  }

  /**
   * Returns the name of the addressing mode.
   * 
   * @return Mode name.
   */

  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the type of data accessed via the addressing mode.
   * 
   * @return Data type.
   */

  public Type getDataType() {
    return dataType;
  }

  /**
   * Returns a collection of addressing mode arguments.
   * 
   * @return Collection of addressing mode arguments.
   */

  public Iterable<MetaArgument> getArguments() {
    return args.values();
  }

  /**
   * Return an argument of the given addressing mode  that has the specified name.
   * 
   * @param name Argument name.
   * @return Argument with the specified name or {@code null} if no such
   *         argument is defined.
   */

  public MetaArgument getArgument(final String name) {
    return args.get(name);
  }

  /**
   * Returns the list of addressing mode argument.
   * 
   * @return Collection of argument names.
   */

  public Iterable<String> getArgumentNames() {
    return args.keySet();
  }

  /**
   * Checks whether the addressing mode has an argument with the specified name.
   * 
   * @param name Argument name.
   * @return {@code true} if the argument is defined of {@code false} otherwise.
   */

  public boolean isArgumentDefined(final String name) {
    return args.containsKey(name);
  }

  /**
   * Checks whether the addressing mode (its attributes) can throw an exception.
   *  
   * @return {@code true} if the addressing mode can throw an exception
   * or {@code false} otherwise.
   */

  public boolean canThrowException() {
    return exception;
  }

  /**
   * Checks whether the addressing mode provides an access to memory.
   * 
   * @return {@code true} if the addressing mode provides an access to memory
   * or {@code false} otherwise.
   */

  public boolean isMemoryAccess() {
    return memoryAccess;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final String argName : getArgumentNames()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(argName);
    }

    return String.format("%s (%s)", name, sb.toString());
  }
}
