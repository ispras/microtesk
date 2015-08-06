/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils.makeConcreteCall;

import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.SelfCheck;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.microtesk.test.template.Primitive;

public final class SelfCheckEngine {
  private SelfCheckEngine() {}

  public static TestSequence solve(
      final EngineContext engineContext,
      final List<SelfCheck> checks) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(checks);

    try {
      final TestSequence.Builder sequenceBuilder = new TestSequence.Builder();
      for (final SelfCheck check : checks) {
        processCheck(engineContext, sequenceBuilder, check);
      }

      final TestSequence sequence = sequenceBuilder.build();

      final long baseAddress = engineContext.getAddress();
      final long newAddress = sequence.setAddress(baseAddress);
      engineContext.setAddress(newAddress);

      return sequence;
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException(e);
    }
  }

  private static void processCheck(
      final EngineContext engineContext,
      final TestSequence.Builder sequenceBuilder,
      final SelfCheck check) throws ConfigurationException {
    InvariantChecks.checkNotNull(check);
    Logger.debug("Processing %s...", check);

    final Primitive abstractMode = check.getMode().getModePrimitive();
    final IAddressingMode concreteMode = EngineUtils.makeMode(engineContext, abstractMode);

    final BitVector value =
        concreteMode.access().load().getRawData();

    Logger.debug("Expected value is 0x%s", value.toHexString());

    final Preparator comparator =
        engineContext.getPreparators().getComparator(abstractMode, value);

    if (null == comparator) {
      throw new GenerationAbortedException(
          String.format("No suitable comparator is found for %s.", check.getMode()));
    }

    final List<Call> abstractCalls =
        comparator.makeInitializer(abstractMode, value);

    for (final Call abstractCall : abstractCalls) {
      final ConcreteCall concreteCall = makeConcreteCall(engineContext, abstractCall);
      sequenceBuilder.add(concreteCall);
    }
  }
}
