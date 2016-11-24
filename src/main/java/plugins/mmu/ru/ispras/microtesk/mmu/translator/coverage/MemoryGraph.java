/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemoryGraph} represents a memory subsystem's control flow graph.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryGraph {

  public static final class Edge {
    private final MmuTransition transition;
    private final Object label;
    private final Set<Object> nextLabels;

    public Edge(
        final MmuTransition transition,
        final Object label,
        final Set<Object> nextLabels) {
      InvariantChecks.checkNotNull(transition);
      InvariantChecks.checkNotNull(nextLabels);

      this.transition = transition;
      this.label = label;
      this.nextLabels = nextLabels;
    }

    public MmuTransition getTransition() {
      return transition;
    }

    public Object getLabel() {
      return label;
    }

    public Set<Object> getNextLabels() {
      return nextLabels;
    }

    public boolean conformsTo(final Object label) {
      if (label == null) {
        return this.label == null;
      }

      if (this.label != null) {
        return this.label.equals(label);
      }

      return this.nextLabels.contains(label);
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }

      if (o == null || !(o instanceof Edge)) {
        return false;
      }

      final Edge r = (Edge) o;
      return transition.equals(r.transition);
    }

    @Override
    public int hashCode() {
      return transition.hashCode();
    }

    @Override
    public String toString() {
      return transition.toString();
    }
  }

  private final Map<MmuAction, ArrayList<Edge>> edges = new LinkedHashMap<>();

  public MemoryGraph() {
  }

  public ArrayList<Edge> getEdges(final MmuAction vertex) {
    InvariantChecks.checkNotNull(vertex);
    return edges.get(vertex);
  }

  public Set<Object> getNextLabels(final MmuAction vertex) {
    InvariantChecks.checkNotNull(vertex);

    final Set<Object> nextLabels = new LinkedHashSet<>();
    final ArrayList<Edge> nextEdges = edges.get(vertex);

    if (nextEdges == null) {
      return nextLabels;
    }

    for (final Edge edge : nextEdges) {
      final Object label = edge.getLabel();

      if (label != null) {
        nextLabels.add(label);
      } else {
        nextLabels.addAll(edge.getNextLabels());
      }
    }

    return nextLabels;
  }

  public void addEdge(final MmuTransition transition, final Object label, final Set<Object> nextLabels) {
    InvariantChecks.checkNotNull(transition);

    final MmuAction vertex = transition.getSource();
    final Edge edge = new Edge(transition, label, nextLabels);

    ArrayList<Edge> out = edges.get(vertex);
    if (out == null) {
      edges.put(vertex, out = new ArrayList<>());
    }

    out.add(edge);
  }

  public void addGraph(final MemoryGraph graph) {
    InvariantChecks.checkNotNull(graph);
    edges.putAll(graph.edges);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof MemoryGraph)) {
      return false;
    }

    final MemoryGraph r = (MemoryGraph) o;
    return edges.equals(r.edges);
  }

  @Override
  public int hashCode() {
    return edges.hashCode();
  }

  @Override
  public String toString() {
    return edges.toString();
  }
}
