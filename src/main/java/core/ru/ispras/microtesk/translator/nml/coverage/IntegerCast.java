/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import static ru.ispras.microtesk.translator.nml.coverage.Utility.nodeIsOperation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.TransformerRule;

public final class IntegerCast {
  public static Map<Enum<?>, TransformerRule> rules() {
    final TransformerRule singleOperandType = new CommonBitVectorTypeRule();

    final TransformerRule arrayKeyRule =
        new DependentOperandRule(0, new AttributeTypeGetter(0), 1);

    final Map<Enum<?>, TransformerRule> rules = new IdentityHashMap<>();
    final Enum<?>[] singleTypeOperations = {
      StandardOperation.EQ,
      StandardOperation.NOTEQ,

      StandardOperation.BVADD,
      StandardOperation.BVSUB,
      StandardOperation.BVMUL,
      StandardOperation.BVAND,
      StandardOperation.BVOR,
      StandardOperation.BVUREM,
      StandardOperation.BVSREM,
      StandardOperation.BVSMOD,
      StandardOperation.BVLSHL,
      StandardOperation.BVASHL,
      StandardOperation.BVLSHR,
      StandardOperation.BVASHR,

      StandardOperation.BVULE,
      StandardOperation.BVULT,
      StandardOperation.BVUGE,
      StandardOperation.BVUGT,
      StandardOperation.BVSLE,
      StandardOperation.BVSLT,
      StandardOperation.BVSGE,
      StandardOperation.BVSGT
    };

    for (Enum<?> opId : singleTypeOperations) {
      rules.put(opId, singleOperandType);
    }

    rules.put(StandardOperation.SELECT, arrayKeyRule);
    rules.put(StandardOperation.STORE, arrayKeyRule);

    final Map<Enum<?>, Enum<?>> operationsToCast = new IdentityHashMap<>();
    operationsToCast.put(StandardOperation.ADD, StandardOperation.BVADD);
    operationsToCast.put(StandardOperation.SUB, StandardOperation.BVSUB);

    final TransformerRule castOperations =
        new CastOperationRule(operationsToCast, singleOperandType);
    for (final Enum<?> id : operationsToCast.keySet()) {
      rules.put(id, castOperations);
    }
    rules.put(StandardOperation.ITE, new IteRule());

    return rules;
  }

  public static Node cast(Node origin, DataType type) {
    final DataType nodeType = origin.getDataType();
    if (nodeType.equals(type)) {
      return origin;
    }
    if (nodeIsInteger(origin)) {
      return castInteger(origin, type);
    }
    if (nodeType.getSize() < type.getSize()) {
      return extendBitVector(origin, type);
    }
    return origin;
  }

  private static Node castInteger(Node origin, DataType type) {
    final BigInteger integer = ((NodeValue) origin).getInteger();
    return NodeValue.newBitVector(BitVector.valueOf(integer, type.getSize()));
  }

  private static Node extendBitVector(Node origin, DataType type) {
    final int extsize = type.getSize() - origin.getDataType().getSize();
    return new NodeOperation(StandardOperation.BVZEROEXT,
                             NodeValue.newInteger(extsize),
                             origin);
  }

  public static int getIntegerOperandIndex(Node node, int start) {
    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      for (int i = start; i < op.getOperandCount(); ++i) {
        if (nodeIsInteger(op.getOperand(i))) {
          return i;
        }
      }
    }
    return -1;
  }

  public static int getBitVectorOperandIndex(Node node, int start) {
    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      for (int i = start; i < op.getOperandCount(); ++i) {
        if (op.getOperand(i).getDataTypeId().equals(DataTypeId.BIT_VECTOR)) {
          return i;
        }
      }
    }
    return -1;
  }

  public static boolean nodeIsInteger(Node node) {
    return node.getKind() == Node.Kind.VALUE &&
           node.getDataType() == DataType.INTEGER;
  }

  public static boolean nodeIsBitVector(Node node) {
    return (node.getKind() == Node.Kind.VALUE || node.getKind() == Node.Kind.VARIABLE) &&
           node.getDataType().getTypeId() == DataTypeId.BIT_VECTOR;
  }

  public static DataType findCommonType(final Collection<? extends Node> nodes) {
    DataType common = DataType.UNKNOWN;
    for (final Node node : nodes) {
      final DataType current = node.getDataType();
      if (compare(current, common) > 0) {
        common = current;
      }
    }
    return common;
  }

  public static int compare(final DataType lhs, final DataType rhs) {
    final List<DataType> priorities = Arrays.asList( 
        DataType.UNKNOWN,
        DataType.BOOLEAN,
        DataType.INTEGER,
        DataType.REAL
    );
    if (lhs.getSize() != rhs.getSize()) {
      return lhs.getSize() - rhs.getSize();
    }
    if (lhs.equals(rhs)) {
      return 0;
    }
    return priorities.indexOf(lhs) - priorities.indexOf(rhs);
  }

  public static boolean hasTypeMismatch(final Collection<? extends Node> nodes) {
    if (nodes.isEmpty()) {
      return false;
    }
    final Iterator<? extends Node> it = nodes.iterator();
    final DataType type = it.next().getDataType();
    while (it.hasNext()) {
      if (!it.next().getDataType().equals(type)) {
        return true;
      }
    }
    return false;
  }
}

