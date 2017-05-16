/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link MemoryAccessStructure} describes a memory access structure, i.e. a sequence of memory
 * accesses (executions) linked with a number of memory-related dependencies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructure {
  /** Memory accesses (accesses). */
  private final List<MemoryAccess> accesses;
  
  public MemoryAccessStructure(
      final List<MemoryAccess> accesses,
      final BufferDependency[][] dependencies) {
    InvariantChecks.checkNotNull(accesses);
    InvariantChecks.checkNotNull(dependencies);
    InvariantChecks.checkTrue(dependencies.length == accesses.size());

    this.accesses = accesses;

    for (int j = 0; j < accesses.size(); j++) {
      final MemoryAccess access = accesses.get(j);
      access.setDependencies(dependencies[j]);
    }
  }

  public MemoryAccessStructure(
      final MemoryAccess access1,
      final MemoryAccess access2,
      final BufferDependency dependency) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);
    InvariantChecks.checkNotNull(dependency);

    this.accesses = new ArrayList<>();

    this.accesses.add(access1);
    this.accesses.add(access2);

    access2.setDependencies(new BufferDependency[] {dependency});
  }

  /**
   * Returns the number of memory accesses in the structure.
   * 
   * @return the size of the memory access structure.
   */
  public int size() {
    return accesses.size();
  }

  /**
   * Returns the {@code i}-th  memory access of the structure.
   * 
   * @param i the index of the memory access to be returned.
   * @return the memory access.
   */
  public MemoryAccess getAccess(final int i) {
    InvariantChecks.checkBounds(i, accesses.size());
    return accesses.get(i);
  }

  /**
   * Returns all memory accesses of the structure.
   * 
   * @return the memory accesses.
   */
  public List<MemoryAccess> getAccesses() {
    return accesses;
  }

  /**
   * Returns the dependency of the {@code j}-th memory access on the {@code i}-th memory access.
   * 
   * <p>There is a restriction: {@code i < j}.</p>
   * 
   * @param i the index of the primary memory access.
   * @param j the index of the secondary memory access.
   * @return the dependency between the memory accesses.
   */
  public BufferDependency getDependency(final int i, final int j) {
    InvariantChecks.checkTrue(i < j);
    InvariantChecks.checkBounds(j, accesses.size());

    return accesses.get(j).getDependency(i);
  }

  /**
   * Returns the united dependency of the {@code j}-th memory access on the previous accesses.
   * 
   * @param j the index of the memory access.
   * @return the united dependency.
   */
  public BufferUnitedDependency getUnitedDependency(final int j) {
    InvariantChecks.checkBounds(j, accesses.size());

    return accesses.get(j).getUnitedDependency();
  }

  @Override
  public String toString() {
    return accesses.toString();
  }
}
