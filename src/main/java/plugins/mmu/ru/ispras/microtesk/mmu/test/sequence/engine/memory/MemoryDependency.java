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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;

/**
 * This class describes a dependency, which is a number of device usage conflicts.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MemoryDependency {
  /** The list of device conflicts. */
  private final List<MemoryHazard> hazards = new ArrayList<>();

  /**
   * Constructs a dependency.
   */
  public MemoryDependency() {}

  /**
   * Constructs a copy of the dependency.
   * 
   * @param dependency the dependency to be copied.
   * @throws IllegalArgumentException if {@code dependency} is null.
   */
  public MemoryDependency(final MemoryDependency dependency) {
    InvariantChecks.checkNotNull(dependency);

    hazards.addAll(dependency.getHazards());
  }

  /**
   * Returns the list of conflicts of the dependency.
   * 
   * @return the list of conflicts.
   */
  public List<MemoryHazard> getHazards() {
    return hazards;
  }

  /**
   * Adds the conflict to the dependency.
   * 
   * @param hazard the conflict.
   * @throws IllegalArgumentException if {@code hazard} is null.
   */
  public void addHazard(final MemoryHazard hazard) {
    InvariantChecks.checkNotNull(hazard);

    hazards.add(hazard);
  }

  /**
   * Returns the conflict of the device.
   * 
   * @param device the device.
   * @return the conflict of the device.
   * @throws IllegalArgumentException if {@code device} is null.
   */
  public MemoryHazard getHazard(final MmuDevice device) {
    InvariantChecks.checkNotNull(device);

    for (final MemoryHazard hazard : hazards) {
      if ((hazard.getDevice() != null) && (hazard.getDevice().equals(device))) {
        return hazard;
      }
    }

    return null;
  }

  /**
   * Checks whether the dependency contains a conflict of the given type.
   * 
   * @param device the device touched upon a conflict.
   * @param hazardType the conflict type.
   * @return {@code true} if the dependency contains a conflict of the given type; {@code false}
   *         otherwise.
   * @throws IllegalArgumentException if {@code device} or {@code hazardType} is null.
   */
  public boolean contains(final MmuDevice device, final MemoryHazard.Type hazardType) {
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(hazardType);

    for (final MemoryHazard hazard : hazards) {
      if (device.equals(hazard.getDevice()) && hazard.getType() == hazardType) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether the dependency contains a conflict of the given type.
   * 
   * @param address the address touched upon a conflict.
   * @param hazardType the conflict type.
   * @return {@code true} if the dependency contains a conflict of the given type; {@code false}
   *         otherwise.
   * @throws IllegalArgumentException if {@code address} or {@code hazardType} is null.
   */
  public boolean contains(final MmuAddress address, final MemoryHazard.Type hazardType) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(hazardType);

    for (final MemoryHazard hazard : hazards) {
      if (address.equals(hazard.getAddress()) && hazard.getType() == hazardType) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    builder.append("{");
    for (final MemoryHazard hazard : hazards) {
      builder.append(comma ? separator : "");
      builder.append(hazard);
    }
    builder.append("}");

    return builder.toString();
  }
}
