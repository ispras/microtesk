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

import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.allocateModes;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.getSituationName;
import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.makeConcreteCall;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchEntry;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchExecution;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchStructure;
import ru.ispras.microtesk.test.sequence.engine.branch.BranchTrace;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.testbase.TestBaseRegistry;
import ru.ispras.testbase.generator.DataGenerator;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchAdapter implements Adapter<BranchSolution> {
  public static final String TEST_DATA_PREFIX = "branch_data_";
  public static final boolean USE_DELAY_SLOTS = true;

  @Override
  public Class<BranchSolution> getSolutionClass() {
    return BranchSolution.class;
  }

  @Override
  public TestSequence adapt(
      final EngineContext engineContext,
      final Sequence<Call> abstractSequence,
      final BranchSolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);
    InvariantChecks.checkNotNull(solution);

    final BranchStructure branchStructure = solution.getBranchStructure();
    InvariantChecks.checkTrue(abstractSequence.size() == branchStructure.size());

    // Allocate uninitialized addressing modes.
    allocateModes(abstractSequence);

    final TestSequence.Builder testSequenceBuilder = new TestSequence.Builder();

    // Maps branch indices to control code.
    final Map<Integer, Sequence<Call>> steps = new LinkedHashMap<>();
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

      final String testDataArray = String.format("%s%d", TEST_DATA_PREFIX, branchNumber);
      branchNumber++;

      final BranchTrace branchTrace = branchEntry.getBranchTrace();
      final Set<Integer> blockCoverage = branchEntry.getBlockCoverage();
      final Set<Integer> slotCoverage = branchEntry.getSlotCoverage();

      final Sequence<Call> controlCode = new Sequence<Call>(); // TODO: read data stream

      boolean isInserted = false;

      // Insert the control code into the basic block if it is possible.
      if (!isInserted && blockCoverage != null) {
        for (final int block : blockCoverage) {
          Sequence<Call> step = steps.get(block);
          if (step == null) {
            steps.put(block, step = new Sequence<Call>());
          }

          step.addAll(controlCode);
          isInserted = true;
        }
      }

      boolean isBasicBlock = isInserted;

      // Insert the control code into the delay slot if it is possible.
      if (USE_DELAY_SLOTS && !isInserted && slotCoverage != null) {
        if (controlCode.size() <= engineContext.getDelaySlotSize()) {
          final int slotPosition = i + 1;

          Sequence<Call> step = steps.get(slotPosition);
          if (step == null) {
            steps.put(slotPosition, step = new Sequence<Call>());
          }

          delaySlots.add(slotPosition);

          step.addAll(controlCode);
          isInserted = true;
        }
      }

      if (!isInserted) {
        // Cannot construct the control code.
        return null;
      }

      // Retrieve the test data generator.
      final DataGenerator testDataGenerator = getGenerator(engineContext, abstractCall);

      try {
        updatePrologue(
            engineContext,
            testSequenceBuilder,
            abstractCall,
            branchTrace,
            isBasicBlock,
            testDataGenerator,
            testDataArray);
      } catch (final ConfigurationException e) {
        // Cannot convert the abstract code into the concrete code.
        return null;
      }
    }

    // Insert the control code into the sequence.
    int correction = 0;

    final Sequence<Call> modifiedSequence = new Sequence<Call>();
    modifiedSequence.addAll(abstractSequence);

    for (final Map.Entry<Integer, Sequence<Call>> entry : steps.entrySet()) {
      final Integer position = entry.getKey();
      final Sequence<Call> controlCode = entry.getValue();

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
      return null;
    }

    return testSequenceBuilder.build();
  }

  private DataGenerator getGenerator(final EngineContext engineContext, final Call abstractCall) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractCall);

    final TestBase testBase = engineContext.getTestBase(); 
    final TestBaseRegistry testBaseRegistry = testBase.getRegistry();
    final String situationName = getSituationName(abstractCall);
    final Collection<DataGenerator> generators = testBaseRegistry.getNamedGenerators(situationName);

    return generators.toArray(new DataGenerator[]{})[0];
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
      final Sequence<Call> abstractSequence)
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
      final Call abstractBranchCall,
      final BranchTrace branchTrace,
      final boolean controlCodeInBasicBlock,
      final DataGenerator testDataGenerator,
      final String testDataStream)
        throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractBranchCall);
    InvariantChecks.checkNotNull(testDataGenerator);

    final Sequence<Call> initDataStream = new Sequence<Call>(); // TODO:
    updatePrologue(engineContext, testSequenceBuilder, initDataStream);

    for (int i = 0; i < branchTrace.size(); i++) {
      final BranchExecution execution = branchTrace.get(i);

      final boolean condition = execution.value();
      testDataGenerator.generate(null); // TODO:

      final int count = controlCodeInBasicBlock ?
          execution.getBlockCoverageCount() : execution.getSlotCoverageCount();

      for (int j = 0; j < count; j++) {
        final Sequence<Call> writeDataStream = new Sequence<Call>(); // TODO:
        updatePrologue(engineContext, testSequenceBuilder, writeDataStream);
      }
    }

    updatePrologue(engineContext, testSequenceBuilder, initDataStream);
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
      final Sequence<Call> abstractSequence)
          throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(testSequenceBuilder);
    InvariantChecks.checkNotNull(abstractSequence);

    for (final Call abstractCall : abstractSequence) {
      updateBody(engineContext, testSequenceBuilder, abstractCall);
    }
  }
}
