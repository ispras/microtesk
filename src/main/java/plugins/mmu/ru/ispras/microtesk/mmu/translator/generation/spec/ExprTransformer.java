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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * Class {@link ExprTransformer} transforms operations that involve shifts and 
 * bit masks into concatenation-based expressions. Supported operations include:
 * <ol>
 * <li>{@link StandardOperation.BVZEROEXT}
 * <li>{@link StandardOperation.BVLSHL}
 * <li>{@link StandardOperation.BVASHL}
 * <li>{@link StandardOperation.BVLSHR}
 * </ol>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class ExprTransformer {
  private final NodeTransformer transformer;

  public ExprTransformer() {
    this.transformer = new NodeTransformer();
    this.transformer.addRule(StandardOperation.BVZEROEXT, new ZeroExtendRule());

    final LeftShiftRule leftShiftRule = new LeftShiftRule();
    this.transformer.addRule(StandardOperation.BVLSHL, leftShiftRule);
    this.transformer.addRule(StandardOperation.BVASHL, leftShiftRule);

    this.transformer.addRule(StandardOperation.BVLSHR, new RightShiftRule());
    this.transformer.addRule(StandardOperation.BVEXTRACT, new NestedFieldRule());

    final BitMaskRule bitMaskRule = new BitMaskRule();
    this.transformer.addRule(StandardOperation.BVAND, bitMaskRule);
    this.transformer.addRule(StandardOperation.BVOR, bitMaskRule);
  }

  public Node transform(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    transformer.walk(expr);
    final Node result = transformer.getResult().iterator().next();

    transformer.reset();
    return result;
  }

  private static final class ZeroExtendRule implements TransformerRule {
    @Override
    public boolean isApplicable(final Node expr) {
      return isOperation(expr, StandardOperation.BVZEROEXT);
    }

    @Override
    public Node apply(final Node expr) {
      final NodeOperation op = (NodeOperation) expr;

      final Node sizeExpr = op.getOperand(0);
      final Node argExpr = op.getOperand(1);

      final int newSize = FortressUtils.extractInt(sizeExpr);
      final int oldSize = argExpr.getDataType().getSize();

      final int deltaSize = newSize - oldSize;
      InvariantChecks.checkGreaterThanZero(deltaSize);

      return new NodeOperation(
          StandardOperation.BVCONCAT,
          NodeValue.newBitVector(BitVector.newEmpty(deltaSize)),
          argExpr
          );
    }
  }

  private static final class LeftShiftRule implements TransformerRule {
    @Override
    public boolean isApplicable(final Node expr) {
      if (!isOperation(expr, StandardOperation.BVLSHL) &&
          !isOperation(expr, StandardOperation.BVASHL)) {
        return false;
      }

      final Node shiftAmount = ((NodeOperation) expr).getOperand(1);
      return shiftAmount.getKind() == Node.Kind.VALUE;
    }

    @Override
    public Node apply(final Node expr) {
      final NodeOperation op = (NodeOperation) expr;

      final Node argExpr = op.getOperand(0);
      final Node shiftAmountExpr = op.getOperand(1);

      final int argSize = argExpr.getDataType().getSize();
      final int shiftAmount = FortressUtils.extractInt(shiftAmountExpr) % argSize;

      if (shiftAmount == 0) {
        return argExpr;
      }

      final Node fieldExpr =
          newField(argExpr, 0, argSize - 1 - shiftAmount);

      return new NodeOperation(
          StandardOperation.BVCONCAT,
          fieldExpr,
          NodeValue.newBitVector(BitVector.newEmpty(shiftAmount))
          );
    }
  }

  private static final class RightShiftRule implements TransformerRule {
    @Override
    public boolean isApplicable(final Node expr) {
      if (!isOperation(expr, StandardOperation.BVLSHR)) {
        return false;
      }

      final Node shiftAmount = ((NodeOperation) expr).getOperand(1);
      return shiftAmount.getKind() == Node.Kind.VALUE;
    }

    @Override
    public Node apply(final Node expr) {
      final NodeOperation op = (NodeOperation) expr;

      final Node argExpr = op.getOperand(0);
      final Node shiftAmountExpr = op.getOperand(1);

      final int argSize = argExpr.getDataType().getSize();
      final int shiftAmount = FortressUtils.extractInt(shiftAmountExpr) % argSize;

      if (shiftAmount == 0) {
        return argExpr;
      }

      final Node fieldExpr =
          newField(argExpr, shiftAmount, argSize - 1);

      return new NodeOperation(
          StandardOperation.BVCONCAT,
          NodeValue.newBitVector(BitVector.newEmpty(shiftAmount)),
          fieldExpr
          );
    }
  }

  private static final class NestedFieldRule implements TransformerRule {
    @Override
    public boolean isApplicable(final Node expr) {
      if (!isOperation(expr, StandardOperation.BVEXTRACT)) {
        return false;
      }

      final NodeOperation op = (NodeOperation) expr;
      if (op.getOperandCount() < 3) {
        return false;
      }

      return isOperation(
          op.getOperand(2), StandardOperation.BVEXTRACT);
    }

    @Override
    public Node apply(final Node expr) {
      final NodeOperation op = (NodeOperation) expr;
      final NodeOperation nestedOp = (NodeOperation) op.getOperand(2);
      final Node target = nestedOp.getOperand(2);

      final int from = FortressUtils.extractInt(op.getOperand(0));
      final int to = FortressUtils.extractInt(op.getOperand(1));

      final int innerFrom = FortressUtils.extractInt(nestedOp.getOperand(0));
      final int innerTo = FortressUtils.extractInt(nestedOp.getOperand(1));

      final int realFrom = Math.min(from, to);
      final int realTo = Math.max(from, to);

      final int realInnerFrom = Math.min(innerFrom, innerTo);
      final int realInnerTo = Math.max(innerFrom, innerTo);

      final int newFrom = realInnerFrom + realFrom;
      final int newTo = newFrom + realTo;

      InvariantChecks.checkTrue(realInnerFrom <= newFrom && newFrom <= realInnerTo);
      InvariantChecks.checkTrue(realInnerFrom <= newTo && newTo <= realInnerTo);

      return newField(target, newFrom, newTo);
    }
  }

  private static final class BitMaskRule implements TransformerRule {
    @Override
    public boolean isApplicable(final Node expr) {
      if (!isOperation(expr, StandardOperation.BVOR) && 
          !isOperation(expr, StandardOperation.BVAND)) {
        return false;
      }

      final NodeOperation op = (NodeOperation) expr;
      if (op.getOperandCount() < 2) {
        return false;
      }

      final Node operand1 = op.getOperand(0);
      final Node operand2 = op.getOperand(1);

      return ((isValue(operand1) && isVariable(operand2)) ||
              (isValue(operand2) && isVariable(operand1)));
    }

    @Override
    public Node apply(final Node expr) {
      final NodeOperation op = (NodeOperation) expr;
      final Node operand1 = op.getOperand(0);
      final Node operand2 = op.getOperand(1);

      final NodeValue mask;
      final NodeVariable variable;

      if (isValue(operand1) && isVariable(operand2)) {
        mask = (NodeValue) operand1;
        variable = (NodeVariable) operand2;
      } else if (isValue(operand2) && isVariable(operand1)) {
        mask = (NodeValue) operand2;
        variable = (NodeVariable) operand1;
      } else {
        throw new IllegalArgumentException();
      }

      InvariantChecks.checkTrue(mask.isType(DataTypeId.BIT_VECTOR));
      InvariantChecks.checkTrue(variable.isType(DataTypeId.BIT_VECTOR));

      InvariantChecks.checkTrue(
          mask.getDataType().getSize() ==
          variable.getDataType().getSize());

      final BitVector maskValue = mask.getData().getBitVector();
      if (op.getOperationId() == StandardOperation.BVAND) {
        return applyAndMask(variable, maskValue);
      } else if (op.getOperationId() == StandardOperation.BVOR) {
        return applyOrMask(variable, maskValue);
      } else {
        throw new IllegalArgumentException();
      }
    }

    private Node applyAndMask(final NodeVariable variable, final BitVector mask) {
      int fieldEnds = mask.getBitSize() - 1;
      boolean isFieldBitSet = mask.getBit(fieldEnds);

      final List<Node> fields = new ArrayList<>();
      for (int bitIndex = mask.getBitSize() - 2; bitIndex >= 0; bitIndex--) {
        final boolean isBitSet = mask.getBit(bitIndex);

        if (isBitSet == isFieldBitSet) {
          continue;
        }

        if (isBitSet) {
          fields.add(NodeValue.newBitVector(BitVector.newEmpty(fieldEnds - bitIndex)));
        } else {
          fields.add(newField(variable, bitIndex + 1, fieldEnds));
        }

        fieldEnds = bitIndex;
        isFieldBitSet = isBitSet;
      }

      if (isFieldBitSet) {
        fields.add(newField(variable, 0, fieldEnds));
      } else {
        fields.add(NodeValue.newBitVector(BitVector.newEmpty(fieldEnds + 1)));
      }

      return newConcat(fields);
    }

    private Node applyOrMask(final NodeVariable variable, final BitVector mask) {
      int fieldEnds = mask.getBitSize() - 1;
      boolean isFieldBitSet = mask.getBit(fieldEnds);

      final List<Node> fields = new ArrayList<>();
      for (int bitIndex = mask.getBitSize() - 2; bitIndex >= 0; bitIndex--) {
        final boolean isBitSet = mask.getBit(bitIndex);

        if (isBitSet == isFieldBitSet) {
          continue;
        }

        if (isBitSet) {
          fields.add(newField(variable, bitIndex + 1, fieldEnds));
        } else {
          final BitVector value = BitVector.newEmpty(fieldEnds - bitIndex);
          value.setAll();
          fields.add(NodeValue.newBitVector(value));
        }

        fieldEnds = bitIndex;
        isFieldBitSet = isBitSet;
      }

      if (isFieldBitSet) {
        final BitVector value = BitVector.newEmpty(fieldEnds + 1);
        value.setAll();
        fields.add(NodeValue.newBitVector(value));
      } else {
        fields.add(newField(variable, 0, fieldEnds));
      }

      return newConcat(fields);
    }
  }

  private static boolean isOperation(final Node node, final Enum<?> opId) {
    if (Node.Kind.OPERATION != node.getKind()) {
      return false;
    }

    final NodeOperation op = (NodeOperation) node;
    return op.getOperationId() == opId;
  }

  private static boolean isValue(final Node node) {
    return Node.Kind.VALUE == node.getKind();
  }

  private static boolean isVariable(final Node node) {
    return Node.Kind.VARIABLE == node.getKind();
  }

  private static Node newField(final Node expr, final int from, final int to) {
    if (expr.getDataType().getSize() == to - from + 1) {
      return expr;
    }

    return new NodeOperation(
        StandardOperation.BVEXTRACT,
        NodeValue.newInteger(to),
        NodeValue.newInteger(from),
        expr
        );
  }

  private static Node newConcat(final List<? extends Node> fields) {
    if (fields.size() == 1) {
      return fields.get(0);
    }

    return new NodeOperation(StandardOperation.BVCONCAT, fields);
  }
}
