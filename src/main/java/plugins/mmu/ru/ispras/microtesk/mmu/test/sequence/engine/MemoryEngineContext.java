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
import ru.ispras.microtesk.utils.function.BiConsumer;
import ru.ispras.microtesk.utils.function.Function;
import ru.ispras.microtesk.utils.function.Supplier;
import ru.ispras.microtesk.utils.function.TriConsumer;
import ru.ispras.microtesk.utils.function.UnaryOperator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryEngineContext} stores information required by an {@link MemoryEngine}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngineContext {
  //TODO: iteration with MMU TestGen
  private final Iterator<MemoryAccessStructure> structureIterator;

  private final Function<MemoryAccess, AddressObject> addrObjectConstructors;
  private final BiConsumer<MemoryAccess, AddressObject> addrObjectCorrectors;
  private final Map<MmuBuffer, UnaryOperator<Long>> addrAllocators;
  private final Map<MmuBuffer, Supplier<Object>> entryConstructors;
  private final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, Object>> entryProviders;

  public MemoryEngineContext(
      final Iterator<MemoryAccessStructure> structureIterator,
      final Function<MemoryAccess, AddressObject> addrObjectConstructors,
      final BiConsumer<MemoryAccess, AddressObject> addrObjectCorrectors,
      final Map<MmuBuffer, UnaryOperator<Long>> addrAllocators,
      final Map<MmuBuffer, Supplier<Object>> entryConstructors,
      final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, Object>> entryProviders) {
    InvariantChecks.checkNotNull(addrObjectConstructors);
    InvariantChecks.checkNotNull(addrObjectCorrectors);
    InvariantChecks.checkNotNull(addrAllocators);
    InvariantChecks.checkNotNull(entryConstructors);
    InvariantChecks.checkNotNull(entryProviders);

    this.structureIterator = structureIterator;
    this.addrObjectConstructors = addrObjectConstructors;
    this.addrObjectCorrectors = addrObjectCorrectors;
    this.addrAllocators = addrAllocators;
    this.entryConstructors = entryConstructors;
    this.entryProviders = entryProviders;
  }

  public Iterator<MemoryAccessStructure> getStructureIterator() {
    return structureIterator;
  }

  public Function<MemoryAccess, AddressObject> getAddrObjectConstructors() {
    return addrObjectConstructors;
  }

  public BiConsumer<MemoryAccess, AddressObject> getAddrObjectCorrectors() {
    return addrObjectCorrectors;
  }

  public Map<MmuBuffer, UnaryOperator<Long>> getAddrAllocators() {
    return addrAllocators;
  }

  public Map<MmuBuffer, Supplier<Object>> getEntryConstructors() {
    return entryConstructors;
  }

  public Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, Object>> getEntryProviders() {
    return entryProviders;
  }
}
