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
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;

final class VariableArray extends Memory {
  private final List<Location> locations;
  private final List<BitVector> values;

  protected VariableArray(
      final String name,
      final Type type,
      final BigInteger length) {
    super(Kind.VAR, name, type, length, false);

    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterOrEq(BigInteger.valueOf(Integer.MAX_VALUE), length);

    final int count = length.intValue();
    final int indexBitSize = getIndexBitSize(count);

    this.locations = new ArrayList<>(count);
    this.values = new ArrayList<>(count);

    for (int index = 0; index < count; ++index) {
      final BitVector value = BitVector.newEmpty(type.getBitSize());
      final LocationAtom atom = new VariableAtom(getName(), BitVector.valueOf(index, indexBitSize), value);
      final Location location = Location.newLocationForAtom(type, atom);

      this.values.add(value);
      this.locations.add(location);
    }
  }

  private VariableArray(final VariableArray other) {
    super(other);

    final int count = other.locations.size();
    InvariantChecks.checkTrue(other.locations.size() == other.values.size());
    final int indexBitSize = getIndexBitSize(count);

    this.locations = new ArrayList<>(count);
    this.values = new ArrayList<>(count);

    for (int index = 0; index < count; ++index) {
      final Type type = other.locations.get(index).getType();
      final BitVector value = other.values.get(index).copy();

      final LocationAtom atom = new VariableAtom(getName(), BitVector.valueOf(index, indexBitSize), value);
      final Location location = Location.newLocationForAtom(type, atom);

      this.values.add(value);
      this.locations.add(location);
    }
  }

  @Override
  public Location access(final int index) {
    return locations.get(index);
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
  public Memory copy() {
    return new VariableArray(this);
  }

  @Override
  public void reset() {
    for (final BitVector value : values) {
      value.reset();
    }
  }
}
