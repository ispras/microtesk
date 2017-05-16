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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;

/**
 * {@link MemorySolution} represents a solution (test data) for a number of dependent instruction
 * calls (memory access structure).
 * 
 * <p>Solution includes test data for individual memory accesses (see {@link AddressObject}) and
 * a set of entries to be written into the buffers.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySolution {

  /** Contains the memory access structure. */
  private final List<MemoryAccess> structure;
  
  /** Contains test data for individual executions. */
  private final List<AddressObject> solution;

  /**
   * Contains entries to be written into the buffers to prepare hit/miss situations.
   * 
   * <p>This map unites the analogous maps of the test data of the executions stored in
   * {@link MemorySolution#solution}.</p>
   */
  private final Map<MmuBufferAccess, Map<BigInteger, EntryObject>> entries = new LinkedHashMap<>();

  /**
   * Constructs an uninitialized solution for the given memory access structure.
   * 
   * @param structure the memory access structure.
   */
  public MemorySolution(final List<MemoryAccess> structure) {
    InvariantChecks.checkNotNull(structure);

    this.structure = structure;
    this.solution = new ArrayList<>(structure.size());

    for (int i = 0; i < structure.size(); i++) {
      final MemoryAccess access = structure.get(i);
      final MemoryAccessPath path = access.getPath();

      this.solution.add(new AddressObject(access));

      for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
        if (!this.entries.containsKey(bufferAccess)) {
          this.entries.put(bufferAccess, new LinkedHashMap<BigInteger, EntryObject>());
        }
      }
    }

  }

  /**
   * Returns the number of executions in the memory access structure.
   * 
   * @return the memory access structure size.
   */
  public int size() {
    return solution.size();
  }

  public List<MemoryAccess> getStructure() {
    return structure;
  }

  /**
   * Returns the test data for the i-th execution.
   * 
   * @param i the execution index.
   * @return the test data.
   * @throws IndexOutOfBoundsException if {@code i} is out of bounds.
   */
  public AddressObject getAddressObject(final int i) {
    InvariantChecks.checkBounds(i, solution.size());

    return solution.get(i);
  }

  /**
   * Returns the test data for all executions.
   * 
   * @return the list of test data.
   */
  public List<AddressObject> getAddressObjects() {
    return solution;
  }

  /**
   * Sets the test data for the i-th execution.
   * 
   * @param i the execution index.
   * @param testData the test data to be set.
   * @throws IndexOutOfBoundsException if {@code i} is out of bounds.
   */
  public void setAddressObject(final int i, final AddressObject testData) {
    InvariantChecks.checkBounds(i, solution.size());
    InvariantChecks.checkNotNull(testData);

    solution.set(i, testData);
  }

  /**
   * Returns the entries to written.
   * 
   * @return the entries.
   */
  public Map<MmuBufferAccess, Map<BigInteger, EntryObject>> getEntries() {
    return entries;
  }

  /**
   * Returns the entries to written to the given buffer.
   * 
   * @param bufferAccess the buffer access.
   * @return the index-to-entry map.
   * @throws IllegalArgumentException if {@code device} is null.
   */
  public Map<BigInteger, EntryObject> getEntries(final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(bufferAccess);
    return entries.get(bufferAccess);
  }

  /**
   * Sets the entries to be written to the given buffer.
   * 
   * @param bufferAccess the buffer access.
   * @param entries the entries to be written.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void setEntries(
      final MmuBufferAccess bufferAccess,
      final Map<BigInteger, EntryObject> entries) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(entries);

    this.entries.put(bufferAccess, entries);
  }

  /**
   * Adds the entry to the set of entries to be written to the given buffer.
   * 
   * @param bufferAccess the buffer access.
   * @param entry the entry to be added.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void addEntry(final MmuBufferAccess bufferAccess, final EntryObject entry) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(entry);

    entries.get(bufferAccess).put(entry.getId(), entry);
  }
}
