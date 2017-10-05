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

package ru.ispras.microtesk.mmu.test.engine.memory;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link EntryTracker} tracks entries to be written to a memory-mapped buffer.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EntryTracker {
  private final BitVector bufferBaseAddress;

  private final Map<BitVector, EntryObject> entries = new LinkedHashMap<>();

  public EntryTracker(final BitVector bufferBaseAddress) {
    this.bufferBaseAddress = bufferBaseAddress;
  }

  public BitVector getEntryAddress(final EntryObject entry) {
    InvariantChecks.checkNotNull(entry);

    final BigInteger baseAddress = bufferBaseAddress.bigIntegerValue(false);
    final BigInteger entryId = entry.getId().bigIntegerValue(false);
    final BigInteger sizeInBytes = BigInteger.valueOf(entry.getEntry().getSizeInBits() >>> 3);

    return BitVector.valueOf(
        baseAddress.add(entryId.multiply(sizeInBytes)), bufferBaseAddress.getBitSize());
  }

  public boolean contains(final long id) {
    return entries.containsKey(id);
  }

  public void add(final EntryObject entry) {
    InvariantChecks.checkNotNull(entry);
    entries.put(entry.getId(), entry);
  }

  public int size() {
    return entries.size();
  }

  public Map<BitVector, EntryObject> getEntries() {
    return entries;
  }
}
