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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.Generator;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Call;

/**
 * {@link BranchTemplateIterator} implements an iterator of branch structures and traces.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTemplateIterator implements Generator<Call> {
  private boolean hasValue;
  private int maxBranchExecution;
  private BranchStructureIterator branchStructureIterator;
  private BranchTraceIterator branchTraceIterator;
  private Map<Integer, BranchTraceSituation> situations;
  private final Map<String, BranchTraceSituation> situationProvider;

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

  private boolean[] conditional;

  public BranchTemplateIterator(
      final BranchStructureIterator branchStructureIterator,
      final int maxBranchExecution,
      final Map<String, BranchTraceSituation> situationProvider) {

    InvariantChecks.checkNotNull(branchStructureIterator);
    InvariantChecks.checkNotNull(situationProvider);
    InvariantChecks.checkTrue(maxBranchExecution >= 0);

    this.branchStructureIterator = branchStructureIterator;
    this.maxBranchExecution = maxBranchExecution;
    this.situationProvider = situationProvider;

    init();

    classifyInstructions();

    // All branch instructions of the same group are either conditional or unconditional.
    for (int i = 0; i < branches.size(); i++) {
      final Set<Call> branchGroup = branches.get(i);

      for (final Call branch : branchGroup) {
        InvariantChecks.checkTrue(branch.isConditionalBranch() == conditional[i]);
      }
    }
  }

  private BranchTemplateIterator(final BranchTemplateIterator r) {
    hasValue = r.hasValue;
    maxBranchExecution = r.maxBranchExecution;
    situations = new LinkedHashMap<>(r.situations);
    situationProvider = r.situationProvider;
    branchStructureIterator = r.branchStructureIterator.clone();
    branchTraceIterator = r.branchTraceIterator.clone();

    branches = new ArrayList<>(r.branches);
    allSlots = new ArrayList<>(r.allSlots);
    safeSlots = new ArrayList<>(r.safeSlots);
    allBlocks = new ArrayList<>(r.allBlocks);

    allInstructions = new LinkedHashSet<>(r.allInstructions);
    safeInstructions = new LinkedHashSet<>(r.safeInstructions);

    if (r.conditional != null) {
      System.arraycopy(r.conditional, 0, conditional =
          new boolean[r.conditional.length], 0, r.conditional.length);
    }
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
        // Use a safe delay slot if an exception can cause infinite looping.
        if (canExceptionCauseInfiniteLooping(i - 1)) {
          final Call call = chooseSafeDelaySlot(entry.getGroupId());
          sequence.add(call);
        } else {
          final Call call = chooseDelaySlot(entry.getGroupId());
          sequence.add(call);
        }
      }
    }

    return sequence;
  }

  private boolean initBranchStructureIterator() {
    branchStructureIterator.init();

    while (branchStructureIterator.hasValue()) {
      if (initBranchTraceIterator(branchStructureIterator.value(), maxBranchExecution)) {
        return true;
      }

      branchStructureIterator.next();
    }

    return branchStructureIterator.hasValue();
  }

  private boolean initBranchTraceIterator(
      final BranchStructure structure, final int maxBranchExecution) {
    branchTraceIterator = new BranchTraceIterator(structure, maxBranchExecution);
    branchTraceIterator.init();

    return branchTraceIterator.hasValue();
  }

  @Override
  public void init() {
    hasValue = true;

    if (!initBranchStructureIterator()) {
      stop();
      return;
    }
    if (!initBranchTraceIterator(branchStructureIterator.value(), maxBranchExecution)) {
      stop();
      return;
    }
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  private boolean nextBranchStructureIterator() {
    if (branchStructureIterator.hasValue()) {
      branchStructureIterator.next();

      while (branchStructureIterator.hasValue()) {
        if (initBranchTraceIterator(branchStructureIterator.value(), maxBranchExecution)) {
          return true;
        }

        branchStructureIterator.next();
      }
    }

    return branchStructureIterator.hasValue();
  }

  private boolean nextBranchTraceIterator() {
    if (branchTraceIterator.hasValue()) {
      branchTraceIterator.next();
    }

    return branchTraceIterator.hasValue();
  }

  @Override
  public void next() {
    if (!hasValue()) {
      return;
    }
    if (nextBranchTraceIterator()) {
      return;
    }
    if (nextBranchStructureIterator()) {
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
