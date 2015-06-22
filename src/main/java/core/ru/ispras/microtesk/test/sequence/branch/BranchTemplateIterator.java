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

package ru.ispras.microtesk.test.sequence.branch;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.Generator;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.branch.internal.IntSampleIterator;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.testbase.knowledge.iterator.IntRangeIterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;

/**
 * {@link BranchTemplateIterator} implements an iterator of valid branch structures for given branch
 * instructions (conditional and unconditional jumps, procedures calls, etc.).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTemplateIterator implements Generator<Call> {
  public static enum Flags {
    /**
     * Do not iterate consecutive basic blocks.
     */
    DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS,

    /**
     * Do not insert instructions that can throw exceptions into delay slots.
     */
    DO_NOT_USE_UNSAFE_DELAY_SLOTS,

    /**
     * Do not insert instructions that can throw exceptions into delay slots if an exception
     * can cause infinite looping.
     */
    DO_NOT_USE_UNSAFE_DELAY_SLOTS_IF_EXCEPTION_CAN_CAUSE_LOOPING
  }

  /** Heuristics flags used by default. */
  public static final EnumSet<Flags> DEFAULT_FLAGS = EnumSet.of(
      Flags.DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS,
      Flags.DO_NOT_USE_UNSAFE_DELAY_SLOTS_IF_EXCEPTION_CAN_CAUSE_LOOPING);

  /** Heuristics flags. */
  private final EnumSet<Flags> flags;

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

  /** All instructions. */
  private Set<Call> allInstructions = new LinkedHashSet<>();
  
  /** Groups of branch instructions (conditional and unconditional jumps, procedure calls, etc.). */
  private List<Set<Call>> branches = new ArrayList<>();

  /** Groups of delay slots (arithmetical instructions, load/store instructions, etc.). */
  private List<Set<Call>> allSlots = new ArrayList<>();

  /** Groups of delay slots which do not cause an exception. */
  private List<Set<Call>> safeSlots = new ArrayList<>();

  /** All instructions that do not cause exceptions. */
  private Set<Call> safeInstructions = new LinkedHashSet<>();

  /** Equivalence classes of basic blocks. */
  private List<Set<Call>> allBlocks = new ArrayList<>();

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

  /** Branch trace iterator. */
  private BranchTraceIterator branchTraceIterator;

  /** Test situations of branch instruction calls. */
  private Map<Integer, BranchTraceSituation> situations;

  /** Test situation provider. */
  private final Map<String, BranchTraceSituation> situationProvider;

  private boolean[] conditional;

  public BranchTemplateIterator(
      final Sequence<Call> allInstructions,
      final boolean delaySlot,
      final int minLength,
      final int maxLength,
      final int minBranchNumber,
      final int maxBranchNumber,
      final int maxBranchExecution,
      final Map<String, BranchTraceSituation> situationProvider,
      final EnumSet<Flags> flags) {

    InvariantChecks.checkTrue(minLength > 0);
    InvariantChecks.checkTrue(minBranchNumber > 0);
    InvariantChecks.checkTrue(maxBranchNumber > 0);
    InvariantChecks.checkTrue(minLength <= maxLength);
    InvariantChecks.checkTrue(minBranchNumber <= maxBranchNumber);
    InvariantChecks.checkTrue(minBranchNumber <= maxLength);
    InvariantChecks.checkTrue(maxBranchNumber <= maxLength);
    InvariantChecks.checkTrue(minLength >= minBranchNumber);
    InvariantChecks.checkNotNull(situationProvider);

    int effectiveMaxLength = maxLength;

    if (flags.contains(Flags.DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS)) {
      final int maxLengthUpperBound = 3 * maxBranchNumber + 1;

      InvariantChecks.checkTrue(minLength <= maxLengthUpperBound);

      if (maxLength > maxLengthUpperBound) {
        effectiveMaxLength = maxLengthUpperBound;
      }
    }

    this.allInstructions.addAll(allInstructions);

    this.delaySlot = delaySlot;
    this.minLength = minLength;
    this.maxLength = effectiveMaxLength;
    this.minBranchNumber = minBranchNumber;
    this.maxBranchNumber = maxBranchNumber;
    this.maxBranchExecution = maxBranchExecution;
    this.situationProvider = situationProvider;
    this.flags = flags;

    init();

    // All branch instructions of the same group are either conditional or unconditional.
    for (int i = 0; i < branches.size(); i++) {
      final Set<Call> branchGroup = branches.get(i);

      for (final Call branch : branchGroup) {
        InvariantChecks.checkTrue(branch.isConditionalBranch() == conditional[i]);
      }
    }
  }

  public BranchTemplateIterator(
      final Sequence<Call> allInstructions,
      final boolean delaySlot,
      final int minLength,
      final int maxLength,
      final int minBranchNumber,
      final int maxBranchNumber,
      final int maxBranchExecution,
      final Map<String, BranchTraceSituation> situationProvider) {
    this(allInstructions, delaySlot, minLength, maxLength, minBranchNumber, maxBranchNumber,
        maxBranchExecution, situationProvider, DEFAULT_FLAGS);
  }

  private BranchTemplateIterator(final BranchTemplateIterator r) {
    delaySlot = r.delaySlot;
    minLength = r.minLength;
    maxLength = r.maxLength;
    minBranchNumber = r.minBranchNumber;
    maxBranchNumber = r.maxBranchNumber;
    maxBranchExecution = r.maxBranchExecution;
    situationProvider = r.situationProvider;

    flags = r.flags;

    branches = new ArrayList<>(r.branches);
    allSlots = new ArrayList<>(r.allSlots);
    safeSlots = new ArrayList<>(r.safeSlots);
    allBlocks = new ArrayList<>(r.allBlocks);

    safeInstructions = new LinkedHashSet<>(r.safeInstructions);

    hasValue = r.hasValue;

    lengthIterator = r.lengthIterator.clone();
    branchNumberIterator = r.branchNumberIterator.clone();
    branchIterator = r.branchIterator.clone();
    slotIterator = r.slotIterator.clone();
    blockIterator = r.blockIterator.clone();
    branchPositionIterator = r.branchPositionIterator.clone();
    branchLabelIterator = r.branchLabelIterator.clone();
    branchTraceIterator = r.branchTraceIterator.clone();

    if (r.conditional != null) {
      System.arraycopy(r.conditional, 0, conditional =
          new boolean[r.conditional.length], 0, r.conditional.length);
    }
  }

  /**
   * Returns the branch structure corresponding to iterators' states.
   * 
   * @return the current branch structure.
   */
  private BranchStructure getBranchStructure() {
    int i, j, branch, block;

    final int length = lengthIterator.value();
    final int branchNumber = branchNumberIterator.value();

    final BranchStructure structure = new BranchStructure(length + (delaySlot ? branchNumber : 0));

    // Positions of branch instructions in test template
    final int[] array = branchPositionIterator.indexArrayValue();

    for (i = j = branch = block = 0; i < length; i++, j++) {
      BranchEntry entry = structure.get(j);

      // Process branch
      if (branch < branchNumber && i == array[branch]) {
        int branchLabel = 0;
        int branchClass = 0;

        if (branchLabelIterator != null) {
          branchLabel = branchLabelIterator.value(branch);
        }

        if (branchIterator != null) {
          branchClass = branchIterator.value(branch);
        }

        entry.setType(conditional[branchClass] ? BranchEntry.Type.IF_THEN : BranchEntry.Type.GOTO);
        entry.setBranchLabel(branchLabel);
        entry.setGroupId(branchClass);

        // Process delay slot
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
      // Process basic block
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

  private Call chooseInstruction(final Set<Call> group) {
    InvariantChecks.checkNotEmpty(group);
    return Randomizer.get().choose(group);
  }

  private Call chooseBranch(final int groupId) {
    return chooseInstruction(branches.get(groupId));
  }

  private Call chooseBasicBlock(final int groupId) {
    return chooseInstruction(allBlocks.get(groupId));
  }

  private Call chooseDelaySlot(final int groupId) {
    return chooseInstruction(allSlots.get(groupId));
  }

  private Call chooseSafeDelaySlot(final int groupId) {
    final Set<Call> group1 = safeSlots.get(groupId);
    final Set<Call> group2 = group1.isEmpty() ? safeInstructions : group1;

    return chooseInstruction(group2);
  }

  private boolean canExceptionCauseInfiniteLooping(final int currentBranchIndex) {
    final BranchStructure structure = branchTraceIterator.value();
    final BranchEntry branch = structure.get(currentBranchIndex);

    // Branch to the delay slot or to the next instruction.
    if (branch.getBranchLabel() == currentBranchIndex + 1
        || branch.getBranchLabel() == currentBranchIndex + 2) {
      return false;
    }

    for (int i = currentBranchIndex + 2; i < structure.size(); i++) {
      final BranchEntry entry = structure.get(i);

      // There exist a backward branch.
      if (entry.isBranch() && entry.getBranchLabel() <= i) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Sequence<Call> value() {
    int i, j;

    final Sequence<Call> sequence = new Sequence<Call>();
    final BranchStructure structure = branchTraceIterator.value();

    situations = new LinkedHashMap<>();

    for (i = j = 0; i < structure.size(); i++) {
      final BranchEntry entry = structure.get(i);

      if (entry.isBranch()) {
        final Call call = chooseBranch(entry.getGroupId());
        // TODO:
        final BranchTraceSituation situation = situationProvider.get(call.getRootOperation().getName());

        situations.put(i, situation);

        situation.setBranchNumber(j++);
        situation.setBranchIndex(i);
        situation.setBranchLabel(entry.getBranchLabel());
        situation.setBranchTrace(entry.getBranchTrace());
        situation.setBlockCoverage(entry.getBlockCoverage());
        situation.setSlotCoverage(entry.getSlotCoverage());

        sequence.add(call);
      } else if (entry.isBasicBlock()) {
        final Call call = chooseBasicBlock(entry.getGroupId());
        sequence.add(call);
      } else {
        if (flags.contains(Flags.DO_NOT_USE_UNSAFE_DELAY_SLOTS)) {
          final Call call = chooseSafeDelaySlot(entry.getGroupId());
          sequence.add(call);
        } else {
          if (flags.contains(Flags.DO_NOT_USE_UNSAFE_DELAY_SLOTS_IF_EXCEPTION_CAN_CAUSE_LOOPING)
              && canExceptionCauseInfiniteLooping(i - 1)) {
            final Call call = chooseSafeDelaySlot(entry.getGroupId());
            sequence.add(call);
          } else {
            final Call call = chooseDelaySlot(entry.getGroupId());
            sequence.add(call);
          }
        }
      }
    }

    return sequence;
  }

  private boolean filterBranchPositionIterator_ConsecutiveBasicBlocks() {
    int branchNumber = branchNumberIterator.value();
    int branchNumberLowerBound = 0;

    final BranchStructure structure = getBranchStructure();

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
      if (!filterBranchPositionIterator_ConsecutiveBasicBlocks()) {
        return false;
      }
    }

    return true;
  }

  private boolean filterBranchLabelIterator_ConsecutiveBasicBlocks() {
    final BranchStructure structure = getBranchStructure();

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
        // If there are no feasible traces, try to use other labels.
        if (initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
          return true;
        }
      }

      branchLabelIterator.next();
    }

    return false;
  }

  private boolean initBranchIterator(final int branchNumber) {
    branchIterator = new ProductIterator<Integer>();

    for (int i = 0; i < branchNumber; i++) {
      branchIterator.registerIterator(new IntRangeIterator(0, branches.size() - 1));
    }

    branchIterator.init();

    return branchIterator.hasValue();
  }

  private boolean initSlotIterator(int branchNumber) {
    slotIterator = new ProductIterator<Integer>();

    for (int i = 0; i < branchNumber; i++) {
      slotIterator.registerIterator(new IntRangeIterator(0, allSlots.size() - 1));
    }

    slotIterator.init();

    return slotIterator.hasValue();
  }

  private boolean initBlockIterator(final int length, final int branchNumber) {
    blockIterator = new ProductIterator<Integer>();

    for (int i = 0; i < length - branchNumber; i++) {
      blockIterator.registerIterator(new IntRangeIterator(0, allBlocks.size() - 1));
    }

    blockIterator.init();

    return blockIterator.hasValue();
  }

  private boolean initBranchTraceIterator(
      final BranchStructure structure, final int maxBranchExecution) {
    branchTraceIterator = new BranchTraceIterator(structure, maxBranchExecution);

    branchTraceIterator.init();

    return branchTraceIterator.hasValue();
  }

  private void classifyInstructions() {
    branches.clear();
    allSlots.clear();
    safeSlots.clear();
    allBlocks.clear();
    safeInstructions.clear();

    // Simple classification.
    final Set<Call> allBranchInstructions = new LinkedHashSet<>();
    final Set<Call> allJumpInstructions = new LinkedHashSet<>();
    final Set<Call> allSafeInstructions = new LinkedHashSet<>();
    final Set<Call> allUnsafeInstructions = new LinkedHashSet<>();

    // Classify instructions.
    for (final Call call : allInstructions) {
      if (call.isBranch()) {
        if (call.isConditionalBranch()) {
          allBranchInstructions.add(call);
        } else {
          allJumpInstructions.add(call);
        }
      } else {
        if (!call.canThrowException()) {
          allSafeInstructions.add(call);
        } else {
          allUnsafeInstructions.add(call);
        }
      }
    }

    if (!allBranchInstructions.isEmpty()) {
      branches.add(allBranchInstructions);
    }
    if (!allJumpInstructions.isEmpty()) {
      branches.add(allJumpInstructions);
    }

    if (!allSafeInstructions.isEmpty()) {
      allSlots.add(allSafeInstructions);
      safeSlots.add(allSafeInstructions);
      allBlocks.add(allSafeInstructions);
    }
    if (!allUnsafeInstructions.isEmpty()) {
      allSlots.add(allUnsafeInstructions);
      allBlocks.add(allUnsafeInstructions);
    }

    // Check branch instructions.
    InvariantChecks.checkNotEmpty(branches);

    conditional = new boolean[branches.size()];
    for (int i = 0; i < branches.size(); i++) {
      final Set<Call> branchGroup = branches.get(i);

      for (final Call branch : branchGroup) {
        conditional[i] = branch.isConditionalBranch();
        break;
      }
    }
  }

  @Override
  public void init() {
    hasValue = true;

    classifyInstructions();

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
    if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
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
        if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
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
        if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
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
          if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
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

          // If there are no feasible traces, try use other labels
          if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
            continue;
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
        if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
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
        if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
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
        if (!initBranchTraceIterator(getBranchStructure(), maxBranchExecution)) {
          return false;
        }

        return true;
      }
    }

    return false;
  }

  private boolean nextBranchTraceIterator() {
    if (branchTraceIterator.hasValue()) {
      branchTraceIterator.next();

      if (branchTraceIterator.hasValue()) {
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
    if (nextBranchTraceIterator()) {
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
  public BranchTemplateIterator clone() {
    return new BranchTemplateIterator(this);
  }
}
