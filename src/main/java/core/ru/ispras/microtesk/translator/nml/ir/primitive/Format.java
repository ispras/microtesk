/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import java.util.Deque;
import java.util.EmptyStackException;
import java.util.LinkedList;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.translator.nml.generation.ExprPrinter;
import ru.ispras.microtesk.translator.nml.generation.PrinterInstance;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.utils.FormatMarker;

public final class Format {
  public static interface Argument {
    boolean isConvertibleTo(FormatMarker kind);
    String convertTo(FormatMarker kind);
    int getBinaryLength();
  }

  public static Argument createArgument(final String str) {
    return new StringBasedArgument(str);
  }

  public static Argument createArgument(final Expr expr) {
    return new ExprBasedArgument(expr);
  }

  public static Argument createArgument(final StatementAttributeCall call) {
    return new AttributeCallBasedArgument(call);
  }

  public static final class ExprBasedArgument implements Argument {
    private final Expr expr;

    public ExprBasedArgument(final Expr expr) {
      this.expr = expr;
    }

    @Override
    public boolean isConvertibleTo(final FormatMarker marker) {
      if (marker.isKind(FormatMarker.Kind.STR)) {
        return true;
      }

      return isModelConvertibleTo(marker);
    }

    private boolean isModelConvertibleTo(final FormatMarker marker) {
      if (marker.isKind(FormatMarker.Kind.BIN)) {
        return true;
      }

      assert (marker.isKind(FormatMarker.Kind.DEC) || marker.isKind(FormatMarker.Kind.HEX));
      final Node node = expr.getNode();

      if (node.isType(DataTypeId.BIT_VECTOR) ||
          node.isType(DataTypeId.LOGIC_INTEGER)) {
        return true;
      }

      assert false : "Unsupported data type: " + node.getDataType();
      return false;
    }

    @Override
    public String convertTo(final FormatMarker marker) {
      assert isConvertibleTo(marker);
      return convertModelTo(marker);
    }

    private String convertModelTo(final FormatMarker marker) {
      if (expr.getNodeInfo().getType() == null) {
        return ExprPrinter.toString(expr);
      }

      final String methodName;
      switch (marker.getKind()) {
        case BIN:
          methodName = "toBinString()";
          break;
        case STR:
          methodName = "toString()";
          break;
        case HEX:
          methodName = "bigIntegerValue(false)";
          break;
        case DEC:
          methodName = "bigIntegerValue()";
          break;
        default:
          throw new IllegalArgumentException("Unsupported marker kind: " + marker.getKind());
      }

      return String.format(
          "%s.%s", ExprPrinter.toString(expr), methodName);
    }

    @Override
    public int getBinaryLength() {
      return expr.getNode().getDataType().getSize();
    }
  }

  public static final class AttributeCallBasedArgument implements Argument {
    private final StatementAttributeCall callInfo;

    public AttributeCallBasedArgument(final StatementAttributeCall callInfo) {
      assert null != callInfo;
      this.callInfo = callInfo;
    }

    private String getCallText() {
      final StringBuilder sb = new StringBuilder();

      if (null != callInfo.getCalleeName()) {
        sb.append(String.format("%s.", callInfo.getCalleeName()));
      } else if (null != callInfo.getCalleeInstance()) {
        sb.append(String.format("%s.", PrinterInstance.toString(callInfo.getCalleeInstance())));
      }

      sb.append(String.format("%s(vars__)", callInfo.getAttributeName()));
      return sb.toString();
    }

    @Override
    public boolean isConvertibleTo(final FormatMarker marker) {
      if (marker.isKind(FormatMarker.Kind.STR)) {
        return true;
      }

      if (!callInfo.getAttributeName().equals(Attribute.IMAGE_NAME)) {
        return false;
      }

      assert (marker.isKind(FormatMarker.Kind.BIN) ||
              marker.isKind(FormatMarker.Kind.DEC) ||
              marker.isKind(FormatMarker.Kind.HEX));

      return true;
    }

    @Override
    public String convertTo(final FormatMarker marker) {
      assert isConvertibleTo(marker);

      if (marker.isKind(FormatMarker.Kind.STR) || marker.isKind(FormatMarker.Kind.BIN)) {
        return getCallText();
      }

      return String.format("Integer.valueOf(%s, 2)", getCallText());
    }

    @Override
    public int getBinaryLength() {
      return 0;
    }

    public String getCalleeName() {
      return callInfo.getCalleeName();
    }

    public String getAttributeName() {
      return callInfo.getAttributeName();
    }

    public Instance getCalleeInstance() {
      return callInfo.getCalleeInstance();
    }
  }

  public static final class StringBasedArgument implements Argument {
    private final String string;

    private StringBasedArgument(final String string) {
      this.string = string;
    }

    @Override
    public boolean isConvertibleTo(final FormatMarker marker) {
      return marker.isKind(FormatMarker.Kind.STR);
    }

    @Override
    public String convertTo(final FormatMarker marker) {
      assert isConvertibleTo(marker);
      return String.format("\"%s\"", string);
    }

    @Override
    public int getBinaryLength() {
      return string.length();
    }
  }

  public static final class TernaryConditionalArgument implements Argument {
    private final Expr expr;
    private final Argument left;
    private final Argument right;

    public Argument getLeft() {
      return left;
    }

    public Argument getRight() {
      return right;
    }

    private TernaryConditionalArgument(final Expr expr, final Argument left, final Argument right) {
      this.expr = expr;
      this.left = left;
      this.right = right;
    }

    @Override
    public boolean isConvertibleTo(final FormatMarker kind) {
      return left.isConvertibleTo(kind) && right.isConvertibleTo(kind);
    }

    @Override
    public String convertTo(final FormatMarker kind) {
      assert isConvertibleTo(kind);
      return String.format("%s ? %s : %s",
          ExprPrinter.toString(expr), left.convertTo(kind), right.convertTo(kind));
    }

    @Override
    public int getBinaryLength() {
      return 0;
    }
  }

  public static final class ConditionBuilder {
    private final Deque<Pair<Expr, Argument>> conditions;

    public ConditionBuilder() {
      this.conditions = new LinkedList<>();
    }

    public void addCondition(final Expr expr, final Argument argument) {
      conditions.push(new Pair<>(
          expr != null ? expr : new Expr(NodeValue.newBoolean(true)), argument));
    }

    public Argument build() {
      if (conditions.isEmpty()) {
        throw new EmptyStackException();
      }

      Argument result = conditions.pop().second;
      while (!conditions.isEmpty()) {
        final Pair<Expr, Argument> cond = conditions.pop();
        result = new TernaryConditionalArgument(cond.first, cond.second, result);
      }

      return result;
    }
  }
}
