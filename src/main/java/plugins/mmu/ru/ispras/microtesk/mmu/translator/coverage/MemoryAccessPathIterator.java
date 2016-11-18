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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import ru.ispras.fortress.util.InvariantChecks;
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
  private final MemoryGraph graph;

  private final class SearchEntry {
    public final ArrayList<MmuTransition> transitions;
    public final Iterator<MmuTransition> iterator;
    public final MemorySymbolicExecutor.Result context;

    public SearchEntry(final MmuAction action, final MemorySymbolicExecutor.Result context) {
      InvariantChecks.checkNotNull(action);

      // TODO: Randomize iteration order.
      this.transitions = graph.get(action);
      this.iterator = graph.get(action).iterator();
      this.context = context;
    }
  }

  private final Stack<SearchEntry> searchStack = new Stack<>();
  private final List<MmuTransition> currentPath = new ArrayList<>();

  private MemoryAccessPath path;

  public MemoryAccessPathIterator(
      final MmuSubsystem memory,
      final MemoryGraph graph) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(graph);

    this.graph = graph;

    final MmuAction startAction = memory.getStartAction();
    final MemorySymbolicExecutor.Result context = new MemorySymbolicExecutor.Result();
    final SearchEntry searchEntry = new SearchEntry(startAction, context);

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
        final MmuTransition transition = searchEntry.iterator.next();
        final MmuAction targetAction = transition.getTarget();

        final MemorySymbolicExecutor.Result context = searchEntry.transitions.size() == 1 ?
            searchEntry.context : new MemorySymbolicExecutor.Result(searchEntry.context);

        if (MemoryEngineUtils.isFeasibleTransition(transition, context)) {
          searchStack.push(new SearchEntry(targetAction, context));
          currentPath.add(transition);

          hasTraversed = false;
          break;
        }
      }

      if (hasTraversed) {
        final boolean isFullPath = searchEntry.transitions.isEmpty();
        final MemoryAccessPath.Builder builder = new MemoryAccessPath.Builder();

        if (isFullPath) {
          builder.addAll(currentPath);
        }

        searchStack.pop();

        if (!currentPath.isEmpty()) {
          currentPath.remove(currentPath.size() - 1);
        }

        if (isFullPath) {
          return builder.build();
        }
      }
    }

    return null;
  }
}
