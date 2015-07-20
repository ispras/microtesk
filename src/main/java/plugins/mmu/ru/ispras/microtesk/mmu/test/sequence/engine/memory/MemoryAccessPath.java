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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

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
    private List<MmuTransition> transitions = new ArrayList<>();

    public void add(final MmuTransition transition) {
      InvariantChecks.checkNotNull(transition);
      this.transitions.add(transition);
    }

    public void addAll(final List<MmuTransition> transitions) {
      InvariantChecks.checkNotNull(transitions);
      this.transitions.addAll(transitions);
    }

    public MemoryAccessPath build() {
      return new MemoryAccessPath(transitions);
    }
  }

  private final List<MmuTransition> transitions;

  public MemoryAccessPath(
      final List<MmuTransition> transitions) {
    InvariantChecks.checkNotNull(transitions);
    this.transitions = transitions;
  }

  public List<MmuTransition> getTransitions() {
    return transitions;
  }

  public MmuTransition getFirstTransition() {
    return transitions.get(0);
  }

  public MmuTransition getLastTransition() {
    return transitions.get(transitions.size() - 1);
  }

  /**
   * Returns the buffers used in the memory access path in order of their occurrence.
   * 
   * @return the list of the buffers with no duplicates.
   */
  public List<MmuBuffer> getBuffers() {
    final List<MmuBuffer> result = new ArrayList<>();
    final Set<MmuBuffer> handled = new HashSet<>();

    for (final MmuTransition transition : transitions) {
      final MmuGuard guard = transition.getGuard();
      final MmuBuffer guardBuffer = guard != null ? guard.getBuffer() : null;

      if (guardBuffer != null && !handled.contains(guardBuffer)) {
        handled.add(guardBuffer);
        result.add(guardBuffer);
      }

      final MmuAction source = transition.getSource();
      final MmuBuffer sourceBuffer = source.getBuffer();

      if (sourceBuffer != null && !handled.contains(sourceBuffer)) {
        handled.add(sourceBuffer);
        result.add(sourceBuffer);
      }

      final MmuAction target = transition.getTarget();
      final MmuBuffer targetBuffer = target.getBuffer();

      if (targetBuffer != null && !handled.contains(targetBuffer)) {
        handled.add(targetBuffer);
        result.add(targetBuffer);
      }
    }

    return result;
  }

  /**
   * Returns the address types used in the memory accesses path in order of their occurrence.
   * 
   * @return the list of the address types with no duplicates.
   */
  public List<MmuAddressType> getAddresses() {
    final List<MmuAddressType> result = new ArrayList<>();
    final Set<MmuAddressType> handled = new HashSet<>();

    for (final MmuTransition transition : transitions) {
      final MmuAction action = transition.getSource();
      final MmuBuffer buffer = action.getBuffer();

      if (buffer != null) {
        final MmuAddressType address = buffer.getAddress();

        if (!handled.contains(address)) {
          handled.add(address);
          result.add(address);
        }
      }
    }

    return result;
  }

  /**
   * Returns the actions executed in the memory access in order of their occurrence in the
   * execution path.
   * 
   * @return the list of the actions.
   */
  public List<MmuAction> getActions() {
    final List<MmuAction> result = new ArrayList<>();

    for (final MmuTransition transition : transitions) {
      final MmuAction action = transition.getSource();
      result.add(action);
    }

    if (!transitions.isEmpty()) {
      final MmuTransition transition = transitions.get(transitions.size() - 1);
      final MmuAction action = transition.getTarget();
      result.add(action);
    }

    return result;
  }

  public BufferAccessEvent getEvent(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    for (final MmuTransition transition : transitions) {
      final MmuGuard guard = transition.getGuard();

      if (guard != null) {
        final MmuBuffer guardBuffer = guard.getBuffer();

        if (buffer.equals(guardBuffer)) {
          return guard.getEvent();
        }
      }
    }

    return null;
  }

  /**
   * Checks whether the execution contains a transition.
   * 
   * @param transition the transition
   * @return {@code true} if the execution contains the transition; {@code false} otherwie.
   */
  public boolean contains(final MmuTransition transition) {
    InvariantChecks.checkNotNull(transition);

    return transitions.contains(transition);
  }

  public boolean contains(final MmuAction action) {
    InvariantChecks.checkNotNull(action);

    final List<MmuAction> actions = getActions();
    return actions.contains(action);
  }

  public boolean contains(final MmuAddressType address) {
    InvariantChecks.checkNotNull(address);

    final Collection<MmuAddressType> addresses = getAddresses();
    return addresses.contains(address);
  }

  public boolean contains(final MmuBuffer device) {
    InvariantChecks.checkNotNull(device);

    final Collection<MmuBuffer> devices = getBuffers();
    return devices.contains(device);
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
    }

    final MmuTransition transition = transitions.get(transitions.size() - 1);
    final MmuAction action = transition.getTarget();

    builder.append(action.getName());

    return builder.toString();
  }
}
