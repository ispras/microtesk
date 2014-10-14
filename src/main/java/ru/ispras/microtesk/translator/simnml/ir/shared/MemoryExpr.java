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

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.model.api.memory.MemoryKind;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class MemoryExpr {
  private final MemoryKind kind;
  private final Type type;
  private final Expr size;

  public MemoryExpr(MemoryKind kind, Type type, Expr size) {
    if (null == type) {
      throw new NullPointerException();
    }

    if (null == size) {
      throw new NullPointerException();
    }

    this.size = size;
    this.type = type;
    this.kind = kind;
  }

  public MemoryKind getKind() {
    return kind;
  }

  public Type getType() {
    return type;
  }

  public Expr getSizeExpr() {
    return size;
  }

  public int getSize() {
    return size.integerValue();
  }

  @Override
  public String toString() {
    return String.format("MemoryExp [kind=%s, type= %s, size= %s]", kind, type, size);
  }
}
