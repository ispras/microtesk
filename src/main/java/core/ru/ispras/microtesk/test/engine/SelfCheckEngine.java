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

package ru.ispras.microtesk.test.engine;

import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.SelfCheck;
import ru.ispras.microtesk.test.ConcreteSequence;
import ru.ispras.microtesk.test.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.Primitive;

public final class SelfCheckEngine {
  private SelfCheckEngine() {}

  public static ConcreteSequence solve(
      final EngineContext engineContext,
      final List<SelfCheck> checks) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(checks);

    try {
      final ConcreteSequence.Builder sequenceBuilder = new ConcreteSequence.Builder();
      for (final SelfCheck check : checks) {
        processCheck(engineContext, sequenceBuilder, check);
      }
      return sequenceBuilder.build();
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException(e);
    }
  }

  private static void processCheck(
      final EngineContext engineContext,
      final ConcreteSequence.Builder sequenceBuilder,
      final SelfCheck check) throws ConfigurationException {
    InvariantChecks.checkNotNull(check);
    Logger.debug("Processing %s...", check);

    final Primitive abstractMode = check.getMode().getModePrimitive();
    final IsaPrimitive concreteMode = EngineUtils.makeMode(engineContext, abstractMode);
    final Model model = engineContext.getModel();

    final BitVector value =
        concreteMode.access(model.getPE(), model.getTempVars()).load().getRawData();

    Logger.debug("Expected value is 0x%s", value.toHexString());

    final PreparatorStore preparators = engineContext.getPreparators();
    final Preparator comparator = preparators.getComparator(abstractMode, value, null);

    if (null == comparator) {
      throw new GenerationAbortedException(
          String.format("No suitable comparator is found for %s.", check.getMode()));
    }

    final List<AbstractCall> abstractCalls =
        comparator.makeInitializer(preparators, abstractMode, value, null);

    for (final AbstractCall abstractCall : abstractCalls) {
      final ConcreteCall concreteCall = EngineUtils.makeConcreteCall(engineContext, abstractCall);
      sequenceBuilder.add(concreteCall);
    }
  }
}
