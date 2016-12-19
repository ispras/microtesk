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
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryEngineUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicExecutor;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicResult;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemoryAccessPathIterator} implements a DFS-based iterator of memory access paths.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessPathIterator implements Iterator<MemoryAccessPathIterator.Result> {
  public static final class Result {
    private final MemoryAccessPath path;
    private final MemorySymbolicResult context;

    public Result(final MemoryAccessPath path, final MemorySymbolicResult context) {
      InvariantChecks.checkNotNull(path);
      InvariantChecks.checkNotNull(context);

      this.path = path;
      this.context = context;
    }

    public MemoryAccessPath getPath() {
      return path;
    }

    public MemorySymbolicResult getContext() {
      return context;
    }
  }

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

      // Randomize order of traversal.
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
    final MemorySymbolicResult context;

    SearchEntry(
        final MmuAction action,
        final Object label,
        final MemorySymbolicResult context) {
      InvariantChecks.checkNotNull(action);
      InvariantChecks.checkNotNull(context);

      final ArrayList<MemoryGraph.Edge> edges = new ArrayList<MemoryGraph.Edge>();
      final ArrayList<MemoryGraph.Edge> allEdges = graph.getEdges(action);

      if (allEdges != null) {
        for (final MemoryGraph.Edge edge : allEdges) {
          if (labels == null /* Ignore a trajectory */ || edge.conformsTo(label)) {
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

  /** Memory subsystem representation. */
  private final MmuSubsystem memory;
  /** Target trajectory. */
  private final Iterator<Object> labels;
  /** Labeled memory graph. */
  private final MemoryGraph graph;
  /** Memory access type. */
  private final MemoryAccessType type;
  /** Constraints for selecting memory access paths. */
  private final MemoryAccessConstraints constraints;

  private final Stack<SearchEntry> searchStack = new Stack<>();
  private final List<MemoryAccessPath.Entry> currentPath = new ArrayList<>();

  private final MemoryAccessStack stack;

  private Result result;

  private int callId = 0;

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints) {
    this(memory, graph, type, constraints, new MemoryAccessStack());
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemorySymbolicResult context) {
    this(memory, null, graph, type, constraints, new MemoryAccessStack(), context);
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final Collection<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints) {
    this(memory, trajectory, graph, type, constraints, new MemoryAccessStack());
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemoryAccessStack stack) {
    this(memory, graph, type, constraints, stack, new MemorySymbolicResult());
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemoryAccessStack stack,
      final MemorySymbolicResult context) {
    this(memory, null, graph, type, constraints, stack, context);
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final Collection<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemoryAccessStack stack) {
    this(memory, trajectory, graph, type, constraints, stack, new MemorySymbolicResult());
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final Collection<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemoryAccessStack stack,
      final MemorySymbolicResult context) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(graph);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(stack);

    this.memory = memory;
    // If trajectory is null, it is ignored.
    this.labels = trajectory != null ? trajectory.iterator() : null;
    this.graph = graph;
    this.type = type;
    this.constraints = constraints;

    final Object label = labels != null && labels.hasNext() ? labels.next() : null;
    final MmuAction startAction = memory.getStartAction();
    final SearchEntry searchEntry = new SearchEntry(startAction, label, context);

    this.searchStack.push(searchEntry);
    this.stack = stack;
    this.result = getNext();
  }

  @Override
  public boolean hasNext() {
    return result != null;
  }

  @Override
  public Result next() {
    final Result result = this.result;
    this.result = getNext();

    return result;
  }

  private Result getNext() {
    while (!searchStack.isEmpty()) {
      final SearchEntry searchEntry = searchStack.peek();
      boolean hasTraversed = true;

      while (searchEntry.iterator.hasNext()) {
        final MemoryGraph.Edge edge = searchEntry.iterator.next();

        final MmuTransition transition = edge.getTransition();
        final MmuAction targetAction = transition.getTarget();

        final Object label = edge.getLabel();
        final Object nextLabel = label != null ?
            (labels != null && labels.hasNext() ? labels.next() : null) : searchEntry.label;

        final MemorySymbolicResult context = searchEntry.edges.size() == 1 ?
            searchEntry.context : new MemorySymbolicResult(searchEntry.context);

        MemorySymbolicResult newContext = context;

        if (MemoryEngineUtils.isFeasibleTransition(transition, type, stack, context /* INOUT */)) {
          hasTraversed = false;
          Logger.debug("Goto %s", transition);

          // Transitions to be added to the memory access path.
          final Collection<MemoryAccessPath.Entry> entries = new ArrayList<>();

          // Prolong the memory access path.
          entries.add(MemoryAccessPath.Entry.NORMAL(transition, stack));

          final MmuBufferAccess bufferAccess = targetAction.getBufferAccess(stack);

          if (bufferAccess != null) {
            final MmuBuffer buffer = bufferAccess.getBuffer();

            InvariantChecks.checkNotNull(buffer);

            // Check whether this is a recursive memory call.
            if (buffer.getKind() == MmuBuffer.Kind.MEMORY) {
              final String frameId = String.format("call(%s_%d)", buffer.getName(), callId++);
              final MemoryAccessStack.Frame frame = stack.call(frameId);

              final MmuAddressInstance formalArg = bufferAccess.getAddress();
              final MmuAddressInstance actualArg = bufferAccess.getArgument();

              Logger.debug("Recursive memory call %s(%s = %s)",
                  buffer.getName(), formalArg, actualArg);

              final MemoryAccessPath.Entry call =
                  MemoryAccessPath.Entry.CALL(frame, formalArg, actualArg);
              final MemoryAccessPath.Entry ret =
                  MemoryAccessPath.Entry.RETURN();

              newContext = new MemorySymbolicResult(context);

              final MemorySymbolicExecutor callExecutor = new MemorySymbolicExecutor(newContext);

              // Call.
              callExecutor.execute(call, false);

              final MemoryAccessPathIterator innerIterator =
                  new MemoryAccessPathIterator(memory, graph, type, constraints, stack, context);

              if (innerIterator.hasNext()) {
                final Result innerResult = innerIterator.next();
                final MemoryAccessPath innerPath = innerResult.getPath();

                // Append a random inner path.
                entries.add(call);
                entries.addAll(innerPath.getEntries());
                entries.add(ret);

                // Update the context.
                newContext = innerResult.getContext();

                final MemorySymbolicExecutor returnExecutor = new MemorySymbolicExecutor(newContext);

                // Return.
                Logger.debug("Return from the recursive memory call");
                returnExecutor.execute(ret, false);
              } else {
                callId--;

                Logger.debug("Recursive memory call is infeasible");
                hasTraversed = true;
              }

              stack.ret();
            }
          }

          if (!hasTraversed) {
            searchStack.push(new SearchEntry(targetAction, nextLabel, newContext));
            currentPath.addAll(entries);
            Logger.debug("Current path size is %d", currentPath.size());

            break;
          }
        } // If feasible transition.
      } // For each outgoing edge.

      if (hasTraversed) {
        final boolean isFullPath = memory.getTransitions(searchEntry.action).isEmpty();
        final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

        if (isFullPath) {
          builder.addAll(currentPath);
        }

        final SearchEntry top = searchStack.pop();

        if (!currentPath.isEmpty()) {
          currentPath.remove(currentPath.size() - 1);
        }

        if (isFullPath) {
          final MemoryAccessPath path = builder.build();

          Logger.debug("Checking feasibility of the memory access path %s", path);

          if (MemoryEngineUtils.isFeasiblePath(memory, path, constraints)) {
            Logger.debug("Feasible memory access path");
            return new Result(path, top.context);
          }

          Logger.debug("Infeasible memory access path");
        }
      }
    } // for each vertex.

    Logger.debug("No path found");
    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
