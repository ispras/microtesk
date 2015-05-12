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

package ru.ispras.microtesk.translator.nml.ir.shared;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.location.Location;

public final class MemoryExpr {
  private final Memory.Kind kind;
  private final Type type;
  private final Expr size;
  private final Location alias;

  MemoryExpr(
      final Memory.Kind kind,
      final Type type,
      final Expr size,
      final Location alias) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(size);

    this.size = size;
    this.type = type;
    this.kind = kind;
    this.alias = alias;
  }

  public Memory.Kind getKind() {
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

  public Location getAlias() {
    return alias;
  }

  @Override
  public String toString() {
    return String.format("MemoryExpr [kind=%s, type=%s, size=%s]",
        kind, type, size);
  }
}
