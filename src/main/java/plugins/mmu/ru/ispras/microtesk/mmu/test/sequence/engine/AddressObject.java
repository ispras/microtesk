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
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * {@link AddressObject} represents test data for an individual {@link MemoryAccess}.
 * 
 * <p>Test data include addresses (virtual and physical ones), auxiliary attributes (cache policy,
 * control bits, etc.), sequences of addresses to be accessed to prepare hit/miss situations and
 * sets of entries to be written into the buffers.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressObject {
  /**
   * Refers to the MMU specification.
   *
   * <p>It is used to initialize the auxiliary data structures and to build a string representation
   * of the test data (to print tags and indices for all relevant buffers).</p>
   */
  private final MmuSubsystem memory = MmuTranslator.getSpecification();

  /** Contains addresses (virtual, physical and intermediate addresses). */
  private final Map<MmuAddressType, Long> addresses = new LinkedHashMap<>();

  /** Contains attributes associated with the instruction call (cache policy, etc.). */
  private final Map<IntegerVariable, Long> attributes = new LinkedHashMap<>();

  /**
   * Contains entries to be written into the buffers.
   * 
   * <p>Typically, one memory access affects one entry of one buffer. It is assumed that each map
   * contains exactly one entry.</p>
   */
  private final Map<MmuBuffer, Map<Long, MmuEntry>> entries = new LinkedHashMap<>();

  /** Refers to the memory access. */
  private MemoryAccess access;

  /**
   * Constructs uninitialized test data for the given access of the given memory subsystem.
   * 
   * @param memory the MMU specification.
   * @param access the memory access.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public AddressObject(final MemoryAccess access) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(access);

    this.access = access;
  }

  public MemoryAccessType getType() {
    return access.getType();
  }

  public MemoryAccessPath getPath() {
    return access.getPath();
  }

  public void setPath(final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    final MemoryAccess access = new MemoryAccess(
        this.access.getType(), path, this.access.getRegion(), this.access.getSegment());

    this.access = access;
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

  public void clearAttrs() {
    attributes.clear();
  }

  /**
   * Returns the address of the given type.
   * 
   * @param addressType the address type.
   * @return the address value.
   * @throws IllegalArgumentException if {@code addressType} is null.
   */
  public long getAddress(final MmuAddressType addressType) {
    InvariantChecks.checkNotNull(addressType);
    return addresses.get(addressType); 
  }

  /**
   * Returns the address map, which maps address types to address values.
   * 
   * @return the address map.
   */
  public Map<MmuAddressType, Long> getAddresses() {
    return addresses; 
  }

  /**
   * Sets the address of the given type.
   * 
   * @param addressType the address type.
   * @param value the address value.
   * @throws IllegalArgumentException if {@code addressType} is null.
   */
  public void setAddress(final MmuAddressType addressType, final long value) {
    InvariantChecks.checkNotNull(addressType);
    addresses.put(addressType, value);
  }

  /**
   * Returns the entries to be written to the given buffer.
   * 
   * @param buffer the buffer to be prepared.
   * @return the entries to written.
   * @throws IllegalArgumentException if {@code buffer} is null.
   */
  public Map<Long, MmuEntry> getEntries(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);
    return entries.get(buffer);
  }

  /**
   * Sets the entries to be written to the given buffer.
   * 
   * @param buffer the buffer to be prepared.
   * @param entries the entries to be written.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void setEntries(final MmuBuffer buffer, final Map<Long, MmuEntry> entries) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(entries);
    InvariantChecks.checkTrue(entries.size() == 1);

    this.entries.put(buffer, entries);
  }

  /**
   * Adds the entry to the set of entries to be written to the given buffer.
   * 
   * @param buffer the buffer to be prepared.
   * @param entryId the internal address of the entry.
   * @param entry the entry to be added.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void addEntry(final MmuBuffer buffer, final long entryId, final MmuEntry entry) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(entry);

    Map<Long, MmuEntry> bufferEntries = entries.get(buffer);

    if (bufferEntries == null) {
      entries.put(buffer, bufferEntries = new LinkedHashMap<>());
    }

    bufferEntries.put(entryId, entry);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    for (final Map.Entry<MmuAddressType, Long> addrEntry : addresses.entrySet()) {
      final MmuAddressType addrType = addrEntry.getKey();
      final long addrValue = addrEntry.getValue();

      builder.append(comma ? separator : "");
      builder.append(String.format("%s=%x", addrType.getVariable().getName(), addrValue));
      comma = true;

      for (final MmuBuffer buffer : memory.getBuffers()) {
        if (buffer.getAddress() == addrType) {
          if (buffer.isReplaceable()) {
            builder.append(comma ? separator : "");
            builder.append(String.format("%s.tag=%x", buffer, buffer.getTag(addrValue)));
            comma = true;
          }

          if (buffer.getSets() > 1) {
            builder.append(comma ? separator : "");
            builder.append(String.format("%s.index=%x", buffer, buffer.getIndex(addrValue)));
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
