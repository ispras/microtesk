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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic.MemorySymbolicResult;

/**
 * {@link MemoryAccess} describes an execution path of a memory access instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccess {
  private final MemoryAccessType type;
  private final MemoryAccessPath path;

  /** Symbolic representation of the memory access. */
  private MemorySymbolicResult symbolicResult; 

  public MemoryAccess(
      final MemoryAccessType type,
      final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(path);

    this.type = type;
    this.path = path;
  }

  public MemoryAccessType getType() {
    return type;
  }

  public MemoryAccessPath getPath() {
    return path;
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
    return String.format("%s, %s", type, path);
  }
}
