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

import java.util.LinkedHashSet;
import java.util.Set;

import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Call;

/**
 * {@link BranchTraceSituation} is a basic class for branch instruction situations.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class BranchTraceSituation {
  /** Order number of the branch instruction. */
  protected int branchNumber;

  /** Index of the branch instruction in the test template. */
  protected int branchIndex;

  /** Index of the target instruction in the test template. */
  protected int branchLabel;

  /** Branch execution trace. */
  protected BranchTrace branchTrace;

  /** Block coverage. */
  protected Set<Integer> blockCoverage;

  /** Slot coverage. */
  protected Set<Integer> slotCoverage;

  /**
   * Constructs a branch trace situation.
   * 
   * @param branchNumber the order number of the branch instruction.
   * @param branchIndex the index of the branch instruction in the test template.
   * @param branchLabel the index of the target instruction the in test template.
   * @param branchTrace the branch execution trace.
   * @param blockCoverage the block coverage.
   * @param slotCoverage the slot coverage.
   */
  public BranchTraceSituation(
      final int branchNumber,
      final int branchIndex,
      final int branchLabel,
      final BranchTrace branchTrace,
      final Set<Integer> blockCoverage,
      final Set<Integer> slotCoverage) {
    this.branchNumber = branchNumber;
    this.branchIndex = branchIndex;
    this.branchLabel = branchLabel;
    this.branchTrace = branchTrace;
    this.blockCoverage = blockCoverage;
    this.slotCoverage = slotCoverage;
  }

  public BranchTraceSituation() {
    this(0, 0, 0, new BranchTrace(), null, null);
  }

  protected BranchTraceSituation(final BranchTraceSituation r) {
    branchNumber = r.branchNumber;
    branchIndex = r.branchIndex;
    branchLabel = r.branchLabel;
    branchTrace = r.branchTrace.clone();
    blockCoverage = r.blockCoverage != null ? new LinkedHashSet<Integer>(r.blockCoverage) : null;
    slotCoverage = r.slotCoverage != null ? new LinkedHashSet<Integer>(r.slotCoverage) : null;
  }

  /**
   * Returns the order number of the branch instruction.
   * 
   * @return the branch number.
   */
  public final int getBranchNumber() {
    return branchNumber;
  }

  /**
   * Sets the order number of the branch instruction.
   * 
   * @param branchNumber the branch number.
   */
  public final void setBranchNumber(int branchNumber) {
    this.branchNumber = branchNumber;
  }

  /**
   * Returns the index of the branch instruction in the test template.
   * 
   * @return the branch index.
   */
  public final int getBranchIndex() {
    return branchIndex;
  }

  /**
   * Sets the index of the branch instruction in the test template.
   * 
   * @param branchIndex> the branch index.
   */
  public final void setBranchIndex(int branchIndex) {
    this.branchIndex = branchIndex;
  }

  /**
   * Returns the index of the target instruction in test template.
   * 
   * @return the branch label.
   */
  public final int getBranchLabel() {
    return branchLabel;
  }

  /**
   * Sets the index of the target instruction in test template.
   * 
   * @param branchLabel the branch label
   */
  public final void setBranchLabel(int branchLabel) {
    this.branchLabel = branchLabel;
  }

  /**
   * Checks whether the branch is forward or not.
   * 
   * @return {@code true} if branch is forward; {@code false} otherwise.
   */
  public final boolean isForwardBranch() {
    return getBranchIndex() < getBranchLabel();
  }

  /**
   * Checks if the branch is backward or not.
   * 
   * @return {@code true} of branch is backward; {@code false} otherwise.
   */
  public final boolean isBackwardBranch() {
    return getBranchIndex() >= getBranchLabel();
  }

  /**
   * Returns branch execution trace.
   * 
   * @return branch execution trace.
   */
  public final BranchTrace getBranchTrace() {
    return branchTrace;
  }

  /**
   * Sets branch execution trace.
   * 
   * @param <code>branchTrace</code> branch execution trace.
   */
  public final void setBranchTrace(BranchTrace branchTrace) {
    this.branchTrace = branchTrace;
  }

  /**
   * Returns block coverage.
   * 
   * @return block coverage.
   */
  public final Set<Integer> getBlockCoverage() {
    return blockCoverage;
  }

  /**
   * Sets block coverage.
   * 
   * @param <code>blockCoverage</code> block coverage.
   */
  public final void setBlockCoverage(Set<Integer> blockCoverage) {
    this.blockCoverage = blockCoverage;
  }

  /**
   * Returns slot coverage.
   * 
   * @return slot coverage.
   */
  public final Set<Integer> getSlotCoverage() {
    return slotCoverage;
  }

  /**
   * Sets slot coverage.
   * 
   * @param <code>slotCoverage</code> slot coverage.
   */
  public final void setSlotCoverage(Set<Integer> slotCoverage) {
    this.slotCoverage = slotCoverage;
  }

  /**
   * Checks if block coverage is not <code>null</code>.
   * 
   * @return {@code true} if block coverage is not null; {@code false} otherwise.
   */
  public final boolean canInsertStepIntoBlock() {
    return blockCoverage != null;
  }

  /**
   * Checks if slot coverage is not <code>null</code>.
   * 
   * @return {@code true} if slot coverage is not null; {@code false} otherwise.
   */
  public final boolean canInsertStepIntoSlot() {
    return slotCoverage != null;
  }

  /**
   * Constructs operands values that satisfy branch condition.
   */
  public abstract void satisfyCondition();

  /**
   * Constructs operands values that violate branch condition.
   */
  public abstract void violateCondition();

  /**
   * Construct operands values that satisfy or violate branch condition depending on the parameter.
   * 
   * @param condition the truth value of the condition.
   */
  public final void satisfyCondition(boolean condition) {
    if (condition) {
      satisfyCondition();
    } else {
      violateCondition();
    }
  }

  /**
   * Returns the step program for the branch instruction.
   * 
   * <p>This method is invoked before calling the {@code construct} method.</p>
   * 
   * @return the step program for the branch instruction.
   */
  public abstract Sequence<Call> step();

  /**
   * Returns the preparation program for the branch instruction.
   * 
   * <p>This method is invoked after execution of the {@code construct} method.</p>
   * 
   * @return the preparation program for the branch instruction.
   */
  public abstract Sequence<Call> prepare();

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(String.format("Target: %d", branchLabel));
    builder.append(", ");
    builder.append(String.format("Trace: %d", branchTrace));

    if (blockCoverage != null) {
      builder.append(", ");
      builder.append(String.format("Blocks: %s", blockCoverage));
    }

    if (slotCoverage != null) {
      builder.append(", ");
      builder.append(String.format("Slots: %s", slotCoverage));
    }

    return builder.toString();
  }
}
