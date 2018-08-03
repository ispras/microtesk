/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.spec;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * {@link MmuBufferAccess} represents an MMU buffer access.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuBufferAccess {
  private final String id;
  private final MmuBuffer buffer;
  private final BufferAccessEvent event;
  private final MemoryAccessContext context;
  private final MmuAddressInstance address;
  private final MmuStruct entry;

  /** Address passed as an argument on buffer access. */
  private final MmuAddressInstance argument;

  public static String getId(
      final MmuBuffer buffer,
      final MemoryAccessContext context) {
    return String.format("%s_%d", buffer.getName(), context.getBufferAccessId(buffer));
  }

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

    this.id = getId(buffer, context);
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

  public String getId() {
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

  public Node getTagExpression() {
    return context.getInstance(id, buffer.getTagExpression());
  }

  public Node getIndexExpression() {
    return context.getInstance(id, buffer.getIndexExpression());
  }

  public Node getOffsetExpression() {
    return context.getInstance(id, buffer.getOffsetExpression());
  }

  public final Collection<MmuBinding> getMatchBindings() {
    if (context.isEmptyStack() && id == null) {
      return buffer.getMatchBindings();
    }

    final Collection<MmuBinding> bindingInstances = new ArrayList<>();

    for (final MmuBinding binding : buffer.getMatchBindings()) {
      bindingInstances.add(binding.getInstance(id, id, context));
    }

    return bindingInstances;
  }

  public MmuBufferAccess getInstance(final String instanceId, final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (context.isEmptyStack() && instanceId == null) {
      return this;
    }

    return new MmuBufferAccess(
        buffer,
        event,
        // The memory access context should be copied.
        new MemoryAccessContext(context),
        address.getInstance(instanceId, context),
        entry.getInstance(instanceId, context),
        argument != null
          ? argument.getInstance(null /* Not related to the buffer */, context)
          : null);
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
        && id.equals(r.id)
        && event == r.event
        && (address != null && address.equals(r.address) || address == null && r.address == null);
  }

  @Override
  public int hashCode() {
    int hashCode = buffer.hashCode();

    hashCode = 31 * hashCode + (id != null ? id.hashCode() : 0);
    hashCode = 31 * hashCode + event.hashCode();
    hashCode = 31 * hashCode + (address != null ? address.hashCode() : 0);

    return hashCode;
  }

  @Override
  public String toString() {
    return String.format("%s(%s:%s[%s])", event, buffer, id, address);
  }
}
