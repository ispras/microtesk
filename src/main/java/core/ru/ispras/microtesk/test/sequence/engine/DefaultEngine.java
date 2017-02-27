/*
 * Copyright 2013-2017 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils.checkRootOp;
import static ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils.makeConcreteCall;
import static ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils.makeInitializer;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.ProcessingElement;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.SelfCheck;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.utils.AddressingModeWrapper;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

/**
 * The job of the {@link DefaultEngine} class is to processes an abstract instruction call
 * sequence (uses symbolic values) and to build a concrete instruction call sequence (uses only
 * concrete values and can be simulated and used to generate source code in assembly language).
 * The {@link DefaultEngine} class performs all necessary data generation and all initializing
 * calls to the generated instruction sequence.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DefaultEngine implements Engine<TestSequence> {
  private Set<AddressingModeWrapper> initializedModes;
  private TestSequence.Builder sequenceBuilder;

  @Override
  public Class<TestSequence> getSolutionClass() {
    return TestSequence.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public void onStartProgram() {
    // Empty
  }

  @Override
  public void onEndProgram() {
    // Empty
  }

  @Override
  public EngineResult<TestSequence> solve(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    try {
      return new EngineResult<>(new SingleValueIterator<>(process(engineContext, abstractSequence)));
    } catch (final ConfigurationException e) {
      return new EngineResult<>(e.getMessage());
    }
  }

  private TestSequence process(
      final EngineContext engineContext,
      final List<Call> abstractSequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    initializedModes = new HashSet<>();
    sequenceBuilder = new TestSequence.Builder();

    try {
      for (final Call abstractCall : abstractSequence) {
        processAbstractCall(engineContext, abstractCall);
      }

      createSelfChecks(engineContext, abstractSequence);
      return sequenceBuilder.build();
    } finally {
      initializedModes = null;
      sequenceBuilder = null;
    }
  }

  private void createSelfChecks(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final boolean isSelfChecks = engineContext.getOptions().getValueAsBoolean(Option.SELF_CHECKS);
    if (!isSelfChecks) {
      return;
    }

    final Set<AddressingModeWrapper> modes = EngineUtils.getOutAddressingModes(abstractSequence);
    for (final AddressingModeWrapper mode : modes) {
      sequenceBuilder.addCheck(new SelfCheck(mode));
    }
  }

  private void registerCall(
      final ProcessingElement processingElement,
      final ConcreteCall call,
      final LabelManager labelManager) {
    InvariantChecks.checkNotNull(call);

    patchLabels(call, labelManager);
    call.execute(processingElement);

    sequenceBuilder.add(call);
  }

  private void registerPrologueCall(
      final ProcessingElement processingElement,
      final ConcreteCall call,
      final LabelManager labelManager) {
    InvariantChecks.checkNotNull(call);

    patchLabels(call, labelManager);
    call.execute(processingElement);

    sequenceBuilder.addToPrologue(call);
  }

  private static void patchLabels(final ConcreteCall call, final LabelManager labelManager) {
    for (final LabelReference labelRef : call.getLabelReferences()) {
      labelRef.resetTarget();

      final Label source = labelRef.getReference();
      final LabelManager.Target target = labelManager.resolve(source);

      if (null != target) {
        final long address = target.getAddress();
        labelRef.getPatcher().setValue(BigInteger.valueOf(address));
      }
    }
  }

  private void processAbstractCall(
      final EngineContext context,
      final Call abstractCall) throws ConfigurationException {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractCall);

    final boolean isDebug = Logger.isDebug();
    Logger.setDebug(context.getOptions().getValueAsBoolean(Option.DEBUG));

    try {
      // Only executable calls are worth printing.
      if (abstractCall.isExecutable()) {
        Logger.debug("%nProcessing %s...", abstractCall);

        final Primitive rootOp = abstractCall.getRootOperation();
        checkRootOp(rootOp);

        processSituations(context, rootOp);
      }

      final ConcreteCall concreteCall = makeConcreteCall(context, abstractCall);
      registerCall(context.getModel().getPE(), concreteCall, context.getLabelManager());
    } finally {
      Logger.setDebug(isDebug);
    }
  }

  private void processSituations(
      final EngineContext engineContext,
      final Primitive primitive) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(primitive);

    for (Argument arg : primitive.getArguments().values()) {
      if (Argument.Kind.OP == arg.getKind()) {
        processSituations(engineContext, (Primitive) arg.getValue());
      }
    }

    final List<Call> initializingCalls = makeInitializer(
        engineContext, primitive, primitive.getSituation(), initializedModes);
    addCallsToPrologue(engineContext, initializingCalls);
  }

  private void addCallsToPrologue(
      final EngineContext context,
      final List<Call> abstractCalls) throws ConfigurationException {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(abstractCalls);

    for (final Call abstractCall : abstractCalls) {
      final ConcreteCall concreteCall = makeConcreteCall(context, abstractCall);
      registerPrologueCall(context.getModel().getPE(), concreteCall, context.getLabelManager());
    }
  }
}
