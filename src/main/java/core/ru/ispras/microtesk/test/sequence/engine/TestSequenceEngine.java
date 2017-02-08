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

package ru.ispras.microtesk.test.sequence.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.sequence.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.test.template.Call;
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
public final class TestSequenceEngine implements Engine<AdapterResult> {
  private final Engine<?> engine;
  private final Adapter<?> adapter;

  public TestSequenceEngine(final Engine<?> engine, final Adapter<?> adapter) {
    InvariantChecks.checkNotNull(engine);
    InvariantChecks.checkNotNull(adapter);

    this.engine = engine;
    this.adapter = adapter;
  }

  public Iterator<AdapterResult> process(
      final EngineContext context, final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractSequence);

    final int instanceIndex = context.getModel().getActivePE();
    Logger.debugHeader("Processing Abstract Sequence (Instance %d)", instanceIndex);
    context.getStatistics().pushActivity(Statistics.Activity.PROCESSING);
    context.getModel().setUseTempState(true);

    try {
      final EngineResult<AdapterResult> result = solve(context, abstractSequence);
      checkResultStatus(result);

      return result.getResult();
    } finally {
      context.getModel().setUseTempState(false);
      context.getStatistics().popActivity(); // PROCESSING
    }
  }

  @Override
  public Class<AdapterResult> getSolutionClass() {
    return AdapterResult.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);

    engine.configure(attributes);
    adapter.configure(attributes);
  }

  @Override
  public EngineResult<AdapterResult> solve(
      final EngineContext context, final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractSequence);

    // Makes a copy as the abstract sequence can be modified by solver or adapter.
    List<Call> sequence = Call.copyAll(Call.expandAtomic(abstractSequence));

    allocateModes(sequence, context.getOptions().getValueAsBoolean(Option.RESERVE_EXPLICIT));
    sequence = expandPreparators(context, sequence);

    final EngineResult<?> result = engine.solve(context, sequence);
    if (result.getStatus() != EngineResult.Status.OK) {
      return new EngineResult<>(result.getStatus(), null, result.getErrors());
    }

    return adapt(adapter, context, sequence, result.getResult());
  }

  private static void allocateModes(
      final List<Call> abstractSequence, final boolean markExplicitAsUsed) {
    final ModeAllocator modeAllocator = ModeAllocator.get();
    if (null != modeAllocator) {
      modeAllocator.allocate(abstractSequence, markExplicitAsUsed);
    }
  }

  private static List<Call> expandPreparators(
      final EngineContext context, final List<Call> abstractSequence) {
    // Labels in repeated parts of a sequence have to be unique only on sequence level.
    LabelUniqualizer.get().resetNumbers();
    return Preparator.expandPreparators(null, context.getPreparators(), abstractSequence);
  }

  private static <T> EngineResult<AdapterResult> adapt(
      final Adapter<T> adapter,
      final EngineContext engineContext,
      final List<Call> abstractSequence,
      final Iterator<?> solutionIterator) {
    return new EngineResult<>(new Iterator<AdapterResult>() {
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
        final Object solution = solutionIterator.value(); 
        final Class<T> solutionClass = adapter.getSolutionClass();

        // Makes a copy as the adapter may modify the abstract sequence.
        final List<Call> abstractSequenceCopy = Call.copyAll(abstractSequence);

        try {
          engineContext.getModel().setUseTempState(true);
          return adapter.adapt(engineContext, abstractSequenceCopy, solutionClass.cast(solution));
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

  private static void checkResultStatus(final EngineResult<AdapterResult> result) {
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
