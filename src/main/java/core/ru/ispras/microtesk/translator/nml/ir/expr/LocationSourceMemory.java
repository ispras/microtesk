/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.expr;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Memory;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class LocationSourceMemory implements LocationSource {
  private final MemoryExpr memory;

  protected LocationSourceMemory(final MemoryExpr memory) {
    InvariantChecks.checkNotNull(memory);
    this.memory = memory;
  }

  @Override
  public NmlSymbolKind getSymbolKind() {
    return NmlSymbolKind.MEMORY;
  }

  public Memory.Kind getKind() {
    return memory.getKind();
  }

  @Override
  public Type getType() {
    return memory.getType();
  }

  public MemoryExpr getMemory() {
    return memory;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    
    if (getClass() != obj.getClass()) {
      return false;
    }

    final LocationSourceMemory other = (LocationSourceMemory) obj;
    return memory == other.memory;
  }
}
