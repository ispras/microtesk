/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.expression.printer.MapBasedPrinter;
import ru.ispras.fortress.expression.printer.OperationDescription;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.translator.nml.ir.expr.Coercion;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Operator;
import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

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
  private final EnumMap<Operator, OperationDescription> operatorMap;

  protected ExprPrinter(final boolean asLocation) {
    this.asLocation = asLocation;
    this.operatorMap = new EnumMap<>(Operator.class);

    addMapping(StandardOperation.EQ,     "", ".equals(", ")");
    addMapping(StandardOperation.NOTEQ, "!", ".equals(", ")");

    addMapping(StandardOperation.AND, "", " && ", "");
    addMapping(StandardOperation.OR,  "", " || ", "");
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

    addMapping(Operator.SQRT,        "", "", ".sqrt()");
    addMapping(Operator.IS_NAN,      "", "", ".isNan()");
    addMapping(Operator.IS_SIGN_NAN, "", "", ".isSignalingNan()");

    setVisitor(new Visitor());
  }

  protected final void addMapping(
      final Operator op,
      final String prefix,
      final String infix,
      final String suffix) {
    operatorMap.put(op, new OperationDescription(prefix, infix, suffix));
  }

  @Override
  protected OperationDescription getOperationDescription(final NodeOperation expr) {
    final Enum<?> operationId = expr.getOperationId();

    if (operationId instanceof Operator) {
      return operatorMap.get(operationId);
    }

    return super.getOperationDescription(expr);
  }

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
      result = String.format("new %s(\"%s\", %d)",
          BigInteger.class.getSimpleName(), value.toString(radix), radix);
    }

    return result;
  }

  private static String valueToString(final Type type, final BigInteger value) {
    return type != null ?
        String.format("Data.valueOf(%s, %s)", type.getJavaText(), bigIntegerToString(value, 16)) :
        bigIntegerToString(value, 10);
  }

  private static String valueToString(final Type type, final BitVector value) {
    return valueToString(type, value.bigIntegerValue(false));
  }

  private final class Visitor extends ExprTreeVisitor {
    private final Deque<Integer> coercionStack = new ArrayDeque<>();

    @Override
    public void onOperationBegin(final NodeOperation expr) {
      InvariantChecks.checkTrue(expr.getUserData() instanceof NodeInfo);
      final NodeInfo nodeInfo = (NodeInfo) expr.getUserData();

      final int coercionCount = appendCoercions(nodeInfo);
      coercionStack.push(coercionCount);

      super.onOperationBegin(expr);
    }

    @Override
    public void onOperationEnd(final NodeOperation expr) {
      super.onOperationEnd(expr);

      final int coercionCount = coercionStack.pop();
      for (int index = 0; index < coercionCount; ++index) {
        appendText(")");
      }
    }

    @Override
    public void onValue(final NodeValue value) {
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

      for (int index = 0; index < coercionCount; ++index) {
        appendText(")");
      }
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      InvariantChecks.checkTrue(variable.getUserData() instanceof NodeInfo);

      final NodeInfo nodeInfo = (NodeInfo) variable.getUserData();

      InvariantChecks.checkTrue(nodeInfo.getSource() instanceof Location);
      final Location source = (Location) nodeInfo.getSource();

      final int coercionCount = appendCoercions(nodeInfo);

      final String text = PrinterLocation.toString(source);
      appendText(asLocation ? text : text + ".load()");

      for (int index = 0; index < coercionCount; ++index) {
        appendText(")");
      }
    }

    private int appendCoercions(final NodeInfo nodeInfo) {
      if (!nodeInfo.isCoersionApplied()) {
        return 0;
      }

      final List<Type> coercionChain = nodeInfo.getCoercionChain();
      int coercionIndex = 0;
      for (; coercionIndex < coercionChain.size() - 1; ++coercionIndex) {
        final Coercion coercion = nodeInfo.getCoercions().get(coercionIndex);

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
  }
}
