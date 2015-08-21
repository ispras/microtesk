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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.classifier.Classifier;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link ClassifierEventBased} classifies memory accesses using buffers and events. Memory accesses
 * are considered to be equivalent if they use the same buffers and causes the same events.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class ClassifierEventBased implements Classifier<MemoryAccessPath> {
  /**
   * Returns the buffers-to-events map of the memory access path.
   * 
   * @param path the memory access path.
   * @return the buffers and events.
   */
  public static Map<MmuBuffer, BufferAccessEvent> getBuffersAndEvents(final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    final Map<MmuBuffer, BufferAccessEvent> result = new LinkedHashMap<>();

    for (final MmuTransition transition : path.getTransitions()) {
      final MmuGuard guard = transition.getGuard();
      if (guard == null) {
        continue;
      }

      final MmuBuffer buffer = guard.getBuffer();
      if (buffer == null) {
        continue;
      }

      final BufferAccessEvent event = guard.getEvent();
      InvariantChecks.checkNotNull(event);

      final BufferAccessEvent oldEvent = result.put(buffer, event);
      InvariantChecks.checkTrue(oldEvent == null || event.equals(oldEvent),
          String.format("Usage of the same buffer %s with different events", buffer.getName()));
    }

    return result;
  }

  /**
   * Returns the collection of the memory access paths grouped by the buffer-event pairs.
   * 
   * @param paths the memory access paths,
   * @return the classes of memory access pairs.
   */
  public static Map<Map<MmuBuffer, BufferAccessEvent>, Set<MemoryAccessPath>>
    getBuffersAndEvents(final Collection<MemoryAccessPath> paths) {
    InvariantChecks.checkNotNull(paths);

    final Map<Map<MmuBuffer, BufferAccessEvent>, Set<MemoryAccessPath>> result =
        new LinkedHashMap<>();

    for (final MemoryAccessPath path : paths) {
      final Map<MmuBuffer, BufferAccessEvent> buffersAndEvents = getBuffersAndEvents(path);

      Set<MemoryAccessPath> group = result.get(buffersAndEvents);
      if (group == null) {
        result.put(buffersAndEvents, group = new LinkedHashSet<>());
      }

      group.add(path);
    }

    return result;
  }

  @Override
  public List<Set<MemoryAccessPath>> classify(final Collection<MemoryAccessPath> paths) {
    InvariantChecks.checkNotNull(paths);
    return new ArrayList<>(getBuffersAndEvents(paths).values());
  }
}