interface TypeGetter {
  DataType getType(Node node);
}

final class AttributeTypeGetter implements TypeGetter {
  final int attrIndex;

  public AttributeTypeGetter(int index) {
    this.attrIndex = index;
  }

  @Override
  public DataType getType(Node node) {
    return (DataType) node.getDataType().getParameters()[this.attrIndex];
  }
}

final class CastOperationRule implements TransformerRule {
  private final Map<Enum<?>, Enum<?>> operations;
  private final TransformerRule castRule;

  CastOperationRule(final Map<Enum<?>, Enum<?>> operations,
                    final TransformerRule castRule) {
    this.operations = operations;
    this.castRule = castRule;
  }

  @Override
  public boolean isApplicable(final Node node) {
    for (final Enum<?> id : operations.keySet()) {
      if (nodeIsOperation(node, id)) {
        return castRule.isApplicable(node);
      }
    }
    return false;
  }

  @Override
  public Node apply(final Node node) {
    final NodeOperation op = (NodeOperation) castRule.apply(node);
    return new NodeOperation(operations.get(op.getOperationId()), op.getOperands());
  }
}

class CommonBitVectorTypeRule implements TransformerRule {
  @Override
  public boolean isApplicable(Node node) {
    return IntegerCast.getBitVectorOperandIndex(node, 0) >= 0 &&
           nodeHasTypeMismatch(node);
  }

  @Override
  public Node apply(Node node) {
    final NodeOperation op = (NodeOperation) node;
    final DataType type = IntegerCast.findCommonType(op.getOperands());

    final List<Node> operands = new ArrayList<>(op.getOperandCount());
    for (final Node operand : op.getOperands()) {
      operands.add(IntegerCast.cast(operand, type));
    }
    return new NodeOperation(op.getOperationId(), operands);
  }

  private static boolean nodeHasTypeMismatch(final Node input) {
    if (input.getKind() != Node.Kind.OPERATION) {
      return false;
    }
    return IntegerCast.hasTypeMismatch(((NodeOperation) input).getOperands());
  }

}

final class DependentOperandRule implements TransformerRule {
  private final int domIndex;
  private final TypeGetter domGetter;
  private final int[] indices;

  public DependentOperandRule(int domIndex, TypeGetter domGetter, int ... indices) {
    this.domIndex = domIndex;
    this.domGetter = domGetter;
    this.indices = indices;
  }

  @Override
  public boolean isApplicable(Node node) {
    final NodeOperation op = (NodeOperation) node;
    final DataType type = domGetter.getType(op.getOperand(domIndex));
    for (int index : indices) {
      if (!type.equals(op.getOperand(index).getDataType())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Node apply(Node node) {
    final NodeOperation op = (NodeOperation) node;
    final DataType type = domGetter.getType(op.getOperand(domIndex));
    final List<Node> operands = new ArrayList<>(op.getOperands());
    for (int index : indices) {
      operands.set(index, IntegerCast.cast(operands.get(index), type));
    }
    return Expression.newOperation(op.getOperationId(), operands);
  }
}

final class IteRule implements TransformerRule {
  @Override
  public boolean isApplicable(final Node node) {
    if (nodeIsOperation(node, StandardOperation.ITE) &&
           IntegerCast.getBitVectorOperandIndex(node, 1) >= 1) {
      final List<Node> operands = ((NodeOperation) node).getOperands();
      return IntegerCast.hasTypeMismatch(operands.subList(1, operands.size()));
    }
    return false;
  }

  @Override
  public Node apply(final Node node) {
    final NodeOperation ite = (NodeOperation) node;
    final List<Node> source = ite.getOperands().subList(1, ite.getOperandCount());
    final DataType type = IntegerCast.findCommonType(source);
    final List<Node> operands = new ArrayList<>(ite.getOperandCount());
    operands.add(ite.getOperand(0));
    for (final Node operand : source) {
      operands.add(IntegerCast.cast(operand, type));
    }
    return new NodeOperation(StandardOperation.ITE, operands);
  }
}
