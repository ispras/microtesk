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

package ru.ispras.microtesk.translator.nml.ir.expression;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.model.api.data.TypeId;
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

/**
 * The ExprFactory class is a factory responsible for constructing expressions.
 * 
 * @author Andrei Tatarnikov
 */

public final class ExprFactory extends WalkerFactoryBase {
  private final ValueInfoCalculator calculator;

  /**
   * Constructor for an expression factory.
   * 
   * @param context Provides facilities for interacting with the tree walker.
   */

  public ExprFactory(WalkerContext context) {
    super(context);
    this.calculator = new ValueInfoCalculator(context);
  }

  /**
   * Creates an expression based on a named constant.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param name Name of the constant.
   * @return New expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if a constant with such name is not defined.
   */

  public Expr namedConstant(Where w, String name) throws SemanticException {
    checkNotNull(w);
    checkNotNull(name);

    if (!getIR().getConstants().containsKey(name)) {
      raiseError(w, new UndefinedConstant(name));
    }

    final LetConstant source = getIR().getConstants().get(name);
    return source.getExpr();
  }


  /**
   * Creates an expression based on a numeric literal.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param text Textual representation of a constant.
   * @param radix Radix used to convert text to a number.
   * @return New expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if the specified text cannot be converted to a number (due to
   *         incorrect format).
   */

  public Expr constant(Where w, String text, int radix) throws SemanticException {
    checkNotNull(w);
    checkNotNull(text);

    final BigInteger bi = new BigInteger(text, radix);
    final NodeInfo nodeInfo = NodeInfo.newConst(new SourceConstant(bi, radix));

    final Data data = Data.newInteger(bi);
    final Node node = new NodeValue(data);

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  /**
   * Creates an expression based on a location.
   * 
   * @param location Location object.
   * @return New expression.
   * 
   * @throws IllegalArgumentException if the parameter is null.
   */

  public Expr location(final Location source) {
    checkNotNull(source);

    final NodeInfo nodeInfo = NodeInfo.newLocation(source);

    final String name = source.toString();
    final Data data = Converter.toFortressData(nodeInfo.getValueInfo());

    final Variable variable = new Variable(name, data);
    final Node node = new NodeVariable(variable);

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  /**
   * Creates an operator-based expression.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param target Preferable value kind (needed to calculate value and type of the result, when
   *        mixed operand types are used).
   * @param id Operator identifier.
   * @param operands Operand expressions.
   * @return New expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if factory fails to create a valid expression (unsupported operator,
   *         incompatible types, etc.).
   */

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
      raiseError(w, String.format(ERR_UNSUPPORTED_OPERATOR, id));
    }

    if (operands.length != op.operands()) {
      raiseError(w, String.format(ERR_OPERAND_NUMBER_MISMATCH, id, op.operands()));
    }

    final List<ValueInfo> values = new ArrayList<ValueInfo>(operands.length);
    final Node[] operandNodes = new Node[operands.length];

    for (int index = 0; index < operands.length; ++index) {
      final Expr operand = operands[index];

      // All Model API values that have the BOOL type are cast to
      // Java boolean values for the sake of simplicity.

      if (operand.getValueInfo().isModelOf(TypeId.BOOL)) {
        final ValueInfo newValueInfo = operand.getValueInfo().toNativeType(Boolean.class);
        final NodeInfo newNodeInfo = operand.getNodeInfo().coerceTo(
            newValueInfo, Coercion.IMPLICIT);

        operand.setNodeInfo(newNodeInfo);
      }

      values.add(operand.getValueInfo());
      operandNodes[index] = operand.getNode();
    }

    // In the case type for shift and rotation expressions 
    // must correspond to the type of the first argument (for MODEL types).

    final ValueInfo castValueInfo;
    if (target == ValueInfo.Kind.MODEL && values.get(0).isModel() &&
        (op == Operator.L_SHIFT  || op == Operator.R_SHIFT ||
         op == Operator.L_ROTATE || op == Operator.R_ROTATE)) {
      castValueInfo = values.get(0); 
    } else {
      castValueInfo = calculator.cast(w, target, values);
    }

    final ValueInfo resultValueInfo = calculator.calculate(w, op, castValueInfo, values);

    final SourceOperator source = new SourceOperator(op, castValueInfo, resultValueInfo);
    final NodeInfo nodeInfo = NodeInfo.newOperator(source);

    final Enum<?> operator = Converter.toFortressOperator(op, castValueInfo);
    final Node node = new NodeOperation(operator, operandNodes);

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  public Expr sqrt(final Where w, final Expr operand) throws SemanticException {
    checkNotNull(w);
    checkNotNull(operand);

    if (!operand.getValueInfo().isModelOf(TypeId.FLOAT)) {
      raiseError(w, "The sqrt operation supports only the float type.");
    }

    final SourceOperator source = new SourceOperator(
        Operator.SQRT, operand.getValueInfo(), operand.getValueInfo());

    final NodeInfo nodeInfo = NodeInfo.newOperator(source);
    final Node node = new NodeOperation(Operator.SQRT, operand.getNode());

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  public Expr isNan(final Where w, final Expr operand) throws SemanticException {
    checkNotNull(w);
    checkNotNull(operand);

    if (!operand.getValueInfo().isModelOf(TypeId.FLOAT)) {
      raiseError(w, "The is_nan operation supports only the float type.");
    }

    final SourceOperator source = new SourceOperator(
        Operator.IS_NAN, operand.getValueInfo(), ValueInfo.createNativeType(Boolean.class));

    final NodeInfo nodeInfo = NodeInfo.newOperator(source);
    final Node node = new NodeOperation(Operator.IS_NAN, operand.getNode());

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  public Expr isSignalingNan(final Where w, final Expr operand) throws SemanticException {
    checkNotNull(w);
    checkNotNull(operand);

    if (!operand.getValueInfo().isModelOf(TypeId.FLOAT)) {
      raiseError(w, "The is_signaling_nan operation supports only the float type.");
    }

    final SourceOperator source = new SourceOperator(
        Operator.IS_SIGN_NAN, operand.getValueInfo(), ValueInfo.createNativeType(Boolean.class));

    final NodeInfo nodeInfo = NodeInfo.newOperator(source);
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

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType())) {
      return src;
    }

    if (srcValueInfo.isNative()) {
      raiseError(w, "Not supported for constants.");
    }

    if (type.getBitSize() < srcValueInfo.getModelType().getBitSize()) {
      raiseError(w, "Size of result type must be >= size of original type.");
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        ValueInfo.createModel(type), Coercion.SIGN_EXTEND);

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

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType())) {
      return src;
    }

