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
import ru.ispras.microtesk.model.api.data.IUnaryOperator;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

public final class BitUnary implements IUnaryOperator {
  private final static Set<TypeId> SUPPORTED_TYPES = EnumSet.of(TypeId.INT, TypeId.CARD);

  private final BitVectorMath.Operations op;

  public BitUnary(BitVectorMath.Operations op) {
    if (null == op) {
      throw new NullPointerException();
    }

    if (op.getOperands() != BitVectorMath.Operands.UNARY) {
      throw new IllegalArgumentException();
    }

    this.op = op;
  }

  @Override
  public Data execute(Data data) {
    final BitVector result = op.execute(data.getRawData());
    return new Data(result, data.getType());
  }

  @Override
  public boolean supports(Type type) {
    return SUPPORTED_TYPES.contains(type.getTypeId());
  }
}
