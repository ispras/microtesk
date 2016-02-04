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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.model.api.tarmac.Record;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Output;

/**
 * The role of the Executor class is to execute (simulate) sequences of instruction calls (concrete
 * calls). It executes instruction by instruction, perform control transfers by labels (if needed)
 * and prints information about important events to the simulator log (currently, the console).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

final class Executor {
  private final EngineContext context;
  private final IModelStateObserver observer;

  private Map<String, List<ConcreteCall>> exceptionHandlers;

  /**
   * Constructs an Executor object.
   * 
   * @param observer Model state observer to evaluate simulation-time outputs.
   * @param logExecution Specifies whether printing to the simulator log is enabled.
   * 
   * @throws IllegalArgumentException if the {@code observer} parameter is {@code null}.
   */

  public Executor(
      final EngineContext context,
      final IModelStateObserver observer) {
    checkNotNull(context);
    checkNotNull(observer);

    this.context = context;
    this.observer = observer;

    this.exceptionHandlers = null;
  }

  public void setExceptionHandlers(final Map<String, List<ConcreteCall>> handlers) {
    checkNotNull(handlers);
    this.exceptionHandlers = handlers;
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
      final TestSequence sequence, final int sequenceIndex) throws ConfigurationException {
    Memory.setUseTempCopies(false);

    final List<ConcreteCall> calls = new ArrayList<>();
    final Map<Long, Integer> addressMap = new LinkedHashMap<>();
    final LabelManager labelManager = new LabelManager();

    registerCalls(calls, addressMap, labelManager, sequence.getPrologue(), sequenceIndex);
    registerCalls(calls, addressMap, labelManager, sequence.getBody(), sequenceIndex);

    if (calls.isEmpty()) {
      return;
    }

    final int startIndex = 0;
    final int endIndex = calls.size() - 1;

    final Map<String, Long> exceptionHandlerAddresses = new HashMap<>();
    registerExceptionHandlers(calls, labelManager, addressMap, exceptionHandlers, exceptionHandlerAddresses);

    patchLabels(calls, labelManager, addressMap);

    List<LabelReference> labelRefs = null;
    int labelRefsIndex = 0;

    int index = startIndex;

    final long startAdress = calls.get(startIndex).getAddress();
    observer.accessLocation("PC").setValue(BigInteger.valueOf(startAdress));

    final int branchExecutionLimit = TestSettings.getBranchExecutionLimit();
    while (index >= 0 && index < calls.size()) {
      final ConcreteCall call = calls.get(index);

      if (branchExecutionLimit > 0 && call.getExecutionCount() >= branchExecutionLimit) {
        throw new GenerationAbortedException(String.format(
            "Instruction %s reached its limit on execution count (%d). " +
            "Probably, the program entered an endless loop. Generation was aborted.",
            call.getText(), branchExecutionLimit));
      }

      if (call.getOrigin() != null) {
        logText(String.format(TestSettings.getOriginFormat(), call.getOrigin()));
      }

      if (call.getAlignment() != null) {
        logText(String.format(TestSettings.getAlignFormat(), call.getAlignment()));
      }

      logOutputs(call.getOutputs());
      logLabels(call.getLabels());

      if (null != call.getText()) {
        logText(String.format("0x%016x %s", call.getAddress(), call.getText()));
      }

      if (!call.isExecutable()) {
        if (index == endIndex) break;
        index++;
        continue;
      }

      if (labelRefs != null) {
        final int delta = index - labelRefsIndex;
        if ((delta < 0) || (delta > context.getDelaySlotSize())) {
          labelRefs = null;
          labelRefsIndex = 0;
        }
      }

      if (!call.getLabelReferences().isEmpty()) {
        labelRefs = call.getLabelReferences();
        labelRefsIndex = index;
      }

      Tarmac.setEnabled(true);
      logCall(call);

      final String exception = call.execute();
      Tarmac.setEnabled(false);

      if (null == exception) {
        // NORMAL EXECUTION
        if (index == endIndex) break;

        final int transferStatus = observer.getControlTransferStatus();
        if (0 == transferStatus) {
          // If there are no transfers, continue to the next instruction.
          index++;
          continue;
        }

        if (null != labelRefs && !labelRefs.isEmpty()) {
          final LabelReference reference = labelRefs.get(0);
          final LabelReference.Target target = reference.getTarget();

          // Resets labels to jump (they are no longer needed after being used).
          labelRefs = null;

          if (null != target) {
            index = target.getPosition();
            final long nextAddress = calls.get(index).getAddress();
            logText(String.format("Jump to label %s: 0x%x", target.getLabel().getUniqueName(), nextAddress));
            observer.accessLocation("PC").setValue(BigInteger.valueOf(nextAddress));
            continue;
          }

          throw new GenerationAbortedException(String.format(
              "Label '%s' passed to '%s' (0x%x) is not defined or%n" +
              "is not accessible in the scope of the current test sequence.",
              reference.getReference().getName(), call.getText(), call.getAddress()));
        }

        // If no label references are found within the delay slot we try to use PC to jump
        final long nextAddress = observer.accessLocation("PC").getValue().longValue();
        if (!addressMap.containsKey(nextAddress)) {
          throw new GenerationAbortedException(String.format(
              "Simulation error. There is no executable code at 0x%x", nextAddress));
        }

        logText(String.format("Jump to address: 0x%x", nextAddress));

        final int nextIndex = addressMap.get(nextAddress);
        index = nextIndex;
      } else {
        // EXCEPTION IS DETECTED

        // Resets labels to jump (they are no longer needed after being used).
        labelRefs = null;

        if (exceptionHandlerAddresses.containsKey(exception)) {
          final long handlerAddress = exceptionHandlerAddresses.get(exception);
          final int handlerIndex = addressMap.get(handlerAddress);

          logText(String.format("Jump to exception handler for %s: 0x%x", exception, handlerAddress));
          index = handlerIndex;
        } else {
          Logger.error("Exception handler for %s is not found. " + MSG_HAVE_TO_CONTINUE, exception);
          if (index == endIndex) break;
          index++;
        }
      }
    }

/*
    final List<ConcreteCall> prologue = sequence.getPrologue();
    if (!prologue.isEmpty()) {
      logText("Initialization:\r\n");
      executeSequence(sequence.getPrologue(), Label.NO_SEQUENCE_INDEX);
      logText("\r\nMain Code:\r\n");
    }

    executeSequence(sequence.getBody(), sequenceIndex);*/
  }

  private void logCall(final ConcreteCall call) {
    if (!call.isExecutable()) {
      return;
    }

    TestEngine.STATISTICS.instructionExecutedCount++;
    if (Tarmac.isEnabled()) {
      Tarmac.addRecord(Record.newInstruction(call));
    }
  }

  private static void registerCalls(
      final List<ConcreteCall> calls,
      final Map<Long, Integer> addressMap,
      final LabelManager labelManager,
      final List<ConcreteCall> sequence,
      final int sequenceIndex) {
    for (final ConcreteCall call : sequence) {
      calls.add(call);

      final long address = call.getAddress();
      final int index = calls.size() - 1;

      if (addressMap.containsKey(address)) {
        final int conflictIndex = addressMap.get(address);
        final ConcreteCall conflictCall = calls.get(conflictIndex);

        // Several calls can be mapped to the same address when a pseudo call with
        // zero size (label, compiler directive, text output etc.) precedes
        // an executable (non-zero size) call. This is considered normal. When such
        // a situation is detected, address mapping is not changed in order to allow
        // jumping on the beginning of calls mapped to the same address.

        boolean isConflictLegal = false;

        // Tests whether all calls mapped to the given address and preceding the current
        // call (ending with the conflict call) are zero-size. If the condition holds,
        // this is considered legal.
        for (int testIndex = index - 1; testIndex >= 0; --testIndex) {
          final ConcreteCall testCall = calls.get(testIndex);

          if (testCall.getByteSize() > 0 || testCall.getAddress() != address) {
            break;
          }

          if (testIndex == conflictIndex) {
            isConflictLegal = true;
            break;
          }
        }

        if (!isConflictLegal) {
          Logger.error(
              "Mapping '%s' (index %d): Address 0x%x is already used by '%s' (index %d).",
              call.getText(), index, address, conflictCall.getText(), conflictIndex);
        }
      } else {
        addressMap.put(address, index);
      }

      labelManager.addAllLabels(call.getLabels(), address, sequenceIndex);
    }
  }

  private static void registerExceptionHandlers(
      final List<ConcreteCall> calls,
      final LabelManager labelManager,
      final Map<Long, Integer> addressMap,
      final Map<String, List<ConcreteCall>> exceptionHandlers,
      final Map<String, Long> exceptionHandlerAddresses) {
    if (exceptionHandlers != null) {
      final Set<Object> handlerSet = new HashSet<>();
      for (final Map.Entry<String, List<ConcreteCall>> e : exceptionHandlers.entrySet()) {
        final String handlerName = e.getKey();
        final List<ConcreteCall> handlerCalls = e.getValue();
 
        if (handlerCalls.isEmpty()) {
          Logger.warning("Empty exception handler: %s", handlerName);
          continue;
        }

        if (handlerSet.contains(handlerCalls)) {
          continue;
        }

        exceptionHandlerAddresses.put(handlerName, handlerCalls.get(0).getAddress());
        registerCalls(calls, addressMap, labelManager, handlerCalls, Label.NO_SEQUENCE_INDEX);

        handlerSet.add(handlerCalls);
      }
    }
  }

  private static void patchLabels(
      final List<ConcreteCall> calls,
      final LabelManager labelManager,
      final Map<Long, Integer> addressMap) {
    // Resolves all label references and patches the instruction call text accordingly.
    for (final ConcreteCall call : calls) {
      for (final LabelReference labelRef : call.getLabelReferences()) {
        labelRef.resetTarget();

        final Label source = labelRef.getReference();
        final LabelManager.Target target = labelManager.resolve(source);

        final String uniqueName;
        final String searchPattern;
        final String patchedText;

        if (null != target) {
          // For code labels
          uniqueName = target.getLabel().getUniqueName();
          final long address = target.getAddress();

          final int index = addressMap.get(address);
          labelRef.setTarget(target.getLabel(), index);

          searchPattern = "<label>0";
          patchedText = call.getText().replace(searchPattern, uniqueName);

          labelRef.getPatcher().setValue(BigInteger.valueOf(address));
        } else {
          if (null != labelRef.getArgumentValue()) {
            // For data labels 
            uniqueName = source.getName();
            searchPattern = String.format("<label>%d", labelRef.getArgumentValue());
          } else {
            // For unrecognized labels
            uniqueName = source.getName();
            searchPattern = "<label>0";

            /*
            throw new GenerationAbortedException(String.format(
                "Label '%s' passed to '%s' (0x%x) is not defined or%n" +
                "is not accessible in the scope of the current test sequence.",
                source.getName(), call.getText(), call.getAddress()));
            */
          }

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
    checkNotNull(outputs);

    for (final Output output : outputs) {
      if (output.isRuntime()) {
        logText(output.evaluate(observer));
      }
    }
  }
  
  private void logLabels(final List<Label> labels) {
    checkNotNull(labels);
    for (final Label label : labels) {
      logText(label.getUniqueName() + ":");
    }
  }

  /**
   * Prints the text to the simulator log if logging is enabled.
   * 
   * @param text Text to be printed.
   */

  private void logText(final String text) {
    if (text != null) {
      Logger.debug(text);
    }
  }

  private static final String MSG_HAVE_TO_CONTINUE =
      "Have to continue to the next instruction.";
}
