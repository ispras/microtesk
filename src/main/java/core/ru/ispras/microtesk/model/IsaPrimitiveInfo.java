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

package ru.ispras.microtesk.model;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.Type;

public abstract class IsaPrimitiveInfo {
  private final IsaPrimitiveKind kind;
  private final String name;
  private final Type type;

  public IsaPrimitiveInfo(
      final IsaPrimitiveKind kind,
      final String name,
      final Type type) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(name);

    this.kind = kind;
    this.name = name;
    this.type = type;
  }

  public final IsaPrimitiveKind getKind() {
    return kind;
  }

  public final String getName() {
    return name;
  }

  public final Type getType() {
    return type;
  }

  public abstract boolean isSupported(final IsaPrimitive primitive);
}
