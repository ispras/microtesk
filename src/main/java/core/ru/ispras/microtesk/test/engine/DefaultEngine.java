/*
 * Copyright 2013-2017 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.test.template.AbstractSequence;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

/**
 * The job of the {@link DefaultEngine} class is to processes an abstract instruction call
 * sequence (uses symbolic values) and to build a concrete instruction call sequence (uses only
 * concrete values and can be simulated and used to generate source code in assembly language).
 * The {@link DefaultEngine} class performs all necessary data generation and all initializing
 * calls to the generated instruction sequence.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DefaultEngine implements Engine {
  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public void onStartProgram() {
    // Empty
  }

  @Override
  public void onEndProgram() {
    // Empty
  }

  @Override
  public EngineResult solve(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    return new EngineResult(new SingleValueIterator<>(abstractSequence));
  }
}
