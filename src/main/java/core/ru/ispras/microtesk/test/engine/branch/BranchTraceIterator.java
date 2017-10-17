/*
 * Copyright 2009-2017 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test.engine.branch;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link BranchTraceIterator} implements an iterator of execution traces for a given branch
 * structure.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTraceIterator implements Iterator<List<BranchEntry>> {
  /** Current branch structure. */
  private final List<BranchEntry> branchStructure;

  /** Branch execution limit. */
  private final int maxBranchExecutions;
  /** Block execution limit. */
  private final int maxBlockExecutions;
  /** Trace count limit. */
  private final int maxExecutionTraces;

  /**
   * Stack of branches.
   * 
   * <p>
   * Each entry is a pair of indices in the branch structure:
   * <ul>
   *   <li>the first index is the destination of the previous branch (0 for the initial one);</li>
   *   <li>the second index points to the current branch.</li>
   * </ul>
   * Provided that {@code i} is the first index and {@code j} is the second index,
   * the range {@code [i, i+1, ..., j)} is composed of the basic block indices.
   * </p>
   */
  private final Stack<Pair<Integer, Integer>> branchStack;

  /** Current block. */
  private Pair<Integer, Integer> currentBlock;

  /** Count of iterated traces. */
  private int traceCount;

  /** Flag that reflects availability of the value. */
  private boolean hasValue;

  /** Execution trace. */
  private List<Integer> trace;

  public BranchTraceIterator(
      final List<BranchEntry> branchStructure,
      final int maxBranchExecutions,
      final int maxBlockExecutions,
      final int maxExecutionTraces) {
    InvariantChecks.checkNotNull(branchStructure);

    this.branchStructure = branchStructure;
    this.maxBranchExecutions = maxBranchExecutions;
    this.maxBlockExecutions = maxBlockExecutions;
    this.maxExecutionTraces = maxExecutionTraces;
    this.branchStack = new Stack<>();

    this.hasValue = false;
    this.trace = null;
  }

  public BranchTraceIterator(
      final List<BranchEntry> branchStructure,
      final int maxExecutionTraces) {
    this(branchStructure, 1, 1, maxExecutionTraces);
  }

  public BranchTraceIterator(final List<BranchEntry> branchStructure) {
    this(branchStructure, 1, 1, -1);
  }

  @Override
  public void init() {
    hasValue = !branchStructure.isEmpty();

    // Clear the execution traces.
    branchStack.clear();
    for (int i = 0; i < branchStructure.size(); i++) {
      final BranchEntry branchEntry = branchStructure.get(i);
      final BranchTrace branchTrace = branchEntry.getBranchTrace();

      branchTrace.clear();
    }

    currentBlock = new Pair<>(-1, 0);
    traceCount = 0;

    searchNextBranch();

    if (currentBlock.second != -1) {
      // Find the first branch execution trace.
      next();
    } else {
      // Do nothing: the structure does not contain branches.
    }
  }

  @Override
  public boolean hasValue() {
    return hasValue && (maxExecutionTraces == -1 || traceCount <= maxExecutionTraces);
  }

  @Override
  public List<BranchEntry> value() {
    InvariantChecks.checkTrue(hasValue());
    return branchStructure;
  }

  public List<Integer> trace() {
    return trace;
  }

  @Override
  public void next() {
    while (hasValue()) {
      final BranchTraceConstructor branchTraceConstructor =
          new BranchTraceConstructor(nextBranchStructure());

      if (hasValue()) {
        trace = branchTraceConstructor.construct();

        if (trace != null) {
          break;
        }
      }
    }

    traceCount++;
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public BranchTraceIterator clone() {
    return new BranchTraceIterator(this);
  }

  private BranchTraceIterator(final BranchTraceIterator r) {
    InvariantChecks.checkNotNull(r);

    this.branchStructure = new ArrayList<>(r.branchStructure.size());
    for (final BranchEntry entry : r.branchStructure) {
      this.branchStructure.add(entry.clone());
    }

    this.maxBranchExecutions = r.maxBranchExecutions;
    this.maxBlockExecutions = r.maxBlockExecutions;
    this.maxExecutionTraces = r.maxExecutionTraces;
    this.currentBlock = new Pair<>(r.currentBlock.first, r.currentBlock.second);
    this.hasValue = r.hasValue;

    this.branchStack = new Stack<>();
    this.branchStack.addAll(r.branchStack);
  }

  private void performBranching() {
    final int currentIndex = currentBlock.second;

    final BranchEntry entry = branchStructure.get(currentIndex);
    final BranchTrace trace = entry.getBranchTrace();
    final BranchExecution execution = trace.getLastExecution();

    // If the condition holds, then go to the target label; otherwise, go on.
    currentBlock = new Pair<>(-1, execution.value() ? entry.getBranchLabel() : currentIndex + 1);
  }

  private void searchNextBranch() {
    final int startIndex = currentBlock.second;

    for (int currentIndex = startIndex; currentIndex < branchStructure.size(); currentIndex++) {
      final BranchEntry entry = branchStructure.get(currentIndex);

      if (entry.isBranch()) {
        // Here is a branch.
        currentBlock = new Pair<>(startIndex, currentIndex);
        return;
      }
    }

    // No branch is found.
    currentBlock = new Pair<>(startIndex, -1);
  }

  private void handleBranch() {
    performBranching();
    searchNextBranch();

    if (!canExecuteBlock()) {
      currentBlock = null;
    }
  }

  private boolean isTraceCompleted() {
    return currentBlock.second == -1;
  }

  private boolean canExecuteBranch() {
    final BranchEntry entry = branchStructure.get(currentBlock.second);
    final BranchTrace trace = entry.getBranchTrace();

    // Check the branch execution limit.
    return trace.size() < maxBranchExecutions;
  }

  private boolean canExecuteBlock() {
    final int a = currentBlock.first;
    final int b = currentBlock.second != -1 ? currentBlock.second - 1 : branchStructure.size() - 1;

    // Check the block execution limit.
    int count = 0;

    for (final Pair<Integer, Integer> previousBlock : branchStack) {
      final int c = previousBlock.first;
      final int d = previousBlock.second - 1;

      // Increment the counter if the current block intersects with the previous one.
      if (a <= d && b >= c) {
        count++;
      }
    }

    return count < maxBlockExecutions;
  }

  private List<BranchEntry> nextBranchStructure() {
    while (hasValue()) {
      if (currentBlock != null && !isTraceCompleted() && canExecuteBranch()) {
        // Trace is incomplete and can be prolonged.
        final BranchEntry entry = branchStructure.get(currentBlock.second);
        final BranchTrace trace = entry.getBranchTrace();

        // Prolong the trace if it is incomplete.
        trace.addExecution(entry.isIfThen());
        branchStack.push(currentBlock);

        // Handle the branch and nullify the current block if it cannot be executed.
        handleBranch();

        if (currentBlock != null && isTraceCompleted()) {
          Logger.debug("Next branch structure (prolonged execution): %s", branchStructure);
          return branchStructure;
        }

        continue;
      } else {
        // Trace is either complete or cannot be prolonged.
        while (!branchStack.isEmpty()) {
          currentBlock = branchStack.peek();

          BranchEntry entry = branchStructure.get(currentBlock.second);
          BranchTrace trace = entry.getBranchTrace();
          BranchExecution execution = trace.getLastExecution();

          if (execution.hasValue()) {
            execution.next();

            if (execution.hasValue()) {
              // Handle the branch and nullify the current block if it cannot be executed.
              handleBranch();

              if (currentBlock != null && isTraceCompleted()) {
                Logger.debug("Next branch structure (updated condition): %s", branchStructure);
                return branchStructure;
              }

              break;
            }
          } // while the stack is not empty.

          trace.removeLastExecution();
          branchStack.pop();
        }

        if (branchStack.isEmpty()) {
          // All execution traces have been enumerated.
          stop();

          Logger.debug("Next branch structure (empty stack): %s", branchStructure);
          return branchStructure;
        }

        continue;
      }
    }

    Logger.debug("Next branch structure (return): %s", branchStructure);
    return branchStructure;
  }
}
