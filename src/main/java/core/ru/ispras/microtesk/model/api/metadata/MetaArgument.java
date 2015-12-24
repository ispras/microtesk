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

import static ru.ispras.fortress.util.InvariantChecks.checkNotEmpty;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Set;

import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.data.Type;

/**
 * The {@code MetaArgument} class describes arguments or addressing modes and operations.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MetaArgument implements MetaData {

  /**
   * Specifies the type of object used as an argument.
   */

  public enum Kind {
    /** Immediate value */
    IMM,
    /** Addressing mode */
    MODE,
    /** Operation */
    OP,
  }

  private final Kind kind;
  private final ArgumentMode mode;
  private final String name;
  private final Set<String> typeNames;
  private final Type dataType;

  /**
   * Constructs a meta argument object.
   * 
   * @param kind the kind of object associated with the argument.
   * @param usageKind the usage mode of the argument.
   * @param name argument name.
   * @param typeNames the set of of type names associated with the argument.
   * @param dataType the data type associated with the argument.
   * 
   * @throws IllegalArgumentException if any argument except for {@code dataType}
   *         it {@code null}; if the set of type names is empty.
   */

  public MetaArgument(
      final Kind kind,
      final ArgumentMode mode,
      final String name,
      final Set<String> typeNames,
      final Type dataType) {
    checkNotNull(kind);
    checkNotNull(mode);
    checkNotNull(name);
    checkNotEmpty(typeNames);

    this.kind = kind;
    this.mode = mode;
    this.name = name;
    this.typeNames = typeNames;
    this.dataType = dataType;
  }

  /**
   * Returns the kind of object associated with the argument.
   * 
   * @return Argument kind.
   */

  public Kind getKind() {
    return kind;
  }

  /**
   * Returns the usage mode of the argument.
   * 
   * @return Argument usage mode.
   */

  public ArgumentMode getMode() {
    return mode;
  }

  /**
   * Returns the name of the argument.
   * 
   * @return Argument name.
   */

  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns an iterator for the collection of type names associated
   * with the argument.
   * 
   * @return An {@link Iterable} object that refers to the collection
   *         of type names (e.g. addressing mode names).
   */

  public Iterable<String> getTypeNames() {
    return typeNames;
  }

  /**
   * Checks whether if the specified type is accepted for the argument.
   * 
   * @param typeName Type name.
   * @return {@code true} if the specified type is accepted for
   *         the argument of {@code false} otherwise.
   */

  public boolean isTypeAccepted(final String typeName) {
    return typeNames.contains(typeName);
  }

  /**
   * Returns the data type associated with the argument. Applicable
   * to immediate values and addressing modes. For operations,
   * it returns {@code null}.
   * 
   * @return Argument data type.
   */

  public Type getDataType() {
    return dataType;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    sb.append(String.format("[%s] %s: %s ",
        mode.getText(), getName(), kind.name().toLowerCase()));

    boolean isFirst = false;
    for (final String typeName: typeNames) {
      if (isFirst) {
        isFirst = false; 
      } else {
        sb.append('|');
      }
      sb.append(typeName);
    }

    if (dataType != null) {
      sb.append(String.format("(%s)", dataType.toString()));
    }

    return sb.toString();
  }
}
