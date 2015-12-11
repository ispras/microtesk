/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
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
  /**
   * {@link Builder} implements {@link MemoryAccessPath} builder.
   * 
   * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
   */
  public static final class Builder {
    private static Collection<MmuAction> getActions(
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);

      final Collection<MmuAction> result = new LinkedHashSet<>();

      for (final MmuTransition transition : transitions) {
        final MmuAction sourceAction = transition.getSource();
        final MmuAction targetAction = transition.getTarget();

        result.add(sourceAction);
        result.add(targetAction);
      }

      return result;
    }

    private static Collection<MmuAddressType> getAddresses(
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);

      final Collection<MmuAddressType> result = new LinkedHashSet<>();

      for (final MmuTransition transition : transitions) {
        final MmuAction action = transition.getSource();
        final MmuBuffer buffer = action.getBuffer();

        if (buffer != null) {
          final MmuAddressType address = buffer.getAddress();
          result.add(address);
        }
      }

      return result;
    }

    private static Collection<MmuBuffer> getBuffers(
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);

      final Collection<MmuBuffer> result = new LinkedHashSet<>();

      for (final MmuTransition transition : transitions) {
        final MmuGuard guard = transition.getGuard();
        final MmuBuffer guardBuffer = guard != null ? guard.getBuffer() : null;

        if (guardBuffer != null) {
          result.add(guardBuffer);
        }

        final MmuAction source = transition.getSource();
        final MmuBuffer sourceBuffer = source.getBuffer();

        if (sourceBuffer != null) {
          result.add(sourceBuffer);
        }

        final MmuAction target = transition.getTarget();
        final MmuBuffer targetBuffer = target.getBuffer();

        if (targetBuffer != null) {
          result.add(targetBuffer);
        }
      }

      return result;
    }

    private static Map<MmuBuffer, BufferAccessEvent> getEvents(
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);

      final Map<MmuBuffer, BufferAccessEvent> result = new LinkedHashMap<>();

      for (final MmuTransition transition : transitions) {
        final MmuGuard guard = transition.getGuard();

        if (guard != null) {
          final MmuBuffer guardBuffer = guard.getBuffer();

          if (guardBuffer != null) {
            result.put(guardBuffer, guard.getEvent());
          }
        }
      }

      return result;
    }

    private static Collection<IntegerVariable> getVariables(
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);

      final Collection<IntegerVariable> result = new LinkedHashSet<>();

      for (final MmuTransition transition : transitions) {
        final MmuGuard guard = transition.getGuard();
        final MmuCondition condition = guard != null ? guard.getCondition() : null;

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
        final Map<IntegerField, MmuBinding> bindings = action.getAction();

        if (bindings != null) {
          for (final Map.Entry<IntegerField, MmuBinding> entry : bindings.entrySet()) {
            final IntegerField lhsField = entry.getKey();
            final MmuBinding binding = entry.getValue();

            // Variables defined in the actions.
            if (binding.getRhs() != null) {
              result.add(lhsField.getVariable());

              // Variables used in the actions.
              for (final IntegerField rhsField : binding.getRhs().getTerms()) {
                result.add(rhsField.getVariable());
              }
            }
          }
        }
      }

      return result;
    }

    private static Collection<MmuSegment> getSegments(
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);

      final MmuSubsystem memory = MmuPlugin.getSpecification();
      final Collection<MmuSegment> segments = new LinkedHashSet<>(memory.getSegments());

      for (final MmuTransition transition : transitions) {
        final MmuGuard guard = transition.getGuard();

        if (guard != null) {
          final Collection<MmuSegment> guardSegments = guard.getSegments();

          if (guardSegments != null) {
            segments.retainAll(guardSegments);
          }
        }
      }

      return segments;
    }

    private static Map<RegionSettings, Collection<MmuSegment>> getRegions(
        final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);

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
      for (final MmuTransition transition : transitions) {
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

            for (final Map.Entry<RegionSettings, Collection<MmuSegment>> entry : regions.entrySet()) {
              final RegionSettings region = entry.getKey();
              final Collection<MmuSegment> segments = entry.getValue();

              segments.retainAll(guardSegments);

              if (segments.isEmpty()) {
                remove.add(region);
              }
            }

            regions.keySet().removeAll(remove);
          }
        }
      }

      return regions;
    }

    private List<MmuTransition> transitions = new ArrayList<>();

    public void add(final MmuTransition transition) {
      InvariantChecks.checkNotNull(transition);
      this.transitions.add(transition);
    }

    public void addAll(final Collection<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);
      this.transitions.addAll(transitions);
    }

    public MemoryAccessPath build() {
      return new MemoryAccessPath(
          transitions,
          getActions(transitions),
          getAddresses(transitions),
          getBuffers(transitions),
          getSegments(transitions),
          getVariables(transitions),
          getEvents(transitions),
          getRegions(transitions));
    }
  }

  private final Collection<MmuTransition> transitions;
  private final Collection<MmuAction> actions;
  private final Collection<MmuAddressType> addresses;
  private final Collection<MmuBuffer> buffers;
  private final Collection<MmuSegment> segments;
  private final Collection<IntegerVariable> variables;
  private final Map<MmuBuffer, BufferAccessEvent> events;
  private final Map<RegionSettings, Collection<MmuSegment>> regions;

  private final MmuTransition firstTransition;
  private final MmuTransition lastTransition;

  private MemorySymbolicExecutor.Result symbolicResult; 

  public MemoryAccessPath(
      final Collection<MmuTransition> transitions,
      final Collection<MmuAction> actions,
      final Collection<MmuAddressType> addresses,
      final Collection<MmuBuffer> buffers,
      final Collection<MmuSegment> segments,
      final Collection<IntegerVariable> variables,
      final Map<MmuBuffer, BufferAccessEvent> events,
      final Map<RegionSettings, Collection<MmuSegment>> regions) {
    InvariantChecks.checkNotNull(transitions);
    InvariantChecks.checkNotEmpty(transitions);
    InvariantChecks.checkNotNull(actions);
    InvariantChecks.checkNotNull(addresses);
    InvariantChecks.checkNotNull(buffers);
    InvariantChecks.checkNotNull(segments);
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(events);
    InvariantChecks.checkNotNull(regions);

    this.transitions = Collections.unmodifiableCollection(transitions);
    this.actions = Collections.unmodifiableCollection(actions);
    this.addresses = Collections.unmodifiableCollection(addresses);
    this.buffers = Collections.unmodifiableCollection(buffers);
    this.segments = Collections.unmodifiableCollection(segments);
    this.variables = Collections.unmodifiableCollection(variables);
    this.events = Collections.unmodifiableMap(events);
    this.regions = Collections.unmodifiableMap(regions);

    final Iterator<MmuTransition> iterator = transitions.iterator();

    MmuTransition transition = iterator.next();
    this.firstTransition = transition;

    while(iterator.hasNext()) {
      transition = iterator.next();
    }

    this.lastTransition = transition;
  }

  public MmuTransition getFirstTransition() {
    return firstTransition;
  }

  public MmuTransition getLastTransition() {
    return lastTransition;
  }

  public Collection<MmuTransition> getTransitions() {
    return transitions;
  }

  public Collection<MmuAction> getActions() {
    return actions;
  }

  public Collection<MmuAddressType> getAddresses() {
    return addresses;
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

  public boolean contains(final MmuTransition transition) {
    InvariantChecks.checkNotNull(transition);
    return transitions.contains(transition);
  }

  public boolean contains(final MmuAction action) {
    InvariantChecks.checkNotNull(action);
    return actions.contains(action);
  }

  public boolean contains(final MmuAddressType address) {
    InvariantChecks.checkNotNull(address);
    return addresses.contains(address);
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

  public MemorySymbolicExecutor.Result getSymbolicResult() {
    return symbolicResult;
  }

  public void setSymbolicResult(final MemorySymbolicExecutor.Result symbolicResult) {
    this.symbolicResult = symbolicResult;
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    for (final MmuTransition transition : transitions) {
      final MmuGuard guard = transition.getGuard();

      if (guard != null && guard.getOperation() == null) {
        builder.append(guard);
        builder.append(separator);
      }

      final MmuAction action = transition.getSource();

      if (action.getBuffer() != null && (guard == null || guard.getBuffer() == null)) {
        builder.append(action.getBuffer());
        builder.append(separator);
      }
    }

    final MmuAction action = lastTransition.getTarget();
    builder.append(action.getName());

    return builder.toString();
  }
}
