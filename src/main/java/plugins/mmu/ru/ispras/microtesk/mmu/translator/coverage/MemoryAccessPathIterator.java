/*
 * Copyright 2016-2017 ISP RAS (http://www.ispras.ru)
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryEngineUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicExecutor;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicResult;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuProgram;
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
    private final MemorySymbolicResult result;
    private final MemoryAccessContext context;

    public Result(
        final MemoryAccessPath path,
        final MemorySymbolicResult result,
        final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(path);
      InvariantChecks.checkNotNull(result);
      InvariantChecks.checkNotNull(context);

      this.path = path;
      this.result = result;
      this.context = context;
    }

    public MemoryAccessPath getPath() {
      return path;
    }

    public MemorySymbolicResult getResult() {
      return result;
    }

    public MemoryAccessContext getContext() {
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

    public boolean isFirst() {
      return index == 0;
    }

    public void stop() {
      index = order.length;
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
    static final int LOOKUP_DEPTH = 4;

    final MmuAction action;
    final List<Object> trajectorySuffix;
    final List<MemoryGraph.Edge> edges;

    EdgeIterator iterator;
    MemorySymbolicResult result;
    MemoryAccessContext context;

    SearchEntry(
        final MmuAction action,
        final List<Object> trajectorySuffix,
        final MemorySymbolicResult result,
        final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(action);
      InvariantChecks.checkNotNull(result);
      InvariantChecks.checkNotNull(context);
      // Parameter trajectorySuffix can be null.

      final ArrayList<MemoryGraph.Edge> edges = new ArrayList<MemoryGraph.Edge>();
      final ArrayList<MemoryGraph.Edge> allEdges = graph.getEdges(action);

      if (allEdges != null) {
        // Ignore trajectories.
        if (trajectorySuffix == null) {
          edges.addAll(allEdges);
        } else {
          for (final MemoryGraph.Edge edge : allEdges) {
            if (lookup(edge, trajectorySuffix, LOOKUP_DEPTH)) {
              edges.add(edge);
            }
          }
        }
      }

      this.action = action;
      this.trajectorySuffix = trajectorySuffix;
      this.edges = edges;
      this.iterator = new EdgeIterator(edges);
      this.result = result;
      this.context = context;
    }

    /**
     * Evaluates whether the given edge can start the given trajectory suffix.
     * 
     * @param edge the edge.
     * @param trajectory the trajectory suffix.
     * @param depth the depth of the analysis.
     * @return true iff the given trajectory is reachable via the given edge;
     */
    private boolean lookup(
        final MemoryGraph.Edge edge,
        final List<Object> trajectory,
        final int depth) {
      final Object label = !trajectory.isEmpty() ? trajectory.get(0) : null;

      if (!edge.conformsTo(label)) {
        // The trajectory cannot be reached via the given edge.
        return false;
      }

      final MmuTransition transition = edge.getTransition();
      final MmuAction targetAction = transition.getTarget();
      final ArrayList<MemoryGraph.Edge> targetEdges = graph.getEdges(targetAction);

      // This is an edge to the terminal node.
      if (targetEdges == null || targetEdges.isEmpty()) {
        return (trajectory.isEmpty() || trajectory.size() == 1 && edge.getLabel() != null);
      }

      if (depth <= 1) {
        // It is unknown whether the trajectory is reachable.
        return true;
      }

      final List<Object> trajectorySuffix = (edge.getLabel() != null)
          ? trajectory.subList(1, trajectory.size())
          : trajectory;

      for (final MemoryGraph.Edge targetEdge : targetEdges) {
        if (lookup(targetEdge, trajectorySuffix, depth - 1)) {
          return true;
        }
      }

      // The trajectory cannot be reached via the given edge.
      return false;
    }

    @Override
    public String toString() {
      return action.toString();
    }
  }

  /** Memory subsystem representation. */
  private final MmuSubsystem memory;
  /** Target trajectory. */
  private final List<Object> trajectory;
  /** Labeled memory graph. */
  private final MemoryGraph graph;
  /** Memory access type. */
  private final MemoryAccessType type;
  /** Constraints for selecting memory access paths. */
  private final MemoryAccessConstraints constraints;

  private final Stack<SearchEntry> searchStack = new Stack<>();
  private final List<MemoryAccessPath.Entry> currentPath = new ArrayList<>();

  private Result result;
  private boolean hasResult;

  private int callId = 0;

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints) {
    this(memory, graph, type, constraints,
        new MemoryAccessContext(), MemoryEngineUtils.newSymbolicResult());
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final List<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints) {
    this(memory, trajectory, graph, type, constraints,
        new MemoryAccessContext(), MemoryEngineUtils.newSymbolicResult());
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemoryAccessContext context,
      final MemorySymbolicResult result) {
    this(memory, null, graph, type, constraints, context, result);
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final List<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemoryAccessContext context,
      final MemorySymbolicResult result) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(graph);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotNull(context);
    // Parameter trajectory can be null.

    this.memory = memory;
    this.trajectory = trajectory;
    this.graph = graph;
    this.type = type;
    this.constraints = constraints;

    final MmuAction startAction = memory.getStartAction();
    final SearchEntry searchEntry = new SearchEntry(startAction, trajectory, result, context);

    this.searchStack.push(searchEntry);

    // Do not assign the result here: it can decrease performance.
    this.result = null;
    this.hasResult = false;
  }

  @Override
  public boolean hasNext() {
    if (!hasResult) {
      result = getNext();
      hasResult = true;
    }

    return result != null;
  }

  @Override
  public Result next() {
    final Result oldResult = result;

    result = getNext();
    hasResult = true;

    return oldResult;
  }

  private static final int PROGRAM_EXTRACTION_DEPTH = 10;

  private MmuProgram getNextProgram(final MmuAction source) {
    // Actions that can be used as sinks.
    Set<MmuAction> targetActions = null;

    // Check whether the transitions can be unified.
    final Collection<MemoryGraph.Edge> edges = graph.getEdges(source);

    for (final MemoryGraph.Edge edge : edges) {
      final Set<MmuAction> traceActions = new LinkedHashSet<>();

      MemoryGraph.Edge currentEdge = edge;
      for (int i = 0; i < PROGRAM_EXTRACTION_DEPTH; i++) {
        final MmuTransition transition = currentEdge.getTransition();

        // Only insignificant transitions can be unified into programs.
        if (currentEdge.getLabel() != null
            || !transition.getBufferAccesses(MemoryAccessContext.EMPTY).isEmpty()) {
          break;
        }

        final MmuAction action = transition.getTarget();
        traceActions.add(action);

        final Collection<MemoryGraph.Edge> currentEdges = graph.getEdges(action);
        if (currentEdges == null || currentEdges.size() != 1) {
          break;
        }

        currentEdge = currentEdges.iterator().next();
      }

      if (targetActions == null) {
        targetActions = traceActions;
      } else {
        targetActions.retainAll(traceActions);
      }

      if (targetActions.isEmpty()) {
        return null;
      }
    }

    final MmuAction finalAction = targetActions.iterator().next();
    final MmuProgram.Builder builder = new MmuProgram.Builder();

    builder.beginSwitch();

    for (final MemoryGraph.Edge edge : edges) {
      final MmuProgram.Builder caseBuilder = new MmuProgram.Builder();

      MemoryGraph.Edge currentEdge = edge;

      while (true) {
        final MmuTransition transition = currentEdge.getTransition();
        final MmuAction action = transition.getTarget();

        caseBuilder.add(transition);

        if (action == finalAction) {
          break;
        }

        final Collection<MemoryGraph.Edge> currentEdges = graph.getEdges(action);

        currentEdge = currentEdges.iterator().next();
      }

      builder.addCase(caseBuilder.build());
    }

    builder.endSwitch();

    return builder.build();
  }

  private MmuProgram getNextProgram(final SearchEntry searchEntry) {
    final boolean isFirstEdge = searchEntry.iterator.isFirst();

    final MemoryGraph.Edge currentEdge = searchEntry.iterator.next();
    final List<MemoryGraph.Edge> edges = searchEntry.edges;

    // If the current edge is the first edge in the list.
    if (edges.size() > 1 && isFirstEdge) {
      final MmuProgram program = getNextProgram(searchEntry.action);

      if (program != null) {
        searchEntry.iterator.stop();
        return program;
      }
    }

    final MmuProgram program = MmuProgram.ATOMIC(currentEdge.getTransition());
    program.setLabel(currentEdge.getLabel());

    Logger.debug("Single transition: %s", program);
    return program;
  }

  private void accessBuffer(final MmuProgram program, final MemoryAccessContext context) {
    for (final MmuTransition transition : program.getTransitions()) {
      for (final MmuBufferAccess bufferAccess : transition.getBufferAccesses(context)) {
        Logger.debug("accessBuffer (before): %s", context);
        context.doAccess(bufferAccess);
        Logger.debug("accessBuffer (after): %s", context);
      }
    }
  }

  private Result getNext() {
    Logger.debug("Searching for a memory access path: %s", trajectory);

    while (!searchStack.isEmpty()) {
      final SearchEntry searchEntry = searchStack.peek();

      MemorySymbolicResult result = searchEntry.result;
      MemoryAccessContext context = searchEntry.context;

      boolean isCompleted = true;

      while (searchEntry.iterator.hasNext()) {
        final List<Object> trajectory = searchEntry.trajectorySuffix;

        final MmuProgram program = getNextProgram(searchEntry);

        final MmuAction sourceAction = program.getSource();
        final MmuAction targetAction = program.getTarget();

        final Object label = program.getLabel();
        final List<Object> trajectorySuffix = (label != null)
            ? (trajectory != null ? trajectory.subList(1, trajectory.size()) : null)
            : trajectory;

        // Go on with the current context.
        MemorySymbolicResult newResult = result;
        MemoryAccessContext newContext = context;

        // Save the current context in the current entry.
        // There should be enough information to reconstruct the saved context.
        if (program.isAtomic() && searchEntry.edges.size() != 1) {
          searchEntry.result = new MemorySymbolicResult(searchEntry.result);
          searchEntry.context = new MemoryAccessContext(searchEntry.context);
        }

        if (MemoryEngineUtils.isFeasibleProgram(
            program, type, context, constraints, result /* INOUT */)) {
          isCompleted = false;

          Logger.debug("Call stack: %s", context);
          Logger.debug("DFS search: %s", searchStack.size());
          Logger.debug("Transition: %s -> %s", sourceAction, targetAction);

          // Entries to be added to the memory access path.
          final Collection<MemoryAccessPath.Entry> entries = new ArrayList<>();

          // Increment the buffer access identifier.
          accessBuffer(program, context);

          final MmuBufferAccess bufferAccess = targetAction.getBufferAccess(context);
          final MmuBuffer buffer = bufferAccess != null ? bufferAccess.getBuffer() : null;

          if (buffer == null || buffer.getKind() != MmuBuffer.Kind.MEMORY) {
            // This is a normal action.
            entries.add(MemoryAccessPath.Entry.NORMAL(program, context));
          } else {
            // This is a recursive memory call.
            final String frameId = String.format("call(%s_%s_%d)",
                sourceAction.getName(), buffer.getName(), callId++);

            Logger.debug("Recursive memory call %s", frameId);
            final MemoryAccessStack.Frame frame = context.doCall(frameId);

            Logger.debug("Memory call frame: %s", frame);
            Logger.debug("Memory call stack: %s", context.getMemoryAccessStack());

            final MemoryAccessPath.Entry call = MemoryAccessPath.Entry.CALL(program, context);
            final MemoryAccessPath.Entry ret = MemoryAccessPath.Entry.RETURN(context);

            // Call.
            final MemorySymbolicExecutor callExecutor = new MemorySymbolicExecutor(result);
            callExecutor.execute(call);

            newResult = new MemorySymbolicResult(result);
            newContext = new MemoryAccessContext(context);

            final MemoryAccessPathIterator innerIterator =
                new MemoryAccessPathIterator(memory, graph, type, constraints, context, result);

            if (innerIterator.hasNext()) {
              final Result innerResult = innerIterator.next();
              final MemoryAccessPath innerPath = innerResult.getPath();

              // Append a random inner path.
              entries.add(call);
              entries.addAll(innerPath.getEntries());
              entries.add(ret);

              // Update the symbolic result.
              newResult = innerResult.getResult();
              newContext = innerResult.getContext();
            } else {
              callId--;

              Logger.debug("Recursive memory call is infeasible");
              isCompleted = true;
            }

            // Return.
            final MemorySymbolicExecutor returnExecutor = new MemorySymbolicExecutor(newResult);
            returnExecutor.execute(ret);

            Logger.debug("Return from the recursive memory call %s", frameId);
            newContext.doReturn();
            Logger.debug("Memory call stack: %s", context.getMemoryAccessStack());
          } // If this is a recursive memory call.

          if (!isCompleted) {
            searchStack.push(new SearchEntry(targetAction, trajectorySuffix, newResult, newContext));
            currentPath.addAll(entries);

            break;
          }
        } else {
          // If feasible transition.
          Logger.debug("Infeasible: %s -> %s", sourceAction, targetAction);
        }
      } // For each outgoing edge.

      if (isCompleted) {
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

          Logger.debug("Memory access %s of length %d for trajectory %s",
              top.context.getMemoryAccessStack().isEmpty() ? "path" : "fragment",
                  path.size(), trajectory);

          return new Result(path, result, context);
        }
      }
    } // While stack is not empty.

    Logger.debug("No feasible memory access path has been found");
    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
