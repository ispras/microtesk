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

public class LengauerTarjanDom<Node, Graph> {
  private final GraphNodes<Node, Graph> adapter;

  private final List<Node> graphNodes = new java.util.ArrayList<>();
  private final List<DTNode> treeNodes = new java.util.ArrayList<>();
  private final Map<Node, Integer> nodeIds = new java.util.HashMap<>();

  public static <N, G> LengauerTarjanDom<N, G> create(final GraphNodes<N, G> adapter) {
    return new LengauerTarjanDom<N, G>(adapter);
  }

  public LengauerTarjanDom(final GraphNodes<Node, Graph> adapter) {
    this.adapter = adapter;
  }

  public DominanceTree<Node, Graph> compute(final Node entry, final Graph graph) {
    for (int i = 0; i < adapter.nodesOf(graph).size(); ++i) {
      treeNodes.add(new DTNode(i + 1));
    }
    enumerateDepthFirst(entry, graph);
    for (int i = treeNodes.size(); i > 1; --i) {
      final DTNode w = getTreeNode(i);
      final DTNode sdom = semidomOf(w);
      sdom.semidominate(w);

      link(w.parent, w);

      for (final DTNode v : w.parent.bucket) {
        final DTNode u = eval(v);
        v.idom = (u.sdom < v.sdom) ? u.selfId : w.parent.selfId;
      }
      w.parent.bucket.clear();
    }
    for (final DTNode w : treeNodes.subList(1, treeNodes.size())) {
      if (w.idom != w.sdom) {
        w.idom = getTreeNode(w.idom).idom;
      }
    }
    treeNodes.get(0).idom = 0;

    final DominanceTree<Node, Graph> tree = new DominanceTree<>(graph, this.adapter);
    for (final DTNode node : treeNodes) {
      if (node.idom != 0) {
        tree.idom.put(graphNodes.get(node.selfId - 1), graphNodes.get(node.idom - 1));
      }
    }
    return tree;
  }

  private DTNode semidomOf(final DTNode w) {
    int sdom = w.sdom;
    for (final DTNode v : w.pred) {
      final DTNode u = eval(v);
      if (u.sdom < sdom) {
        sdom = u.sdom;
      }
    }
    return getTreeNode(sdom);
  }

  private DTNode enumerateDepthFirst(final Node entry, final Graph g) {
    final DTNode dtn = assignTreeNode(entry);
    for (final Node succ : adapter.successorsOf(entry, g)) {
      final DTNode node;
      if (!nodeIds.containsKey(succ)) {
        node = enumerateDepthFirst(succ, g);
        node.parent = dtn;
      } else {
        node = getTreeNode(succ);
      }
      node.pred.add(dtn);
    }
    return dtn;
  }

  private DTNode assignTreeNode(final Node node) {
    final int id = graphNodes.size() + 1;
    graphNodes.add(node);
    nodeIds.put(node, id);

    return getTreeNode(id);
  }

  private DTNode getTreeNode(final Node node) {
    return getTreeNode(nodeIds.get(node));
  }

  private DTNode getTreeNode(final int id) {
    return treeNodes.get(id - 1);
  }

  private DTNode eval(final DTNode node) {
    if (node.ancestor == null) {
      return node;
    }
    return compress(node);
  }

  private DTNode compress(final DTNode node) {
    final DTNode ancestor = node.ancestor;
    if (ancestor.ancestor != null) {
      final DTNode label = compress(ancestor);
      if (label.sdom < node.label.sdom) {
        node.label = label;
      }
      node.ancestor = ancestor.ancestor;
    }
    return node.label;
  }

  private void link(final DTNode source, final DTNode target) {
    target.ancestor = source;
  }

  private static final class DTNode {
    final int selfId;
    int sdom;
    int idom = 0;
    DTNode parent;
    DTNode ancestor;
    DTNode label;
    List<DTNode> pred = new java.util.ArrayList<>();
    List<DTNode> bucket = new java.util.ArrayList<>();


    DTNode(final int index) {
      this.selfId = index;
      this.sdom = index;
      this.label = this;
    }

    void semidominate(final DTNode that) {
      bucket.add(that);
      that.sdom = this.selfId;
    }
  }
}
