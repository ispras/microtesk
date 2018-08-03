/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.engine.memory;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.model.spec.MmuAction;
import ru.ispras.microtesk.mmu.model.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.model.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.model.spec.MmuGuard;
import ru.ispras.microtesk.mmu.model.spec.MmuProgram;
import ru.ispras.microtesk.mmu.model.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.model.spec.MmuTransition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * {@link AccessPath} represents an execution path of a memory access instruction.
 *
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class AccessPath {
  public static final AccessPath EMPTY = new AccessPath();

  public static final class Entry {
    public static enum Kind {
      NORMAL,
      CALL,
      RETURN
    }

    public static Entry NORMAL(
        final boolean isStart,
        final MmuProgram program,
        final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(program);
      return new Entry(isStart, Kind.NORMAL, program, context);
    }

    public static Entry CALL(
        final boolean isStart,
        final MmuProgram program,
        final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(program);
      InvariantChecks.checkNotNull(context);

      return new Entry(isStart, Kind.CALL, program, context);
    }

    public static Entry RETURN(
        final boolean isStart,
        final MemoryAccessContext context) {
      return new Entry(isStart, Kind.RETURN, MmuProgram.EMPTY, context);
    }

    private final boolean isStart;
    private final Kind kind;
    private final MmuProgram program;
    private final MemoryAccessContext context;

    private Entry(
        final boolean isStart,
        final Kind kind,
        final MmuProgram program,
        final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(kind);
      InvariantChecks.checkNotNull(program);
      InvariantChecks.checkNotNull(context);

      this.isStart = isStart;
      this.kind = kind;
      this.program = program;
      this.context = new MemoryAccessContext(context);
    }

    public boolean isStart() {
      return isStart;
    }

    public Kind getKind() {
      return kind;
    }

    public MmuProgram getProgram() {
      return program;
    }

    public MemoryAccessContext getContext() {
      return context;
    }

    public MemoryAccessStack getStack() {
      return context.getMemoryAccessStack();
    }

    public MemoryAccessStack.Frame getFrame() {
      return getStack().getFrame();
    }

    @Override
    public String toString() {
      return program.toString();
    }
  }

  public static final class Builder {
    private static Collection<MmuAction> getActions(
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      final Collection<MmuAction> result = new LinkedHashSet<>();

      for (final Entry entry : entries) {
        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuAction sourceAction = transition.getSource();
          final MmuAction targetAction = transition.getTarget();

          result.add(sourceAction);
          result.add(targetAction);
        }
      }

      return result;
    }

    private static Collection<MmuAddressInstance> getAddressInstances(
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      final MmuSubsystem memory = MmuPlugin.getSpecification();
      final Collection<MmuAddressInstance> result = new LinkedHashSet<>();

      // Virtual address.
      result.add(memory.getVirtualAddress());

      // Intermediate addresses.
      for (final Entry entry : entries) {
        final MmuProgram program = entry.getProgram();
        final MemoryAccessContext context = entry.getContext();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuAction source = transition.getSource();
          final MmuAction target = transition.getTarget();

          for (final MmuAction action : new MmuAction[] { source, target } ) {
            final MmuBufferAccess bufferAccess = action.getBufferAccess(context);

            if (bufferAccess != null) {
              result.add(bufferAccess.getAddress());
            }
          }
        }
      }

      // Physical address.
      result.add(memory.getPhysicalAddress());

      return result;
    }

    private static Collection<MmuBufferAccess> getBufferAccesses(
        final EnumSet<BufferAccessEvent> events,
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(events);
      InvariantChecks.checkNotNull(entries);

      final Collection<MmuBufferAccess> bufferAccesses = new LinkedHashSet<>();

      for (final Entry entry : entries) {
        final MmuProgram program = entry.getProgram();
        final MemoryAccessContext context = entry.getContext();

        for (final MmuTransition transition : program.getTransitions()) {
          for (final MmuBufferAccess bufferAccess : transition.getBufferAccesses(context)) {
            if (events.contains(bufferAccess.getEvent())) {
              bufferAccesses.add(bufferAccess);
            }
          }
        }
      }

      return bufferAccesses;
    }

    private final Collection<Entry> entries = new ArrayList<>();

    public void add(final Entry entry) {
      InvariantChecks.checkNotNull(entry);
      this.entries.add(entry);
    }

    public void addAll(final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);
      this.entries.addAll(entries);
    }

    public AccessPath build() {
      return new AccessPath(
          entries,
          getActions(entries),
          getAddressInstances(entries),
          getBufferAccesses(EnumSet.of(BufferAccessEvent.HIT, BufferAccessEvent.MISS), entries),
          getBufferAccesses(EnumSet.of(BufferAccessEvent.READ), entries),
          getBufferAccesses(EnumSet.of(BufferAccessEvent.WRITE), entries),
          getBufferAccesses(EnumSet.allOf(BufferAccessEvent.class), entries));
    }
  }

  private final Collection<Entry> entries;
  private final Collection<MmuAction> actions;
  private final Collection<MmuAddressInstance> addressInstances;
  private final Collection<MmuBufferAccess> bufferChecks;
  private final Collection<MmuBufferAccess> bufferReads;
  private final Collection<MmuBufferAccess> bufferWrites;
  private final Collection<MmuBufferAccess> bufferAccesses;
  private final Collection<MmuBuffer> buffers;

  private final Entry firstEntry;
  private final Entry lastEntry;

  public AccessPath(
      final Collection<Entry> entries,
      final Collection<MmuAction> actions,
      final Collection<MmuAddressInstance> addressInstances,
      final Collection<MmuBufferAccess> bufferChecks,
      final Collection<MmuBufferAccess> bufferReads,
      final Collection<MmuBufferAccess> bufferWrites,
      final Collection<MmuBufferAccess> bufferAccesses) {
    InvariantChecks.checkNotNull(entries);
    InvariantChecks.checkNotNull(actions);
    InvariantChecks.checkNotNull(addressInstances);
    InvariantChecks.checkNotNull(bufferChecks);
    InvariantChecks.checkNotNull(bufferReads);
    InvariantChecks.checkNotNull(bufferWrites);
    InvariantChecks.checkNotNull(bufferAccesses);

    this.entries = Collections.unmodifiableCollection(entries);
    this.actions = Collections.unmodifiableCollection(actions);
    this.addressInstances = Collections.unmodifiableCollection(addressInstances);
    this.bufferChecks = Collections.unmodifiableCollection(bufferChecks);
    this.bufferReads = Collections.unmodifiableCollection(bufferReads);
    this.bufferWrites = Collections.unmodifiableCollection(bufferWrites);
    this.bufferAccesses = Collections.unmodifiableCollection(bufferAccesses);

    final Collection<MmuBuffer> buffers = new LinkedHashSet<>();

    for (final MmuBufferAccess bufferAccess : bufferAccesses) {
      buffers.add(bufferAccess.getBuffer());
    }

    this.buffers = Collections.unmodifiableCollection(buffers);

    if (!entries.isEmpty()) {
      final Iterator<Entry> iterator = entries.iterator();

      Entry entry = iterator.next();
      this.firstEntry = entry;

      while (iterator.hasNext()) {
        entry = iterator.next();
      }

      this.lastEntry = entry;
    } else {
      this.firstEntry = null;
      this.lastEntry = null;
    }
  }

  public AccessPath() {
    this(
      Collections.<Entry>emptyList(),
      Collections.<MmuAction>emptyList(),
      Collections.<MmuAddressInstance>emptyList(),
      Collections.<MmuBufferAccess>emptyList(),
      Collections.<MmuBufferAccess>emptyList(),
      Collections.<MmuBufferAccess>emptyList(),
      Collections.<MmuBufferAccess>emptyList()
    );
  }

  public int size() {
    return entries.size();
  }

  public Entry getFirstEntry() {
    return firstEntry;
  }

  public Entry getLastEntry() {
    return lastEntry;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  public Collection<MmuAction> getActions() {
    return actions;
  }

  public Collection<MmuAddressInstance> getAddressInstances() {
    return addressInstances;
  }

  public Collection<MmuBufferAccess> getBufferChecks() {
    return bufferChecks;
  }

  public Collection<MmuBufferAccess> getBufferReads() {
    return bufferReads;
  }

  public Collection<MmuBufferAccess> getBufferWrites() {
    return bufferWrites;
  }

  public Collection<MmuBufferAccess> getBufferAccesses() {
    return bufferAccesses;
  }

  public Collection<MmuBuffer> getBuffers() {
    return buffers;
  }

  public boolean contains(final MmuAction action) {
    InvariantChecks.checkNotNull(action);
    return actions.contains(action);
  }

  public boolean contains(final MmuAddressInstance addressInstance) {
    InvariantChecks.checkNotNull(addressInstance);
    return addressInstances.contains(addressInstance);
  }

  public boolean contains(final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(bufferAccess);

    switch (bufferAccess.getEvent()) {
      case HIT:
      case MISS:
        return bufferChecks.contains(bufferAccess);
      case READ:
        return bufferReads.contains(bufferAccess);
      case WRITE:
        return bufferWrites.contains(bufferAccess);
      default:
        return false;
    }
  }

  public boolean contains(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);
    return buffers.contains(buffer);
  }

  public Collection<BufferAccessEvent> getEvents(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    final Collection<BufferAccessEvent> events = new LinkedHashSet<>();

    for (final MmuBufferAccess bufferAccess : bufferAccesses) {
      final MemoryAccessStack stack = bufferAccess.getContext().getMemoryAccessStack();

      // Recursive accesses are not taken into account.
      if (bufferAccess.getBuffer() == buffer && stack.isEmpty()) {
        events.add(bufferAccess.getEvent());
      }
    }

    return events;
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    for (final Entry entry : entries) {
      final MmuProgram program = entry.getProgram();
      final MemoryAccessContext context = entry.getContext();

      if (builder.length() != 0) {
        builder.append(separator);
      }

      if (program == null) {
        builder.append(entry.getKind());
      } else {
        builder.append(entry.getKind());
        builder.append(": ");

        if (program.isAtomic()) {
          final MmuTransition transition = program.getTransition();

          final MmuAction source = transition.getSource();
          builder.append(source.getName());

          final MmuGuard guard = transition.getGuard();
          if (guard != null) {
            builder.append(separator);
            builder.append(guard);
          }

          final MmuAction target = transition.getTarget();
          final MmuBufferAccess bufferAccess = target.getBufferAccess(context);

          if (bufferAccess != null) {
            builder.append(separator);
            builder.append(bufferAccess);
          }
        } else {
          builder.append("...");
        }
      }
    }

    final MmuProgram lastProgram = lastEntry.getProgram();
    final MmuAction action = lastProgram.getTarget();

    if (builder.length() != 0) {
      builder.append(separator);
    }
    builder.append(action.getName());

    return builder.toString();
  }
}
