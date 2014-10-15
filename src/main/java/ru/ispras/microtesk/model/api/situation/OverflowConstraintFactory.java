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

package ru.ispras.microtesk.model.api.situation;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.model.api.situation.IConstraintFactory;

public abstract class OverflowConstraintFactory implements IConstraintFactory {
  public static final int BIT_VECTOR_LENGTH = 64;

  public static final DataType BIT_VECTOR_TYPE;
  public static final NodeValue INT_ZERO;
  public static final NodeValue INT_BASE_SIZE;
  public static final NodeOperation INT_SIGN_MASK;

  static {
    BIT_VECTOR_TYPE = DataType.BIT_VECTOR(BIT_VECTOR_LENGTH);
    INT_ZERO = new NodeValue(Data.newBitVector(0, BIT_VECTOR_LENGTH));
    INT_BASE_SIZE = new NodeValue(Data.newBitVector(32, BIT_VECTOR_LENGTH));

    INT_SIGN_MASK = new NodeOperation(
      StandardOperation.BVLSHL,
      new NodeOperation(StandardOperation.BVNOT, INT_ZERO),
      INT_BASE_SIZE
      );
  }

  protected static NodeOperation IsValidPos(Node arg) {
    return new NodeOperation(
      StandardOperation.EQ,
      new NodeOperation(StandardOperation.BVAND, arg, INT_SIGN_MASK),
      INT_ZERO
      );
  }

  protected static NodeOperation IsValidNeg(Node arg) {
    return new NodeOperation(
      StandardOperation.EQ,
      new NodeOperation(StandardOperation.BVAND, arg, INT_SIGN_MASK),
      INT_SIGN_MASK
      );
  }

  protected static Node IsValidSignedInt(Node arg) {
    return Node.OR(IsValidPos(arg), IsValidNeg(arg));
  }

  protected static Node isNotEqual(Node left, Node right) {
    return isNot(new NodeOperation(StandardOperation.EQ, left, right));
  }

  protected static Node isNot(Node expr) {
    return Node.NOT(expr);
  }
}
