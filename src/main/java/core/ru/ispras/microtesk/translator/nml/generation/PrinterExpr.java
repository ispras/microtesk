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

package ru.ispras.microtesk.translator.nml.generation;

import java.math.BigInteger;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.translator.nml.ir.expression.Coercion;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.expression.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expression.Operands;
import ru.ispras.microtesk.translator.nml.ir.expression.Operator;
import ru.ispras.microtesk.translator.nml.ir.expression.SourceConstant;
import ru.ispras.microtesk.translator.nml.ir.expression.SourceOperator;
import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.valueinfo.ValueInfo;

public final class PrinterExpr {
  private final Expr expr;
  private final NodeInfo nodeInfo;
  private final List<ValueInfo> coercionChain;
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

    return printCoersion(0);
  }

  private String printCoersion(int coercionIndex) {
    if (coercionIndex >= coercionChain.size()) {
      return printExpression();
    }

    final List<ValueInfo> previousVI = nodeInfo.getPreviousValueInfo();

    final ValueInfo target = coercionChain.get(coercionIndex);
    final ValueInfo source = previousVI.get(coercionIndex);

    return String.format(
        CoercionFormatter.getFormat(
            nodeInfo.getCoercions().get(coercionIndex), target, source),
        printCoersion(++coercionIndex)
        );
  }

  private String printExpression() {
    if (nodeInfo.getValueInfo().isConstant() && nodeInfo.getValueInfo().getNativeValue() instanceof BigInteger) {
      return bigIntegerToHexString((BigInteger) nodeInfo.getValueInfo().getNativeValue());
    }

    switch (nodeInfo.getKind()) {
      case CONST: {
        final SourceConstant source = (SourceConstant) nodeInfo.getSource();
        return constToString(source);
      }

      case NAMED_CONST: {
        final LetConstant source = (LetConstant) nodeInfo.getSource();
        return namedConstToString(source);
      }

      case LOCATION: {
        final Location source = (Location) nodeInfo.getSource();
        return locationToString(source);
      }

      case OPERATOR: {
        final SourceOperator source = (SourceOperator) nodeInfo.getSource();
        return operatorToString(source);
      }

      default: {
        assert false : "Unknown expression node kind: " + nodeInfo.getKind();
        return "";
      }
    }
  }

  private String constToString(final SourceConstant source) {
    final BigInteger value = source.getValue();

    final String result;
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0 && 
        value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
      result = (source.getRadix() == 10) ?
          value.toString(10) : "0x" + value.toString(16);
    } else if (value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0 && 
        value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
      result = ((source.getRadix() == 10) ? 
          value.toString(10) : "0x" + value.toString(16)) + "L";
    } else {
      // throw new IllegalArgumentException("To large number " + value);
      result = String.format("new BigInteger(\"%s\", %d)", value.toString(source.getRadix()), source.getRadix());
    }

    return result;
  }

  public static String bigIntegerToHexString(final BigInteger bi) {
    if (bi.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0 && 
        bi.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
      return "0x" + bi.toString(16);
    }

    if (bi.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0 && 
        bi.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
      return "0x" + bi.toString(16) + "L";
    }

    return String.format("new BigInteger(\"%s\", 16)", bi.toString(16));
  }

  private String namedConstToString(LetConstant source) {
    return source.getName();
  }

  private String locationToString(Location source) {
    if (asLocation) {
      return PrinterLocation.toString(source);
    }

    return PrinterLocation.toString(source) + ".load()";
  }

  private String operatorToString(SourceOperator source) {
    final NodeOperation nodeExpr = (NodeOperation) expr.getNode();
    final Operator op = source.getOperator();

    if (op == Operator.ITE) {
      return String.format("%s ? %s : %s",
        new PrinterExpr(new Expr(nodeExpr.getOperand(0))),
        new PrinterExpr(new Expr(nodeExpr.getOperand(1))),
        new PrinterExpr(new Expr(nodeExpr.getOperand(2))));
    }

    if (source.getCastValueInfo().isModel()) {
      if (Operands.UNARY.count() == nodeExpr.getOperandCount()) {
        final Node operandNode = nodeExpr.getOperand(0);
        return toModelString(op, operandToString(source, operandNode, false));
      }

      if (Operands.BINARY.count() == nodeExpr.getOperandCount()) {
        final Node operandNode1 = nodeExpr.getOperand(0);
        final Node operandNode2 = nodeExpr.getOperand(1);

        return toModelString(
            op,
            operandToString(source, operandNode1, true),
            operandToString(source, operandNode2, true)
            );
      }

      throw new IllegalArgumentException(String.format(
          "Unsupported operand number: %d, operator: %s.",
          nodeExpr.getOperandCount(),
          source.getOperator())
          );
    }

    if (Operands.UNARY.count() == nodeExpr.getOperandCount()) {
      final Node operandNode = nodeExpr.getOperand(0);

      return toOperatorString(op, operandToString(source, operandNode, true));
    }

    if (Operands.BINARY.count() == nodeExpr.getOperandCount()) {
      final Node operandNode1 = nodeExpr.getOperand(0);
      final Node operandNode2 = nodeExpr.getOperand(1);

      return toOperatorString(op, operandToString(source, operandNode1, true),
          operandToString(source, operandNode2, true));
    }

    throw new IllegalArgumentException(String.format(
      "Unsupported operand number: %d, operator: %s.",
      nodeExpr.getOperandCount(),
      source.getOperator())
      );
  }

  private static String operandToString(
      final SourceOperator operatorInfo,
      final Node operandNode,
      final boolean needsBrackets) {
    final Expr operand = new Expr(operandNode);
    final PrinterExpr printer = new PrinterExpr(operand);

    boolean enclose = false;
    if (needsBrackets && NodeInfo.Kind.OPERATOR == operand.getNodeInfo().getKind()) {
      final SourceOperator operandSource = (SourceOperator) operand.getNodeInfo().getSource();
      enclose = operandSource.getOperator().priority() < operatorInfo.getOperator().priority();
    }

    final String text;
    if (operatorInfo.getCastValueInfo().hasEqualType(operand.getValueInfo())) {
      text = printer.toString();
    } else {
      final String format = CoercionFormatter.getFormat(
          Coercion.IMPLICIT,
          operatorInfo.getCastValueInfo(),
          operand.getValueInfo()
          );
      text = String.format(format, printer);
    }

    return enclose ? String.format("(%s)", text) : text;
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

  private static final Map<Operator, String> operatorsNative = createNativeOperators();

  private static Map<Operator, String> createNativeOperators() {
    final Map<Operator, String> result = new EnumMap<Operator, String>(Operator.class);

    result.put(Operator.OR, "%s || %s");
    result.put(Operator.AND, "%s && %s");
    result.put(Operator.BIT_OR, "%s | %s");
    result.put(Operator.BIT_XOR, "%s ^ %s");
    result.put(Operator.BIT_AND, "%s & %s");
    result.put(Operator.EQ, "%s == %s");
    result.put(Operator.NOT_EQ, "%s != %s");
    result.put(Operator.LEQ, "%s <= %s");
    result.put(Operator.GEQ, "%s >= %s");
    result.put(Operator.LESS, "%s < %s");
    result.put(Operator.GREATER, "%s > %s");
    result.put(Operator.L_SHIFT, "%s << %s");
    result.put(Operator.R_SHIFT, "%s >> %s");
    result.put(Operator.L_ROTATE, "Integer.rotateLeft(%s, %s)");
    result.put(Operator.R_ROTATE, "Integer.rotateRight(%s, %s)");
    result.put(Operator.PLUS, "%s + %s");
    result.put(Operator.MINUS, "%s - %s");
    result.put(Operator.MUL, "%s * %s");
    result.put(Operator.DIV, "%s / %s");
    result.put(Operator.MOD, "%s % %s");
    result.put(Operator.POW, "(int)Math.pow(%s, %s)");
    result.put(Operator.UPLUS, "+%s");
    result.put(Operator.UMINUS, "-%s");
    result.put(Operator.BIT_NOT, "~%s");
    result.put(Operator.NOT, "!%s");

    return result;
  }

  private static String toModelString(Operator op, String arg) {
    return String.format(operators.get(op), arg);
  }

  private static String toModelString(Operator op, String arg1, String arg2) {
    return String.format(operators.get(op), arg1, arg2);
  }

  private static final String toOperatorString(Operator op, String arg) {
    return String.format(operatorsNative.get(op), arg);
  }

  private static final String toOperatorString(Operator op, String arg1, String arg2) {
    return String.format(operatorsNative.get(op), arg1, arg2);
  }
}


