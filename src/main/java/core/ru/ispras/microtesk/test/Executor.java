/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.List;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Output;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.utils.PrintingUtils;

/**
 * The role of the Executor class is to execute (simulate) sequences of instruction calls (concrete
 * calls). It executes instruction by instruction, perform control transfers by labels (if needed)
 * and prints information about important events to the simulator log (currently, the console).
 * 
 * @author Andrei Tatarnikov
 */

final class Executor {
  private final IModelStateObserver observer;
  private final boolean logExecution;
  private final int executionLimit;

  private List<LabelReference> labelRefs;

  /**
   * Constructs an Executor object.
   * 
   * @param observer Model state observer to evaluate simulation-time outputs.
   * @param logExecution Specifies whether printing to the simulator log is enabled.
   * 
   * @throws IllegalArgumentException if the {@code observer} parameter is {@code null}.
   */

  public Executor(
      final IModelStateObserver observer,
      final boolean logExecution,
      final int executionLimit) {
    checkNotNull(observer);

    this.observer = observer;
    this.logExecution = logExecution;
    this.executionLimit = executionLimit;
    this.labelRefs = null;
  }

  /**
   * Executes the specified sequence of instruction calls (concrete calls) and prints information
   * about important events to the simulator log.
   * 
   * @param sequence Sequence of executable (concrete) instruction calls.
   * 
   * @throws IllegalArgumentException if the parameter is {@code null}.
   * @throws ConfigurationException if during the interaction with the microprocessor model an error
   *         caused by an invalid format of the request has occurred (typically, it happens when
   *         evaluating an {@link Output} object causes an invalid request to the model state
   *         observer).
   */

  public void executeSequence(
      final TestSequence sequence) throws ConfigurationException {
    final List<ConcreteCall> prologue = sequence.getPrologue();
    if (!prologue.isEmpty()) {
      logText("Initialization:\r\n");
      executeSequence(sequence.getPrologue());
      logText("\r\nMain Code:\r\n");
    }

    executeSequence(sequence.getBody());
  }

  private void executeSequence(
      final List<ConcreteCall> sequence) throws ConfigurationException {
    checkNotNull(sequence);

    // Remembers all labels defined by the sequence and its positions.
    final LabelManager labelManager = new LabelManager();
    for (int index = 0; index < sequence.size(); ++index) {
      final ConcreteCall call = sequence.get(index);
      labelManager.addAllLabels(call.getLabels(), index);
    }

    // Resolves all label references and patches the instruction call text accordingly.
    for (int index = 0; index < sequence.size(); ++index) {
      final ConcreteCall call = sequence.get(index);
      call.resetText();

      for (LabelReference labelRef : call.getLabelReferences()) {
        labelRef.resetTarget();

        final Label source = labelRef.getReference();
        final LabelManager.Target target = labelManager.resolve(source);

        final String uniqueName;

        if (null != target) {
          uniqueName = target.getLabel().getUniqueName();
          labelRef.setTarget(target.getLabel(), target.getPosition());
        } else {
          // For data labels 
          uniqueName = source.getName();
        }

        // TODO: TEMPORARY IMPLEMENTATION
        final String searchPattern = 
            String.format("<label>%d", labelRef.getArgumentValue());
        final String patchedText = 
            call.getText().replace(searchPattern, uniqueName);

        call.setText(patchedText);
      }

      // Kill all unused "<label>" markers.
      if (null != call.getText()) {
        call.setText(call.getText().replace("<label>", ""));
      }
    }

    int currentPos = 0;
    final int endPos = sequence.size();

    while (currentPos < endPos) {
      final ConcreteCall call = sequence.get(currentPos);

      if (executionLimit > 0 && call.getExecutionCount() >= executionLimit) {
        throw new GenerationAbortedException(String.format(
            "Instruction %s reached its limit on execution count (%d). " +
            "Probably, the program entered an endless loop. Generation was aborted.",
            call.getText(), executionLimit));
      }

      currentPos = executeCall(call, currentPos);
    }
  }

  /**
   * Executes the specified instruction call (concrete call) and returns the position of the next
   * instruction call to be executed. Also, it prints the textual representation of the call and
   * debugging outputs linked to the call to the simulator log (if logging is enabled). If the
   * method fails to deal with a control transfer in a proper way it prints a warning message and
   * returns the position of the instruction call that immediately follows the current one.
   * 
   * @param call Instruction call to be executed.
   * @param currentPos Position of the current call.
   * @return Position of the next instruction call to be executed.
   * 
   * @throws ConfigurationException if failed to evaluate an Output object associated with the
   *         instruction call.
   */

  private int executeCall(final ConcreteCall call, final int currentPos)
      throws ConfigurationException {
    logOutputs(call.getOutputs());

    // If the call is not executable (contains only attributes like
    // labels or outputs, but no "body"), continue to the next instruction.
    if (!call.isExecutable()) {
      return currentPos + 1;
    }

    logText(call.getText());
    call.execute();

    // Saves labels to jump in case there is a branch delay slot.
    if (!call.getLabelReferences().isEmpty()) {
      labelRefs = call.getLabelReferences();
    }

    // TODO: Support instructions with 2+ labels (needs API)
    final int transferStatus = observer.getControlTransferStatus();

    // If there are no transfers, continue to the next instruction.
    if (0 == transferStatus) {
      return currentPos + 1;
    }

    if ((null == labelRefs) || labelRefs.isEmpty()) {
      logText(MSG_NO_LABEL_LINKED);
      return currentPos + 1;
    }

    final LabelReference reference = labelRefs.get(0);
    final LabelReference.Target target = reference.getTarget();

    // Resets labels to jump (they are no longer needed after being used).
    labelRefs = null;

    if (null == target) {
      logText(String.format(MSG_NO_LABEL_DEFINED, reference.getReference().getName()));
      return currentPos + 1;
    }

    logText("Jump to label: " + target.getLabel().getUniqueName());
    return target.getPosition();
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
    checkNotNull(outputs);

    for (final Output output : outputs) {
      if (output.isRuntime()) {
        logText(output.evaluate(observer));
      }
    }
  }

  /**
   * Prints the text to the simulator log if logging is enabled.
   * 
   * @param text Text to be printed.
   */

  public void logText(final String text) {
    if (logExecution && text != null) {
      PrintingUtils.trace(text);
    }
  }

  private static final String MSG_HAVE_TO_CONTINUE =
    "Have to continue to the next instruction.";

  private static final String MSG_NO_LABEL_LINKED =
    "Warning: No label to jump is linked to the current instruction. " + MSG_HAVE_TO_CONTINUE;

  private static final String MSG_NO_LABEL_DEFINED =
    "Warning: No label called %s is defined in the current sequence. " + MSG_HAVE_TO_CONTINUE;
}
