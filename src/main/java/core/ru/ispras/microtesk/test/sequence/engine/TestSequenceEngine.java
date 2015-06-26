/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.microtesk.test.sequence.engine.common.EngineUtils.allocateModes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.test.template.Call;

/**
 * @author <a href="mailto:kotsynyak@ispras.ru">Artem Kotsynyak</a>
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
      final EngineContext engineContext, final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final EngineResult<?> result = solve(engine, engineContext, abstractSequence);

    if (result.getStatus() != EngineResult.Status.OK) {
      return new EngineResult<AdapterResult>(result.getStatus(), null, result.getErrors());
    }

    return adapt(adapter, engineContext, abstractSequence, result.getResult());
  }

  public Iterator<AdapterResult> process(
      final EngineContext engineContext, final List<Call> abstractSequence) {
    final EngineResult<AdapterResult> result = solve(engineContext, abstractSequence);

    if (result.getStatus() != EngineResult.Status.OK) {
      final String msg = listErrors(
          "Failed to find a solution for abstract call sequence", result.getErrors());

      throw new IllegalStateException(msg);
    }

    return result.getResult();
  }

  private static <T> EngineResult<T> solve(
      final Engine<T> engine,
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    // Solver may modify the abstract sequence.
    final List<Call> abstractSequenceCopy = Call.newCopy(abstractSequence);
    return engine.solve(engineContext, abstractSequenceCopy);
  }

  private static <T> EngineResult<AdapterResult> adapt(
      final Adapter<T> adapter,
      final EngineContext engineContext,
      final List<Call> abstractSequence,
      final Iterator<?> solutionIterator) {
    final Iterator<AdapterResult> resultIterator = new Iterator<AdapterResult>() {
      @Override public void init() {
        solutionIterator.init();
      }

      @Override public boolean hasValue() {
        return solutionIterator.hasValue();
      }

      @Override public AdapterResult value() {
        final Object solution = solutionIterator.value(); 
        final Class<T> solutionClass = adapter.getSolutionClass();

        // Adapter may modify the abstract sequence.
        final List<Call> abstractSequenceCopy = Call.newCopy(abstractSequence);
        // Allocate uninitialized addressing modes.
        allocateModes(abstractSequenceCopy);

        Memory.setUseTempCopies(true);

        return adapter.adapt(engineContext, abstractSequenceCopy, solutionClass.cast(solution));
      }

      @Override public void next() {
        solutionIterator.next();
      }
    };

    return new EngineResult<AdapterResult>(resultIterator);
  }

  private static String listErrors(final String message, final Collection<String> errors) {
    if (errors.isEmpty()) {
      return message;
    }
    final StringBuilder builder = new StringBuilder(message);
    builder.append(" Errors:");
    for (final String error : errors) {
      builder.append(System.lineSeparator() + "  " + error);
    }
    return builder.toString();
  }
}

