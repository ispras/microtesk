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

package ru.ispras.microtesk.test.sequence.branch.internal;

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
  private final int maxBranchExecution;

  /** Stack of branches. */
  private final Stack<Integer> branchStack;

  /** Current branch index. */
  private int currentBranch;

  /** Flag that reflects availability of the value. */
  private boolean hasValue;

  /**
   * Constructs a branch trace iterator.
   * 
   * @param branchStructure the branch structure whose execution traces to be iterated.
   * @param maxBranchExecution the branch execution limit.
   */
  public BranchTraceIterator(final BranchStructure branchStructure, final int maxBranchExecution) {
    InvariantChecks.checkNotNull(branchStructure);

    this.branchStructure = branchStructure;
    this.maxBranchExecution = maxBranchExecution;
    this.branchStack = new Stack<Integer>();
  }

  private BranchTraceIterator(final BranchTraceIterator r) {
    this.branchStructure = r.branchStructure.clone();
    this.maxBranchExecution = r.maxBranchExecution;
    this.currentBranch = r.currentBranch;
    this.hasValue = r.hasValue;

    this.branchStack = new Stack<Integer>();
    this.branchStack.addAll(r.branchStack);
  }

  @Override
  public void init() {
    branchStack.clear();

    for (currentBranch = 0; currentBranch < branchStructure.size(); currentBranch++) {
      BranchEntry entry = branchStructure.get(currentBranch);

      if (entry.isBranch()) {
        break;
      }
    }

    hasValue = !branchStructure.isEmpty();

    // Find the first branch execution trace.
    next();
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public BranchStructure value() {
    return branchStructure;
  }

  private void performBranching() {
    final BranchEntry entry = branchStructure.get(currentBranch);
    final BranchTrace trace = entry.getBranchTrace();
    final BranchExecution execution = trace.getLastExecution();

    currentBranch = execution.value() ? entry.getBranchLabel() : currentBranch + 1;
  }

  private void searchNextBranch() {
    for (; currentBranch < branchStructure.size(); currentBranch++) {
      BranchEntry entry = branchStructure.get(currentBranch);

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

      if (!isTraceCompleted && trace.size() < maxBranchExecution) {
        // Prolong the trace if it is not completed.
        {
          trace.addExecution(entry.isIfThen());
          branchStack.push(currentBranch);
        }

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

          {
            trace.removeLastExecution();
            branchStack.pop();
          }
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

  @Override
  public void next() {
    while (hasValue()) {
      final BranchTraceConstructor branchTraceConstructor =
          new BranchTraceConstructor(nextBranchStructure());

      if (hasValue() && branchTraceConstructor.construct()) {
        break;
      }
    }
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public BranchTraceIterator clone() {
    return new BranchTraceIterator(this);
  }
}
