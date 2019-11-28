/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.tracer;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.ConcreteCall;

/**
 * The {@link Record} class describes Tracer log records.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
  */
public abstract class Record {
  private static long instructionId = -1;

  public static void resetInstructionCount() {
    instructionId = -1;
  }

  private final RecordKind kind;
  private final long time;

  public Record(final RecordKind kind, final long time) {
    this.kind = kind;
    this.time = time;
  }

  public RecordKind getKind() {
    return kind;
  }

  public long getTime() {
    return time;
  }

  @Override
  public String toString() {
    return String.format("%d clk", time);
  }

  public static Record newInstruction(final ConcreteCall call, final int cpu) {
    return new Instruction(call, cpu);
  }

  public static Record newMemoryAccess(
      final long address,
      final BitVector data,
      final boolean isWrite) {
    return new MemoryAccess(address, data, isWrite);
  }

  public static Record newRegisterWrite(
      final String register,
      final BitVector value) {
    return new RegisterWrite(register, value);
  }

  private static final class Instruction extends Record {
    private BitVector instrId;
    private final int cpu;
    private long addr;
    private String disasm;

    private Instruction(final ConcreteCall call, final int cpu) {
      super(RecordKind.INSTRUCT, ++instructionId);
      InvariantChecks.checkNotNull(call);

      this.cpu = cpu;
      this.addr = call.getAddress().longValue();
      this.disasm = call.getExecutable().getText();

      try {
        final String image = call.getImage();
        this.instrId = BitVector.valueOf(image, 2, image.length());
      } catch (final NumberFormatException e) {
        Logger.error(
            "Failed to parse image for instruction call %s: '%s'. Reason: %s.",
            disasm,
            call.getImage(),
            e.getMessage()
        );

        this.addr = 0;
      }
    }

    @Override
    public String toString() {
      return String.format(
          "%s %d IT (%d) %016x %s A svc_ns : %s",
          super.toString(),
          cpu,
          getTime(),
          addr,
          instrId.toHexString(true).toLowerCase(),
          disasm
          );
    }
  }

  private static final class MemoryAccess extends Record {
    private final long address;
    private final BitVector data;
    private final boolean isWrite;

    private MemoryAccess(
        final long address,
        final BitVector data,
        final boolean isWrite) {
      super(RecordKind.MEMORY, instructionId);

      InvariantChecks.checkNotNull(address);
      InvariantChecks.checkNotNull(data);

      this.address = address;
      this.data = data;
      this.isWrite = isWrite;
    }

    @Override
    public String toString() {
      return String.format(
          "%s M%s%d %016x %s",
          super.toString(),
          isWrite ? "W" : "R",
          data.getByteSize(),
          address,
          data.toHexString()
          );
    }
  }

  private static final class RegisterWrite extends Record {
    private final String register;
    private final BitVector value;

    private RegisterWrite(
        final String register,
        final BitVector value) {
      super(RecordKind.REGISTER, instructionId);

      InvariantChecks.checkNotNull(register);
      InvariantChecks.checkNotNull(value);

      this.register = register;
      this.value = value;
    }

    @Override
    public String toString() {
      return String.format(
          "%s R %s %s",
          super.toString(),
          register,
          value.toHexString().toLowerCase()
          );
    }
  }
}
