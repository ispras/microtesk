/*
 * Copyright 2006-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.model.spec.MmuEntry;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * {@link EntryObject} stores information about a buffer entry.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class EntryObject {
  private final BitVector id;
  private final MmuEntry entry;

  /** Address objects that use this entry. */
  private final Collection<AddressObject> addrObjects = new LinkedHashSet<>();

  public EntryObject(final BitVector id, final MmuEntry entry) {
    InvariantChecks.checkNotNull(entry);

    this.id = id;
    this.entry = entry;
  }

  public BitVector getId() {
    return id;
  }

  public MmuEntry getEntry() {
    return entry;
  }

  public Collection<AddressObject> getAddrObjects() {
    return addrObjects;
  }

  public void addAddrObject(final AddressObject addrObject) {
    InvariantChecks.checkNotNull(addrObject);
    addrObjects.add(addrObject);
  }

  public boolean isAuxiliary() {
    return addrObjects.isEmpty();
  }
}
