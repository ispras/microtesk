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
 * The role of the {@link Executor} class is to execute (simulate) sequences of instruction calls
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
    public static enum Kind {
      BREAK_POINT,
      UNDEFINED_LABEL,
    }

    private final Kind kind;
    private final Long address;
    private final LabelReference labelReference;

    protected static Status newBreakPoint(final long address) {
      return new Status(Kind.BREAK_POINT, address, null);
    }

    protected static Status newUndefinedLabel(final LabelReference labelReference) {
      InvariantChecks.checkNotNull(labelReference);
      return new Status(Kind.UNDEFINED_LABEL, null, labelReference);
    }

    private Status(
        final Kind kind,
        final Long address,
        final LabelReference labelReference) {
      this.kind = kind;
      this.address = address;
      this.labelReference = labelReference;
    }

    public Kind getKind() {
      return kind;
    }

    public final long getAddress() {
      InvariantChecks.checkNotNull(address);
      return address;
    }

    public LabelReference getLabelReference() {
      InvariantChecks.checkNotNull(labelReference);
      return labelReference;
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

  private final class Fetcher {
    private final Code code;
    private final long startAddress;
    private long address;
    private Code.Iterator iterator;

    private Fetcher(final Code code, final long address) {
      InvariantChecks.checkNotNull(code);

      this.code = code;
      this.startAddress = address;
      this.address = address;
      this.iterator = code.getIterator(address, true);
    }

    public boolean canFetch() {
      return null != fetch();
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
      if (!code.isBreakAddress(address)) {
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
      InvariantChecks.checkNotNull(iterator);

      final ConcreteCall current = iterator.current();
      InvariantChecks.checkNotNull(current); 

      iterator.next();
      final ConcreteCall next = iterator.current();

      address = null != next ? next.getAddress() : address + current.getByteSize();
    }

    public void jump(final long jumpAddress) {
      address = jumpAddress;
      iterator = code.hasAddress(jumpAddress) ? code.getIterator(jumpAddress, false) : null;
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
      final Code executorCode,
      final long startAddress,
      final long endAddress) {
    InvariantChecks.checkNotNull(executorCode);

    if (context.getOptions().getValueAsBoolean(Option.NO_SIMULATION)) {
      Logger.debug("Simulation is disabled");
      return;
    }

    context.getStatistics().pushActivity(Statistics.Activity.SIMULATING);
    try {
      long address = startAddress;
      long previousAddress = startAddress;
      do {
        previousAddress = address;
        final Status status = execute(executorCode, address);

        if (Status.Kind.UNDEFINED_LABEL == status.getKind()) {
          throw new GenerationAbortedException(String.format(
              "Label '%s' is undefined or unavailable in the current execution scope.",
              status.getLabelReference().getReference().getName()));
        }

        address = status.getAddress();
      } while (address != endAddress && address != previousAddress);
    } catch (final ConfigurationException e) {
      throw new GenerationAbortedException("Simulation failed", e);
    } finally {
      context.getStatistics().popActivity();
    }
  }

  private Status execute(final Code code, final long startAddress) throws ConfigurationException {
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
          return Status.newUndefinedLabel(reference);
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

    return Status.newBreakPoint(fetcher.getAddress());
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
