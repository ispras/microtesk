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

package ru.ispras.microtesk.model.api.memory;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

final class PhysicalMemory extends Memory {
  private final MemoryStorage storage;

  public PhysicalMemory(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length) {
    super(kind, name, type, length, false);
    this.storage = new MemoryStorage(length, type.getBitSize()).setId(name);
  }

  @Override
  public Location access(final int index) {
    return access((long) index);
  }

  @Override
  public Location access(final long index) {
    final BitVector address = BitVector.valueOf(index, storage.getAddressBitSize());
    return Location.newLocationForRegion(getType(), storage, address);
  }

  @Override
  public Location access(final BigInteger index) {
    final BitVector address = BitVector.valueOf(index, storage.getAddressBitSize());
    return Location.newLocationForRegion(getType(), storage, address);
  }

  @Override
  public Location access(final Data address) {
    checkNotNull(address);
    return Location.newLocationForRegion(getType(), storage, address.getRawData());
  }

  @Override
  public void reset() {
    storage.reset();
  }

  @Override
  public final MemoryAllocator newAllocator(final int addressableUnitBitSize) {
    return new MemoryAllocator(storage, addressableUnitBitSize);
  }

  @Override
  public void setUseTempCopy(boolean value) {
    storage.setUseTempCopy(value);
  }
}
