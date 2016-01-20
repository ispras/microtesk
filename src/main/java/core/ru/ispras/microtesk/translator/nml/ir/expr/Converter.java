/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.data.DataType;
import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class Converter {
  private Converter() {}

  private static final Set<TypeId> BV_BASED_TYPES =
      EnumSet.of(TypeId.INT, TypeId.CARD, TypeId.FLOAT);

  public static DataType toFortressDataType(final Type type) {
    final TypeId typeId = type.getTypeId();
    if (typeId == TypeId.BOOL) {
      return DataType.BOOLEAN;
    }

    if (BV_BASED_TYPES.contains(typeId)) {
      return DataType.BIT_VECTOR(type.getBitSize());
    }

    throw new IllegalArgumentException("Unsupported type: "  + type);
  }
}
