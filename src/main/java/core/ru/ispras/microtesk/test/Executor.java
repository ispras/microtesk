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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.ProcessingElement;
import ru.ispras.microtesk.model.memory.LocationAccessor;
import ru.ispras.microtesk.model.tracer.Tracer;
import ru.ispras.microtesk.model.tracer.Record;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineUtils;
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
public final class Executor {
  /**
   * The {@link Status} class describes the execution status. It specifies the point
   * where execution was stopped and the reason why it was stopped.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public static final class Status {
    private final long address;
    private final LabelReference labelReference;

    private Status(final long address, final LabelReference labelReference) {
      this.address = address;
      this.labelReference = labelReference;
    }

    public static Status newAddress(final long address) {
      return new Status(address, null);
    }

    public static Status newUndefinedLabel(final long address, final LabelReference labelReference) {
      InvariantChecks.checkNotNull(labelReference);
      return new Status(address, labelReference);
    }

    public final long getAddress() {
      return address;
    }

    public boolean isLabelReference() {
      return null != labelReference;
    }

    public LabelReference getLabelReference() {
      return labelReference;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(String.format("0x%016x", getAddress()));
      if (isLabelReference()) {
        sb.append(String.format(
            " (waiting for label %s)", labelReference.getReference().getName()));
      }
      return sb.toString();
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
      return (null != getCall() || null != invalidCall) && !isNextAfterNull;
    }

    public ConcreteCall fetch() {
      final ConcreteCall call = getCall();

      if (null != call) {
        return call;
      }

      Logger.debug("Unable to fetch a call. No executable code at 0x%016x.", getAddress());
      if (null != invalidCall) {
        Logger.debug("The 'invalid_instruction' handler will be executed.");
      }

      return invalidCall;
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
  private final LabelManager labelManager;
  private final boolean isPresimulation;
  private boolean isPauseOnUndefinedLabel;
  private boolean isSelfCheckMode;
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
   * @param labelManager Label manager that stores all refined labels.
   * @param isPresimulation Flag that specifies whether the presimulation mode is on.
   *
   * @throws IllegalArgumentException if the argument is {@code null}.
   */
  public Executor(
      final EngineContext context,
      final LabelManager labelManager,
      final boolean isPresimulation) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(labelManager);

    this.context = context;
    this.labelManager = labelManager;
    this.isPresimulation = isPresimulation;
    this.isPauseOnUndefinedLabel = true;
    this.isSelfCheckMode = false; 
    this.listener = null;

    this.exceptionCall = EngineUtils.makeSpecialConcreteCall(context, "exception");
    this.invalidCall = EngineUtils.makeSpecialConcreteCall(context, "invalid_instruction");
    this.branchExecutionLimit = context.getOptions().getValueAsInteger(Option.BRANCH_LIMIT);
    this.isLoggingEnabled = context.getOptions().getValueAsBoolean(Option.VERBOSE);
    this.originFormat = context.getOptions().getValueAsString(Option.ORIGIN_FORMAT);
    this.alignFormat = context.getOptions().getValueAsString(Option.ALIGN_FORMAT);
  }

  public Executor(final EngineContext context) {
    this(context, context.getLabelManager(), false);
  }

  public void setPauseOnUndefinedLabel(final boolean value) {
    this.isPauseOnUndefinedLabel = value;
  }

  public void setSelfCheckMode(final boolean value) {
    this.isSelfCheckMode = value;
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
        if (status.isLabelReference()) {
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
    final Fetcher fetcher = new Fetcher(code, startAddress);

    while (fetcher.canFetch() && !fetcher.isBreakReached()) {
      final ConcreteCall call = fetcher.fetch();
      setPC(fetcher.getAddress());

      // NON-EXECUTABLE
      if (!call.isExecutable()) {
        logCall(call);
        fetcher.next();
        continue;
      }

      for (final LabelReference reference : call.getLabelReferences()) {
        final Label label = reference.getReference();

        LabelManager.Target target = reference.getTarget();
        if (null == target) {
          target = labelManager.resolve(label);
          if (null != target) {
            reference.setTarget(target);
            reference.getPatcher().setValue(BigInteger.valueOf(target.getAddress()));
          }
        }

        if (null == target) {
          if (isPauseOnUndefinedLabel) {
            logReferenceUndefinedLabel(call, label);
            return Status.newUndefinedLabel(fetcher.getAddress(), reference);
          }

          if (!isSelfCheckMode) {
            throw new GenerationAbortedException(String.format(
                "Label %s is undefined or unavailable in the current context.",
                reference.getReference().getName())
                );
          }
        }
      }

      final String exception = executeCall(call);

      // EXCEPTION
      if (null != exception) {
        final Long handlerAddress = getExceptionHandlerAddress(code, exception);
        if (null != handlerAddress) {
          logJumpExceptionHandler(exception, handlerAddress);
          fetcher.jump(handlerAddress);

          if (!fetcher.code.hasAddress(handlerAddress)) {
            logUnallocatedAddress(handlerAddress);
            return Status.newAddress(handlerAddress);
          }
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
      if (address == fetcher.getAddress()) {
        throw new GenerationAbortedException(String.format(
            "Program counter was not updated: 0x%016x.", address));
      }

      // If no label references are found within the delay slot we try to use PC to jump
      logJump(address, null);
      fetcher.jump(address);

      // For presimulation, a break point is set on a jump to an unallocated address
      if (isPresimulation && !fetcher.code.hasAddress(address)) {
        fetcher.code.addBreakAddress(address);
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

  private String executeCall(final ConcreteCall call) throws ConfigurationException {
    if (null != listener) {
      listener.onBeforeExecute(context, call);
    }

    logCall(call);

    if (!isPresimulation) {
      Tracer.setEnabled(true);
      if (invalidCall != call) {
        context.getStatistics().incTraceLength();
        if (Tracer.isEnabled()) {
          Tracer.addRecord(Record.newInstruction(call, context.getModel().getActivePE()));
        }
      }
    }

    final String exception = call.execute(context.getModel().getPE());

    if (!isPresimulation) {
      Tracer.setEnabled(false);
    }

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
      sb.append("label ");
      sb.append(label.getUniqueName());
      sb.append(" at ");
    }

    sb.append(addressText);
    Logger.debug(sb.toString());
  }

  private void logReferenceUndefinedLabel(
      final ConcreteCall call,
      final Label label) throws ConfigurationException {
    Logger.debug("Reference to undefined label %s:", label.getName());
    logCall(call);
    Logger.debug("Call is not executed. Simulation is paused until the label is allocated.");
  }

  private void logJumpExceptionHandler(final String exception, final long address) {
    Logger.debug(
        "Jump to handler of %s at 0x%016x.", exception, address);
  }

  private void logUnallocatedAddress(final long address) {
    Logger.debug(
        "Address 0x%016x is unallocated. Simulation is paused until it is allocated.", address);
  }
}

/////////////////////////////////////////////////////////
/*
final MemoryDevice memory = context.getModel().getPE().getMemoryDevice();
final BitVector data = memory.load(BitVector.valueOf(call.getAddress() >> 2, memory.getAddressBitSize()));
System.out.println("!!! 0x" + data.toHexString());

final DecoderResult result = context.getModel().getDecoder().decode(data);
final IsaPrimitive decoded = result.getPrimitive();
System.out.println("!!! " + decoded.syntax(context.getModel().getTempVars()));

final LocationAccessor pa = getStateObserver().accessLocation("MMU_PA");
System.out.println(String.format("!!! VA=0x%016x, PA=0x%016x", call.getAddress(), pa.getValue()));
*/
/////////////////////////////////////////////////////////