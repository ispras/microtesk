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
import ru.ispras.microtesk.translator.nml.generation.PrinterExpr;
import ru.ispras.microtesk.translator.nml.generation.PrinterInstance;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.utils.FormatMarker;

public final class Format {
  public static interface Argument {
    boolean isConvertibleTo(FormatMarker kind);
    String convertTo(FormatMarker kind);
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

  private static final class ExprBasedArgument implements Argument {
    private final Expr expr;

    public ExprBasedArgument(final Expr expr) {
      this.expr = expr;
    }

    @Override
    public boolean isConvertibleTo(final FormatMarker marker) {
      if (FormatMarker.STR == marker) {
        return true;
      }

      return isModelConvertibleTo(marker);
    }

    private boolean isModelConvertibleTo(final FormatMarker marker) {
      if (FormatMarker.BIN == marker) {
        return true;
      }

      assert ((FormatMarker.DEC == marker) || (FormatMarker.HEX == marker));
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
        return new PrinterExpr(expr).toString();
      }

      final String methodName;
      if (FormatMarker.BIN == marker || FormatMarker.STR == marker) {
        methodName = "toBinString()";
      } else if (FormatMarker.HEX == marker) {
        methodName = "bigIntegerValue(false)";
      } else if (FormatMarker.DEC == marker) {
        methodName = "bigIntegerValue()";
      } else {
        throw new IllegalArgumentException("Unsupported marker: " + marker);
      }

      return String.format(
          "%s.%s", new PrinterExpr(expr), methodName);
    }
  }

  private static final class AttributeCallBasedArgument implements Argument {
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

      sb.append(String.format("%s()", callInfo.getAttributeName()));
      return sb.toString();
    }

    @Override
    public boolean isConvertibleTo(FormatMarker marker) {
      if (FormatMarker.STR == marker) {
        return true;
      }

      if (!callInfo.getAttributeName().equals(Attribute.IMAGE_NAME)) {
        return false;
      }

      assert ((FormatMarker.BIN == marker) || (FormatMarker.DEC == marker) || (FormatMarker.HEX == marker));
      return true;
    }

    @Override
    public String convertTo(final FormatMarker marker) {
      assert isConvertibleTo(marker);

      if ((FormatMarker.STR == marker) || (FormatMarker.BIN == marker)) {
        return getCallText();
      }

      return String.format("Integer.valueOf(%s, 2)", getCallText());
    }
  }

  private static final class StringBasedArgument implements Argument {
    private final String string;

    private StringBasedArgument(final String string) {
      this.string = string;
    }

    @Override
    public boolean isConvertibleTo(final FormatMarker kind) {
      return FormatMarker.STR == kind;
    }

    @Override
    public String convertTo(final FormatMarker marker) {
      assert isConvertibleTo(marker);
      return String.format("\"%s\"", string);
    }
  }

  private static final class TernaryConditionalArgument implements Argument {
    private final Expr expr;
    private final Argument left;
    private final Argument right;

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
          new PrinterExpr(expr), left.convertTo(kind), right.convertTo(kind));
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
