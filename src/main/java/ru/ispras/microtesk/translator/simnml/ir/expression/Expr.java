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

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

/**
 * The role of the Expr class is to describe Sim-nML expressions. The class aggregates a Fortress
 * expression and provides methods to obtain additional information.
 * 
 * @author Andrei Tatarnikov
 */

public final class Expr {
  /**
   * Constant expression equal to one (1).
   */

  public static final Expr CONST_ONE = newConstant(1);

  /**
   * Class for holding a reduced expression that is represented by the formula: constant +
   * polynomial, where constant is a constant integer value and polynomial is expression that cannot
   * be reduced any further.
   * 
   * @author Andrei Tatarnikov
   */

  public static class Reduced {
    public final int constant;
    public final Expr polynomial;

    private Reduced(int constant, Expr polynomial) {
      this.constant = constant;
      this.polynomial = polynomial;
    }
  }

  private final Node node;

  /**
   * Constructs an expression basing on a Fortress expression tree.
   * 
   * @param node A Fortress expression.
   * 
   * @throws NullPointerException if the parameter is null.
   * @throws IllegalArgumentException is the user attribute of the node does not refer to a
   *         {@link NodeInfo} object or the kind of the NodeInfo object is not compatible to the
   *         node type.
   */

  public Expr(Node node) {
    if (null == node) {
      throw new NullPointerException();
    }

    if (!(node.getUserData() instanceof NodeInfo)) {
      throw new IllegalArgumentException();
    }

    if (!((NodeInfo) node.getUserData()).getKind().isCompatibleNode(node.getKind())) {
      throw new IllegalArgumentException();
    }

    this.node = node;
  }

  /**
   * Creates a constant expression basing on the specified integer value.
   * 
   * @param value Integer value.
   * @return Constant expression.
   */

  public static Expr newConstant(int value) {
    final SourceConstant source = new SourceConstant(value, 10);
    final NodeInfo nodeInfo = NodeInfo.newConst(source);

    final Data data = Converter.toFortressData(nodeInfo.getValueInfo());
    final Node node = new NodeValue(data);

    node.setUserData(nodeInfo);
    return new Expr(node);
  }

  /**
   * Returns a Fortress expression tree describing the expression.
   * 
   * @return Fortress expression tree.
   */

  public Node getNode() {
    return node;
  }

  /**
   * Returns additional information on the expression (user data of the Fortress expression node
   * describing the given expression).
   * 
   * @return a {@link NodeInfo} object.
   */

  public NodeInfo getNodeInfo() {
    return (NodeInfo) node.getUserData();
  }

  /**
   * Sets additional information on the expression (user data of the Fortress expression node
   * describing the given expression).
   * 
   * @param nodeInfo A {@link NodeInfo} object to be assigned as user data to the Fortress
   *        expression node representing the given expression.
   */

  public void setNodeInfo(NodeInfo nodeInfo) {
    if (null == nodeInfo) {
      throw new NullPointerException();
    }

    node.setUserData(nodeInfo);
  }

  /**
   * Returns information on the value produced by the expression (type information and value for
   * statically calculated expressions).
   * 
   * @return Value information object.
   */

  public ValueInfo getValueInfo() {
    return getNodeInfo().getValueInfo();
  }

  /**
   * A convenience method that returns an integer value if the expression is represented by a
   * statically calculated constant integer expression.
   * 
   * @return Integer value.
   * 
   * @throws IllegalStateException if it is not a constant integer expression.
   */

  public int integerValue() {
    final ValueInfo vi = getValueInfo();

    if (vi.isConstant() && vi.isNativeOf(Integer.class)) {
      return ((Number) vi.getNativeValue()).intValue();
    }

    throw new IllegalStateException("Not a constant integer expression.");
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
    if (null == expr) {
      throw new NullPointerException();
    }

    if (expr.getValueInfo().isConstant()) {
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
      case NAMED_CONST:
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
    if (null == expr) {
      throw new NullPointerException();
    }

    assert Node.Kind.OPERATION == expr.getNode().getKind();

    final NodeOperation nodeExpr = (NodeOperation) expr.getNode();
    final NodeInfo nodeInfo = expr.getNodeInfo();

    final SourceOperator source = (SourceOperator) nodeInfo.getSource();

    if (Operator.PLUS != source.getOperator() && Operator.MINUS != source.getOperator()) {
      // Return without changes.
      return new Reduced(0, expr);
    }

    final boolean isPlus = Operator.PLUS == source.getOperator();
    assert source.getOperator().operands() == Operands.BINARY.count();

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

    final NodeInfo ni1 = (NodeInfo) node1.getUserData();
    final NodeInfo ni2 = (NodeInfo) node2.getUserData();

    final ValueInfo vi1 = ni1.getValueInfo();
    final ValueInfo vi2 = ni2.getValueInfo();

    if (vi1.isConstant() && vi2.isConstant() && vi1.equals(vi2)) {
      return true;
    }

    if (!vi1.hasEqualType(vi2)) {
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
      final SourceOperator operator1 = (SourceOperator) ni1.getSource();
      final SourceOperator operator2 = (SourceOperator) ni2.getSource();

      if (!operator1.getOperator().equals(operator2.getOperator())) {
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
}
