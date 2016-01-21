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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.model.api.data.floatx.FloatX;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.errors.UndefinedConstant;

import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;
import ru.ispras.microtesk.translator.nml.ir.valueinfo.ValueInfo;
import ru.ispras.microtesk.translator.nml.ir.valueinfo.ValueInfoCalculator;

public final class ExprFactory extends WalkerFactoryBase {

  public ExprFactory(final WalkerContext context) {
    super(context);
  }

  public Expr constant(final Where w, final String text, final int radix) throws SemanticException {
    checkNotNull(w);
    checkNotNull(text);

    final Expr expr = new Expr(NodeValue.newInteger(text, radix));
    expr.setNodeInfo(NodeInfo.newConst(null)); // No nML type is associated

    return expr;
  }

  public Expr namedConstant(final Where w, final String name) throws SemanticException {
    checkNotNull(w);
    checkNotNull(name);

    if (!getIR().getConstants().containsKey(name)) {
      raiseError(w, new UndefinedConstant(name));
    }

    final LetConstant source = getIR().getConstants().get(name);
    return new Expr(source.getExpr().getNode());
  }

  public Expr location(final Location source) {
    checkNotNull(source);

    final NodeInfo nodeInfo = NodeInfo.newLocation(source);

    final String name = source.toString();
    final DataType dataType = Converter.toFortressDataType(nodeInfo.getType());

    final Node node = new NodeVariable(name, dataType);
    node.setUserData(nodeInfo);

    return new Expr(node);
  }

