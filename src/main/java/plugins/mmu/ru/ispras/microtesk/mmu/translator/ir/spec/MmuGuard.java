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

import ru.ispras.microtesk.mmu.translator.ir.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.MemoryOperation;

/**
 * {@link MmuGuard} represents a guard, i.e. a transition activation condition.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuGuard {
  /** Operation: {@code LOAD}, {@code STORE} or {@code null} (any operation). */
  private final MemoryOperation operation;
  /** Device (buffer). */
  private final MmuDevice device;
  /** Event: {@code HIT} or {@code MISS}. */
  private final BufferAccessEvent event;
  /** Logical condition. */
  private final MmuCondition condition;

  public MmuGuard(
      final MemoryOperation operation,
      final MmuDevice device,
      final BufferAccessEvent event,
      final MmuCondition condition) {
    this.operation = operation;
    this.device = device;
    this.event = event;
    this.condition = condition;
  }

  public MmuGuard(
      final MmuDevice device,
      final BufferAccessEvent event,
      final MmuCondition condition) {
    this(null, device, event, condition);
  }

  public MmuGuard(final MmuDevice device, final BufferAccessEvent event) {
    this(null, device, event, null);
  }

  public MmuGuard(final MmuCondition condition) {
    this(null, null, null, condition);
  }

  public MmuGuard(final MemoryOperation operation, final MmuCondition condition) {
    this(operation, null, null, condition);
  }

  public MmuGuard(final MemoryOperation operation) {
    this(operation, null, null, null);
  }

  public MmuDevice getDevice() {
    return device;
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

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    if (operation != null) {
      builder.append(operation);
    }

    if (device != null) {
      builder.append(builder.length() > 0 ? separator : "");
      builder.append(String.format("%s.Event=%s", device, event));
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
