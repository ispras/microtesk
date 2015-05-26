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

package ru.ispras.microtesk.translator.mmu.coverage;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.MmuAddress;
import ru.ispras.microtesk.translator.mmu.spec.MmuDevice;

/**
 * This class represents a united dependency, which combines information on dependencies of a single
 * execution from other ones.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class UnitedDependency {
  /** The address conflicts. */
  private Map<MmuAddress, UnitedHazard> addrUnitedHazards = new LinkedHashMap<>();
  /** The device conflicts. */
  private Map<MmuDevice, UnitedHazard> deviceUnitedHazards = new LinkedHashMap<>();

  /**
   * Constructs a united dependency.
   * 
   * @param dependencies the non-empty set of dependencies to be united, which is represented as a
   *        map with conflicts as keys and execution indices as values.
   */
  public UnitedDependency(final Map<Dependency, Integer> dependencies) {
    InvariantChecks.checkNotNull(dependencies);

    final Map<MmuAddress, Map<Hazard, Integer>> addressConflicts = new LinkedHashMap<>();
    final Map<MmuDevice, Map<Hazard, Integer>> deviceConflicts = new LinkedHashMap<>();

    // Gather information on conflicts.
    for (final Map.Entry<Dependency, Integer> entry : dependencies.entrySet()) {
      final Dependency dependency = entry.getKey();
      final int dependsOn = entry.getValue();

      for (final Hazard conflict : dependency.getHazards()) {
        final MmuAddress address = conflict.getAddress();
        final MmuDevice device = conflict.getDevice();

        Map<Hazard, Integer> conflicts;

        if (address != null) {
          if ((conflicts = addressConflicts.get(address)) == null) {
            addressConflicts.put(address, conflicts = new LinkedHashMap<>());
          }
        } else if (device != null) {
          if ((conflicts = deviceConflicts.get(device)) == null) {
            deviceConflicts.put(device, conflicts = new LinkedHashMap<>());
          }
        } else {
          throw new IllegalArgumentException("The dependency type is unknown");
        }

        conflicts.put(conflict, dependsOn);
      }
    }

    // Extend device conflicts with the address conflicts.
    for (final Map.Entry<MmuDevice, Map<Hazard, Integer>> entry : deviceConflicts.entrySet()) {
      final MmuDevice device = entry.getKey();
      final Map<Hazard, Integer> conflicts = entry.getValue();
      final Map<Hazard, Integer> moreConflicts = addressConflicts.get(device.getAddress());

      if (moreConflicts != null) {
        conflicts.putAll(moreConflicts);
      }
    }

    // Construct the united conflicts for address spaces.
    for (final Map.Entry<MmuAddress, Map<Hazard, Integer>> entry : addressConflicts.entrySet()) {
      final MmuAddress address = entry.getKey();
      final Map<Hazard, Integer> conflicts = entry.getValue();

      addrUnitedHazards.put(address, new UnitedHazard(conflicts));
    }

    // Construct the united conflicts for devices.
    for (final Map.Entry<MmuDevice, Map<Hazard, Integer>> entry : deviceConflicts.entrySet()) {
      final MmuDevice device = entry.getKey();
      final Map<Hazard, Integer> conflicts = entry.getValue();

      deviceUnitedHazards.put(device, new UnitedHazard(conflicts));
    }
  }

  /**
   * Returns the united conflict for the given address.
   * 
   * @param address the address.
   * @return the united conflict.
   */
  public UnitedHazard getHazard(final MmuAddress address) {
    return addrUnitedHazards.get(address);
  }

  /**
   * Returns all united conflicts over addresses.
   * 
   * @return the collection of the united conflicts.
   */
  public Map<MmuAddress, UnitedHazard> getAddrHazards() {
    return addrUnitedHazards;
  }

  /**
   * Returns the united conflict for the given device.
   * 
   * @param device the device.
   * @return the united conflict.
   */
  public UnitedHazard getHazard(final MmuDevice device) {
    return deviceUnitedHazards.get(device);
  }

  /**
   * Returns all united conflicts over devices.
   * 
   * @return the collection of the united conflicts.
   */
  public Map<MmuDevice, UnitedHazard> getDeviceHazards() {
    return deviceUnitedHazards;
  }

  /**
   * Returns all united conflicts over devices with the given address type.
   * 
   * @return the collection of the united conflicts.
   */
  public Map<MmuDevice, UnitedHazard> getDeviceHazards(final MmuAddress address) {
    // TODO: Optimization is required.
    final Map<MmuDevice, UnitedHazard> result = new LinkedHashMap<>();

    for (final Map.Entry<MmuDevice, UnitedHazard> entry : deviceUnitedHazards.entrySet()) {
      final MmuDevice device = entry.getKey();
      final UnitedHazard conflict = entry.getValue();

      if (device.getAddress() == address) {
        result.put(device, conflict);
      }
    }

    return result;
  }

  /**
   * Returns the set of execution indices for the given conflict type of the given address.
   * 
   * @param conflictType the conflict type.
   * @return the set of execution indices.
   */
  public Set<Integer> getRelation(final MmuAddress address, final Hazard.Type conflictType) {
    final UnitedHazard conflict = getHazard(address);

    return conflict != null ? conflict.getRelation(conflictType) : new LinkedHashSet<Integer>();
  }

  /**
   * Returns the set of execution indices for the given conflict type of the given device.
   * 
   * @param conflictType the conflict type.
   * @return the set of execution indices.
   */
  public Set<Integer> getRelation(final MmuDevice device, final Hazard.Type conflictType) {
    final UnitedHazard conflict = getHazard(device);

    return conflict != null ? conflict.getRelation(conflictType) : new LinkedHashSet<Integer>();
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    for (final Map.Entry<MmuAddress, UnitedHazard> addrEntry : addrUnitedHazards.entrySet()) {
      final MmuAddress addrType = addrEntry.getKey();
      final UnitedHazard addrHazard = addrEntry.getValue();

      final Hazard.Type addrHazardType = Hazard.Type.ADDR_EQUAL;
      final Set<Integer> addrHazardRelation = addrHazard.getRelation(addrHazardType);

      if (!addrHazardRelation.isEmpty()) {
        builder.append(comma ? separator : "");
        builder.append(String.format("%s.%s=%s", addrType, addrHazardType, addrHazardRelation));
        comma = true;
      }
    }

    for (final Map.Entry<MmuDevice, UnitedHazard> deviceEntry : deviceUnitedHazards.entrySet()) {
      final MmuDevice deviceType = deviceEntry.getKey();
      final UnitedHazard deviceHazard = deviceEntry.getValue();

      final Hazard.Type[] deviceHazardTypes = new Hazard.Type[] {
          Hazard.Type.TAG_EQUAL,
          Hazard.Type.TAG_NOT_EQUAL,
          Hazard.Type.TAG_REPLACED,
          Hazard.Type.TAG_NOT_REPLACED
      };

      for (final Hazard.Type deviceHazardType : deviceHazardTypes) {
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
    return getRelation(address, Hazard.Type.ADDR_EQUAL);
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

    relation.addAll(getRelation(device, Hazard.Type.TAG_EQUAL));
    relation.addAll(getRelation(device, Hazard.Type.TAG_NOT_EQUAL));
    relation.addAll(getRelation(device, Hazard.Type.TAG_REPLACED));
    relation.addAll(getRelation(device, Hazard.Type.TAG_NOT_REPLACED));

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

    relation.addAll(getRelation(device, Hazard.Type.TAG_EQUAL));

    for (final MmuDevice child : device.getChildren()) {
      relation.addAll(getRelation(child, Hazard.Type.TAG_EQUAL));
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
    return getRelation(device, Hazard.Type.TAG_REPLACED);
  }
}
