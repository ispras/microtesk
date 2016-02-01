/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.errors.UndefinedConstant;

import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class ExprFactory extends WalkerFactoryBase {

  public ExprFactory(final WalkerContext context) {
    super(context);
  }

  public Expr constant(final Where w, final String text, final int radix) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(text);

    final Expr expr = new Expr(NodeValue.newInteger(text, radix));
    expr.setNodeInfo(NodeInfo.newConst(null)); // No nML type is associated

    return expr;
  }

  public Expr namedConstant(final Where w, final String name) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(name);

    if (!getIR().getConstants().containsKey(name)) {
      raiseError(w, new UndefinedConstant(name));
    }

    final LetConstant source = getIR().getConstants().get(name);
    return new Expr(source.getExpr().getNode());
  }

  public Expr location(final Location source) {
    InvariantChecks.checkNotNull(source);

    final NodeInfo nodeInfo = NodeInfo.newLocation(source);

    final String name = source.toString();
    final DataType dataType = TypeCast.getFortressDataType(nodeInfo.getType());

    final Node node = new NodeVariable(name, dataType);
    node.setUserData(nodeInfo);

    return new Expr(node);
  }

  public Expr operator(
      final Where w,
      final String id,
      final Expr... operands) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(operands);

    final Operator op = Operator.forText(id);
    if (null == op) {
      raiseError(w, String.format("The %s operator is not supported.", id));
    }

    if (operands.length != op.getOperandCount()) {
      raiseError(w, String.format("The %s operator requires %d operands.", id, op.getOperandCount()));
    }

    // Unary plus is redundant.
    if (op == Operator.UPLUS) {
      return operands[0];
    }

    final TypeCalculator typeCalculator =
        new TypeCalculator(w, Arrays.asList(operands));

    final List<Node> operandNodes = new ArrayList<>(operands.length);
    for (final Expr operand : operands) {
      final Expr updatedOperand = typeCalculator.enforceCommonType(operand);
      operandNodes.add(updatedOperand.getNode());
    }

    final Node node;
    if (typeCalculator.isAllConstant()) {
      final DataTypeId dataTypeId = typeCalculator.getCommonDataType().getTypeId();
      final Enum<?> operator = op.getFortressOperator(dataTypeId);

      if (null == operator) {
        raiseError(w, String.format("The %s operator is not applicable to %s.", op, dataTypeId));
      }

      final Node operation = new NodeOperation(operator, operandNodes);
      node = Transformer.reduce(operation);

      if (node == operation) {
        raiseError(w, "Failed to calculate the result of a constant expression.");
      }
    } else {
      final TypeId typeId = typeCalculator.getCommonType().getTypeId();
      final Enum<?> operator = op.getFortressOperator(typeId);

      if (null == operator) {
        raiseError(w, String.format("The %s operator is not applicable to %s.", op, typeId));
      }

      node = new NodeOperation(operator, operandNodes);

      final Type resultType = op.isBoolean() ? Type.BOOLEAN : typeCalculator.getCommonType();
      final NodeInfo nodeInfo = NodeInfo.newOperator(op, resultType);
      node.setUserData(nodeInfo);
    }

    return new Expr(node);
  }

  public Expr sqrt(final Where w, final Expr operand) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(operand);

    if (!operand.isTypeOf(TypeId.FLOAT)) {
      raiseError(w, "The sqrt operation supports only the float type.");
    }

    final Type type = operand.getNodeInfo().getType();

    final NodeInfo nodeInfo = NodeInfo.newOperator(Operator.SQRT, type);
    final Node node = new NodeOperation(Operator.SQRT, operand.getNode());

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  public Expr isNan(final Where w, final Expr operand) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(operand);

    if (!operand.isTypeOf(TypeId.FLOAT)) {
      raiseError(w, "The is_nan operation supports only the float type.");
    }

    final NodeInfo nodeInfo = NodeInfo.newOperator(Operator.IS_NAN, Type.BOOLEAN);
    final Node node = new NodeOperation(Operator.IS_NAN, operand.getNode());

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  public Expr isSignalingNan(final Where w, final Expr operand) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(operand);

    if (!operand.isTypeOf(TypeId.FLOAT)) {
      raiseError(w, "The is_signaling_nan operation supports only the float type.");
    }

    final NodeInfo nodeInfo = NodeInfo.newOperator(Operator.IS_SIGN_NAN, Type.BOOLEAN);
    final Node node = new NodeOperation(Operator.IS_SIGN_NAN, operand.getNode());

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  public Expr signExtend(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);
    InvariantChecks.checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return TypeCast.castConstantTo(src, type);
    }

    if (type.getBitSize() < src.getNodeInfo().getType().getBitSize()) {
      raiseError(w, "Size of result type must be >= size of original type.");
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(type, Coercion.SIGN_EXTEND);
    src.setNodeInfo(newNodeInfo);

    return src;
  }

  public Expr zeroExtend(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);
    InvariantChecks.checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return TypeCast.castConstantTo(src, type);
    }

    if (type.getBitSize() < src.getNodeInfo().getType().getBitSize()) {
      raiseError(w, "Size of result type must be >= size of original type.");
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(type, Coercion.ZERO_EXTEND);
    src.setNodeInfo(newNodeInfo);

    return src;
  }

  public Expr coerce(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);
    InvariantChecks.checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return TypeCast.castConstantTo(src, type);
    }

    if (!type.getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only integer types are supported.",
          type.getTypeName()));
    }

    if (!src.getNodeInfo().getType().getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only integer types are supported.",
          src.getNodeInfo().getType().getTypeName()));
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(type, Coercion.COERCE);
    src.setNodeInfo(newNodeInfo);

    return src;
  }

  public Expr cast(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);
    InvariantChecks.checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return TypeCast.castConstantTo(src, type);
    }

    if (src.getNodeInfo().getType().getBitSize() != type.getBitSize()) {
      raiseError(w, "cast does not allow changing data size.");
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(type, Coercion.CAST);
    src.setNodeInfo(newNodeInfo);

    return src;
  }

  public Expr int_to_float(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);
    InvariantChecks.checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      raiseError(w, "int_to_float is not supported for constant expressions.");
    }

    if (!src.getNodeInfo().getType().getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only integer types are supported.",
          src.getNodeInfo().getType().getTypeName()));
    }

    if (src.getNodeInfo().getType().getBitSize() != 32 &&
        src.getNodeInfo().getType().getBitSize() != 64) {
      raiseError(w, "Only 32 and 64-bit integers are supported.");
    }

    if (type.getTypeId() != TypeId.FLOAT) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only float is supported.",
          type.getTypeName()));
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(type, Coercion.INT_TO_FLOAT);
    src.setNodeInfo(newNodeInfo);

    return src;
  }

  public Expr float_to_int(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);
    InvariantChecks.checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      raiseError(w, "float_to_int is not supported for constant expressions.");
    }

    if (!src.isTypeOf(TypeId.FLOAT)) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only float is supported.",
          src.getNodeInfo().getType().getTypeName()));
    }

    if (!type.getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only integer types are supported.",
          type.getTypeName()));
    }

    if (type.getBitSize() != 32 && type.getBitSize() != 64) {
      raiseError(w, "Only 32 and 64-bit integers are supported.");
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(type, Coercion.FLOAT_TO_INT);
    src.setNodeInfo(newNodeInfo);

    return src;
  }

  public Expr float_to_float(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);
    InvariantChecks.checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      raiseError(w, "float_to_float is not supported for constant expressions.");
    }

    if (!src.isTypeOf(TypeId.FLOAT)) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only float is supported.",
          src.getNodeInfo().getType().getTypeName()));
    }

    if (type.getTypeId() != TypeId.FLOAT) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only float is supported.",
          type.getTypeName()));
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(type, Coercion.FLOAT_TO_FLOAT);
    src.setNodeInfo(newNodeInfo);

    return src;
  }

  public Expr condition(final Where w, final List<Pair<Expr, Expr>> blocks) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(blocks);
    InvariantChecks.checkTrue(blocks.size() >= 2);

    final List<Expr> values = new ArrayList<>(blocks.size());
    for (final Pair<Expr, Expr> block : blocks) {
      values.add(block.second);
    }

    final TypeCalculator typeCalculator = new TypeCalculator(w, values);

    final Pair<Expr, Expr> elseBlock = blocks.get(blocks.size() - 1);
    InvariantChecks.checkTrue(elseBlock.first.getNode().equals(NodeValue.newBoolean(true)));

    Expr result = elseBlock.second;

    for (int index = blocks.size() - 2; index >= 0; index--) {
      final Pair<Expr, Expr> currentBlock = blocks.get(index);

      final Expr condition = currentBlock.first;
      final Expr value = typeCalculator.enforceCommonType(currentBlock.second);

      if (condition.getNode().equals(NodeValue.newBoolean(true))) {
        result = value;
      } else if (condition.getNode().equals(NodeValue.newBoolean(false))) {
        // result stays the same
      } else {
        final NodeOperation node = new NodeOperation(
            StandardOperation.ITE,
            condition.getNode(),
            value.getNode(),
            result.getNode()
            );

        final NodeInfo nodeInfo =
            NodeInfo.newOperator(Operator.ITE, typeCalculator.getCommonType());

        node.setUserData(nodeInfo);
        result = new Expr(node);
      }
    }

    return result;
  }

  public Expr evaluateConst(final Where w, final Expr src) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);

    if (!src.isConstant()) {
      raiseError(w, ERR_NOT_STATIC);
    }

    return src;
  }

  public Expr evaluateSize(final Where w, final Expr src) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);

    if (!src.isConstant()) {
      raiseError(w, ERR_NOT_STATIC);
    }

    if (!src.getNode().isType(DataTypeId.LOGIC_INTEGER)) {
      raiseError(w, ERR_NOT_CONST_INTEGER);
    }

    return src;
  }

  public Expr evaluateIndex(final Where w, final Expr src) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);

    if (src.getNode().isType(DataTypeId.LOGIC_INTEGER) ||
        src.isTypeOf(TypeId.CARD) ||
        src.isTypeOf(TypeId.INT)) {
      return src;
    }

    raiseError(w, ERR_NOT_INDEX);
    return null; // Never executed.
  }

  public Expr evaluateLogic(final Where w, final Expr src) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);

    if (src.getNode().isType(DataTypeId.LOGIC_BOOLEAN)) {
      return src;
    }

    raiseError(w, "The expression cannot be evaluated to a boolean value.");
    return null; // Never executed.
  }

  public Expr evaluateData(final Where w, final Expr src) throws SemanticException {
    InvariantChecks.checkNotNull(w);
    InvariantChecks.checkNotNull(src);

    return src;
  }

  private final class TypeCalculator {
    private final Where w;
    private final DataType dataType; // Common Fortress type
    private final Type type;         // Common Fortress type
    private final boolean constant;  // All operands are constant

    private TypeCalculator(final Where w, final List<Expr> operands) throws SemanticException {
      InvariantChecks.checkNotNull(w);
      InvariantChecks.checkNotEmpty(operands);

      DataType commonDataType = null;
      Type commonType = null;

      boolean isAllConstant = true;
      for (final Expr operand : operands) {
        isAllConstant &= operand.isConstant();

        final DataType currentDataType = operand.getNode().getDataType();
        final DataType previousDataType = commonDataType;
   
        commonDataType = previousDataType == null ?
            currentDataType : TypeCast.getCastDataType(previousDataType, currentDataType);

        if (commonDataType == null) {
          raiseError(w, String.format(
              "Incompatible operand data types: %s and %s", previousDataType, currentDataType));
        } 

        final Type currentType = operand.getNodeInfo().getType();
        final Type previousType = commonType;

        if (currentType != null) { // Current operand is typed
          commonType = previousType == null ?
              currentType : TypeCast.getCastType(previousType, currentType);

          if (commonType == null) {
            raiseError(w, String.format("Incompatible operand types: %s and %s",
                previousType.getTypeName(), currentType.getTypeName()));
          }
        }
      }

      // Fortress type must always be calculated
      InvariantChecks.checkNotNull(commonDataType);

      // If no common nML type, all operands must be constants or the expression is inconsistent.
      InvariantChecks.checkTrue((null == commonType) == isAllConstant);

      this.w = w;
      this.dataType = commonDataType;
      this.type = commonType;
      this.constant = isAllConstant;
    }

    private Expr enforceCommonType(final Expr expr) throws SemanticException {
      InvariantChecks.checkNotNull(expr);

      final Node result;
      if (type == null) {
        result = TypeCast.castConstantTo(expr.getNode(), dataType);
      } else if (expr.isConstant()) {
        final Expr castOperand = TypeCast.castConstantTo(expr, type);
        result = castOperand.getNode();
      } else {
        if (expr.isTypeOf(type)) {
          raiseError(w, "Type mismatch. All operands must be " + type.getTypeName());
        }
        result = expr.getNode();
      }

      return new Expr(result);
    }

    private DataType getCommonDataType() {
      return dataType;
    }

    private Type getCommonType() {
      return type;
    }

    private boolean isAllConstant() {
      return constant;
    }
  }

  private static final String ERR_NOT_STATIC =
      "The expression cannot be statically calculated.";

  private static final String ERR_NOT_CONST_INTEGER =
      "The expression cannot be used to specify size since it cannot be evaluated to an integer constant.";

  private static final String ERR_NOT_INDEX =
      "The expression cannot be used as an index since it is not an integer value.";
}
