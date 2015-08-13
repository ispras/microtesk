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

package ru.ispras.microtesk.mmu.test.sequence.engine;

import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.utils.function.Function;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriConsumer;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryEngineContext} stores information required by an {@link MemoryEngine}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngineContext {
  // TODO: Integration with MMU TestGen.
  private final Iterator<MemoryAccessStructure> structureIterator;

  private final Function<MemoryAccess, AddressObject> addrObjectConstructor;
  private final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, MmuEntry>> entryProviders;
  private final Map<MmuBuffer, Predicate<Long>> hitCheckers;

  public MemoryEngineContext(
      final Iterator<MemoryAccessStructure> structureIterator,
      final Function<MemoryAccess, AddressObject> addrObjectConstructor,
      final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, MmuEntry>> entryProviders,
      final Map<MmuBuffer, Predicate<Long>> hitCheckers) {
    InvariantChecks.checkNotNull(addrObjectConstructor);
    InvariantChecks.checkNotNull(entryProviders);
    InvariantChecks.checkNotNull(hitCheckers);

    this.structureIterator = structureIterator;
    this.addrObjectConstructor = addrObjectConstructor;
    this.entryProviders = entryProviders;
    this.hitCheckers = hitCheckers;
  }

  public Iterator<MemoryAccessStructure> getStructureIterator() {
    return structureIterator;
  }

  public Function<MemoryAccess, AddressObject> getAddrObjectConstructor() {
    return addrObjectConstructor;
  }

  public Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, MmuEntry>> getEntryProviders() {
    return entryProviders;
  }

  public Map<MmuBuffer, Predicate<Long>> getHitCheckers() {
    return hitCheckers;
  }
}
