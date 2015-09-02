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

import java.util.Arrays;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.state.LocationAccessor;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

public abstract class Location implements LocationAccessor {
  private final Type type;

  protected Location(final Type type) {
    InvariantChecks.checkNotNull(type);
    this.type = type;
  }

  public static Location newLocationForConst(final Data data) {
    return LocationImpl.newLocationForConst(data);
  }

  public static Location newLocationForRegion(
      final Type type,
      final MemoryStorage storage,
      final BitVector address) {
    return LocationImpl.newLocationForRegion(type, storage, address);
  }

  public final Type getType() {
    return type;
  }

  @Override
  public final int getBitSize() {
    return type.getBitSize();
  }

  public abstract Location castTo(TypeId typeId);

  public abstract boolean isInitialized();
  public abstract Data load();
  public abstract void store(Data data);
  public abstract Location assign(Location source);

  public abstract Location bitField(Data start, Data end);
  public abstract Location bitField(int start, int end);

  public final Location bitField(final Data index) {
    return bitField(index, index);
  }

  public final Location bitField(final int index) {
    return bitField(index, index);
  }

  public abstract Location concat(Location argument);

  public static Location concat(final Location... locations) {
    return LocationImpl.concat(locations);
  }

  public final Location repeat(final int count) {
    InvariantChecks.checkGreaterThanZero(count);

    if (count == 1) {
      return this;
    }

    final Location[] array = new Location[count];
    Arrays.fill(array, this);

    return concat(array); 
  }
}
