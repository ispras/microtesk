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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;

/**
 * {@link BufferUnitedDependency} represents a united dependency, which combines information on
 * dependencies of a single execution from other ones.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferUnitedDependency {
  private final Map<MmuBufferAccess, BufferUnitedHazard> unitedHazards = new LinkedHashMap<>();

  public BufferUnitedDependency(final Map<BufferDependency, Integer> dependencies) {
    InvariantChecks.checkNotNull(dependencies);

    final Map<MmuBufferAccess, Map<BufferHazard, Set<Pair<Integer, BufferHazard.Instance>>>>
        data = new LinkedHashMap<>();

    // Gather information about buffer access hazards.
    for (final Map.Entry<BufferDependency, Integer> entry : dependencies.entrySet()) {
      final BufferDependency dependency = entry.getKey();
      final Integer index = entry.getValue();

      for (final BufferHazard.Instance instance : dependency.getHazards()) {
        final MmuBufferAccess bufferAccess = instance.getPrimaryAccess();

        Map<BufferHazard, Set<Pair<Integer, BufferHazard.Instance>>> hazards = data.get(bufferAccess);
        if (hazards == null) {
          data.put(bufferAccess, hazards = new LinkedHashMap<>());
        }

        Set<Pair<Integer, BufferHazard.Instance>> relation = hazards.get(instance.getHazardType());
        if (relation == null) {
          hazards.put(instance.getHazardType(), relation = new LinkedHashSet<>());
        }

        relation.add(new Pair<Integer, BufferHazard.Instance>(index, instance));
      }
    }

    // Construct the united hazards for buffer accesses.
    for (final Map.Entry<MmuBufferAccess, Map<BufferHazard, Set<Pair<Integer, BufferHazard.Instance>>>>
        entry : data.entrySet()) {
      final MmuBufferAccess bufferAccess = entry.getKey();
      final Map<BufferHazard, Set<Pair<Integer, BufferHazard.Instance>>> hazards = entry.getValue();

      unitedHazards.put(bufferAccess, new BufferUnitedHazard(bufferAccess, hazards));
    }
  }

  public BufferUnitedHazard getHazard(final MmuBufferAccess bufferAccess) {
    return unitedHazards.get(bufferAccess);
  }

  public Map<MmuBufferAccess, BufferUnitedHazard> getBufferHazards() {
    return unitedHazards;
  }

  public Set<Pair<Integer, BufferHazard.Instance>> getRelation(
      final MmuBufferAccess bufferAccess,
      final BufferHazard.Type hazardType) {
    final BufferUnitedHazard hazard = getHazard(bufferAccess);
    return hazard != null ? hazard.getRelation(hazardType) : new LinkedHashSet<Pair<Integer, BufferHazard.Instance>>();
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    for (final Map.Entry<MmuBufferAccess, BufferUnitedHazard> entry : unitedHazards.entrySet()) {
      final MmuBufferAccess bufferAccess = entry.getKey();
      final BufferUnitedHazard unitedHazard = entry.getValue();

      final BufferHazard.Type[] hazardTypes = new BufferHazard.Type[] {
          BufferHazard.Type.TAG_EQUAL,
          BufferHazard.Type.TAG_NOT_EQUAL,
          BufferHazard.Type.TAG_REPLACED,
          BufferHazard.Type.TAG_NOT_REPLACED
      };

      for (final BufferHazard.Type hazardType : hazardTypes) {
        final Set<Pair<Integer, BufferHazard.Instance>> bufferHazardRelation =
            unitedHazard.getRelation(hazardType);

        if (!bufferHazardRelation.isEmpty()) {
          builder.append(comma ? separator : "");
          builder.append(String.format("%s.%s=%s", bufferAccess, hazardType, bufferHazardRelation));
          comma = true;
        }
      }
    }

    return builder.toString();
  }

  public Set<Pair<Integer, BufferHazard.Instance>> getIndexEqualRelation(
      final MmuBufferAccess bufferAccess) {
    final Set<Pair<Integer, BufferHazard.Instance>> relation = new LinkedHashSet<>();

    relation.addAll(getRelation(bufferAccess, BufferHazard.Type.TAG_EQUAL));
    relation.addAll(getRelation(bufferAccess, BufferHazard.Type.TAG_NOT_EQUAL));
    relation.addAll(getRelation(bufferAccess, BufferHazard.Type.TAG_REPLACED));
    relation.addAll(getRelation(bufferAccess, BufferHazard.Type.TAG_NOT_REPLACED));

    return relation;
  }

  public Set<Pair<Integer, BufferHazard.Instance>> getTagEqualRelation(
      final MmuBufferAccess bufferAccess) {
    final Set<Pair<Integer, BufferHazard.Instance>> relation = new LinkedHashSet<>();

    relation.addAll(getRelation(bufferAccess, BufferHazard.Type.TAG_EQUAL));

    for (final MmuBufferAccess childAccess : bufferAccess.getChildAccesses()) {
      relation.addAll(getRelation(childAccess, BufferHazard.Type.TAG_EQUAL));
    }

    return relation;
  }

  public Set<Pair<Integer, BufferHazard.Instance>> getTagReplacedRelation(
      final MmuBufferAccess bufferAccess) {
    return getRelation(bufferAccess, BufferHazard.Type.TAG_REPLACED);
  }
}
