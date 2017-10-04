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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;

/**
 * {@link MmuAction} describes an action, i.e. a named set of assignments.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MmuAction {
  /** Unique name. */
  private final String name;
  /** Specifies whether the action is raise exception. */
  private final boolean exception;
  /** Buffer used in the action or {@code null}. */
  private final MmuBufferAccess bufferAccess;
  /** Assignments performed by the action. */
  private final Map<Node, MmuBinding> action = new HashMap<>();
  /** Marks associated with the action. */
  private final Set<String> marks = new LinkedHashSet<>();

  private MmuAction(
      final String name,
      final boolean exception,
      final MmuBufferAccess bufferAccess,
      final List<MmuBinding> assignments) {
    InvariantChecks.checkNotNull(name);
    // The buffer access is allowed to be null.

    this.name = name;
    this.exception = exception;
    this.bufferAccess = bufferAccess;

    for (final MmuBinding assignment : assignments) {
      action.put(assignment.getLhs(), assignment);
    }
  }

  public MmuAction(
      final String name,
      final boolean exception) {
    this(name, exception, null, Collections.<MmuBinding>emptyList());
  }

  public MmuAction(final String name) {
    this(name, false);
  }

  public MmuAction(
      final String name,
      final MmuBufferAccess bufferAccess,
      final MmuBinding... assignments) {
    this(name, false, bufferAccess, Arrays.asList(assignments));
  }

  public MmuAction(
      final String name,
      final MmuBinding... assignments) {
    this(name, null, assignments);
  }

  public MmuAction(
      final String name,
      final MmuBufferAccess bufferAccess,
      final MmuStruct lhs,
      final MmuStruct rhs) {
    this(
        name,
        false,
        bufferAccess,
        null != lhs && null != rhs ? lhs.bindings(rhs) : Collections.<MmuBinding>emptyList()
        );
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);
  }

  public MmuAction(
      final String name,
      final MmuStruct lhs,
      final MmuStruct rhs) {
    this(name, null, lhs, rhs);
  }

  public String getName() {
    return name;
  }

  public boolean isException() {
    return exception;
  }

  public MmuBufferAccess getBufferAccess(final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (bufferAccess == null) {
      return bufferAccess;
    }

    final String instanceId = MmuBufferAccess.getId(bufferAccess.getBuffer(), context);
    return bufferAccess.getInstance(instanceId, context);
  }

  public Map<Node, MmuBinding> getAssignments(
      final String lhsInstanceId,
      final String rhsInstanceId,
      final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (context.isEmptyStack() && lhsInstanceId == null && rhsInstanceId == null) {
      return action;
    }

    final Map<Node, MmuBinding> actionInstance = new LinkedHashMap<>();

    for (final Map.Entry<Node, MmuBinding> entry : action.entrySet()) {
      final Node lhs = entry.getKey();
      final MmuBinding rhs = entry.getValue();

      actionInstance.put(
          context.getInstance(lhsInstanceId, lhs),
          rhs.getInstance(lhsInstanceId, rhsInstanceId, context));
    }

    return actionInstance;
  }

  public void addMark(final String mark) {
    InvariantChecks.checkNotNull(mark);
    marks.add(mark);
  }

  public Set<String> getMarks() {
    return marks;
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
    final StringBuilder builder = new StringBuilder();

    builder.append(name);

    if (!marks.isEmpty()) {
      builder.append(", ");
      builder.append(String.format("marks: %s", marks));
    }

    if (bufferAccess != null) {
      builder.append(", ");
      builder.append(String.format("buffer: %s", bufferAccess.getBuffer()));
    }

    return builder.toString();
  }
}
