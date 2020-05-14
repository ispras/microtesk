package ru.ispras.microtesk.translator.mir.graph;

public interface AdjacencyList {
  int nodeNumber();
  int edgeNumber();
  int edgeNumber(int from);

  int edgeOf(int from, int n);

  void addEdge(int from, int to);

  default int removeEdge(int from, int n) {
    throw new UnsupportedOperationException();
  }
}
