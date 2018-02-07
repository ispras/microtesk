/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.template.AccessConstraints;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link Access} describes an execution path of a memory access instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Access {
  public static final Access NONE =
      new Access(
          MemoryAccessType.NONE,
          AccessPath.EMPTY,
          AccessConstraints.EMPTY
      );

  private final MemoryAccessType type;
  private final AccessPath path;
  private final AccessConstraints constraints;
  private final Map<Integer, BufferDependency> dependencies;

  /** Symbolic representation of the memory access. */
  private SymbolicResult symbolicResult; 

  public Access(
      final MemoryAccessType type,
      final AccessPath path,
      final AccessConstraints constraints) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    this.type = type;
    this.path = path;
    this.constraints = constraints;
    this.dependencies = new LinkedHashMap<>();
    this.symbolicResult = null;
  }

  public Access(final Access other) {
    this(other.type, other.path, other.constraints);
  }

  public MemoryAccessType getType() {
    return type;
  }

  public AccessPath getPath() {
    return path;
  }

  public AccessConstraints getConstraints() {
    return constraints;
  }

  public boolean hasDependencies() {
    return !dependencies.isEmpty();
  }

  public BufferDependency getDependency(final int i) {
    return dependencies.get(i);
  }

  public void setDependency(final int i, final BufferDependency dependency) {
    InvariantChecks.checkNotNull(dependency);
    dependencies.put(i, dependency);
  }

  public void clearDependencies() {
    dependencies.clear();
  }

  public BufferUnitedDependency getUnitedDependency() {
    final Map<BufferDependency, Integer> result = new LinkedHashMap<>();

    for (final Map.Entry<Integer, BufferDependency> entry : dependencies.entrySet()) {
      result.put(entry.getValue(), entry.getKey());
    }

    return new BufferUnitedDependency(result);
  }

  public boolean hasSymbolicResult() {
    return symbolicResult != null;
  }

  public SymbolicResult getSymbolicResult() {
    return symbolicResult;
  }

  public void setSymbolicResult(final SymbolicResult symbolicResult) {
    this.symbolicResult = symbolicResult;
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    builder.append("[");

    builder.append("Access: ");
    builder.append(String.format("%s, %s", type, path));

    builder.append(", ");
    builder.append("Dependencies: ");

    boolean comma = false;
    for (final Map.Entry<Integer, BufferDependency> entry : dependencies.entrySet()) {
      if (comma) {
        builder.append(separator);
      }
      builder.append(String.format("[%d]=%s", entry.getKey(), entry.getValue()));
      comma = true;
    }

    builder.append("]");

    return builder.toString();
  }
}
