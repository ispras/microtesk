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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class represents a united dependency, which combines information on dependencies of a single
 * execution from other ones.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuUnitedDependency {
  /** The index of the dependent execution. */
  private int index;

  /** The address conflicts. */
  private Map<MmuAddress, MmuUnitedConflict> addressUnitedConflicts = new HashMap<>();
  /** The device conflicts. */
  private Map<MmuDevice, MmuUnitedConflict> deviceUnitedConflicts = new HashMap<>();

  /**
   * Constructs a united dependency.
   * 
   * @param index the execution index.
   * @param dependencies the non-empty set of dependencies to be united, which is represented as a
   *        map with conflicts as keys and execution indices as values.
   */
  public MmuUnitedDependency(final int index, final Map<MmuDependency, Integer> dependencies) {
    InvariantChecks.checkNotNull(dependencies);

    this.index = index;

    final Map<MmuAddress, Map<MmuConflict, Integer>> addressConflicts = new HashMap<>();
    final Map<MmuDevice, Map<MmuConflict, Integer>> deviceConflicts = new HashMap<>();

    // Gather information on conflicts.
    for (final Map.Entry<MmuDependency, Integer> entry : dependencies.entrySet()) {
      final MmuDependency dependency = entry.getKey();
      final int dependsOn = entry.getValue();

      for (final MmuConflict conflict : dependency.getConflicts()) {
        final MmuAddress address = conflict.getAddress();
        final MmuDevice device = conflict.getDevice();

        Map<MmuConflict, Integer> conflicts;

        if (address != null) {
          if ((conflicts = addressConflicts.get(address)) == null) {
            addressConflicts.put(address, conflicts = new HashMap<>());
          }
        } else if (device != null) {
          if ((conflicts = deviceConflicts.get(address)) == null) {
            deviceConflicts.put(device, conflicts = new HashMap<>());
          }
        } else {
          throw new IllegalArgumentException("The dependency type is unknown");
        }

        conflicts.put(conflict, dependsOn);
      }
    }

    // Extend device conflicts with the address conflicts.
    for (final Map.Entry<MmuDevice, Map<MmuConflict, Integer>> entry : deviceConflicts.entrySet()) {
      final MmuDevice device = entry.getKey();
      final Map<MmuConflict, Integer> conflicts = entry.getValue();

      conflicts.putAll(addressConflicts.get(device.getAddress()));
    }

    // Construct the united conflicts for address spaces.
    for (final Map.Entry<MmuAddress, Map<MmuConflict, Integer>> entry : addressConflicts.entrySet()) {
      final MmuAddress address = entry.getKey();
      final Map<MmuConflict, Integer> conflicts = entry.getValue();

      addressUnitedConflicts.put(address, new MmuUnitedConflict(index, conflicts));
    }

    // Construct the united conflicts for devices.
    for (final Map.Entry<MmuDevice, Map<MmuConflict, Integer>> entry : deviceConflicts.entrySet()) {
      final MmuDevice device = entry.getKey();
      final Map<MmuConflict, Integer> conflicts = entry.getValue();

      deviceUnitedConflicts.put(device, new MmuUnitedConflict(index, conflicts));
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
   * Returns the united conflict for the given address.
   * 
   * @param address the address.
   * @return the united conflict.
   */
  public MmuUnitedConflict getConflict(final MmuAddress address) {
    return addressUnitedConflicts.get(address);
  }

  /**
   * Returns the united conflict for the given device.
   * 
   * @param device the device.
   * @return the united conflict.
   */
  public MmuUnitedConflict getConflict(final MmuDevice device) {
    return deviceUnitedConflicts.get(device);
  }
}
