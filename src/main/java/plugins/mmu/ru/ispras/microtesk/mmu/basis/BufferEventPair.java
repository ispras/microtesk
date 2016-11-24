/*
 * Copyright 2006-2016 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * {@link BufferEventPair} represents a buffer-event pair.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferEventPair {
  private MmuBuffer buffer;
  private BufferAccessEvent event;

  public BufferEventPair(final MmuBuffer buffer, final BufferAccessEvent event) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(event);

    this.buffer = buffer;
    this.event = event;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public BufferAccessEvent getEvent() {
    return event;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof BufferEventPair)) {
      return false;
    }

    final BufferEventPair r = (BufferEventPair) o;

    return buffer.equals(r.buffer) && event.equals(r.event);
  }

  @Override
  public int hashCode() {
    return 31 * buffer.hashCode() + event.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", buffer, event);
  }
}
