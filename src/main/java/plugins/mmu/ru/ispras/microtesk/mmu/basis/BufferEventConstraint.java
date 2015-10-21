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

package ru.ispras.microtesk.mmu.basis;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * The {@link BufferEventConstraint} class describes constraints on buffer
 * access events.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class BufferEventConstraint {
  private final MmuBuffer buffer;
  private final Set<BufferAccessEvent> events;

  public BufferEventConstraint(
      final MmuBuffer buffer,
      final BufferAccessEvent event) {
    this(buffer, event != null ? EnumSet.of(event) : null);
  }

  public BufferEventConstraint(
      final MmuBuffer buffer,
      final Set<BufferAccessEvent> events) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotEmpty(events);

    this.buffer = buffer;
    this.events = Collections.unmodifiableSet(events);
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public Set<BufferAccessEvent> getEvents() {
    return events;
  }

  @Override
  public String toString() {
    return String.format(
        "BufferEventConstraint [buffer=%s, events=%s]", buffer, events);
  }
}
