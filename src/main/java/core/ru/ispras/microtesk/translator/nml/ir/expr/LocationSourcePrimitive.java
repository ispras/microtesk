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

package ru.ispras.microtesk.translator.nml.ir.expr;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class LocationSourcePrimitive implements LocationSource {
  private final Primitive primitive;

  protected LocationSourcePrimitive(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);
    this.primitive = primitive;
  }

  @Override
  public NmlSymbolKind getSymbolKind() {
    return NmlSymbolKind.ARGUMENT;
  }

  public Primitive.Kind getKind() {
    return primitive.getKind();
  }

  @Override
  public Type getType() {
    return primitive.getReturnType();
  }

  public Primitive getPrimitive() {
    return primitive;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final LocationSourcePrimitive other = (LocationSourcePrimitive) obj;
    return primitive == other.primitive;
  }
}
