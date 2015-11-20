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

package ru.ispras.microtesk.test.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

public final class BufferPreparatorBuilder {
  private final String bufferId;
  private final LazyData address;
  private final Map<String, LazyData> entry;
  private final LazyData entryData;
  private final List<Call> calls;

  protected BufferPreparatorBuilder(final String bufferId) {
    InvariantChecks.checkNotNull(bufferId);

    this.bufferId = bufferId;
    this.address = new LazyData();
    this.entry = new HashMap<>();
    this.entryData = new LazyData();
    this.calls = new ArrayList<>();
  }

  public String getBufferId() {
    return bufferId;
  }

  public LazyValue newAddressReference() {
    return new LazyValue(address);
  }

  public LazyValue newAddressReference(final int start, final int end) {
    return new LazyValue(address, start, end);
  }

  public LazyValue newEntryReference() {
    return new LazyValue(entryData);
  }

  public LazyValue newEntryReference(final int start, final int end) {
    return new LazyValue(entryData, start, end);
  }

  public LazyValue newEntryFieldReference(final String fieldId) {
    InvariantChecks.checkNotNull(fieldId);

    final LazyData field = getEntryField(fieldId);
    return new LazyValue(field);
  }

  public LazyValue newEntryFieldReference(
      final String fieldId, final int start, final int end) {
    InvariantChecks.checkNotNull(fieldId);

    final LazyData field = getEntryField(fieldId);
    return new LazyValue(field, start, end);
  }

  private LazyData getEntryField(final String fieldId) {
    LazyData field = entry.get(fieldId);

    if (null == field) {
      field = new LazyData();
      entry.put(fieldId, field);
    }

    return field;
  }

  public void addCall(final Call call) {
    InvariantChecks.checkNotNull(call);
    calls.add(call);
  }

  public BufferPreparator build() {
    return new BufferPreparator(
        bufferId,
        address,
        entry,
        entryData,
        calls
        );
  }
}
