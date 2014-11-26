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
import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.model.api.type.Type;

public final class ArithmUnary implements IUnaryOperator {
  // Sim-nML spec: these operators (unary +,-) are used only for
  // INT, FLOAT and FIX data types.

  private final static Set<TypeId> SUPPORTED_TYPES = EnumSet.of(
    TypeId.INT
    // , ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
    // , ETypeID.FIX // NOT SUPPORTED IN THIS VERSION
  );

  private final BitVectorMath.Operations op;

  public ArithmUnary(BitVectorMath.Operations op) {
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
  public boolean supports(Type argType) {
    return SUPPORTED_TYPES.contains(argType.getTypeId());
  }
}
