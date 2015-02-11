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
import ru.ispras.fortress.data.DataType;

public final class Address {
  private final String id;
  private final Type type;

  public Address(String id, Type type) {
    checkNotNull(id);
    checkNotNull(type);

    this.id = id;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public Type getType() {
    return type;
  }

  public DataType getDataType() {
    return type.getDataType();
  }

  public int getBitSize() {
    return type.getBitSize();
  }

  @Override
  public String toString() {
    return String.format("address %s[%s]", id, type);
  }
}
