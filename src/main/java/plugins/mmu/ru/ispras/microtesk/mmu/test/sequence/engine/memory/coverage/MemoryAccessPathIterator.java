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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage;

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
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.DataType;
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
  /**
   * {@link Result} represents an item returned by {@link MemoryAccessPathIterator}.
   */
  public static final class Result {
    private final MemoryAccessPath path;
    private final MemorySymbolicResult result;

    public Result(final MemoryAccessPath path, final MemorySymbolicResult result) {
      InvariantChecks.checkNotNull(path);
      InvariantChecks.checkNotNull(result);

      this.path = path;
      this.result = result;
    }

    public MemoryAccessPath getPath() {
      return path;
    }

    public MemorySymbolicResult getResult() {
      return result;
    }
  }

  /**
   * {@link OutgoingEdgeIterator} implements a random-order iterator of outgoing edges.
   */
  private static final class OutgoingEdgeIterator implements Iterator<MemoryGraph.Edge> {
    final ArrayList<MemoryGraph.Edge> edges;

    private final Integer order[];
    private int index;

    public OutgoingEdgeIterator(final ArrayList<MemoryGraph.Edge> edges) {
      InvariantChecks.checkNotNull(edges);

      this.edges = edges;

      this.order = new Integer[edges.size()];
      this.index = 0;

      for (int i = 0; i < order.length; i++) {
        order[i] = i;
      }

      // Randomize order of traversal.
      Randomizer.get().permute(order);
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

  /**
   * {@link SearchStackEntry} represents a DFS stack entry.
   */
  private final class SearchStackEntry {
    public static final int LOOKUP_DEPTH = 4;

    public final int rollbackCount;
    public final MmuAction action;
    public final List<Object> trajectorySuffix;
    public final List<MemoryGraph.Edge> outgoingEdges;

    private OutgoingEdgeIterator iterator;
    private MemorySymbolicResult result;

    public SearchStackEntry(
        final int rollbackCount,
        final MmuAction action,
        final List<Object> trajectorySuffix,
        final MemorySymbolicResult result) {
      InvariantChecks.checkNotNull(action);
      // Parameter trajectorySuffix can be null.
      InvariantChecks.checkNotNull(result);

      final ArrayList<MemoryGraph.Edge> outgoingEdges = new ArrayList<MemoryGraph.Edge>();
      final ArrayList<MemoryGraph.Edge> allEdges = graph.getEdges(action);

      if (allEdges != null) {
        // Ignore trajectories.
        if (trajectorySuffix == null) {
          outgoingEdges.addAll(allEdges);
        } else {
          for (final MemoryGraph.Edge edge : allEdges) {
            if (lookup(edge, trajectorySuffix, LOOKUP_DEPTH)) {
              outgoingEdges.add(edge);
            }
          }
        }
      }

      this.rollbackCount = rollbackCount;
      this.action = action;
      this.trajectorySuffix = trajectorySuffix;
      this.outgoingEdges = outgoingEdges;
      this.iterator = new OutgoingEdgeIterator(outgoingEdges);
      this.result = result;
    }

    /**
     * Estimates whether the given edge can start the given trajectory suffix.
     * 
     * @param edge the edge to be checked.
     * @param trajectory the trajectory suffix.
     * @param depth the depth of the analysis.
     * @return {@code true} iff the given trajectory is reachable via the given edge.
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
  /** User-defined constraints for selecting memory access paths. */
  private final MemoryAccessConstraints constraints;

  private final Stack<SearchStackEntry> searchStack = new Stack<>();
  private final List<MemoryAccessPath.Entry> currentPath = new ArrayList<>();

  /** Maximum number of recursive memory calls. */
  private final int recursionLimit;

  private Result result;
  private boolean hasResult;

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final int recursionLimit) {
    this(
        memory,
        graph,
        type,
        constraints,
        MemoryEngineUtils.newSymbolicResult(),
        recursionLimit);
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final List<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final int recursionLimit) {
    this(
        memory,
        trajectory,
        graph,
        type,
        constraints,
        MemoryEngineUtils.newSymbolicResult(),
        recursionLimit);
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemorySymbolicResult result,
      final int recursionLimit) {
    this(memory, null, graph, type, constraints, result, recursionLimit);
  }

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final List<Object> trajectory,
      final MemoryGraph graph,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final MemorySymbolicResult result,
      final int recursionLimit) {
    InvariantChecks.checkNotNull(memory);
    // Parameter trajectory can be null (in this case, the trajectory is ignored).
    InvariantChecks.checkNotNull(graph);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    this.memory = memory;
    this.trajectory = trajectory;
    this.graph = graph;
    this.type = type;
    this.constraints = constraints;

    final MmuAction startAction = memory.getStartAction();
    final SearchStackEntry searchEntry = new SearchStackEntry(0, startAction, trajectory, result);

    this.searchStack.push(searchEntry);

    this.recursionLimit = recursionLimit;

    // Do not perform result = getNext() here (this will decrease the initialization time).
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

  /**
   * Returns a program, i.e. a hammock of transitions, outgoing from the given source.
   * 
   * @param source the source action.
   * @return the next program.
   */
  private MmuProgram getNextProgram(final MmuAction source) {
    // Actions that can serve as hammock sinks.
    Set<MmuAction> targetActions = null;

    // Check whether the transitions can be unified.
    final Collection<MemoryGraph.Edge> edges = graph.getEdges(source);

    for (final MemoryGraph.Edge edge : edges) {
      final Set<MmuAction> traceActions = new LinkedHashSet<>();

      MemoryGraph.Edge currentEdge = edge;
      for (int i = 0; i < PROGRAM_EXTRACTION_DEPTH; i++) {
        final MmuTransition transition = currentEdge.getTransition();

        final Collection<MmuBufferAccess> bufferAccesses =
            transition.getBufferAccesses(MemoryAccessContext.EMPTY);

        // Only insignificant transitions can be unified into hammocks.
        // Transition is significant if it has a non-null label or accesses a buffer.
        if (currentEdge.getLabel() != null || !bufferAccesses.isEmpty()) {
          break;
        }

        final MmuAction action = transition.getTarget();
        traceActions.add(action);

        // Only hammocks of the following kind are derived (this can be improved further):
        // sets of linear paths that start in the same source and end in the same sink.
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

  private MmuProgram getNextProgram(final SearchStackEntry searchEntry) {
    // This should be done before calling the next() method.
    final boolean isFirstEdge = searchEntry.iterator.isFirst();

    final MemoryGraph.Edge currentEdge = searchEntry.iterator.next();
    final List<MemoryGraph.Edge> edges = searchEntry.outgoingEdges;

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

    return program;
  }

  private static void accessBuffer(final MmuProgram program, final MemoryAccessContext context) {
    for (final MmuTransition transition : program.getTransitions()) {
      for (final MmuBufferAccess bufferAccess : transition.getBufferAccesses(context)) {
        context.doAccess(bufferAccess);
      }
    }
  }

  private static List<Object> getTrajectorySuffix(final List<Object> trajectory, final Object label) {
    if (trajectory == null || label == null) {
      return trajectory;
    }

    return trajectory.subList(1, trajectory.size());
  }

  private static MemoryAccessPath.Entry getEntry(
      final MmuProgram program,
      final MemoryAccessContext context) {
    final MemoryAccessContext copyContext = new MemoryAccessContext(context);

    // Increment the buffer access identifier.
    accessBuffer(program, copyContext);

    final MmuAction targetAction = program.getTarget();
    final MmuBufferAccess bufferAccess = targetAction.getBufferAccess(MemoryAccessContext.EMPTY);
    final MmuBuffer buffer = bufferAccess != null ? bufferAccess.getBuffer() : null;

    // Context should contain updated buffer access identifiers.
    return (buffer == null || buffer.getKind() != MmuBuffer.Kind.MEMORY)
        ? MemoryAccessPath.Entry.NORMAL(program, copyContext)
        : MemoryAccessPath.Entry.CALL(program, copyContext);
  }

  private static MemoryAccessType getType(final MmuProgram program) {
    final MmuAction targetAction = program.getTarget();

    final MmuBufferAccess bufferAccess = targetAction.getBufferAccess(MemoryAccessContext.EMPTY);
    InvariantChecks.checkNotNull(bufferAccess);

    final DataType dataType = DataType.type(bufferAccess.getBuffer().getBitSize() >> 3);

    return bufferAccess.getEvent() == BufferAccessEvent.READ
        ? MemoryAccessType.LOAD(dataType)
        : MemoryAccessType.STORE(dataType);
  }

  private Result getNext() {
    Logger.debug("Searching for a memory access path: %s", trajectory);

    while (!searchStack.isEmpty()) {
      final SearchStackEntry searchEntry = searchStack.peek();

      boolean isCompletedPath = true;

      while (searchEntry.iterator.hasNext()) {
        final List<Object> trajectory = searchEntry.trajectorySuffix;
        final MmuProgram program = getNextProgram(searchEntry);

        // Go on with the current context.
        MemorySymbolicResult result = searchEntry.result;

        // If there are several outgoing edges, save the current context in the entry.
        if (program.isAtomic() && searchEntry.outgoingEdges.size() != 1) {
          searchEntry.result = new MemorySymbolicResult(searchEntry.result);
        }

        final MemoryAccessPath.Entry entry = getEntry(program, result.getContext());

        if (MemoryEngineUtils.isFeasibleEntry(entry, type, constraints, result /* INOUT */)) {
          isCompletedPath = false;

          // Entries to be added to the memory access path.
          final Collection<MemoryAccessPath.Entry> entries = new ArrayList<>();
          entries.add(entry);

          MemorySymbolicResult newResult;

          if (entry.getKind() == MemoryAccessPath.Entry.Kind.NORMAL) {
            newResult = result;
          } else /* MemoryAccessPath.Entry.Kind.CALL */ {
            final MemoryAccessStack stack = result.getContext().getMemoryAccessStack();

            if (stack.size() > recursionLimit) {
              newResult = result;
              isCompletedPath = true;
            } else {
              newResult = new MemorySymbolicResult(result);

              final MemoryAccessPathIterator innerIterator = new MemoryAccessPathIterator(
                  memory, graph, getType(program), constraints, result, recursionLimit - 1);

              while (innerIterator.hasNext()) {
                final Result innerResult = innerIterator.next();
                final MemoryAccessPath innerPath = innerResult.getPath();

                // Access to a memory-mapped buffer should reach the memory.
                if (!innerPath.contains(memory.getTargetBuffer())) {
                  continue;
                }

                // Append a random inner path.
                entries.addAll(innerPath.getEntries());
                entries.add(MemoryAccessPath.Entry.RETURN(newResult.getContext()));

                // Update the symbolic result.
                newResult = innerResult.getResult();
                break;
              }

              // Recursive memory call is infeasible.
              isCompletedPath = !innerIterator.hasNext();
            }

            // Return.
            final MemorySymbolicExecutor returnExecutor = new MemorySymbolicExecutor(newResult);
            returnExecutor.execute(MemoryAccessPath.Entry.RETURN(newResult.getContext()));
          }

          if (!isCompletedPath) {
            final MmuAction target = program.getTarget();
            final List<Object> suffix = getTrajectorySuffix(trajectory, program.getLabel());

            searchStack.push(new SearchStackEntry(entries.size(), target, suffix, newResult));
            currentPath.addAll(entries);

            break;
          }
        } // If the entry is feasible.
      } // For each outgoing edge.

      if (isCompletedPath) {
        final boolean isFullPath = memory.getTransitions(searchEntry.action).isEmpty();
        final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

        if (isFullPath) {
          builder.addAll(currentPath);
        }

        final SearchStackEntry topEntry = searchStack.pop();

        for (int i = 0; i < topEntry.rollbackCount; i++) {
          currentPath.remove(currentPath.size() - 1);
        }

        if (isFullPath) {
          final MemoryAccessPath path = builder.build();

          Logger.debug("Memory access %s of length %d for trajectory %s",
              topEntry.result.getContext().getMemoryAccessStack().isEmpty() ? "path" : "fragment",
              path.size(), trajectory);

          return new Result(path, topEntry.result);
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
