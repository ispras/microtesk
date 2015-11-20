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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

public final class BufferPreparator {
  private final String bufferId;
  private final LazyData address;
  private final Map<String, LazyData> entry;
  private final LazyData entryData;
  private final List<Call> calls;

  protected BufferPreparator(
      final String bufferId,
      final LazyData address,
      final Map<String, LazyData> entry,
      final LazyData entryData,
      final List<Call> calls) {
    InvariantChecks.checkNotNull(bufferId);
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(entry);
    InvariantChecks.checkNotNull(calls);

    this.bufferId = bufferId;
    this.address = address;
    this.entry = Collections.unmodifiableMap(entry);
    this.entryData = entryData;
    this.calls = Collections.unmodifiableList(calls);
  }

  public String getBufferId() {
    return bufferId;
  }

  public List<Call> makeInitializer(
      final BitVector addressValue,
      final Map<String, BitVector> entryFieldValues) {
    InvariantChecks.checkNotNull(addressValue);
    InvariantChecks.checkNotNull(entryFieldValues);
    InvariantChecks.checkFalse(entryFieldValues.isEmpty(), "Entry is empty.");

    address.setValue(addressValue);

    final BitVector[] fieldValues = new BitVector[entry.size()];
    int fieldIndex = 0;
    for (final Map.Entry<String, LazyData> e : entry.entrySet()) {
      final String fieldId = e.getKey();
      final LazyData field = e.getValue();

      final BitVector fieldValue = entryFieldValues.get(fieldId);
      if (null == fieldValue) {
        throw new IllegalArgumentException(String.format(
            "No value for the %s entry field is provided.", fieldId));
      }

      field.setValue(fieldValue);

      fieldValues[fieldIndex] = fieldValue;
      fieldIndex++;
    }

    final BitVector entryValue = BitVector.newMapping(fieldValues);
    entryData.setValue(entryValue);

    return Call.newCopy(calls);
  }
}
