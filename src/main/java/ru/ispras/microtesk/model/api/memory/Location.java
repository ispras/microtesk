/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The Location class represents memory location of the specified size that store data of the
 * specified data type. A location represents a bit array that stores a piece of data (like a
 * register or an address in the main memory).
 * 
 * @author Andrei Tatarnikov
 */

public final class Location {

  private final Type type;
  private final BitVector rawData;
  private final boolean readOnly;

  private IMemoryAccessHandler handler;

  public Location(Type type) {
    this(type, BitVector.newEmpty(type.getBitSize()), false, null);
  }

  public Location(Data data) {
    this(data.getType(), data.getRawData(), true, null);
  }

  private Location(Type type, BitVector rawData, boolean readOnly, IMemoryAccessHandler handler) {
    this.type = type;
    this.rawData = rawData;
    this.readOnly = readOnly;
    this.handler = handler;
  }

  BitVector getRawData() {
    return rawData;
  }

  public final Type getType() {
    return type;
  }

  public final boolean isReadOnly() {
    return readOnly;
  }

  public Location assign(Location arg) {
    store(arg.load());
    return this;
  }

  public void reset() {
    assert !isReadOnly();
    rawData.reset();
  }

  /**
   * Concatenates two locations: a.concat(b) = a(high) :: b(low)
   */

  public Location concat(Location arg) {
    return new Location(
      type.resize(type.getBitSize() + arg.getType().getBitSize()),
      BitVector.newMapping(arg.rawData /* low */, rawData /* high */),
      readOnly || arg.readOnly,
      handler
      );
  }

  public static Location concat(Location... locations) {
    assert locations.length > 0;

    if (1 == locations.length) {
      return locations[0];
    }

    final BitVector[] rawDataArray = new BitVector[locations.length];

    boolean readOnly = false;
    int totalBitSize = 0;

    final Type type = locations[0].getType();
    for (int index = 0; index < locations.length; ++index) {
      readOnly = readOnly || locations[index].readOnly;
      rawDataArray[index] = locations[index].rawData;
      totalBitSize += rawDataArray[index].getBitSize();
    }

    return new Location(
      type.resize(totalBitSize),
      BitVector.newMapping(rawDataArray),
      readOnly,
      null
      );
  }

  public Location bitField(int start, int end) {
    if ((start < 0) || (end < 0)) {
      throw new IllegalArgumentException();
    }

    if (start > end) {
      return bitField(end, start);
    }

    final int bitSize = end - start + 1;
    return new Location(
      type,
      BitVector.newMapping(rawData, start, bitSize),
      readOnly,
      handler
      );
  }

  public Data load() {
    // TODO: Multiple handlers (in the case of concatenation)

    /*
     * // TODO: NOT SUPPORTED IN THE CURRENT VERSION.
     * 
     * if (null != handler) { final RawData cachedRawData = handler.onLoad(); if (null !=
     * cachedRawData) return new Data(new RawDataStore(cachedRawData), type); }
     */

    return new Data(rawData.copy(), type);
  }

  public void store(Data data) {
    // TODO: Multiple handlers (in the case of concatenation)

    assert !readOnly; // TODO: Throw exception

    /*
     * // TODO: NOT SUPPORTED IN THE CURRENT VERSION.
     * 
     * if (null != handler) { if (handler.onStore(data.getRawData())) return; }
     */

    rawData.assign(data.getRawData());
  }

  /**
   * Returns a copy of stored data. This method is needed to monitor the state of a data location.
   * It provide access without simulation of storage operations.
   * 
   * @return A data object.
   */

  public Data getDataCopy() {
    return new Data(rawData.copy(), type);
  }

  /*
   * TODO: NOT SUPPORTED IN THE CURRENT VERSION.
   * 
   * public void advise(IMemoryAccessHandler handler) { assert null == this.handler; this.handler =
   * handler; }
   * 
   * public void unadvise() { assert null != this.handler; this.handler = null; }
   */

  public LocationAccessor externalAccess() {
    return new LocationAccessor(this);
  }
}
