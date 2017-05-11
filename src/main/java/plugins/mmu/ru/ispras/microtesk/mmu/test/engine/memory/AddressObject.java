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

package ru.ispras.microtesk.mmu.test.engine.memory;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

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

  /*** Contains the data values. */
  private final Map<IntegerVariable, BigInteger> data = new LinkedHashMap<>();

  /*** Contains the address values of the given memory access. */
  private final Map<MmuAddressInstance, BigInteger> addresses = new LinkedHashMap<>();

  /** Contains entries to be written into non-transparent buffers. */
  private final Map<MmuBufferAccess, EntryObject> entries = new LinkedHashMap<>();

  public AddressObject(final MemoryAccess access) {
    InvariantChecks.checkNotNull(access);
    this.access = access;
  }

  public MemoryAccess getAccess() {
    return access;
  }

  public Map<IntegerVariable, BigInteger> getData() {
    return data; 
  }

  public BigInteger getData(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);
    return data.get(variable); 
  }

  public void setData(final IntegerVariable variable, final BigInteger value) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);
    data.put(variable, value);
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
    InvariantChecks.checkNotNull(addrValue);
    addresses.put(addrType, addrValue);
  }

  public BigInteger getAddress(final MmuBufferAccess bufferAccess) {
    return getAddress(bufferAccess.getAddress()); 
  }

  public void setAddress(final MmuBufferAccess bufferAccess, final BigInteger addrValue) {
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
    final MmuAddressInstance virtAddrType = memory.getVirtualAddress();
    final BigInteger virtAddrValue = addresses.get(virtAddrType);

    builder.append(String.format("%s[0x%s]", memory.getName(), virtAddrValue.toString(16)));

    for (final MmuBufferAccess bufferAccess : access.getPath().getBufferReads()) {
      final MmuBuffer buffer = bufferAccess.getBuffer();
      final MmuAddressInstance addrType = bufferAccess.getAddress();
      final BigInteger addrValue = addresses.get(addrType);

      builder.append(separator);
      builder.append(String.format("%s[0x%s]", buffer.getName(), addrValue.toString(16)));

      if (!buffer.isReplaceable()) {
        final EntryObject entryObject = getEntry(bufferAccess);
        builder.append(String.format("=%s", entryObject.getEntry()));
      }
    }

    return builder.toString();
  }
}
