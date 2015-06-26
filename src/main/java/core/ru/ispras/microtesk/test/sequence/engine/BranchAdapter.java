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

package ru.ispras.microtesk.test.sequence.engine;

import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.getTestData;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeConcreteCall;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeInitializer;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeStreamInit;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeStreamRead;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeStreamWrite;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.setUnknownImmValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchEntry;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchExecution;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchStructure;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchTrace;
import ru.ispras.microtesk.test.sequence.engine.common.TestBaseQueryCreator;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.testbase.BranchDataGenerator;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestData;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchAdapter implements Adapter<BranchSolution> {
  public static final boolean USE_DELAY_SLOTS = true;

  @Override
  public Class<BranchSolution> getSolutionClass() {
    return BranchSolution.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public AdapterResult adapt(
      final EngineContext engineContext,
      final List<Call> abstractSequence,
      final BranchSolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);
    InvariantChecks.checkNotNull(solution);

    final BranchStructure branchStructure = solution.getBranchStructure();
    InvariantChecks.checkTrue(abstractSequence.size() == branchStructure.size());

    Logger.debug("Branch Structure: %s", branchStructure);

    final TestSequence.Builder testSequenceBuilder = new TestSequence.Builder();

    // Maps branch indices to control code.
    final Map<Integer, List<Call>> steps = new LinkedHashMap<>();
    // Contains positions of the delay slots.
    final Set<Integer> delaySlots = new HashSet<>();

    // Construct the control code to enforce the given execution trace.
    int branchNumber = 0;

    for (int i = 0; i < abstractSequence.size(); i++) {
      final Call abstractCall = abstractSequence.get(i);
      final BranchEntry branchEntry = branchStructure.get(i);

      if (!branchEntry.isIfThen()) {
        continue;
      }

      branchNumber++;

      final BranchTrace branchTrace = branchEntry.getBranchTrace();
      final Set<Integer> blockCoverage = branchEntry.getBlockCoverage();
      final Set<Integer> slotCoverage = branchEntry.getSlotCoverage();

      final String testDataStream = getTestDataStream(abstractCall);
      final List<Call> controlCode = makeStreamRead(engineContext, testDataStream);

      boolean isEnforced = false;

      // Insert the control code into the basic block if it is possible.
      if (!isEnforced && blockCoverage != null) {
        for (final int block : blockCoverage) {
          List<Call> step = steps.get(block);
          if (step == null) {
            // Add the control code just after the basic block (the code should follow the label).
            final int codePosition = block + 1;
            steps.put(codePosition, step = new ArrayList<Call>());
          }

          step.addAll(controlCode);
        }

        // Block coverage is allowed to be empty; this means that no additional code is required. 
        isEnforced = true;
      }

      boolean isBasicBlock = isEnforced;

      // Insert the control code into the delay slot if it is possible.
      if (USE_DELAY_SLOTS && !isEnforced && slotCoverage != null) {
        if (controlCode.size() <= engineContext.getDelaySlotSize()) {
          // Delay slot follows the branch.
          final int slotPosition = i + 1;

          List<Call> step = steps.get(slotPosition);
          if (step == null) {
            steps.put(slotPosition, step = new ArrayList<Call>());
          }

          delaySlots.add(slotPosition);

          step.addAll(controlCode);
          isEnforced = true;
        }
      }

      if (!isEnforced) {
        return new AdapterResult(
            String.format("Cannot construct the control code %d: blockCoverage=%s, slotCoverage=%s",
                branchNumber, blockCoverage, slotCoverage));
      }

      try {
        updatePrologue(
            engineContext,
            testSequenceBuilder,
            abstractCall,
            branchTrace,
            isBasicBlock);
      } catch (final ConfigurationException e) {
        return new AdapterResult("Cannot convert the abstract sequence into the concrete one");
      }
    }

    // Insert the control code into the sequence.
    int correction = 0;

    final List<Call> modifiedSequence = new ArrayList<Call>();
    modifiedSequence.addAll(abstractSequence);

    for (final Map.Entry<Integer, List<Call>> entry : steps.entrySet()) {
      final int position = entry.getKey();
      final List<Call> controlCode = entry.getValue();

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

    try {
      updateBody(engineContext, testSequenceBuilder, modifiedSequence);
    } catch (final ConfigurationException e) {
      // Cannot convert the abstract code into the concrete code.
      return new AdapterResult("Cannot convert the abstract sequence into the concrete one");
    }

    return new AdapterResult(testSequenceBuilder.build());
  }

  private void updatePrologue(
      final EngineContext engineContext,
      final TestSequence.Builder testSequenceBuilder,
      final Call abstractCall)
          throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractCall);

    final ConcreteCall concreteCall = makeConcreteCall(engineContext, abstractCall);
    testSequenceBuilder.addToPrologue(concreteCall);
  }

  private void updatePrologue(
      final EngineContext engineContext,
      final TestSequence.Builder testSequenceBuilder,
      final List<Call> abstractSequence)
          throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractSequence);

    for (final Call abstractCall : abstractSequence) {
      updatePrologue(engineContext, testSequenceBuilder, abstractCall);
    }
  }

  private void updatePrologue(
      final EngineContext engineContext,
      final TestSequence.Builder testSequenceBuilder,
      final Call abstractCall,
      final boolean branchCondition,
      final boolean writeIntoStream)
        throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractCall);

    final Primitive primitive = abstractCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive);

    final Situation situation = primitive.getSituation();
    InvariantChecks.checkNotNull(situation);

    final Map<String, Object> attributes = situation.getAttributes();
    InvariantChecks.checkNotNull(attributes);

    // Specify the situation's parameter (branch condition).
    final Map<String, Object> newAttributes = new HashMap<>(attributes);

    newAttributes.put(BranchDataGenerator.PARAM_CONDITION,
        branchCondition ?
            BranchDataGenerator.PARAM_CONDITION_THEN :
            BranchDataGenerator.PARAM_CONDITION_ELSE);

    final Situation newSituation = new Situation(situation.getName(), newAttributes);

    final TestBaseQueryCreator queryCreator =
        new TestBaseQueryCreator(engineContext, newSituation, primitive);

    final TestData testData = getTestData(engineContext, primitive, queryCreator);
    Logger.debug(testData.toString());

    // Set unknown immediate values (if there are any).
    setUnknownImmValues(queryCreator.getUnknownImmValues(), testData);

    // Initialize test data to ensure branch execution.
    boolean isInitialized = false;

    for (final Map.Entry<String, Node> testDatum : testData.getBindings().entrySet()) {
      final String name = testDatum.getKey();
      final Argument arg = queryCreator.getModes().get(name);

      if (arg == null || arg.getKind() != Argument.Kind.MODE || arg.getMode() == ArgumentMode.OUT) {
        continue;
      }

      final Primitive mode = (Primitive) arg.getValue();
      final BitVector value = FortressUtils.extractBitVector(testDatum.getValue());

      final List<Call> initializingCalls = new ArrayList<Call>();

      initializingCalls.addAll(makeInitializer(engineContext, mode, value));
      if (writeIntoStream) {
        final String testDataStream = getTestDataStream(abstractCall);
        initializingCalls.addAll(makeStreamWrite(engineContext, testDataStream));
      }

      updatePrologue(engineContext, testSequenceBuilder, initializingCalls);
      isInitialized = true;
    }

    InvariantChecks.checkTrue(isInitialized);
  }

  private void updatePrologue(
      final EngineContext engineContext,
      final TestSequence.Builder testSequenceBuilder,
      final Call abstractBranchCall,
      final BranchTrace branchTrace,
      final boolean controlCodeInBasicBlock)
        throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractBranchCall);

    // If the control code is not executed before the first branch execution,
    // the registers of the branch instruction should be initialized.
    boolean initNeeded = !branchTrace.isEmpty();
    // Data stream is not used if the trace is empty or consists of one execution.
    boolean streamUsed = false;

    for (int i = 0; i < branchTrace.size(); i++) {
      final BranchExecution execution = branchTrace.get(i);
      final boolean branchCondition = execution.value();

      // Count defines how many times the control code is executed before calling the branch.
      final int count = controlCodeInBasicBlock ?
          execution.getBlockCoverageCount() : execution.getSlotCoverageCount();

      if(i == 0 && count > 0) {
        initNeeded = false;
      }

      for (int j = 0; j < count; j++) {
        // Data stream should be initialized before the first write. 
        if (!streamUsed) {
          final String testDataStream = getTestDataStream(abstractBranchCall);
          final List<Call> initDataStream = makeStreamInit(engineContext, testDataStream);

          updatePrologue(engineContext, testSequenceBuilder, initDataStream);
          streamUsed = true;
        }

        updatePrologue(
            engineContext,
            testSequenceBuilder,
            abstractBranchCall,
            branchCondition,
            true /* Write into the stream */);
      }
    }

    // Initialize the data stream if it was used. 
    if (streamUsed) {
      final String testDataStream = getTestDataStream(abstractBranchCall);
      final List<Call> initDataStream = makeStreamInit(engineContext, testDataStream);

      updatePrologue(engineContext, testSequenceBuilder, initDataStream);
    }

    // Initialize the registers if it is needed. 
    if (initNeeded) {
      final BranchExecution execution = branchTrace.get(0);
      final boolean branchCondition = execution.value();

      updatePrologue(
          engineContext,
          testSequenceBuilder,
          abstractBranchCall,
          branchCondition,
          false /* Write into the registers */);
    }
  }

  private void updateBody(
      final EngineContext engineContext,
      final TestSequence.Builder testSequenceBuilder,
      final Call abstractCall)
          throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractCall);

    final ConcreteCall concreteCall = makeConcreteCall(engineContext, abstractCall);
    testSequenceBuilder.add(concreteCall);
  }

  private void updateBody(
      final EngineContext engineContext,
      final TestSequence.Builder testSequenceBuilder,
      final List<Call> abstractSequence)
          throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractSequence);

    for (final Call abstractCall : abstractSequence) {
      updateBody(engineContext, testSequenceBuilder, abstractCall);
    }
  }

  private String getTestDataStream(final Call abstractBranchCall) {
    InvariantChecks.checkNotNull(abstractBranchCall);

    final Primitive primitive = abstractBranchCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive);

    final Situation situation = primitive.getSituation();
    InvariantChecks.checkNotNull(situation);

    final Object testDataStream = situation.getAttribute(BranchDataGenerator.PARAM_STREAM);
    InvariantChecks.checkNotNull(testDataStream);

    return testDataStream.toString();
  }
}
