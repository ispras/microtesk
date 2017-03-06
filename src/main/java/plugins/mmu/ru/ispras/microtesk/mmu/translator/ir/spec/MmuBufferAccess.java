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

import java.util.ArrayList;
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;

/**
 * {@link MmuBufferAccess} represents an MMU buffer access.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuBufferAccess {
  /** Buffer being accessed. */
  private final MmuBuffer buffer;
  /** Memory access stack. */
  private final MemoryAccessStack stack;
  /** Address used to access the buffer. */
  private final MmuAddressInstance address;
  /** Buffer entry being accessed. */
  private final MmuStruct entry;
  /** Address passed as an argument on buffer access. */
  private final MmuAddressInstance argument;

  public MmuBufferAccess(
      final MmuBuffer buffer,
      final MemoryAccessStack stack,
      final MmuAddressInstance address,
      final MmuStruct entry,
      final MmuAddressInstance argument) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(address);

    this.buffer = buffer;
    this.stack = stack;
    this.address = address;
    this.entry = entry;
    this.argument = argument;
  }

  public MmuBufferAccess(
      final MmuBuffer buffer,
      final MmuAddressInstance address,
      final MmuStruct entry,
      final MmuAddressInstance argument) {
    this(buffer, MemoryAccessStack.EMPTY, address, entry, argument);
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public MemoryAccessStack getStack() {
    return stack;
  }

  public MmuAddressInstance getAddress() {
    return address;
  }

  public MmuStruct getEntry() {
    return entry;
  }

  public MmuAddressInstance getArgument() {
    return argument;
  }

  // TODO:
  public MmuBufferAccess getInstance(final MemoryAccessStack stack) {
    InvariantChecks.checkNotNull(stack);

    if (stack.isEmpty()) {
      return this;
    }

    return new MmuBufferAccess(
        buffer,
        // The memory access stack should be copied.
        new MemoryAccessStack(stack),
        address.getInstance(stack),
        entry.getInstance(stack),
        argument != null ? argument.getInstance(stack) : null);
  }

  public MmuBufferAccess getParentAccess() {
    return new MmuBufferAccess(buffer.getParent(), stack, address, entry, argument);
  }

  public Collection<MmuBufferAccess> getChildAccesses() {
    final Collection<MmuBufferAccess> childAccesses = new ArrayList<>();

    for (final MmuBuffer child : buffer.getChildren()) {
      childAccesses.add(new MmuBufferAccess(child, stack, address, entry, argument));
    }

    return childAccesses;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof MmuBufferAccess)) {
      return false;
    }

    final MmuBufferAccess r = (MmuBufferAccess) o;

    return buffer.equals(r.buffer)
        && (address != null && address.equals(r.address) || address == null && r.address == null);
  }

  @Override
  public int hashCode() {
    return 31 * buffer.hashCode() + (address != null ? address.hashCode() : 0);
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", buffer, address);
  }
}
