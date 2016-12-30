/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.ProcessingElement;
import ru.ispras.microtesk.model.api.memory.LocationAccessor;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.model.api.tarmac.Record;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Output;

/**
 * The role of the {@link Executor} class is to execute (simulate) sequences of instruction calls
 * (concrete calls). It executes instruction by instruction, perform control transfers by labels
 * (if needed) and prints information about important events to the simulator log (currently,
 * the console).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Executor {
  /**
   * The {@link Listener} interface is to be implemented by classes that monitor
   * execution of instruction calls.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public interface Listener {
    void onBeforeExecute(EngineContext context, ConcreteCall call);
    void onAfterExecute(EngineContext context, ConcreteCall call);
  }

  private static final class LabelTracker {
    private final int delaySlotSize;
    private List<LabelReference> labels;
    private int distance;

    public LabelTracker(final int delaySlotSize) {
      InvariantChecks.checkGreaterOrEqZero(delaySlotSize);
      this.delaySlotSize = delaySlotSize;
      reset();
    }

    public void track(final ConcreteCall call) {
      if (!call.isExecutable()) {
        return;
      }

      if (labels != null) {
        if (distance > delaySlotSize) {
          reset();
        } else {
          distance++;
        }
      }

      if (!call.getLabelReferences().isEmpty()) {
        labels = call.getLabelReferences();
        distance = 0;
      }
    }

    public void reset() {
      this.labels = null;
      this.distance = 0;
    }

    public LabelReference getLabel() {
      return null != labels ? labels.get(0) : null;
    }
  }

  private final EngineContext context;
  private Listener listener;

  private final ConcreteCall exceptionCall;
  private final ConcreteCall invalidCall;
  private final int branchExecutionLimit;
  private final boolean isLoggingEnabled;
  private final String originFormat;
  private final String alignFormat;

  /**
   * Constructs an Executor object.
   * 
   * @param context Generation engine context.
   * 
   * @throws IllegalArgumentException if the argument is {@code null}.
   */
  public Executor(final EngineContext context) {
    InvariantChecks.checkNotNull(context);

    this.context = context;
    this.listener = null;

    this.exceptionCall = EngineUtils.makeSpecialConcreteCall(context, "exception");
    this.invalidCall = EngineUtils.makeSpecialConcreteCall(context, "invalid_instruction");
    this.branchExecutionLimit = context.getOptions().getValueAsInteger(Option.BRANCH_LIMIT);
    this.isLoggingEnabled = context.getOptions().getValueAsBoolean(Option.VERBOSE);
    this.originFormat = context.getOptions().getValueAsString(Option.ORIGIN_FORMAT);
    this.alignFormat = context.getOptions().getValueAsString(Option.ALIGN_FORMAT);
  }

  public void setListener(final Listener listener) {
    this.listener = listener;
  }

  private ProcessingElement getStateObserver() {
    return context.getModel().getPE();
  }

  private LocationAccessor getPC() throws ConfigurationException {
    return getStateObserver().accessLocation("PC");
  }

  /**
   * Executes the specified sequence of instruction calls (concrete calls) and prints information
   * about important events to the simulator log.
   * 
   * @throws IllegalArgumentException if the parameter is {@code null}.
   * @throws GenerationAbortedException if during the interaction with the microprocessor model
   *         an error caused by an invalid format of the request has occurred (typically, it
   *         happens when evaluating an {@link Output} object causes an invalid request to the
   *         model state observer).
   */
  public void execute(
      final ExecutorCode executorCode,
      final int startIndex,
      final int endIndex) {
    InvariantChecks.checkNotNull(executorCode);
    InvariantChecks.checkTrue(startIndex <= endIndex);
    InvariantChecks.checkTrue(executorCode.isInBounds(startIndex));
    InvariantChecks.checkTrue(executorCode.isInBounds(endIndex));

    if (context.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
      Logger.debug("Simulation is disabled");
      return;
    }

    context.getStatistics().pushActivity(Statistics.Activity.SIMULATING);

    try {
      for (int index = 0; index < context.getModel().getPENumber(); index++) {
        Logger.debugHeader("Instance %d", index);
        context.getModel().setActivePE(index);
        executeCalls(executorCode, startIndex, endIndex);
      }
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException("Simulation failed", e);
    } finally {
      context.getStatistics().popActivity();
    }
  }

  private void executeCalls(
      final ExecutorCode code,
      final int startIndex,
      final int endIndex) throws ConfigurationException {
    final LabelTracker labelTracker = new LabelTracker(context.getDelaySlotSize());

    int index = startIndex;
    boolean isInvalidNeverCalled = true;

    while (code.isInBounds(index) || (null != invalidCall && isInvalidNeverCalled)) {
      final ConcreteCall call = code.isInBounds(index) ? code.getCall(index) : invalidCall;
      isInvalidNeverCalled = isInvalidNeverCalled && (call != invalidCall);

      if (call != invalidCall) {
        final long address = call.getAddress();
        getPC().setValue(BigInteger.valueOf(address));
      }

      checkExecutionCount(call);
      logCall(call);
      labelTracker.track(call);

      if (!call.isExecutable()) {
        if (index == endIndex) break;
        index++;
        continue;
      }

      final String exception = executeCall(call);
      if (null == exception) {
        // NORMAL EXECUTION

        final long address = getPC().getValue().longValue();
        final boolean isJump = address != call.getAddress() + call.getByteSize();

        if (!isJump) {
          // If there are no transfers, continue to the next instruction if there is such.
          if (index == endIndex) break;
          index = getNextCallIndex(code, index, null != invalidCall); 
          continue;
        }

        final LabelReference reference = labelTracker.getLabel();
        // Resets labels to jump (they are no longer needed after being used).
        labelTracker.reset();

        if (null != reference) {
          final LabelManager.Target target = reference.getTarget();
          if (null != target && code.hasAddress(target.getAddress())) {
            final long nextAddress = target.getAddress();
            index = code.getCallIndex(nextAddress);
            Logger.debug("Jump to label %s: 0x%x", target.getLabel().getUniqueName(), nextAddress);
            continue;
          }

          throw new GenerationAbortedException(String.format(
              "Label '%s' passed to '%s' (0x%x) is not defined or%n" +
              "is not accessible in the scope of the current test sequence.",
              reference.getReference().getName(), call.getText(), call.getAddress()));
        }

        // If no label references are found within the delay slot we try to use PC to jump
        index = getCallIndex(code, getPC().getValue().longValue(), null != invalidCall);
      } else {
        // EXCEPTION IS DETECTED

        // Resets labels to jump (they are no longer needed after being used).
        labelTracker.reset();

        if (null != exceptionCall) { // op exception is defined and must do all dispatching job
          exceptionCall.execute(context.getModel().getPE());
          index = getCallIndex(code, getPC().getValue().longValue(), null != invalidCall);
        } else {
          if (code.hasHandler(exception)) {
            final long handlerAddress = code.getHandlerAddress(exception);
            final int handlerIndex = code.getCallIndex(handlerAddress);

            Logger.debug("Jump to exception handler for %s: 0x%x", exception, handlerAddress);
            index = handlerIndex;
          } else if (call == invalidCall) {
            Logger.error(
                "Exception handler for %s is not found. Execution will be terminated.", exception);
          } else {
            Logger.error(
                "Exception handler for %s is not found. Have to continue to the next instruction.",
                exception);
            if (index == endIndex) break;
            index = getNextCallIndex(code, index, null != invalidCall);
          }
        }
      }
    }
  }

  private void checkExecutionCount(final ConcreteCall call) {
    if (branchExecutionLimit > 0 && call.getExecutionCount() >= branchExecutionLimit) {
      throw new GenerationAbortedException(String.format(
          "Instruction %s reached its limit on execution count (%d). " +
          "Probably, the program entered an endless loop. Generation was aborted.",
          call.getText(), branchExecutionLimit
          ));
    }
  }

  private static int getNextCallIndex(
      final ExecutorCode code,
      final int currentIndex,
      final boolean isInvalidCallHandled) {
    InvariantChecks.checkNotNull(code);
    InvariantChecks.checkGreaterOrEqZero(currentIndex);

    final ConcreteCall currentCall = code.getCall(currentIndex);
    final long nextAddress = currentCall.getAddress() + currentCall.getByteSize();

    final int nextIndex = currentIndex + 1;
    final ConcreteCall nextCall = code.getCall(nextIndex);

    if (nextCall.getAddress() == nextAddress) {
      return nextIndex;
    }

    // If the next call is aligned, it is not a problem.
    if (null != nextCall.getAlignment() && null == nextCall.getOrigin()) {
      return nextIndex;
    }

    if (!isInvalidCallHandled) {
      throw new GenerationAbortedException(String.format(
          "Simulation error. There is no executable code at 0x%x", nextAddress));
    }

    return -1;
  }

  private static int getCallIndex(
      final ExecutorCode code,
      final long address,
      final boolean isInvalidCallHandled) {
    InvariantChecks.checkNotNull(code);

    if (code.hasAddress(address)) {
      Logger.debug("Jump to address: 0x%x", address);
      return code.getCallIndex(address);
    }

    if (!isInvalidCallHandled) {
      throw new GenerationAbortedException(String.format(
          "Simulation error. There is no executable code at 0x%x", address));
    }

    return -1;
  }

  private String executeCall(final ConcreteCall call) {
    if (null != listener) {
      listener.onBeforeExecute(context, call);
    }

    if (invalidCall != call) {
      context.getStatistics().incTraceLength();
      if (Tarmac.isEnabled()) {
        Tarmac.addRecord(Record.newInstruction(call));
      }
    }

    Tarmac.setEnabled(true);
    final String exception = call.execute(context.getModel().getPE());
    Tarmac.setEnabled(false);

    if (null != listener) {
      listener.onAfterExecute(context, call);
    }

    return exception;
  }

  private void logCall(final ConcreteCall call) throws ConfigurationException {
    if(!isLoggingEnabled) {
      return;
    }

    if (null != call.getOrigin()) {
      Logger.debug(originFormat, call.getOrigin());
    }

    if (null != call.getAlignment()) {
      Logger.debug(alignFormat, call.getAlignment());
    }

    for (final Output output : call.getOutputs()) {
      if (output.isRuntime()) {
        Logger.debug(output.evaluate(getStateObserver()));
      }
    }

    for (final Label label : call.getLabels()) {
      Logger.debug(label.getUniqueName() + ":");
    }

    if (invalidCall != call && null != call.getText()) {
      Logger.debug("0x%016x %s", call.getAddress(), call.getText());
    }
  }
}
