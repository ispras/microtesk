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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterOrEqZero;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class Entry {
  public static final Entry EMPTY = new Entry(0, Collections.<String, Field>emptyMap());

  private final int bitSize;
  private final Map<String, Field> fields;

  public Entry(int bitSize, Map<String, Field> fields) {
    checkGreaterOrEqZero(bitSize);
    checkNotNull(fields);

    this.bitSize = bitSize;
    this.fields = Collections.unmodifiableMap(fields);
  }

  public int getBitSize() {
    return bitSize;
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
    return String.format("entry [fields=%s]", fields);
  }
}
