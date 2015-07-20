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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link MemoryAccess} represents the execution path of a memory access instruction.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccess {
  private final MemoryAccessType type;
  private final MemoryAccessPath path;

  public MemoryAccess(
      final MemoryAccessType accessType,
      final MemoryAccessPath accessPath) {
    InvariantChecks.checkNotNull(accessType);
    InvariantChecks.checkNotNull(accessPath);

    this.type = accessType;
    this.path = accessPath;
  }

  public MemoryAccessType getType() {
    return type;
  }

  public MemoryAccessPath getPath() {
    return path;
  }

  @Override
  public String toString() {
    return String.format("%s, %s", type, path);
  }
}
