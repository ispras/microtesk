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

package ru.ispras.microtesk.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.GeneratorConfig;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.ExceptionHandler;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link TestEngineUtils} class provides utility methods to be
 * used by the template processor to construct parts of test programs.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TestEngineUtils {
  private TestEngineUtils() {}

  public static TestSequenceEngine getEngine(final Block block) {
    InvariantChecks.checkNotNull(block);

    final String engineName;
    final String adapterName;

    if (block.isExternal()) {
      engineName = "trivial";
      adapterName = engineName;
    } else {
      engineName = block.getAttribute("engine", "default");
      adapterName = block.getAttribute("adapter", engineName);
    }

    final Engine<?> engine = GeneratorConfig.get().getEngine(engineName);
    InvariantChecks.checkNotNull(engine);

    final Adapter<?> adapter = GeneratorConfig.get().getAdapter(adapterName);
    InvariantChecks.checkNotNull(adapter);

    if (!adapter.getSolutionClass().isAssignableFrom(engine.getSolutionClass())) {
      throw new IllegalStateException("Mismatched solver/adapter pair");
    }

    final TestSequenceEngine testSequenceEngine = new TestSequenceEngine(engine, adapter);
    testSequenceEngine.configure(block.getAttributes());

    return testSequenceEngine;
  }

  public static List<Call> getSingleSequence(final Block block) {
    InvariantChecks.checkNotNull(block);

    final Iterator<List<Call>> iterator = block.getIterator();
    iterator.init();

    if (!iterator.hasValue()) {
      return Collections.emptyList();
    }

    final List<Call> result = iterator.value();

    iterator.next();
    InvariantChecks.checkFalse(iterator.hasValue(), "A single sequence is expected.");

    return result;
  }

  public static TestSequence getTestSequence(final AdapterResult adapterResult) {
    InvariantChecks.checkNotNull(adapterResult);

    if (adapterResult.getStatus() != AdapterResult.Status.OK) {
      throw new GenerationAbortedException(String.format(
          "Adapter Error: %s", adapterResult.getErrors()));
    }

    final TestSequence result = adapterResult.getResult();
    InvariantChecks.checkNotNull(result);

    return result;
  }

  public static TestSequence getSingleTestSequence(final Iterator<AdapterResult> iterator) {
    InvariantChecks.checkNotNull(iterator);

    iterator.init();
    InvariantChecks.checkTrue(iterator.hasValue());

    final TestSequence result = getTestSequence(iterator.value());

    iterator.next();
    InvariantChecks.checkFalse(iterator.hasValue(), "A single sequence is expected.");

    return result;
  }

  public static TestSequence makeTestSequenceForExternalBlock(
      final EngineContext engineContext,
      final Block block) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(block);
    InvariantChecks.checkTrue(block.isExternal());

    final TestSequenceEngine engine = getEngine(block);
    final List<Call> abstractSequence = getSingleSequence(block);

    final Iterator<AdapterResult> iterator = engine.process(engineContext, abstractSequence);
    return getSingleTestSequence(iterator);
  }

  public static TestSequence makeTestSequenceForExceptionHandler(
      final EngineContext engineContext,
      final ExceptionHandler.Section section) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(section);

    final List<Call> calls = new ArrayList<>();
    calls.add(Call.newComment(String.format("Exceptions: %s", section.getExceptions())));
    calls.add(Call.newOrigin(section.getOrigin(), false));
    calls.addAll(section.getCalls());

    final List<ConcreteCall> concreteCalls = EngineUtils.makeConcreteCalls(engineContext, calls);
    final TestSequence.Builder concreteSequenceBuilder = new TestSequence.Builder();
    concreteSequenceBuilder.add(concreteCalls);

    return concreteSequenceBuilder.build();
  }

  public static Pair<List<TestSequence>, Map<String, TestSequence>> makeExceptionHandler(
      final EngineContext engineContext,
      final ExceptionHandler handler) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(handler);

    final List<TestSequence> sequences = new ArrayList<>(handler.getSections().size());
    final Map<String, TestSequence> handlers = new LinkedHashMap<>(); 

    for (final ExceptionHandler.Section section : handler.getSections()) {
      final TestSequence sequence = makeTestSequenceForExceptionHandler(engineContext, section);
      sequences.add(sequence);

      for (final String exception : section.getExceptions()) {
        if (null != handlers.put(exception, sequence)) {
          Logger.warning("Exception handler for %s is redefined.", exception);
        }
      }
    }

    return new Pair<>(sequences, handlers);
  }

  /**
   * Checks whether the specified abstract sequence has a fixed origin.
   * <p>A sequence has a fixed origin if it starts with an {@code .org} directive
   * that specifies an absolute origin and comes before any executable calls.
   * 
   * @param sequence Abstract sequence to be checked.
   * @return {@code true} if the sequence has a fixed origin or {@code false} otherwise.
   * 
   * @throws IllegalArgumentException if the argument is {@code null}.
   */
  public static boolean isOriginFixed(final List<Call> sequence) {
    InvariantChecks.checkNotNull(sequence);

    for (final Call call : sequence) {
      if (null != call.getOrigin()) {
        return !call.isRelativeOrigin();
      }

      if (call.isExecutable()) {
        break;
      }
    }

    return false;
  }

  /**
   * Checks whether the specified label is defined in the abstract sequence.
   * <p>Note: Labels are considered equal if they have the same name.
   * 
   * @param sequence Abstract sequence to be checked.
   * @param label Label to be searched for.
   * @return {@code true} if the sequence defines the specified label or {@code false} otherwise.
   * 
   * @throws IllegalArgumentException if any of the arguments is {@code null}.
   */
  public static boolean isLabelDefined(final List<Call> sequence, final Label label) {
    InvariantChecks.checkNotNull(sequence);
    InvariantChecks.checkNotNull(label);

    for (final Call call : sequence) {
      for (final Label currentLabel : call.getLabels()) {
        if (label.getName().equals(currentLabel.getName())) {
          return true;
        }
      }
    }

    return false;
  }
}
