/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.model.api.type.Type;

public final class BitBinary implements IBinaryOperator {
  private final static Set<TypeId> SUPPORTED_TYPES = EnumSet.of(TypeId.INT, TypeId.CARD);

  private final BitVectorMath.Operations op;

  public BitBinary(BitVectorMath.Operations op) {
    if (null == op) {
      throw new NullPointerException();
    }

    if (op.getOperands() != BitVectorMath.Operands.BINARY) {
      throw new IllegalArgumentException();
    }

    this.op = op;
  }

  private static Type getResultType(Type lhs, Type rhs) {
    // result type is INT if one of the parameters is INT.

    if (rhs.getTypeId() == TypeId.INT) {
      return rhs;
    }

    return lhs;
  }

  @Override
  public final Data execute(Data lhs, Data rhs) {
    final Type resultType = getResultType(lhs.getType(), rhs.getType());
    final BitVector result = op.execute(lhs.getRawData(), rhs.getRawData());

    return new Data(result, resultType);
  }

  @Override
  public boolean supports(Type lhs, Type rhs) {
    if (!SUPPORTED_TYPES.contains(lhs.getTypeId())) {
      return false;
    }

    if (!SUPPORTED_TYPES.contains(rhs.getTypeId())) {
      return false;
    }

    // Restriction of the current version: size should be equal
    if (lhs.getBitSize() != rhs.getBitSize()) {
      return false;
    }

    return true;
  }
}
