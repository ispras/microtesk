package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.TransformerRule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class IntegerCast {
  public static Map<Enum<?>, TransformerRule> rules() {
    final TransformerRule singleOperandType = new TransformerRule() {
      @Override
      public boolean isApplicable(Node node) {
        return getIntegerOperandIndex(node, 0) >= 0 &&
               getBitVectorOperandIndex(node, 0) >= 0;
      }

      @Override
      public Node apply(Node node) {
        final NodeOperation op = (NodeOperation) node;
        final DataType type =
            op.getOperand(getBitVectorOperandIndex(op, 0)).getDataType();

        int index = -1;
        final List<Node> operands = new ArrayList<>(op.getOperands());
        while ((index = getIntegerOperandIndex(op, index + 1)) >= 0) {
          operands.set(index, cast(operands.get(index), type));
        }
        return Expression.newOperation(op.getOperationId(), operands);
      }
    };

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
        if (nodeIsBitVector(op.getOperand(i))) {
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
