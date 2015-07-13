/*
 * Copyright 2006-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * {@link AddressObject} represents test data for an individual {@link MemoryAccess}.
 * 
 * <p>Test data include addresses (virtual and physical ones), auxiliary attributes (cache policy,
 * control bits, etc.), sequences of addresses to be accessed to prepare hit/miss situations and
 * sets of entries to be written into the devices (buffers).</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressObject {

  /**
   * Refers to the MMU specification.
   *
   * <p>It is used to initialize the auxiliary data structures and to build a string representation
   * of the test data (to print tags and indices for all relevant devices).</p>
   */
  private final MmuSubsystem memory;

  /** Refers to the instruction call (execution). */
  private final MemoryAccess execution;

  /** Contains addresses (virtual, physical and intermediate addresses). */
  private final Map<MmuAddress, Long> addresses = new LinkedHashMap<>();

  /** Contains attributes associated with the instruction call (cache policy, etc.). */
  private final Map<IntegerVariable, Long> attributes = new LinkedHashMap<>();

  /**
   * Contains entries to be written into the devices (buffers).
   * 
   * <p>Typically, one instruction call (execution) affects one entry of one device. It is assumed
   * that each map contains exactly one entry.</p>
   */
  private final Map<MmuDevice, Map<Long, Object>> entries = new LinkedHashMap<>();

  /**
   * Constructs uninitialized test data for the given execution of the given memory subsystem.
   * 
   * @param memory the MMU specification.
   * @param execution the execution under processing.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public AddressObject(final MmuSubsystem memory, final MemoryAccess execution) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(execution);

    this.memory = memory;
    this.execution = execution;
  }

  /**
   * Returns the operation type.
   * 
   * @return {@code MemoryOperation#LOAD} or {@code MemoryOperation#STORE}.
   */
  public MemoryOperation getOperation() {
    return execution.getOperation();
  }

  /**
   * Returns the data type (size of a data block being loaded or stored).
   * 
   * @return {@link DataType#BYTE}, {@link DataType#HWORD}, {@link DataType#WORD}, or
   *         {@link DataType#DWORD}.
   */
  public DataType getDataType() {
    return execution.getDataType();
  }

  /**
   * Returns the value of the given attribute.
   * 
   * @param variable the attribute (variable).
   * @return the attribute value.
   * @throws IllegalArgumentException if {@code variable} is null.
   */
  public Long getAttrValue(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);
    return attributes.get(variable);
  }

  /**
   * Sets the value of the given attribute.
   * 
   * @param variable the attribute (variable).
   * @param value the value to be set.
   * @throws IllegalArgumentException if {@code variable} is null.
   */
  public void setAttrValue(final IntegerVariable variable, final long value) {
    InvariantChecks.checkNotNull(variable);
    attributes.put(variable, value);
  }

  /**
   * Returns the address of the given type.
   * 
   * @param addressType the address type.
   * @return the address value.
   * @throws IllegalArgumentException if {@code addressType} is null.
   */
  public long getAddress(final MmuAddress addressType) {
    InvariantChecks.checkNotNull(addressType);
    return addresses.get(addressType); 
  }

  /**
   * Returns the address map, which maps address types to address values.
   * 
   * @return the address map.
   */
  public Map<MmuAddress, Long> getAddresses() {
    return addresses; 
  }

  /**
   * Sets the address of the given type.
   * 
   * @param addressType the address type.
   * @param value the address value.
   * @throws IllegalArgumentException if {@code addressType} is null.
   */
  public void setAddress(final MmuAddress addressType, final long value) {
    InvariantChecks.checkNotNull(addressType);
    addresses.put(addressType, value);
  }

  /**
   * Returns the entries to be written to the given device.
   * 
   * @param device the device to be prepared.
   * @return the entries to written.
   * @throws IllegalArgumentException if {@code device} is null.
   */
  public Map<Long, Object> getEntries(final MmuDevice device) {
    InvariantChecks.checkNotNull(device);
    return entries.get(device);
  }

  /**
   * Sets the entries to be written to the given device.
   * 
   * @param device the device to be prepared.
   * @param entries the entries to be written.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void setEntries(final MmuDevice device, final Map<Long, Object> entries) {
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(entries);
    InvariantChecks.checkTrue(entries.size() == 1);

    this.entries.put(device, entries);
  }

  /**
   * Adds the entry to the set of entries to be written to the given device.
   * 
   * @param device the device to be prepared.
   * @param entryId the internal address of the entry.
   * @param entry the entry to be added.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void addEntry(final MmuDevice device, final long entryId, final Object entry) {
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(entry);

    Map<Long, Object> deviceEntries = entries.get(device);

    if (deviceEntries == null) {
      entries.put(device, deviceEntries = new LinkedHashMap<>());
    }

    deviceEntries.put(entryId, entry);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    for (final Map.Entry<MmuAddress, Long> addrEntry : addresses.entrySet()) {
      final MmuAddress addrType = addrEntry.getKey();
      final long addrValue = addrEntry.getValue();

      builder.append(comma ? separator : "");
      builder.append(String.format("%s=%x", addrType.getVariable().getName(), addrValue));
      comma = true;

      for (final MmuDevice device : memory.getDevices()) {
        if (device.getAddress() == addrType) {
          if (device.isReplaceable()) {
            builder.append(comma ? separator : "");
            builder.append(String.format("%s.Tag=%x", device, device.getTag(addrValue)));
            comma = true;
          }

          if (device.getSets() > 1) {
            builder.append(comma ? separator : "");
            builder.append(String.format("%s.Index=%x", device, device.getIndex(addrValue)));
            comma = true;
          }
        }
      }
    }

    for (final Map.Entry<IntegerVariable, Long> attrEntry : attributes.entrySet()) {
      final IntegerVariable attrKey = attrEntry.getKey();
      final Long attrValue = attrEntry.getValue();

      builder.append(comma ? separator : "");
      builder.append(String.format("%s=%x", attrKey.getName(), attrValue));
      comma = true;
    }

    return builder.toString();
  }
}
