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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.util.Collection;
import java.util.LinkedHashSet;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.Load;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;

/**
 * {@link EntryObject} represents information about a buffer entry.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EntryObject {
  private final long id;
  private final MmuEntry entry;

  /** Address objects that use this entry. */
  private final Collection<AddressObject> addrObjects = new LinkedHashSet<>();

  /** Auxiliary loads that use this entry. */
  private final Collection<Load> loads = new LinkedHashSet<>();

  public EntryObject(final long id, final MmuEntry entry) {
    InvariantChecks.checkNotNull(entry);

    this.id = id;
    this.entry = entry;
  }

  public long getId() {
    return id;
  }

  public MmuEntry getEntry() {
    return entry;
  }

  public Collection<AddressObject> getAddrObjects() {
    return addrObjects;
  }

  public Collection<Load> getLoads() {
    return loads;
  }

  public void addAddrObject(final AddressObject addrObject) {
    InvariantChecks.checkNotNull(addrObject);
    addrObjects.add(addrObject);
  }

  public void addLoad(final Load load) {
    InvariantChecks.checkNotNull(load);
    loads.add(load);
  }

  public boolean isAuxiliary() {
    return addrObjects.isEmpty();
  }
}
