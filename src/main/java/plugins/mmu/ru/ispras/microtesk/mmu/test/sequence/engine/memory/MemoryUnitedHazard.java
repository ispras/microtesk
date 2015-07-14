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

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * This class represents a united hazard, which combines information on hazards on a single
 * device and/or an address space.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryUnitedHazard {
  /** The address space touched upon the hazard. */
  private MmuAddressType address;
  /** The device touched upon the hazard. */
  private MmuBuffer device;

  /** Maps a hazard type into a set of execution indices. */
  private EnumMap<MemoryHazard.Type, Set<Integer>> relation = new EnumMap<>(MemoryHazard.Type.class);

  /**
   * Constructs a united hazard.
   * 
   * @param hazards the non-empty set of hazards to be united, which is represented as a map
   *        with hazards as keys and execution indices as values.
   */
  public MemoryUnitedHazard(final Map<MemoryHazard, Set<Integer>> hazards) {
    InvariantChecks.checkNotNull(hazards);
    InvariantChecks.checkNotEmpty(hazards.keySet());

    // Initialize the relation map with empty sets of indices.
    for (final MemoryHazard.Type hazardType : MemoryHazard.Type.values()) {
      relation.put(hazardType, new LinkedHashSet<Integer>());
    }

    for (final Map.Entry<MemoryHazard, Set<Integer>> entry : hazards.entrySet()) {
      final MemoryHazard hazard = entry.getKey();
      final Set<Integer> dependsOn = entry.getValue();

      // Check the consistency of the hazards.
      if (address == null) {
        address = hazard.getAddress();
      } else if (hazard.getAddress() != null && !address.equals(hazard.getAddress())) {
        throw new IllegalArgumentException(
            String.format("The use of different address spaces in a hazard: %s != %s", address,
                hazard.getAddress()));
      }

      if (device == null) {
        device = hazard.getDevice();
      } else if (hazard.getDevice() != null && !device.equals(hazard.getDevice())) {
        throw new IllegalArgumentException(
            String.format("The use of different devices in a hazard: %s != %s", device,
                hazard.getDevice()));
      }

      // Update the relation map.
      final Set<Integer> indices = relation.get(hazard.getType());
      indices.addAll(dependsOn);
    }

    if (address == null) {
      address = device.getAddress();
    } else if (device != null && !address.equals(device.getAddress())) {
      throw new IllegalArgumentException("The device and the address space are incompatible");
    }
  }

  // TODO:
  public MmuAddressType getAddress() {
    return address;
  }

  // TODO:
  public MmuBuffer getDevice() {
    return device;
  }

  /**
   * Returns the set of execution indices for the given hazard type.
   * 
   * @param hazardType the hazard type.
   * @return the set of execution indices.
   */
  public Set<Integer> getRelation(final MemoryHazard.Type hazardType) {
    return relation.get(hazardType);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    final String resource = (address != null ? address : device).toString();

    boolean comma = false;

    for (final MemoryHazard.Type hazardType : MemoryHazard.Type.values()) {
      builder.append(comma ? separator : "");
      String.format("%s.%s=%s", resource, hazardType, relation.get(hazardType));
      comma = true;
    }

    return builder.toString();
  }
}
