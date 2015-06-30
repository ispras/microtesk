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

package ru.ispras.microtesk.translator.mmu.ir.spec;

import ru.ispras.microtesk.translator.mmu.ir.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.translator.mmu.ir.spec.basis.MemoryOperation;

/**
 * {@link MmuGuard} describes a guard (transition activation condition).
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuGuard {
  /** The operation: {@code LOAD}, {@code STORE} or {@code null} (any operation). */
  private final MemoryOperation operation;
  /** The device (buffer). */
  private final MmuDevice device;
  /** The event: {@code HIT} or {@code MISS}. */
  private final BufferAccessEvent event;
  /** The condition (set of equalities). */
  private final MmuCondition condition;

  /**
   * Constructs a guard.
   * 
   * @param operation the operation.
   * @param device the device.
   * @param event the event.
   * @param condition the condition.
   */
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

  /**
   * Constructs a guard.
   * 
   * @param device the device.
   * @param event the event.
   * @param condition the condition.
   */
  public MmuGuard(
      final MmuDevice device,
      final BufferAccessEvent event,
      final MmuCondition condition) {
    this(null, device, event, condition);
  }

  /**
   * Constructs a guard.
   * 
   * @param device the device.
   * @param event the event.
   */
  public MmuGuard(final MmuDevice device, final BufferAccessEvent event) {
    this(null, device, event, null);
  }

  /**
   * Constructs a guard.
   * 
   * @param condition the condition.
   */
  public MmuGuard(final MmuCondition condition) {
    this(null, null, null, condition);
  }

  /**
   * Constructs a guard.
   * 
   * @param operation the operation.
   * @param condition the condition.
   */
  public MmuGuard(final MemoryOperation operation, final MmuCondition condition) {
    this(operation, null, null, condition);
  }

  /**
   * Constructs a guard.
   * 
   * @param operation the operation.
   */
  public MmuGuard(final MemoryOperation operation) {
    this(operation, null, null, null);
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
   * Returns the operation.
   * 
   * @return the operation.
   */
  public MemoryOperation getOperation() {
    return operation;
  }

  /**
   * Returns the event (does not make sense if {@code device == null}).
   * 
   * @return the event.
   */
  public BufferAccessEvent getEvent() {
    return event;
  }

  /**
   * Returns the condition.
   * 
   * @return the condition.
   */
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
      for (final MmuEquality equality : condition.getEqualities()) {
        builder.append(builder.length() > 0 ? separator : "");
        builder.append(equality);
      }
    }

    return builder.toString();
  }
}
