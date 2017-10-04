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

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;

/**
 * {@link MmuGuard} represents a guard, i.e. a transition activation condition.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuGuard {
  /** Operation: {@code LOAD}, {@code STORE} or {@code null} (any operation). */
  private final MemoryOperation operation;
  /** Buffer access. */
  private final MmuBufferAccess bufferAccess;
  /** Logical condition. */
  private final Node condition;
  /** Admissible memory segment. */
  private final MmuSegment segment;
  /** Memory segment hit/miss. */
  private final boolean isHit;

  public MmuGuard(
      final MemoryOperation operation,
      final MmuBufferAccess bufferAccess,
      final Node condition,
      final MmuSegment segment,
      final boolean isHit) {
    this.operation = operation;
    this.bufferAccess = bufferAccess;
    this.condition = condition;
    this.segment = segment;
    this.isHit = isHit;
  }

  public MmuGuard(
      final MmuBufferAccess bufferAccess,
      final Node condition) {
    this(null, bufferAccess, condition, null, false);
  }

  public MmuGuard(final MmuBufferAccess bufferAccess) {
    this(null, bufferAccess, null, null, false);
  }

  public MmuGuard(final Node condition) {
    this(null, null, condition, null, false);
  }

  public MmuGuard(final MemoryOperation operation, final Node condition) {
    this(operation, null, condition, null, false);
  }

  public MmuGuard(final MemoryOperation operation) {
    this(operation, null, null, null, false);
  }

  public MmuGuard(final MmuSegment segment, final boolean hit) {
    this(null, null, null, segment, hit);
  }

  public MmuBufferAccess getBufferAccess(final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (bufferAccess == null) {
      return bufferAccess;
    }

    final String instanceId = MmuBufferAccess.getId(bufferAccess.getBuffer(), context);
    return bufferAccess.getInstance(instanceId, context);
  }

  public Node getCondition(final String instanceId, final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (context.isEmptyStack() && instanceId == null) {
      return condition;
    }

    return context.getInstance(instanceId, condition);
  }

  public MemoryOperation getOperation() {
    return operation;
  }

  public MmuSegment getSegment() {
    return segment;
  }

  public boolean isHit() {
    return isHit;
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    if (operation != null) {
      builder.append(operation);
    }

    if (bufferAccess != null) {
      builder.append(builder.length() > 0 ? separator : "");
      builder.append(bufferAccess);
    }

    if (condition != null) {
      builder.append(builder.length() > 0 ? separator : "");
      builder.append(condition);
    }

    if (segment != null) {
      builder.append(builder.length() > 0 ? separator : "");
      builder.append(segment);
    }

    return builder.toString();
  }
}
