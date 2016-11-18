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
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.utils.function.BiFunction;
import ru.ispras.microtesk.utils.function.Chooser;

/**
 * Given a memory subsystem specification and an abstraction function,
 * {@link AbstractPathExtractor} extracts all possible abstract paths. 
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AbstractPathExtractor<T> implements
    BiFunction<MmuSubsystem, Abstraction<T>, Map<Collection<T>, Chooser<MemoryAccessPath>>> {

  private static final class SearchEntry {
    public MmuAction action;
    public Collection<MmuTransition> transitions;
    public Iterator<MmuTransition> iterator;

    public SearchEntry(final MmuAction action, final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(action);
      InvariantChecks.checkNotNull(transitions);

      this.action = action;
      this.transitions = transitions;
      this.iterator = transitions.iterator();
    }
  }

  public Map<Collection<T>, Chooser<MemoryAccessPath>> apply(
      final MmuSubsystem memory,
      final Abstraction<T> abstraction) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(abstraction);

    // Each action is mapped to the outgoing abstract paths (sequences of abstract objects).
    // In turn, each abstract path is mapped to the corresponding subgraphs.
    final Map<MmuAction, Map<Collection<T>, Set<MemoryGraph>>> actionAbstractPaths = new HashMap<>();

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
        if (!actionAbstractPaths.containsKey(targetAction)) {
          final Collection<MmuTransition> targetTransitions = memory.getTransitions(targetAction);

          searchStack.push(new SearchEntry(targetAction, targetTransitions));
          hasTraversed = false;

          break;
        }
      }

      if (hasTraversed) {
        if (searchEntry.transitions.isEmpty()) {
          // Terminal actions are mapped to the empty abstract paths.
          // In turn, empty abstract paths are mapped to the empty subgraphs.
          actionAbstractPaths.put(searchEntry.action,
              Collections.singletonMap(Collections.emptyList(), Collections.emptySet()));
        } else {
          // For non-terminal actions, the abstract paths of the successive actions are composed.
          final Map<Collection<T>, Set<MemoryGraph>> abstractPaths = new HashMap<>();
          final Map<MmuTransition, Set<MemoryGraph>> transitionGraphs = new HashMap<>();

          for (final MmuTransition transition : searchEntry.transitions) {
            final Set<MemoryGraph> graphs = new HashSet<>();
            transitionGraphs.put(transition, graphs);

            final MmuAction targetAction = transition.getTarget();
            final Map<Collection<T>, Set<MemoryGraph>> suffixes =
                actionAbstractPaths.get(targetAction);

            // Perform abstraction of the transition.
            final T abstractTransition = abstraction.apply(transition);

            for (final Map.Entry<Collection<T>, Set<MemoryGraph>> entry : suffixes.entrySet()) {
              // Calculate abstract paths.
              final Collection<T> abstractPath = new ArrayList<>();

              if (abstractTransition != null) {
                abstractPath.add(abstractTransition);
              }

              abstractPath.addAll(entry.getKey());

              Set<MemoryGraph> abstractPathGraphs = abstractPaths.get(abstractPath);
              if (abstractPathGraphs == null) {
                abstractPaths.put(abstractPath, abstractPathGraphs = new HashSet<>());
              }

              for (final MemoryGraph graph : entry.getValue()) {
                final MemoryGraph graphCopy = new MemoryGraph(graph);

                abstractPathGraphs.add(graphCopy);
                graphs.add(graphCopy);
              }

              abstractPaths.put(abstractPath, abstractPathGraphs);
            }
          }

          // Add transitions to the related subgraphs.
          for (final MmuTransition transition : searchEntry.transitions) {
            final Set<MemoryGraph> graphs = transitionGraphs.get(transition);

            for (final MemoryGraph graph : graphs) {
              ArrayList<MmuTransition> out = graph.get(searchEntry.action);

              if (out == null) {
                graph.put(searchEntry.action, out = new ArrayList<>());
              }

              out.add(transition);
            }
          }

          actionAbstractPaths.put(searchEntry.action, abstractPaths);
        }

        // The traversed action is removed from the DFS stack.
        searchStack.pop();
      }
    }

    final Map<Collection<T>, Chooser<MemoryAccessPath>> result = new HashMap<>();

    for (final Map.Entry<Collection<T>, Set<MemoryGraph>> entry :
        actionAbstractPaths.get(startAction).entrySet()) {
      result.put(entry.getKey(), new MemoryAccessPathExtractor(memory, entry.getValue()));
    }

    return result;
  }
}
