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

package ru.ispras.microtesk.translator.nml.ir.expr;

import java.math.BigInteger;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;
import ru.ispras.microtesk.translator.nml.ir.location.Location;

public final class Expr {
  private final Node node;

  public Expr(final Node node) {
    InvariantChecks.checkNotNull(node);
    this.node = node;
  }

  public boolean isConstant() {
    return Node.Kind.VALUE == node.getKind();
  }

  public boolean isTypeOf(final TypeId typeId) {
    InvariantChecks.checkNotNull(typeId);

    final Type type = getNodeInfo().getType();
    return type != null && type.getTypeId() == typeId;
  }

  public boolean isTypeOf(final Type otherType) {
    InvariantChecks.checkNotNull(otherType);

    final Type thisType = getNodeInfo().getType();
    return thisType != null && this.equals(otherType);
  }

  public Node getNode() {
    return node;
  }

  public NodeInfo getNodeInfo() {
    return (NodeInfo) node.getUserData();
  }

  public void setNodeInfo(final NodeInfo nodeInfo) {
    InvariantChecks.checkNotNull(nodeInfo);
    node.setUserData(nodeInfo);
  }

  public int integerValue() {
    return bigIntegerValue().intValue();
  }

  public BigInteger bigIntegerValue() {
    if (isConstant() && node.isType(DataTypeId.LOGIC_INTEGER)) {
      return ((NodeValue) node).getInteger();
    }

    throw new IllegalStateException("Not a constant integer expression.");
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Expr other = (Expr) obj;
    return compareNodes(this.node, other.node);
  }

  private static boolean compareNodes(Node node1, Node node2) {
    if (node1 == node2) {
      return true;
    }

    if ((null == node1) || (null == node2)) {
      return false;
    }

    if (node1.getKind() == Node.Kind.VALUE &&
        node2.getKind() == Node.Kind.VALUE &&
        node1.equals(node2)) {
      return true;
    }

    final NodeInfo ni1 = (NodeInfo) node1.getUserData();
    final NodeInfo ni2 = (NodeInfo) node2.getUserData();

    if (!ni1.getType().equals(ni2.getType())) {
      return false;
    }

    if (node1.getKind() != node2.getKind()) {
      return false;
    }

    if (Node.Kind.VARIABLE == node1.getKind()) {
      final Location location1 = (Location) ni1.getSource();
      final Location location2 = (Location) ni2.getSource();

      return location1.equals(location2);
    }

    if (Node.Kind.OPERATION == node1.getKind()) {
      final Operator operator1 = (Operator) ni1.getSource();
      final Operator operator2 = (Operator) ni2.getSource();

      if (!operator1.equals(operator2)) {
        return false;
      }

      final NodeOperation nodeExpr1 = (NodeOperation) node1;
      final NodeOperation nodeExpr2 = (NodeOperation) node2;

      if (nodeExpr1.getOperandCount() != nodeExpr2.getOperandCount()) {
        return false;
      }

      for (int index = 0; index < nodeExpr1.getOperandCount(); ++index) {
        if (!compareNodes(nodeExpr1.getOperand(index), nodeExpr2.getOperand(index))) {
          return false;
        }
      }

      return true;
    }

    // NoveValue1 != NodeValue2 because, otherwise, vi1.equals(vi2)
    // in the beginning of the method would have returned true.

    assert (Node.Kind.VALUE == node1.getKind());
    return false;
  }

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
      return "Reduced [constant=" + constant + 
             ", polynomial=" + polynomial.getNode() + "]";
    }
  }

  /**
   * Transforms the expression to the format: polynomial + constant, where polynomial is some
   * expression that could not be further simplified and constant is an integer constant value.
   * Generally speaking, the transformation algorithm extracts all expressions that can be
   * statically calculated from the given expression and places their calculated value to the
   * constant field. The remaining part of the expression is placed in the polynomial field.
   * 
   * @return A reduced expression.
   */
  public Reduced reduce() {
    return reduce(this);
  }

  private static Reduced reduce(Expr expr) {
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

      final Node polynomial = new NodeOperation(
          StandardOperation.MINUS, right.polynomial.getNode());

      polynomial.setUserData(expr.getNodeInfo());
      return new Reduced(constant, new Expr(polynomial));
    }
 
    return new Reduced(constant, left.polynomial);
  }

  @Override
  public String toString() {
    return node.toString();
  }
}
