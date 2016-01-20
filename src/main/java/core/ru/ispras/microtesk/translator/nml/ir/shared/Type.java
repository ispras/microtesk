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

package ru.ispras.microtesk.translator.nml.ir.shared;

import java.util.Arrays;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.TypeId;

public final class Type {
  public final static Type BOOLEAN = new Type(TypeId.BOOL, 1);
  public final static Type LOGIC_INT = new Type(TypeId.INT, 0);

  public static Type INT(final int bitSize) {
    InvariantChecks.checkGreaterThanZero(bitSize);
    return new Type(TypeId.INT, bitSize);
  }

  public static Type CARD(final int bitSize) {
    InvariantChecks.checkGreaterThanZero(bitSize);
    return new Type(TypeId.CARD, bitSize);
  }

  public static Type FLOAT(final int fracBitSize, final int expBitSize) {
    InvariantChecks.checkGreaterThanZero(fracBitSize);
    InvariantChecks.checkGreaterThanZero(expBitSize);

    // 1 is added to make room for implicit sign bit
    final int bitSize = fracBitSize + expBitSize + 1;

    return new Type(
        TypeId.FLOAT,
        null,
        bitSize,
        new int[] {fracBitSize, expBitSize}
        );
  }

  private static final Class<?> MODEL_API_CLASS =
      ru.ispras.microtesk.model.api.data.Type.class;

  private final TypeId typeId;
  private final String aliasName;
  private final int bitSize;
  private final int[] fieldSizes;

  private Type(
      final TypeId typeId,
      final String aliasName,
      final int bitSize,
      final int[] fieldSizes) {
    InvariantChecks.checkNotNull(typeId);
    InvariantChecks.checkGreaterThanZero(bitSize);
    InvariantChecks.checkTrue(fieldSizes.length > 0);

    this.typeId = typeId;
    this.aliasName = aliasName;
    this.bitSize = bitSize;
    this.fieldSizes = fieldSizes;
  }

  private Type(final Type type, final String aliasName) {
    this.typeId = type.typeId;
    this.aliasName = aliasName;
    this.bitSize = type.bitSize;
    this.fieldSizes = type.fieldSizes;
  }

  private Type(final TypeId typeId, final int bitSize) {
    this(typeId, null, bitSize, new int[] {bitSize});
  }

  private Type(final TypeId typeId, final int... fieldSizes) {
    this(typeId, null, getTotalSize(fieldSizes), fieldSizes);
  }

  private static int getTotalSize(final int... fieldSizes) {
    int totalSize = 0;
    for (final int size : fieldSizes) {
      totalSize += size;
    }
    return totalSize;
  }

  public Type alias(final String name) {
    InvariantChecks.checkNotNull(name);

    if (name.equals(aliasName)) {
      return this;
    }

    return new Type(this, name);
  }

  public Type resize(final int newBitSize) {
    if (newBitSize == bitSize) {
      return this;
    }

    return new Type(typeId, newBitSize);
  }

  public TypeId getTypeId() {
    return typeId;
  }

  public int getBitSize() {
    return bitSize;
  }

  public String getAlias() {
    return aliasName;
  }

  public String getJavaText() {
    return (null != aliasName) ? aliasName : getTypeName();
  }

  public String getTypeName() {
    final StringBuilder sb = new StringBuilder();

    for (final int fieldSize : fieldSizes) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(fieldSize);
    }

    return String.format(
        "%s.%s(%s)", MODEL_API_CLASS.getSimpleName(), getTypeId(), sb);
  }

  @Override
  public String toString() {
    return String.format("%s, bitSize=%d, alias=%s",
      getTypeName(), getBitSize(), aliasName != null ? aliasName : "<undefined>");
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

    return Arrays.equals(this.fieldSizes, other.fieldSizes);
  }
}
