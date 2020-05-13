/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.codegen.spec;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Condition {
  public enum Type {
    AND,
    OR;

    public static Type not(final Type type) {
      InvariantChecks.checkNotNull(type);
      return type == AND ? OR : AND;
    }
  }

  private final Type type;
  private final List<Node> atoms;

  private Condition(final Node atom) {
    checkBoolean(atom);

    this.type = Type.AND;
    this.atoms = Collections.singletonList(atom);
  }

  private Condition(final Type type, final List<Node> atoms) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotEmpty(atoms);

    for (final Node atom : atoms) {
      checkBoolean(atom);
    }

    this.type = type;
    this.atoms = Collections.unmodifiableList(atoms);
  }

  private static void checkBoolean(final Node atom) {
    InvariantChecks.checkNotNull(atom);
    if (!atom.isType(DataTypeId.LOGIC_BOOLEAN)) {
      throw new IllegalStateException("Boolean expression is expected: " + atom);
    }
  }

  public boolean isSingle() {
    return atoms.size() == 1;
  }

  public Type getType() {
    return type;
  }

  public List<Node> getAtoms() {
    return atoms;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Condition [");
    sb.append(String.format("type=%s, size=%d, atoms={", type, atoms.size()));

    boolean comma = false;
    for (final Node atom : atoms) {
      if (comma) {
        sb.append(", ");
      }
      comma = true;
      sb.append(atom.toString());
    }

    sb.append("}]");
    return sb.toString();
  }

  public static Condition extract(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    if (Node.Kind.VARIABLE == expr.getKind()) {
      return new Condition(expr);
    } else if (Node.Kind.OPERATION == expr.getKind()) {
      return extract((NodeOperation) expr);
    } else {
      throw new IllegalArgumentException(String.format(
          "%s is illegal node type for conditions: %s", expr.getKind(), expr));
    }
  }

  private static Condition extract(final NodeOperation expr) {
    InvariantChecks.checkNotNull(expr);

    if (ExprUtils.isOperation(expr, StandardOperation.EQ, StandardOperation.NOTEQ)) {
      return new Condition(expr);
    }

    if (ExprUtils.isOperation(expr, StandardOperation.NOT)) {
      final Condition condition = extract(expr.getOperand(0));
      return condition.not();
    }

    if (ExprUtils.isOperation(expr, StandardOperation.AND, StandardOperation.OR)) {
      final Condition lhs = extract(expr.getOperand(0));
      final Condition rhs = extract(expr.getOperand(1));
      return ExprUtils.isOperation(expr, StandardOperation.AND) ? lhs.and(rhs) : lhs.or(rhs);
    }

    throw new IllegalStateException(
        String.format("Unsupported operatior in a condition expression: %s", expr));
  }

  public Condition not() {
    if (isSingle()) {
      return new Condition(not(atoms.get(0)));
    }

    final List<Node> notAtoms = new ArrayList<Node>(atoms.size());
    for (final Node atom : atoms) {
      notAtoms.add(not(atom));
    }

    return new Condition(Type.not(type), notAtoms);
  }

  private static Node not(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    if (ExprUtils.isVariable(expr)) {
      return Nodes.not(expr);
    }

    if (ExprUtils.isOperation(expr)) {
      return not((NodeOperation) expr);
    }

    throw new IllegalArgumentException(
        String.format("%s is illegal node type for conditions: %s", expr.getKind(), expr));
  }

  private static Node not(final NodeOperation expr) {
    InvariantChecks.checkNotNull(expr);

    if (ExprUtils.isOperation(expr, StandardOperation.NOT)) {
      return expr.getOperand(0);
    }

    if (ExprUtils.isOperation(expr, StandardOperation.EQ)) {
      return Nodes.noteq(expr.getOperand(0), expr.getOperand(1));
    }

    if (ExprUtils.isOperation(expr, StandardOperation.NOTEQ)) {
      return Nodes.eq(expr.getOperand(0), expr.getOperand(1));
    }

    throw new IllegalStateException(
        String.format("Unsupported operatior in a condition expression: %s", expr));
  }

  public Condition and(final Condition other) {
    InvariantChecks.checkNotNull(other);
    return merge(Type.AND, other);
  }

  public Condition or(final Condition other) {
    InvariantChecks.checkNotNull(other);
    return merge(Type.OR, other);
  }

  private Condition merge(final Type mergeType, final Condition other) {
    InvariantChecks.checkNotNull(other);

    if ((this.type == mergeType && other.type == mergeType)
        || (this.isSingle() && other.isSingle())
        || (this.type == mergeType && other.isSingle())
        || (other.type == mergeType && this.isSingle())) {
      final List<Node> newAtoms = new ArrayList<Node>(this.atoms);

      newAtoms.addAll(other.atoms);
      return new Condition(mergeType, newAtoms);
    }

    throw new IllegalStateException(String.format(
        "Cannot perform %s with %s and %s. Condition types must be equal.",
        mergeType, this, other));
  }

}
