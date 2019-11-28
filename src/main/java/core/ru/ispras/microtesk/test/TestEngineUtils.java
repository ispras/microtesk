/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.engine.*;
import ru.ispras.microtesk.test.template.*;
import ru.ispras.microtesk.test.template.directive.Directive;
import ru.ispras.microtesk.test.template.directive.DirectiveOrigin;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.*;

/**
 * {@link TestEngineUtils} implements utility methods to be used by the template processor
 * to construct parts of test programs.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class TestEngineUtils {
  private TestEngineUtils() {}

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

  public static ConcreteSequence makeExternalTestSequence(
      final EngineContext engineContext,
      final Block block) throws ConfigurationException {
    return makeExternalTestSequence(engineContext, block, "External Code");
  }

  public static ConcreteSequence makeExternalTestSequence(
      final EngineContext engineContext,
      final Block block,
      final String title) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(block);
    InvariantChecks.checkTrue(block.isExternal());

    final List<AbstractCall> abstractSequence = getSingleSequence(block);

    LabelUniqualizer.get().resetNumbers();
    final List<AbstractCall> expandedAbstractSequence =
        Preparator.expandPreparators(null, engineContext.getPreparators(), abstractSequence);

    final List<ConcreteCall> concreteCalls =
        EngineUtils.makeConcreteCalls(engineContext, expandedAbstractSequence);

    final ConcreteSequence sequence =
        ConcreteSequence.newConcreteSequence(block.getSection(), concreteCalls);

    sequence.setTitle(String.format("%s (%s)", title, block.getWhere()));
    return sequence;
  }

  public static Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>> makeExceptionHandler(
      final EngineContext engineContext,
      final ExceptionHandler exceptionHandler) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(exceptionHandler);

    final List<ConcreteSequence> sequences =
        new ArrayList<>(exceptionHandler.getEntryPoints().size());
    final Map<String, ConcreteSequence> handlers = new LinkedHashMap<>();

    for (final ExceptionHandler.EntryPoint entryPoint : exceptionHandler.getEntryPoints()) {
      final ConcreteSequence sequence = makeTestSequenceForExceptionHandler(
          engineContext, exceptionHandler.getSection(), entryPoint);
      sequences.add(sequence);

      for (final String exception : entryPoint.getExceptions()) {
        if (null != handlers.put(exception, sequence)) {
          Logger.warning("Exception handler for %s is redefined.", exception);
        }
      }
    }

    return new Pair<>(sequences, handlers);
  }

  private static ConcreteSequence makeTestSequenceForExceptionHandler(
      final EngineContext engineContext,
      final Section section,
      final ExceptionHandler.EntryPoint entryPoint) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(entryPoint);

    final List<AbstractCall> calls = new ArrayList<>();
    final AbstractCallBuilder abstractCallBuilder = new AbstractCallBuilder(new BlockId());

    final Output comment = new Output(
        Output.Kind.COMMENT, String.format("Exceptions: %s", entryPoint.getExceptions()));

    abstractCallBuilder.addOutput(comment);
    abstractCallBuilder.setDirective(engineContext.getDataDirectiveFactory().newOrigin(entryPoint.getOrigin()));

    calls.add(abstractCallBuilder.build());
    calls.addAll(entryPoint.getCalls());

    final List<ConcreteCall> concreteCalls = EngineUtils.makeConcreteCalls(engineContext, calls);
    final ConcreteSequence.Builder concreteSequenceBuilder = new ConcreteSequence.Builder(section);
    concreteSequenceBuilder.add(concreteCalls);

    return concreteSequenceBuilder.build();
  }

  /**
   * Checks whether the specified label is defined in the abstract sequence.
   * 
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
   * Re-throws the specified exception. Used to stop propagation of checked exceptions.
   *
   * <p>Unchecked exceptions (extend {@link RuntimeException}) are re-thrown as they are.
   * Checked exceptions are wrapped into the {@link GenerationAbortedException} class.
   *
   * @param e Exception to be re-thrown.
   */
  public static void rethrowException(final Exception e) {
    if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    } else {
      throw new GenerationAbortedException(e);
    }
  }

  public static boolean canBeAllocatedAfter(final ConcreteSequence previous, final Block block) {
    return null == previous || previous.isAllocated() || isOriginFixed(block);
  }

  /**
   * Checks whether the specified block has a fixed origin. Applied only to external blocks
   * that hold a single sequence.
   * <p>A sequence has a fixed origin if it starts with an {@code .org} directive
   * that specifies an absolute origin and comes before any executable calls.
   *
   * @param block Block to be checked.
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
      final Directive directive = call.getDirective();

      // Checks whether the directive fixes the memory location.
      if (null != directive && directive instanceof DirectiveOrigin) {
        return true;
      }

      if (call.isExecutable()) {
        break;
      }
    }

    return false;
  }

  public static boolean isAtEndOf(
      final Executor.Status status,
      final ConcreteSequence sequence) {
    return sequence != null
        && sequence.isAllocated()
        && sequence.getEndAddress() == status.getAddress();
  }

  public static boolean isAtEndOfAny(
      final Executor.Status status,
      final Collection<ConcreteSequence> sequences) {
    for (final ConcreteSequence sequence : sequences) {
      return isAtEndOf(status, sequence);
    }

    return false;
  }

  public static int findAtEndOf(
      final List<Executor.Status> statuses,
      final ConcreteSequence sequence) {
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
      final ConcreteSequence sequence) {
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
    for (final Engine engine : EngineConfig.get().getEngines()) {
      engine.onStartProgram();
    }

    for (final InitializerMaker initializerMaker : EngineConfig.get().getInitializerMakers()) {
      initializerMaker.onStartProgram();
    }
  }

  /**
   * Sends notifications to all registered engines and adapters about
   * the end of generating a test program file.
   */
  public static void notifyProgramEnd() {
    for (final Engine engine : EngineConfig.get().getEngines()) {
      engine.onEndProgram();
    }

    for (final InitializerMaker initializerMaker : EngineConfig.get().getInitializerMakers()) {
      initializerMaker.onEndProgram();
    }
  }
}
