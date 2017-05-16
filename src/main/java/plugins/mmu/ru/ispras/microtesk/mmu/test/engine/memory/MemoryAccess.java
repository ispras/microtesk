/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.engine.memory.symbolic.MemorySymbolicResult;
import ru.ispras.microtesk.mmu.test.template.MemoryAccessConstraints;

/**
 * {@link MemoryAccess} describes an execution path of a memory access instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccess {
  private final MemoryAccessType type;
  private final MemoryAccessPath path;

  private final MemoryAccessConstraints constraints;

  private BufferDependency[] dependencies;

  /** Symbolic representation of the memory access. */
  private MemorySymbolicResult symbolicResult; 

  public MemoryAccess(
      final MemoryAccessType type,
      final MemoryAccessPath path,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    this.type = type;
    this.path = path;
    this.constraints = constraints;
  }

  public MemoryAccessType getType() {
    return type;
  }

  public MemoryAccessPath getPath() {
    return path;
  }

  public MemoryAccessConstraints getConstraints() {
    return constraints;
  }

  /**
   * Returns the dependency of this memory access from the {@code i}-th memory access.
   * 
   * @param i the index of the primary memory access.
   * @return the dependency.
   */
  public BufferDependency getDependency(final int i) {
    InvariantChecks.checkBounds(i, dependencies.length);
    return dependencies[i];
  }

  public BufferDependency[] getDependencies() {
    return dependencies;
  }

  public void setDependencies(final BufferDependency[] dependencies) {
    this.dependencies = dependencies;
  }

  /**
   * Returns the united dependency of the {@code j}-th memory access on the previous accesses.
   * 
   * @return the united dependency.
   */
  public BufferUnitedDependency getUnitedDependency() {
    final Map<BufferDependency, Integer> result = new LinkedHashMap<>();

    for (int i = 0; i < dependencies.length; i++) {
      final BufferDependency dependency = dependencies[i];

      if (dependency != null) {
        result.put(dependency, i);
      }
    }

    return new BufferUnitedDependency(result);
  }

  public boolean hasSymbolicResult() {
    return symbolicResult != null;
  }

  public MemorySymbolicResult getSymbolicResult() {
    return symbolicResult;
  }

  public void setSymbolicResult(final MemorySymbolicResult symbolicResult) {
    this.symbolicResult = symbolicResult;
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    builder.append("[");

    builder.append("Access: ");
    builder.append(String.format("%s, %s", type, path));

    if (dependencies != null) {
      builder.append(", ");
      builder.append("Dependencies: ");

      boolean comma = false;
      for (int i = 0; i < dependencies.length; i++) {
        if (comma) {
          builder.append(separator);
        }
        builder.append(String.format("[%d]=%s", i, dependencies[i]));
        comma = true;
      }
    }

    builder.append("]");

    return builder.toString();
  }
}
