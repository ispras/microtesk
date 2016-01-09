/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.state.Resetter;

/**
 * The InstructionCall class provides methods to run execution simulation
 * of some instruction within the processor model.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class InstructionCall {
  private final Resetter resetter;
  private final IOperation instruction;
  private final String image;
  private final int byteSize;

  /**
   * Creates an instruction call object based on an nML operation. The operation usually
   * represents a composite object encapsulating a hierarchy of aggregated operations
   * that make up a microprocessor instruction.
   * 
   * @param instruction The root operation of the nML operation hierarchy.
   * 
   * @throws IllegalArgumentException if any of the parameters equals {@code null}.
   */

  public InstructionCall(
      final Resetter resetter,
      final IOperation instruction) {
    InvariantChecks.checkNotNull(resetter);
    InvariantChecks.checkNotNull(instruction);

    this.resetter = resetter;
    this.instruction = instruction;
    this.image = instruction.image();

    final int bitSize = image.length();
    this.byteSize = bitSize % 8 == 0 ? bitSize / 8 : bitSize / 8 + 1;
  }

  /**
   * Runs simulation of a corresponding instruction described within the model.
   */

  public void execute() {
    resetter.reset();
    instruction.action();
  }

  /**
   * Return the assembly code for the specified call (for example, the addition instruction
   * of a MIPS processor: addu $1, $1, $2).
   * 
   * @return Text for the instruction call (assembler code).
   */

  public String getText() {
    return instruction.syntax();
  }

  /**
   * Returns image (binary representation) of the instruction call.
   * 
   * @return Image (binary representation) of the instuction call.
   */

  public String getImage() {
    return image;
  }

  /**
   * Returns the size of the instruction in bytes.
   * 
   * @return Size of the instruction in bytes.
   */

  public int getByteSize() {
    return byteSize;
  }

  @Override
  public String toString() {
    return String.format(
        "%s : %s : %d bytes", getText(), image, byteSize);
  }
}
