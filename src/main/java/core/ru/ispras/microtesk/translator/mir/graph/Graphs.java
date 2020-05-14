package ru.ispras.microtesk.translator.mir.graph;

import java.util.Objects;

public final class Graphs {
  public static int[] topologicalOrder(final AdjacencyList g) {
    final var sort = new TopologicalSort(g);
    sort.visitAll();
    return sort.ordered;
  }

  private static final class TopologicalSort {
    final AdjacencyList g;
    final byte[] marks;
    final int[] ordered;
    final int ntotal;
    int ndone = 0;

    TopologicalSort(final AdjacencyList g) {
      this.g  = Objects.requireNonNull(g);

      int nnodes = g.nodeNumber();
      this.marks = new byte[nnodes];
      this.ordered = new int[nnodes];
      this.ntotal = nnodes;
    }

    void visitAll() {
      for (int i = 0; i < ntotal; ++i) {
        if (marks[i] == 0) {
          visit(i + 1);
        }
      }
    }

    void visit(int nodeid) {
      final int index = nodeid - 1;
      final int mark = marks[index];
      if (mark == 0) {
        marks[index] = 1;
        final int nedges = g.edgeNumber(nodeid);
        for (int i = 0; i < nedges; ++i) {
          visit(g.edgeOf(nodeid, i));
        }
        marks[index] = 2;
        ordered[ntotal - ndone - 1] = nodeid;
        ++ndone;
      } else if (mark == 1) {
        throw new IllegalStateException("Input graph contains cycle");
      }
    }
  }

  private Graphs() { }
}
