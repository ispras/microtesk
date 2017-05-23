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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.SelfCheck;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.engine.utils.AddressingModeWrapper;
import ru.ispras.microtesk.test.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.AbstractSequence;
import ru.ispras.microtesk.test.template.LabelUniqualizer;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.microtesk.utils.StringUtils;
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
  private final Adapter adapter;

  public TestSequenceEngine(final Engine engine, final Adapter adapter) {
    InvariantChecks.checkNotNull(engine);
    InvariantChecks.checkNotNull(adapter);

    this.engine = engine;
    this.adapter = adapter;
  }

  public TestSequenceEngineResult process(
      final EngineContext context, final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractSequence);

    final int instanceIndex = context.getModel().getActivePE();
    Logger.debugHeader("Processing Abstract Sequence (Instance %d)", instanceIndex);
    context.getStatistics().pushActivity(Statistics.Activity.PROCESSING);
    context.getModel().setUseTempState(true);

    try {
      final TestSequenceEngineResult result = solve(context, abstractSequence);
      checkResultStatus(result);

      return result;
    } finally {
      context.getModel().setUseTempState(false);
      context.getStatistics().popActivity(); // PROCESSING
    }
  }

  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);

    engine.configure(attributes);
    adapter.configure(attributes);
  }

  public TestSequenceEngineResult solve(
      final EngineContext context, final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractSequence);

    // Makes a copy as the abstract sequence can be modified by solver or adapter.
    List<AbstractCall> sequence = AbstractCall.copyAll(
        AbstractCall.expandAtomic(abstractSequence.getSequence()));

    allocateModes(sequence, context.getOptions().getValueAsBoolean(Option.RESERVE_EXPLICIT));
    sequence = expandPreparators(context, sequence);

    final AbstractSequence newAbstractSequence = new AbstractSequence(sequence);
    final EngineResult result = engine.solve(context, newAbstractSequence);

    if (result.getStatus() != EngineResult.Status.OK) {
      return new TestSequenceEngineResult(result.getStatus(), null, result.getErrors());
    }

    final TestSequenceEngineResult engineResult = adapt(adapter, context, result.getResult());

    // TODO: temporary implementation of self-checks.
    if (context.getOptions().getValueAsBoolean(Option.SELF_CHECKS)) {
      final List<SelfCheck> selfChecks = createSelfChecks(sequence);
      engineResult.setSelfChecks(selfChecks);
    }

    return engineResult;
  }

  private static void allocateModes(
      final List<AbstractCall> abstractSequence, final boolean markExplicitAsUsed) {
    final ModeAllocator modeAllocator = ModeAllocator.get();
    if (null != modeAllocator) {
      modeAllocator.allocate(abstractSequence, markExplicitAsUsed);
    }
  }

  private static List<SelfCheck> createSelfChecks(final List<AbstractCall> abstractSequence) {
    InvariantChecks.checkNotNull(abstractSequence);

    final Set<AddressingModeWrapper> modes = EngineUtils.getOutAddressingModes(abstractSequence);
    final List<SelfCheck> selfChecks = new ArrayList<>(modes.size());

    for (final AddressingModeWrapper mode : modes) {
      selfChecks.add(new SelfCheck(mode));
    }

    return selfChecks;
  }

  private static List<AbstractCall> expandPreparators(
      final EngineContext context, final List<AbstractCall> abstractSequence) {
    // Labels in repeated parts of a sequence have to be unique only on sequence level.
    LabelUniqualizer.get().resetNumbers();
    return Preparator.expandPreparators(null, context.getPreparators(), abstractSequence);
  }

  private static TestSequenceEngineResult adapt(
      final Adapter adapter,
      final EngineContext engineContext,
      final Iterator<AbstractSequence> solutionIterator) {
    return new TestSequenceEngineResult(new Iterator<AdapterResult>() {
      @Override
      public void init() {
        solutionIterator.init();
      }

      @Override
      public boolean hasValue() {
        return solutionIterator.hasValue();
      }

      @Override
      public AdapterResult value() {
        final AbstractSequence abstractSequence = solutionIterator.value();

        // Makes a copy as the adapter may modify the abstract sequence.
        final AbstractSequence abstractSequenceCopy =
            new AbstractSequence(AbstractCall.copyAll(abstractSequence.getSequence()));

        try {
          engineContext.getModel().setUseTempState(true);
          return adapter.adapt(engineContext, abstractSequenceCopy);
        } finally {
          engineContext.getModel().setUseTempState(false);
        }
      }

      @Override
      public void next() {
        solutionIterator.next();
      }

      @Override
      public void stop() {
        solutionIterator.stop();
      }

      @Override
      public Iterator<AdapterResult> clone() {
        throw new UnsupportedOperationException();
      }
    });
  }

  private static void checkResultStatus(final TestSequenceEngineResult result) {
    if (EngineResult.Status.OK != result.getStatus()) {
      throw new IllegalStateException(listErrors(
          "Failed to find a solution for an abstract call sequence.", result.getErrors()));
    }
  }

  private static String listErrors(final String message, final Collection<String> errors) {
    if (errors.isEmpty()) {
      return message;
    }

    final String separator = System.lineSeparator() + "  ";
    return message + " Errors:" + separator + StringUtils.toString(errors, separator);
  }
}
