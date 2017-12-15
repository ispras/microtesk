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

import java.math.BigInteger;
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
import ru.ispras.microtesk.test.engine.EngineParameterInteger;
import ru.ispras.microtesk.test.engine.SequenceSelector;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.BlockId;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.Value;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

/**
 * {@link BranchEngine} implements a test engine that constructs test cases by enumerating
 * feasible execution traces of the bounded length.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchEngine implements Engine {
  public static final String ID = "branch";
  public static final boolean USE_DELAY_SLOTS = true;

  /** Percentage of branch instructions in the stream. */
  static final EngineParameterInteger PARAM_BRANCH_PERCENTAGE =
      new EngineParameterInteger("branch_percentage", 10);

  /** Maximum number of executions of a single branch instruction. */
  static final EngineParameterInteger PARAM_BRANCH_LIMIT =
      new EngineParameterInteger("branch_exec_limit", 1);

  /** Maximum number of executions of a single basic block. */
  static final EngineParameterInteger PARAM_BLOCK_LIMIT =
      new EngineParameterInteger("block_exec_limit", 1);

  /** Maximum number of execution traces to be enumerated. */
  static final EngineParameterInteger PARAM_TRACE_LIMIT =
      new EngineParameterInteger("trace_count_limit", -1);

  public static final String IF_THEN_SITUATION_SUFFIX = "if-then";
  public static final String GOTO_SITUATION_SUFFIX = "goto";
  public static final String AUTO_LABEL_PREFIX = "auto_label";

  /** Attribute {@code executed} is used to mark executed and non-taken code. */
  public static final String ATTR_EXECUTED = "executed";
  /** Attribute {@code branches} is used to mark branch instructions to be used. */
  public static final String ATTR_BRANCHES = "branches";

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

  static boolean isBranch(final AbstractCall abstractCall) {
    return isIfThen(abstractCall) || isGoto(abstractCall);
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

  static int getRegisterId(final AbstractCall abstractBranchCall) {
    InvariantChecks.checkNotNull(abstractBranchCall);

    final Primitive primitive = abstractBranchCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive);

    for (final Argument argument : primitive.getArguments().values()) {
      if (argument.getKind() != Argument.Kind.MODE) {
        continue;
      }

      InvariantChecks.checkTrue(argument.getValue() instanceof Primitive);
      final Primitive mode = (Primitive) argument.getValue();

      InvariantChecks.checkTrue(mode.getArguments().size() == 1);
      for (final Argument index : mode.getArguments().values()) {
        final Object value = index.getValue();

        if (value instanceof BigInteger) {
          final BigInteger integerValue = (BigInteger) value;
          return integerValue.intValue();
        }

        if (value instanceof Value) {
          final Value lazyValue = (Value) value;
          final BigInteger integerValue = lazyValue.getValue();
          return integerValue.intValue();
        }

        InvariantChecks.checkTrue(false, "Unknown argument type: " + index);
      }
    }

    return -1;
  }

  /** Branch percentage: default value is 10%. */
  private int branchPercentage = PARAM_BRANCH_PERCENTAGE.getDefaultValue();

  /** Branch execution limit: default value is 1. */
  private int branchExecutionLimit = PARAM_BRANCH_LIMIT.getDefaultValue();

  /** Block execution limit: default value is 1. */
  private int blockExecutionLimit = PARAM_BLOCK_LIMIT.getDefaultValue();

  /** Trace count limit: default value is -1 (no limitations). */
  private int traceCountLimit = PARAM_TRACE_LIMIT.getDefaultValue();

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

    branchPercentage = PARAM_BRANCH_PERCENTAGE.parse(
        attributes.get(PARAM_BRANCH_PERCENTAGE.getName()));
    InvariantChecks.checkTrue(0 <= branchPercentage && branchPercentage < 100);

    branchExecutionLimit = PARAM_BRANCH_LIMIT.parse(
        attributes.get(PARAM_BRANCH_LIMIT.getName()));
    InvariantChecks.checkTrue(branchExecutionLimit >= 0 || branchExecutionLimit == -1);

    blockExecutionLimit = PARAM_BLOCK_LIMIT.parse(
        attributes.get(PARAM_BLOCK_LIMIT.getName()));
    InvariantChecks.checkTrue(blockExecutionLimit >= 0   || blockExecutionLimit == -1);
    InvariantChecks.checkTrue(branchExecutionLimit != -1 || blockExecutionLimit != -1);

    traceCountLimit = PARAM_TRACE_LIMIT.parse(
        attributes.get(PARAM_TRACE_LIMIT.getName()));
    InvariantChecks.checkTrue(traceCountLimit >= 0 || traceCountLimit == -1);
  }

  @Override
  public Iterator<AbstractSequence> solve(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    // The branch engine may modify the original sequence.
    final List<AbstractCall> oldSequence = abstractSequence.getSequence();
    final List<AbstractCall> newSequence = new ArrayList<>();

    // Branches to be injected into test cases.
    final List<AbstractCall> branchesBlock = new ArrayList<>();
    // Code to be put along an execution trace.
    final List<AbstractCall> executedBlock = new ArrayList<>();
    // Code to be put into non-executed basic blocks.
    final List<AbstractCall> nontakenBlock = new ArrayList<>();

    // Take the above-mentioned blocks from the sequence.
    for (int i = 0; i < oldSequence.size(); i++) {
      final AbstractCall abstractCall = oldSequence.get(i);
      final Map<String, Object> attributes = abstractCall.getAttributes();

      final Boolean isExecuted = (Boolean) attributes.get(ATTR_EXECUTED);
      final Boolean isBranches = (Boolean) attributes.get(ATTR_BRANCHES);

      if (isExecuted != null) {
        if (isExecuted) {
          executedBlock.add(abstractCall);
        } else {
          nontakenBlock.add(abstractCall);
        }
        continue;
      }

      if (isBranches != null) {
        // The attribute value is not of importance.
        branchesBlock.add(abstractCall);
        continue;
      }

      // The rest code is added to the sequence without changes.
      newSequence.add(abstractCall);
    }

    // Add the required number of branches to the sequence.
    if (!branchesBlock.isEmpty()) {
      final int sequenceSize = newSequence.size() + executedBlock.size() + nontakenBlock.size();
      final int branchNumber = (branchPercentage * sequenceSize) / (100 - branchPercentage);

      for (int i = 0; i < branchNumber; i++) {
        // Choose a random branch from the branches block.
        final AbstractCall branchCall = Randomizer.get().choose(branchesBlock);

        // Insert the chosen branch into a random position.
        final int randomIndex = Randomizer.get().nextIntRange(0, newSequence.size());
        final AbstractCall randomCall = randomIndex < newSequence.size()
            ? newSequence.get(randomIndex) : null;

        // Do not break the block-branch pairs.
        final int branchIndex = randomCall != null && isBranch(randomCall)
            ? randomIndex + 1 : randomIndex;

        // Add the chosen branch.
        newSequence.add(branchIndex, new AbstractCall(branchCall));
        // Insert an empty basic block before the branch (place for control code).
        newSequence.add(branchIndex, AbstractCall.newEmpty());
      }
    }

    // Collect information about labels.
    final LabelManager labelManager = new LabelManager();

    for (int i = 0; i < newSequence.size(); i++) {
      final AbstractCall abstractCall = newSequence.get(i);

      for (final Label label : abstractCall.getLabels()) {
        labelManager.addLabel(label, i);
      }
    }

    // Transform the sequence into the branch structure.
    final List<BranchEntry> branchStructure = new ArrayList<>();

    int autoLabel = 0;
    int delaySlot = 0;

    for (final AbstractCall abstractCall : newSequence) {
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

      // To create initialization code, the engine needs to know branch registers.
      if (branchEntry.isIfThen()) {
        final int registerId = getRegisterId(abstractCall);

        Logger.debug("Register ID: %d", registerId);
        branchEntry.setRegisterId(registerId);
      }

      branchStructure.add(branchEntry);

      // Set the target label and start the delay slot.
      if (isIfThen || isGoto) {
        final LabelReference targetReference = abstractCall.getTargetReference();
        InvariantChecks.checkNotNull(targetReference);

        // Branching to any place in the sequence (_label).
        if (targetReference.getReference() == null) {
          // Generate the label name.
          final String name = String.format("%s_%d", AUTO_LABEL_PREFIX, autoLabel++);
          final BlockId blockId = new BlockId();
          final Label targetLabel = new Label(name, blockId);

          // Put the label in a random position.
          final int randomIndex = Randomizer.get().nextIntRange(0, newSequence.size() - 1);
          final AbstractCall randomCall = newSequence.get(randomIndex);

          // Jump to a basic block to be able to update the next branch register.
          final int targetIndex = randomIndex > 0 && isBranch(randomCall)
              ? randomIndex - 1 : randomIndex;

          final AbstractCall targetCall = newSequence.get(targetIndex);
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
              branchExecutionLimit,
              blockExecutionLimit,
              traceCountLimit
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

        for (int i = 0; i < newSequence.size(); i++) {
          final AbstractCall abstractCall = newSequence.get(i);
          final BranchEntry branchEntry = branchStructure.get(i);

          setBranchEntry(abstractCall, branchEntry);
        }

        return insertComments(
            insertExecutedCode(
                engineContext,
                executedBlock,
                nontakenBlock,
                branchStructure,
                executionTrace,
                insertControlCode(
                    engineContext,
                    new AbstractSequence(
                        abstractSequence.getSection(),
                        AbstractCall.copyAll(newSequence),
                        abstractSequence.getFlags(),
                        abstractSequence.getIndexes(),
                        abstractSequence.getPositions())
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
      // Get the branch entry stored in the call attribute.
      final AbstractCall abstractCall = sequence.get(i);
      final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);

      // Only conditional branches are taken into account.
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
          final int codePosition = block;

          List<AbstractCall> step = steps.get(codePosition);
          if (step == null) {
            steps.put(codePosition, step = new ArrayList<AbstractCall>());
          }

          Logger.debug("Control code of length %d for instruction %d put to block %d",
              controlCode.size(), i, block);

          step.addAll(controlCode);
        }

        // Do nothing: block coverage is allowed to be empty (no additional code is required).

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
        InvariantChecks.checkTrue(false);
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
          abstractSequence.getSequence().set(position + i, AbstractCall.newEmpty());
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
                String.format("Execution trace: %s", branchEntry.getBranchTrace())));
      }
    }

    return abstractSequence;
  }
}
