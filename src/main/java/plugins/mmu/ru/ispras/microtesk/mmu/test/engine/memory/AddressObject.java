/*
 * Copyright 2006-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.model.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.model.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.model.spec.MmuSubsystem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link AddressObject} represents test data for a single memory access.
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
  private final Access access;

  /*** Contains the data values. */
  private final Map<NodeVariable, BitVector> data = new LinkedHashMap<>();

  /*** Contains the address values of the given memory access. */
  private final Map<MmuAddressInstance, BitVector> addresses = new LinkedHashMap<>();

  /** Contains entries to be written into non-transparent buffers. */
  private final Map<MmuBufferAccess, EntryObject> entries = new LinkedHashMap<>();

  public AddressObject(final Access access) {
    InvariantChecks.checkNotNull(access);
    this.access = access;
  }

  public Access getAccess() {
    return access;
  }

  public Map<NodeVariable, BitVector> getData() {
    return data;
  }

  public BitVector getData(final NodeVariable variable) {
    InvariantChecks.checkNotNull(variable);
    return data.get(variable);
  }

  public void setData(final NodeVariable variable, final BitVector value) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);
    data.put(variable, value);
  }

  public Map<MmuAddressInstance, BitVector> getAddresses() {
    return addresses;
  }

  public BitVector getAddress(final MmuAddressInstance addrType) {
    InvariantChecks.checkNotNull(addrType);
    return addresses.get(addrType);
  }

  public BitVector getAddress(final MmuBufferAccess bufferAccess) {
    return getAddress(bufferAccess.getAddress());
  }

  public void setAddress(final MmuAddressInstance addrType, final BitVector addrValue) {
    InvariantChecks.checkNotNull(addrType);
    InvariantChecks.checkNotNull(addrValue);
    addresses.put(addrType, addrValue);
  }

  public void setAddress(final MmuBufferAccess bufferAccess, final BitVector addrValue) {
    setAddress(bufferAccess.getAddress(), addrValue);
  }

  public Map<MmuBufferAccess, EntryObject> getEntries() {
    return entries;
  }

  public EntryObject getEntry(final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(bufferAccess);
    return entries.get(bufferAccess);
  }

  public void setEntry(final MmuBufferAccess bufferAccess, final EntryObject entry) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(entry);

    entry.addAddrObject(this);
    entries.put(bufferAccess, entry);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    final MmuSubsystem memory = MmuPlugin.getSpecification();
    final MmuAddressInstance addressType = memory.getVirtualAddress();
    final BitVector addressValue = addresses.get(addressType);
    InvariantChecks.checkNotNull(addressValue, addresses.toString());

    builder.append(String.format("%s[0x%s]", memory.getName(), addressValue.toHexString()));

    for (final MmuBufferAccess bufferAccess : access.getPath().getBufferReads()) {
      final MmuBuffer buffer = bufferAccess.getBuffer();
      final MmuAddressInstance type = bufferAccess.getAddress();
      final BitVector value = addresses.get(type);
      InvariantChecks.checkNotNull(value);

      builder.append(separator);
      builder.append(String.format("%s[0x%s]", buffer.getName(), value.toHexString()));

      if (!buffer.isReplaceable()) {
        final EntryObject entryObject = getEntry(bufferAccess);
        builder.append(String.format("=%s", entryObject.getEntry()));
      }
    }

    return builder.toString();
  }
}