    if (srcValueInfo.isNative()) {
      raiseError(w, "Not supported for constants.");
    }

    if (type.getBitSize() < srcValueInfo.getModelType().getBitSize()) {
      raiseError(w, "Size of result type must be >= size of original type.");
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        ValueInfo.createModel(type), Coercion.ZERO_EXTEND);

    src.setNodeInfo(newNodeInfo);
    return src;
  }

  /**
   * Performs type coercion. Source expression is coerced to a Model API type.
   * 
   * @param src Source expression (its attributes will be modified).
   * @param type Target type.
   * @return Coerced expression.
   * @throws SemanticException 
   * 
   * @throws NullPointerException if any of the parameters is null.
   */

  public Expr coerce(
      final Where w,
      final Expr src,
      final Type type) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType())) {
      return src;
    }

    if (srcValueInfo.isNative()) {
      raiseError(w, "Not supported for constants.");
    }

    if (!type.getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only integer types are supported.",
          type.getTypeName()));
    }

    if (!srcValueInfo.getModelType().getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only integer types are supported.",
          srcValueInfo.getModelType().getTypeName()));
    }

    final ValueInfo newValueInfo = ValueInfo.createModel(type);
    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        newValueInfo, Coercion.COERCE);

    src.setNodeInfo(newNodeInfo);
    return src;
  }

  /**
   * Performs type coercion. Source expression is coerced to a Native Java type.
   * 
   * @param src Source expression (its attributes will be modified).
   * @param type Target type.
   * @return Coerced expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   */

  public Expr coerce(final Where w, final Expr src, final Class<?> type) {
    checkNotNull(w);
    checkNotNull(src);
    checkNotNull(type);

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isNativeOf(type)) {
      return src;
    }

    final ValueInfo newValueInfo = srcValueInfo.toNativeType(type);
    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        newValueInfo, Coercion.COERCE);

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

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType())) {
      return src;
    }

    if (srcValueInfo.isNative()) {
      raiseError(w, "Not supported for constants.");
    }

    if (srcValueInfo.getModelType().getBitSize() != type.getBitSize()) {
      raiseError(w, "cast does not allow changing data size.");
    }

    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        ValueInfo.createModel(type), Coercion.CAST);

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

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType())) {
      return src;
    }

    if (srcValueInfo.isNative()) {
      raiseError(w, "Not supported for constants.");
    }

    if (!srcValueInfo.getModelType().getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only integer types are supported.",
          srcValueInfo.getModelType().getTypeName()));
    }

    if (srcValueInfo.getModelType().getBitSize() != 32 &&
        srcValueInfo.getModelType().getBitSize() != 64) {
      raiseError(w, "Only 32 and 64-bit integers are supported.");
    }

    if (type.getTypeId() != TypeId.FLOAT) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only float is supported.",
          type.getTypeName()));
    }

    final ValueInfo newValueInfo = ValueInfo.createModel(type);
    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        newValueInfo, Coercion.INT_TO_FLOAT);

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

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType())) {
      return src;
    }

    if (srcValueInfo.isNative()) {
      raiseError(w, "Not supported for constants.");
    }

    if (!srcValueInfo.isModelOf(TypeId.FLOAT)) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only float is supported.",
          srcValueInfo.getModelType().getTypeName()));
    }

    if (!type.getTypeId().isInteger()) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only integer types are supported.",
          type.getTypeName()));
    }

    if (type.getBitSize() != 32 &&
        type.getBitSize() != 64) {
      raiseError(w, "Only 32 and 64-bit integers are supported.");
    }

    final ValueInfo newValueInfo = ValueInfo.createModel(type);
    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        newValueInfo, Coercion.FLOAT_TO_INT);

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

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType())) {
      return src;
    }

    if (srcValueInfo.isNative()) {
      raiseError(w, "Not supported for constants.");
    }

    if (!srcValueInfo.isModelOf(TypeId.FLOAT)) {
      raiseError(w, String.format(
          "Cannot cast from %s. Only float is supported.",
          srcValueInfo.getModelType().getTypeName()));
    }

    if (type.getTypeId() != TypeId.FLOAT) {
      raiseError(w, String.format(
          "Cannot cast to %s. Only float is supported.",
          type.getTypeName()));
    }

    final ValueInfo newValueInfo = ValueInfo.createModel(type);
    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
        newValueInfo, Coercion.FLOAT_TO_FLOAT);

    src.setNodeInfo(newNodeInfo);
    return src;
  }

  /**
   * Creates a conditional expression based on the if-elif-else construction. The returned
   * expression is represented by a hierarchy of expressions based on the ITE ternary operator.
   * 
   * @param Position in a source file (needed for error reporting).
   * @param conds List of conditions and expressions associated with them. The 'else' condition is
   *        the last item if presents.
   * @return New expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws IllegalArgumentException if the list of conditions is empty.
   * @throws SemanticException if the types of expressions to be selected do not match.
   */

  public Expr condition(Where w, List<Condition> conds) throws SemanticException {
    checkNotNull(w);
    checkConditions(w, conds);

    final Deque<Condition> stack = new ArrayDeque<Condition>(conds);

    Expr tail = stack.peekLast().isElse() ? stack.removeLast().getExpression() : null;
    final ValueInfo tailVI = tail.getValueInfo();

    while (!stack.isEmpty()) {
      final Condition current = stack.removeLast();

      final Expr cond = current.getCondition();
      final Expr expr = current.getExpression();

      final ValueInfo condVI = cond.getValueInfo();
      final ValueInfo exprVI = expr.getValueInfo();

      ValueInfo resultVI = exprVI.typeInfoOnly(); // By default
      if (condVI.isConstant()) {
        final boolean isCondTrue = ((Boolean) condVI.getNativeValue());

        if (isCondTrue) {
          resultVI = exprVI;
        } else if (tail != null) {
          resultVI = tailVI;
        }
      }

      final SourceOperator source = new SourceOperator(Operator.ITE, resultVI, resultVI);
      final NodeInfo nodeInfo = NodeInfo.newOperator(source);

      final Node node = new NodeOperation(
        StandardOperation.ITE, cond.getNode(), expr.getNode(), tail.getNode());
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

    final Expr firstExpression = it.next().getExpression();
    final ValueInfo firstValueInfo = firstExpression.getValueInfo();

    while (it.hasNext()) {
      final Expr currentExpression = it.next().getExpression();
      final ValueInfo currentValueInfo = currentExpression.getValueInfo();

      if (!currentValueInfo.hasEqualType(firstValueInfo)) {
        raiseError(w, String.format(
          ERR_TYPE_MISMATCH, currentValueInfo.getTypeName(), firstValueInfo.getTypeName()));
      }
    }
  }

  /**
   * Checks whether the specified expression is a constant expression. Constant expressions are
   * statically calculated at translation time and are represented by constant Java values
   * (currently, "int" or "long"). If the source expression is not constant, an exception is raised.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param src Source expression (returned if meets the conditions).
   * @return Constant expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if the expression is not constant.
   */

  public Expr evaluateConst(Where w, Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (!srcValueInfo.isConstant()) {
      raiseError(w, ERR_NOT_STATIC);
    }

    return src;
  }

  /**
   * Checks whether the specified expression is a size expression. Size expressions are constant
   * expressions represented by Java integer values (int). If the source expression is not a size
   * expression, an exception is raised.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param src Source expression (returned if meets the conditions).
   * @return Size expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if the expression does not meet the requirements for a size
   *         expression.
   */

  public Expr evaluateSize(Where w, Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (!srcValueInfo.isConstant()) {
      raiseError(w, ERR_NOT_STATIC);
    }

    if (!srcValueInfo.isNativeOf(BigInteger.class)) {
      raiseError(w, ERR_NOT_CONST_INTEGER);
    }

    return src;
  }

  /**
   * Evaluates the specified expression to an index expression. The result of such an expression is
   * a Java integer value (int). If the source expression cannot be evaluated to an index
   * expression, an exception is raised. It it can, a required cast is performed.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param src Source expression (its attributes are modified if required).
   * @return Index expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if the expression does not meet the requirements for an index
   *         expression.
   */

  public Expr evaluateIndex(Where w, Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isNativeOf(BigInteger.class) ||
        srcValueInfo.isModelOf(TypeId.CARD)    ||
        srcValueInfo.isModelOf(TypeId.INT)) {
      return src;
    }

    raiseError(w, ERR_NOT_INDEX);
    return null; // Never executed.
  }

  /**
   * Evaluates the specified expression to an logic expression. The result of such an expression is
   * a Java boolean value (boolean). If the source expression cannot be evaluated to a logic
   * expression, an exception is raised. It it can, a required cast is performed.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param src Source expression (its attributes are modified if required).
   * @return Logic expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if the expression does not meet the requirements for a logic
   *         expression.
   */

  public Expr evaluateLogic(final Where w, final Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isNativeOf(Boolean.class)) {
      return src;
    }

    if (srcValueInfo.isModel()) {
      final ValueInfo newValueInfo = srcValueInfo.toNativeType(Boolean.class);
      final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(newValueInfo, Coercion.IMPLICIT);

      src.setNodeInfo(newNodeInfo);
      return src;
    }

    raiseError(w, ERR_NOT_BOOLEAN);
    return null; // Never executed.
  }

  /**
   * Evaluates the specified expression to a Model API data expression. If the source expression is
   * represented by a native Java expression, a required cast is performed. If the cast is not
   * supported (e.g due to incompatible data types), an exception is raised.
   * 
   * @param w Position in a source file (needed for error reporting).
   * @param src Source expression (its attributes are modified if required).
   * @return Model API data expression.
   * 
   * @throws NullPointerException if any of the parameters is null.
   * @throws SemanticException if the expression cannot be converted to a Model API data expression
   *         (e.g. incompatible data types).
   */

  public Expr evaluateData(final Where w, final Expr src) throws SemanticException {
    checkNotNull(w);
    checkNotNull(src);

    final ValueInfo srcValueInfo = src.getValueInfo();
    if (srcValueInfo.isModelOf(TypeId.BOOL)) {
      NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(
          ValueInfo.createNativeType(Boolean.class),
          Coercion.IMPLICIT
          );

      newNodeInfo = newNodeInfo.coerceTo(
          ValueInfo.createModel(
              Type.CARD(srcValueInfo.getModelType().getBitSize())), Coercion.IMPLICIT);

      src.setNodeInfo(newNodeInfo);
      return src;
    }

    if (srcValueInfo.isModel()) {
      return src;
    }

    if (!srcValueInfo.isNativeOf(BigInteger.class)) {
      raiseError(w, String.format(ERR_NOT_LOCATION_COMPATIBLE, srcValueInfo.getTypeName()));
    }

    final Type type;
    final int size;
    if (srcValueInfo.isConstant()) {
      final BigInteger value = (BigInteger) srcValueInfo.getNativeValue();
      final int usedSize =  value.bitLength();

      int adjustedSize = 1;
      while (adjustedSize < usedSize) {
        adjustedSize *= 2;
      }
      size = adjustedSize;
      type = value.compareTo(BigInteger.ZERO) < 0 ? Type.INT(size) : Type.CARD(size);
    } else {
      size = Integer.SIZE;
      type = Type.INT(size);
    }

    final ValueInfo newValueInfo = ValueInfo.createModel(type);
    final NodeInfo newNodeInfo = src.getNodeInfo().coerceTo(newValueInfo, Coercion.IMPLICIT);

    src.setNodeInfo(newNodeInfo);
    return src;
  }

  private static final String ERR_UNSUPPORTED_OPERATOR = "The %s operator is not supported.";

  private static final String ERR_OPERAND_NUMBER_MISMATCH = "The %s operator requires %d operands.";

  private static final String ERR_TYPE_MISMATCH =
      "%s is unexpected. All parts of the current conditional expression must have the %s type.";

  private static final String ERR_NOT_STATIC = "The expression cannot be statically calculated.";

  private static final String ERR_NOT_CONST_INTEGER =
      "The expression cannot be used to specify size since it cannot be evaluated to an integer constant.";

  private static final String ERR_NOT_INDEX =
      "The expression cannot be used as an index since it is not an integer value.";

  private static final String ERR_NOT_BOOLEAN =
      "The expression cannot be evaluated to a boolean value (Java boolean).";

  private static final String ERR_NOT_LOCATION_COMPATIBLE =
      "The %s type cannot be stored in a location.";
}
