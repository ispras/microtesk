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

package ru.ispras.microtesk.model.metadata;

import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.IsaPrimitiveKind;
import ru.ispras.microtesk.utils.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * The {@code MetaArgument} class describes arguments or addressing modes and operations.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MetaArgument implements MetaData {
  private final String name;
  private final IsaPrimitiveKind kind;
  private final ArgumentMode mode;
  private final Set<String> typeNames;
  private final Type dataType;

  /**
   * Constructs a meta argument object for an immediate argument.
   * 
   * @param name argument name.
   * @param type the data type associated with the argument.
   * 
   * @throws IllegalArgumentException if any argument is {@code null}.
   */
  public MetaArgument(
      final String name,
      final Type type) {
    this(
        name,
        IsaPrimitiveKind.IMM,
        ArgumentMode.IN,
        Collections.singleton(Immediate.TYPE_NAME),
        type
        );
    InvariantChecks.checkNotNull(type);
  }

  /**
   * Constructs a meta argument object for an addressing mode argument.
   * 
   * @param name argument name.
   * @param type object describing the argument type.
   * @param mode the usage mode of the argument.
   * 
   * @throws IllegalArgumentException if any argument is {@code null}.
   */
  public MetaArgument(
      final String name,
      final MetaAddressingMode type,
      final ArgumentMode mode) {
    this(
        name,
        IsaPrimitiveKind.MODE,
        mode,
        type != null ? Collections.singleton(type.getName()) : null,
        type != null ? type.getDataType() : null
        );
    InvariantChecks.checkNotNull(type);
  }

  /**
   * Constructs a meta argument object for an operation argument.
   * 
   * @param name argument name.
   * @param type object describing the argument type.
   * @param mode the usage mode of the argument.
   * 
   * @throws IllegalArgumentException if any argument is {@code null}.
   */
  public MetaArgument(
      final String name,
      final MetaOperation type,
      final ArgumentMode mode) {
    this(
        name,
        IsaPrimitiveKind.OP,
        mode,
        type != null ? Collections.singleton(type.getName()) : null,
        null
        );
    InvariantChecks.checkNotNull(type);
  }

  /**
   * Constructs a meta argument object for an group.
   * 
   * @param name argument name.
   * @param type object describing the argument type.
   * @param mode the usage mode of the argument.
   * 
   * @throws IllegalArgumentException if any argument is {@code null}.
   */
  public MetaArgument(
      final String name,
      final MetaGroup type,
      final ArgumentMode mode) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(mode);

    this.name = name;
    this.kind = type.getKind() == MetaGroup.Kind.OP ? IsaPrimitiveKind.OP : IsaPrimitiveKind.MODE;
    this.mode = mode;
    this.typeNames = MetaDataUtils.toNameSetRecursive(type.getItems());
    this.dataType = type.getDataType();
  }

  /**
   * Constructs a meta argument object.
   * 
   * @param name argument name.
   * @param kind the kind of object associated with the argument.
   * @param mode the usage mode of the argument.
   * @param typeNames the set of of type names associated with the argument.
   * @param dataType the data type associated with the argument.
   * 
   * @throws IllegalArgumentException if any argument except for {@code dataType}
   *         is {@code null}; if the set of type names is empty.
   */
  public MetaArgument(
      final String name,
      final IsaPrimitiveKind kind,
      final ArgumentMode mode,
      final Set<String> typeNames,
      final Type dataType) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotEmpty(typeNames);

    this.name = name;
    this.kind = kind;
    this.mode = mode;
    this.typeNames = typeNames;
    this.dataType = dataType;
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
   * Returns the data type associated with the argument. Applicable
   * to immediate values and addressing modes. For operations,
   * it returns {@code null}.
   * 
   * @return Argument data type.
   */
  @Override
  public Type getDataType() {
    return dataType;
  }

  /**
   * Returns the kind of object associated with the argument.
   * 
   * @return Argument kind.
   */
  public IsaPrimitiveKind getKind() {
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
   * Returns an iterator for the collection of type names associated
   * with the argument.
   * 
   * @return An {@link Iterable} object that refers to the collection
   *         of type names (e.g. addressing mode names).
   */
  public Collection<String> getTypeNames() {
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    sb.append(String.format("[%s] %s: %s ",
        mode.getText(), getName(), kind.name().toLowerCase()));

    sb.append(StringUtils.toString(typeNames, "|"));

    if (dataType != null) {
      sb.append(String.format("(%s)", dataType.toString()));
    }

    return sb.toString();
  }
}
