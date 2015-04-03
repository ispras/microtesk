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

package ru.ispras.microtesk.translator.mmu.spec;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class represents a united conflict, which combines information on conflicts on a single
 * device and/or an address space.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuUnitedConflict {
  /** The index of the dependent execution. */
  private int index;

  /** The address space touched upon the conflict. */
  private MmuAddress address;
  /** The device touched upon the conflict. */
  private MmuDevice device;

  /** Maps a conflict type into a set of execution indices. */
  private EnumMap<MmuConflict.Type, Set<Integer>> relation = new EnumMap<>(MmuConflict.Type.class);

  /**
   * Constructs a united conflict.
   * 
   * @param index the execution index.
   * @param conflicts the non-empty set of conflicts to be united, which is represented as a map
   *        with conflicts as keys and execution indices as values.
   */
  public MmuUnitedConflict(final int index, final Map<MmuConflict, Integer> conflicts) {
    InvariantChecks.checkNotNull(conflicts);

    if (conflicts.isEmpty()) {
      throw new IllegalArgumentException("The empty set of conflicts");
    }

    this.index = index;

    // Initialize the relation map with empty sets of indices.
    for (final MmuConflict.Type conflictType : MmuConflict.Type.values()) {
      relation.put(conflictType, new LinkedHashSet<Integer>());
    }

    for (final Map.Entry<MmuConflict, Integer> entry : conflicts.entrySet()) {
      final MmuConflict conflict = entry.getKey();
      final int dependsOn = entry.getValue();
      
      // Check the consistency of the conflicts.
      if (address == null) {
        address = conflict.getAddress();
      } else if (conflict.getAddress() != null && !address.equals(conflict.getAddress())) {
        throw new IllegalArgumentException(String.format(
            "The use of different address spaces in a conflict: %s != %s", address,
            conflict.getAddress()));
      }

      if (device == null) {
        device = conflict.getDevice();
      } else if (conflict.getDevice() != null && !device.equals(conflict.getDevice())) {
        throw new IllegalArgumentException(String.format(
            "The use of different devices in a conflict: %s != %s", device,
            conflict.getDevice()));
      }

      // Update the relation map.
      final Set<Integer> indices = relation.get(conflict.getType());
      indices.add(dependsOn);
    }

    if (address == null) {
      address = device.getAddress();
    } else if (device != null && !address.equals(device.getAddress())) {
      throw new IllegalArgumentException("The device and the address space are incompatible");
    }
  }

  /**
   * Returns the index of the dependent execution.
   * 
   * @return the execution index.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the set of execution indices for the given conflict type.
   * 
   * @param conflictType the conflict type.
   * @return the set of execution indices.
   */
  public Set<Integer> getRelation(final MmuConflict.Type conflictType) {
    return relation.get(conflictType);
  }
}
