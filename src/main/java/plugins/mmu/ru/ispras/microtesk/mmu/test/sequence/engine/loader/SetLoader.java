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

package ru.ispras.microtesk.mmu.test.sequence.engine.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.BufferAccessEvent;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SetLoader implements Loader {
  private final MmuDevice device;
  private final long index;

  private final EnumMap<BufferAccessEvent, Map<Long, GoalReacher>> eventGoals =
      new EnumMap<>(BufferAccessEvent.class);

  public SetLoader(final MmuDevice device, final long index) {
    this.device = device;
    this.index = index;
  }

  public MmuDevice getDevice() {
    return device;
  }
  
  public long getIndex() {
    return index;
  }

  public boolean contains(final BufferAccessEvent event, final long address) {
    final Map<Long, GoalReacher> tagGoals = eventGoals.get(event);
    if (tagGoals == null) {
      return false;
    }

    final GoalReacher goalReacher = tagGoals.get(device.getTag(address));
    return goalReacher != null;
  }

  public void addLoads(final BufferAccessEvent event, final long address, final List<Long> loads) {
    Map<Long, GoalReacher> tagGoals = eventGoals.get(event);
    if (tagGoals == null) {
      eventGoals.put(event, tagGoals = new LinkedHashMap<>());
    }

    final long tag = device.getTag(address);
    GoalReacher goalReacher = tagGoals.get(tag);
    if (goalReacher == null) {
      tagGoals.put(tag, goalReacher = new GoalReacher(device, event, address));
    }

    goalReacher.addLoads(event, address, loads);
  }

  private List<Long> prepareLoads(final Collection<GoalReacher> goalReachers) {
    final List<Long> sequence = new ArrayList<>();

    int sequenceLength = 0;

    for (final GoalReacher goalReacher : goalReachers) {
      final List<Long> loads = goalReacher.prepareLoads();

      sequence.addAll(loads);
      sequenceLength += loads.size();

      // Some kind of optimization.
      if (sequenceLength >= device.getWays()) {
        break;
      }
    }

    return sequence;
  }

  @Override
  public List<Long> prepareLoads() {
    final List<Long> sequence = new ArrayList<>();

    // Evict data.
    final Map<Long, GoalReacher> missGoals = eventGoals.get(BufferAccessEvent.MISS);
    if (missGoals != null) {
      sequence.addAll(prepareLoads(missGoals.values()));
    }

    // Load data.
    final Map<Long, GoalReacher> hitGoals = eventGoals.get(BufferAccessEvent.HIT);
    if (hitGoals != null) {
      sequence.addAll(prepareLoads(hitGoals.values()));
    }

    return sequence;
  }
}
