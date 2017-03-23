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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicResult;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuProgram;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.settings.AccessSettings;
import ru.ispras.microtesk.settings.RegionSettings;

/**
 * {@link MemoryAccessPath} represents the execution path of a memory access instruction.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessPath {

  public final static class Entry {
    public static enum Kind {
      NORMAL,
      CALL,
      RETURN
    }

    public static Entry NORMAL(final MmuProgram program, final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(program);
      return new Entry(Kind.NORMAL, program, context);
    }

    public static Entry CALL(final MmuProgram program, final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(program);
      InvariantChecks.checkNotNull(context);

      return new Entry(Kind.CALL, program, context);
    }

    public static Entry RETURN(final MemoryAccessContext context) {
      return new Entry(Kind.RETURN, MmuProgram.EMPTY, context);
    }

    private final Kind kind;
    private final MmuProgram program;
    private final MemoryAccessContext context;

    private Entry(
        final Kind kind,
        final MmuProgram program,
        final MemoryAccessContext context) {
      InvariantChecks.checkNotNull(kind);
      InvariantChecks.checkNotNull(program);
      InvariantChecks.checkNotNull(context);

      this.kind = kind;
      this.program = program;
      this.context = new MemoryAccessContext(context);
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

    private static Collection<MmuSegment> getSegments(
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      Collection<MmuSegment> segments = null;

      for (final Entry entry : entries) {
        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuGuard guard = transition.getGuard();

          if (guard != null) {
            final Collection<MmuSegment> guardSegments = guard.getSegments();
  
            if (guardSegments != null) {
              if (segments == null) {
                segments = new LinkedHashSet<>(guardSegments);
              } else {
                segments.retainAll(guardSegments);
              }
            }
          }
        }
      }

      return segments != null ? segments : Collections.<MmuSegment>emptyList();
    }

    private static Map<RegionSettings, Collection<MmuSegment>> getRegions(
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      final MmuSubsystem memory = MmuPlugin.getSpecification();
      final Map<RegionSettings, Collection<MmuSegment>> regions = new LinkedHashMap<>();

      // Compose all regions and the corresponding segments.
      for (final RegionSettings region : memory.getRegions()) {
        final Collection<MmuSegment> regionSegments = new LinkedHashSet<>();

        for (final AccessSettings regionAccess: region.getAccesses()) {
          regionSegments.add(memory.getSegment(regionAccess.getSegment()));
        }

        if (!regionSegments.isEmpty()) {
          regions.put(region, regionSegments);
        }
      }

      // Strike out irrelevant regions and segments.
      for (final Entry entry : entries) {
        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuGuard guard = transition.getGuard();

          if (guard != null) {
            // Regions.
            final Collection<String> guardRegionNames = guard.getRegions();

            if (guardRegionNames != null) {
              final Collection<RegionSettings> guardRegions = new ArrayList<RegionSettings>();

              for (final String regionName : guardRegionNames) {
                guardRegions.add(memory.getRegion(regionName));
              }

              regions.keySet().retainAll(guardRegions);
            }

            // Segments.
            final Collection<MmuSegment> guardSegments = guard.getSegments();

            if (guardSegments != null) {
              final Collection<RegionSettings> remove = new ArrayList<>();

              for (final Map.Entry<RegionSettings, Collection<MmuSegment>> region : regions.entrySet()) {
                final RegionSettings key = region.getKey();
                final Collection<MmuSegment> value = region.getValue();

                value.retainAll(guardSegments);

                if (value.isEmpty()) {
                  remove.add(key);
                }
              }

              regions.keySet().removeAll(remove);
            }
          }
        }
      }

      return regions;
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

    public MemoryAccessPath build() {
      return new MemoryAccessPath(
          entries,
          getActions(entries),
          getAddressInstances(entries),
          getBufferAccesses(EnumSet.of(BufferAccessEvent.HIT, BufferAccessEvent.MISS), entries),
          getBufferAccesses(EnumSet.of(BufferAccessEvent.READ), entries),
          getBufferAccesses(EnumSet.of(BufferAccessEvent.WRITE), entries),
          getSegments(entries),
          getRegions(entries));
    }
  }

  private final Collection<Entry> entries;
  private final Collection<MmuAction> actions;
  private final Collection<MmuAddressInstance> addressInstances;
  private final Collection<MmuBufferAccess> bufferChecks;
  private final Collection<MmuBufferAccess> bufferReads;
  private final Collection<MmuBufferAccess> bufferWrites;
  private final Collection<MmuBuffer> buffers;
  private final Collection<MmuSegment> segments;
  private final Map<RegionSettings, Collection<MmuSegment>> regions;

  private final Entry firstEntry;
  private final Entry lastEntry;

  private MemorySymbolicResult symbolicResult; 

  public MemoryAccessPath(
      final Collection<Entry> entries,
      final Collection<MmuAction> actions,
      final Collection<MmuAddressInstance> addressInstances,
      final Collection<MmuBufferAccess> bufferChecks,
      final Collection<MmuBufferAccess> bufferReads,
      final Collection<MmuBufferAccess> bufferWrites,
      final Collection<MmuSegment> segments,
      final Map<RegionSettings, Collection<MmuSegment>> regions) {
    InvariantChecks.checkNotNull(entries);
    InvariantChecks.checkNotEmpty(entries);
    InvariantChecks.checkNotNull(actions);
    InvariantChecks.checkNotNull(addressInstances);
    InvariantChecks.checkNotNull(bufferChecks);
    InvariantChecks.checkNotNull(bufferReads);
    InvariantChecks.checkNotNull(bufferWrites);
    InvariantChecks.checkNotNull(segments);
    InvariantChecks.checkNotNull(regions);

    this.entries = Collections.unmodifiableCollection(entries);
    this.actions = Collections.unmodifiableCollection(actions);
    this.addressInstances = Collections.unmodifiableCollection(addressInstances);
    this.bufferChecks = Collections.unmodifiableCollection(bufferChecks);
    this.bufferReads = Collections.unmodifiableCollection(bufferReads);
    this.bufferWrites = Collections.unmodifiableCollection(bufferWrites);
    this.segments = Collections.unmodifiableCollection(segments);
    this.regions = Collections.unmodifiableMap(regions);

    final Collection<MmuBuffer> buffers = new LinkedHashSet<>();

    for (final MmuBufferAccess bufferAccess : bufferReads) {
      buffers.add(bufferAccess.getBuffer());
    }

    for (final MmuBufferAccess bufferAccess : bufferWrites) {
      buffers.add(bufferAccess.getBuffer());
    }

    this.buffers = Collections.unmodifiableCollection(buffers);

    final Iterator<Entry> iterator = entries.iterator();

    Entry entry = iterator.next();
    this.firstEntry = entry;

    while(iterator.hasNext()) {
      entry = iterator.next();
    }

    this.lastEntry = entry;
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

  public Collection<MmuBuffer> getBuffers() {
    return buffers;
  }

  public Collection<MmuSegment> getSegments() {
    return segments;
  }

  public Map<RegionSettings, Collection<MmuSegment>> getRegions() {
    return regions;
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

    for (final MmuBufferAccess bufferAccess : bufferChecks) {
      if (bufferAccess.getBuffer() == buffer) {
        events.add(bufferAccess.getEvent());
      }
    }

    return events;
  }

  public boolean hasSymbolicResult() {
    return symbolicResult != null;
  }

  public MemorySymbolicResult getSymbolicResult() {
    return symbolicResult;
  }

  public void setSymbolicResult(final MemorySymbolicResult symbolicResult) {
    this.symbolicResult = symbolicResult;
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
