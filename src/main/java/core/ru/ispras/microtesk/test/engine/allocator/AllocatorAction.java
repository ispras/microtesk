/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine.allocator;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.utils.SharedObject;

/**
 * The {@link AllocatorAction} class describes an allocator actions.
 * These actions modify allocation flags (free, reserved, visible) for a registers.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class AllocatorAction {

  public enum Kind {
    FREE,
    RESERVED,
    VISIBLE
  }

  private final Primitive primitive;
  private final Kind kind;
  private final boolean value;
  private final boolean applyToAll;

  public AllocatorAction(
      final Primitive primitive,
      final Kind kind,
      final boolean value,
      final boolean applyToAll) {
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkTrue(primitive.getKind() == Primitive.Kind.MODE);
    InvariantChecks.checkNotNull(kind);

    this.primitive = primitive;
    this.kind = kind;
    this.value = value;
    this.applyToAll = applyToAll;
  }

  public AllocatorAction(final AllocatorAction other) {
    InvariantChecks.checkNotNull(other);

    this.primitive = (Primitive)((SharedObject<?>) other.primitive).getCopy();
    this.kind = other.kind;
    this.value = other.value;
    this.applyToAll = other.applyToAll;
  }

  public Primitive getPrimitive() {
    return primitive;
  }

  public Kind getKind() {
    return kind;
  }

  public boolean getValue() {
    return value;
  }

  public boolean isApplyToAll() {
    return applyToAll;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(primitive.getName());
    sb.append('(');

    sb.append(kind.name());
    sb.append('=');
    sb.append(value);

    if (applyToAll) {
      sb.append(", ALL");
    }

    sb.append(')');
    return sb.toString();
  }
}
