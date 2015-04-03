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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class describes a dependency, which is a number of device usage conflicts.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuDependency {
  /** The list of device conflicts. */
  private final List<MmuConflict> conflicts = new ArrayList<>();

  /**
   * Constructs a dependency.
   */
  public MmuDependency() {}

  /**
   * Constructs a copy of the dependency.
   * 
   * @param dependency the dependency to be copied.
   * @throws NullPointerException if {@code dependency} is null.
   */
  public MmuDependency(final MmuDependency dependency) {
    InvariantChecks.checkNotNull(dependency);

    conflicts.addAll(dependency.getConflicts());
  }

  /**
   * Returns the list of conflicts of the dependency.
   * 
   * @return the list of conflicts.
   */
  public List<MmuConflict> getConflicts() {
    return conflicts;
  }

  /**
   * Adds the conflict to the dependency.
   * 
   * @param conflict the conflict.
   * @throws NullPointerException if {@code conflict} is null.
   */
  public void addConflict(final MmuConflict conflict) {
    InvariantChecks.checkNotNull(conflict);

    conflicts.add(conflict);
  }

  /**
   * Returns the conflict of the device.
   * 
   * @param device the device.
   * @return the conflict of the device.
   * @throws NullPointerException if {@code device} is null.
   */
  public MmuConflict getConflict(final MmuDevice device) {
    InvariantChecks.checkNotNull(device);

    for (final MmuConflict conflict : conflicts) {
      if ((conflict.getDevice() != null) && (conflict.getDevice().equals(device))) {
        return conflict;
      }
    }

    return null;
  }

  /**
   * Checks whether the dependency contains a conflict of the given type.
   * 
   * @param device the device touched upon a conflict.
   * @param conflictType the conflict type.
   * @return {@code true} if the dependency contains a conflict of the given type; {@code false}
   *         otherwise.
   * @throws NullPointerException if {@code device} or {@code conflictType} is null.
   */
  public boolean containsConflict(final MmuDevice device, final MmuConflict.Type conflictType) {
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(conflictType);

    for (final MmuConflict conflict : conflicts) {
      if (device.equals(conflict.getDevice()) && conflict.getType() == conflictType) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether the dependency contains a conflict of the given type.
   * 
   * @param address the address touched upon a conflict.
   * @param conflictType the conflict type.
   * @return {@code true} if the dependency contains a conflict of the given type; {@code false}
   *         otherwise.
   * @throws NullPointerException if {@code address} or {@code conflictType} is null.
   */
  public boolean containsConflict(final MmuAddress address, final MmuConflict.Type conflictType) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(conflictType);

    for (final MmuConflict conflict : conflicts) {
      if (address.equals(conflict.getAddress()) && conflict.getType() == conflictType) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder("Conflicts: ");

    for (final MmuConflict conflict : conflicts) {
      string.append("[");
      string.append(conflict.toString());
      string.append("]");
    }

    return string.toString();
  }
}
