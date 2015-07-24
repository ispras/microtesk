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
import ru.ispras.microtesk.basis.classifier.Classifier;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link ClassifierEventBased} classifies memory accesses using devices and events. Memory accesses
 * are considered to be equivalent if they use the same devices and causes the same events.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class ClassifierEventBased implements Classifier<MemoryAccessPath> {
  @Override
  public List<Set<MemoryAccessPath>> classify(final Collection<MemoryAccessPath> paths) {
    InvariantChecks.checkNotNull(paths);

    final List<Set<MemoryAccessPath>> pathClasses = new ArrayList<>();

    final Map<Map<MmuBuffer, BufferAccessEvent>, Set<MemoryAccessPath>> buffersEventsAndPaths =
        new LinkedHashMap<>();

    for (final MemoryAccessPath path : paths) {
      final Map<MmuBuffer, BufferAccessEvent> buffersAndEvents =
          getBuffersAndEvents(path.getTransitions());

      Set<MemoryAccessPath> pathClass = buffersEventsAndPaths.get(buffersAndEvents);
      if (pathClass == null) {
        buffersEventsAndPaths.put(buffersAndEvents, pathClass = new HashSet<>());
      }

      pathClass.add(path);
    }

    pathClasses.addAll(new ArrayList<>(buffersEventsAndPaths.values()));

    return pathClasses;
  }

  /**
   * Returns the map of devices and events of this execution.
   * 
   * @param transitions list of transition
   * @return the map of devices and events.
   */
  private Map<MmuBuffer, BufferAccessEvent> getBuffersAndEvents(
      final List<MmuTransition> transitions) {
    InvariantChecks.checkNotNull(transitions);

    Map<MmuBuffer, BufferAccessEvent> result = new LinkedHashMap<>();

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
   */
  private Map<MmuBuffer, BufferAccessEvent> addDeviceAndEvent(final MmuAction action,
      final MmuTransition transition, final Map<MmuBuffer, BufferAccessEvent> result) {
    InvariantChecks.checkNotNull(action);
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(result);

    final MmuBuffer device = action.getBuffer();
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