final class CoercionFormatter {
  private static final Map<Class<?>, String> modelToNativeMap = createModelToNative();
  private static final Map<Class<?>, String> nativeToNativeMap = createNativeToNative();

  private static Map<Class<?>, String> createNativeToNative() {
    final Map<Class<?>, String> result = new HashMap<Class<?>, String>();

    result.put(Integer.class, "((int) %%s)");
    result.put(Long.class, "((long) %%s)");
    result.put(Boolean.class, "0 != %%s");

    return result;
  }

  private static Map<Class<?>, String> createModelToNative() {
    final Map<Class<?>, String> result = new HashMap<Class<?>, String>();

    result.put(BigInteger.class, "intValue");
    result.put(Integer.class, "intValue");
    result.put(Long.class, "longValue");
    result.put(Boolean.class, "booleanValue");

    return result;
  }

  private static final String DATA_CLASS = Data.class.getSimpleName();

  //private static final String COERCE_METHOD = "coerce";
  private static final String VALUE_OF_METHOD = "valueOf";

  private static final String TO_MODEL_FORMAT = "%s.%s(%s, %%s)";
  private static final String TO_NATIVE_FORMAT = "%%s.%s()";

  private static final String ERR_REDUNDANT_COERCION = "Redundant coercion. Equal types: %s.";
  private static final String ERR_UNSUPPORTED_COERCION = "Cannot perform coercion from %s to %s.";

