/*
 * Copyright 2006-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * {@link Load} represents a load operation to be performed to prepare a memory buffer.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Load {
  private final MmuBuffer buffer;

  private final BufferAccessEvent targetEvent;
  private final long targetAddress;

  private final long address;

  /**
   * Construct a load object.
   * 
   * @param buffer the memory buffer being prepared.
   * @param targetEvent the event to be reached on the target address.
   * @param targetAddress the target address.
   * @param address the accessed address.
   */
  public Load(
      final MmuBuffer buffer,
      final BufferAccessEvent targetEvent,
      final long targetAddress,
      final long address) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(targetEvent);

    this.buffer = buffer;

    this.targetEvent = targetEvent;
    this.targetAddress = targetAddress;

    this.address = address;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public BufferAccessEvent getTargetEvent() {
    return targetEvent;
  }

  public long getTargetAddress() {
    return targetAddress;
  }

  public Long getAddress() {
    return address;
  }
}
