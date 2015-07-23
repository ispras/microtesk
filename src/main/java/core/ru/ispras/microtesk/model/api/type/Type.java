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

import static ru.ispras.fortress.util.InvariantChecks.checkBounds;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The Type class stores information on a type defined in the design specification. This includes
 * type identifier and size of the data in bits.
 * 
 * <p>For example, the following definition in nML:
 * 
 * <pre>type index = card(6)</pre>
 * 
 * corresponds to:
 * 
 * <pre>Type index = Type.CARD(6);</pre>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class Type {
  private static final class TypeDefCreator implements TypeCreator {
    private final String name;
    private final Type type;

    TypeDefCreator(final String name, final Type type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public Type createWithParams(final int... params) {
      if (params.length > 0) {
        throw new IllegalArgumentException(String.format(
            "The %s type not have any parameters.", name));
      }
      return type;
    }
  }

  private static final Map<String, TypeCreator> typeCreators = new HashMap<>();
  static {
    for (final TypeId typeId : TypeId.values()) {
      typeCreators.put(typeId.name(), typeId);
      typeCreators.put(typeId.name().toLowerCase(), typeId);
    }
  }

  public static Type typeOf(final String name, final int... params) {
    checkNotNull(name);

    final TypeCreator creator = typeCreators.get(name);
    if (null == creator) {
      throw new IllegalArgumentException("Unknown type: " + name);
    }

    return creator.createWithParams(params);
  }

  public static Type def(final String name, final Type type) {
    checkNotNull(name);
    checkNotNull(type);

    if (typeCreators.containsKey(name)) {
      throw new IllegalArgumentException(String.format(
          "The %s type is already defined.", name)); 
    }

    typeCreators.put(name, new TypeDefCreator(name, type));
    return type;
  }

  public static Type INT(final int bitSize) {
    return TypeId.INT.createWithParams(bitSize);
  }

  public static Type CARD(final int bitSize) {
    return TypeId.CARD.createWithParams(bitSize);
  }

  public static Type BOOL(final int bitSize) {
    return TypeId.BOOL.createWithParams(bitSize);
  }

  public static Type FLOAT(final int fracBitSize, final int expBitSize) {
    return TypeId.FLOAT.createWithParams(fracBitSize, expBitSize);
  }

  public static Type FIX(final int beforeBinPtSize, final int afterBinPtSize) {
    return TypeId.FIX.createWithParams(beforeBinPtSize, afterBinPtSize);
  }

  private final TypeId typeId;
  private final int[] fieldSizes;
  private final int bitSize;

  Type(final TypeId typeId, final int bitSize, final int... fieldSizes) {
    checkNotNull(typeId);
    checkGreaterThanZero(bitSize);

    this.typeId = typeId;
    this.bitSize = bitSize;
    this.fieldSizes = fieldSizes;
  }

  public Type resize(final int newBitSize) {
    checkGreaterThanZero(bitSize);

    if (bitSize == newBitSize) {
      return this;
    }

    return new Type(typeId, newBitSize);
  }

  public Type castTo(final TypeId newTypeId) {
    checkNotNull(typeId);

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
    checkBounds(index, getFieldCount());
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

    return String.format("%s.%s(%s)",
      getClass().getSimpleName(), typeId, sb);
  }
}
