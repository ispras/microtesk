/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.sim;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.expression.printer.MapBasedPrinter;
import ru.ispras.fortress.expression.printer.OperationDescription;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.Execution;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Operator;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExprPrinter extends MapBasedPrinter {

  public static String toString(final Expr expr, final boolean asLocation) {
    if (null == expr) {
      return "";
    }

    return new ExprPrinter(asLocation).toString(expr.getNode());
  }

  public static String toString(final Expr expr) {
    return toString(expr, false);
  }

  private final boolean asLocation;
  private final Map<Operator, OperationDescription> operatorMap;
  private final Map<Enum<?>, String> castOperatorMap;
  private final Deque<Enum<?>> operatorStack;

  protected ExprPrinter(final boolean asLocation) {
    this.asLocation = asLocation;
    this.operatorMap = new EnumMap<>(Operator.class);
    this.castOperatorMap = new HashMap<>();
    this.operatorStack = new ArrayDeque<>();

    addMapping(StandardOperation.EQ,     "", ".equals(", ")");
    addMapping(StandardOperation.NOTEQ, "!", ".equals(", ")");

    addMapping(StandardOperation.AND, "", " && ", "");
    addMapping(StandardOperation.OR,  "(", " || ", ")");
    addMapping(StandardOperation.NOT, "!(", "", ")");

    addMapping(StandardOperation.ITE, "(", new String[] {" ? ", " : "}, ")");

    addMapping(StandardOperation.LESS,      "(", ".compareTo(", ") < 0)");
    addMapping(StandardOperation.LESSEQ,    "(", ".compareTo(", ") <= 0)");
    addMapping(StandardOperation.GREATER,   "(", ".compareTo(", ") > 0)");
    addMapping(StandardOperation.GREATEREQ, "(", ".compareTo(", ") >= 0)");

    addMapping(StandardOperation.MINUS,  "", "", ".negate()");
    addMapping(StandardOperation.PLUS,   "", "", "");

    addMapping(StandardOperation.ADD,    "", ".add(", ")");
    addMapping(StandardOperation.SUB,    "", ".subtract(", ")");
    addMapping(StandardOperation.MUL,    "", ".multiply(", ")");
    addMapping(StandardOperation.DIV,    "", ".divide(", ")");
    addMapping(StandardOperation.MOD,    "", ".mod(", ")");
    addMapping(StandardOperation.POWER,  "", ".pow(", ")");

    addMapping(StandardOperation.BVNOT,  "", "", ".not()");
    addMapping(StandardOperation.BVNEG,  "", "", ".negate()");

    addMapping(StandardOperation.BVOR,   "", ".or(", ")");
    addMapping(StandardOperation.BVXOR,  "", ".xor(", ")");
    addMapping(StandardOperation.BVAND,  "", ".and(", ")");

    addMapping(StandardOperation.BVADD,  "", ".add(", ")");
    addMapping(StandardOperation.BVSUB,  "", ".subtract(", ")");
    addMapping(StandardOperation.BVMUL,  "", ".multiply(", ")");
    addMapping(StandardOperation.BVUDIV, "", ".divide(", ")");
    addMapping(StandardOperation.BVSDIV, "", ".divide(", ")");
    addMapping(StandardOperation.BVUREM, "", ".mod(", ")");
    addMapping(StandardOperation.BVSREM, "", ".mod(", ")");
    addMapping(StandardOperation.BVSMOD, "", ".mod(", ")");

    addMapping(StandardOperation.BVLSHL, "", ".shiftLeft(", ")");
    addMapping(StandardOperation.BVASHL, "", ".shiftLeft(", ")");
    addMapping(StandardOperation.BVLSHR, "", ".shiftRight(", ")");
    addMapping(StandardOperation.BVASHR, "", ".shiftRight(", ")");
    addMapping(StandardOperation.BVROL,  "", ".rotateLeft(", ")");
    addMapping(StandardOperation.BVROR,  "", ".rotateRight(", ")");

    addMapping(StandardOperation.BVULE, "(", ".compareTo(", ") <= 0)");
    addMapping(StandardOperation.BVULT, "(", ".compareTo(", ") < 0)");
    addMapping(StandardOperation.BVUGE, "(", ".compareTo(", ") >= 0)");
    addMapping(StandardOperation.BVUGT, "(", ".compareTo(", ") > 0)");
    addMapping(StandardOperation.BVSLE, "(", ".compareTo(", ") <= 0)");
    addMapping(StandardOperation.BVSLT, "(", ".compareTo(", ") < 0)");
    addMapping(StandardOperation.BVSGE, "(", ".compareTo(", ") >= 0)");
    addMapping(StandardOperation.BVSGT, "(", ".compareTo(", ") > 0)");

    addMapping(StandardOperation.BVREPEAT,
        "", new String[] {".repeat("}, ")", new int[] {1, 0});

    addMapping(StandardOperation.BVEXTRACT,
        "", new String[] {".bitField(", ", "}, ")", new int[] {2, 0, 1});

    addMapping(StandardOperation.BVCONCAT,
        "Location.concat(", ", ", ")");

    addMapping(Operator.SQRT,        "", "", ".sqrt()");
    addMapping(Operator.ROUND,       "", "", ".round()");
    addMapping(Operator.IS_NAN,      "", "", ".isNan()");
    addMapping(Operator.IS_SIGN_NAN, "", "", ".isSignalingNan()");

    addMappingForCast(StandardOperation.BVSIGNEXT, "signExtend");
    addMappingForCast(StandardOperation.BVZEROEXT, "zeroExtend");
    addMappingForCast(Operator.INT_TO_FLOAT, "intToFloat");
    addMappingForCast(Operator.FLOAT_TO_INT, "floatToInt");
    addMappingForCast(Operator.FLOAT_TO_FLOAT, "floatToFloat");
    addMappingForCast(Operator.COERCE, "coerce");

    setVisitor(new Visitor());
  }

  protected final void addMapping(
      final Operator op,
      final String prefix,
      final String infix,
      final String suffix) {
    operatorMap.put(op, new OperationDescription(prefix, infix, suffix));
  }

  private void addMappingForCast(final Enum<?> op, final String text) {
    castOperatorMap.put(op, text);
  }

  @Override
  protected OperationDescription getOperationDescription(final NodeOperation expr) {
    Enum<?> opId = expr.getOperationId();

    if (opId == StandardOperation.BVEXTRACT) {
      InvariantChecks.checkTrue(expr.getUserData() instanceof NodeInfo);
      final NodeInfo nodeInfo = (NodeInfo) expr.getUserData();

      final Operator innerOpId = (Operator) nodeInfo.getSource();
      if (innerOpId == Operator.COERCE) {
        opId = Operator.COERCE;
      }
    }

    if (castOperatorMap.containsKey(opId)) {
      InvariantChecks.checkTrue(expr.getUserData() instanceof NodeInfo);
      final NodeInfo nodeInfo = (NodeInfo) expr.getUserData();

      final Type type = nodeInfo.getType();
      InvariantChecks.checkNotNull(type);

      final String operationText = String.format("%s.%s(%s, ",
          Data.class.getSimpleName(), castOperatorMap.get(opId), type.getJavaText());

      return new OperationDescription(operationText, "", ")");
    }

    return opId instanceof Operator
        ? operatorMap.get(opId) : super.getOperationDescription(expr);
  }

  public static String bigIntegerToString(final BigInteger value, final int radix) {
    InvariantChecks.checkNotNull(value);

    final String result;
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0
        && value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
      result = (radix == 10) ? value.toString(radix) : "0x" + value.toString(radix);
    } else if (value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0
        && value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
      result = ((radix == 10) ? value.toString(radix) : "0x" + value.toString(radix)) + "L";
    } else {
      result = String.format("new %s(\"%s\", %d)",
          BigInteger.class.getSimpleName(), value.toString(radix), radix);
    }

    return result;
  }

  private static String valueToString(final Type type, final BigInteger value) {
    if (type == null) {
      return bigIntegerToString(value, 10);
    }

    return String.format(
        "%s.valueOf(%s, %s)",
        Data.class.getSimpleName(),
        type.getJavaText(),
        bigIntegerToString(value, 16)
        );
  }

  private static String valueToString(final Type type, final BitVector value) {
    return valueToString(type, value.bigIntegerValue(false));
  }

  private final class Visitor extends ExprTreeVisitor {
    private final Deque<Integer> coercionStack = new ArrayDeque<>();

    @Override
    public void onOperationBegin(final NodeOperation expr) {
      if (expr.getUserData() instanceof NodeInfo) {
        final NodeInfo nodeInfo = (NodeInfo) expr.getUserData();

        final int coercionCount = appendCoercions(nodeInfo);
        coercionStack.push(coercionCount);
        operatorStack.push(expr.getOperationId());
      }

      super.onOperationBegin(expr);
    }

    @Override
    public void onOperationEnd(final NodeOperation expr) {
      super.onOperationEnd(expr);

      if (expr.getUserData() instanceof NodeInfo) {
        final int coercionCount = coercionStack.pop();

        if (coercionCount > 0 && ExprUtils.isOperation(expr, StandardOperation.BVCONCAT)) {
          appendText(".load()");
        }

        appendCloseBrackets(coercionCount);
        operatorStack.pop();
      }
    }

    @Override
    public void onOperandBegin(
        final NodeOperation operation,
        final Node operand,
        final int index) {
      if (operation.getUserData() instanceof NodeInfo) {
        final NodeInfo nodeInfo = (NodeInfo) operation.getUserData();

        final Enum<?> opId = operation.getOperationId();
        final Enum<?> innerOpId = (Enum<?>) nodeInfo.getSource();
        final boolean isLast = (operation.getOperandCount() - 1) == index;

        if ((castOperatorMap.containsKey(opId)
            || castOperatorMap.containsKey(innerOpId)) && !isLast) {
          // Skips all operands but the last
          setStatus(Status.SKIP);
        }
      }

      super.onOperandBegin(operation, operand, index);
    }

    @Override
    public void onOperandEnd(
        final NodeOperation operation,
        final Node operand,
        final int index) {
      if (operation.getUserData() instanceof NodeInfo) {
        final NodeInfo nodeInfo = (NodeInfo) operation.getUserData();

        final Enum<?> opId = operation.getOperationId();
        final Enum<?> innerOpId = (Enum<?>) nodeInfo.getSource();
        final boolean isLast = (operation.getOperandCount() - 1) == index;

        if ((castOperatorMap.containsKey(opId)
            || castOperatorMap.containsKey(innerOpId)) && !isLast) {
          // Restores status
          setStatus(Status.OK);
        }
      }

      super.onOperandEnd(operation, operand, index);

      if (ExprUtils.isOperation(operand, StandardOperation.BVCONCAT)
          && !((NodeInfo) operand.getUserData()).isCoersionApplied()) {
        appendText(".load()");
      }
    }

    @Override
    public void onValue(final NodeValue value) {
      if (value.isType(DataTypeId.LOGIC_STRING)) {
        // Hack to deal with internal variables described by string constants
        final Expr expr = new Expr(value);
        if (expr.isInternalVariable()) {
          final int coercionCount = appendCoercions(expr.getNodeInfo());

          appendText(Execution.class.getSimpleName() + "." + value.getValue().toString());
          if (!asLocation) {
            appendText(".load()");
          }

          appendCloseBrackets(coercionCount);
          return;
        }

        appendText(String.format("\"%s\"", value.getValue()));
        return;
      }

      InvariantChecks.checkTrue(value.getUserData() instanceof NodeInfo);
      final NodeInfo nodeInfo = (NodeInfo) value.getUserData();

      final Type type = nodeInfo.getType();

      final String text;
      switch (value.getDataTypeId()) {
        case LOGIC_BOOLEAN:
          text = value.toString();
          break;

        case BIT_VECTOR:
          text = valueToString(type, value.getBitVector());
          break;

        case LOGIC_INTEGER:
          text = valueToString(type, value.getInteger());
          break;

        default:
          throw new IllegalArgumentException(
              "Unsupported type: " + value.getDataTypeId());
      }

      final int coercionCount = appendCoercions(nodeInfo);
      appendText(text);
      appendCloseBrackets(coercionCount);
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      if (variable.isType(DataTypeId.LOGIC_STRING)) {
        InvariantChecks.checkTrue(variable.getUserData() instanceof StatementAttributeCall);
        final StatementAttributeCall callInfo = (StatementAttributeCall) variable.getUserData();

        final StringBuilder sb = new StringBuilder();
        if (null != callInfo.getCalleeName()) {
          sb.append(String.format("%s.", callInfo.getCalleeName()));
        } else if (null != callInfo.getCalleeInstance()) {
          sb.append(String.format("%s.", PrinterInstance.toString(callInfo.getCalleeInstance())));
        }

        final String attrName = callInfo.getAttributeName();
        final boolean isSyntax = Attribute.SYNTAX_NAME.equals(attrName);

        sb.append(String.format("%s(vars__)", isSyntax ? "text" : attrName));
        appendText(sb.toString());

        return;
      }

      InvariantChecks.checkTrue(variable.getUserData() instanceof NodeInfo);
      final NodeInfo nodeInfo = (NodeInfo) variable.getUserData();

      InvariantChecks.checkTrue(nodeInfo.getSource() instanceof Location);
      final Location source = (Location) nodeInfo.getSource();

      final int coercionCount = appendCoercions(nodeInfo);
      final String text = PrinterLocation.toString(source);

      final boolean isConcatOperand =
          operatorStack.peek() == StandardOperation.BVCONCAT;

      appendText(asLocation || isConcatOperand ? text : text + ".load()");
      appendCloseBrackets(coercionCount);
    }

    private int appendCoercions(final NodeInfo nodeInfo) {
      if (!nodeInfo.isCoersionApplied()) {
        return 0;
      }

      final List<Type> coercionChain = nodeInfo.getCoercionChain();
      int coercionIndex = 0;
      for (; coercionIndex < coercionChain.size() - 1; ++coercionIndex) {
        final NodeInfo.Coercion coercion = nodeInfo.getCoercions().get(coercionIndex);

        final Type target = coercionChain.get(coercionIndex);
        final Type source = coercionChain.get(coercionIndex + 1);

        InvariantChecks.checkFalse(target.equals(source), "Redundant coercion.");

        final String methodName = coercion.getMethodName();
        final String text = String.format(
              "%s.%s(%s, ",
              Data.class.getSimpleName(),
              methodName,
              target.getJavaText()
              );

        appendText(text);
      }

      return coercionIndex;
    }

    private void appendCloseBrackets(final int count) {
      for (int index = 0; index < count; ++index) {
        appendText(")");
      }
    }
  }
}
