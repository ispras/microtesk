/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

final class LocationImpl extends Location {

  private static final class Source implements Atom {
    private final MemoryStorage storage;
    private final BitVector address;

    private final int bitSize;
    private final int startBitPos;

    public Source(
        final MemoryStorage storage,
        final BitVector address,
        final int bitSize,
        final int startBitPos) {
      this.storage = storage;
      this.address = address;
      this.bitSize = bitSize;
      this.startBitPos = startBitPos;
    }

    @Override
    public boolean isInitialized() {
      return storage.isInitialized(address);
    }

    @Override
    public Source resize(
        final int newBitSize,
        final int newStartBitPos) {
      return new Source(storage, address, newBitSize, newStartBitPos);
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
      final BitVector region = storage.read(address);

      if (region.getBitSize() == bitSize) {
        return region;
      } 

      return BitVector.newMapping(region, startBitPos, bitSize);
    }

    @Override
    public void store(final BitVector data) {
      InvariantChecks.checkNotNull(data);

      final BitVector region;
      if (bitSize == storage.getRegionBitSize()) {
        region = data;
      } else {
        region = storage.read(address).copy();
        final BitVector mapping = BitVector.newMapping(region, startBitPos, bitSize);
        mapping.assign(data);
      }

      storage.write(address, region);
    }

    @Override
    public String toString() {
      return String.format("%s[%d]<%d..%d>",
          storage.getId(),
          address.bigIntegerValue(false),
          startBitPos,
          startBitPos + bitSize - 1);
    }
  }

  private LocationImpl(final Type type, final Atom atom) {
    this(type, Collections.singletonList(atom));
  }

  public LocationImpl(final Type type, final List<Atom> atoms) {
    super(type, atoms);
  }

  public static LocationImpl newLocationForConst(final Data data) {
    InvariantChecks.checkNotNull(data);

    final String storageId = "#constant";
    final BitVector zeroAddress = BitVector.valueOf(0, 1);

    final Type type = data.getType();
    final int bitSize = type.getBitSize();

    final MemoryStorage storage =
        new MemoryStorage(BigInteger.ONE, bitSize).setId(storageId);

    storage.write(zeroAddress, data.getRawData());
    storage.setReadOnly(false);

    final Source source = new Source(storage, zeroAddress, bitSize, 0);
    return new LocationImpl(type, source);
  }

  public static LocationImpl newLocationForRegion(
      final Type type,
      final MemoryStorage storage,
      final BitVector address) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(storage);
    //checkBounds(regionIndex, storage.getRegionCount());

    if (type.getBitSize() != storage.getRegionBitSize()) {
      throw new IllegalArgumentException();
    }

    final Source source = new Source(
        storage, address, type.getBitSize(), 0);

    return new LocationImpl(type, source);
  }

  public Data load() {
    final BitVector rawData = readData(getAtoms(), true);
    return new Data(rawData, getType());
  }

  public void store(Data data) {
    InvariantChecks.checkNotNull(data);

    if (getBitSize() != data.getType().getBitSize()) {
      throw new IllegalArgumentException(String.format(
          "Assigning %d-bit data to %d-bit location.", data.getType().getBitSize(), getBitSize()));
    }

    final BitVector rawData = data.getRawData();
    InvariantChecks.checkNotNull(rawData);

    writeData(getAtoms(), rawData, true);
  }

  @Override
  public String toBinString() {
    final BitVector rawData = readData(getAtoms(), false); 
    return rawData.toBinString();
  }

  @Override
  public BigInteger getValue() {
    final BitVector rawData = readData(getAtoms(), false);
    return rawData.bigIntegerValue(false);
  }

  @Override
  public void setValue(final BigInteger value) {
    InvariantChecks.checkNotNull(value);

    final BitVector rawData = BitVector.valueOf(value, getBitSize());
    writeData(getAtoms(), rawData, false);
  }
}
