/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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
 * The role of the {@link Executor} class is to execute (simulate) instruction calls
 * (concrete calls). It executes instruction by instruction, perform control transfers by labels
 * (if needed) and prints information about important events to the simulator log (currently,
 * the console).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class Executor {
  /**
   * The {@link Status} class describes the execution status. It specifies the point
   * where execution was stopped and the reason why it was stopped.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public static final class Status {
    private final Object data;

    public static Status newAddress(final long address) {
      return new Status(address);
    }

    public static Status newLabelReference(final LabelReference labelReference) {
      return new Status(labelReference);
    }

    private Status(final Object data) {
      InvariantChecks.checkTrue(data instanceof Long || data instanceof LabelReference);
      this.data = data;
    }

    public boolean isAddress() {
      return data instanceof Long;
    }

    public boolean isLabelReference() {
      return data instanceof LabelReference;
    }

    public final long getAddress() {
      InvariantChecks.checkTrue(data instanceof Long);
      return (Long) data;
    }

    public LabelReference getLabelReference() {
      InvariantChecks.checkTrue(data instanceof LabelReference);
      return (LabelReference) data;
    }

    @Override
    public String toString() {
      return isAddress() ? String.format("Address: 0x%016x", getAddress()) : data.toString();
    }
  }

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
        distance++;
        if (distance > delaySlotSize) {
          reset();
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

  private final class Fetcher {
    private final Code code;
    private final long startAddress;
    private final boolean isStartFromUnallocatedAddress;
    private long address;
    private Code.Iterator iterator;
    private boolean isNextAfterNull;

    private Fetcher(final Code code, final long address) {
      InvariantChecks.checkNotNull(code);

      this.code = code;
      this.startAddress = address;
      this.isStartFromUnallocatedAddress = !code.hasAddress(address);
      this.address = address;
      this.iterator = isStartFromUnallocatedAddress ? null : code.getIterator(address, true);
      this.isNextAfterNull = false;
    }

    public boolean canFetch() {
      return null != fetch() && !isNextAfterNull;
    }

    public ConcreteCall fetch() {
      final ConcreteCall call = getCall();
      return null != call ? call : invalidCall;
    }

    private ConcreteCall getCall() {
      return null == iterator ? null : iterator.current();
    }

    public long getAddress() {
      return address;
    }

    public boolean isBreakReached() {
      if (!code.isBreakAddress(address) || isStartFromUnallocatedAddress) {
        return false;
      }

      final ConcreteCall call = getCall();
      if (address == startAddress && null != call) {
        return false;
      }

      // This is done to fetch all non-executable calls.
      return null == call || call.isExecutable();
    }

    public void next() {
      if (null == iterator) {
        isNextAfterNull = true;
        return;
      }

      final ConcreteCall current = iterator.current();
      if (null == current) {
        isNextAfterNull = true;
        return;
      }

      iterator.next();
      final ConcreteCall next = iterator.current();

      address = null != next ? next.getAddress() : address + current.getByteSize();
    }

    public void jump(final long jumpAddress) {
      address = jumpAddress;
      iterator = !code.hasAddress(jumpAddress) ? null : code.getIterator(jumpAddress, false);
      isNextAfterNull = false;
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

  /**
   * Executes code starting from the specified address until (1) a break point is reached
   * and no executable code follows after this point or (2) an attempt to jump to an undefined
   * label is made.
   * 
   * @param code Code to be executed.
   * @param startAddress Start address.
   * @return Execution status (address or label).
   * 
   * @throws GenerationAbortedException (1) if an endless loop is detected; (2) if execution jumped
   *         to an address that holds no executable instructions and no handling is provided for
   *         this situation; (3) if an error related to interaction with the model occurs.
   */
  public Status execute(final Code code, final long startAddress) {
    InvariantChecks.checkNotNull(code);
    InvariantChecks.checkFalse(context.getOptions().getValueAsBoolean(Option.NO_SIMULATION));

    context.getStatistics().pushActivity(Statistics.Activity.SIMULATING);
    try {
      long address = startAddress;
      long previousAddress = startAddress;
      Status status = Status.newAddress(startAddress);

      do {
        previousAddress = address;

        status = executeToBreak(code, address);
        if (!status.isAddress()) {
          return status;
        }

        address = status.getAddress();
      } while (code.hasBlockStartAt(address) && address != previousAddress);

      return status;
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException("Simulation failed", e);
    } finally {
      context.getStatistics().popActivity();
    }
  }

  /**
   * Executes code starting from the specified address until: (1) a break point is reached or
   * (2) an attempt to jump to an undefined label is made.
   * 
   * @param code Code to be executed.
   * @param startAddress Start address.
   * @return Execution status (address or label).
   * 
   * @throws ConfigurationException if an error related to interaction with the model occurs.
   * @throws GenerationAbortedException (1) if an endless loop is detected; (2) if execution
   *         jumped to an address that holds no executable instructions and no handling is provided
   *         for this situation.
   */
  private Status executeToBreak(
      final Code code,
      final long startAddress) throws ConfigurationException {
    final LabelTracker labelTracker = new LabelTracker(context.getDelaySlotSize());
    final Fetcher fetcher = new Fetcher(code, startAddress);

    while (fetcher.canFetch() && !fetcher.isBreakReached()) {
      final ConcreteCall call = fetcher.fetch();
      setPC(fetcher.getAddress());

      logCall(call);
      labelTracker.track(call);

      // NON-EXECUTABLE
      if (!call.isExecutable()) {
        fetcher.next();
        continue;
      }

      final String exception = executeCall(call);

      // EXCEPTION
      if (null != exception) {
        final Long handlerAddress = getExceptionHandlerAddress(code, exception);
        if (null != handlerAddress) {
          labelTracker.reset(); // Resets labels to jump (no longer needed after jump to handler).
          logJump(handlerAddress, null);
          fetcher.jump(handlerAddress);
        } else {
          Logger.error("Exception handler for %s is not found.", exception);
          Logger.message("Execution will be continued from the next instruction.");
          fetcher.next();
        }

        continue;
      }

      final long address = getPC();
      final boolean isJump = address != call.getAddress() + call.getByteSize();

      // NORMAL
      if (!isJump) {
        fetcher.next();
        continue;
      }

      // JUMP
      final LabelReference reference = labelTracker.getLabel();
      labelTracker.reset(); // Resets labels to jump (no longer needed after being used).

      if (null != reference) {
        if (null == reference.getTarget()) {
          return Status.newLabelReference(reference);
        }

        final long labelAddress = reference.getTarget().getAddress();
        logJump(labelAddress, reference.getTarget().getLabel());
        fetcher.jump(labelAddress);
      } else {
        // If no label references are found within the delay slot we try to use PC to jump
        logJump(address, null);
        fetcher.jump(address);
      }
    }

    if (!fetcher.isBreakReached()) {
      throw new GenerationAbortedException(String.format(
          "Simulation error. No executable code at 0x%016x.", fetcher.getAddress()));
    }

    return Status.newAddress(fetcher.getAddress());
  }

  private ProcessingElement getStateObserver() {
    return context.getModel().getPE();
  }

  private LocationAccessor getPCLocation() throws ConfigurationException {
    return getStateObserver().accessLocation("PC");
  }

  private long getPC() throws ConfigurationException {
    return getPCLocation().getValue().longValue();
  }

  private void setPC(final long address) throws ConfigurationException {
    getPCLocation().setValue(BigInteger.valueOf(address));
  }

  private Long getExceptionHandlerAddress(
      final Code code,
      final String exception) throws ConfigurationException {
    InvariantChecks.checkNotNull(code);
    InvariantChecks.checkNotNull(exception);

    if (null != exceptionCall) {
      // op exception is defined and must do all dispatching job
      exceptionCall.execute(context.getModel().getPE());
      return getPC();
    }

    if (code.hasHandler(exception)) {
      return code.getHandlerAddress(exception);
    }

    return null;
  }

  private String executeCall(final ConcreteCall call) {
    if (null != listener) {
      listener.onBeforeExecute(context, call);
    }

    Tarmac.setEnabled(true);
    if (invalidCall != call) {
      context.getStatistics().incTraceLength();
      if (Tarmac.isEnabled()) {
        Tarmac.addRecord(Record.newInstruction(call));
      }
    }

    final String exception = call.execute(context.getModel().getPE());
    Tarmac.setEnabled(false);

    if (null != listener) {
      listener.onAfterExecute(context, call);
    }

    if (invalidCall != call) {
      checkExecutionCount(call);
    }

    return exception;
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

  private void logJump(final long address, final Label label) {
    if(!isLoggingEnabled) {
      return;
    }

    final String addressText = String.format("0x%016x", address);
    final StringBuilder sb = new StringBuilder("Jump to ");

    if (null != label) {
      sb.append(label.getUniqueName());
      sb.append(" at ");
    }

    sb.append(addressText);
    Logger.debug(sb.toString());
  }
}
