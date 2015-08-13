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

import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;

/**
 * {@link MmuGuard} represents a guard, i.e. a transition activation condition.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuGuard {
  /** Operation: {@code LOAD}, {@code STORE} or {@code null} (any operation). */
  private final MemoryOperation operation;
  /** Device (buffer). */
  private final MmuBuffer buffer;
  /** Event: {@code HIT} or {@code MISS}. */
  private final BufferAccessEvent event;
  /** Logical condition. */
  private final MmuCondition condition;
  /** Admissible memory regions. */
  private final Collection<String> regions;
  /** Admissible memory segments. */
  private final Collection<MmuSegment> segments;
  
  public MmuGuard(
      final MemoryOperation operation,
      final MmuBuffer buffer,
      final BufferAccessEvent event,
      final MmuCondition condition,
      final Collection<String> regions,
      final Collection<MmuSegment> segments) {
    this.operation = operation;
    this.buffer = buffer;
    this.event = event;
    this.condition = condition;
    this.regions = regions;
    this.segments = segments;
  }

  public MmuGuard(
      final MmuBuffer buffer,
      final BufferAccessEvent event,
      final MmuCondition condition) {
    this(null, buffer, event, condition, null, null);
  }

  public MmuGuard(final MmuBuffer buffer, final BufferAccessEvent event) {
    this(null, buffer, event, null, null, null);
  }

  public MmuGuard(final MmuCondition condition) {
    this(null, null, null, condition, null, null);
  }

  public MmuGuard(final MemoryOperation operation, final MmuCondition condition) {
    this(operation, null, null, condition, null, null);
  }

  public MmuGuard(final MemoryOperation operation) {
    this(operation, null, null, null, null, null);
  }

  public MmuGuard(final Collection<String> regions, final Collection<MmuSegment> segments) {
    this(null, null, null, null, regions, segments);
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public MemoryOperation getOperation() {
    return operation;
  }

  public BufferAccessEvent getEvent() {
    return event;
  }

  public MmuCondition getCondition() {
    return condition;
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

    if (buffer != null) {
      builder.append(builder.length() > 0 ? separator : "");
      builder.append(String.format("%s.Event=%s", buffer, event));
    }

    if (condition != null) {
      for (final MmuConditionAtom equality : condition.getAtoms()) {
        builder.append(builder.length() > 0 ? separator : "");
        builder.append(equality);
      }
    }

    return builder.toString();
  }
}
