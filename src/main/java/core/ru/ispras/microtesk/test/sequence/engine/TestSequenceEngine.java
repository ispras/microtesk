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

import java.util.Collection;
import java.util.Collections;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.test.template.Call;

/**
 * @author <a href="mailto:kotsynyak@ispras.ru">Artem Kotsynyak</a>
 */
public final class TestSequenceEngine implements Engine<TestSequence> {
  private final Engine<?> engine;
  private final Adapter<?> adapter;

  public TestSequenceEngine(final Engine<?> engine, final Adapter<?> adapter) {
    InvariantChecks.checkNotNull(engine);
    InvariantChecks.checkNotNull(adapter);

    this.engine = engine;
    this.adapter = adapter;
  }

  @Override
  public Class<TestSequence> getSolutionClass() {
    return TestSequence.class;
  }

  @Override
  public EngineResult<TestSequence> solve(
      final EngineContext engineContext, final Sequence<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final EngineResult<?> result = solve(engine, engineContext, abstractSequence);

    if (result.getStatus() != EngineResult.Status.OK) {
      return new EngineResult<TestSequence>(result.getStatus(), null, result.getErrors());
    }

    return adapt(adapter, engineContext, abstractSequence, result.getResult());
  }

  public Iterator<TestSequence> process(
      final EngineContext engineContext, final Sequence<Call> abstractSequence) {
    final EngineResult<TestSequence> result = solve(engineContext, abstractSequence);

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
      final Sequence<Call> abstractSequence) {
    return engine.solve(engineContext, abstractSequence);
  }

  private static <T> EngineResult<TestSequence> adapt(
      final Adapter<T> adapter,
      final EngineContext engineContext,
      final Sequence<Call> abstractSequence,
      final Iterator<?> solutionIterator) {
    final Iterator<TestSequence> testSequenceIterator = new Iterator<TestSequence>() {
      @Override public void init() {
        solutionIterator.init();
      }

      @Override public boolean hasValue() {
        return solutionIterator.hasValue();
      }

      @Override public TestSequence value() {
        final Object solution = solutionIterator.value(); 
        final Class<T> solutionClass = adapter.getSolutionClass();
        final TestSequence testSequence =
            adapter.adapt(engineContext, abstractSequence, solutionClass.cast(solution));

        return testSequence;
      }

      @Override public void next() {
        solutionIterator.next();
      }
    };

    return new EngineResult<TestSequence>(
        EngineResult.Status.OK, testSequenceIterator, Collections.<String>emptyList());
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

