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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * {@link MemoryAccessStructure} describes a memory access structure, i.e. a sequence of memory
 * accesses (executions) linked with a number of memory-related dependencies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructure {
  /** Memory subsystem specification. */
  private final MmuSubsystem memory;
  /** Memory accesses (accesses). */
  private final List<MemoryAccess> accesses;
  /** Dependencies between the memory accesses. */
  private final MemoryDependency[][] dependencies;

  public MemoryAccessStructure(
      final MmuSubsystem memory,
      final List<MemoryAccess> accesses,
      final MemoryDependency[][] dependencies) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(accesses);
    InvariantChecks.checkNotNull(dependencies);
    InvariantChecks.checkTrue(dependencies.length == accesses.size());

    this.memory = memory;
    this.accesses = accesses;
    this.dependencies = dependencies;
  }

  public MemoryAccessStructure(
      final MmuSubsystem memory,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final MemoryDependency dependency) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);
    InvariantChecks.checkNotNull(dependency);

    this.memory = memory;

    this.accesses = new ArrayList<>();
    this.accesses.add(access1);
    this.accesses.add(access2);

    this.dependencies = new MemoryDependency[2][2];
    this.dependencies[0][0] = null;
    this.dependencies[1][0] = dependency;
    this.dependencies[0][1] = null;
    this.dependencies[1][1] = null;
  }

  /**
   * Returns the memory subsystem specification.
   * 
   * @return the memory subsystem.
   */
  public MmuSubsystem getSubsystem() {
    return memory;
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
  public MemoryDependency getDependency(final int i, final int j) {
    InvariantChecks.checkTrue(i < j);
    InvariantChecks.checkBounds(j, dependencies.length);
    InvariantChecks.checkBounds(i, dependencies[j].length);

    return dependencies[j][i];
  }

  /**
   * Returns the dependencies between all memory accesses.
   * 
   * @return the dependencies between all memory accesses.
   */
  public MemoryDependency[][] getDependencies() {
    return dependencies;
  }

  /**
   * Returns the united dependency of the {@code j}-th memory access on the previous accesses.
   * 
   * @param j the index of the memory access.
   * @return the united dependency.
   */
  public MemoryUnitedDependency getUnitedDependency(final int j) {
    InvariantChecks.checkBounds(j, dependencies.length);

    final Map<MemoryDependency, Integer> dependencies = new LinkedHashMap<>();

    for (int i = 0; i < j; i++) {
      final MemoryDependency dependency = getDependency(i, j);

      if (dependency != null) {
        dependencies.put(dependency, i);
      }
    }

    return new MemoryUnitedDependency(dependencies);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append("Executions: ");
    builder.append(accesses.toString());

    builder.append(", ");
    builder.append("Dependencies:");

    boolean comma = false;
    for (int i = 0; i < dependencies.length; i++) {
      for (int j = 0; j < dependencies.length; j++) {
        if (comma) {
          builder.append(", ");
        }
        builder.append(String.format("[%d][%d]=%s", j, i, dependencies[j][i]));
        comma = true;
      }
    }

    return builder.toString();
  }
}
