/*
 * Copyright 2006-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;

/**
 * {@link AddressObject} represents test data for an individual {@link MemoryAccess}.
 * 
 * <p>
 * Test data include addresses (virtual, physical and intermediate ones), auxiliary attributes
 * (cache policy, control bits, etc.), sequences of addresses to be accessed to prepare hit/miss
 * situations, and sets of entries to be written into the buffers.
 * </p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressObject {
  private final MemoryAccess access;

  /*** Contains the address values of the given memory access. */
  private final Map<MmuAddressInstance, BigInteger> addresses = new LinkedHashMap<>();

  /**
   * Contains entries to be written into non-transparent buffers.
   * 
   * <p>
   * Typically, a single memory access uses one entry per buffer (one entry of a page table,
   * one entry of a TLB, etc.). Thus, each value is a singleton.
   * </p>
   */
  private final Map<MmuBufferAccess, Map<BigInteger, EntryObject>> entries = new LinkedHashMap<>();

  public AddressObject(final MemoryAccess access) {
    InvariantChecks.checkNotNull(access);
    this.access = access;
  }

  public MemoryAccess getAccess() {
    return access;
  }

  public Map<MmuAddressInstance, BigInteger> getAddresses() {
    return addresses; 
  }

  public BigInteger getAddress(final MmuAddressInstance addrType) {
    InvariantChecks.checkNotNull(addrType);
    return addresses.get(addrType); 
  }

  public void setAddress(final MmuAddressInstance addrType, final BigInteger addrValue) {
    InvariantChecks.checkNotNull(addrType);
    addresses.put(addrType, addrValue);
  }

  public BigInteger getAddress(final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(bufferAccess);
    return addresses.get(bufferAccess.getAddress()); 
  }

  public void setAddress(final MmuBufferAccess bufferAccess, final BigInteger addrValue) {
    InvariantChecks.checkNotNull(bufferAccess);
    addresses.put(bufferAccess.getAddress(), addrValue);
  }

  public Map<MmuBufferAccess, Map<BigInteger, EntryObject>> getEntries() {
    return entries;
  }

  public Map<BigInteger, EntryObject> getEntries(final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(bufferAccess);
    return entries.get(bufferAccess);
  }

  public void setEntries(
      final MmuBufferAccess bufferAccess,
      final Map<BigInteger, EntryObject> entries) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(entries);

    for (final EntryObject entry : entries.values()) {
      entry.addAddrObject(this);
    }

    this.entries.put(bufferAccess, entries);
  }

  public void addEntry(final MmuBufferAccess bufferAccess, final EntryObject entry) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(entry);

    Map<BigInteger, EntryObject> bufferEntries = entries.get(bufferAccess);
    if (bufferEntries == null) {
      entries.put(bufferAccess, bufferEntries = new LinkedHashMap<>());
    }

    entry.addAddrObject(this);
    bufferEntries.put(entry.getId(), entry);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    for (final Map.Entry<MmuAddressInstance, BigInteger> addrEntry : addresses.entrySet()) {
      final MmuAddressInstance addrType = addrEntry.getKey();
      final String addrName = addrType.getVariable().getName();
      final BigInteger addrValue = addrEntry.getValue();

      builder.append(comma ? separator : "");
      builder.append(String.format("%s=0x%s", addrName, addrValue.toString(16)));
      comma = true;

      for (final MmuBufferAccess bufferAccess : access.getPath().getBufferReads()) {
        final MmuBuffer buffer = bufferAccess.getBuffer();

        if (addrType.equals(bufferAccess.getAddress())) {
          if (buffer.isReplaceable()) {
            builder.append(comma ? separator : "");
            builder.append(String.format("%s.tag=0x%x", buffer, buffer.getTag(addrValue)));
            comma = true;
          }

          if (buffer.getSets() > 1) {
            builder.append(comma ? separator : "");
            builder.append(String.format("%s.index=0x%x", buffer, buffer.getIndex(addrValue)));
            comma = true;
          }
        }
      }
    }

    return builder.toString();
  }
}
