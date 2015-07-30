/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

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
    if (node.getKind() == Node.Kind.VARIABLE) {
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

    if (lhs.getKind() == Node.Kind.VARIABLE &&
        rhs.getKind() == Node.Kind.VALUE) {
      final String name = ((NodeVariable) lhs).getName();

      scopes.peek().put(name, rhs);
      names.remove(name);
    }
    scopes.peek().keySet().removeAll(names);
  }

  private static Set<NodeVariable> collectLhs(final Node node) {
    switch (node.getKind()) {
    case VARIABLE:
      return Collections.singleton((NodeVariable) node);

    case OPERATION:
      return collectLhs(node, new HashSet<NodeVariable>());

    }
    return Collections.emptySet();
  }

  private static Set<String> collectNames(final Collection<NodeVariable> nodes) {
    if (nodes.isEmpty()) {
      return Collections.emptySet();
    }
    final Set<String> names = new HashSet<>();
    for (final NodeVariable node : nodes) {
      names.add(node.getName());
    }
    return names;
  }

  private static Set<NodeVariable> collectLhs(final Node node,
                                              final Set<NodeVariable> bag) {
    if (node.getKind() == Node.Kind.VARIABLE) {
      bag.add((NodeVariable) node);
    } else if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      if (op.getOperationId() instanceof StandardOperation) {
        switch ((StandardOperation) op.getOperationId()) {
        case BVCONCAT:
          for (final Node operand : op.getOperands()) {
            collectLhs(operand, bag);
          }
          break;

        case BVEXTRACT:
          collectLhs(op.getOperand(2), bag);
          break;
        }
      }
    }
    if (bag.isEmpty()) {
      return Collections.emptySet();
    }
    return bag;
  }
}
