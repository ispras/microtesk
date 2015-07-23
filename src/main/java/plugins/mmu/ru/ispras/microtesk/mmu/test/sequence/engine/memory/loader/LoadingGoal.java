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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

/**
 * {@link LoadingGoal} represents a sequence of loads to reach a certain buffer access event.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class LoadingGoal implements Loader {
  private final MmuBuffer buffer;

  private final BufferAccessEvent targetEvent;
  private final long targetAddress;

  private final List<Load> loads = new ArrayList<>();

  /**
   * Constructs a load sequence.
   * 
   * @param buffer the memory buffer.
   * @param targetEvent the event to be reached. 
   * @param targetAddress the target address.
   */
  public LoadingGoal(
      final MmuBuffer buffer,
      final BufferAccessEvent targetEvent,
      final long targetAddress) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(targetEvent);

    this.buffer = buffer;

    this.targetEvent = targetEvent;
    this.targetAddress = targetAddress;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public BufferAccessEvent getTargetEvent() {
    return targetEvent;
  }

  public long getTargetAddress() {
    return targetAddress;
  }

  /**
   * Appends the addresses to be accessed to reach the buffer access event specified in the
   * constructor.
   * 
   * @param addresses the addresses to be accessed.
   */
  public void addLoads(final List<Long> addresses) {
    InvariantChecks.checkNotNull(addresses);

    for (final long address : addresses) {
      loads.add(new Load(address));
    }
  }

  @Override
  public List<Load> prepareLoads() {
    return loads;
  }
}
