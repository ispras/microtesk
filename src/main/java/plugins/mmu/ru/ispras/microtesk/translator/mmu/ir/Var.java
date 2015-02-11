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

public final class Var {
  private final String id;
  private final Type type;

  // Address, Buffer(entry) or null.
  private final TypeProvider typeProvider;

  public Var(String id, Type type) {
    this(id, type, null);
  }

  public Var(String id, Type type, TypeProvider typeProvider) {
    checkNotNull(id);
    checkNotNull(type);

    if (type == Type.VOID) {
      throw new IllegalArgumentException(Type.VOID + " is not allowed.");
    }

    this.id = id;
    this.type = type;
    this.typeProvider = typeProvider;
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
    final String typeAlias = (typeProvider == null) ? "" : typeProvider.getTypeAlias() + "="; 
    return String.format("var %s[%s%s]", id, typeAlias, type);
  }
}
