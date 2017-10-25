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
  public static final boolean USE_DELAY_SLOTS = true;

  @Override
  public void configure(final Map<String, Object> attributes) {}

  @Override
  public void onStartProgram() {}

  @Override
  public void onEndProgram() {}


  @Override
  public List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
      final TestData testData, /* Not in use */
      final Map<String, Argument> modes,
      final Set<AddressingModeWrapper> initializedModes) throws ConfigurationException {
    return makeInitializer(engineContext, abstractCall, primitive, situation);
  }

  private List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
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

    final TestBaseQueryCreator queryCreator =
        new TestBaseQueryCreator(
            engineContext,
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

  private List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkNotNull(situation);

    final BranchEntry branchEntry = BranchEngine.getBranchEntry(abstractCall);
    InvariantChecks.checkNotNull(branchEntry);

    final BranchTrace branchTrace = branchEntry.getBranchTrace();
    InvariantChecks.checkNotNull(branchTrace);

    // If the branch is not executed, initialize immediate arguments.
    if (branchTrace.isEmpty()) {
      return makeInitializer(
          engineContext,
          abstractCall,
          primitive,
          situation,
          false /* Branch is not taken */,
          false /* Branch condition is ignored */,
          true  /* Write into the stream */);
    }

    final List<AbstractCall> initializer = new ArrayList<>();

    // If the control code is not executed before the first branch execution,
    // the registers of the branch instruction should be initialized.
    boolean initNeeded = !branchTrace.isEmpty();
    // Data stream is not used if the trace is empty or consists of the same condition values.
    boolean streamUsed = false;

    // It cannot be guaranteed that two branches do not share a register.
    final boolean sharedRegisters = true;

    // There is no need to construct the control code if the branch condition does not change.
    // However, if multiple branches shares the same register, it makes sense.
    @SuppressWarnings("unused")
    final boolean streamBasedInitialization = sharedRegisters || branchTrace.getChangeNumber() > 0;

    if (streamBasedInitialization) {
      for (int i = 0; i < branchTrace.size(); i++) {
        final BranchExecution execution = branchTrace.get(i);
        final boolean branchCondition = execution.value();

        // Count defines how many times the control code is executed before calling the branch.
        final int count = getCount(branchEntry, execution);
        Logger.debug(String.format("Branch execution: i=%d, condition=%b, count=%d",
            i, branchCondition, count));

        if(i == 0 && count > 0) {
          initNeeded = false;
        }

        for (int j = 0; j < count; j++) {
          // Data stream should be initialized before the first write. 
          if (!streamUsed) {
            final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
            final List<AbstractCall> initDataStream =
                EngineUtils.makeStreamInit(engineContext, testDataStream);

            initializer.addAll(initDataStream);
            streamUsed = true;
          }

          initializer.addAll(
              makeInitializer(
                  engineContext,
                  abstractCall,
                  primitive,
                  situation,
                  true /* Branch is taken */,
                  branchCondition,
                  true /* Write into the stream */)
              );
        }
      }

      // Initialize the data stream if it was used. 
      if (streamUsed) {
        final String testDataStream = BranchEngine.getTestDataStream(abstractCall);
        final List<AbstractCall> initDataStream =
            EngineUtils.makeStreamInit(engineContext, testDataStream);

        initializer.addAll(initDataStream);
      }
    }

    // Initialize the registers if it is needed. 
    if (initNeeded) {
      final BranchExecution execution = branchTrace.get(0);
      final boolean branchCondition = execution.value();
      final List<AbstractCall> initRegisters = makeInitializer(
          engineContext,
          abstractCall,
          primitive,
          situation,
          true /* Branch is taken */,
          branchCondition,
          false /* Write into the registers */);

      initializer.addAll(initRegisters);
    }

    return initializer;
  }

  private int getCount(
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
