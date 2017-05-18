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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.memory.AddressTranslator;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.engine.EngineConfig;
import ru.ispras.microtesk.test.engine.Adapter;
import ru.ispras.microtesk.test.engine.AdapterResult;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineResult;
import ru.ispras.microtesk.test.engine.TestSequenceEngine;
import ru.ispras.microtesk.test.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.Block;
import ru.ispras.microtesk.test.template.AbstractCall;
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

    final Engine<?> engine = EngineConfig.get().getEngine(engineName);
    InvariantChecks.checkNotNull(engine);

    final Adapter<?> adapter = EngineConfig.get().getAdapter(adapterName);
    InvariantChecks.checkNotNull(adapter);

    if (!adapter.getSolutionClass().isAssignableFrom(engine.getSolutionClass())) {
      throw new IllegalStateException("Mismatched solver/adapter pair");
    }

    final TestSequenceEngine testSequenceEngine = new TestSequenceEngine(engine, adapter);
    testSequenceEngine.configure(block.getAttributes());

    return testSequenceEngine;
  }

  public static List<AbstractCall> getSingleSequence(final Block block) {
    InvariantChecks.checkNotNull(block);

    final Iterator<List<AbstractCall>> iterator = block.getIterator();
    iterator.init();

    if (!iterator.hasValue()) {
      return Collections.emptyList();
    }

    final List<AbstractCall> result = iterator.value();

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

  public static TestSequence makeExternalTestSequence(
      final EngineContext engineContext,
      final Block block) {
    return makeExternalTestSequence(engineContext, block, "External Code");
  }

  public static TestSequence makeExternalTestSequence(
      final EngineContext engineContext,
      final Block block,
      final String title) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(block);
    InvariantChecks.checkTrue(block.isExternal());

    final TestSequenceEngine engine = getEngine(block);
    final List<AbstractCall> abstractSequence = getSingleSequence(block);

    final EngineResult<AdapterResult> engineResult = engine.process(engineContext, abstractSequence);
    final Iterator<AdapterResult> iterator = engineResult.getResult();

    final TestSequence sequence = getSingleTestSequence(iterator);
    sequence.setTitle(String.format("%s (%s)", title, block.getWhere()));

    return sequence;
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

  private static TestSequence makeTestSequenceForExceptionHandler(
      final EngineContext engineContext,
      final ExceptionHandler.Section section) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(section);

    final List<AbstractCall> calls = new ArrayList<>();
    calls.add(AbstractCall.newComment(String.format("Exceptions: %s", section.getExceptions())));
    calls.add(AbstractCall.newOrigin(section.getOrigin(), false));
    calls.addAll(section.getCalls());

    final List<ConcreteCall> concreteCalls = EngineUtils.makeConcreteCalls(engineContext, calls);
    final TestSequence.Builder concreteSequenceBuilder = new TestSequence.Builder();
    concreteSequenceBuilder.add(concreteCalls);

    return concreteSequenceBuilder.build();
  }

  public static BigInteger getSequenceAddress(final List<AbstractCall> sequence) {
    InvariantChecks.checkNotNull(sequence);

    for (final AbstractCall call : sequence) {
      final BigInteger origin = call.getOrigin();
      if (null != origin) {
        return !call.isRelativeOrigin() ?
            AddressTranslator.get().virtualFromOrigin(origin) : null;
      }

      if (call.isExecutable()) {
        break;
      }
    }

    return null;
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
  public static boolean isLabelDefined(final List<AbstractCall> sequence, final Label label) {
    InvariantChecks.checkNotNull(sequence);
    InvariantChecks.checkNotNull(label);

    for (final AbstractCall call : sequence) {
      for (final Label currentLabel : call.getLabels()) {
        if (label.getName().equals(currentLabel.getName())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Rethows the specified exception. Used to stop propagation of checked exceptions.
   * 
   * <p>Unchecked exceptions (extend {@link RuntimeException}) are rethrown as they are.
   * Checked exceptions are wrapped into the {@link GenerationAbortedException} class.
   * 
   * @param e Exception to be rethrown.
   */
  public static void rethrowException(final Exception e) {
    if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    } else {
      throw new GenerationAbortedException(e);
    }
  }

  public static boolean canBeAllocatedAfter(final TestSequence previous, final Block block) {
    return null == previous || previous.isAllocated() || isOriginFixed(block);
  }

  /**
   * Checks whether the specified block has a fixed origin. Applied only to external blocks
   * that hold a single sequence.
   * <p>A sequence has a fixed origin if it starts with an {@code .org} directive
   * that specifies an absolute origin and comes before any executable calls.
   * 
   * @param sequence Block to be checked.
   * @return {@code true} if the sequence has a fixed origin or {@code false} otherwise.
   * 
   * @throws IllegalArgumentException if the argument is {@code null} or it is not
   *         an external code block.
   */
  public static boolean isOriginFixed(final Block block) {
    InvariantChecks.checkNotNull(block);
    InvariantChecks.checkTrue(block.isExternal());

    final List<AbstractCall> sequence = getSingleSequence(block);
    InvariantChecks.checkNotNull(sequence);

    for (final AbstractCall call : sequence) {
      if (null != call.getOrigin()) {
        return !call.isRelativeOrigin();
      }

      if (call.isExecutable()) {
        break;
      }
    }

    return false;
  }

  public static boolean isAtEndOf(
      final Executor.Status status,
      final TestSequence sequence) {
    return sequence != null &&
           sequence.isAllocated() &&
           sequence.getEndAddress() == status.getAddress();
  }

  public static boolean isAtEndOfAny(
      final Executor.Status status,
      final Collection<TestSequence> sequences) {
    for (final TestSequence sequence : sequences) {
      return isAtEndOf(status, sequence);
    }

    return false;
  }

  public static int findAtEndOf(
      final List<Executor.Status> statuses,
      final TestSequence sequence) {
    if (statuses.isEmpty()) {
      // No instances started execution yet - return 0 (can select any)
      return 0;
    }

    for (int index = 0; index < statuses.size(); index++) {
      if (isAtEndOf(statuses.get(index), sequence)) {
        // Found it!
        return index;
      }
    }

    // Nothing is found.
    return -1;
  }

  public static void checkAllAtEndOf(
      final List<Executor.Status> statuses,
      final TestSequence sequence) {
    for (int index = 0; index < statuses.size(); index++) {
      final Executor.Status status = statuses.get(index);
      if (!isAtEndOf(status, sequence)) {
        throw new GenerationAbortedException(String.format(
            "Instance %d is at address %s and it cannot reach the end of test program.",
            index, status)
            );
      }
    }
  }

  /**
   * Sends notifications to all registered engines and adapters about
   * the start of generating a test program file.
   */
  public static void notifyProgramStart() {
    for (final Engine<?> engine : EngineConfig.get().getEngines()) {
      engine.onStartProgram();
    }

    for (final Adapter<?> adapter : EngineConfig.get().getAdapters()) {
      adapter.onStartProgram();
    }
  }

  /**
   * Sends notifications to all registered engines and adapters about
   * the end of generating a test program file.
   */
  public static void notifyProgramEnd() {
    for (final Engine<?> engine : EngineConfig.get().getEngines()) {
      engine.onEndProgram();
    }

    for (final Adapter<?> adapter : EngineConfig.get().getAdapters()) {
      adapter.onEndProgram();
    }
  }
}
