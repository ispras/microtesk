/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link MmuBufferAccess} represents an MMU buffer access.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuBufferAccess {
  /** Buffer being accessed. */
  private final MmuBuffer buffer;
  /** Address used to access the buffer. */
  private final MmuAddressInstance address;
  /** Buffer entry being accessed. */
  private final MmuStruct entry;

  public MmuBufferAccess(
      final MmuBuffer buffer,
      final MmuAddressInstance address,
      final MmuStruct entry) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(address);

    this.buffer = buffer;
    this.address = address;
    this.entry = entry;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public MmuAddressInstance getAddress() {
    return address;
  }

  public MmuStruct getEntry() {
    return entry;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof MmuBufferAccess)) {
      return false;
    }

    final MmuBufferAccess r = (MmuBufferAccess) o;

    return buffer.equals(r.buffer);
  }

  @Override
  public int hashCode() {
    return 31 * buffer.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s", buffer);
  }
}
