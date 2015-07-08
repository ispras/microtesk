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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;

/**
 * This class represents a united dependency, which combines information on dependencies of a single
 * execution from other ones.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryUnitedDependency {
  /** The address hazards. */
  private Map<MmuAddress, MemoryUnitedHazard> addrUnitedHazards = new LinkedHashMap<>();
  /** The device hazards. */
  private Map<MmuDevice, MemoryUnitedHazard> deviceUnitedHazards = new LinkedHashMap<>();

  /**
   * Constructs a united dependency.
   * 
   * @param dependencies the non-empty set of dependencies to be united, which is represented as a
   *        map with hazards as keys and execution indices as values.
   */
  public MemoryUnitedDependency(final Map<MemoryDependency, Integer> dependencies) {
    InvariantChecks.checkNotNull(dependencies);

    final Map<MmuAddress, Map<MemoryHazard, Set<Integer>>> addressHazards = new LinkedHashMap<>();
    final Map<MmuDevice, Map<MemoryHazard, Set<Integer>>> deviceHazards = new LinkedHashMap<>();

    // Gather information on hazards.
    for (final Map.Entry<MemoryDependency, Integer> entry : dependencies.entrySet()) {
      final MemoryDependency dependency = entry.getKey();
      final int dependsOn = entry.getValue();

      for (final MemoryHazard hazard : dependency.getHazards()) {
        final MmuAddress address = hazard.getAddress();
        final MmuDevice device = hazard.getDevice();

        Map<MemoryHazard, Set<Integer>> hazards;

        if (address != null) {
          if ((hazards = addressHazards.get(address)) == null) {
            addressHazards.put(address, hazards = new LinkedHashMap<>());
          }
        } else if (device != null) {
          if ((hazards = deviceHazards.get(device)) == null) {
            deviceHazards.put(device, hazards = new LinkedHashMap<>());
          }
        } else {
          throw new IllegalArgumentException("The dependency type is unknown");
        }

        Set<Integer> relation = hazards.get(hazard);
        if (relation == null) {
          hazards.put(hazard, relation = new LinkedHashSet<>());
        }

        relation.add(dependsOn);
      }
    }

    // Extend device hazards with the address hazards.
    for (final Map.Entry<MmuDevice, Map<MemoryHazard, Set<Integer>>> entry : deviceHazards.entrySet()) {
      final MmuDevice device = entry.getKey();
      final Map<MemoryHazard, Set<Integer>> hazards = entry.getValue();
      final Map<MemoryHazard, Set<Integer>> moreHazards = addressHazards.get(device.getAddress());

      if (moreHazards != null) {
        hazards.putAll(moreHazards);
      }
    }

    // Construct the united hazards for address spaces.
    for (final Map.Entry<MmuAddress, Map<MemoryHazard, Set<Integer>>> entry : addressHazards.entrySet()) {
      final MmuAddress address = entry.getKey();
      final Map<MemoryHazard, Set<Integer>> hazards = entry.getValue();

      addrUnitedHazards.put(address, new MemoryUnitedHazard(hazards));
    }

    // Construct the united hazards for devices.
    for (final Map.Entry<MmuDevice, Map<MemoryHazard, Set<Integer>>> entry : deviceHazards.entrySet()) {
      final MmuDevice device = entry.getKey();
      final Map<MemoryHazard, Set<Integer>> hazards = entry.getValue();

      deviceUnitedHazards.put(device, new MemoryUnitedHazard(hazards));
    }
  }

  /**
   * Returns the united hazard for the given address.
   * 
   * @param address the address.
   * @return the united hazard.
   */
  public MemoryUnitedHazard getHazard(final MmuAddress address) {
    return addrUnitedHazards.get(address);
  }

  /**
   * Returns all united hazards over addresses.
   * 
   * @return the collection of the united hazards.
   */
  public Map<MmuAddress, MemoryUnitedHazard> getAddrHazards() {
    return addrUnitedHazards;
  }

  /**
   * Returns the united hazard for the given device.
   * 
   * @param device the device.
   * @return the united hazard.
   */
  public MemoryUnitedHazard getHazard(final MmuDevice device) {
    return deviceUnitedHazards.get(device);
  }

  /**
   * Returns all united hazards over devices.
   * 
   * @return the collection of the united hazards.
   */
  public Map<MmuDevice, MemoryUnitedHazard> getDeviceHazards() {
    return deviceUnitedHazards;
  }

  /**
   * Returns all united hazards over devices with the given address type.
   * 
   * @return the collection of the united hazards.
   */
  public Map<MmuDevice, MemoryUnitedHazard> getDeviceHazards(final MmuAddress address) {
    // TODO: Optimization is required.
    final Map<MmuDevice, MemoryUnitedHazard> result = new LinkedHashMap<>();

    for (final Map.Entry<MmuDevice, MemoryUnitedHazard> entry : deviceUnitedHazards.entrySet()) {
      final MmuDevice device = entry.getKey();
      final MemoryUnitedHazard hazard = entry.getValue();

      if (device.getAddress() == address) {
        result.put(device, hazard);
      }
    }

    return result;
  }

  /**
   * Returns the set of execution indices for the given hazard type of the given address.
   * 
   * @param hazardType the hazard type.
   * @return the set of execution indices.
   */
  public Set<Integer> getRelation(final MmuAddress address, final MemoryHazard.Type hazardType) {
    final MemoryUnitedHazard hazard = getHazard(address);

    return hazard != null ? hazard.getRelation(hazardType) : new LinkedHashSet<Integer>();
  }

  /**
   * Returns the set of execution indices for the given hazard type of the given device.
   * 
   * @param hazardType the hazard type.
   * @return the set of execution indices.
   */
  public Set<Integer> getRelation(final MmuDevice device, final MemoryHazard.Type hazardType) {
    final MemoryUnitedHazard hazard = getHazard(device);

    return hazard != null ? hazard.getRelation(hazardType) : new LinkedHashSet<Integer>();
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    for (final Map.Entry<MmuAddress, MemoryUnitedHazard> addrEntry : addrUnitedHazards.entrySet()) {
      final MmuAddress addrType = addrEntry.getKey();
      final MemoryUnitedHazard addrHazard = addrEntry.getValue();

      final MemoryHazard.Type addrHazardType = MemoryHazard.Type.ADDR_EQUAL;
      final Set<Integer> addrHazardRelation = addrHazard.getRelation(addrHazardType);

      if (!addrHazardRelation.isEmpty()) {
        builder.append(comma ? separator : "");
        builder.append(String.format("%s.%s=%s", addrType, addrHazardType, addrHazardRelation));
        comma = true;
      }
    }

    for (final Map.Entry<MmuDevice, MemoryUnitedHazard> deviceEntry : deviceUnitedHazards.entrySet()) {
      final MmuDevice deviceType = deviceEntry.getKey();
      final MemoryUnitedHazard deviceHazard = deviceEntry.getValue();

      final MemoryHazard.Type[] deviceHazardTypes = new MemoryHazard.Type[] {
          MemoryHazard.Type.TAG_EQUAL,
          MemoryHazard.Type.TAG_NOT_EQUAL,
          MemoryHazard.Type.TAG_REPLACED,
          MemoryHazard.Type.TAG_NOT_REPLACED
      };

      for (final MemoryHazard.Type deviceHazardType : deviceHazardTypes) {
        final Set<Integer> deviceHazardRelation = deviceHazard.getRelation(deviceHazardType);

        if (!deviceHazardRelation.isEmpty()) {
          builder.append(comma ? separator : "");
          builder.append(String.format("%s.%s=%s",
              deviceType, deviceHazardType, deviceHazardRelation));
          comma = true;
        }
      }
    }

    return builder.toString();
  }

  // -----------------------------------------------------------------------------------------------
  // Useful Shortcuts
  // -----------------------------------------------------------------------------------------------

  /**
   * Returns the address-equal relation for the given address type.
   * 
   * <p>Given an execution index {@code j} and an address type {@code a}, the address-equal relation
   * is a set of execution indices {@code I} such that for each {@code i} in {@code I},
   * {@code i < j} and {@code a.address(i) = a.address(j)}.</p>
   * 
   * @param address the address type being used.
   * @return the set of execution indices.
   */
  public Set<Integer> getAddrEqualRelation(final MmuAddress address) {
    return getRelation(address, MemoryHazard.Type.ADDR_EQUAL);
  }

  /**
   * Returns the index-equal relation for the given device.
   * 
   * <p>Given an execution index {@code j} and a device {@code d}, the index-equal relation is a set
   * of execution indices {@code I} such that for each {@code i} in {@code I}, {@code i < j} and
   * {@code d.index(i) = d.index(j)}.</p>
   * 
   * @param device the device being accessed.
   * @return the set of execution indices.
   */
  public Set<Integer> getIndexEqualRelation(final MmuDevice device) {
    final Set<Integer> relation = new LinkedHashSet<>();

    relation.addAll(getRelation(device, MemoryHazard.Type.TAG_EQUAL));
    relation.addAll(getRelation(device, MemoryHazard.Type.TAG_NOT_EQUAL));
    relation.addAll(getRelation(device, MemoryHazard.Type.TAG_REPLACED));
    relation.addAll(getRelation(device, MemoryHazard.Type.TAG_NOT_REPLACED));

    return relation;
  }

  /**
   * Returns the tag-equal relation for the given device.
   * 
   * <p>Given an execution index {@code j} and a device {@code d}, the tag-equal relation is a set
   * of execution indices {@code I} such that for each {@code i} in {@code I}, {@code i < j} and
   * {@code d.index(i) = d.index(j) & d.tag(i) = d.tag(j)}.</p>
   * 
   * <p>If the device has children (so-called views), the tag-equal relation is supplemented with
   * the children's relations.</p>
   * 
   * @param device the device being accessed.
   * @return the set of execution indices.
   */
  public Set<Integer> getTagEqualRelation(final MmuDevice device) {
    final Set<Integer> relation = new LinkedHashSet<>();

    relation.addAll(getRelation(device, MemoryHazard.Type.TAG_EQUAL));

    for (final MmuDevice child : device.getChildren()) {
      relation.addAll(getRelation(child, MemoryHazard.Type.TAG_EQUAL));
    }

    return relation;
  }

  /**
   * Returns the tag-replaced relation for the given device.
   * 
   * <p>Given an execution index {@code j} and a device {@code d}, the tag-replaced relation is a
   * set of execution indices {@code I} such that {@code |I| <= 1} and for each {@code i} in
   * {@code I}, {@code i < j} and {@code d.index(i) = d.index(j) & d.tag(j) = replaced(d, i)}.</p>
   * 
   * @param device the device being accessed.
   * @return the set of execution indices.
   */
  public Set<Integer> getTagReplacedRelation(final MmuDevice device) {
    return getRelation(device, MemoryHazard.Type.TAG_REPLACED);
  }
}
