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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;

/**
 * The {@link BufferPreparator} describes instruction sequences to set up the state of MMU buffers.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class BufferPreparator {
  private final String bufferId;
  private final BufferPreparatorAddressEntry addressEntry;
  private final List<Call> calls;
  private final LabelUniqualizer.SeriesId labelSeriesId;

  protected BufferPreparator(
      final String bufferId,
      final BufferPreparatorAddressEntry addressAndEntry,
      final List<Call> calls) {
    InvariantChecks.checkNotNull(bufferId);
    InvariantChecks.checkNotNull(addressAndEntry);
    InvariantChecks.checkNotNull(calls);

    this.bufferId = bufferId;
    this.addressEntry = addressAndEntry;
    this.calls = Collections.unmodifiableList(calls);
    this.labelSeriesId = LabelUniqualizer.get().newSeries();
  }

  public String getBufferId() {
    return bufferId;
  }

  public List<Call> makeInitializer(
      final PreparatorStore preparators,
      final BitVector addressValue,
      final Map<String, BitVector> entryFieldValues) {
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(addressValue);
    InvariantChecks.checkNotNull(entryFieldValues);

    Logger.debug("Making a preparator for buffer %s", getBufferId());
    Logger.debug("Address: %s", addressValue);
    Logger.debug("Entry fields: %s", entryFieldValues);

    final LazyData address = addressEntry.getAddress();
    final Map<String, LazyData> entry = addressEntry.getEntry();
    final LazyData entryData = addressEntry.getEntryData();

    address.setValue(addressValue);

    for (final Map.Entry<String, LazyData> e : entry.entrySet()) {
      final String fieldId = e.getKey();
      final LazyData field = e.getValue();

      final BitVector fieldValue = entryFieldValues.get(fieldId);
      if (null == fieldValue) {
        throw new IllegalArgumentException(String.format(
            "No value for the %s entry field is provided.", fieldId));
      }

      field.setValue(fieldValue);
    }

    if (!entryFieldValues.isEmpty()) {
      final List<BitVector> fieldValues = new ArrayList<>(entryFieldValues.values());
      Collections.reverse(fieldValues);

      final BitVector entryValue =
          BitVector.newMapping(fieldValues.toArray(new BitVector[fieldValues.size()]));

      entryData.setValue(entryValue);

      Logger.debug("Entry data: %s", entryValue);
    }

    return Preparator.expandPreparators(labelSeriesId, preparators, calls);
  }
}
