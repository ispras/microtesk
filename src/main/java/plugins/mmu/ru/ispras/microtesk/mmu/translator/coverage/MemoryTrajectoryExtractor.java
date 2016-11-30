/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * Given a memory subsystem specification and an abstraction (labeling) function,
 * {@link MemoryTrajectoryExtractor} extracts all possible trajectories (abstract paths). 
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryTrajectoryExtractor {

  public static final class Result {
    private final Collection<Collection<Object>> trajectories;
    private final MemoryGraph graph;

    public Result() {
      final Collection<Object> trajectory = Collections.<Object>emptyList();

      this.trajectories = Collections.<Collection<Object>>singleton(trajectory);
      this.graph = new MemoryGraph();
    }

    public Result(final Collection<Collection<Object>> trajectories, final MemoryGraph graph) {
      InvariantChecks.checkNotNull(trajectories);
      InvariantChecks.checkNotNull(graph);

      this.trajectories = trajectories;
      this.graph = graph;
    }

    public Collection<Collection<Object>> getTrajectories() {
      return trajectories;
    }

    public MemoryGraph getGraph() {
      return graph;
    }
  }

  private static final class SearchEntry {
    final MmuAction action;
    final Collection<MemoryGraph.Edge> edges;
    final Iterator<MemoryGraph.Edge> iterator;

    SearchEntry(final MmuAction action, final Collection<MemoryGraph.Edge> edges) {
      InvariantChecks.checkNotNull(action);
      InvariantChecks.checkNotNull(edges);

      this.action = action;
      this.edges = edges;
      this.iterator = edges.iterator();
    }
  }

  /** Reference to the memory specification. */
  private final MmuSubsystem memory;

  public MemoryTrajectoryExtractor(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);
    this.memory = memory;
  }

  public Result apply(final MemoryGraphAbstraction abstraction) {
    InvariantChecks.checkNotNull(abstraction);

    // Memory graph to be labeled.
    final MemoryGraph graph = new MemoryGraph();
    graph.addEdges(memory.getTransitions());

    // Each action is mapped to the pair of the set of trajectories.
    final Map<MmuAction, Collection<Collection<Object>>> actionTrajectories = new HashMap<>();

    // Classical DFS-based graph exploration.
    final Stack<SearchEntry> searchStack = new Stack<>();

    final MmuAction startAction = memory.getStartAction();
    final Collection<MemoryGraph.Edge> startTransitions = graph.getEdges(startAction);

    searchStack.push(new SearchEntry(startAction, startTransitions));

    while (!searchStack.isEmpty()) {
      final SearchEntry searchEntry = searchStack.peek();
      boolean hasTraversed = true;

      while (searchEntry.iterator.hasNext()) {
        final MemoryGraph.Edge edge = searchEntry.iterator.next();
        final MmuTransition transition = edge.getTransition();
        final MmuAction targetAction = transition.getTarget();

        // If the target action has not been traversed, it is added to the DFS stack.
        if (!actionTrajectories.containsKey(targetAction)) {
          // Check whether there is a loop.
          for (final SearchEntry entry : searchStack) {
            InvariantChecks.checkFalse(entry.action == targetAction);
          }

          final Collection<MemoryGraph.Edge> targetEdges = graph.getEdges(targetAction);

          searchStack.push(new SearchEntry(targetAction,
              targetEdges != null
                ? targetEdges
                : Collections.<MemoryGraph.Edge>emptyList()));

          hasTraversed = false;
          break;
        }
      }

      if (hasTraversed) {
        if (searchEntry.edges.isEmpty()) {
          Logger.debug("Processing terminal action %s", searchEntry.action);

          actionTrajectories.put(searchEntry.action,
            Collections.<Collection<Object>>singleton(Collections.<Object>emptyList()));
        } else {
          Logger.debug("Processing action %s", searchEntry.action);
          final Collection<Collection<Object>> trajectories = new LinkedHashSet<>();

          for (final MemoryGraph.Edge edge : searchEntry.edges) {
            final MmuTransition transition = edge.getTransition();
            Logger.debug("Processing transition %s", transition);

            final MmuAction targetAction = transition.getTarget();
            final Collection<Collection<Object>> targetTrajectories =
              actionTrajectories.get(targetAction);

            // Perform abstraction of the transition.
            final Object label = abstraction.apply(memory, transition);
            Logger.debug("Transition has abstracted to %s", label);

            final Set<Object> nextLabels = graph.getNextLabels(targetAction);

            // Calculate trajectories.
            if (label == null) {
              trajectories.addAll(targetTrajectories);
            } else {
              for (final Collection<Object> oldTrajectory : targetTrajectories) {
                final Collection<Object> newTrajectory = new ArrayList<>();

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

    return new Result(actionTrajectories.get(startAction), graph);
  }
}
