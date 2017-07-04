/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.ConcreteSequence;
import ru.ispras.microtesk.test.sequence.GeneratorConfig;
import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class AbstractSequenceProcessor {
  private AbstractSequenceProcessor() {}
  private static AbstractSequenceProcessor instance = null;

  public static AbstractSequenceProcessor get() {
    if (null == instance) {
      instance = new AbstractSequenceProcessor();
    }
    return instance;
  }

  public Iterator<ConcreteSequence> process(
      final EngineContext engineContext,
      final Map<String, Object> attributes,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(attributes);
    InvariantChecks.checkNotNull(abstractSequence);

    final List<Iterator<AbstractSequence>> iterators = new ArrayList<>();
    for (final Engine engine : EngineConfig.get().getEngines()) {
      final SequenceSelector selector = engine.getSequenceSelector(); 
      final AbstractSequence engineSequence = selector.select(abstractSequence);

      final EngineResult engineResult = engine.solve(engineContext, engineSequence);
      iterators.add(engineResult.getResult());
    }

    //final Iterator<List<AbstractSequence>> combinator = makeCombinator(combinatorName, iterators);
    return null;
  }

  private static Iterator<List<AbstractSequence>> makeCombinator(
      final String combinatorName,
      final List<Iterator<AbstractSequence>> iterators) {
    InvariantChecks.checkNotNull(combinatorName);
    InvariantChecks.checkNotNull(iterators);

    final GeneratorConfig<AbstractSequence> config = GeneratorConfig.get();
    final Combinator<AbstractSequence> combinator = config.getCombinator(combinatorName);

    combinator.initialize(iterators);
    combinator.init();

    return combinator;
  }
}
