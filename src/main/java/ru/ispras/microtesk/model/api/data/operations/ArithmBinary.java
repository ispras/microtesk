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

/*
 * TODO: This implementation should be reviewed and rewritten in accordance with the Sim-nML
 * specification (see Kanpur theses).
 * 
 * The current implementation of ArithmPlus has the following restrictions:
 * 
 * 1. Operands are assumed to have the same type (and size). 2. In the case of overflow (when we are
 * working with two big values) data is truncated. According to Sim-nML specification, the resulting
 * data might be extended with additional bits.
 */

public final class ArithmBinary implements IBinaryOperator {
  private final static Set<TypeId> SUPPORTED_TYPES = EnumSet.of(TypeId.INT, TypeId.CARD
// , ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
// , ETypeID.FIX // NOT SUPPORTED IN THIS VERSION
  );

  private final BitVectorMath.Operations op;

  public ArithmBinary(BitVectorMath.Operations op) {
    if (null == op) {
      throw new NullPointerException();
    }

    if (op.getOperands() != BitVectorMath.Operands.BINARY) {
      throw new IllegalArgumentException();
    }

    this.op = op;
  }

  private static Type getResultType(Type left, Type right) {
    // result type is INT if one of the parameters is INT.

    if (right.getTypeId() == TypeId.INT) {
      return right;
    }

    return left;
  }

  @Override
  public final Data execute(Data left, Data right) {
    final Type resultType = getResultType(left.getType(), right.getType());
    final BitVector result = op.execute(left.getRawData(), right.getRawData());

    return new Data(result, resultType);
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
    if (left.getBitSize() != right.getBitSize()) {
      return false;
    }

    return true;
  }
}
