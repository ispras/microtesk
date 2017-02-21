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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicResult;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
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

    public static Entry NORMAL(
        final MmuProgram program,
        final MemoryAccessStack.Frame frame) {
      InvariantChecks.checkNotNull(program);
      return new Entry(Kind.NORMAL, program, frame);
    }

    public static Entry CALL(
        final MmuProgram program,
        final MemoryAccessStack.Frame frame) {
      InvariantChecks.checkNotNull(frame);
      InvariantChecks.checkNotNull(program);

      return new Entry(Kind.CALL, program, frame);
    }

    public static Entry RETURN() {
      return new Entry(Kind.RETURN, MmuProgram.EMPTY, null);
    }

    private final Kind kind;
    private final MmuProgram program;
    private final MemoryAccessStack.Frame frame;

    private Entry(
        final Kind kind,
        final MmuProgram program,
        final MemoryAccessStack.Frame frame) {
      InvariantChecks.checkNotNull(kind);
      InvariantChecks.checkNotNull(program);

      this.kind = kind;
      this.program = program;
      this.frame = frame;
    }

    public boolean isNormal() {
      return kind == Kind.NORMAL;
    }

    public boolean isCall() {
      return kind == Kind.CALL;
    }

    public boolean isReturn() {
      return kind == Kind.RETURN;
    }

    public MmuProgram getProgram() {
      return program;
    }

    public boolean hasFrame() {
      return frame != null;
    }

    public MemoryAccessStack.Frame getFrame() {
      return frame;
    }

    @Override
    public String toString() {
      return program.toString();
    }
  }

  private static void updateStack(final MemoryAccessStack stack, final Entry entry) {
    InvariantChecks.checkNotNull(stack);
    InvariantChecks.checkNotNull(entry);

    if (entry.isCall()) {
      stack.call(entry.getFrame());
    } else if (entry.isReturn()) {
      stack.ret();
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

      final MemoryAccessStack stack = new MemoryAccessStack();
      final Collection<MmuAddressInstance> result = new LinkedHashSet<>();

      // Virtual address.
      result.add(memory.getVirtualAddress());

      // Intermediate addresses.
      for (final Entry entry : entries) {
        updateStack(stack, entry);

        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          for (final MmuAction action
              : new MmuAction[] { transition.getSource(), transition.getTarget() } ) {
            final MmuBufferAccess bufferAccess = action.getBufferAccess(stack);
  
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
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      final MemoryAccessStack stack = new MemoryAccessStack();
      final Collection<MmuBufferAccess> result = new LinkedHashSet<>();

      for (final Entry entry : entries) {
        updateStack(stack, entry);

        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuGuard guard = transition.getGuard();
          final MmuBufferAccess guardBufferAccess = guard != null
              ? guard.getBufferAccess(stack) : null;

          if (guardBufferAccess != null) {
            result.add(guardBufferAccess);
          }

          for (final MmuAction action
              : new MmuAction[] { transition.getSource(), transition.getTarget() } ) {
            final MmuBufferAccess actionBufferAccess = action.getBufferAccess(stack);

            if (actionBufferAccess != null) {
              result.add(actionBufferAccess);
            }
          }
        }
      }

      return result;
    }

    private static Map<MmuBufferAccess, BufferAccessEvent> getEvents(
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      final MemoryAccessStack stack = new MemoryAccessStack();
      final Map<MmuBufferAccess, BufferAccessEvent> result = new LinkedHashMap<>();

      for (final Entry entry : entries) {
        updateStack(stack, entry);

        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuGuard guard = transition.getGuard();

          if (guard != null) {
            final MmuBufferAccess guardBufferAccess = guard.getBufferAccess(stack);

            if (guardBufferAccess != null) {
              result.put(guardBufferAccess, guard.getEvent());
            }
          }
        }
      }

      return result;
    }

    private static Collection<IntegerVariable> getVariables(
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      final MemoryAccessStack stack = new MemoryAccessStack();
      final Collection<IntegerVariable> result = new LinkedHashSet<>();

      for (final Entry entry : entries) {
        updateStack(stack, entry);

        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuGuard guard = transition.getGuard();
          final MmuCondition condition = guard != null ? guard.getCondition(stack) : null;

          // Variables used in the guards.
          if (condition != null) {
            for (final MmuConditionAtom atom : condition.getAtoms()) {
              for (final IntegerField field : atom.getLhsExpr().getTerms()) {
                result.add(field.getVariable());
              }
            }
          }

          // Variables used/defined in the actions.
          final MmuAction action = transition.getSource();
          final Map<IntegerField, MmuBinding> bindings = action.getAction(stack);

          if (bindings != null) {
            for (final Map.Entry<IntegerField, MmuBinding> binding : bindings.entrySet()) {
              final IntegerField key = binding.getKey();
              final MmuBinding value = binding.getValue();

              // Variables defined in the actions.
              if (value.getRhs() != null) {
                result.add(key.getVariable());

                // Variables used in the actions.
                for (final IntegerField rhsField : value.getRhs().getTerms()) {
                  result.add(rhsField.getVariable());
                }
              }
            }
          }
        }
      }

      return result;
    }

    private static Collection<MmuSegment> getSegments(
        final Collection<Entry> entries) {
      InvariantChecks.checkNotNull(entries);

      final MmuSubsystem memory = MmuPlugin.getSpecification();
      final Collection<MmuSegment> segments = new LinkedHashSet<>(memory.getSegments());

      for (final Entry entry : entries) {
        final MmuProgram program = entry.getProgram();

        for (final MmuTransition transition : program.getTransitions()) {
          final MmuGuard guard = transition.getGuard();

          if (guard != null) {
            final Collection<MmuSegment> guardSegments = guard.getSegments();
  
            if (guardSegments != null) {
              segments.retainAll(guardSegments);
            }
          }
        }
      }

      return segments;
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
          getBufferAccesses(entries),
          getSegments(entries),
          getVariables(entries),
          getEvents(entries),
          getRegions(entries));
    }
  }

  private final Collection<Entry> entries;
  private final Collection<MmuAction> actions;
  private final Collection<MmuAddressInstance> addressInstances;
  private final Collection<MmuBufferAccess> bufferAccesses;
  private final Collection<MmuBuffer> buffers;
  private final Collection<MmuSegment> segments;
  private final Collection<IntegerVariable> variables;
  private final Map<MmuBufferAccess, BufferAccessEvent> events;
  private final Map<RegionSettings, Collection<MmuSegment>> regions;

  private final Entry firstEntry;
  private final Entry lastEntry;

  private MemorySymbolicResult symbolicResult; 

  public MemoryAccessPath(
      final Collection<Entry> entries,
      final Collection<MmuAction> actions,
      final Collection<MmuAddressInstance> addressInstances,
      final Collection<MmuBufferAccess> bufferAccesses,
      final Collection<MmuSegment> segments,
      final Collection<IntegerVariable> variables,
      final Map<MmuBufferAccess, BufferAccessEvent> events,
      final Map<RegionSettings, Collection<MmuSegment>> regions) {
    InvariantChecks.checkNotNull(entries);
    InvariantChecks.checkNotEmpty(entries);
    InvariantChecks.checkNotNull(actions);
    InvariantChecks.checkNotNull(addressInstances);
    InvariantChecks.checkNotNull(bufferAccesses);
    InvariantChecks.checkNotNull(segments);
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(events);
    InvariantChecks.checkNotNull(regions);

    this.entries = Collections.unmodifiableCollection(entries);
    this.actions = Collections.unmodifiableCollection(actions);
    this.addressInstances = Collections.unmodifiableCollection(addressInstances);
    this.bufferAccesses = Collections.unmodifiableCollection(bufferAccesses);
    this.segments = Collections.unmodifiableCollection(segments);
    this.variables = Collections.unmodifiableCollection(variables);
    this.events = Collections.unmodifiableMap(events);
    this.regions = Collections.unmodifiableMap(regions);

    final Collection<MmuBuffer> buffers = new LinkedHashSet<>();

    for (final MmuBufferAccess bufferAccess : bufferAccesses) {
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

  public Collection<MmuBufferAccess> getBufferAccesses() {
    return bufferAccesses;
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

  public Collection<IntegerVariable> getVariables() {
    return variables;
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
    return bufferAccesses.contains(bufferAccess);
  }

  public boolean contains(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);
    return buffers.contains(buffer);
  }

  public boolean contains(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);
    return variables.contains(variable);
  }

  public BufferAccessEvent getEvent(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);
    return events.get(buffer);
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

    final MemoryAccessStack stack = new MemoryAccessStack();

    boolean comma = false;

    for (final Entry entry : entries) {
      updateStack(stack, entry);

      if (comma) {
        builder.append(separator);
      }

      if (entry.isCall()) {
        builder.append("CALL");
      } else if (entry.isReturn()) {
        builder.append("RETURN");
      } else {
        final MmuProgram program = entry.getProgram();

        if (program.isAtomic()) {
          final MmuTransition transition = program.getTransition();
          final MmuGuard guard = transition.getGuard();

          if (guard != null && guard.getOperation() == null) {
            builder.append(guard);
            builder.append(separator);
          }

          final MmuAction action = program.getSource();
          final MmuBufferAccess bufferAccess = action.getBufferAccess(stack);

          if (bufferAccess != null && (guard == null || guard.getBufferAccess(stack) == null)) {
            builder.append(bufferAccess);
            builder.append(separator);
          }
        } else {
          builder.append("...");
        }
      }

      comma = true;
    }

    final MmuProgram lastProgram = lastEntry.getProgram();
    final MmuAction action = lastProgram.getTarget();
    builder.append(action.getName());

    return builder.toString();
  }
}
