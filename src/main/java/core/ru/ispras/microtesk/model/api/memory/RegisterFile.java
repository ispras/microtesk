/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;

final class RegisterFile extends Memory {
  private final List<Location> locations;
  private final List<RegisterAtom> atoms;

  protected RegisterFile(
      final String name,
      final Type type,
      final BigInteger length) {
    super(Kind.REG, name, type, length, false);

    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterOrEq(BigInteger.valueOf(Integer.MAX_VALUE), length);

    final Pair<List<Location>, List<RegisterAtom>> registers =
        newRegisters(name, type, length.intValue());

    this.locations = registers.first;
    this.atoms = registers.second;
  }

  private static Pair<List<Location>, List<RegisterAtom>>
      newRegisters(final String memory, final Type type, final int count) {
    final List<RegisterAtom> atoms = new ArrayList<>(count);
    final List<Location> locations = new ArrayList<>(count);

    final int bitSize = type.getBitSize();
    final int indexBitSize = getIndexBitSize(count);

    for(int index = 0; index < count; ++index) {
      final RegisterAtom atom = new RegisterAtom(memory, BitVector.valueOf(index, indexBitSize), bitSize);
      atoms.add(atom);

      final Location location = Location.newLocationForAtom(type, atom);
      locations.add(location);
    }

    return new Pair<>(locations, atoms);
  }

  private RegisterFile(final RegisterFile other) {
    super(other);

    final int count = other.locations.size();
    InvariantChecks.checkTrue(other.locations.size() == other.atoms.size());

    this.locations = new ArrayList<>(count);
    this.atoms = new ArrayList<>(count);

    for (int index = 0; index < count; ++index) {
      final Type type = other.locations.get(index).getType();
      final RegisterAtom atom = new RegisterAtom(other.atoms.get(index));
      final Location location = Location.newLocationForAtom(type, atom);

      this.atoms.add(atom);
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
    return new RegisterFile(this);
  }

  @Override
  public void reset() {
    for (final RegisterAtom atom : atoms) {
      atom.reset();
    }
  }

  private static final class RegisterAtom extends LocationAtom {
    private final BitVector value;
    private final BitVector flags;

    private RegisterAtom(final String memory, final BitVector index, final int bitSize) {
      super(memory, index, bitSize, 0);

      InvariantChecks.checkNotNull(memory);
      InvariantChecks.checkNotNull(index);
      InvariantChecks.checkGreaterThanZero(bitSize);

      this.value = BitVector.newEmpty(bitSize);
      this.flags = BitVector.newEmpty(bitSize);
    }

    private RegisterAtom(final RegisterAtom other) {
      super(other);
      this.value = other.value.copy();
      this.flags = BitVector.newEmpty(getBitSize()); // Flags are reset for the new copy.
    }

    private RegisterAtom(
        final RegisterAtom other,
        final int bitSize,
        final int startBitPos) {
      super(other, bitSize, startBitPos);

      InvariantChecks.checkGreaterThanZero(bitSize);
      InvariantChecks.checkGreaterOrEqZero(startBitPos);

      InvariantChecks.checkBounds(startBitPos, other.value.getBitSize());
      InvariantChecks.checkBoundsInclusive(startBitPos + bitSize, other.value.getBitSize());

      this.value = other.value;
      this.flags = other.flags;
    }

    @Override
    public boolean isLoggable() {
      return true;
    }

    @Override
    public boolean isInitialized() {
      final BitVector initialized;
      if (flags.getBitSize() == getBitSize()) {
        initialized = flags;
      } else {
        initialized = BitVector.newMapping(flags, getStartBitPos(), getBitSize());
      }
      return initialized.isAllSet();
    }

    @Override
    public LocationAtom resize(final int newBitSize, final int newStartBitPos) {
      return new RegisterAtom(this, newBitSize, newStartBitPos);
    }

    @Override
    public BitVector load(final boolean callHandler) {
      return BitVector.newMapping(value, getStartBitPos(), getBitSize());
    }

    @Override
    public void store(final BitVector data, final boolean callHandler) {
      InvariantChecks.checkNotNull(data);
      InvariantChecks.checkTrue(data.getBitSize() == getBitSize());

      BitVector.newMapping(value, getStartBitPos(), getBitSize()).assign(data);
      BitVector.newMapping(flags, getStartBitPos(), getBitSize()).setAll();
    }

    public void reset() {
      BitVector.newMapping(value, getStartBitPos(), getBitSize()).reset();
      BitVector.newMapping(flags, getStartBitPos(), getBitSize()).reset();
    }
  }
}
