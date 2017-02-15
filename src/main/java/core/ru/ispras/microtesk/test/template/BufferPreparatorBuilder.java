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

package ru.ispras.microtesk.test.template;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link BufferPreparatorBuilder} class is responsible for construction
 * of buffer preparators.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class BufferPreparatorBuilder {
  private final String bufferId;
  private final BufferPreparatorAddressEntry addressEntry;
  private final List<Call> calls;

  protected BufferPreparatorBuilder(final String bufferId) {
    InvariantChecks.checkNotNull(bufferId);

    this.bufferId = bufferId;
    this.addressEntry = new BufferPreparatorAddressEntry();
    this.calls = new ArrayList<>();
  }

  private BufferPreparatorAddressEntry getAddressEntry(final int level) {
    return addressEntry;
  }

  public String getBufferId() {
    return bufferId;
  }

  public LazyValue newAddressReference() {
    return getAddressEntry(0).newAddressReference();
  }

  public LazyValue newAddressReference(final int start, final int end) {
    return getAddressEntry(0).newAddressReference(start, end);
  }

  public LazyValue newEntryReference() {
    return getAddressEntry(0).newEntryReference();
  }

  public LazyValue newEntryReference(final int start, final int end) {
    return getAddressEntry(0).newEntryReference(start, end);
  }

  public LazyValue newEntryFieldReference(final String fieldId) {
    return getAddressEntry(0).newEntryFieldReference(fieldId);
  }

  public LazyValue newEntryFieldReference(
      final String fieldId,
      final int start,
      final int end) {
    return getAddressEntry(0).newEntryFieldReference(fieldId, start, end);
  }

  public void addCall(final Call call) {
    InvariantChecks.checkNotNull(call);
    calls.add(call);
  }

  public BufferPreparator build() {
    return new BufferPreparator(bufferId, addressEntry, calls);
  }
}
