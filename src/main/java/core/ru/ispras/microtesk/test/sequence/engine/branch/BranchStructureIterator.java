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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.IntRangeIterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;

/**
 * {@link BranchStructureExecutionIterator} implements an iterator of valid branch structures for given branch
 * instructions (conditional and unconditional jumps, procedures calls, etc.).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchStructureIterator implements Iterator<BranchStructure> {
  public static enum Flags {
    /**
     * Do not iterate consecutive basic blocks.
     */
    DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS,
  }

  /** Heuristics flags used by default. */
  public static final EnumSet<Flags> DEFAULT_FLAGS = EnumSet.of(
      Flags.DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS);

  /** Heuristics flags. */
  private final EnumSet<Flags> flags;

  private final int ifThenNumber;
  private final int gotoNumber;
  private final int blockNumber;
  private final int slotNumber;

  /** Existence of a branch delay slot. */
  private final boolean delaySlot;

  /** Minimal length of branch structure (number of branches and basic blocks). */
  private final int minLength;

  /** Maximal length of branch structure (number of branches and basic blocks). */
  private final int maxLength;

  /** Minimal number of branch instructions. */
  private final int minBranchNumber;

  /** Maximal number of branch instructions. */
  private final int maxBranchNumber;

  /** Maximal number of branch executions. */
  private final int maxBranchExecution;

  /** Flag that reflects availability of the value. */
  private boolean hasValue;

  /** Iterator of branch structure lengths. */
  private IntRangeIterator lengthIterator;

  /** Iterator of branch instruction numbers. */
  private IntRangeIterator branchNumberIterator;

  /** Iterator of branch positions. */
  private IntSampleIterator branchPositionIterator;

  /** Iterator of branch labels. */
  private ProductIterator<Integer> branchLabelIterator;

  /** Iterator of branch instruction groups. */
  private ProductIterator<Integer> branchIterator;

  /** Iterator of delay slot groups. */
  private ProductIterator<Integer> slotIterator;

  /** Iterator of basic block groups. */
  private ProductIterator<Integer> blockIterator;

  public BranchStructureIterator(
      final int ifThenNumber,
      final int gotoNumber,
      final int blockNumber,
      final int slotNumber,
      final boolean delaySlot,
      final int minLength,
      final int maxLength,
      final int minBranchNumber,
      final int maxBranchNumber,
      final int maxBranchExecution,
      final EnumSet<Flags> flags) {

    InvariantChecks.checkTrue(ifThenNumber >= 0);
    InvariantChecks.checkTrue(gotoNumber >= 0);
    InvariantChecks.checkTrue(blockNumber >= 0);
    InvariantChecks.checkTrue(slotNumber >= 0);
    InvariantChecks.checkTrue(minLength > 0);
    InvariantChecks.checkTrue(minBranchNumber > 0);
    InvariantChecks.checkTrue(maxBranchNumber > 0);
    InvariantChecks.checkTrue(minLength <= maxLength);
    InvariantChecks.checkTrue(minBranchNumber <= maxBranchNumber);
    InvariantChecks.checkTrue(minBranchNumber <= maxLength);
    InvariantChecks.checkTrue(maxBranchNumber <= maxLength);
    InvariantChecks.checkTrue(minLength >= minBranchNumber);

    int effectiveMaxLength = maxLength;

    if (flags.contains(Flags.DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS)) {
      final int maxLengthUpperBound = 3 * maxBranchNumber + 1;

      InvariantChecks.checkTrue(minLength <= maxLengthUpperBound);

      if (maxLength > maxLengthUpperBound) {
        effectiveMaxLength = maxLengthUpperBound;
      }
    }

    this.ifThenNumber = ifThenNumber;
    this.gotoNumber = gotoNumber;
    this.blockNumber = blockNumber;
    this.slotNumber = slotNumber;
    this.delaySlot = delaySlot;
    this.minLength = minLength;
    this.maxLength = effectiveMaxLength;
    this.minBranchNumber = minBranchNumber;
    this.maxBranchNumber = maxBranchNumber;
    this.maxBranchExecution = maxBranchExecution;
    this.flags = flags;

    init();
  }

  public BranchStructureIterator(
      final int ifThenNumber,
      final int gotoNumber,
      final int blockNumber,
      final int slotNumber,
      final boolean delaySlot,
      final int minLength,
      final int maxLength,
      final int minBranchNumber,
      final int maxBranchNumber,
      final int maxBranchExecution) {
    this(ifThenNumber, gotoNumber, blockNumber, slotNumber, delaySlot, minLength, maxLength,
        minBranchNumber, maxBranchNumber, maxBranchExecution, DEFAULT_FLAGS);
  }

  private BranchStructureIterator(final BranchStructureIterator r) {
    ifThenNumber = r.ifThenNumber;
    gotoNumber = r.gotoNumber;
    blockNumber = r.blockNumber;
    slotNumber = r.slotNumber;
    delaySlot = r.delaySlot;
    minLength = r.minLength;
    maxLength = r.maxLength;
    minBranchNumber = r.minBranchNumber;
    maxBranchNumber = r.maxBranchNumber;
    maxBranchExecution = r.maxBranchExecution;
    flags = r.flags;
    hasValue = r.hasValue;
    lengthIterator = r.lengthIterator.clone();
    branchNumberIterator = r.branchNumberIterator.clone();
    branchIterator = r.branchIterator.clone();
    slotIterator = r.slotIterator.clone();
    blockIterator = r.blockIterator.clone();
    branchPositionIterator = r.branchPositionIterator.clone();
    branchLabelIterator = r.branchLabelIterator.clone();
  }

  @Override
  public BranchStructure value() {
    int i, j, branch, block;

    final int length = lengthIterator.value();
    final int branchNumber = branchNumberIterator.value();

    final BranchStructure structure = new BranchStructure(length + (delaySlot ? branchNumber : 0));

    // Positions of branch instructions in the test template.
    final int[] array = branchPositionIterator.indexArrayValue();

    for (i = j = branch = block = 0; i < length; i++, j++) {
      BranchEntry entry = structure.get(j);

      // Process the branch instruction.
      if (branch < branchNumber && i == array[branch]) {
        int branchLabel = 0;
        int branchClass = 0;

        if (branchLabelIterator != null) {
          branchLabel = branchLabelIterator.value(branch);
        }

        if (branchIterator != null) {
          branchClass = branchIterator.value(branch);
        }

        entry.setType(branchClass < ifThenNumber ? BranchEntry.Type.IF_THEN : BranchEntry.Type.GOTO);
        entry.setBranchLabel(branchLabel);
        entry.setGroupId(branchClass);

        // Process the delay slot.
        if (delaySlot) {
          int slotClass = 0;

          if (slotIterator != null) {
            slotClass = slotIterator.value(branch);
          }

          final BranchEntry slot = structure.get(++j);

          slot.setType(BranchEntry.Type.DELAY_SLOT);
          slot.setGroupId(slotClass);
        }

        branch++;
      }
      // Process the basic block.
      else {
        int blockClass = 0;

        if (blockIterator != null) {
          blockClass = blockIterator.value(block);
        }

        entry.setType(BranchEntry.Type.BASIC_BLOCK);
        entry.setGroupId(blockClass);

        block++;
      }
    }

    return structure;
  }

  private boolean filterBranchPositionIteratorConsecutiveBasicBlocks() {
    int branchNumber = branchNumberIterator.value();
    int branchNumberLowerBound = 0;

    final BranchStructure structure = value();

    branchNumberLowerBound = 0;
    for (int i = 1; i < structure.size(); i++) {
      final BranchEntry pre = structure.get(i - 1);
      final BranchEntry now = structure.get(i);

      if (pre.isBasicBlock() && now.isBasicBlock()) {
        branchNumberLowerBound++;
      }
    }

    if (branchNumber < branchNumberLowerBound) {
      return false;
    }

    return true;
  }

  private boolean filterBranchPositionIterator() {
    if (flags.contains(Flags.DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS)) {
      if (!filterBranchPositionIteratorConsecutiveBasicBlocks()) {
        return false;
      }
    }

    return true;
  }

  private boolean filterBranchLabelIterator_ConsecutiveBasicBlocks() {
    final BranchStructure structure = value();

    final Set<Integer> jumps = new HashSet<Integer>();
    final Set<Integer> blocks = new HashSet<Integer>();

    for (int i = 0; i < structure.size(); i++) {
      final BranchEntry now = structure.get(i);

      if (now.isBranch()) {
        jumps.add(now.getBranchLabel());
      } else if (i > 0) {
        final BranchEntry pre = structure.get(i - 1);

        if (pre.isBasicBlock() && now.isBasicBlock()) {
          blocks.add(i);
        }
      }
    }

    return jumps.containsAll(blocks);
  }

  private boolean filterBranchLabelIterator() {
    if (flags.contains(Flags.DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS)) {
      if (!filterBranchLabelIterator_ConsecutiveBasicBlocks()) {
        return false;
      }
    }

    return true;
  }

  private boolean initLengthIterator() {
    lengthIterator = new IntRangeIterator(minLength, maxLength);
    lengthIterator.init();

    return lengthIterator.hasValue();
  }

  private boolean initBranchNumberIterator() {
    branchNumberIterator = new IntRangeIterator(minBranchNumber, maxBranchNumber);
    branchNumberIterator.init();

    return branchNumberIterator.hasValue();
  }

  private boolean initBranchPositionIterator_NoFiltration(
      final int length, final int branchNumber) {
    branchPositionIterator = new IntSampleIterator(0, length - 1, branchNumber);
    branchPositionIterator.init();

    return true;
  }

  private boolean initBranchPositionIterator(final int length, final int branchNumber) {
    branchPositionIterator = new IntSampleIterator(0, length - 1, branchNumber);
    branchPositionIterator.init();

    while (branchPositionIterator.hasValue()) {
      if (filterBranchPositionIterator()) {
        return true;
      }

      branchPositionIterator.next();
    }

    return false;
  }

  private boolean initBranchLabelIterator(final int length, final int branchNumber) {
    branchLabelIterator = new ProductIterator<Integer>();

    for (int i = 0; i < branchNumber; i++) {
      branchLabelIterator.registerIterator(new IntRangeIterator(0, (length - 1)
          + (delaySlot ? branchNumber : 0)));
    }

    branchLabelIterator.init();

    while (branchLabelIterator.hasValue()) {
      if (filterBranchLabelIterator()) {
        return true;
      }

      branchLabelIterator.next();
    }

    return false;
  }

  private boolean initBranchIterator(final int branchNumber) {
    branchIterator = new ProductIterator<Integer>();

    for (int i = 0; i < branchNumber; i++) {
      branchIterator.registerIterator(new IntRangeIterator(0, (ifThenNumber + gotoNumber) - 1));
    }

    branchIterator.init();

    return branchIterator.hasValue();
  }

  private boolean initSlotIterator(int branchNumber) {
    slotIterator = new ProductIterator<Integer>();

    for (int i = 0; i < branchNumber; i++) {
      slotIterator.registerIterator(new IntRangeIterator(0, slotNumber - 1));
    }

    slotIterator.init();

    return slotIterator.hasValue();
  }

  private boolean initBlockIterator(final int length, final int branchNumber) {
    blockIterator = new ProductIterator<Integer>();

    for (int i = 0; i < length - branchNumber; i++) {
      blockIterator.registerIterator(new IntRangeIterator(0, blockNumber - 1));
    }

    blockIterator.init();

    return blockIterator.hasValue();
  }

  @Override
  public void init() {
    hasValue = true;

    if (!initLengthIterator()) {
      stop();
      return;
    }
    if (!initBranchNumberIterator()) {
      stop();
      return;
    }
    if (!initBranchPositionIterator(minLength, minBranchNumber)) {
      stop();
      return;
    }
    if (!initBranchLabelIterator(minLength, minBranchNumber)) {
      stop();
      return;
    }
    if (!initBranchIterator(minBranchNumber)) {
      stop();
      return;
    }
    if (delaySlot && !initSlotIterator(minBranchNumber)) {
      stop();
      return;
    }
    if (!initBlockIterator(minLength, minBranchNumber)) {
      stop();
      return;
    }
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  private boolean nextLengthIterator() {
    if (lengthIterator.hasValue()) {
      lengthIterator.next();

      if (lengthIterator.hasValue()) {
        int length = lengthIterator.value();
        int branchNumber = branchNumberIterator.value();

        if (!initBranchIterator(branchNumber)) {
          return false;
        }
        if (!initSlotIterator(branchNumber)) {
          return false;
        }
        if (!initBlockIterator(length, branchNumber)) {
          return false;
        }
        if (!initBranchPositionIterator(length, branchNumber)) {
          return false;
        }
        if (!initBranchLabelIterator(length, branchNumber)) {
          return false;
        }

        return true;
      }
    }

    return false;
  }

  private boolean nextBranchNumberIterator() {
    if (branchNumberIterator.hasValue()) {
      branchNumberIterator.next();

      if (branchNumberIterator.hasValue()) {
        int length = lengthIterator.value();
        int branchNumber = branchNumberIterator.value();

        if (!initBranchIterator(branchNumber)) {
          return false;
        }
        if (!initSlotIterator(branchNumber)) {
          return false;
        }
        if (!initBlockIterator(length, branchNumber)) {
          return false;
        }
        if (!initBranchPositionIterator_NoFiltration(length, branchNumber)) {
          return false;
        }
        if (!initBranchLabelIterator(length, branchNumber)) {
          return false;
        }

        return true;
      }
    }

    return false;
  }

  private boolean nextBranchPositionIterator() {
    int length = lengthIterator.value();
    int branchNumber = branchNumberIterator.value();

    while (branchPositionIterator.hasValue()) {
      branchPositionIterator.next();

      if (branchPositionIterator.hasValue()) {
        if (filterBranchPositionIterator()) {
          if (!initBranchLabelIterator(length, branchNumber)) {
            return false;
          }
          if (!initBranchIterator(branchNumber)) {
            return false;
          }
          if (!initSlotIterator(branchNumber)) {
            return false;
          }
          if (!initBlockIterator(length, branchNumber)) {
            return false;
          }

          return true;
        }
      }
    }

    return false;
  }

  private boolean nextBranchLabelIterator() {
    int length = lengthIterator.value();
    int branchNumber = branchNumberIterator.value();

    while (branchLabelIterator.hasValue()) {
      branchLabelIterator.next();

      if (branchLabelIterator.hasValue()) {
        if (filterBranchLabelIterator()) {
          if (!initBranchIterator(branchNumber)) {
            return false;
          }
          if (!initSlotIterator(branchNumber)) {
            return false;
          }
          if (!initBlockIterator(length, branchNumber)) {
            return false;
          }

          return true;
        }
      }
    }

    return false;
  }

  private boolean nextBranchIterator() {
    int length = lengthIterator.value();
    int branchNumber = branchNumberIterator.value();

    if (branchIterator.hasValue()) {
      branchIterator.next();

      if (branchIterator.hasValue()) {
        if (!initSlotIterator(branchNumber)) {
          return false;
        }
        if (!initBlockIterator(length, branchNumber)) {
          return false;
        }

        return true;
      }
    }

    return false;
  }

  private boolean nextSlotIterator() {
    int length = lengthIterator.value();
    int branchNumber = branchNumberIterator.value();

    if (slotIterator.hasValue()) {
      slotIterator.next();

      if (slotIterator.hasValue()) {
        if (!initBlockIterator(length, branchNumber)) {
          return false;
        }

        return true;
      }
    }

    return false;
  }

  private boolean nextBlockIterator() {
    if (blockIterator.hasValue()) {
      blockIterator.next();

      if (blockIterator.hasValue()) {
        return true;
      }
    }

    return false;
  }

  /** Makes iteration iteration. */
  @Override
  public void next() {
    if (!hasValue()) {
      return;
    }
    if (nextBlockIterator()) {
      return;
    }
    if (delaySlot && nextSlotIterator()) {
      return;
    }
    if (nextBranchIterator()) {
      return;
    }
    if (nextBranchLabelIterator()) {
      return;
    }
    if (nextBranchPositionIterator()) {
      return;
    }
    if (nextBranchNumberIterator()) {
      return;
    }
    if (nextLengthIterator()) {
      return;
    }

    stop();
  }

  public void stop() {
    hasValue = false;
  }

  @Override
  public BranchStructureIterator clone() {
    return new BranchStructureIterator(this);
  }
}
