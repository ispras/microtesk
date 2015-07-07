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
 * {@link MmuAction} describes an action, i.e. a named set of assignments.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuAction {
  /** Unique name. */
  private final String name;
  /** Device used in the action or {@code null}. */
  private final MmuDevice device;
  /** Assignments performed by the action. */
  private final Map<IntegerField, MmuAssignment> action = new HashMap<>();

  public MmuAction(final String name, final MmuDevice device, final MmuAssignment... assignments) {
    InvariantChecks.checkNotNull(name);
    // The device is allowed to be null.

    this.name = name;
    this.device = device;

    for (final MmuAssignment assignment : assignments) {
      action.put(assignment.getLhs(), assignment);
    }
  }

  public MmuAction(final String name, final MmuAssignment... assignments) {
    this(name, null, assignments);
  }

  public String getName() {
    return name;
  }

  public MmuDevice getDevice() {
    return device;
  }

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
