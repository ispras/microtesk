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
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.memory.Location.Atom;
import ru.ispras.microtesk.model.api.type.Type;

final class RegisterFile extends Memory {
  private final List<Location> locations;
  private List<Location> tempLocations;

  protected RegisterFile(
      final String name,
      final Type type,
      final BigInteger length) {
    super(Kind.REG, name, type, length, false);

    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterOrEq(BigInteger.valueOf(Integer.MAX_VALUE), length);

    final int count = length.intValue();
    this.locations = newLocations(type, count);

    this.tempLocations = null;
  }

  private static List<Location> newLocations(final Type type, final int count) {
    final List<Location> locations = new ArrayList<Location>(count);
    final int bitSize = type.getBitSize();
    for(int index = 0; index < count; ++index) {
      final Location location = new Location(type, new RegisterAtom(bitSize));
      locations.add(location);
    }

    return locations;
  }

  @Override
  public Location access(final int index) {
    if (null != tempLocations) {
      return tempLocations.get(index);
    } else {
      return locations.get(index);
    }
  }

  @Override
  public Location access(final long index) {
    return access((int) index);
  }

  @Override
  public Location access(final BigInteger index) {
    return access(index.intValue());
  }

  @Override
  public Location access(final Data index) {
    return access(index.getRawData().intValue());
  }

  @Override
  public void reset() {
    // Do nothing
  }

  @Override
  public MemoryAllocator newAllocator(int addressableUnitBitSize) {
    throw new UnsupportedOperationException(
        "Allocators are not supported for registers.");
  }

  @Override
  public void setUseTempCopy(final boolean value) {
    final boolean isUsed = null != tempLocations;
    if (value == isUsed) {
      return;
    }

    if (value) {
      tempLocations = newLocations(getType(), getLength().intValue());
    } else {
      tempLocations = null;
    }
  }

  private static class RegisterAtom implements Location.Atom {
    private final BitVector value;
    private final BitVector flags;

    private final int bitSize;
    private final int startBitPos;

    public RegisterAtom(final int bitSize) {
      InvariantChecks.checkGreaterThanZero(bitSize);

      this.value = BitVector.newEmpty(bitSize);
      this.flags = BitVector.newEmpty(bitSize);

      this.bitSize = bitSize;
      this.startBitPos = 0;
    }

    @Override
    public boolean isInitialized() {
      final BitVector initialized;
      if (flags.getBitSize() == bitSize) {
        initialized = flags;
      } else {
        initialized = BitVector.newMapping(flags, startBitPos, bitSize);
      }

      // All bits  must be 1.
      return false;
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
    public Atom resize(final int newBitSize, final int newStartBitPos) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public BitVector load() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void store(final BitVector data) {
      // TODO Auto-generated method stub
      
    }
  }
}
