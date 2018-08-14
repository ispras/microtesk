/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import java.util.Collections;
import java.util.List;

/**
 * The {@link MemoryPreparator} describes instruction sequences to set up memory state.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MemoryPreparator {
  private final int dataSize;
  private final LazyData addressHolder;
  private final LazyData dataHolder;
  private final List<AbstractCall> calls;
  private final LabelUniqualizer.SeriesId labelSeriesId;

  protected MemoryPreparator(
      final int dataSize,
      final LazyData addressHolder,
      final LazyData dataHolder,
      final List<AbstractCall> calls) {
    InvariantChecks.checkGreaterThanZero(dataSize);
    InvariantChecks.checkNotNull(addressHolder);
    InvariantChecks.checkNotNull(dataHolder);
    InvariantChecks.checkNotNull(calls);

    this.dataSize = dataSize;
    this.addressHolder = addressHolder;
    this.dataHolder = dataHolder;
    this.calls = Collections.unmodifiableList(calls);
    this.labelSeriesId = LabelUniqualizer.get().newSeries();
  }

  public int getDataSize() {
    return dataSize;
  }

  public List<AbstractCall> makeInitializer(
      final PreparatorStore preparators,
      final BitVector addressValue,
      final BitVector dataValue) {
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(addressValue);
    InvariantChecks.checkNotNull(dataValue);

    Logger.debug("Making a memory preparator (size: %d)", dataSize);
    Logger.debug("Address: 0x%s", addressValue.toHexString());
    Logger.debug("Data: 0x%s", dataValue.toHexString());

    InvariantChecks.checkTrue(dataSize == dataValue.getBitSize());

    addressHolder.setValue(addressValue);
    dataHolder.setValue(dataValue);

    return Preparator.expandPreparators(labelSeriesId, preparators, calls);
  }
}
