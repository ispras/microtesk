/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.memory.Memory;

import java.math.BigInteger;

public final class MemoryExpr {
  private final Memory.Kind kind;
  private final String name;
  private final Type type;
  private final BigInteger size;
  private final boolean shared;
  private final Alias alias;

  MemoryExpr(
      final Memory.Kind kind,
      final String name,
      final Type type,
      final BigInteger size,
      final boolean shared,
      final Alias alias) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(size);
    InvariantChecks.checkGreaterThan(size, BigInteger.ZERO);

    this.kind = kind;
    this.name = name;
    this.type = type;
    this.size = size;
    this.shared = shared;
    this.alias = alias;
  }

  public Memory.Kind getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public BigInteger getSize() {
    return size;
  }

  public boolean isShared() {
    return shared;
  }

  public Alias getAlias() {
    return alias;
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryExpr [kind=%s, name=%s, type=%s, size=%s, shared=%b]",
        kind,
        name,
        type,
        size,
        shared
        );
  }
}
