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

package ru.ispras.microtesk.model.api.data.operations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.model.api.type.Type;

public class ArithmMul implements IBinaryOperator {
  private final static Set<TypeId> SUPPORTED_TYPES = Collections.unmodifiableSet(EnumSet.of(
    TypeId.INT, TypeId.CARD
// , ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
// , ETypeID.FIX // NOT SUPPORTED IN THIS VERSION
  ));

  @Override
  public Data execute(Data left, Data right) {
    assert left.getType().equals(right.getType()) : "Restriction: types (and sizes) should match.";

    final int result = left.getRawData().intValue() * right.getRawData().intValue();
    final Type resultType = left.getType();

    return new Data(BitVector.valueOf(result, resultType.getBitSize()), resultType);
  }

  @Override
  public boolean supports(Type left, Type right) {
    if (!SUPPORTED_TYPES.contains(left.getTypeId())) {
      return false;
    }

    if (!SUPPORTED_TYPES.contains(right.getTypeId())) {
      return false;
    }

    // Restriction of the current version: type and size should match.
    final boolean equalSize = left.getBitSize() == right.getBitSize();

    if (!equalSize) {
      return false;
    }

    final boolean equalType = left.getTypeId() == right.getTypeId();

    if (!equalType) {
      final boolean integerTypes =
        (left.getTypeId() == TypeId.INT && right.getTypeId() == TypeId.CARD) ||
        (left.getTypeId() == TypeId.CARD && right.getTypeId() == TypeId.INT);

      if (!integerTypes) {
        return false;
      }
    }

    return true;
  }
}
