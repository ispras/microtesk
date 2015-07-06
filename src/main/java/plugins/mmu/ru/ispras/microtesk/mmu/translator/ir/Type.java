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

import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import ru.ispras.fortress.data.DataType;

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
