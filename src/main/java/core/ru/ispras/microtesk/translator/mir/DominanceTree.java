/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DominanceTree<Node, Graph> {
  final Graph graph;
  final GraphNodes<Node, Graph> adapter;

  final Map<Node, Node> idom = new java.util.HashMap<>();
  final Map<Node, Set<Node>> children = new java.util.HashMap<>();
  final Map<Node, Set<Node>> frontiers = new java.util.HashMap<>();

  public DominanceTree(final Graph graph, final GraphNodes<Node, Graph> adapter) {
    this.graph = graph;
    this.adapter = adapter;
  }

  public Map<Node, Set<Node>> calculateFrontiers() {
    final Set<Node> root = new java.util.HashSet<>(idom.values());
    root.removeAll(idom.keySet());

    final Context ctx = new Context(this.idom, this.frontiers);
    ctx.getFrontier(root.iterator().next());

    children.putAll(ctx.nodeChildren);

    return frontiers;
  }

  private List<Node> successorsOf(final Node node) {
    return adapter.successorsOf(node, graph);
  }

  private final class Context {
    final Map<Node, Node> idom;
    final Map<Node, Set<Node>> frontiers;
    final Map<Node, Set<Node>> nodeChildren;

    Context(final Map<Node, Node> idom, final Map<Node, Set<Node>> frontiers) {
      this.idom = idom;
      this.frontiers = frontiers;
      this.nodeChildren = calculateTree(idom);
    }

    Set<Node> getFrontier(final Node node) {
      Set<Node> df = frontiers.get(node);
      if (df == null) {
        df = calculateFrontier(node);
        frontiers.put(node, df);
      }
      return df;
    }

    Set<Node> calculateFrontier(final Node node) {
      final Set<Node> frontier = new java.util.LinkedHashSet<>();
      if (nodeChildren.containsKey(node)) {
        for (final Node child : nodeChildren.get(node)) {
          for (final Node cdf : getFrontier(child)) {
            if (!node.equals(idom.get(cdf))) {
              frontier.add(cdf);
            }
          }
        }
      }
      for (final Node child : successorsOf(node)) {
        if (!node.equals(idom.get(child))) {
          frontier.add(child);
        }
      }
      return frontier;
    }
  }

  private static <Node> Map<Node, Set<Node>> calculateTree(final Map<Node, Node> idom) {
    final Map<Node, Set<Node>> nodeChildren = new java.util.HashMap<>();
    for (final Map.Entry<Node, Node> entry : idom.entrySet()) {
      Set<Node> children = nodeChildren.get(entry.getValue());
      if (children == null) {
        children = new java.util.LinkedHashSet<>();
        nodeChildren.put(entry.getValue(), children);
      }
      children.add(entry.getKey());
    }
    return nodeChildren;
  }
}
