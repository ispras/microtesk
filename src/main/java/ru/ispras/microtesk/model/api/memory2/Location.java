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

public final class Location {

  private static final class Source {
    final MemoryStorage storage;
    final boolean isHandled;

    final int regionIndex;
    final int bitSize;
    final int startBitPos;

    Source(
        MemoryStorage storage,
        boolean isHandled,
        int regionIndex,
        int bitSize,
        int startBitPos) {

      checkNotNull(storage);

      this.storage = storage;
      this.isHandled = isHandled;
      this.regionIndex = regionIndex;
      this.bitSize = bitSize;
      this.startBitPos = startBitPos;
    }

    Source resize(int newBitSize, int newStartBitPos) {
      return new Source(storage, isHandled, regionIndex, newBitSize, newStartBitPos);
    }
  }

  private final Type type;
  private final List<Source> sources;

  static Location newLocationForRegion(
      Type type, MemoryStorage storage, int regionIndex, boolean isHandled) {

    checkNotNull(type);
    checkNotNull(storage);

    if (type.getBitSize() != storage.getRegionBitSize()) {
      throw new IllegalArgumentException();
    }

    if (!(0 <= regionIndex && regionIndex < storage.getRegionCount())) { 
      throw new IndexOutOfBoundsException();
    }

    final List<Source> sources = Collections.singletonList(
        new Source(storage, isHandled, regionIndex, type.getBitSize(), 0));

    return new Location(type, sources);
  }

  private Location(Type type, List<Source> sources) {
    checkNotNull(type);
    checkNotNull(sources);

    this.type = type;
    this.sources = sources;
  }

  public Type getType() {
    return type;
  }

  public int getBitSize() {
    return type.getBitSize();
  }

  public Data load() {
    final BitVector rawData = readData(true);
    return new Data(rawData, type);
  }

  public void store(Data value) {
    checkNotNull(value);

    if (getBitSize() != value.getType().getBitSize()) {
      throw new IllegalArgumentException();
    }

    writeData(value.getRawData(), true);
  }

  public Location assign(Location source) {
    store(source.load());
    return this;
  }

  public Location bitField(int index) {
    return bitField(index, index);
  }

  public Location bitField(int start, int end) {
    checkBounds(start);
    checkBounds(end);

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
          sources.add(source.resize(newBitSize, source.startBitPos + (source.bitSize - newBitSize)));
          break;
        } else {
          sources.add(source);
        }
      } else if (sourceStart <= end && end <= sourceEnd) {
        sources.add(source.resize(source.bitSize - (sourceEnd - end), source.startBitPos));
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
    final BitVector rawData = readData(false); 
    return rawData.toBinString();
  }

  public BigInteger getValue() {
    final BitVector rawData = readData(false);
    return new BigInteger(rawData.toByteArray());
  }

  private BitVector readData(boolean handle) {
    final MemoryAccessHandler handler = handle ? Memory.getHandler() : null;
    final BitVector[] dataItems = new BitVector[sources.size()]; 
    
    for (int index = 0; index < sources.size(); ++index) {
      final Source source = sources.get(index);

      if (source.isHandled && null != handler) {
        // TODO
        throw new UnsupportedOperationException("Not supported yet");
      }
      else {
        final BitVector region = source.storage.read(source.regionIndex);
        
        if (region.getBitSize() == source.bitSize) {
          dataItems[index] = region;
        } else {
          dataItems[index] = BitVector.newMapping(region, source.startBitPos, source.bitSize);
        }
      }
    }

    if (1 == dataItems.length) {
      return dataItems[0];
    }

    return BitVector.newMapping(dataItems);
  }

  private void writeData(BitVector data, boolean handle) {
    final MemoryAccessHandler handler = handle ? Memory.getHandler() : null;

    int pos = 0;
    for (Source source : sources) {
      if (source.isHandled && null != handler) {
        // TODO
        throw new UnsupportedOperationException("Not supported yet");
      } else {
        if (source.bitSize == source.storage.getRegionBitSize()) {
          
        } else {

        }
      }

      pos += source.bitSize; 
    }

    // TODO !!!
  }

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }

  private final void checkBounds(int index) {
    if (!(0 <= index && index < getBitSize())) {
      throw new IndexOutOfBoundsException();
    }
  }
}

/*

  private class LocationImpl implements Location {
    private final int index;

    private LocationImpl(int index) {
      this.index = index;
    }

    @Override
    public Data load() {
      if (null != handler) {
        final List<MemoryRegion> regions = handler.onLoad(
            Collections.singletonList(new MemoryRegion(storage, index)));
        return new Data(regions.get(0).getData(), type);
      }

      return new Data(storage.read(index), type);
    }

    @Override
    public void store(Data data) {
      if (null != handler) {
        handler.onStore(Collections.singletonList(
            new MemoryRegion(storage, index, data.getRawData())));
        return;
      }

      storage.write(index, data.getRawData());
    }
  }

*/
