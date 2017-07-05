/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.ConcreteSequence;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.LabelUniqualizer;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link TestSequenceEngine} class processes an abstract sequence with the
 * specified solver engine and adapts the results with the specified adapter
 * to produce a collection of concrete sequences.
 * 
 * @author <a href="mailto:kotsynyak@ispras.ru">Artem Kotsynyak</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class TestSequenceEngine {
  private final Engine engine;
  private boolean isTrivial;

  public TestSequenceEngine(final Engine engine) {
    InvariantChecks.checkNotNull(engine);
    this.engine = engine;
    this.isTrivial = false;
  }

  public Iterator<ConcreteSequence> process(
      final EngineContext context, final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractSequence);

    final int instanceIndex = context.getModel().getActivePE();
    Logger.debugHeader("Processing Abstract Sequence (Instance %d)", instanceIndex);
    context.getStatistics().pushActivity(Statistics.Activity.PROCESSING);
    context.getModel().setUseTempState(true);

    try {
      return solve(context, abstractSequence);
    } finally {
      context.getModel().setUseTempState(false);
      context.getStatistics().popActivity(); // PROCESSING
    }
  }

  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);
    engine.configure(attributes);
    isTrivial = "trivial".equals(attributes.get("engine"));
  }

  public Iterator<ConcreteSequence> solve(
      final EngineContext context, final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractSequence);

    // Makes a copy as the abstract sequence can be modified by solver or adapter.
    List<AbstractCall> sequence = AbstractCall.copyAll(
        AbstractCall.expandAtomic(abstractSequence.getSequence()));

    allocateModes(sequence, context.getOptions().getValueAsBoolean(Option.RESERVE_EXPLICIT));
    sequence = expandPreparators(context, sequence);

    final AbstractSequence newAbstractSequence =
        new AbstractSequence(abstractSequence.getSection(), sequence);

    final Iterator<AbstractSequence> testBases = engine.solve(context, newAbstractSequence);
    return new SequenceConcretizer(context, isTrivial, testBases);
  }

  private static void allocateModes(
      final List<AbstractCall> abstractSequence, final boolean markExplicitAsUsed) {
    final ModeAllocator modeAllocator = ModeAllocator.get();
    if (null != modeAllocator) {
      modeAllocator.allocate(abstractSequence, markExplicitAsUsed);
    }
  }

  private static List<AbstractCall> expandPreparators(
      final EngineContext context, final List<AbstractCall> abstractSequence) {
    // Labels in repeated parts of a sequence have to be unique only on sequence level.
    LabelUniqualizer.get().resetNumbers();
    return Preparator.expandPreparators(null, context.getPreparators(), abstractSequence);
  }
}
