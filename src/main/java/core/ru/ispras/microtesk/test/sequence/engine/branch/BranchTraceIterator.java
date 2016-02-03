/*
 * Copyright 2009-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.engine.branch;

import java.util.Stack;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@BranchTraceIterator} implements an iterator of execution traces for a given branch structure.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTraceIterator implements Iterator<BranchStructure> {
  /** Current branch structure. */
  private final BranchStructure branchStructure;

  /** Upper bound of branch occurrences in a trace. */
  private final int maxBranchExecutions;
  /** Upper bound of execution traces. */
  private final int maxExecutionTraces;

  /** Stack of branches. */
  private final Stack<Integer> branchStack;

  /** Current branch index. */
  private int currentBranch;
  /** Count of iterated traces. */
  private int traceCount;

  /** Flag that reflects availability of the value. */
  private boolean hasValue;

  public BranchTraceIterator(
      final BranchStructure branchStructure,
      final int maxBranchExecutions,
      final int maxExecutionTraces) {
    InvariantChecks.checkNotNull(branchStructure);
    InvariantChecks.checkTrue(maxBranchExecutions >= 0);
    InvariantChecks.checkTrue(maxExecutionTraces >= 0 || maxExecutionTraces == -1);

    this.branchStructure = branchStructure;
    this.maxBranchExecutions = maxBranchExecutions;
    this.maxExecutionTraces = maxExecutionTraces;
    this.branchStack = new Stack<Integer>();

    this.hasValue = false;
  }

  public BranchTraceIterator(final BranchStructure branchStructure, final int maxExecutionTraces) {
    this(branchStructure, 1, maxExecutionTraces);
  }

  public BranchTraceIterator(final BranchStructure branchStructure) {
    this(branchStructure, 1, -1);
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

    currentBranch = 0;
    traceCount = 0;

    searchNextBranch();

    if (currentBranch != -1) {
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
  public BranchStructure value() {
    InvariantChecks.checkTrue(hasValue());
    return branchStructure;
  }

  @Override
  public void next() {
    while (hasValue()) {
      final BranchTraceConstructor branchTraceConstructor =
          new BranchTraceConstructor(nextBranchStructure());

      if (hasValue() && branchTraceConstructor.construct()) {
        break;
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

    this.branchStructure = r.branchStructure.clone();
    this.maxBranchExecutions = r.maxBranchExecutions;
    this.maxExecutionTraces = r.maxExecutionTraces;
    this.currentBranch = r.currentBranch;
    this.hasValue = r.hasValue;

    this.branchStack = new Stack<Integer>();
    this.branchStack.addAll(r.branchStack);
  }

  private void performBranching() {
    final BranchEntry entry = branchStructure.get(currentBranch);
    final BranchTrace trace = entry.getBranchTrace();
    final BranchExecution execution = trace.getLastExecution();

    currentBranch = execution.value() ? entry.getBranchLabel() : currentBranch + 1;
  }

  private void searchNextBranch() {
    for (; currentBranch < branchStructure.size(); currentBranch++) {
      final BranchEntry entry = branchStructure.get(currentBranch);

      if (entry.isBranch()) {
        return;
      }
    }

    currentBranch = -1;
  }

  private void handleBranch() {
    performBranching();
    searchNextBranch();
  }

  private boolean isTraceCompleted() {
    return currentBranch == -1;
  }

  private BranchStructure nextBranchStructure() {
    while (hasValue()) {
      final boolean isTraceCompleted = isTraceCompleted();

      if (isTraceCompleted) {
        currentBranch = branchStack.peek();
      }

      BranchEntry entry = branchStructure.get(currentBranch);
      BranchTrace trace = entry.getBranchTrace();
      BranchExecution execution = trace.isEmpty() ? null : trace.getLastExecution();

      if (!isTraceCompleted && trace.size() < maxBranchExecutions) {
        // Prolong the trace if it is not completed.
        trace.addExecution(entry.isIfThen());
        branchStack.push(currentBranch);

        handleBranch();

        if (isTraceCompleted()) {
          return branchStructure;
        } else {
          continue;
        }
      } else {
        if (isTraceCompleted) {
          if (!trace.isEmpty() && execution.hasValue()) {
            // Try to change last execution
            execution.next();

            if (execution.hasValue()) {
              handleBranch();

              if (isTraceCompleted()) {
                return branchStructure;
              } else {
                continue;
              }
            }
          }
        }

        // Backtracking.
        while (!branchStack.isEmpty()) {
          currentBranch = branchStack.peek();

          entry = branchStructure.get(currentBranch);
          trace = entry.getBranchTrace();
          execution = trace.getLastExecution();

          if (execution.hasValue()) {
            execution.next();

            if (execution.hasValue()) {
              handleBranch();

              if (isTraceCompleted()) {
                return branchStructure;
              } else {
                break;
              }
            }
          }

          trace.removeLastExecution();
          branchStack.pop();
        }

        if (branchStack.isEmpty()) {
          stop();
          return branchStructure;
        }

        continue;
      }
    }

    return branchStructure;
  }
}
