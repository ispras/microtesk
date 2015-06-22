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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BranchTrace} represents an execution trace of a branch instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTrace {
  private final List<BranchExecution> executions = new ArrayList<>();

  public BranchTrace() {}

  private BranchTrace(final BranchTrace r) {
    for (final BranchExecution execution : r.executions) {
      executions.add(execution.clone());
    }
  }

  public int size() {
    return executions.size();
  }

  public boolean isEmpty() {
    return executions.isEmpty();
  }

  public BranchExecution get(final int i) {
    InvariantChecks.checkBoundsInclusive(i, executions.size());
    return executions.get(i);
  }

  /**
   * Returns the last execution of the trace.
   * 
   * @return the last execution.
   */
  public BranchExecution getLastExecution() {
    InvariantChecks.checkNotEmpty(executions);
    return executions.get(executions.size() - 1);
  }

  /**
   * Adds an execution to the trace.
   */
  public void add() {
    executions.add(new BranchExecution());
  }

  /**
   * Adds an execution to the trace.
   * 
   * @param conditionalBranch the type of the branch instruction.
   */
  public void addExecution(boolean conditionalBranch) {
    executions.add(new BranchExecution(conditionalBranch));
  }

  /**
   * Removes the last execution from the trace.
   */
  public void removeLastExecution() {
    InvariantChecks.checkNotEmpty(executions);
    executions.remove(executions.size() - 1);
  }

  /**
   * Returns the number of true conditions in the trace.
   * 
   * @return the number of true conditions.
   */
  public int getTrueNumber() {
    int count = 0;

    for (final BranchExecution execution : executions) {
      if (execution.value()) {
        count++;
      }
    }

    return count;
  }

  /**
   * Returns the number of false conditions in the trace.
   * 
   * @return the number of false conditions.
   */
  public int getFalseNumber() {
    int count = 0;

    for (final BranchExecution execution : executions) {
      if (!execution.value()) {
        count++;
      }
    }

    return count;
  }

  /**
   * Returns the number of condition changes in the trace.
   * 
   * @return the number of condition changes.
   */
  public int getChangeNumber() {
    if (executions.size() < 2) {
      return 0;
    }

    int count = 0;

    for (int i = 1; i < executions.size(); i++) {
      final BranchExecution pre = executions.get(i - 1);
      final BranchExecution now = executions.get(i);

      if (pre.value() != now.value()) {
        count++;
      }
    }

    return count;
  }

  /**
   * Returns the first position of condition change for simple branching.
   * 
   * @return the first position of condition change or 0.
   */
  public int getChangePosition() {
    final BranchExecution first = executions.get(0);

    for (int i = 1; i < executions.size(); i++) {
      final BranchExecution execution = executions.get(i);

      if (first.value() != execution.value()) {
        return i;
      }
    }

    return 0;
  }

  /**
   * Checks whether the branch is fictitious (condition does not change) or not.
   * 
   * @return {@code true} if the branch is fictitious; {@code false} otherwise.
   */
  public boolean isFictitious() {
    return getChangeNumber() == 0;
  }

  /**
   * Checks whether the branch is simple (condition does not change more than one time) or not.
   * 
   * @return {@code true} if the branch is simple; {@code false} otherwise.
   */
  public boolean isSimple() {
    return getChangeNumber() <= 1;
  }

  /**
   * Checks whether the branch is pointed (there is only one branch execution which condition
   * equals to the negation of the first one.
   * 
   * @return {@code true} if the branch is pointed; {@code false} otherwise.
   */
  public boolean isPointed() {
    int count = 0;

    final BranchExecution first = executions.get(0);

    for (int i = 1; i < executions.size(); i++) {
      final BranchExecution execution = executions.get(i);

      if (first.value() != execution.value()) {
        count++;
      }
    }

    return count == 1;
  }

  /**
   * Returns the condition value of the given execution.
   * 
   * @param i the execution index.
   * @return the condition value.
   */
  public boolean getCondition(int i) {
    final BranchExecution execution = executions.get(i);
    return execution.value();
  }

  @Override
  public BranchTrace clone() {
    return new BranchTrace(this);
  }
}
