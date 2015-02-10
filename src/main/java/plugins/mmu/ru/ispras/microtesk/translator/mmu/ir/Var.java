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
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;

import java.util.Collection;

public final class Var {
  private final String id;
  private final int bitSize;
  private final Entry entry;

  public static Var newInstance(String id, int bitSize) {
    return new Var(id, bitSize, Entry.EMPTY);
  }

  public static Var newInstance(String id, Entry entry) {
    return new Var(id, entry.getBitSize(), entry);
  }

  private Var(String id, int bitSize, Entry entry) {
    checkNotNull(id);
    checkGreaterThanZero(bitSize);
    checkNotNull(entry);

    this.id = id;
    this.bitSize = bitSize;
    this.entry = entry;
  }

  public String getId() {
    return id;
  }

  public int getBitSize() {
    return bitSize;
  }

  public int getFieldCount() {
    return entry.getFieldCount();
  }

  public Collection<Field> getFields() {
    return entry.getFields();
  }

  public Field getField(String name) {
    return entry.getField(name);
  }

  @Override
  public String toString() {
    return String.format("var %s(%d)%s",
        id, bitSize, getFieldCount() == 0 ? "" : getFields());
  }
}
