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

  private final EngineContext context;
  private Listener listener;

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
   * @param executorCode Execution context that contains all code of the current test program.
   * @param sequenceCode Sequence of executable (concrete) instruction calls.
   * @param sequenceIndex Sequence index.
   * @param abortOnUndefinedLabel Aborts generation when a reference to an undefined label is
   *        detected. This is the default behavior, which can changed in special cases (e.g.
   *        self-checks that contain references to labels defined in epilogue).
   * 
   * @throws IllegalArgumentException if the parameter is {@code null}.
   * @throws GenerationAbortedException if during the interaction with the microprocessor model
   *         an error caused by an invalid format of the request has occurred (typically, it
   *         happens when evaluating an {@link Output} object causes an invalid request to the
   *         model state observer).
   */
  public void execute(
      final ExecutorCode executorCode,
      final List<ConcreteCall> sequenceCode,
      final int sequenceIndex,
      final boolean abortOnUndefinedLabel) {
    try {
      InvariantChecks.checkNotNull(executorCode);
      InvariantChecks.checkNotNull(sequenceCode);
      executeSequence(executorCode, sequenceCode, sequenceIndex, abortOnUndefinedLabel);
    } catch (final ConfigurationException e) {
      final java.io.StringWriter writer = new java.io.StringWriter();
      e.printStackTrace(new java.io.PrintWriter(writer));
      throw new GenerationAbortedException(String.format(
          "Simulation failed: %s%n%s", e.getMessage(), writer));
    }
  }

  private void executeSequence(
      final ExecutorCode executorCode,
      final List<ConcreteCall> sequence,
      final int sequenceIndex,
      final boolean abortOnUndefinedLabel) throws ConfigurationException {
    if (sequence.isEmpty()) {
      return;
    }

    final int startIndex = executorCode.getCallCount();
    executorCode.addCalls(sequence);
    final int endIndex = executorCode.getCallCount() - 1;

    // TODO: patch labels in exception handler code
    // (need refactoring to have it in separate collection)
    patchLabels(
        context.getLabelManager(),
        executorCode,
        startIndex,
        endIndex,
        sequenceIndex,
        abortOnUndefinedLabel
        );

    if (context.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
      logText("Simulation is disabled");
      return;
    }

    context.getStatistics().pushActivity(Statistics.Activity.SIMULATING);

    for (int index = 0; index < context.getModel().getPENumber(); index++) {
      Logger.debugHeader("Instance %d", index);
      context.getModel().setActivePE(index);
      executeCalls(executorCode, startIndex, endIndex);
    }

    context.getStatistics().popActivity();
  }

  private void executeCalls(
      final ExecutorCode code,
      final int startIndex,
      final int endIndex) throws ConfigurationException {
    List<LabelReference> labelRefs = null;
    int labelRefsIndex = 0;
    int index = startIndex;

    // Number of non-executable instructions between labelRefsIndex and index (in delay slot)
    int nonExecutableCount = 0; 

    final int branchExecutionLimit =
        context.getOptions().getValueAsInteger(Option.BRANCH_LIMIT);

    final ConcreteCall invalidCall =
        EngineUtils.makeSpecialConcreteCall(context, "invalid_instruction");

    final ConcreteCall exceptionCall =
        EngineUtils.makeSpecialConcreteCall(context, "exception");

    while (code.isInBounds(index) ||
           null != invalidCall && invalidCall.getExecutionCount() == 0) {

      final ConcreteCall call =
          code.isInBounds(index) ? code.getCall(index) : invalidCall;

      if (call != invalidCall) {
        final long address = call.getAddress();
        getPC().setValue(BigInteger.valueOf(address));
      }

      if (branchExecutionLimit > 0 && call.getExecutionCount() >= branchExecutionLimit) {
        throw new GenerationAbortedException(String.format(
            "Instruction %s reached its limit on execution count (%d). " +
            "Probably, the program entered an endless loop. Generation was aborted.",
            call.getText(), branchExecutionLimit));
      }

      if (call.getOrigin() != null) {
        logText(String.format(
            context.getOptions().getValueAsString(Option.ORIGIN_FORMAT), call.getOrigin()));
      }

      if (call.getAlignment() != null) {
        logText(String.format(
            context.getOptions().getValueAsString(Option.ALIGN_FORMAT), call.getAlignment()));
      }

      logOutputs(call.getOutputs());
      logLabels(call.getLabels());

      if (invalidCall != call && null != call.getText()) {
        logText(String.format("0x%016x %s", call.getAddress(), call.getText()));
      }

      if (!call.isExecutable()) {
        if (index == endIndex) break;
        index++;
        nonExecutableCount++;
        continue;
      }

      if (labelRefs != null) {
        // nonExecutableCount is excluded from number of instructions presumably in delay slot
        final int delta = index - labelRefsIndex - nonExecutableCount;
        if ((delta < 0) || (delta > context.getDelaySlotSize())) {
          labelRefs = null;
          labelRefsIndex = 0;
          nonExecutableCount = 0;
        }
      }

      if (!call.getLabelReferences().isEmpty()) {
        labelRefs = call.getLabelReferences();
        labelRefsIndex = index;
        nonExecutableCount = 0;
      }

      Tarmac.setEnabled(true);
      if (invalidCall != call) {
        logCall(call);
      }

      notifyBeforeExecute(call);
      final String exception = call.execute(context.getModel().getPE());
      notifyAfterExecute(call);
      Tarmac.setEnabled(false);

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

        if (null != labelRefs && !labelRefs.isEmpty()) {
          final LabelReference reference = labelRefs.get(0);
          final LabelManager.Target target = reference.getTarget();

          // Resets labels to jump (they are no longer needed after being used).
          labelRefs = null;

          if (null != target && code.hasAddress(target.getAddress())) {
            final long nextAddress = target.getAddress();
            index = code.getCallIndex(nextAddress);
            logText(String.format("Jump to label %s: 0x%x", target.getLabel().getUniqueName(), nextAddress));
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
        labelRefs = null;

        if (null != exceptionCall) { // op exception is defined and must do all dispatching job
          exceptionCall.execute(context.getModel().getPE());
          index = getCallIndex(code, getPC().getValue().longValue(), null != invalidCall);
        } else {
          if (code.hasHandler(exception)) {
            final long handlerAddress = code.getHandlerAddress(exception);
            final int handlerIndex = code.getCallIndex(handlerAddress);

            logText(String.format("Jump to exception handler for %s: 0x%x", exception, handlerAddress));
            index = handlerIndex;
          } else if (call == invalidCall) {
            Logger.error(
                "Exception handler for %s is not found. Execution will be terminated.", exception);
          } else {
            Logger.error("Exception handler for %s is not found. " + MSG_HAVE_TO_CONTINUE, exception);
            if (index == endIndex) break;
            index = getNextCallIndex(code, index, null != invalidCall);
          }
        }
      }
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
      logText(String.format("Jump to address: 0x%x", address));
      return code.getCallIndex(address);
    }

    if (!isInvalidCallHandled) {
      throw new GenerationAbortedException(String.format(
          "Simulation error. There is no executable code at 0x%x", address));
    }

    return -1;
  }

  private void logCall(final ConcreteCall call) {
    if (!call.isExecutable()) {
      return;
    }

    context.getStatistics().incTraceLength();
    if (Tarmac.isEnabled()) {
      Tarmac.addRecord(Record.newInstruction(call));
    }
  }

  private static void patchLabels(
      final LabelManager labelManager,
      final ExecutorCode code,
      final int startIndex,
      final int endIndex,
      final int sequenceIndex,
      final boolean abortOnUndefined) {
    // Resolves all label references and patches the instruction call text accordingly.
    for (int index = startIndex; index <= endIndex; ++index) {
      final ConcreteCall call = code.getCall(index);

      // Resolves all label references and patches the instruction call text accordingly.
      for (final LabelReference labelRef : call.getLabelReferences()) {
        labelRef.resetTarget();

        final Label source = labelRef.getReference();
        source.setSequenceIndex(sequenceIndex);

        final LabelManager.Target target = labelManager.resolve(source);

        final String uniqueName;
        final String searchPattern;
        final String patchedText;

        if (null != target) { // Label is found
          labelRef.setTarget(target);

          uniqueName = target.getLabel().getUniqueName();
          final long address = target.getAddress();

          if (null != labelRef.getArgumentValue()) {
            searchPattern = String.format("<label>%d", labelRef.getArgumentValue());
          } else {
            labelRef.getPatcher().setValue(BigInteger.ZERO);
            searchPattern = "<label>0";
          }

          patchedText = call.getText().replace(searchPattern, uniqueName);
          labelRef.getPatcher().setValue(BigInteger.valueOf(address));
        } else { // Label is not found
          if (abortOnUndefined) {
            throw new GenerationAbortedException(String.format(
                "Label '%s' passed to '%s' (0x%x) is not defined or%n" +
                "is not accessible in the scope of the current test sequence.",
                source.getName(), call.getText(), call.getAddress()));
          }

          uniqueName = source.getName();
          searchPattern = "<label>0";

          patchedText = call.getText().replace(searchPattern, uniqueName);
        }

        call.setText(patchedText);
      }

      // Kill all unused "<label>" markers.
      if (null != call.getText()) {
        call.setText(call.getText().replace("<label>", ""));
      }
    }
  }

  /**
   * Evaluates and prints the collection of {@link Output} objects.
   * 
   * @param o List of {@link Output} objects.
   * 
   * @throws IllegalArgumentException if the parameter is {@code null}.
   * @throws ConfigurationException if failed to evaluate the information in an Output
   *         object due to an incorrect request to the model state observer.
   */
  private void logOutputs(
      final List<Output> outputs) throws ConfigurationException {
    InvariantChecks.checkNotNull(outputs);

    for (final Output output : outputs) {
      if (output.isRuntime()) {
        logText(output.evaluate(getStateObserver()));
      }
    }
  }

  private void logLabels(final List<Label> labels) {
    InvariantChecks.checkNotNull(labels);
    for (final Label label : labels) {
      logText(label.getUniqueName() + ":");
    }
  }

  /**
   * Prints the text to the simulator log if logging is enabled.
   * 
   * @param text Text to be printed.
   */
  private static void logText(final String text) {
    if (text != null) {
      Logger.debug(text);
    }
  }

  private void notifyAfterExecute(final ConcreteCall call) {
    if (null != listener) {
      listener.onBeforeExecute(context, call);
    }
  }

  private void notifyBeforeExecute(final ConcreteCall call) {
    if (null != listener) {
      listener.onAfterExecute(context, call);
    }
  }

  private static final String MSG_HAVE_TO_CONTINUE =
      "Have to continue to the next instruction.";
}
