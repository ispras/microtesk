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
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;

/**
 * {@link MmuBufferAccess} represents an MMU buffer access.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuBufferAccess {
  private final int id;
  private final MmuBuffer buffer;
  private final BufferAccessEvent event;
  private final MemoryAccessContext context;
  private final MmuAddressInstance address;
  private final MmuStruct entry;

  /** Address passed as an argument on buffer access. */
  private final MmuAddressInstance argument;

  public MmuBufferAccess(
      final MmuBuffer buffer,
      final BufferAccessEvent event,
      final MemoryAccessContext context,
      final MmuAddressInstance address,
      final MmuStruct entry,
      final MmuAddressInstance argument) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(event);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(address);

    this.id = context.getBufferAccessId(buffer);
    this.buffer = buffer;
    this.event = event;
    this.context = context;
    this.address = address;
    this.entry = entry;
    this.argument = argument;
  }

  public MmuBufferAccess(
      final MmuBuffer buffer,
      final BufferAccessEvent event,
      final MmuAddressInstance address,
      final MmuStruct entry,
      final MmuAddressInstance argument) {
    this(buffer, event, MemoryAccessContext.EMPTY, address, entry, argument);
  }

  public int getId() {
    return id;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public BufferAccessEvent getEvent() {
    return event;
  }

  public MemoryAccessContext getContext() {
    return context;
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

  public MmuExpression getTagExpression() {
    return buffer.getTagExpression().getInstance(id, context);
  }

  public MmuExpression getIndexExpression() {
    return buffer.getIndexExpression().getInstance(id, context);
  }

  public MmuExpression getOffsetExpression() {
    return buffer.getOffsetExpression().getInstance(id, context);
  }

  public final Collection<MmuBinding> getMatchBindings() {
    if (context.isEmptyStack() && id == 0) {
      return buffer.getMatchBindings();
    }

    final Collection<MmuBinding> bindingInstances = new ArrayList<>();

    for (final MmuBinding binding : buffer.getMatchBindings()) {
      bindingInstances.add(binding.getInstance(id, id, context));
    }

    return bindingInstances;
  }

  public MmuBufferAccess getInstance(final int instanceId, final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (context.isEmptyStack() && instanceId == 0) {
      return this;
    }

    return new MmuBufferAccess(
        buffer,
        event,
        // The memory access context should be copied.
        new MemoryAccessContext(context),
        address.getInstance(instanceId, context),
        entry.getInstance(instanceId, context),
        argument != null ? argument.getInstance(0 /* Not related to the buffer */, context) : null);
  }

  public MmuBufferAccess getParentAccess() {
    return new MmuBufferAccess(buffer.getParent(), event, context, address, entry, argument);
  }

  public Collection<MmuBufferAccess> getChildAccesses() {
    final Collection<MmuBufferAccess> childAccesses = new ArrayList<>();

    for (final MmuBuffer child : buffer.getChildren()) {
      childAccesses.add(new MmuBufferAccess(child, event, context, address, entry, argument));
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
        && id == r.id
        && event == r.event
        && (address != null && address.equals(r.address) || address == null && r.address == null);
  }

  @Override
  public int hashCode() {
    int hashCode = buffer.hashCode();

    hashCode = 31 * hashCode + id;
    hashCode = 31 * hashCode + event.hashCode();
    hashCode = 31 * hashCode + (address != null ? address.hashCode() : 0);

    return hashCode;
  }

  @Override
  public String toString() {
    return String.format("%s(%s:%d[%s])", event, buffer, id, address);
  }
}
