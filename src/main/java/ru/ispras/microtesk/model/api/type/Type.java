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

package ru.ispras.microtesk.model.api.type;

import java.util.Arrays;

/**
 * The Type class stores information on a type defined in the design specification. This includes
 * type identifier and size of the data in bits.
 * 
 * <p>For example, the following definition in Sim-nML:
 * 
 * <pre>type index = card(6)</pre>
 * 
 * corresponds to:
 * 
 * <pre>Type index = Type.CARD(6);</pre>
 * 
 * @author Andrei Tatarnikov
 */

public final class Type {
  public static Type INT(int bitSize) {
    return new Type(TypeId.INT, bitSize);
  }

  public static Type CARD(int bitSize) {
    return new Type(TypeId.CARD, bitSize);
  }

  public static Type BOOL(int bitSize) {
    return new Type(TypeId.BOOL, bitSize);
  }

  public static Type FLOAT(int fracBitSize, int expBitSize) {
    return new Type(TypeId.FLOAT, fracBitSize, expBitSize);
  }

  public static Type FIX(int beforeBinPtSize, int afterBinPtSize) {
    return new Type(TypeId.FIX, beforeBinPtSize, afterBinPtSize);
  }

  private final TypeId typeId;
  private final int[] fieldSizes;
  private final int bitSize;

  private Type(TypeId typeId, int... fieldSizes) {
    if (null == typeId) {
      throw new NullPointerException();
    }

    if (null == fieldSizes) {
      throw new NullPointerException();
    }

    if (0 == fieldSizes.length) {
      throw new IllegalArgumentException();
    }

    this.typeId = typeId;
    this.fieldSizes = fieldSizes;

    int totalSize = 0;
    for (int fieldSize : fieldSizes) {
      totalSize += fieldSize;
    }
    this.bitSize = totalSize;
  }

  public Type resize(int newBitSize) {
    if (bitSize == newBitSize) {
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

  public int getFieldCount() {
    return fieldSizes.length;
  }

  public int getFieldSize(int index) {
    if ((index < 0) || (index >= getFieldCount())) {
      throw new IndexOutOfBoundsException();
    }
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
  public boolean equals(Object obj) {
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
    for (int fieldSize : fieldSizes) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(fieldSize);
    }

    return String.format("%s.%s(%s)",
      getClass().getSimpleName(), typeId, sb);
  }
}
