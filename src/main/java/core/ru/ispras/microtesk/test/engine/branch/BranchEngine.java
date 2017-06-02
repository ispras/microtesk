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

import static ru.ispras.microtesk.test.engine.utils.EngineUtils.getSituationName;
import static ru.ispras.microtesk.test.engine.utils.EngineUtils.makeStreamRead;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineResult;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.AbstractSequence;
import ru.ispras.microtesk.test.template.Label;
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

  static boolean isIfThen(final AbstractCall abstractCall) {
    // Self check.
    final String situationName = getSituationName(abstractCall);
    final boolean flag = situationName != null && situationName.endsWith(IF_THEN_SITUATION_SUFFIX);

    /*
    // FIXME: The check is temporary disabled
    final boolean result = abstractCall.isBranch() && abstractCall.isConditionalBranch();
    InvariantChecks.checkTrue(result == flag);

    return result;*/

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
  public EngineResult solve(
      final EngineContext engineContext, final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    // Collect information about labels.
    final LabelManager labels = new LabelManager();
    for (int i = 0; i < abstractSequence.size(); i++) {
      final AbstractCall call = abstractSequence.getSequence().get(i);
      for (final Label label : call.getLabels()) {
        labels.addLabel(label, i);
      }
    }

    // Transform the abstract sequence into the branch structure.
    final List<BranchEntry> branchStructure = new ArrayList<>(abstractSequence.size());
    for (int i = 0; i < abstractSequence.size(); i++) {
      branchStructure.add(new BranchEntry(BranchEntry.Type.BASIC_BLOCK, -1, -1));
    }

    int delaySlot = 0;
    for (int i = 0; i < abstractSequence.size(); i++) {
      final AbstractCall abstractCall = abstractSequence.getSequence().get(i);
      final BranchEntry branchEntry = branchStructure.get(i);

      final boolean isIfThen = isIfThen(abstractCall);
      final boolean isGoto = isGoto(abstractCall);

      // Set the branch entry type.
      if (isIfThen) {
        // Using branches in delay slots is prohibited.
        InvariantChecks.checkTrue(delaySlot == 0);
        branchEntry.setType(BranchEntry.Type.IF_THEN);
      } else if (isGoto) {
        // Using branches in delay slots is prohibited.
        InvariantChecks.checkTrue(delaySlot == 0);
        branchEntry.setType(BranchEntry.Type.GOTO);
      } else if (delaySlot > 0) {
        branchEntry.setType(BranchEntry.Type.DELAY_SLOT);
        delaySlot--;
      } else {
        branchEntry.setType(BranchEntry.Type.BASIC_BLOCK);
      }

      // Set the target label and start the delay slot.
      if (isIfThen || isGoto) {
        final Label label = abstractCall.getTargetLabel();

        final LabelManager.Target target = labels.resolve(label);
        InvariantChecks.checkNotNull(target, "Undefined label: " + label);

        branchEntry.setBranchLabel((int) target.getAddress());
        delaySlot = engineContext.getDelaySlotSize();
      }
    }

    Logger.debug("Branch Structure: %s", branchStructure);

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

        for (int i = 0; i < abstractSequence.size(); i++) {
          final AbstractCall abstractCall = abstractSequence.getSequence().get(i);
          final BranchEntry branchEntry = branchStructure.get(i);

          setBranchEntry(abstractCall, branchEntry);
        }

        return insertControlCode(engineContext, abstractSequence);
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

    return new EngineResult(EngineResult.Status.OK, iterator, Collections.<String>emptyList());
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
    for (int i = 0; i < abstractSequence.size(); i++) {
      final AbstractCall abstractCall = abstractSequence.getSequence().get(i);
      final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);

      if (!branchEntry.isIfThen()) {
        continue;
      }

      final Set<Integer> blockCoverage = branchEntry.getBlockCoverage();
      final Set<Integer> slotCoverage = branchEntry.getSlotCoverage();

      final String testDataStream = getTestDataStream(abstractCall);
      final List<AbstractCall> controlCode = makeStreamRead(engineContext, testDataStream);

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

        // Block coverage is allowed to be empty; this means that no additional code is required. 
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

    // Insert the control code into the sequence.
    int correction = 0;

    final List<AbstractCall> modifiedSequence =
        new ArrayList<AbstractCall>(abstractSequence.getSequence());

    for (final Map.Entry<Integer, List<AbstractCall>> entry : steps.entrySet()) {
      final int position = entry.getKey();
      final List<AbstractCall> controlCode = entry.getValue();

      modifiedSequence.addAll(position + correction, controlCode);

      if (delaySlots.contains(position)) {
        // Remove the old delay slot.
        for (int i = 0; i < controlCode.size(); i++) {
          modifiedSequence.remove(position + correction + controlCode.size());
        }
      } else {
        // Update the correction offset.
        correction += controlCode.size();
      }
    }

    return new AbstractSequence(modifiedSequence);
  }
}
