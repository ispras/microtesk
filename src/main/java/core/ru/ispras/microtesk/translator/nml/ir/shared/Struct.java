/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.TypeId;

public final class Struct {
  public static final class Field {
    private final int offset;
    private final Type type;

    private Field(final Type type, final int offset) {
      this.offset = offset;
      this.type = type;
    }

    public Type getType() { return type; }
    public int getBitSize() { return type.getBitSize(); }

    public int getMin() { return offset; }
    public int getMax() { return offset + getBitSize(); }
  }

  private final Map<String, Field> fields;
  private int bitSize;
  private final TypeId typeId;

  public Struct() {
    this.fields = new LinkedHashMap<>();
    this.bitSize = 0;
    this.typeId = TypeId.CARD;
  }

  public void addField(final String name, final Type type) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);

    fields.put(name, new Field(type, bitSize));
    bitSize += type.getBitSize();
  }

  public Field getField(final String name) {
    InvariantChecks.checkNotNull(name);
    return fields.get(name);
  }

  public Field getField(final List<String> names) {
    InvariantChecks.checkNotEmpty(names);

    if (names.size() == 1) {
      return getField(names.get(0));
    }

    Struct struct = this;
    Field field = null;

    int offset = 0;
    for (final String name : names) {
      if (null == struct) { return null; }
      field = struct.getField(name);
      if (null == field) { return null; }

      offset += field.offset;
      struct = field.getType().getStruct();
    }

    return new Field(field.getType(), offset);
  }

  public int getBitSize() {
    return bitSize;
  }

  public TypeId getTypeId() {
    return typeId;
  }
}
