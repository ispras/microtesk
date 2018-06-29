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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineUtils;
import ru.ispras.microtesk.test.engine.InitializerMaker;
import ru.ispras.microtesk.test.engine.TestBaseQueryCreator;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private List<AbstractCall> makePreInitializer(
      final EngineContext engineContext,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
      final boolean isStreamBased) throws ConfigurationException {
    final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);
    InvariantChecks.checkNotNull(branchEntry);

    final BranchTrace branchTrace = branchEntry.getBranchTrace();
    InvariantChecks.checkNotNull(branchTrace);

    if (branchTrace.isEmpty() || !branchEntry.isRegisterFirstUse()) {
      return Collections.<AbstractCall>emptyList();
    }

    final List<AbstractCall> initializer = new ArrayList<>();

    // Reset the stream if it is used.
    if (isStreamBased) {
      final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
      initializer.addAll(EngineUtils.makeStreamInit(engineContext, testDataStream));
    }

    return initializer;
  }

  private List<AbstractCall> makePostInitializer(
      final EngineContext engineContext,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
      final boolean isStreamBased) throws ConfigurationException {
    final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);
    InvariantChecks.checkNotNull(branchEntry);

    final BranchTrace branchTrace = branchEntry.getBranchTrace();
    InvariantChecks.checkNotNull(branchTrace);

    if (branchTrace.isEmpty() || !branchEntry.isRegisterFirstUse()) {
      return Collections.<AbstractCall>emptyList();
    }

    final List<AbstractCall> initializer = new ArrayList<>();

    // Reset the stream if is used.
    if (isStreamBased) {
      final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
      initializer.addAll(EngineUtils.makeStreamInit(engineContext, testDataStream));
    }

    final int processingCount = 0;
    final BranchExecution execution = branchTrace.get(processingCount);
    final int executionCount = getControlCodeExecutionCount(branchEntry, processingCount);

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
              execution.value(),
              false /* Write into the registers */);

      initializer.addAll(initRegisters);
    }

    return initializer;
  }

  @Override
  public List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final int processingCount,
      final Stage stage,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
      final TestData testData, /* Unused */
      final Map<String, Primitive> targetModes) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkNotNull(situation);

    final boolean isDebug = Logger.isDebug();
    Logger.setDebug(engineContext.getOptions().getValueAsBoolean(Option.DEBUG_PRINT) &&
                    engineContext.getOptions().getValueAsBoolean(Option.VERBOSE));
    try {

    final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);
    InvariantChecks.checkNotNull(branchEntry);

    Logger.debug("Make initializer (stage=%s, count=%d): %s, %s",
        stage, processingCount, abstractCall, branchEntry);

    InvariantChecks.checkTrue(stage == Stage.PRE || stage == Stage.POST || 0 <= processingCount);

    // There is no need to construct the control code if the branch condition does not change.
    // However, if multiple branches shares the same register, it makes sense.
    final boolean isStreamBased = true;

    if (stage == Stage.PRE) {
      return makePreInitializer(
          engineContext, abstractCall, primitive, situation, isStreamBased);
    }

    if (stage == Stage.POST) {
      return makePostInitializer(
          engineContext, abstractCall, primitive, situation, isStreamBased);
    }

    final BranchTrace branchTrace = branchEntry.getBranchTrace();
    InvariantChecks.checkTrue(branchTrace != null && !branchTrace.isEmpty());

    if (isStreamBased) {
      final List<AbstractCall> initializer = new ArrayList<>();

      // The initializer maker is called every time the control code is simulated.
      // The control code can be executed more times than the corresponding branch, e.g.
      //
      //   START: control code
      //          if (something) goto STOP
      //          target branch
      //          goto START
      //   STOP:
      //
      // If processingCount > branchTrace.size(), we can write everything into the stream.

      final BranchExecution execution =
          processingCount < branchTrace.size() ? branchTrace.get(processingCount) : null;

      // Calculate how many times the control code is executed before calling the branch.
      final int executionCount = getControlCodeExecutionCount(branchEntry, processingCount);

      final boolean branchCondition =
          processingCount < branchTrace.size() ? execution.value() : Randomizer.get().nextBoolean();

      Logger.debug("Branch execution: processingCount=%d, condition=%b, executionCount=%d",
          processingCount, branchCondition, executionCount);

      for (int i = 0; i < executionCount; i++) {
        initializer.addAll(
            makeInitializer(
                engineContext,
                processingCount,
                abstractCall,
                primitive,
                situation,
                branchCondition,
                true /* Write into the stream */)
        );
      }

      return initializer;
    } // Stream based.

    return Collections.<AbstractCall>emptyList();
    } finally {
      Logger.setDebug(isDebug);
    }
  }

  private List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final int processingCount,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
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
    EngineUtils.setUnknownImmValues(testData, queryCreator.getUnknownImmValues());

    // Initialize test data to ensure branch execution.
    for (final Map.Entry<String, Object> testDatum : testData.getBindings().entrySet()) {
      final String name = testDatum.getKey();
      final Primitive mode = queryCreator.getTargetModes().get(name);

      if (mode == null) {
        continue;
      }

      final BitVector value = FortressUtils.extractBitVector((Node) testDatum.getValue());
      initializer.addAll(EngineUtils.makeInitializer(engineContext, mode, value));

      if (writeIntoStream) {
        final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
        initializer.addAll(EngineUtils.makeStreamWrite(engineContext, testDataStream));
      }
    }

    return initializer;
  }

  private int getControlCodeExecutionCount(
      final BranchEntry branchEntry,
      final int processingCount) {
    int result = 0;

    final BranchTrace branchTrace = branchEntry.getBranchTrace();

    final BranchExecution execution = processingCount < branchTrace.size()
        ? branchTrace.get(processingCount)
        : branchTrace.getLastExecution();

    final Set<Integer> coverage = branchEntry.isControlCodeInBasicBlock()
        ? branchEntry.getBlockCoverage()
        : branchEntry.getSlotCoverage();

    final Map<Integer, Integer> segment;

    if (processingCount < branchTrace.size()) {
      // Blocks executed before the branch's current execution.
      segment = branchEntry.isControlCodeInBasicBlock()
          ? execution.getPreBlocks()
          : execution.getPreSlots();
    } else {
      // Blocks executed after the branch's last execution.
      segment = branchEntry.isControlCodeInBasicBlock()
          ? execution.getPostBlocks()
          : execution.getPostSlots();
    }

    for (final int item : coverage) {
      final Integer count = segment.get(item);
      result += (count != null ? count : 0);
    }

    Logger.debug(String.format(
        "Control code count: count=%d, coverage=%s, segment=%s", result, coverage, segment));

    return result;
  }
}
