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

package ru.ispras.microtesk.translator.nml.generation;

import java.math.BigInteger;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.translator.nml.ir.expr.Coercion;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Operator;
import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class PrinterExpr {

  public static String bigIntegerToString(final BigInteger value, final int radix) {
    InvariantChecks.checkNotNull(value);

    final String result;
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0 && 
        value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
      result = (radix == 10) ? value.toString(radix) : "0x" + value.toString(radix);
    } else if (value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0 && 
        value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
      result = ((radix == 10) ? value.toString(radix) : "0x" + value.toString(radix)) + "L";
    } else {
      result = String.format("new BigInteger(\"%s\", %d)", value.toString(radix), radix);
    }

    return result;
  }

  public static String bitVectorToString(final BitVector value) {
    InvariantChecks.checkNotNull(value);

    final int bitSize = value.getBitSize();
    final String hexValue = value.toHexString();

    final String text;
    if (value.getBitSize() <= Integer.SIZE) {
      text = String.format("%s.valueOf(0x%s, %d)",
          BitVector.class.getSimpleName(), hexValue, bitSize);
    } else if (bitSize <= Long.SIZE) {
      text = String.format("%s.valueOf(0x%sL, %d)",
          BitVector.class.getSimpleName(), hexValue, bitSize);
    } else {
      text = String.format("%s.valueOf(\"%s\", 16, %d)",
          BitVector.class.getSimpleName(), hexValue, bitSize);
    }

    return text;
  }

  private final Expr expr;
  private final NodeInfo nodeInfo;
  private final List<Type> coercionChain;
  private final boolean asLocation;

  public PrinterExpr(Expr expr) {
    this(expr, false);
  }

  public PrinterExpr(Expr expr, boolean asLocation) {
    this.expr = expr;
    this.asLocation = asLocation;

    if (null != expr) {
      this.nodeInfo = expr.getNodeInfo();
      this.coercionChain = expr.getNodeInfo().getCoercionChain();
    } else {
      this.nodeInfo = null;
      this.coercionChain = null;
    }
  }

  @Override
  public String toString() {
    if (null == expr) {
      return "";
    }

    if (nodeInfo.isCoersionApplied()) {
      printExpression();
    }

    return printCoersion(0);
  }

  private String printCoersion(int coercionIndex) {
    if (coercionIndex >= coercionChain.size() - 1) {
      return printExpression();
    }

    final Coercion coercion = nodeInfo.getCoercions().get(coercionIndex);
    final Type target = coercionChain.get(coercionIndex);
    final Type source = coercionChain.get(coercionIndex + 1);

    return String.format(
        getFormat(coercion, target, source),
        printCoersion(++coercionIndex)
        );
  }

  private String printExpression() {
    if (expr.isConstant() && nodeInfo.getType() == null) {
      if (expr.getNode().isType(DataTypeId.LOGIC_BOOLEAN)) {
        return expr.getNode().toString();
      }

      if (expr.getNode().isType(DataTypeId.BIT_VECTOR)) {
        return bigIntegerToString(
            ((NodeValue) expr.getNode()).getBitVector().bigIntegerValue(false), 10);
      }

      return bigIntegerToString(((NodeValue) expr.getNode()).getInteger(), 16);
    }

    switch (nodeInfo.getKind()) {
      case CONST: {
        return constToString(nodeInfo.getType());
      }

      case LOCATION: {
        final Location source = (Location) nodeInfo.getSource();
        return locationToString(source);
      }

      case OPERATOR: {
        final Operator source = (Operator) nodeInfo.getSource();
        return operatorToString(source);
      }

      default: {
        assert false : "Unknown expression node kind: " + nodeInfo.getKind();
        return "";
      }
    }
  }

  private String constToString(final Type type) {
    final NodeValue value = (NodeValue) expr.getNode();
    switch (value.getDataTypeId()) {
      case LOGIC_BOOLEAN:
        return value.toString();

      case BIT_VECTOR:
        return String.format("Data.valueOf(%s, %s)",
            type.getJavaText(), bigIntegerToString(value.getBitVector().bigIntegerValue(false), 16));

      case LOGIC_INTEGER:
        return String.format("Data.valueOf(%s, %s)",
            type.getJavaText(), bigIntegerToString(value.getInteger(), 16));

      default:
        throw new IllegalArgumentException(
            "Unsupported type: " + value.getDataTypeId());
    }
  }

  private String locationToString(Location source) {
    if (asLocation) {
      return PrinterLocation.toString(source);
    }

    return PrinterLocation.toString(source) + ".load()";
  }

  private String operatorToString(Operator op) {
    final NodeOperation nodeExpr = (NodeOperation) expr.getNode();

    if (op == Operator.ITE) {
      return String.format("%s ? %s : %s",
          new PrinterExpr(new Expr(nodeExpr.getOperand(0))),
          new PrinterExpr(new Expr(nodeExpr.getOperand(1))),
          new PrinterExpr(new Expr(nodeExpr.getOperand(2))));
    }

    if (1 == nodeExpr.getOperandCount()) {
      final Node operandNode = nodeExpr.getOperand(0);
      return toOperatorString(op, operandToString(op, operandNode, false));
    }

    if (2 == nodeExpr.getOperandCount()) {
      final Node operandNode1 = nodeExpr.getOperand(0);
      final Node operandNode2 = nodeExpr.getOperand(1);

      return toOperatorString(
          op,
          operandToString(op, operandNode1, true),
          operandToString(op, operandNode2, true)
          );
    }

    throw new IllegalArgumentException(String.format(
        "Unsupported operand number: %d, operator: %s.", nodeExpr.getOperandCount(), op));
  }

  private static String operandToString(
      final Operator operatorInfo,
      final Node operandNode,
      final boolean needsBrackets) {
    final Expr operand = new Expr(operandNode);
    final PrinterExpr printer = new PrinterExpr(operand);
    return printer.toString();
  }

  private static final Map<Operator, String> operators = createModelOperators();
  private static Map<Operator, String> createModelOperators() {
    final Map<Operator, String> result = new EnumMap<>(Operator.class);

    result.put(Operator.OR,  "%s || %s");
    result.put(Operator.AND, "%s && %s");
    result.put(Operator.NOT, "!%s");

    result.put(Operator.BIT_OR,  "%s.or(%s)");
    result.put(Operator.BIT_XOR, "%s.xor(%s)");
    result.put(Operator.BIT_AND, "%s.and(%s)");

    result.put(Operator.EQ,      "%s.equals(%s)");
    result.put(Operator.NOT_EQ,  "!%s.equals(%s)");

    result.put(Operator.LEQ,     "%s.compareTo(%s) <= 0");
    result.put(Operator.GEQ,     "%s.compareTo(%s) >= 0");
    result.put(Operator.LESS,    "%s.compareTo(%s) < 0");
    result.put(Operator.GREATER, "%s.compareTo(%s) > 0");

    result.put(Operator.L_SHIFT,  "%s.shiftLeft(%s)");
    result.put(Operator.R_SHIFT,  "%s.shiftRight(%s)");
    result.put(Operator.L_ROTATE, "%s.rotateLeft(%s)");
    result.put(Operator.R_ROTATE, "%s.rotateRight(%s)");

    result.put(Operator.PLUS,     "%s.add(%s)");
    result.put(Operator.MINUS,    "%s.subtract(%s)");
    result.put(Operator.MUL,      "%s.multiply(%s)");
    result.put(Operator.DIV,      "%s.divide(%s)");
    result.put(Operator.MOD,      "%s.mod(%s)");
    result.put(Operator.POW,      "%s.pow(%s)");

    result.put(Operator.UPLUS,    "%s");
    result.put(Operator.UMINUS,   "%s.negate()");
    result.put(Operator.BIT_NOT,  "%s.not()");

    result.put(Operator.SQRT,        "%s.sqrt()");
    result.put(Operator.IS_NAN,      "%s.isNan");
    result.put(Operator.IS_SIGN_NAN, "%s.isSignalingNan");

    return Collections.unmodifiableMap(result);
  }

  private static String toOperatorString(Operator op, String arg) {
    return String.format(operators.get(op), arg);
  }

  private static String toOperatorString(Operator op, String arg1, String arg2) {
    return String.format(operators.get(op), arg1, arg2);
  }

  private static String getFormat(
      final Coercion coercionType,
      final Type target,
      final Type source) {
    InvariantChecks.checkNotNull(coercionType);
    InvariantChecks.checkNotNull(target);

    // This invariant is protected by NodeInfo and ExprPrinter.
    if (target.equals(source)) {
      throw new IllegalArgumentException(String.format(
          "Redundant coercion. Equal types: %s.", target.getTypeName()));
    }

    final String methodName = coercionType.getMethodName();
    return String.format(
          "%s.%s(%s, %%s)",
          Data.class.getSimpleName(),
          methodName,
          target.getJavaText()
          );
  }
}
