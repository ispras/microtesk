/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.antlrex;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Operator;

final class ExprReducer {
  /**
   * Class for holding a reduced expression that is represented by the formula:
   * constant + polynomial, where constant is a constant integer value and
   * polynomial is expression that cannot be reduced any further.
   *
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public static class Reduced {
    public final int constant;
    public final Expr polynomial;

    private Reduced(final int constant, final Expr polynomial) {
      this.constant = constant;
      this.polynomial = polynomial;
    }

    @Override
    public String toString() {
      return "Reduced [constant=" + constant + ", polynomial=" + polynomial.getNode() + "]";
    }
  }

  /**
   * Transforms the expression to the format: polynomial + constant, where polynomial is some
   * expression that could not be further simplified and constant is an integer constant value.
   * Generally speaking, the transformation algorithm extracts all expressions that can be
   * statically calculated from the given expression and places their calculated value to the
   * constant field. The remaining part of the expression is placed in the polynomial field.
   *
   * @param expr Expression to be reduced.
   * @return A reduced expression.
   */
  public static Reduced reduce(final Expr expr) {
    InvariantChecks.checkNotNull(expr);

    if (expr.isConstant()) {
      return new Reduced(expr.integerValue(), null);
    }

    if (expr.getNodeInfo().isCoersionApplied()) {
      // If a coercion is applied, return without
      // changes as it may affect the result.
      return new Reduced(0, expr);
    }

    final NodeInfo.Kind kind = expr.getNodeInfo().getKind();
    switch (kind) {
      case LOCATION:
        // Locations cannot be reduced, return without changes.
        return new Reduced(0, expr);

      case CONST:
        // Must not reach here. Constants are dealt with by
        // first check in the method (isConstant).
        throw new IllegalStateException();

      case OPERATOR:
        return reduceOp(expr);

      default:
        throw new IllegalArgumentException("Unknown node kind: " + kind);
    }
  }

  private static Reduced reduceOp(Expr expr) {
    InvariantChecks.checkNotNull(expr);
    assert Node.Kind.OPERATION == expr.getNode().getKind();

    final NodeOperation nodeExpr = (NodeOperation) expr.getNode();
    final NodeInfo nodeInfo = expr.getNodeInfo();

    final Operator source = (Operator) nodeInfo.getSource();
    if (Operator.PLUS != source && Operator.MINUS != source) {
      // Return without changes.
      return new Reduced(0, expr);
    }


    final boolean isPlus = Operator.PLUS == source;
    assert source.getOperandCount() == 2;

    final Reduced left = reduce(new Expr(nodeExpr.getOperand(0)));
    final Reduced right = reduce(new Expr(nodeExpr.getOperand(1)));

    final int constant = isPlus ? left.constant + right.constant : left.constant - right.constant;

    if (null != left.polynomial && null != right.polynomial) {
      final Node polynomial = new NodeOperation(
          nodeExpr.getOperationId(), left.polynomial.getNode(), right.polynomial.getNode());

      polynomial.setUserData(expr.getNodeInfo());
      return new Reduced(constant, new Expr(polynomial));
    }

    if (null == left.polynomial) {
      if (isPlus) {
        return new Reduced(constant, right.polynomial);
      }

      final Node polynomial = Nodes.minus(right.polynomial.getNode());
      polynomial.setUserData(expr.getNodeInfo());

      return new Reduced(constant, new Expr(polynomial));
    }

    return new Reduced(constant, left.polynomial);
  }
}
