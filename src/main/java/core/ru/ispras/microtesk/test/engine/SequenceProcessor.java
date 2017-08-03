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
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.ConcreteSequence;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.sequence.GeneratorConfig;
import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.LabelUniqualizer;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

public final class SequenceProcessor {
  private SequenceProcessor() {}
  private static SequenceProcessor instance = null;

  public static SequenceProcessor get() {
    if (null == instance) {
      instance = new SequenceProcessor();
    }
    return instance;
  }

  public Iterator<ConcreteSequence> process(
      final EngineContext context,
      final Map<String, Object> attributes,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractSequence);

    final int instanceIndex = context.getModel().getActivePE();
    Logger.debugHeader("Processing Abstract Sequence (Instance %d)", instanceIndex);

    context.getStatistics().pushActivity(Statistics.Activity.PROCESSING);
    try {
      return processSequence(context, attributes, abstractSequence);
    } finally {
      context.getStatistics().popActivity(); // PROCESSING
    }
  }

  private Iterator<ConcreteSequence> processSequence(
      final EngineContext engineContext,
      final Map<String, Object> attributes,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(attributes);
    InvariantChecks.checkNotNull(abstractSequence);

    final String engineId = (String) attributes.get("engine");

    final boolean isTrivial = "trivial".equals(engineId);
    final boolean isBranch = "branch".equals(engineId);

    final AbstractSequence defaultAbstractSequence =
        expandAbstractSequence(engineContext, abstractSequence);

    // FIXME: Temporary implementation
    if (isBranch) {
      final Engine engine = EngineConfig.get().getEngine(engineId);
      engine.configure(attributes);

      final Iterator<AbstractSequence> iterator = engine.solve(engineContext, defaultAbstractSequence);
      return new SequenceConcretizer(engineContext, false, iterator);
    }

    final List<Iterator<AbstractSequence>> iterators = new ArrayList<>();
    for (final Engine engine : EngineConfig.get().getEngines()) {
      final SequenceSelector selector = engine.getSequenceSelector(); 
      final AbstractSequence engineSequence = selector.select(defaultAbstractSequence);

      if (null != engineSequence) {
        engine.configure(attributes);
        final Iterator<AbstractSequence> iterator = engine.solve(engineContext, engineSequence);

        InvariantChecks.checkNotNull(iterator);
        iterators.add(iterator);
      }
    }

    if (iterators.isEmpty()) {
      return new SequenceConcretizer(
          engineContext, isTrivial, new SingleValueIterator<>(defaultAbstractSequence));
    }

    final Iterator<List<AbstractSequence>> combinator =
        makeCombinator("diagonal", iterators);

    final Iterator<AbstractSequence> merger =
        new SequenceMerger(defaultAbstractSequence, combinator);

    return new SequenceConcretizer(engineContext, isTrivial, merger);
  }

  private static AbstractSequence expandAbstractSequence(
      final EngineContext context,
      final AbstractSequence abstractSequence) {
    // Makes a copy as the abstract sequence can be modified by solver or adapter.
    final List<AbstractCall> calls = AbstractCall.copyAll(
        AbstractCall.expandAtomic(abstractSequence.getSequence()));

    allocateModes(calls, context.getOptions().getValueAsBoolean(Option.RESERVE_EXPLICIT));

    final List<AbstractCall> expandedCalls = expandPreparators(context, calls);
    return new AbstractSequence(abstractSequence.getSection(), expandedCalls);
  }

  private static void allocateModes(
      final List<AbstractCall> abstractSequence,
      final boolean markExplicitAsUsed) {
    final ModeAllocator modeAllocator = ModeAllocator.get();
    if (null != modeAllocator) {
      modeAllocator.allocate(abstractSequence, markExplicitAsUsed);
    }
  }

  private static List<AbstractCall> expandPreparators(
      final EngineContext context,
      final List<AbstractCall> abstractSequence) {
    // Labels in repeated parts of a sequence have to be unique only on sequence level.
    LabelUniqualizer.get().resetNumbers();
    return Preparator.expandPreparators(null, context.getPreparators(), abstractSequence);
  }

  private static <T> Iterator<List<T>> makeCombinator(
      final String combinatorName,
      final List<Iterator<T>> iterators) {
    InvariantChecks.checkNotNull(combinatorName);
    InvariantChecks.checkNotNull(iterators);

    final GeneratorConfig<T> config = GeneratorConfig.get();
    final Combinator<T> combinator = config.getCombinator(combinatorName);

    combinator.initialize(iterators);
    return combinator;
  }
}