  static String getFormat(
      final Coercion coercionType,
      final ValueInfo target,
      final ValueInfo source) {
    InvariantChecks.checkNotNull(coercionType);
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(source);

    // This invariant is protected by NodeInfo and ExprPrinter.
    if (target.hasEqualType(source)) {
      throw new IllegalArgumentException(
          String.format(ERR_REDUNDANT_COERCION, target.getTypeName()));
    }

    assert target.isModel() || target.isNative();
    assert source.isModel() || source.isNative();

    if (target.isModel()) {
      final String methodName = source.isModel() ?
          coercionType.getMethodName() : VALUE_OF_METHOD;

      return String.format(
          TO_MODEL_FORMAT,
          DATA_CLASS,
          methodName,
          target.getModelType().getJavaText()
          );
    }

    if (source.isModel()) {
      // Model BOOLEAN  values do not require conversion
      // since boolean methods of Data always return Java boolean.
      if (source.isModelOf(TypeId.BOOL) && target.getNativeType().equals(Boolean.class)) {
        return "%s";
      }

      final String methodName = modelToNativeMap.get(target.getNativeType());
      if (null == methodName) {
        throw new IllegalArgumentException(String.format(
          ERR_UNSUPPORTED_COERCION, target.getTypeName(), source.getTypeName()));
      }
      return String.format(TO_NATIVE_FORMAT, methodName);
    } else {
      final String coercionFormat = nativeToNativeMap.get(target.getNativeType());
      if (null == coercionFormat) {
        throw new IllegalArgumentException(String.format(
          ERR_UNSUPPORTED_COERCION, target.getTypeName(), source.getTypeName()));
      }
      return coercionFormat;
    }
  }
}
