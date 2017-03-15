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

import java.util.Collection;

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
  private final MmuCondition condition;
  /** Admissible memory regions. */
  private final Collection<String> regions;
  /** Admissible memory segments. */
  private final Collection<MmuSegment> segments;
  
  public MmuGuard(
      final MemoryOperation operation,
      final MmuBufferAccess bufferAccess,
      final MmuCondition condition,
      final Collection<String> regions,
      final Collection<MmuSegment> segments) {
    this.operation = operation;
    this.bufferAccess = bufferAccess;
    this.condition = condition;
    this.regions = regions;
    this.segments = segments;
  }

  public MmuGuard(
      final MmuBufferAccess bufferAccess,
      final MmuCondition condition) {
    this(null, bufferAccess, condition, null, null);
  }

  public MmuGuard(final MmuBufferAccess bufferAccess) {
    this(null, bufferAccess, null, null, null);
  }

  public MmuGuard(final MmuCondition condition) {
    this(null, null, condition, null, null);
  }

  public MmuGuard(final MmuConditionAtom condition) {
    this(null, null, new MmuCondition(condition), null, null);
  }

  public MmuGuard(final MemoryOperation operation, final MmuCondition condition) {
    this(operation, null, condition, null, null);
  }

  public MmuGuard(final MemoryOperation operation) {
    this(operation, null, null, null, null);
  }

  public MmuGuard(final Collection<String> regions, final Collection<MmuSegment> segments) {
    this(null, null, null, regions, segments);
  }

  public MmuBufferAccess getBufferAccess(final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (bufferAccess == null) {
      return bufferAccess;
    }

    final int instanceId = context.getBufferAccessId(bufferAccess.getBuffer());
    return bufferAccess.getInstance(instanceId, context);
  }

  public MmuCondition getCondition(final int instanceId, final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    if (condition == null || context.isEmptyStack() && instanceId == 0) {
      return condition;
    }

    return condition.getInstance(instanceId, context);
  }

  public MemoryOperation getOperation() {
    return operation;
  }

  public Collection<String> getRegions() {
    return regions;
  }

  public Collection<MmuSegment> getSegments() {
    return segments;
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

    if (regions != null) {
      builder.append(builder.length() > 0 ? separator : "");
      builder.append(regions);
    }

    if (segments != null) {
      builder.append(builder.length() > 0 ? separator : "");
      builder.append(segments);
    }

    return builder.toString();
  }
}
