/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;

import java.util.Collections;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;

public final class Type extends Nested<Type> {
  private final int bitSize;
  private final Map<String, Type> fields;
  private final DataType dataType;
  private final BitVector defaultValue;

  public Type(final int bitSize) {
    this(bitSize, null);
  }

  public Type(final int bitSize, final BitVector defaultValue) {
    checkTrue(bitSize > 0);

    this.bitSize = bitSize;
    this.fields = Collections.emptyMap();
    this.dataType = DataType.BIT_VECTOR(bitSize);
    this.defaultValue = defaultValue;
  }

  public Type(final Map<String, Type> fields) {
    checkNotNull(fields);

    int totalBitSize = 0;
    for (final Type field : fields.values()) {
      totalBitSize += field.getBitSize();
    }

    checkTrue(totalBitSize > 0);
    this.bitSize = totalBitSize;
    this.fields = Collections.unmodifiableMap(fields);
    this.dataType = DataType.UNKNOWN;
    this.defaultValue = null;
  }

  public boolean isStruct() {
    return !fields.isEmpty();
  }

  public int getBitSize() {
    return bitSize;
  }

  public DataType getDataType() {
    return dataType;
  }

  public BitVector getDefaultValue() {
    return defaultValue;
  }

  public Map<String, Type> getFields() {
    return fields;
  }

  @Override
  protected Type getNested(final String name) {
    return getFields().get(name);
  }

  @Override
  public String toString() {
    return String.format("type [size=%d, fields=%s]", bitSize, fields);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || !(o instanceof Type)) {
      return false;
    }
    final Type type = (Type) o;
    return bitSize == type.bitSize &&
           fields.equals(type.fields);
  }

  @Override
  public int hashCode() {
    return bitSize * 31 + fields.hashCode();
  }
}

/*
public final class Type {
  public static final Type VOID = new Type();

  private final int bitSize;
  private final Map<String, Field> fields;
  private final DataType dataType;

  public Type(final int bitSize) {
    checkGreaterThanZero(bitSize);
    this.bitSize = bitSize;
    this.fields = Collections.emptyMap();
    this.dataType = DataType.BIT_VECTOR(bitSize);
  }

  public Type(final Map<String, Field> fields) {
    checkNotNull(fields);

    int totalBitSize = 0;
    for (final Field field : fields.values()) {
      totalBitSize += field.getBitSize();
    }

    checkGreaterThanZero(totalBitSize);
    this.bitSize = totalBitSize;
    this.fields = Collections.unmodifiableMap(fields);
    this.dataType = DataType.BIT_VECTOR(totalBitSize);
  }

  private Type() {
    this.bitSize = 0;
    this.fields = Collections.emptyMap();
    this.dataType = null;
  }

  public int getBitSize() {
    return bitSize;
  }

  public DataType getDataType() {
    return dataType;
  }

  public int getFieldCount() {
    return fields.size();
  }

  public Collection<Field> getFields() {
    return fields.values();
  }

  public Field getField(String name) {
    checkNotNull(name);
    return fields.get(name);
  }

  @Override
  public String toString() {
    return String.format("type [size=%d, fields=%s]", bitSize, fields);
  }
}
*/
