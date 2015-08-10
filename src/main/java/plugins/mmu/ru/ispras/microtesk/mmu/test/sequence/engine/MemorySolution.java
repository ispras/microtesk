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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.MemoryLoader;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;

/**
 * {@link MemorySolution} represents a solution (test data) for a number of dependent instruction
 * calls (memory access structure).
 * 
 * <p>Solution includes test data for individual memory accesses (see {@link AddressObject}) and
 * a set of entries to be written into the devices (buffers).</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySolution {

  /** Contains the memory access structure. */
  private final MemoryAccessStructure structure;
  
  /** Contains test data for individual executions. */
  private final List<AddressObject> solution;

  /** Contains addresses to be accessed to prepare hit/miss situations. */
  private final MemoryLoader loader;

  /**
   * Contains entries to be written into the devices to prepare hit/miss situations.
   * 
   * <p>This map unites the analogous maps of the test data of the executions stored in
   * {@link MemorySolution#solution}.</p>
   */
  private final Map<MmuBuffer, Map<Long, MmuEntry>> entries = new LinkedHashMap<>();

  /**
   * Constructs an uninitialized solution for the given memory access structure.
   * 
   * @param structure the memory access structure.
   */
  public MemorySolution(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);

    this.structure = structure;

    this.solution = new ArrayList<>(structure.size());
    for (int i = 0; i < structure.size(); i++) {
      final MemoryAccess execution = structure.getAccess(i);

      this.solution.add(new AddressObject(execution));
    }

    for (final MmuBuffer device : MmuTranslator.getSpecification().getDevices()) {
      this.entries.put(device, new LinkedHashMap<Long, MmuEntry>());
    }

    loader = new MemoryLoader();
  }

  /**
   * Returns the number of executions in the memory access structure.
   * 
   * @return the memory access structure size.
   */
  public int size() {
    return solution.size();
  }

  public MemoryAccessStructure getStructure() {
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
   * Returns the memory loader.
   * 
   * @return the memory loader.
   */
  public MemoryLoader getLoader() {
    return loader;
  }

  /**
   * Returns the entries to written to the given device.
   * 
   * @param device the MMU device (buffer).
   * @return the index-to-entry map.
   * @throws IllegalArgumentException if {@code device} is null.
   */
  public Map<Long, MmuEntry> getEntries(final MmuBuffer device) {
    InvariantChecks.checkNotNull(device);

    return entries.get(device);
  }

  /**
   * Sets the entries to be written to the given device.
   * 
   * @param device the MMU device (buffer).
   * @param entries the entries to be written.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void setEntries(final MmuBuffer device, final Map<Long, MmuEntry> entries) {
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(entries);

    this.entries.put(device, entries);
  }

  /**
   * Adds the entry to the set of entries to be written to the given device.
   * 
   * @param device the MMU device (buffer).
   * @param internalAddress the internal address of the entry (index).
   * @param entry the entry to be added.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public void addEntry(final MmuBuffer device, final long internalAddress, final MmuEntry entry) {
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(internalAddress);
    InvariantChecks.checkNotNull(entry);

    entries.get(device).put(internalAddress, entry);
  }
}
