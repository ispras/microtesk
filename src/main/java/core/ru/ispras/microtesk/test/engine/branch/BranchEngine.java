/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.microtesk.test.engine.EngineUtils.getSituationName;
import static ru.ispras.microtesk.test.engine.EngineUtils.makeStreamRead;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.engine.AbstractSequence;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.SequenceSelector;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.BlockId;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

/**
 * {@link BranchEngine} implements a test engine that constructs test cases by enumerating
 * feasible execution traces of bounded length.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchEngine implements Engine {
  public static final String ID = "branch";
  public static final boolean USE_DELAY_SLOTS = true;

  /** Maximum number of executions of a single branch instruction. */
  public static final String PARAM_BRANCH_LIMIT = "branch_exec_limit";
  public static final int PARAM_BRANCH_LIMIT_DEFAULT = 1;

  /** Maximum number of execution traces to be enumerated. */
  public static final String PARAM_TRACE_LIMIT = "trace_count_limit";
  public static final int PARAM_TRACE_LIMIT_DEFAULT = -1;

  public static final String IF_THEN_SITUATION_SUFFIX = "if-then";
  public static final String GOTO_SITUATION_SUFFIX = "goto";

  public static final String ATTR_EXECUTED = "executed";

  static boolean isIfThen(final AbstractCall abstractCall) {
    // Self check.
    final String situationName = getSituationName(abstractCall);
    final boolean flag = situationName != null && situationName.endsWith(IF_THEN_SITUATION_SUFFIX);

    /*
    // FIXME: The check is temporary disabled
    final boolean result = abstractCall.isBranch() && abstractCall.isConditionalBranch();
    InvariantChecks.checkTrue(result == flag);

    return result;
    */

    return flag;
  }

  static boolean isGoto(final AbstractCall abstractCall) {
    // Self check.
    final String situationName = getSituationName(abstractCall);
    final boolean flag = situationName != null && situationName.endsWith(GOTO_SITUATION_SUFFIX);

    /*
    // FIXME: The check is temporary disabled
    final boolean result = abstractCall.isBranch() && !abstractCall.isConditionalBranch();
    InvariantChecks.checkTrue(result == flag);

    return result;
    */

    return flag;
  }

  static BranchEntry getBranchEntry(final AbstractCall abstractCall) {
    final Map<String, Object> attributes = abstractCall.getAttributes();
    return (BranchEntry) attributes.get("branchEntry");
  }

  static void setBranchEntry(final AbstractCall abstractCall, final BranchEntry branchEntry) {
    final Map<String, Object> attributes = abstractCall.getAttributes();
    attributes.put("branchEntry", branchEntry);
  }

  static String getTestDataStream(final AbstractCall abstractBranchCall) {
    InvariantChecks.checkNotNull(abstractBranchCall);

    final Primitive primitive = abstractBranchCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive);

    final Situation situation = primitive.getSituation();
    InvariantChecks.checkNotNull(situation);

    final Object testDataStream = situation.getAttribute(BranchDataGenerator.PARAM_STREAM);
    InvariantChecks.checkNotNull(testDataStream);

    return testDataStream.toString();
  }

  /** Branch execution limit: default value is 1. */
  private int maxBranchExecutions = PARAM_BRANCH_LIMIT_DEFAULT;
  /** Trace count limit: default value is -1 (no limitations). */
  private int maxExecutionTraces = PARAM_TRACE_LIMIT_DEFAULT;

  private final SequenceSelector sequenceSelector = new SequenceSelector(ID, false);

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public SequenceSelector getSequenceSelector() {
    return sequenceSelector;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);

    final Object branchExecLimit = attributes.get(PARAM_BRANCH_LIMIT);
    maxBranchExecutions = branchExecLimit != null ?
        Integer.parseInt(branchExecLimit.toString()) : PARAM_BRANCH_LIMIT_DEFAULT;

    final Object traceCountLimit = attributes.get(PARAM_TRACE_LIMIT);
    maxExecutionTraces = traceCountLimit != null ?
        Integer.parseInt(traceCountLimit.toString()) : PARAM_TRACE_LIMIT_DEFAULT;
  }

  @Override
  public Iterator<AbstractSequence> solve(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final AbstractSequence processedSequence = new AbstractSequence(abstractSequence);
    final List<AbstractCall> sequence = processedSequence.getSequence();

    // Collect information about labels and executed/non-executed calls.
    final LabelManager labelManager = new LabelManager();

    final List<AbstractCall> executedCode = new ArrayList<>();
    final List<AbstractCall> nontakenCode = new ArrayList<>();

    int branchStructureSize = 0;
    for (int i = 0; i < sequence.size(); i++) {
      final AbstractCall abstractCall = sequence.get(i);
      final Map<String, Object> attributes = abstractCall.getAttributes();

      // Do not add executed/non-executed calls to the branch structure.
      final Boolean isExecuted = (Boolean) attributes.get(ATTR_EXECUTED);

      if (isExecuted != null) {
        if (isExecuted) {
          executedCode.add(abstractCall);
        } else {
          nontakenCode.add(abstractCall);
        }

        // Replace this call with the empty one.
        sequence.set(i, AbstractCall.newEmpty());
        continue;
      }

      for (final Label label : abstractCall.getLabels()) {
        labelManager.addLabel(label, branchStructureSize);
      }

      branchStructureSize++;
    }

    // Transform the abstract sequence into the branch structure.
    final List<BranchEntry> branchStructure = new ArrayList<>();

    int autoLabel = 0;
    int delaySlot = 0;
    for (final AbstractCall abstractCall : sequence) {
      final boolean isIfThen = isIfThen(abstractCall);
      final boolean isGoto = isGoto(abstractCall);

      final BranchEntry.Type type;

      // Set the branch entry type.
      if (isIfThen) {
        // Using branches in delay slots is prohibited.
        InvariantChecks.checkTrue(delaySlot == 0);
        type = BranchEntry.Type.IF_THEN;
      } else if (isGoto) {
        // Using branches in delay slots is prohibited.
        InvariantChecks.checkTrue(delaySlot == 0);
        type = BranchEntry.Type.GOTO;
      } else if (delaySlot > 0) {
        type = BranchEntry.Type.DELAY_SLOT;
        delaySlot--;
      } else {
        type = BranchEntry.Type.BASIC_BLOCK;
      }

      final BranchEntry branchEntry = new BranchEntry(type);
      branchStructure.add(branchEntry);

      // Set the target label and start the delay slot.
      if (isIfThen || isGoto) {
        final LabelReference targetReference = abstractCall.getTargetReference();
        InvariantChecks.checkNotNull(targetReference);

        // Branching to any place in the sequence (_label).
        if (targetReference.getReference() == null) {
          // Generate the label name.
          final String name = String.format("auto_label_%d", autoLabel++);
          final BlockId blockId = new BlockId();
          final Label targetLabel = new Label(name, blockId);

          // Put the label in a random position.
          final int targetIndex = Randomizer.get().nextIntRange(0, branchStructureSize - 1);
          final AbstractCall targetCall = sequence.get(targetIndex);
          targetCall.getLabels().add(targetLabel);

          // Patch the label in the call.
          targetReference.setReference(targetLabel);
          // Associate the label with the chosen position.
          labelManager.addLabel(targetLabel, targetIndex);
        }

        final Label targetLabel = targetReference.getReference();
        final LabelManager.Target target = labelManager.resolve(targetLabel);
        InvariantChecks.checkNotNull(target, "Undefined label: " + targetLabel);

        branchEntry.setBranchLabel((int) target.getAddress());
        delaySlot = engineContext.getDelaySlotSize();
      }
    }

    Logger.debug("Branch structure: %s", branchStructure);

    final Iterator<AbstractSequence> iterator = new Iterator<AbstractSequence>() {
      /** Iterator of branch structures. */
      private final Iterator<List<BranchEntry>> branchStructureIterator =
          new SingleValueIterator<>(branchStructure);

      /** Iterator of branch structures and execution trances. */
      private final BranchExecutionIterator branchStructureExecutionIterator =
          new BranchExecutionIterator(
              branchStructureIterator,
              maxBranchExecutions,
              maxExecutionTraces
          );

      @Override
      public void init() {
        branchStructureExecutionIterator.init();
      }

      @Override
      public boolean hasValue() {
        return branchStructureExecutionIterator.hasValue();
      }

      @Override
      public AbstractSequence value() {
        final List<BranchEntry> branchStructure = branchStructureExecutionIterator.value();
        Logger.debug("Branch structure: %s", branchStructure);

        final List<Integer> executionTrace = branchStructureExecutionIterator.trace();
        Logger.debug("Execution trace: %s", executionTrace);

        int index = 0;
        for (final AbstractCall abstractCall : sequence) {
          final Map<String, Object> attributes = abstractCall.getAttributes();
          if (attributes.containsKey(ATTR_EXECUTED)) {
            continue;
          }

          final BranchEntry branchEntry = branchStructure.get(index);
          setBranchEntry(abstractCall, branchEntry);

          index++;
        }

        return insertComments(
            insertExecutedCode(
                engineContext,
                executedCode,
                nontakenCode,
                branchStructure,
                executionTrace,
                insertControlCode(
                    engineContext,
                    new AbstractSequence(processedSequence)
                )
            )
        );
      }

      @Override
      public void next() {
        branchStructureExecutionIterator.next();
      }

      @Override
      public void stop() {
        branchStructureExecutionIterator.stop();
      }

      @Override
      public Iterator<AbstractSequence> clone() {
        throw new UnsupportedOperationException();
      }
    };

    return iterator;
  }

  @Override
  public void onStartProgram() {}

  @Override
  public void onEndProgram() {}

  private AbstractSequence insertControlCode(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    // Maps branch indices to control code (the map should be sorted).
    final SortedMap<Integer, List<AbstractCall>> steps = new TreeMap<>();
    // Contains positions of the delay slots.
    final Set<Integer> delaySlots = new HashSet<>();

    // Construct the control code to enforce the given execution trace.
    final List<AbstractCall> sequence = abstractSequence.getSequence();
    for (int i = 0; i < sequence.size(); i++) {
      final AbstractCall abstractCall = sequence.get(i);
      final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);

      if (branchEntry == null || !branchEntry.isIfThen()) {
        continue;
      }

      final Set<Integer> blockCoverage = branchEntry.getBlockCoverage();
      final Set<Integer> slotCoverage = branchEntry.getSlotCoverage();

      final String testDataStream = getTestDataStream(abstractCall);
      final List<AbstractCall> controlCode = makeStreamRead(engineContext, testDataStream);
      if (!controlCode.isEmpty()) {
        controlCode.get(0).getAttributes().put("dependsOn", abstractCall);
      }

      branchEntry.setControlCodeInBasicBlock(false);
      branchEntry.setControlCodeInDelaySlot(false);

      // Insert the control code into the basic block if it is possible.
      if (blockCoverage != null) {
        for (final int block : blockCoverage) {
          // Add the control code just after the basic block (the code should follow the label).
          final int codePosition = block + 1;

          List<AbstractCall> step = steps.get(codePosition);
          if (step == null) {
            steps.put(codePosition, step = new ArrayList<AbstractCall>());
          }

          Logger.debug("Control code of length %d for instruction %d put to block %d",
              controlCode.size(), i, block);

          step.addAll(controlCode);
        }

        // Block coverage is allowed to be empty (in this case, no additional code is required).
        // Do nothing.

        branchEntry.setControlCodeInBasicBlock(true);
      }

      // Insert the control code into the delay slot if it is possible.
      if (USE_DELAY_SLOTS && !branchEntry.isControlCodeInBasicBlock() && slotCoverage != null) {
        if (controlCode.size() <= engineContext.getDelaySlotSize()) {
          // Delay slot follows the branch.
          final int slotPosition = i + 1;

          List<AbstractCall> step = steps.get(slotPosition);
          if (step == null) {
            steps.put(slotPosition, step = new ArrayList<AbstractCall>());
          }

          delaySlots.add(slotPosition);

          step.addAll(controlCode);
          branchEntry.setControlCodeInDelaySlot(true);
        }
      }

      if (!branchEntry.isControlCodeInBasicBlock() && !branchEntry.isControlCodeInDelaySlot()) {
        Logger.debug("Cannot construct the control code %d: blockCoverage=%s, slotCoverage=%s",
            i, blockCoverage, slotCoverage);
        return null;
      }
    }

    for (final Map.Entry<Integer, List<AbstractCall>> entry : steps.entrySet()) {
      final int position = entry.getKey();
      final List<AbstractCall> controlCode = entry.getValue();

      abstractSequence.addPrologue(position, controlCode);
      if (delaySlots.contains(position)) {
        // Remove the old delay slot.
        for (int i = 0; i < controlCode.size(); i++) {
          abstractSequence.getPrologues().put(position + 1, null);
        }
      }
    }

    return abstractSequence;
  }

  private AbstractSequence insertExecutedCode(
      final EngineContext engineContext,
      final List<AbstractCall> executedCode,
      final List<AbstractCall> nontakenCode,
      final List<BranchEntry> branchStructure,
      final List<Integer> executionTrace,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(executedCode);
    InvariantChecks.checkNotNull(nontakenCode);
    InvariantChecks.checkNotNull(executionTrace);
    InvariantChecks.checkNotNull(abstractSequence);

    // Executed blocks with no repetitions.
    final List<Integer> executedBlocks = new ArrayList<>();
    for (final int i : executionTrace) {
      final BranchEntry branchEntry = branchStructure.get(i);
      if (branchEntry.isBasicBlock() && !executedBlocks.contains(i)) {
        executedBlocks.add(i);
      }
    }

    // Non-taken blocks with no repetitions.
    final List<Integer> nontakenBlocks = new ArrayList<>();
    for (final int i : executionTrace) {
      final BranchEntry branchEntry = branchStructure.get(i);
      if (branchEntry.isBranch()) {
        for (int j = i + 1; j < branchStructure.size(); j++) {
          final BranchEntry nextBranchEntry = branchStructure.get(j);

          if (nextBranchEntry.isDelaySlot()) {
            continue;
          }
          if (nextBranchEntry.isBasicBlock() && !nontakenBlocks.contains(j)) {
            nontakenBlocks.add(j);
          }
          break;
        }
      }
    }
    nontakenBlocks.removeAll(executedBlocks);

    // Consequent blocks should be considered as a single one.
    final List<Integer> redundantBlocks = new ArrayList<>();
    for (int i = 0; i < executedBlocks.size() - 1; i++) {
      final int thisBlock = executedBlocks.get(i);
      final int nextBlock = executedBlocks.get(i + 1);

      if (nextBlock == thisBlock + 1) {
        redundantBlocks.add(nextBlock);
      }
    }
    executedBlocks.removeAll(redundantBlocks);

    Logger.debug("Executed code: %s, executed blocks: %s", executedCode, executedBlocks);
    insertCodeIntoBlocks("Executed code", executedCode, executedBlocks, abstractSequence);

    Logger.debug("Nontaken code: %s, nontaken blocks: %s", nontakenCode, nontakenBlocks);
    insertCodeIntoBlocks("Non-taken code", nontakenCode, nontakenBlocks, abstractSequence);

    return abstractSequence;
  }

  private static void insertCodeIntoBlocks(
      final String comment,
      final List<AbstractCall> code,
      final List<Integer> blocks,
      final AbstractSequence abstractSequence) {
    if (blocks.isEmpty()) {
      return;
    }

    int codeIndex = 0;
    int codeLength = code.size();
    int blockNumber = blocks.size();

    for (int i = 0; i < blocks.size(); i++) {
      final int block = blocks.get(i);
      final int length = codeLength / blockNumber;
      final List<AbstractCall> codePart = code.subList(codeIndex, codeIndex + length);

      abstractSequence.addPrologue(block,
          AbstractCall.newComment(String.format("Begin: %s (%d)", comment, i)));
      abstractSequence.addPrologue(block, codePart);
      abstractSequence.addPrologue(block,
          AbstractCall.newComment(String.format("End: %s (%d)", comment, i)));

      codeIndex += length;
      codeLength -= length;
      blockNumber--;
    }
  }

  private static AbstractSequence insertComments(final AbstractSequence abstractSequence) {
    if (abstractSequence == null) {
      return null;
    }

    final List<AbstractCall> sequence = abstractSequence.getSequence();
    for (int i = 0; i < sequence.size(); i++) {
      final AbstractCall abstractCall = sequence.get(i);
      final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);

      if (null != branchEntry && branchEntry.isIfThen()) {
        abstractSequence.addPrologue(i,
            AbstractCall.newComment(
                String.format("Execution trace: $s", branchEntry.getBranchTrace())));
      }
    }

    return abstractSequence;
  }
}
