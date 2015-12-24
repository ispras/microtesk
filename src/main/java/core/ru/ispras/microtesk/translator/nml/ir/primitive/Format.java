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

import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.translator.nml.generation.PrinterExpr;
import ru.ispras.microtesk.translator.nml.generation.PrinterInstance;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;
import ru.ispras.microtesk.utils.FormatMarker;

public final class Format {
  public static interface Argument {
    public boolean isConvertibleTo(FormatMarker kind);
    public String convertTo(FormatMarker kind);
  }

  public static Argument createArgument(String str) {
    return new StringBasedArgument(str);
  }

  public static Argument createArgument(Expr expr) {
    return new ExprBasedArgument(expr);
  }

  public static Argument createArgument(StatementAttributeCall call) {
    return new AttributeCallBasedArgument(call);
  }

  private static final class ExprBasedArgument implements Argument {
    private final Expr expr;

    public ExprBasedArgument(Expr expr) {
      this.expr = expr;
    }

    @Override
    public boolean isConvertibleTo(FormatMarker marker) {
      if (FormatMarker.STR == marker) {
        return true;
      }

      if (expr.getValueInfo().isModel()) {
        return isModelConvertibleTo(marker);
      }

      return isJavaConvertibleTo(marker);
    }

    private boolean isModelConvertibleTo(FormatMarker marker) {
      if (FormatMarker.BIN == marker) {
        return true;
      }

      assert ((FormatMarker.DEC == marker) || (FormatMarker.HEX == marker));

      final Type type = expr.getValueInfo().getModelType();
      if (TypeId.CARD == type.getTypeId() || TypeId.INT == type.getTypeId()) {
        return true;
      }

      assert false : "Unsupported model data type.";
      return false;
    }

    private boolean isJavaConvertibleTo(FormatMarker marker) {
      final Class<?> type = expr.getValueInfo().getNativeType();

      if (!type.equals(int.class) || !type.equals(Integer.class)) {
        return false;
      }

      assert ((FormatMarker.BIN == marker) || (FormatMarker.DEC == marker) || (FormatMarker.HEX == marker));
      return true;
    }

    @Override
    public String convertTo(FormatMarker marker) {
      assert isConvertibleTo(marker);

      if (expr.getValueInfo().isModel()) {
        return convertModelTo(marker);
      }

      return convertJavaTo(marker);
    }

    private String convertModelTo(FormatMarker marker) {
      final String methodName;

      if (FormatMarker.BIN == marker) {
        methodName = "toBinString";
      } else if (FormatMarker.STR == marker) {
        methodName = "toString";
      } else {
        final int bitSize = expr.getValueInfo().getModelType().getBitSize();
        if (bitSize <= 32) {
          methodName = "intValue";
        } else if (bitSize <= 64) {
          methodName = "longValue";
        } else {
          methodName = "bigIntegerValue";
        }
      }

      return String.format("%s.getRawData().%s()", new PrinterExpr(expr), methodName);
    }

    private String convertJavaTo(FormatMarker marker) {
      final PrinterExpr printer = new PrinterExpr(expr);
      return (FormatMarker.BIN == marker) ? 
        String.format("Integer.toBinaryString(%s)", printer) : printer.toString();
    }
  }

  private static final class AttributeCallBasedArgument implements Argument {
    private final StatementAttributeCall callInfo;

    public AttributeCallBasedArgument(StatementAttributeCall callInfo) {
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
    public String convertTo(FormatMarker marker) {
      assert isConvertibleTo(marker);

      if ((FormatMarker.STR == marker) || (FormatMarker.BIN == marker)) {
        return getCallText();
      }

      return String.format("Integer.valueOf(%s, 2)", getCallText());
    }
  }

  private static final class StringBasedArgument implements Argument {
    private final String string;

    private StringBasedArgument(String string) {
      this.string = string;
    }

    @Override
    public boolean isConvertibleTo(FormatMarker kind) {
      return FormatMarker.STR == kind;
    }

    @Override
    public String convertTo(FormatMarker marker) {
      assert isConvertibleTo(marker);
      return String.format("\"%s\"", string);
    }
  }

  private static final class TernaryConditionalArgument implements Argument {
    private final Expr expr;
    private final Argument left;
    private final Argument right;

    private TernaryConditionalArgument(Expr expr, Argument left, Argument right) {
      this.expr = expr;
      this.left = left;
      this.right = right;
    }

    @Override
    public boolean isConvertibleTo(FormatMarker kind) {
      return left.isConvertibleTo(kind) && right.isConvertibleTo(kind);
    }

    @Override
    public String convertTo(FormatMarker kind) {
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

    public void addCondition(Expr expr, Argument argument) {
      if (null == expr) {
        expr = Expr.CONST_ONE; // Fake value to be ignored.
      }
      conditions.push(new Pair<>(expr, argument));
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
