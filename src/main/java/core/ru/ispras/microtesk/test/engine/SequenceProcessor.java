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
import java.util.Collections;
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
      final EngineContext engineContext,
      final Map<String, Object> attributes,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final int instanceIndex = engineContext.getModel().getActivePE();
    Logger.debugHeader("Processing Abstract Sequence (Instance %d)", instanceIndex);

    engineContext.getStatistics().pushActivity(Statistics.Activity.PROCESSING);
    try {
      final AbstractSequence expandedAbstractSequence =
          expandAbstractSequence(engineContext, abstractSequence);

      return processSequence(engineContext, attributes, expandedAbstractSequence);
    } finally {
      engineContext.getStatistics().popActivity(); // PROCESSING
    }
  }

  private Iterator<ConcreteSequence> processSequence(
      final EngineContext engineContext,
      final Map<String, Object> attributes,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(attributes);
    InvariantChecks.checkNotNull(abstractSequence);

    final Object enginesAttribute = attributes.get("engines");

    final boolean isEnginesEnabled = !Boolean.FALSE.equals(enginesAttribute);
    final boolean isPresimulation = !Boolean.FALSE.equals(attributes.get("presimulation"));

    if (!isEnginesEnabled) {
      // All engines are disabled
      return new SequenceConcretizer(
          engineContext, isPresimulation, new SingleValueIterator<>(abstractSequence));
    }

    final Map<String, Object> engineAttributeMap = toMap(enginesAttribute);
    final Iterator<AbstractSequence> abstractSequenceIterator =
        processSequenceWithEngines(engineContext, engineAttributeMap, abstractSequence);

    return new SequenceConcretizer(engineContext, isPresimulation, abstractSequenceIterator);
  }

  private static Iterator<AbstractSequence> processSequenceWithEngines(
      final EngineContext engineContext,
      final Map<String, Object> engineAttributes,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(engineAttributes);
    InvariantChecks.checkNotNull(abstractSequence);

    final List<Iterator<AbstractSequence>> iterators = new ArrayList<>();

    for (final Engine engine : EngineConfig.get().getEngines()) {
      final Iterator<AbstractSequence> iterator =
          processSequenceWithEngine(engine, engineContext, engineAttributes, abstractSequence);

      if (null != iterator) {
        iterators.add(iterator);
      }
    }

    if (iterators.isEmpty()) {
      return new SingleValueIterator<>(abstractSequence);
    }

    final Object combinatorId = engineAttributes.get("combinator");
    final Iterator<List<AbstractSequence>> combinator =
        makeCombinator(null != combinatorId ? combinatorId.toString() : "diagonal", iterators);

    return new SequenceMerger(abstractSequence, combinator);
  }

  private static Iterator<AbstractSequence> processSequenceWithEngine(
      final Engine engine,
      final EngineContext engineContext,
      final Map<String, Object> attributes,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engine);
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(attributes);
    InvariantChecks.checkNotNull(abstractSequence);

    final String engineId = engine.getId();
    final Object engineAttribute = attributes.get(engineId);

    final boolean isEngineEnabled = !Boolean.FALSE.equals(engineAttribute);
    if (!isEngineEnabled) {
      // The engines is disabled.
      return null;
    }

    final SequenceSelector selector = engine.getSequenceSelector();
    final AbstractSequence engineSequence = selector.select(abstractSequence);

    if (null == engineSequence) {
      // No calls are selected for processing.
      return null;
    }

    final Map<String, Object> engineAttributeMap = toMap(engineAttribute);
    if (engineAttributeMap.isEmpty()) {
      Logger.warning("No attributes are provided for the '%s' engine.", engineId);
    }

    engine.configure(engineAttributeMap);
    final Iterator<AbstractSequence> iterator = engine.solve(engineContext, engineSequence);
    InvariantChecks.checkNotNull(iterator);

    return iterator;
  }

  private static AbstractSequence expandAbstractSequence(
      final EngineContext engineContext,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    // Makes a copy as the abstract sequence can be modified by solver or adapter.
    final List<AbstractCall> calls = AbstractCall.copyAll(
        AbstractCall.expandAtomic(abstractSequence.getSequence()));

    allocateModes(calls, engineContext.getOptions().getValueAsBoolean(Option.RESERVE_EXPLICIT));

    final List<AbstractCall> expandedCalls = expandPreparators(engineContext, calls);
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

  private static AbstractSequence expandProloguesAndEpilogues(
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(abstractSequence);
    final List<AbstractCall> abstractCalls = new ArrayList<>();

    for (int index = 0; index < abstractSequence.getSequence().size(); ++index) {
      if (null != abstractSequence.getPrologues()) {
        final List<AbstractCall> prologue = abstractSequence.getPrologues().get(index);
        if (null != prologue) {
          abstractCalls.addAll(prologue);
        }
      }

      final AbstractCall call = abstractSequence.getSequence().get(index);
      abstractCalls.add(call);

      if (null != abstractSequence.getEpilogues()) {
        final List<AbstractCall> epilogue = abstractSequence.getEpilogues().get(index);
        if (null != epilogue) {
          abstractCalls.addAll(epilogue);
        }
      }
    }

    return new AbstractSequence(abstractSequence.getSection(), abstractCalls);
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

  @SuppressWarnings("unchecked")
  private static Map<String, Object> toMap(final Object object) {
    if (object instanceof Map) {
      return (Map<String, Object>) object;
    }

    if (null != object) {
      Logger.warning(object + " is not a map. Empty map will be used.");
    }

    return Collections.emptyMap();
  }
}
