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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryEngineUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemorySymbolicExecutor;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemoryAccessPathIterator} implements a DFS-based iterator of memory access paths.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessPathIterator implements Iterator<MemoryAccessPath> {

  private static final class EdgeIterator implements Iterator<MemoryGraph.Edge> {
    final ArrayList<MemoryGraph.Edge> edges;

    int index;
    int order[];

    EdgeIterator(final ArrayList<MemoryGraph.Edge> edges) {
      InvariantChecks.checkNotNull(edges);

      this.edges = edges;

      this.index = 0;
      this.order = new int[edges.size()];

      for (int i = 0; i < order.length; i++) {
        order[i] = i;
      }

      for (int i = 0; i < order.length; i++) {
        final int j = Randomizer.get().nextIntRange(0, order.length - 1);
        final int k = Randomizer.get().nextIntRange(0, order.length - 1);

        final int temp = order[j];

        order[j] = order[k];
        order[k] = temp;
      }
    }

    @Override
    public boolean hasNext() {
      return index < order.length;
    }

    @Override
    public MemoryGraph.Edge next() {
      InvariantChecks.checkBounds(index, order.length);
      return edges.get(order[index++]);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private final class SearchEntry {
    final MmuAction action;
    final Object label;
    final ArrayList<MemoryGraph.Edge> edges;
    final Iterator<MemoryGraph.Edge> iterator;
    final MemorySymbolicExecutor.Result context;

    SearchEntry(
        final MmuAction action,
        final Object label,
        final MemorySymbolicExecutor.Result context) {
      InvariantChecks.checkNotNull(action);
      InvariantChecks.checkNotNull(context);

      final ArrayList<MemoryGraph.Edge> edges = new ArrayList<MemoryGraph.Edge>();
      final ArrayList<MemoryGraph.Edge> allEdges = graph.getEdges(action);

      if (allEdges != null) {
        for (final MemoryGraph.Edge edge : allEdges) {
          if (edge.conformsTo(label)) {
            edges.add(edge);
          }
        }
      }

      this.action = action;
      this.label = label;
      this.edges = edges;
      this.iterator = new EdgeIterator(edges);
      this.context = context;
    }
  }

  private final MmuSubsystem memory;
  private final Iterator<Object> labels;
  private final MemoryGraph graph;
  private final MemoryAccessType type;
  private final MemoryAccessConstraints constraints;

  private final Stack<SearchEntry> searchStack = new Stack<>();
  private final List<MmuTransition> currentPath = new ArrayList<>();

  private MemoryAccessPath path;

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final Collection<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(trajectory);
    InvariantChecks.checkNotNull(graph);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    this.memory = memory;
    this.labels = trajectory.iterator();
    this.graph = graph;
    this.type = type;
    this.constraints = constraints;

    final Object label = labels.hasNext() ? labels.next() : null;
    final MmuAction startAction = memory.getStartAction();
    final MemorySymbolicExecutor.Result context = new MemorySymbolicExecutor.Result();
    final SearchEntry searchEntry = new SearchEntry(startAction, label, context);

    searchStack.push(searchEntry);

    this.path = getNextPath();
  }

  @Override
  public boolean hasNext() {
    return path != null;
  }

  @Override
  public MemoryAccessPath next() {
    final MemoryAccessPath result = path;

    path = getNextPath();
    return result;
  }

  private MemoryAccessPath getNextPath() {
    while (!searchStack.isEmpty()) {
      final SearchEntry searchEntry = searchStack.peek();
      boolean hasTraversed = true;

      while (searchEntry.iterator.hasNext()) {
        final MemoryGraph.Edge edge = searchEntry.iterator.next();

        final MmuTransition transition = edge.getTransition();
        final MmuAction targetAction = transition.getTarget();

        final Object label = edge.getLabel();
        final Object nextLabel = label != null ?
            (labels.hasNext() ? labels.next() : null) : searchEntry.label;

        final MemorySymbolicExecutor.Result context = searchEntry.edges.size() == 1 ?
            searchEntry.context : new MemorySymbolicExecutor.Result(searchEntry.context);

        if (MemoryEngineUtils.isFeasibleTransition(transition, type, context)) {
          searchStack.push(new SearchEntry(targetAction, nextLabel, context));
          currentPath.add(transition);

          hasTraversed = false;
          break;
        }
      }

      if (hasTraversed) {
        final boolean isFullPath = memory.getTransitions(searchEntry.action).isEmpty();
        final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

        if (isFullPath) {
          builder.addAll(currentPath);
        }

        searchStack.pop();

        if (!currentPath.isEmpty()) {
          currentPath.remove(currentPath.size() - 1);
        }

        if (isFullPath) {
          final MemoryAccessPath result = builder.build();

          if (MemoryEngineUtils.isFeasiblePath(memory, result, constraints)) {
            Logger.debug("Feasible memory access path: %s", result);
            return result;
          }

          Logger.debug("Infeasible memory access path: %s", result);
        }
      }
    }

    Logger.debug("No path found");
    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
