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

import java.util.LinkedHashSet;
import java.util.Set;

import ru.ispras.testbase.knowledge.iterator.BooleanIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link BranchExecution} represents a single execution of a branch instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchExecution implements Iterator<Boolean> {
  private final BooleanIterator iterator;

  /** Sequence of basic blocks between two executions of the branch instruction. */
  private final Set<Integer> blocks = new LinkedHashSet<>();

  /** Sequence of delay slots between two executions of branch instruction. */
  private final Set<Integer> slots = new LinkedHashSet<>();

  /** Block coverage count (number of blocks executions followed by the branch execution). */
  private int blockCoverageCount;

  /** Slot coverage count (number of slot executions followed by the branch execution). */
  private int slotCoverageCount;

  /**
   * Constructs a branch execution;
   * 
   * @param conditionalBranch the flag that indicates if the branch is conditional or not.
   */
  public BranchExecution(final boolean conditionalBranch) {
    iterator = new BooleanIterator();

    iterator.init();
    iterator.setValue(!conditionalBranch);
  }

  public BranchExecution() {
    this(true);
  }

  private BranchExecution(final BranchExecution r) {
    iterator = r.iterator.clone();

    blockCoverageCount = r.blockCoverageCount;
    slotCoverageCount  = r.slotCoverageCount;

    blocks.addAll(r.blocks);
    slots.addAll(r.slots);
  }

  /**
   * Returns the block coverage count.
   * 
   * @return the block coverage count.
   */
  public int getBlockCoverageCount() {
    return blockCoverageCount;
  }

  /**
   * Sets the block coverage count.
   * 
   * @param count the coverage count to be set.
   */
  public void setBlockCoverageCount(final int count) {
    this.blockCoverageCount = count;
  }

  /**
   * Returns the slot coverage count.
   * 
   * @return the slot coverage count.
   */
  public int getSlotCoverageCount() {
    return slotCoverageCount;
  }

  /**
   * Sets the slot coverage count.
   * 
   * @param count the coverage count to be set.
   */
  public void setSlotCoverageCount(final int count) {
    this.slotCoverageCount = count;
  }

  /**
   * Returns the trace segment consisting of basic blocks.
   * 
   * @return the sequence of basic blocks between two executions of the branch instruction.
   */
  public Set<Integer> getBlockSegment() {
    return blocks;
  }

  /**
   * Returns the trace segment consisting of delay slots.
   * 
   * @return the sequence of delay slots between two executions of the branch instruction.
   */
  public Set<Integer> getSlotSegment() {
    return slots;
  }

  /** Clears the trace segment. */
  public void clear() {
    blockCoverageCount = 0;
    slotCoverageCount = 0;

    blocks.clear();
    slots.clear();
  }

  @Override
  public void init() {
    iterator.init();

    clear();
  }

  @Override
  public boolean hasValue() {
    return iterator.hasValue();
  }

  @Override
  public Boolean value() {
    return iterator.value();
  }

  @Override
  public void next() {
    iterator.next();

    clear();
  }

  @Override
  public void stop() {
    iterator.stop();
  }

  @Override
  public String toString() {
    return value() + (blocks.isEmpty() ? "" : " " + blocks.toString());
  }

  @Override
  public BranchExecution clone() {
    return new BranchExecution(this);
  }
}
