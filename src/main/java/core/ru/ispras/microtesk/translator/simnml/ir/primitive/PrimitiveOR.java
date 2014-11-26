/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class PrimitiveOR extends Primitive {
  private final List<Primitive> ors;

  PrimitiveOR(String name, Kind kind, List<Primitive> ors) {
    super(
      name,
      kind,
      true,
      getReturnType(ors),
      (null == ors) || ors.isEmpty() ? null : ors.get(0).getAttrNames()
    );

    this.ors = ors;
  }

  protected void addParentReference(PrimitiveAND parent, String referenceName) {
    super.addParentReference(parent, referenceName);

    for (Primitive target : ors) {
      target.addParentReference(parent, referenceName);
    }
  }

  public List<Primitive> getORs() {
    return ors;
  }

  private static Type getReturnType(List<Primitive> ors) {
    if (null == ors) {
      throw new NullPointerException();
    }

    return ors.get(0).getReturnType();
  }
}
