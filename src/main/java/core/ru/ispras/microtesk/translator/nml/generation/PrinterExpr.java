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
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.translator.nml.ir.expr.Coercion;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
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
    return new ExprPrinter(asLocation).toString(expr.getNode());
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
