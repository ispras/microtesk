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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.Classifier;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.MemoryOperation;

/**
 * This class describes the policy of unification by devices and events.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public final class ClassifierDevice implements Classifier<MemoryAccess> {
  @Override
  public List<Set<MemoryAccess>> classify(final Collection<MemoryAccess> executions) {
    final List<Set<MemoryAccess>> executionsOfOperation = new ArrayList<>();

    for (MemoryOperation operation : MemoryOperation.values()) {
      // For Store and Load
      final Map<Map<MmuDevice, BufferAccessEvent>, Set<MemoryAccess>> mapExecution =
          new LinkedHashMap<>();

      for (final MemoryAccess execution : executions) {
        if (execution.getOperation() == operation) {
          final Map<MmuDevice, BufferAccessEvent> mapDevices =
              getDevicesAndEvents(execution.getTransitions());

          if (mapExecution.containsKey(mapDevices)) {
            final Set<MemoryAccess> mmuExecutionClass = mapExecution.get(mapDevices);
            mmuExecutionClass.add(execution);
          } else {
            final Set<MemoryAccess> mmuExecutionClass = new HashSet<>();
            mmuExecutionClass.add(execution);
            mapExecution.put(mapDevices, mmuExecutionClass);
          }
        }
      }

      executionsOfOperation.addAll(new ArrayList<>(mapExecution.values()));
    }
    return executionsOfOperation;
  }

  /**
   * Returns the map of devices and events of this execution.
   * 
   * @param transitions list of transition
   * @return the map of devices and events.
   * @throws NullPointerException if {@code conflicts} is null.
   */
  private Map<MmuDevice, BufferAccessEvent> getDevicesAndEvents(
      final List<MmuTransition> transitions) {
    InvariantChecks.checkNotNull(transitions);

    Map<MmuDevice, BufferAccessEvent> result = new LinkedHashMap<>();

    for (final MmuTransition transition : transitions) {
      final MmuAction action = transition.getSource();
      result = addDeviceAndEvent(action, transition, result);
    }

    final MmuTransition transition = transitions.get(transitions.size() - 1);
    final MmuAction action = transition.getTarget();
    result = addDeviceAndEvent(action, transition, result);

    return result;
  }

  /**
   * Adds to the map the device and event of this execution.
   * 
   * @param action the transition action
   * @param transition contains an event
   * @param result the map of devices and events
   * @return the map of devices and events.
   * @throws NullPointerException if some parameters are null.
   * @throws IllegalArgumentException if using the same device for different events.
   */
  private Map<MmuDevice, BufferAccessEvent> addDeviceAndEvent(final MmuAction action,
      final MmuTransition transition, final Map<MmuDevice, BufferAccessEvent> result) {
    InvariantChecks.checkNotNull(action);
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(result);

    final MmuDevice device = action.getDevice();
    BufferAccessEvent event;
    if (transition.getGuard() != null) {
      event = transition.getGuard().getEvent();
    } else {
      event = null;
    }

    if (device != null) {
      if (result.get(device) == null) {
        result.put(device, event);
      } else {
        BufferAccessEvent eventResult = result.get(device);
        if ((eventResult == null) || (eventResult.equals(event))) {
          result.put(device, event);
        } else if (event == null) {
          result.put(device, eventResult);
        } else {
          throw new IllegalStateException("Error: Using the same device: " + device.getName()
              + " for different events: (" + eventResult + ", " + event + ")");
        }
      }
    }
    return result;
  }
}
