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

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

final class PhysicalMemory extends Memory {
  private final MemoryDevice storage;
  private MemoryDevice handler;

  public PhysicalMemory(
      final String name,
      final Type type,
      final BigInteger length) {
    super(Kind.MEM, name, type, length, false);
    this.storage = new MemoryStorage(length, type.getBitSize()).setId(name);
    this.handler = null;
  }

  public MemoryDevice getStorage() {
    return storage;
  }

  public MemoryDevice getHandler() {
    return handler;
  }

  public MemoryDevice getDevice() {
    return null != handler ? handler : storage;
  }

  public void setHandler(final MemoryDevice handler) {
    InvariantChecks.checkNotNull(handler);
    this.handler = handler;
  }

  @Override
  public Location access(final int index) {
    return access(index & 0x00000000FFFFFFFFL);
  }

  @Override
  public Location access(final long index) {
    final BitVector address = BitVector.valueOf(index, storage.getAddressBitSize());
    return newLocationForRegion(address);
  }

  @Override
  public Location access(final BigInteger index) {
    final BitVector address = BitVector.valueOf(index, storage.getAddressBitSize());
    return newLocationForRegion(address);
  }

  @Override
  public Location access(final Data address) {
    InvariantChecks.checkNotNull(address);
    return newLocationForRegion(address.getRawData());
  }

  @Override
  public void reset() {
    // Do nothing. Memory is not reset.
  }

  @Override
  public void setUseTempCopy(boolean value) {
    storage.useTemporaryContext(value);
  }

  private Location newLocationForRegion(final BitVector address) {
    InvariantChecks.checkNotNull(address);

    final PhysicalMemoryAtom atom =
        new PhysicalMemoryAtom(address, getType().getBitSize(), 0);

    return new Location(getType(), atom);
  }

  private final class PhysicalMemoryAtom implements Location.Atom {
    private final BitVector address;
    private final int bitSize;
    private final int startBitPos;

    private PhysicalMemoryAtom(
        final BitVector address,
        final int bitSize,
        final int startBitPos) {
      this.address = address;
      this.bitSize = bitSize;
      this.startBitPos = startBitPos;
    }

    @Override
    public boolean isInitialized() {
      return storage.isInitialized(address);
    }

    @Override
    public PhysicalMemoryAtom resize(
        final int newBitSize,
        final int newStartBitPos) {
      return new PhysicalMemoryAtom(address, newBitSize, newStartBitPos);
    }

    @Override
    public int getBitSize() {
      return bitSize;
    }

    @Override
    public int getStartBitPos() {
      return startBitPos;
    }

    @Override
    public BitVector load() {
      final BitVector region = storage.load(address);
      return BitVector.newMapping(region, startBitPos, bitSize);
    }

    @Override
    public void store(final BitVector data) {
      InvariantChecks.checkNotNull(data);

      final BitVector region;
      if (bitSize == storage.getDataBitSize()) {
        region = data;
      } else {
        region = storage.load(address).copy();
        final BitVector mapping = BitVector.newMapping(region, startBitPos, bitSize);
        mapping.assign(data);
      }

      storage.store(address, region);
    }

    @Override
    public String toString() {
      return String.format("%s[%d]<%d..%d>",
          getName(),
          address.bigIntegerValue(false),
          startBitPos,
          startBitPos + bitSize - 1);
    }
  }
}
