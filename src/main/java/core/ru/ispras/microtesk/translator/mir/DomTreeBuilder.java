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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DomTreeBuilder {
  public static Cfg newCfg(final MirContext ctx) {
    final Map<BasicBlock, Node> map = new java.util.IdentityHashMap<>();
    for (final BasicBlock bb : ctx.blocks) {
      final Node node = new Node(bb);
      map.put(bb, node);
    }
    for (final Node node : map.values()) {
      final List<BasicBlock> succList = targetsOf(node.bb);
      for (final BasicBlock bb : succList) {
        final Node succ = map.get(bb);
        node.succ.add(succ);
        succ.pred.add(node);
      }
    }

    final Cfg cfg = new Cfg();
    final Node entry = map.get(ctx.blocks.get(0));
    depthFirstOrder(entry, cfg.nodes);

    return cfg;
  }

  private static List<BasicBlock> targetsOf(final BasicBlock bb) {
    final Instruction insn = Lists.lastOf(bb.insns);
    if (insn instanceof Instruction.Branch) {
      return ((Instruction.Branch) insn).successors;
    }
    return Collections.emptyList();
  }

  private static void depthFirstOrder(final Node node, final List<Node> nodes) {
    nodes.add(node);
    node.setIndex(nodes.size());

    for (final Node succ : node.succ) {
      if (succ.semidom == 0) {
        succ.parent = node.index;
        depthFirstOrder(succ, nodes);
      }
    }
  }

  static class Cfg {
    final List<Node> nodes = new java.util.ArrayList<>();
    final List<List<Node>> bucket = new java.util.ArrayList<>();

    Cfg() {
      bucket.addAll(Collections.nCopies(nodes.size(), Collections.<Node>emptyList()));
    }

    void evaluateDom() {
      for (int i = nodes.size() - 1; i > 0; --i) {
        final Node node = nodes.get(i);
        for (final Node pred : node.pred) {
          final Node u = eval(pred);
          if (u.semidom < node.semidom) {
            node.semidom = u.semidom;
          }
        }
        addToBucket(node);
        link(getNode(node.parent), node);

        final List<Node> list = bucket.get(node.parent - 1);
        for (final Node v : list) {
          final Node u = eval(v);
          v.dom = (u.semidom < v.semidom) ? u.index : node.parent;
        }
        list.clear();
      }
      for (int i = 1; i < nodes.size(); ++i) {
        final Node node = nodes.get(i);
        if (node.dom != node.semidom) {
          node.dom = getNode(node.dom).dom;
        }
      }
      nodes.get(0).dom = 0;
    }

    private Node eval(final Node node) {
      if (node.ancestor == 0) {
        return node;
      } else {
        compress(node);
        return getNode(node.label);
      }
    }

    private void compress(final Node node) {
      final Node ancestor = getNode(node.ancestor);
      if (ancestor.ancestor != 0) {
        compress(ancestor);
        final Node label = getNode(ancestor.label);
        if (label.semidom < getNode(node.label).semidom) {
          node.label = label.index;
        }
        node.ancestor = ancestor.ancestor;
      }
    }

    private void link(final Node source, final Node target) {
      target.ancestor = source.index;
    }

    private Node getNode(final int index) {
      return nodes.get(index - 1);
    }

    private void addToBucket(final Node node) {
      final int index = node.semidom - 1;
      List<Node> list = bucket.get(index);
      if (list.isEmpty()) {
        list = new java.util.ArrayList<>();
        bucket.set(index, list);
      }
      list.add(node);
    }
  }

  static class Node {
    public final List<Node> pred = new java.util.ArrayList<>();
    public final List<Node> succ = new java.util.ArrayList<>();
    public final BasicBlock bb;

    public int parent = 0;
    public int index;
    public int semidom;
    public int dom = 0;
    public int ancestor = 0;
    public int label;

    Node(final BasicBlock bb) {
      this.bb = bb;
    }

    public void setIndex(final int index) {
      this.index = index;
      this.semidom = index;
      this.label = index;
    }
  }
}
