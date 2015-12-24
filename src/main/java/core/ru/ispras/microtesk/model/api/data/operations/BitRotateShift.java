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

package ru.ispras.microtesk.model.api.data.operations;

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.data.TypeId;

public final class BitRotateShift implements IBinaryOperator {
  private final static Set<TypeId> SUPPORTED_TYPES = EnumSet.of(TypeId.INT, TypeId.CARD);

  private final BitVectorMath.Operations unsignedOp;
  private final BitVectorMath.Operations signedOp;

  public BitRotateShift(
      final BitVectorMath.Operations unsignedOp,
      final BitVectorMath.Operations signedOp) {
    InvariantChecks.checkNotNull(unsignedOp);
    InvariantChecks.checkTrue(unsignedOp.getOperands() == BitVectorMath.Operands.BINARY);

    InvariantChecks.checkNotNull(signedOp);
    InvariantChecks.checkTrue(signedOp.getOperands() == BitVectorMath.Operands.BINARY);

    this.unsignedOp = unsignedOp;
    this.signedOp = signedOp;
  }

  public BitRotateShift(final BitVectorMath.Operations op) {
    this(op, op);
  }

  @Override
  public final Data execute(final Data lhs, final Data rhs) {
    final BitVector result;

    if (lhs.getType().getTypeId() == TypeId.CARD) {
      result = unsignedOp.execute(lhs.getRawData(), rhs.getRawData());
    } else {
      result = signedOp.execute(lhs.getRawData(), rhs.getRawData());
    }

    return new Data(result, lhs.getType());
  }

  @Override
  public final boolean supports(final Type lhs, final Type rhs) {
    if (!SUPPORTED_TYPES.contains(lhs.getTypeId())) {
      return false;
    }

    if (!SUPPORTED_TYPES.contains(rhs.getTypeId())) {
      return false;
    }

    return true;
  }
}
