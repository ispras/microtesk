/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.memory2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

import static ru.ispras.microtesk.utils.InvariantChecks.*;

public final class Location {

  private static final class Source {
    final MemoryStorage storage;
    final int regionIndex;
    final int bitSize;
    final int startBitPos;

    Source(
        MemoryStorage storage,
        int regionIndex,
        int bitSize,
        int startBitPos) {

      this.storage = storage;
      this.regionIndex = regionIndex;
      this.bitSize = bitSize;
      this.startBitPos = startBitPos;
    }

    Source resize(int newBitSize, int newStartBitPos) {
      return new Source(storage, regionIndex, newBitSize, newStartBitPos);
    }

    @Override
    public String toString() {
      return String.format("%s[%d]<%d..%d>",
          storage.getId(), regionIndex, startBitPos, startBitPos + bitSize - 1);
    }
  }

  private final Type type;
  private final List<Source> sources;

  static Location newLocationForRegion(
      Type type, MemoryStorage storage, int regionIndex) {

    checkNotNull(type);
    checkNotNull(storage);
    checkBounds(regionIndex, storage.getRegionCount());

    if (type.getBitSize() != storage.getRegionBitSize()) {
      throw new IllegalArgumentException();
    }

    final List<Source> sources = Collections.singletonList(
        new Source(storage, regionIndex, type.getBitSize(), 0));

    return new Location(type, sources);
  }

  private Location(Type type, List<Source> sources) {
    checkNotNull(type);
    checkNotNull(sources);

    if (sources.isEmpty()) {
      throw new IllegalArgumentException();
    }

    this.type = type;
    this.sources = sources;
  }

  public Location castTo(TypeId typeId) {
    checkNotNull(typeId);
    return new Location(type.castTo(typeId), sources);
  }

  public Type getType() {
    return type;
  }

  public int getBitSize() {
    return type.getBitSize();
  }

  public Data load() {
    final MemoryAccessHandler handler = Memory.getGlobalHandler();

    final BitVector rawData;
    if (null == handler) {
      rawData = readDataDirecty(sources);
    } else {
      rawData = readDataViaHandler(handler, sources);
    }

    return new Data(rawData, type);
  }

  public void store(Data data) {
    checkNotNull(data);

    if (getBitSize() != data.getType().getBitSize()) {
      throw new IllegalArgumentException();
    }

    final BitVector rawData = data.getRawData();
    checkNotNull(rawData);

    final MemoryAccessHandler handler = Memory.getGlobalHandler();
    if (null == handler) {
      writeDataDirecty(rawData, sources);
    } else {
      writeDataViaHandler(handler, rawData, sources);
    }
  }

  public Location assign(Location source) {
    checkNotNull(source);
    store(source.load());
    return this;
  }

  public Location bitField(int index) {
    return bitField(index, index);
  }

  public Location bitField(int start, int end) {
    // System.out.printf("Bit field: %d %d %n", start, end);
    
    checkBounds(start, getBitSize());
    checkBounds(end, getBitSize());

    if (start > end) {
      return bitField(end, start);
    }

    final int newBitSize = end - start + 1;
    final Type newType = type.resize(newBitSize);

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

    return new Location(newType, newSources);
  }

  public Location concat(Location argument) {
    return Location.concat(this, argument);
  }

  public static Location concat(Location first, Location second, Location ... others) {
    checkNotNull(first);
    checkNotNull(second);

    int newSourceCount = first.sources.size() + second.sources.size();
    int newBitSize = first.getBitSize() + second.getBitSize();

    for (Location location : others) {
      checkNotNull(location);
      newSourceCount += location.sources.size();
      newBitSize += location.getBitSize();
    }

    final List<Source> newSources = new ArrayList<Source>(newSourceCount);

    newSources.addAll(first.sources);
    newSources.addAll(second.sources);

    for (Location location : others) {
      newSources.addAll(location.sources);
    }

    final Type newType = first.getType().resize(newBitSize);
    return new Location(newType, newSources);
  }

  public String toBinString() {
    final BitVector rawData = readDataDirecty(sources); 
    return rawData.toBinString();
  }

  public BigInteger getValue() {
    final BitVector rawData = readDataDirecty(sources);
    return new BigInteger(rawData.toByteArray());
  }
  
  private static BitVector readDataDirecty(List<Source> sources) {
    final BitVector[] dataItems = new BitVector[sources.size()]; 
    for (int index = 0; index < sources.size(); ++index) {
      final Source source = sources.get(index);
      final BitVector region = source.storage.read(source.regionIndex);

      if (region.getBitSize() == source.bitSize) {
        dataItems[index] = region;
      } else {
        dataItems[index] = BitVector.newMapping(region, source.startBitPos, source.bitSize);
      }
    }

    if (1 == dataItems.length) {
      return dataItems[0];
    }

    return BitVector.newMapping(dataItems);
  }

  private static BitVector readDataViaHandler(MemoryAccessHandler handler, List<Source> sources) {
    //TODO: NOT IMPLEMENTED
    throw new UnsupportedOperationException();
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
        regionData = storage.read(source.regionIndex).copy();

        final BitVector mapping = 
            BitVector.newMapping(regionData, source.startBitPos, source.bitSize);

        mapping.assign(dataItem);
      }

      storage.write(source.regionIndex, regionData);
      position += source.bitSize;
    }
  }

  private static void writeDataViaHandler(
      MemoryAccessHandler handler, BitVector rawData, List<Source> sources) {
    //TODO: NOT IMPLEMENTED
    throw new UnsupportedOperationException();
  }
}
