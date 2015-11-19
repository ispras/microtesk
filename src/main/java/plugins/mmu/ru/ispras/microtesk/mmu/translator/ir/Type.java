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
  private final String id;
  private final int bitSize;
  private final Map<String, Type> fields;
  private final DataType dataType;
  private final BitVector defaultValue;

  public Type(final int bitSize) {
    this(bitSize, null);
  }

  public Type(final int bitSize, final BitVector defaultValue) {
    checkTrue(bitSize > 0);

    this.id = null;
    this.bitSize = bitSize;
    this.fields = Collections.emptyMap();
    this.dataType = DataType.BIT_VECTOR(bitSize);
    this.defaultValue = defaultValue;
  }

  public Type(final String id, final Map<String, Type> fields) {
    checkNotNull(fields);
    checkNotNull(id);

    this.id = id;

    int totalBitSize = 0;
    for (final Type field : fields.values()) {
      totalBitSize += field.getBitSize();
    }

    checkTrue(totalBitSize > 0);
    this.bitSize = totalBitSize;
    this.fields = Collections.unmodifiableMap(fields);
    this.dataType = DataType.BIT_VECTOR(totalBitSize);
    this.defaultValue = null;
  }

  public String getId() {
    return id;
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
    return String.format(
        "type %s[size=%d, fields=%s]",
        id != null ? id : "",
        bitSize,
        fields
        );
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
