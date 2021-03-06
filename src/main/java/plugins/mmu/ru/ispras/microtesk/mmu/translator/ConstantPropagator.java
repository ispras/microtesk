/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator;

import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.mmu.translator.ir.Var;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ConstantPropagator {
  private final Deque<Map<String, Node>> scopes = new ArrayDeque<>();
  private final Deque<Set<String>> assigned = new ArrayDeque<>();

  public ConstantPropagator() {
    scopes.push(new HashMap<String, Node>());
    assigned.push(new HashSet<String>());
  }

  public void newScope() {
    scopes.push(new HashMap<>(scopes.peek()));
  }

  public void popScope() {
    scopes.pop();
  }

  public void newInvalidationChain() {
    assigned.push(new HashSet<String>());
  }

  public void invalidateAssigned() {
    final Set<String> chain = assigned.pop();
    assigned.peek().addAll(chain);
    scopes.peek().keySet().removeAll(chain);
  }

  public Node get(final Node node) {
    if (ExprUtils.isVariable(node)) {
      final String name = ((NodeVariable) node).getName();
      final Node value = scopes.peek().get(name);
      if (value != null) {
        return value;
      }
    }

    return node;
  }

  public void assign(final Node lhs, final Node rhs) {
    final Set<String> names = collectNames(collectLhs(lhs));
    assigned.peek().addAll(names);

    if (ExprUtils.isVariable(lhs) && ExprUtils.isValue(rhs)) {
      final String name = ((NodeVariable) lhs).getName();

      scopes.peek().put(name, rhs);
      names.remove(name);
    }

    scopes.peek().keySet().removeAll(names);
  }

  private static Set<NodeVariable> collectLhs(final Node node) {
    if (ExprUtils.isVariable(node)) {
      return Collections.singleton((NodeVariable) node);
    }

    if (ExprUtils.isOperation(node)) {
      return collectLhs(node, new HashSet<NodeVariable>());
    }

    return Collections.emptySet();
  }

  private static Set<NodeVariable> collectLhs(final Node node, final Set<NodeVariable> bag) {
    if (ExprUtils.isVariable(node)) {
      bag.add((NodeVariable) node);
    } else if (ExprUtils.isOperation(node, StandardOperation.BVCONCAT)) {
      for (final Node operand : ((NodeOperation) node).getOperands()) {
        collectLhs(operand, bag);
      }
    } else if (ExprUtils.isOperation(node, StandardOperation.BVEXTRACT)) {
      collectLhs(((NodeOperation) node).getOperand(2), bag);
    }

    if (bag.isEmpty()) {
      return Collections.emptySet();
    }

    return bag;
  }

  private static Set<String> collectNames(final Collection<NodeVariable> nodes) {
    if (nodes.isEmpty()) {
      return Collections.emptySet();
    }

    final Set<String> names = new HashSet<>();
    for (final NodeVariable node : nodes) {
      names.add(node.getName());
      if (node.getUserData() instanceof Var) {
        collectNames((Var) node.getUserData(), names);
      }
    }

    return names;
  }

  private static int collectNames(final Var var, final Set<String> bag) {
    int n = 0;

    if (bag.add(var.getName())) {
      ++n;
    }

    for (final Var field : var.getFields().values()) {
      n += collectNames(field, bag);
    }

    return n;
  }
}
