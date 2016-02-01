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

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class TypeCast {
  private TypeCast() {}

  private static final Set<TypeId> BV_BASED_TYPES = EnumSet.of(
      TypeId.INT, TypeId.CARD, TypeId.FLOAT
  );

  private static final TypeId TYPE_CAST_MAP[][] = {
    { null,         TypeId.CARD,  TypeId.INT,   TypeId.BOOL, },
    { TypeId.CARD,  TypeId.CARD,  TypeId.CARD,  null         },
    { TypeId.INT,   TypeId.CARD,  TypeId.INT,   null         },
    { TypeId.BOOL,  null,         null,         TypeId.BOOL  }
  };

  private static final DataTypeId DATA_TYPE_CAST_MAP[][] = {
    { null, DataTypeId.BIT_VECTOR, DataTypeId.LOGIC_INTEGER, DataTypeId.LOGIC_BOOLEAN, },
    { DataTypeId.BIT_VECTOR, DataTypeId.BIT_VECTOR, DataTypeId.BIT_VECTOR, DataTypeId.BIT_VECTOR},
    { DataTypeId.LOGIC_INTEGER, DataTypeId.BIT_VECTOR, DataTypeId.LOGIC_INTEGER, null },
    { DataTypeId.LOGIC_BOOLEAN, DataTypeId.BIT_VECTOR, null, DataTypeId.LOGIC_BOOLEAN }
  };

  private static <T> T searchInMatrix(final T[][] matrix, final T left, final T right) {
    InvariantChecks.checkNotNull(matrix);
    InvariantChecks.checkNotNull(left);
    InvariantChecks.checkNotNull(right);

    if (left.equals(right)) {
      return left;
    }

    int col = 0; // left -> col
    for (int columnIndex = 1; columnIndex < matrix[0].length; ++columnIndex) {
      if (matrix[0][columnIndex] == left) {
        col = columnIndex;
        break;
      }
    }

    if (0 == col) { // left is not found
      return null;
    }

    int row = 0; // right -> row
    for (int rowIndex = 1; rowIndex < matrix.length; ++rowIndex) {
      if (matrix[rowIndex][0] == right) {
        row = rowIndex;
        break;
      }
    }

    if (0 == row) { // right is not found
      return null;
    }

    return matrix[col][row];
  }

  public static TypeId getCastTypeId(final TypeId left, final TypeId right) {
    return searchInMatrix(TYPE_CAST_MAP, left, right);
  }

  public static Type getCastType(final Type left, final Type right) {
    InvariantChecks.checkNotNull(left);
    InvariantChecks.checkNotNull(right);

    if (left.equals(right)) {
      return left;
    }

    final TypeId typeId = getCastTypeId(left.getTypeId(), right.getTypeId());
    if (null == typeId) {
      return null;
    }

    final int bitSize = Math.max(left.getBitSize(), right.getBitSize());
    return (typeId == left.getTypeId()) ? left.resize(bitSize) : right.resize(bitSize);
  }

  public static DataTypeId getCastDataTypeId(final DataTypeId left, final DataTypeId right) {
    return searchInMatrix(DATA_TYPE_CAST_MAP, left, right);
  }

  public static DataType getCastDataType(final DataType left, final DataType right) {
    InvariantChecks.checkNotNull(left);
    InvariantChecks.checkNotNull(right);

    if (left.equals(right)) {
      return left;
    }

    final DataTypeId dataTypeId = getCastDataTypeId(left.getTypeId(), right.getTypeId());
    if (null == dataTypeId) {
      return null;
    }

    return DataType.newDataType(
        dataTypeId, Math.max(left.getSize(), right.getSize()));
  }

  public static Expr castConstantTo(final Expr value, final Type type) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(value.isConstant());
    InvariantChecks.checkNotNull(type);

    if (value.isTypeOf(type)) {
      return value;
    }

    final BitVector data = getBitVector(value, type);
    final NodeValue node = type.getTypeId() == TypeId.BOOL ? 
        NodeValue.newBoolean(!data.isAllReset()) :
        NodeValue.newBitVector(data); 

    final Expr expr = new Expr(node);
    expr.setNodeInfo(NodeInfo.newConst(type));

    return expr;
  }

  private static BitVector getBitVector(final Expr value, final Type type) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(value.isConstant());
    InvariantChecks.checkNotNull(type);

    final int bitSize = type.getBitSize();
    final boolean signExtend = value.isTypeOf(TypeId.INT);

    final NodeValue node = (NodeValue) value.getNode();
    switch (node.getDataTypeId()) {
      case BIT_VECTOR:
        return node.getBitVector().resize(bitSize, signExtend);

      case LOGIC_INTEGER:
        return BitVector.valueOf(node.getInteger(), bitSize);

      case LOGIC_BOOLEAN:
        return BitVector.valueOf(node.getBoolean()).resize(bitSize, false);

      default:
        throw new IllegalArgumentException(
            "Unsupported data type: " + node.getDataType());
    }
  }

  public static Node castConstantTo(final Node value, final DataType type) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(value.getKind() == Node.Kind.VALUE);
    InvariantChecks.checkNotNull(type);

    final DataType valueType = value.getDataType();
    if (type.equals(valueType)) {
      return value; 
    }

    if (type.getTypeId() == DataTypeId.BIT_VECTOR) {
      final NodeValue valueNode = (NodeValue) value;

      switch (valueType.getTypeId()) {
        case LOGIC_INTEGER:
          return NodeValue.newBitVector(
              BitVector.valueOf(valueNode.getInteger(), type.getSize()));

        case LOGIC_BOOLEAN:
          return NodeValue.newBitVector(
              BitVector.valueOf(valueNode.getBoolean()).resize(type.getSize(), false));

        case BIT_VECTOR:
          return NodeValue.newBitVector(
              valueNode.getBitVector().resize(type.getSize(), false));
      }
    }

    throw new IllegalArgumentException(String.format(
        "Сoercion from %s to %s is unsupported.", valueType, type));
  }

  public static DataType getFortressDataType(final Type type) {
    InvariantChecks.checkNotNull(type);

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
