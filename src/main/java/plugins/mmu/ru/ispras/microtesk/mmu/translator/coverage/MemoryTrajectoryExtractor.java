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
import java.util.HashSet;
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
    final Collection<MmuTransition> transitions;
    final Iterator<MmuTransition> iterator;

    SearchEntry(
        final MmuAction action,
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(action);
      InvariantChecks.checkNotNull(transitions);

      this.action = action;
      this.transitions = transitions;
      this.iterator = transitions.iterator();
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

    // Each action is mapped to the pair of the set of trajectories and the memory graph.
    final Map<MmuAction, Result> actionResults = new HashMap<>();

    // Classical DFS-based graph exploration.
    final Stack<SearchEntry> searchStack = new Stack<>();

    final MmuAction startAction = memory.getStartAction();
    final Collection<MmuTransition> startTransitions = memory.getTransitions(startAction);

    searchStack.push(new SearchEntry(startAction, startTransitions));

    while (!searchStack.isEmpty()) {
      final SearchEntry searchEntry = searchStack.peek();
      boolean hasTraversed = true;

      while (searchEntry.iterator.hasNext()) {
        final MmuTransition transition = searchEntry.iterator.next();
        final MmuAction targetAction = transition.getTarget();

        // If the target action has not been traversed, it is added to the DFS stack.
        if (!actionResults.containsKey(targetAction)) {
          // Check whether there is a loop.
          for (final SearchEntry entry : searchStack) {
            InvariantChecks.checkFalse(entry.action == targetAction);
          }

          final Collection<MmuTransition> targetTransitions = memory.getTransitions(targetAction);

          searchStack.push(new SearchEntry(targetAction, targetTransitions));
          hasTraversed = false;

          break;
        }
      }

      if (hasTraversed) {
        Logger.debug("Processing action %s", searchEntry.action);

        final Collection<Collection<Object>> trajectories = new LinkedHashSet<>();
        final MemoryGraph graph = new MemoryGraph();

        final Collection<MmuAction> targetActions = new HashSet<>();

        for (final MmuTransition transition : searchEntry.transitions) {
          Logger.debug("Processing transition %s", transition);

          final MmuAction targetAction = transition.getTarget();
          final Result targetResult = actionResults.get(targetAction);
          final MemoryGraph targetGraph = targetResult.getGraph();

          // Perform abstraction of the transition.
          final Object label = abstraction.apply(memory, transition);
          Logger.debug("Transition has abstracted to %s", label);

          final Set<Object> nextLabels = targetGraph.getNextLabels(targetAction);

          // Calculate trajectories.
          if (label == null) {
            trajectories.addAll(targetResult.getTrajectories());
          } else {
            for (final Collection<Object> oldTrajectory : targetResult.getTrajectories()) {
              final Collection<Object> newTrajectory = new ArrayList<>();

              newTrajectory.add(label);
              newTrajectory.addAll(oldTrajectory);

              trajectories.add(newTrajectory);
            }
          }

          if (!targetActions.contains(targetAction)) {
            graph.addGraph(targetGraph);
            targetActions.add(targetAction);
          }

          graph.addEdge(transition, label, nextLabels);
        }

        Logger.debug("Action trajectories: %s", trajectories);
        actionResults.put(searchEntry.action, new Result(trajectories, graph));

        // The traversed action is removed from the DFS stack.
        searchStack.pop();
      }
    }

    return actionResults.get(startAction);
  }
}
