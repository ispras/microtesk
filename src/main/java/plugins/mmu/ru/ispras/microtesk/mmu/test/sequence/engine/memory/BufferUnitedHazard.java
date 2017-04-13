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

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;

/**
 * {@link BufferUnitedHazard} represents a united buffer access hazard,
 * which combines information on hazards for a single buffer access.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferUnitedHazard {
  private final MmuBufferAccess bufferAccess;

  /** Maps a hazard type into a set of execution indices. */
  private final EnumMap<BufferHazard.Type, Set<Pair<Integer, BufferHazard.Instance>>> relation =
      new EnumMap<>(BufferHazard.Type.class);

  public BufferUnitedHazard(
      final MmuBufferAccess bufferAccess,
      final Map<BufferHazard, Set<Pair<Integer, BufferHazard.Instance>>> hazards) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(hazards);
    InvariantChecks.checkNotEmpty(hazards.keySet());

    // Initialize the relation map with empty sets of indices.
    for (final BufferHazard.Type hazardType : BufferHazard.Type.values()) {
      relation.put(hazardType, new LinkedHashSet<Pair<Integer, BufferHazard.Instance>>());
    }

    for (final Map.Entry<BufferHazard, Set<Pair<Integer, BufferHazard.Instance>>> entry
        : hazards.entrySet()) {
      final BufferHazard hazard = entry.getKey();
      final Set<Pair<Integer, BufferHazard.Instance>> dependsOn = entry.getValue();

      // Update the relation map.
      final Set<Pair<Integer, BufferHazard.Instance>> indices = relation.get(hazard.getType());
      indices.addAll(dependsOn);
    }

    InvariantChecks.checkNotNull(bufferAccess);
    this.bufferAccess = bufferAccess;
  }

  public MmuBufferAccess getBufferAccess() {
    return bufferAccess;
  }

  public Set<Pair<Integer, BufferHazard.Instance>> getRelation() {
    final Set<Pair<Integer, BufferHazard.Instance>> result = new LinkedHashSet<>();

    for (final Set<Pair<Integer, BufferHazard.Instance>> hazards : relation.values()) {
      result.addAll(hazards);
    }

    return result;
  }

  public Set<Pair<Integer, BufferHazard.Instance>> getRelation(final BufferHazard.Type hazardType) {
    InvariantChecks.checkNotNull(hazardType);
    return relation.get(hazardType);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    for (final BufferHazard.Type hazardType : BufferHazard.Type.values()) {
      builder.append(comma ? separator : "");
      String.format("%s.%s=%s", bufferAccess, hazardType, relation.get(hazardType));
      comma = true;
    }

    return builder.toString();
  }
}
