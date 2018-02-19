/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.test.engine.memory;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Given a memory subsystem specification and an abstraction (labeling) function,
 * {@link TrajectoryExtractor} extracts all possible trajectories (abstract paths).
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class TrajectoryExtractor {

  /**
   * {@link Result} represents a result of {@link TrajectoryExtractor}.
   */
  public static final class Result {
    private final Collection<List<Object>> trajectories;
    private final Graph graph;

    public Result(final Collection<List<Object>> trajectories, final Graph graph) {
      InvariantChecks.checkNotNull(trajectories);
      InvariantChecks.checkNotNull(graph);

      this.trajectories = trajectories;
      this.graph = graph;
    }

    public Collection<List<Object>> getTrajectories() {
      return trajectories;
    }

    public Graph getGraph() {
      return graph;
    }
  }

  private static final class SearchEntry {
    final MmuAction action;
    final Collection<Graph.Edge> edges;
    final Iterator<Graph.Edge> iterator;

    SearchEntry(final MmuAction action, final Collection<Graph.Edge> edges) {
      InvariantChecks.checkNotNull(action);
      InvariantChecks.checkNotNull(edges);

      this.action = action;
      this.edges = edges;
      this.iterator = edges.iterator();
    }
  }

  /** Reference to the memory specification. */
  private final MmuSubsystem memory;

  public TrajectoryExtractor(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);
    this.memory = memory;
  }

  public Result apply(final MemoryAccessType accessType, final GraphAbstraction abstraction) {
    // Parameters accessType can be null.
    InvariantChecks.checkNotNull(abstraction);

    // Memory graph to be labeled.
    final Graph graph = new Graph(memory, accessType);

    // Each action is mapped to the pair of the set of trajectories.
    final Map<MmuAction, Collection<List<Object>>> actionTrajectories = new HashMap<>();

    // Classical DFS-based graph exploration.
    final Stack<SearchEntry> searchStack = new Stack<>();

    final MmuAction startAction = memory.getStartAction();
    final Collection<Graph.Edge> startEdges = graph.getEdges(startAction);

    searchStack.push(new SearchEntry(startAction, startEdges));

    while (!searchStack.isEmpty()) {
      final SearchEntry searchEntry = searchStack.peek();
      boolean hasTraversed = true;

      while (searchEntry.iterator.hasNext()) {
        final Graph.Edge edge = searchEntry.iterator.next();
        final MmuTransition transition = edge.getTransition();
        final MmuAction targetAction = transition.getTarget();

        // If the target action has not been traversed, it is added to the DFS stack.
        if (!actionTrajectories.containsKey(targetAction)) {
          // Check whether there is a loop.
          for (final SearchEntry entry : searchStack) {
            InvariantChecks.checkFalse(entry.action == targetAction);
          }

          final Collection<Graph.Edge> targetEdges = graph.getEdges(targetAction);

          searchStack.push(new SearchEntry(targetAction,
              targetEdges != null
                ? targetEdges
                : Collections.<Graph.Edge>emptyList()));

          hasTraversed = false;
          break;
        }
      }

      if (hasTraversed) {
        if (searchEntry.edges.isEmpty()) {
          actionTrajectories.put(searchEntry.action,
              Collections.<List<Object>>singleton(Collections.<Object>emptyList()));
        } else {
          final Collection<List<Object>> trajectories = new LinkedHashSet<>();

          for (final Graph.Edge edge : searchEntry.edges) {
            final MmuTransition transition = edge.getTransition();

            final MmuAction targetAction = transition.getTarget();
            final Collection<List<Object>> targetTrajectories =
                actionTrajectories.get(targetAction);

            // Perform abstraction of the transition.
            final Object label = abstraction.apply(memory, transition);
            final Set<Object> nextLabels = graph.getNextLabels(targetAction);

            // Calculate trajectories.
            if (label == null) {
              trajectories.addAll(targetTrajectories);
            } else {
              for (final Collection<Object> oldTrajectory : targetTrajectories) {
                final List<Object> newTrajectory = new ArrayList<>();

                newTrajectory.add(label);
                newTrajectory.addAll(oldTrajectory);

                trajectories.add(newTrajectory);
              }
            }

            // Set labels.
            edge.setLabel(label);
            edge.setNextLabels(nextLabels);
          }

          actionTrajectories.put(searchEntry.action, trajectories);
        }

        // The traversed action is removed from the DFS stack.
        searchStack.pop();
      }
    }

    final Collection<List<Object>> trajectories = actionTrajectories.get(startAction);
    Logger.debug("Number of trajectories is %d", trajectories.size());
    Logger.debug("Trajectories are %s", trajectories);

    return new Result(trajectories, graph);
  }
}
