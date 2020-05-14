package ru.ispras.microtesk.translator.mir.graph;

import java.util.Objects;

public final class AdjacencyList2Form implements AdjacencyList {
  private final long[] edges;

  public AdjacencyList2Form(int size) {
    this.edges = new long[size];
  }

  @Override
  public int nodeNumber() {
    return edges.length;
  }

  @Override
  public int edgeNumber() {
    final int nnodes = nodeNumber();

    int nedges = 0;
    for (int i = 1; i <= nnodes; ++i) {
      nedges += edgeNumber(i);
    }
    return nedges;
  }

  @Override
  public int edgeNumber(int from) {
    Objects.checkIndex(from - 1, edges.length);
    final long edges = get(from);
    final int e0 = unpackEdge(edges, 0);
    final int e1 = unpackEdge(edges, 1);
    return reduceInt(e0) + reduceInt(e1);
  }

  @Override
  public int edgeOf(int from, int n) {
    Objects.checkIndex(from - 1, edges.length);
    Objects.checkIndex(n, 2);

    return unpackEdge(get(from), n);
  }

  @Override
  public void addEdge(int from, int dst) {
    Objects.checkIndex(from - 1, edges.length);
    Objects.checkIndex(dst - 1, edges.length);

    final long packed = get(from);
    final int edge0 = unpackEdge(packed, 0);
    final int edge1 = unpackEdge(packed, 1);

    if (edge0 != 0 && edge1 != 0) {
      throw new IllegalStateException(
          String.format(
            "Unable to add edge (%d, %d): has (%d, %d), (%d, %d), at most 2 allowed",
            from, dst, from, edge0, from, edge1));
    } else if (edge0 == 0) {
      set(from, dst, edge1);
    } else {
      set(from, edge0, dst);
    }
  }

  @Override
  public int removeEdge(int from, int to) {
    Objects.checkIndex(from - 1, edges.length);
    Objects.checkIndex(to - 1, edges.length);

    final long edges = get(from);
    final int e0 = unpackEdge(edges, 0);
    final int e1 = unpackEdge(edges, 1);
    if (e1 == to) {
      set(from, e0, 0);
      return to;
    } else if (e0 == to) {
      set(from, 0, e1);
      return to;
    }
    return 0;
  }

  private long get(int id) {
    return edges[id - 1];
  }

  private void set(int from, int dst0, int dst1) {
    edges[from - 1] = packEdge(dst0, dst1);
  }

  private static long packEdge(int edge0, int edge1) {
    return (((long) edge1) << 32) | (long) edge0;
  }

  private static int unpackEdge(long packed, int n) {
    final long mask32 = 0xFFFFFFFF;
    if (n == 0) {
      return (int) (packed & mask32);
    } else {
      return (int) (((packed & (mask32 << 32)) >> 32) & mask32);
    }
  }

  private static int reduceInt(int n) {
    return ((n >> 31) | (-n >>> 31)) & 0x1;
  }
}
