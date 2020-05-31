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

import ru.ispras.fortress.util.InvariantChecks;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link BufferPreparatorBuilder} class is responsible for construction
 * of buffer preparators.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class BufferPreparatorBuilder
    implements CodeBlockBuilder<BufferPreparator>, Addressable {
  private final String bufferId;
  private int levels;
  private final BufferPreparatorAddressEntry addressEntry;
  private final List<AbstractCall> calls;

  protected BufferPreparatorBuilder(final String bufferId) {
    InvariantChecks.checkNotNull(bufferId);

    this.bufferId = bufferId;
    this.levels = 0;
    this.addressEntry = new BufferPreparatorAddressEntry();
    this.calls = new ArrayList<>();
  }

  private BufferPreparatorAddressEntry getAddressEntry(final int level) {
    return addressEntry;
  }

  public String getBufferId() {
    return bufferId;
  }

  public void setLevels(final int value) {
    InvariantChecks.checkGreaterOrEqZero(value);
    levels = value;
  }

  @Override
  public LazyValue newAddressReference(final int level) {
    return getAddressEntry(level).newAddressReference();
  }

  @Override
  public LazyValue newAddressReference(final int level, final int start, final int end) {
    return getAddressEntry(level).newAddressReference(start, end);
  }

  public LazyValue newEntryReference(final int level) {
    return getAddressEntry(level).newEntryReference();
  }

  public LazyValue newEntryReference(final int level, final int start, final int end) {
    return getAddressEntry(level).newEntryReference(start, end);
  }

  public LazyValue newEntryFieldReference(final int level, final String fieldId) {
    return getAddressEntry(level).newEntryFieldReference(fieldId);
  }

  public LazyValue newEntryFieldReference(
      final int level,
      final String fieldId,
      final int start,
      final int end) {
    return getAddressEntry(level).newEntryFieldReference(fieldId, start, end);
  }

  @Override
  public void addCall(final AbstractCall call) {
    InvariantChecks.checkNotNull(call);
    calls.add(call);
  }

  @Override
  public BufferPreparator build() {
    return new BufferPreparator(bufferId, levels, addressEntry, calls);
  }
}
