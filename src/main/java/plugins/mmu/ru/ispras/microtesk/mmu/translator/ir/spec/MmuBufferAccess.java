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
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;

/**
 * {@link MmuBufferAccess} represents an MMU buffer access.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuBufferAccess {
  public static enum Kind {
    CHECK,
    READ,
    WRITE
  }

  private final int id;
  private final MmuBuffer buffer;
  private final Kind kind;
  private final MemoryAccessContext context;
  private final MmuAddressInstance address;
  private final MmuStruct entry;

  /** Address passed as an argument on buffer access. */
  private final MmuAddressInstance argument;

  private IntegerRange addressRange;

  public MmuBufferAccess(
      final MmuBuffer buffer,
      final Kind kind,
      final MemoryAccessContext context,
      final MmuAddressInstance address,
      final MmuStruct entry,
      final MmuAddressInstance argument) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(address);

    this.id = context.getBufferAccessId(buffer);
    this.buffer = buffer;
    this.kind = kind;
    this.context = context;
    this.address = address;
    this.entry = entry;
    this.argument = argument;
  }

  public MmuBufferAccess(
      final MmuBuffer buffer,
      final Kind kind,
      final MmuAddressInstance address,
      final MmuStruct entry,
      final MmuAddressInstance argument) {
    this(buffer, kind, MemoryAccessContext.EMPTY, address, entry, argument);
  }

  public int getId() {
    return id;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public Kind getKind() {
    return kind;
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

  // TODO:
  public MmuBufferAccess getInstance(final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (context.isInitial(buffer)) {
      return this;
    }

    return new MmuBufferAccess(
        buffer,
        kind,
        // The memory access context should be copied.
        new MemoryAccessContext(context),
        address.getInstance(context),
        entry.getInstance(context),
        argument != null ? argument.getInstance(context) : null);
  }

  public MmuBufferAccess getParentAccess() {
    return new MmuBufferAccess(buffer.getParent(), kind, context, address, entry, argument);
  }

  public Collection<MmuBufferAccess> getChildAccesses() {
    final Collection<MmuBufferAccess> childAccesses = new ArrayList<>();

    for (final MmuBuffer child : buffer.getChildren()) {
      childAccesses.add(new MmuBufferAccess(child, kind, context, address, entry, argument));
    }

    return childAccesses;
  }

  public IntegerRange getAddressRange() {
    return addressRange;
  }

  public void setAddressRange(final IntegerRange addressRange) {
    this.addressRange = addressRange;
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
        && kind == r.kind
        && (address != null && address.equals(r.address) || address == null && r.address == null);
  }

  @Override
  public int hashCode() {
    int hashCode = buffer.hashCode();

    hashCode = 31 * hashCode + id;
    hashCode = 31 * hashCode + kind.hashCode();
    hashCode = 31 * hashCode + (address != null ? address.hashCode() : 0);

    return hashCode;
  }

  @Override
  public String toString() {
    return String.format("%s(%s:%d[%s])", kind, buffer, id, address);
  }
}
