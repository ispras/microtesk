/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.utils;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.Reducer;
import ru.ispras.fortress.transformer.ValueProvider;
import ru.ispras.fortress.util.InvariantChecks;

public final class FortressUtils {
  private FortressUtils() {}

  public static int extractInt(final Node expr) {
    checkConstantValue(expr);
    final NodeValue value = (NodeValue) expr;

    switch (value.getDataTypeId()) {
      case LOGIC_INTEGER:
        return value.getInteger().intValue();

      case BIT_VECTOR:
        return value.getBitVector().intValue();

      default:
        throw new IllegalStateException(String.format("%s cannot be converted to int", value));
    }
  }

  public static BitVector extractBitVector(final Node expr) {
    checkConstantValue(expr);
    return ((NodeValue) expr).getBitVector();
  }

  private static void checkConstantValue(final Node expr) {
    checkNotNull(expr);
    if (expr.getKind() != Node.Kind.VALUE) {
      throw new IllegalStateException(String.format("%s is not a constant value.", expr));
    }
  }

  public static Node makeNodeInteger(final int value) {
    return new NodeValue(Data.newInteger(value));
  }

  public static Node makeNodeInteger(final BigInteger value) {
    return new NodeValue(Data.newInteger(value));
  }

  public static Node makeNodeBitVector(final BitVector value) {
    return new NodeValue(Data.newBitVector(value));
  }

  public static Node makeNodeVariable(final Variable variable) {
    return new NodeVariable(variable);
  }

  public static Node makeNodeExtract(final Variable variable, final int lower, final int upper) {
    return new NodeOperation(
        StandardOperation.BVEXTRACT,
        makeNodeInteger(upper),
        makeNodeInteger(lower),
        makeNodeVariable(variable));
  }

  public static Node makeNodeExtract(final Variable variable) {
    return makeNodeExtract(variable, 0, variable.getType().getSize() - 1);
  }

  public static Node makeNodeExtract(final Variable variable, final int bit) {
    return makeNodeExtract(variable, bit, bit);
  }

  public static Node makeNodeConcat(final List<Node> operands) {
    return new NodeOperation(StandardOperation.BVCONCAT, operands);
  }

  public static Node makeNodeReverseConcat(final Node... operands) {
    final List<Node> reversedOperands = Arrays.<Node>asList(operands);
    Collections.reverse(reversedOperands);

    return makeNodeConcat(reversedOperands);
  }

  public static Node makeNodeEqual(final Node lhs, final Node rhs) {
    return new NodeOperation(StandardOperation.EQ, lhs, rhs);
  }

  public static Node makeNodeNotEqual(final Node lhs, final Node rhs) {
    return new NodeOperation(StandardOperation.NOTEQ, lhs, rhs);
  }

  public static Node makeNodeAnd(final List<Node> operands) {
    return new NodeOperation(StandardOperation.AND, operands);
  }

  public static Node makeNodeAnd(final Node... operands) {
    return makeNodeAnd(Arrays.<Node>asList(operands));
  }

  public static Node makeNodeOr(final List<Node> operands) {
    return new NodeOperation(StandardOperation.OR, operands);
  }

  public static Node makeNodeOr(final Node... operands) {
    return makeNodeOr(Arrays.<Node>asList(operands));
  }

  public static Variable getVariable(final Node node) {
    switch (node.getKind()) {
      case VARIABLE:
        final NodeVariable variable = (NodeVariable) node;
        return variable.getVariable();
      case OPERATION:
        final NodeOperation operation = (NodeOperation) node;
        InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.BVEXTRACT);

        final NodeVariable vector = (NodeVariable) operation.getOperand(2);
        return vector.getVariable();
      default:
        return null;
    }
  }

  public static int getLowerBit(final Node node) {
    switch (node.getKind()) {
      case VARIABLE:
        return 0;
      case OPERATION:
        final NodeOperation operation = (NodeOperation) node;
        InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.BVEXTRACT);

        final NodeValue lowerBitValue = (NodeValue) operation.getOperand(1);
        return lowerBitValue.getInteger().intValue();
      default:
        InvariantChecks.checkTrue(false);
        return -1;
    }
  }

  public static int getUpperBit(final Node node) {
    switch (node.getKind()) {
      case VARIABLE:
        final NodeVariable variable = (NodeVariable) node;
        return variable.getVariable().getType().getSize() - 1;
      case OPERATION:
        final NodeOperation operation = (NodeOperation) node;
        InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.BVEXTRACT);

        final NodeValue lowerBitValue = (NodeValue) operation.getOperand(0);
        return lowerBitValue.getInteger().intValue();
      default:
        InvariantChecks.checkTrue(false);
        return -1;
    }
  }

  public static int getBitSize(final Node node) {
    return (getUpperBit(node) - getLowerBit(node)) + 1;
  }

  public static BigInteger evaluateInteger(final Node node, final ValueProvider valueProvider) {
    final Node result = Reducer.reduce(valueProvider, node);
    InvariantChecks.checkNotNull(result);

    if (result.getKind() != Node.Kind.VALUE) {
      return null;
    }

    final NodeValue value = (NodeValue) result;
    return value.getDataTypeId() == DataTypeId.BIT_VECTOR
        ? value.getBitVector().bigIntegerValue(false)
        : value.getInteger();
  }

  public static BigInteger evaluateInteger(final Node node) {
    final Node result = Reducer.reduce(node);
    InvariantChecks.checkNotNull(result);

    if (result.getKind() != Node.Kind.VALUE) {
      return null;
    }

    final NodeValue value = (NodeValue) result;
    return value.getDataTypeId() == DataTypeId.BIT_VECTOR
        ? value.getBitVector().bigIntegerValue(false)
        : value.getInteger();
  }

  public static BitVector evaluateBitVector(final Node node, final ValueProvider valueProvider) {
    final Node result = Reducer.reduce(valueProvider, node);
    InvariantChecks.checkNotNull(result);

    if (result.getKind() != Node.Kind.VALUE) {
      return null;
    }

    final NodeValue value = (NodeValue) result;
    return value.getBitVector();
  }

  public static BitVector evaluateBitVector(final Node node) {
    final Node result = Reducer.reduce(node);
    InvariantChecks.checkNotNull(result);

    if (result.getKind() != Node.Kind.VALUE) {
      return null;
    }

    final NodeValue value = (NodeValue) result;
    return value.getBitVector();
  }

  public static Boolean check(final Node node, final ValueProvider valueProvider) {
    final Node result = Reducer.reduce(valueProvider, node);
    InvariantChecks.checkNotNull(result);

    if (result.getKind() != Node.Kind.VALUE) {
      return null;
    }

    final NodeValue value = (NodeValue) result;
    return value.getBoolean();
  }

  public static Boolean check(final Node node) {
    final Node result = Reducer.reduce(node);
    InvariantChecks.checkNotNull(result);

    if (result.getKind() != Node.Kind.VALUE) {
      return null;
    }

    final NodeValue value = (NodeValue) result;
    return value.getBoolean();
  }
}
