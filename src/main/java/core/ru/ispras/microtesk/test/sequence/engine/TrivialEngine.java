/*
 * Copyright 2016-2017 ISP RAS (http://www.ispras.ru)
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

import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

/**
 * The job of the {@link TrivialEngine} class is processing abstract instruction call
 * sequences to build a concrete instruction call sequences (that can be simulated and
 * printed into assembly code). The abstract calls are processed as they are.
 *
 * <p>NOTE: Processing abstract calls as they are means that the engine does perform
 * any presimulation, data generation or creation of initializing calls. Therefore,
 * it requires that abstract sequences NOT USE ANY symbolic forms of values like
 * unknown immediates ("_") and situations. Unknown values will cause exceptions.
 * Situations will be ignored.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class TrivialEngine implements Engine<TestSequence> {
  @Override
  public Class<TestSequence> getSolutionClass() {
    return TestSequence.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public EngineResult<TestSequence> solve(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    try {
      return new EngineResult<>(new SingleValueIterator<>(process(engineContext, abstractSequence)));
    } catch (final ConfigurationException e) {
      return new EngineResult<>(e.getMessage());
    }
  }

  private TestSequence process(
      final EngineContext engineContext,
      final List<Call> abstractSequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final TestSequence.Builder sequenceBuilder = new TestSequence.Builder();
    sequenceBuilder.add(EngineUtils.makeConcreteCalls(engineContext, abstractSequence));

    return sequenceBuilder.build();
  }
}
