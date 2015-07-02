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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.IntegerField;

/**
 * {@link MmuAction} describes an action, which is a named set of assignments.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuAction {
  /** The action name. */
  private final String name;
  /** The action device. */
  private final MmuDevice device;
  /** The action. */
  private final Map<IntegerField, MmuAssignment> action = new HashMap<>();

  /**
   * Constructs an action.
   * 
   * @param name the action name.
   * @param device the device.
   * @param assignments the action assignments.
   * @throws NullPointerException if {@code name} is null.
   */
  public MmuAction(final String name, final MmuDevice device, final MmuAssignment... assignments) {
    InvariantChecks.checkNotNull(name);

    this.name = name;
    this.device = device;

    for (final MmuAssignment assignment : assignments) {
      action.put(assignment.getField(), assignment);
    }
  }

  /**
   * Constructs an action.
   * 
   * @param name the name.
   * @param assignments the action assignments.
   */
  public MmuAction(final String name, final MmuAssignment... assignments) {
    this(name, null, assignments);
  }

  /**
   * Returns the name of the action.
   * 
   * @return the action name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the device.
   * 
   * @return the device.
   */
  public MmuDevice getDevice() {
    return device;
  }

  /**
   * Returns the set of assignments.
   * 
   * @return the set of assignments.
   */
  public Map<IntegerField, MmuAssignment> getAction() {
    return action;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof MmuAction)) {
      return false;
    }

    final MmuAction r = (MmuAction) o;
    return name.equals(r.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }
}
