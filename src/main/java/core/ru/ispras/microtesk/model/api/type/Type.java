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

package ru.ispras.microtesk.model.api.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

public final class Type {
  private static final Map<String, Type> ALIASES = new HashMap<>();

  public static Type typeOf(final String name, final int... params) {
    InvariantChecks.checkNotNull(name);

    final TypeId typeId = TypeId.fromName(name);
    if (null != typeId) {
      return typeId.newType(params);
    }

    final Type aliasType = ALIASES.get(name);
    if (null == aliasType) {
      throw new IllegalArgumentException("Unknown type: " + name);
    }

    if (params.length > 0) {
      throw new IllegalArgumentException(
          String.format("The %s type must not have any parameters.", name));
    }

    return aliasType;
  }

  public static Type def(final String name, final Type type) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);

    if (null != TypeId.fromName(name) || null != ALIASES.get(name)) {
      throw new IllegalArgumentException(
          String.format("The %s type is already defined.", name)); 
    }

    ALIASES.put(name, type);
    return type;
  }

  public static Type INT(final int bitSize) {
    return TypeId.INT.newType(bitSize);
  }

  public static Type CARD(final int bitSize) {
    return TypeId.CARD.newType(bitSize);
  }

  public static Type BOOL(final int bitSize) {
    return TypeId.BOOL.newType(bitSize);
  }

  public static Type FLOAT(final int fracBitSize, final int expBitSize) {
    return TypeId.FLOAT.newType(fracBitSize, expBitSize);
  }

  private final TypeId typeId;
  private final int[] fieldSizes;
  private final int bitSize;

  Type(final TypeId typeId, final int bitSize, final int... fieldSizes) {
    InvariantChecks.checkNotNull(typeId);
    InvariantChecks.checkGreaterThanZero(bitSize);

    this.typeId = typeId;
    this.bitSize = bitSize;
    this.fieldSizes = fieldSizes;
  }

  public boolean isInteger() {
    return typeId.isInteger();
  }

  public Type resize(final int newBitSize) {
    InvariantChecks.checkGreaterThanZero(bitSize);

    if (bitSize == newBitSize) {
      return this;
    }

    return new Type(typeId, newBitSize);
  }

  public Type castTo(final TypeId newTypeId) {
    InvariantChecks.checkNotNull(typeId);

    if (typeId == newTypeId) {
      return this;
    }

    return new Type(newTypeId, bitSize);
  }

  public TypeId getTypeId() {
    return typeId;
  }

  public int getBitSize() {
    return bitSize;
  }

  public int getFieldCount() {
    return fieldSizes.length;
  }

  public int getFieldSize(final int index) {
    InvariantChecks.checkBounds(index, getFieldCount());
    return fieldSizes[index];
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + typeId.hashCode();
    result = prime * result + Arrays.hashCode(fieldSizes);

    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Type other = (Type) obj;
    if (typeId != other.typeId) {
      return false;
    }

    if (bitSize != other.bitSize) {
      return false;
    }

    return Arrays.equals(fieldSizes, other.fieldSizes);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    if (fieldSizes.length == 0) {
      sb.append(bitSize);
    } else {
      for (final int fieldSize : fieldSizes) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(fieldSize);
      }
    }

    return String.format(
        "%s.%s(%s)", getClass().getSimpleName(), typeId, sb);
  }
}
