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

package ru.ispras.microtesk.translator.simnml.generation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.NodeInfo;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operands;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.expression.SourceConstant;
import ru.ispras.microtesk.translator.simnml.ir.expression.SourceOperator;
import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

public final class PrinterExpr {
  private final Expr expr;
  private final NodeInfo nodeInfo;
  private final List<ValueInfo> coercionChain;

  public PrinterExpr(Expr expr) {
    this.expr = expr;

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

    return String.format(CoercionFormatter.getFormat(target, source),
      printCoersion(++coercionIndex));
  }

  private String printExpression() {
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

  private String constToString(SourceConstant source) {
    final Object value = source.getValue();
    final String result;

    if (Integer.class == value.getClass()) {
      result = (source.getRadix() == 10) ? 
        Integer.toString(((Number) value).intValue()) :
        "0x" + Integer.toHexString((Integer) value);
    } else if (Long.class == value.getClass()) {
      result = (source.getRadix() == 10) ?
        Long.toString(((Number) value).longValue()) + "L" :
        "0x"+ Long.toHexString(((Number) value).longValue()) + "L";
    } else {
      assert false : "Unsuported constant value type: " + value.getClass().getSimpleName();
      result = value.toString();
    }

    return result;
  }

  private String namedConstToString(LetConstant source) {
    return source.getName();
  }

  private String locationToString(Location source) {
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
      final StringBuilder sb = new StringBuilder();

      for (int index = 0; index < nodeExpr.getOperandCount(); ++index) {
        final Node operandNode = nodeExpr.getOperand(index);

        sb.append(", ");
        sb.append(operandToString(source, operandNode, false));
      }

      return String.format("%s.execute(%s%s)",
        DataEngine.class.getSimpleName(),
        toModelString(source.getOperator()),
        sb.toString()
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

  private static String operandToString(SourceOperator operatorInfo, Node operandNode,
      boolean needsBrackets) {
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
      final String format =
          CoercionFormatter.getFormat(operatorInfo.getCastValueInfo(), operand.getValueInfo());
      text = String.format(format, printer);
    }

    return enclose ? String.format("(%s)", text) : text;
  }

  private static final Map<Operator, EOperatorID> operators = createModelOperators();

  private static Map<Operator, EOperatorID> createModelOperators() {
    final Map<Operator, EOperatorID> result = new EnumMap<Operator, EOperatorID>(Operator.class);

    result.put(Operator.OR, EOperatorID.OR);
    result.put(Operator.AND, EOperatorID.AND);
    result.put(Operator.BIT_OR, EOperatorID.BIT_OR);
    result.put(Operator.BIT_XOR, EOperatorID.BIT_XOR);
    result.put(Operator.BIT_AND, EOperatorID.BIT_AND);
    result.put(Operator.EQ, EOperatorID.EQ);
    result.put(Operator.NOT_EQ, EOperatorID.NOT_EQ);
    result.put(Operator.LEQ, EOperatorID.LESS_EQ);
    result.put(Operator.GEQ, EOperatorID.GREATER_EQ);
    result.put(Operator.LESS, EOperatorID.LESS);
    result.put(Operator.GREATER, EOperatorID.GREATER);
    result.put(Operator.L_SHIFT, EOperatorID.L_SHIFT);
    result.put(Operator.R_SHIFT, EOperatorID.R_SHIFT);
    result.put(Operator.L_ROTATE, EOperatorID.L_ROTATE);
    result.put(Operator.R_ROTATE, EOperatorID.R_ROTATE);
    result.put(Operator.PLUS, EOperatorID.PLUS);
    result.put(Operator.MINUS, EOperatorID.MINUS);
    result.put(Operator.MUL, EOperatorID.MUL);
    result.put(Operator.DIV, EOperatorID.DIV);
    result.put(Operator.MOD, EOperatorID.MOD);
    result.put(Operator.POW, EOperatorID.POW);
    result.put(Operator.UPLUS, EOperatorID.UNARY_PLUS);
    result.put(Operator.UMINUS, EOperatorID.UNARY_MINUS);
    result.put(Operator.BIT_NOT, EOperatorID.BIT_NOT);
    result.put(Operator.NOT, EOperatorID.NOT);

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

  private static String toModelString(Operator op) {
    return EOperatorID.class.getSimpleName() + "." + operators.get(op).name();
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

    result.put(Integer.class, "intValue");
    result.put(Long.class, "longValue");
    result.put(Boolean.class, "booleanValue");

    return result;
  }

  private static final String ENGINE_CLASS = DataEngine.class.getSimpleName();

  private static final String COERCE_METHOD = "coerce";
  private static final String VALUE_OF_METHOD = "valueOf";

  private static final String TO_MODEL_FORMAT = "%s.%s(%s, %%s)";
  private static final String TO_NATIVE_FORMAT = "%s.%s(%%s)";

  private static final String ERR_REDUNDANT_COERCION = "Redundant coercion. Equal types: %s.";
  private static final String ERR_UNSUPPORTED_COERCION = "Cannot perform coercion from %s to %s.";

  static String getFormat(ValueInfo target, ValueInfo source) {
    if (null == target) {
      throw new NullPointerException();
    }

    if (null == source) {
      throw new NullPointerException();
    }

    // This invariant is protected by NodeInfo and ExprPrinter.
    if (target.hasEqualType(source)) {
      throw new IllegalArgumentException(
        String.format(ERR_REDUNDANT_COERCION, target.getTypeName()));
    }

    assert target.isModel() || target.isNative();
    assert source.isModel() || source.isNative();

    if (target.isModel()) {
      final String methodName = source.isModel() ?
        COERCE_METHOD : VALUE_OF_METHOD;

      return String.format(TO_MODEL_FORMAT,
        ENGINE_CLASS, methodName, target.getModelType().getJavaText());
    }

    if (source.isModel()) {
      final String methodName = modelToNativeMap.get(target.getNativeType());
      if (null == methodName) {
        throw new IllegalArgumentException(String.format(
          ERR_UNSUPPORTED_COERCION, target.getTypeName(), source.getTypeName()));
      }
      return String.format(TO_NATIVE_FORMAT, ENGINE_CLASS, methodName);
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
