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

import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.test.ConcreteSequence;
import ru.ispras.microtesk.test.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.AbstractSequence;

/**
 * @author <a href="mailto:kotsynyak@ispras.ru">Artem Kotsynyak</a>
 */
public final class TrivialAdapter implements Adapter {
  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public AdapterResult adapt(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    try {
      return new AdapterResult(process(engineContext, abstractSequence));
    } catch (final ConfigurationException e) {
      return new AdapterResult(e.getMessage());
    }
  }

  private ConcreteSequence process(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final ConcreteSequence.Builder sequenceBuilder = new ConcreteSequence.Builder();
    sequenceBuilder.add(
        EngineUtils.makeConcreteCalls(engineContext, abstractSequence.getSequence()));

    return sequenceBuilder.build();
  }

  @Override
  public void onStartProgram() {
    // Empty
  }

  @Override
  public void onEndProgram() {
    // Empty
  }
}
