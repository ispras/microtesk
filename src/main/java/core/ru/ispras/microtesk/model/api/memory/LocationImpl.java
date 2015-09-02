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

import static ru.ispras.fortress.util.InvariantChecks.checkBounds;
import static ru.ispras.fortress.util.InvariantChecks.checkNotEmpty;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

final class LocationImpl extends Location {
  private final List<Source> sources;

  private static final class Source {
    public final MemoryStorage storage;
    public final BitVector address;
    public final int bitSize;
    public final int startBitPos;

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

    public boolean isInitialized() {
      return storage.isInitialized(address);
    }

    public Source resize(
        final int newBitSize,
        final int newStartBitPos) {
      return new Source(storage, address, newBitSize, newStartBitPos);
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

  private LocationImpl(final Type type, final Source source) {
    this(type, Collections.singletonList(source));
  }

  private LocationImpl(final Type type, final List<Source> sources) {
    super(type);

    checkNotEmpty(sources);
    this.sources = sources;
  }

  public static LocationImpl newLocationForConst(final Data data) {
    checkNotNull(data);

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
    checkNotNull(type);
    checkNotNull(storage);
    //checkBounds(regionIndex, storage.getRegionCount());

    if (type.getBitSize() != storage.getRegionBitSize()) {
      throw new IllegalArgumentException();
    }

    final Source source = new Source(
        storage, address, type.getBitSize(), 0);

    return new LocationImpl(type, source);
  }

  public LocationImpl castTo(TypeId typeId) {
    checkNotNull(typeId);

    if (getType().getTypeId() == typeId) {
      return this;
    }

    return new LocationImpl(getType().castTo(typeId), sources);
  }

  public boolean isInitialized() {
    for (final Source source : sources) {
      if (!source.isInitialized()) {
        return false;
      }
    }
    return true;
  }

  public Data load() {
    final BitVector rawData = readDataDirecty(sources);
    return new Data(rawData, getType());
  }

  public void store(Data data) {
    checkNotNull(data);

    if (getBitSize() != data.getType().getBitSize()) {
      throw new IllegalArgumentException(String.format(
          "Assigning %d-bit data to %d-bit location.", data.getType().getBitSize(), getBitSize()));
    }

    final BitVector rawData = data.getRawData();
    checkNotNull(rawData);

    writeDataDirecty(rawData, sources);
  }

  public LocationImpl assign(Location source) {
    checkNotNull(source);
    store(source.load());
    return this;
  }

  public LocationImpl bitField(int start, int end) {
    // System.out.printf("Bit field: %d %d %n", start, end);

    checkBounds(start, getBitSize());
    checkBounds(end, getBitSize());

    if (start > end) {
      return bitField(end, start);
    }

    if ((start == 0) && (end == (getBitSize() - 1))) {
      return this;
    }

    final int newBitSize = end - start + 1;
    final Type newType = getType().resize(newBitSize);

    final List<Source> newSources = new ArrayList<Source>();

    int pos = 0;
    for (Source source : sources) {
      final int sourceStart = pos; 
      final int sourceEnd = pos + source.bitSize - 1;

      if (sourceStart <= start && start <= sourceEnd) {
        if (end <= sourceEnd) {
          final int newStartBitPos = source.startBitPos + (start - pos);
          final Source newSource = source.resize(newBitSize, newStartBitPos);
          newSources.add(newSource);
          break;
        } else {
          newSources.add(source);
        }
      } else if (sourceStart <= end && end <= sourceEnd) {
        newSources.add(source.resize(source.bitSize - (sourceEnd - end), source.startBitPos));
        break;
      }

      pos = sourceEnd + 1;
    }

    return new LocationImpl(newType, newSources);
  }

  public Location concat(Location argument) {
    return Location.concat(this, argument);
  }

  public static Location concat(Location ... locations) {
    if (locations.length == 0) {
      throw new IllegalArgumentException();
    }

    if (locations.length == 1) {
      return locations[0];
    }

    int newBitSize = 0;
    final List<Source> newSources = new ArrayList<Source>();

    for (Location location : locations) {
      checkNotNull(location);
      newBitSize += location.getBitSize();
      newSources.addAll(((LocationImpl) location).sources);
    }

    final Type newType = locations[0].getType().resize(newBitSize);
    return new LocationImpl(newType, newSources);
  }

  @Override
  public String toBinString() {
    final BitVector rawData = readDataDirecty(sources); 
    return rawData.toBinString();
  }

  @Override
  public BigInteger getValue() {
    final BitVector rawData = readDataDirecty(sources);
    return rawData.bigIntegerValue(false);
  }

  @Override
  public void setValue(final BigInteger value) {
    checkNotNull(value);

    // System.out.println("############## " + toBinString()); 
    // System.out.println("############## Before Assigning 0x" + value);

    final BitVector rawData = BitVector.valueOf(value, getBitSize());
    writeDataDirecty(rawData, sources);

    // System.out.println("############## " + toBinString()); 
    // System.out.println("############## After Assigning 0x" + value);
  }

  private static BitVector readDataDirecty(List<Source> sources) {
    final BitVector[] dataItems = new BitVector[sources.size()]; 
    for (int index = 0; index < sources.size(); ++index) {
      final Source source = sources.get(index);
      final BitVector region = source.storage.read(source.address);

      if (region.getBitSize() == source.bitSize) {
        dataItems[index] = region;
      } else {
        dataItems[index] = BitVector.newMapping(region, source.startBitPos, source.bitSize);
      }
    }

    if (1 == dataItems.length) {
      return dataItems[0].copy();
    }

    return BitVector.newMapping(dataItems).copy();
  }

  private static void writeDataDirecty(BitVector data, List<Source> sources) {
    int position = 0;
    for (Source source : sources) {
      final MemoryStorage storage = source.storage;

      final BitVector dataItem = 
          BitVector.newMapping(data, position, source.bitSize);

      final BitVector regionData;

      if (source.bitSize == storage.getRegionBitSize()) {
        regionData = dataItem;
      } else {
        regionData = storage.read(source.address).copy();

        final BitVector mapping = 
            BitVector.newMapping(regionData, source.startBitPos, source.bitSize);

        mapping.assign(dataItem);
      }

      storage.write(source.address, regionData);
      position += source.bitSize;
    }
  }
}