  /*
  public Expr operator(
      final Where w,
      final ValueInfo.Kind target,
      final String id,
      final Expr... operands) throws SemanticException {
    checkNotNull(w);
    checkNotNull(target);
    checkNotNull(id);
    checkNotNull(operands);

    final Operator op = Operator.forText(id);
    if (null == op) {
      raiseError(w, String.format("The %s operator is not supported.", id));
    }

    if (operands.length != op.operands()) {
      raiseError(w, String.format("The %s operator requires %d operands.", id, op.operands()));
    }

    DataType commonDataType = null; // Common Fortress type
    Type commonType = null; // Common nML type

    for (final Expr operand : operands) {
      final DataType currentDataType = operand.getNode().getDataType();
      InvariantChecks.checkNotNull(currentDataType);
 
      commonDataType = commonDataType == null ?
          currentDataType : TypeCast.getCastDataType(commonDataType, currentDataType);
      
      final Type currentType = operand.getNodeInfo().getType();

      // Current operand is typed
      if (currentType == null) {
        continue;
      }
 
      // Common nML type is not known yet
      if (castType == null) {
        castType = currentType;
        continue;
      }

      castType = TypeCast.getCastType(castType, operand.getNodeInfo().getType());

      // No common type is found
      if (castType == null) {
        raiseError(w, String.format("Incompatible operand types: %s and %s",
            castType.getTypeName(), currentType.getTypeName()));
      }
    }

    final List<Node> operandNodes = new ArrayList<>(operands.length);
    for (final Expr operand : operands) {
      final Node operandNode;

      if (castType == null) {
        operandNode = operand.getNode();
      } else if (operand.isConstant()) {
        final Expr castOperand = castConstantTo(operand, castType);
        operandNode = castOperand.getNode();
      } else {
        if (operand.isTypeOf(castType)) {
          raiseError(w, "Type mismatch. All operands must be " + castType.getTypeName());
        }
        operandNode = operand.getNode();
      }

      operandNodes.add(operandNode);
    }

    final Enum<?> operator = Converter.toFortressOperator(op, castType);
    final Type resultType = Converter.toResultType(op, castType);

    final Node node = new NodeOperation(operator, operandNodes);
    final NodeInfo nodeInfo = NodeInfo.newOperator(op, resultType);

    node.setUserData(nodeInfo);
    
    final Node reducedNode = Transformer.reduce(node);

    return new Expr(node);
  }
  */
/*
  public Expr sqrt(final Where w, final Expr operand) throws SemanticException {
    checkNotNull(w);
    checkNotNull(operand);

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
    checkNotNull(w);
    checkNotNull(operand);

    if (!operand.isTypeOf(TypeId.FLOAT)) {
      raiseError(w, "The is_nan operation supports only the float type.");
    }

    final NodeInfo nodeInfo = NodeInfo.newOperator(Operator.IS_NAN, Type.BOOLEAN);
    final Node node = new NodeOperation(Operator.IS_NAN, operand.getNode());

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  public Expr isSignalingNan(final Where w, final Expr operand) throws SemanticException {
    checkNotNull(w);
    checkNotNull(operand);

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
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return castConstantTo(src, type);
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
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return castConstantTo(src, type);
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
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return castConstantTo(src, type);
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
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return castConstantTo(src, type);
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
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
    }

    if (src.isConstant()) {
      return castConstantTo(src, type);
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
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
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
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    if (src.isTypeOf(type)) {
      return src;
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

  public Expr condition(Where w, List<Condition> conds) throws SemanticException {
    checkNotNull(w);
    checkConditions(w, conds);

    final Deque<Condition> stack = new ArrayDeque<>(conds);
    Expr tail = stack.peekLast().isElse() ? stack.removeLast().getExpression() : null;

    while (!stack.isEmpty()) {
      final Condition current = stack.removeLast();

      final Expr cond = current.getCondition();
      final Expr expr = current.getExpression();

      Type resultType = expr.getNodeInfo().getType();
      if (cond.isConstant()) {
        final boolean isCondTrue = ((NodeValue) cond.getNode()).getBoolean();

        if (isCondTrue) {
          resultType = expr.getNodeInfo().getType();
        } else if (tail != null) {
          resultType = tail.getNodeInfo().getType();
        }
      }

      final Node node = new NodeOperation(
          StandardOperation.ITE, cond.getNode(), expr.getNode(), tail.getNode());

      final NodeInfo nodeInfo = NodeInfo.newOperator(Operator.ITE, resultType);
      node.setUserData(nodeInfo);

      tail = new Expr(node);
    }

    return tail;
  }

  private void checkConditions(Where w, List<Condition> conds) throws SemanticException {
    checkNotNull(conds);

    if (conds.isEmpty()) {
      throw new IllegalArgumentException("Empty conditions.");
    }

    final Iterator<Condition> it = conds.iterator();
    final Expr firstExpr = it.next().getExpression();
    final Type firstType = firstExpr.getNodeInfo().getType();

    while (it.hasNext()) {
      final Expr currentExpr = it.next().getExpression();
      final Type currentType = currentExpr.getNodeInfo().getType();

      if (!currentExpr.isTypeOf(firstExpr.getNodeInfo().getType())) {
        raiseError(w, String.format(
            ERR_TYPE_MISMATCH,
            currentType != null ? currentType.getTypeName() : currentExpr.getNode().getDataType(),
            firstType != null ? firstType.getTypeName() : firstExpr.getNode().getDataType())
            );
      }
    }
  }

  public Expr evaluateConst(Where w, Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    if (!src.isConstant()) {
      raiseError(w, ERR_NOT_STATIC);
    }

    return src;
  }

  public Expr evaluateSize(Where w, Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    if (!src.isConstant()) {
      raiseError(w, ERR_NOT_STATIC);
    }

    if (!src.getNode().isType(DataTypeId.LOGIC_INTEGER)) {
      raiseError(w, ERR_NOT_CONST_INTEGER);
    }

    return src;
  }

  public Expr evaluateIndex(Where w, Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    if (src.getNode().isType(DataTypeId.LOGIC_INTEGER) ||
        src.isTypeOf(TypeId.CARD) ||
        src.isTypeOf(TypeId.INT)) {
      return src;
    }

    raiseError(w, ERR_NOT_INDEX);
    return null; // Never executed.
  }

  public Expr evaluateLogic(final Where w, final Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    if (src.getNode().isType(DataTypeId.LOGIC_BOOLEAN)) {
      return src;
    }

    raiseError(w, ERR_NOT_BOOLEAN);
    return null; // Never executed.
  }

  public Expr evaluateData(final Where w, final Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    return src;
  }
*/
  private static Expr castConstantTo(final Expr value, final Type type) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(value.isConstant());
    InvariantChecks.checkNotNull(type);

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

  private static final String ERR_TYPE_MISMATCH =
      "%s is unexpected. All parts of the current conditional expression must have the %s type.";

  private static final String ERR_NOT_STATIC =
      "The expression cannot be statically calculated.";

  private static final String ERR_NOT_CONST_INTEGER =
      "The expression cannot be used to specify size since it cannot be evaluated to an integer constant.";

  private static final String ERR_NOT_INDEX =
      "The expression cannot be used as an index since it is not an integer value.";

  private static final String ERR_NOT_BOOLEAN =
      "The expression cannot be evaluated to a boolean value (Java boolean).";

  private static final String ERR_NOT_LOCATION_COMPATIBLE =
      "The %s type cannot be stored in a location.";
}
