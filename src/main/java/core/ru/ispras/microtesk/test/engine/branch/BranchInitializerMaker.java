/*
 * Copyright 2009-2017 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.test.engine.AddressingModeWrapper;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineUtils;
import ru.ispras.microtesk.test.engine.InitializerMaker;
import ru.ispras.microtesk.test.engine.TestBaseQueryCreator;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestData;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchInitializerMaker implements InitializerMaker {
  public static final boolean USE_DELAY_SLOTS = BranchEngine.USE_DELAY_SLOTS;

  @Override
  public void configure(final Map<String, Object> attributes) {}

  @Override
  public void onStartProgram() {}

  @Override
  public void onEndProgram() {}

  @Override
  public List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final int processingCount,
      final Stage stage,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
      final TestData testData, /* Unused */
      final Map<String, Argument> modes,
      final Set<AddressingModeWrapper> initializedModes) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkNotNull(situation);

    Logger.setDebug(true);
    Logger.debug("Make initializer for call: %s", abstractCall);

    final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);
    InvariantChecks.checkNotNull(branchEntry);

    Logger.debug("Make initializer for entry: %s", branchEntry);

    final BranchTrace branchTrace = branchEntry.getBranchTrace();
    InvariantChecks.checkNotNull(branchTrace);

    Logger.debug("Processing count: %d, branch trace: %d", processingCount, branchTrace.size());

    InvariantChecks.checkTrue(
        stage == Stage.POST /* The final pass */ ||
        (0 <= processingCount && processingCount < branchTrace.size()));

    // It cannot be guaranteed that two branches do not share a register.
    final boolean sharedRegisters = true;

    // There is no need to construct the control code if the branch condition does not change.
    // However, if multiple branches shares the same register, it makes sense.
    @SuppressWarnings("unused")
    final boolean streamBasedInitialization = sharedRegisters || branchTrace.getChangeNumber() > 0;

    // The final pass.
    if (stage == Stage.POST) {
      Logger.debug("Terminate");

      if (branchTrace.isEmpty() || !branchEntry.isRegisterFirstUse()) {
        return Collections.<AbstractCall>emptyList();
      }

      final List<AbstractCall> initializer = new ArrayList<>();

      // Reset the stream if is used.
      if (streamBasedInitialization) {
        final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
        initializer.addAll(EngineUtils.makeStreamInit(engineContext, testDataStream));
      }

      final BranchExecution execution = branchTrace.get(0);
      final int executionCount = getControlCodeExecutionCount(branchEntry, execution);

      // If the control code is not invoked before the first branch execution and
      // this is the first use of the branch register, the register should be initialized.
      if (executionCount == 0) {
        final List<AbstractCall> initRegisters =
            makeInitializer(
                engineContext,
                processingCount,
                abstractCall,
                primitive,
                situation,
                true /* Branch is taken */,
                execution.value(),
                false /* Write into the registers */);

        initializer.addAll(initRegisters);
      }

      return initializer;
    }

    if (streamBasedInitialization) {
      final List<AbstractCall> initializer = new ArrayList<>();

      final BranchExecution execution = branchTrace.get(processingCount);
      final boolean branchCondition = execution.value();

      // Calculate how many times the control code is executed before calling the branch.
      final int executionCount = getControlCodeExecutionCount(branchEntry, execution);

      Logger.debug("Branch execution: processingCount=%d, condition=%b, executionCount=%d",
          processingCount, branchCondition, executionCount);

      // The data stream should be initialized before writing into it. 
      if (processingCount == 0 && branchEntry.isRegisterFirstUse()) {
        final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
        initializer.addAll(EngineUtils.makeStreamInit(engineContext, testDataStream));
      }

      for (int i = 0; i < executionCount; i++) {
        initializer.addAll(
            makeInitializer(
                engineContext,
                processingCount,
                abstractCall,
                primitive,
                situation,
                true /* Branch is taken */,
                branchCondition,
                true /* Write into the stream */)
            );
      }

      return initializer;
    } // If stream-based initialization is used.

    return Collections.<AbstractCall>emptyList();
  }

  private List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final int processingCount,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
      final boolean branchTaken,
      final boolean branchCondition,
      final boolean writeIntoStream) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkNotNull(situation);

    final List<AbstractCall> initializer = new ArrayList<>();

    final Map<String, Object> attributes = situation.getAttributes();
    InvariantChecks.checkNotNull(attributes);

    // Specify the situation's parameter (branch condition).
    final Map<String, Object> newAttributes = new HashMap<>(attributes);

    newAttributes.put(BranchDataGenerator.PARAM_CONDITION,
        branchCondition
          ? BranchDataGenerator.PARAM_CONDITION_THEN
          : BranchDataGenerator.PARAM_CONDITION_ELSE);

    final Situation newSituation = new Situation(situation.getName(), newAttributes);

    final TestBaseQueryCreator queryCreator = new TestBaseQueryCreator(
        engineContext,
        processingCount,
        null /* Abstract sequence */,
        newSituation,
        primitive
        );

    final TestData testData =
        EngineUtils.getTestData(engineContext, primitive, newSituation, queryCreator);
    Logger.debug("Test data: %s", testData);

    // Set unknown immediate values (if there are any).
    EngineUtils.setUnknownImmValues(queryCreator.getUnknownImmValues(), testData);

    if (branchTaken) {
      // Initialize test data to ensure branch execution.
      for (final Map.Entry<String, Object> testDatum : testData.getBindings().entrySet()) {
        final String name = testDatum.getKey();
        final Argument arg = queryCreator.getModes().get(name);

        if (arg == null
            || arg.getKind() != Argument.Kind.MODE
            || arg.getMode() == ArgumentMode.OUT) {
          continue;
        }

        final Primitive mode = (Primitive) arg.getValue();
        final BitVector value = FortressUtils.extractBitVector((Node) testDatum.getValue());

        initializer.addAll(EngineUtils.makeInitializer(engineContext, mode, value));
        if (writeIntoStream) {
          final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
          initializer.addAll(EngineUtils.makeStreamWrite(engineContext, testDataStream));
        }
      }
    }

    return initializer;
  }

  private int getControlCodeExecutionCount(
      final BranchEntry entry,
      final BranchExecution execution) {
    InvariantChecks.checkNotNull(entry);
    InvariantChecks.checkNotNull(execution);

    int result = 0;

    final Set<Integer> coverage = entry.isControlCodeInBasicBlock()
        ? entry.getBlockCoverage()
        : entry.getSlotCoverage();

    final Map<Integer, Integer> segment = entry.isControlCodeInBasicBlock()
        ? execution.getPreBlocks()
        : execution.getPreSlots();

    for (final int item : coverage) {
      final Integer count = segment.get(item);
      result += (count != null ? count : 0);
    }

    Logger.debug(String.format(
        "Control code count: count=%d, coverage=%s, segment=%s", result, coverage, segment));

    return result;
  }
}
