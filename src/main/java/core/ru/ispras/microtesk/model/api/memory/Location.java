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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.data.Data;

import ru.ispras.microtesk.model.api.state.LocationAccessor;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

public abstract class Location implements LocationAccessor {

  protected interface Atom {
    boolean isInitialized();

    int getBitSize();
    int getStartBitPos();
    Atom resize(int newBitSize, int newStartBitPos);

    BitVector load();
    void store(BitVector data);
  }

  private final Type type;
  private final List<Atom> atoms;

  protected Location(final Type type, final Atom atom) {
    this(type, atom != null ? Collections.singletonList(atom) : null);
  }

  protected Location(final Type type, final List<Atom> atoms) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotEmpty(atoms);

    this.type = type;
    this.atoms = atoms;
  }

  protected final List<Atom> getAtoms() {
    return atoms;
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

  public final Location castTo(final TypeId typeId) {
    InvariantChecks.checkNotNull(typeId);

    if (getType().getTypeId() == typeId) {
      return this;
    }

    return new LocationImpl(getType().castTo(typeId), atoms);
  }

  public final boolean isInitialized() {
    for (final Atom atom : atoms) {
      if (!atom.isInitialized()) {
        return false;
      }
    }
    return true;
  }

  public abstract Data load();
  public abstract void store(Data data);

  public final Location assign(final Location source) {
    InvariantChecks.checkNotNull(source);
    store(source.load());
    return this;
  }

  public final Location bitField(final int start, final int end) {
    InvariantChecks.checkBounds(start, getBitSize());
    InvariantChecks.checkBounds(end, getBitSize());

    if (start > end) {
      return bitField(end, start);
    }

    if ((start == 0) && (end == (getBitSize() - 1))) {
      return this;
    }

    final int newBitSize = end - start + 1;
    final Type newType = getType().resize(newBitSize);

    final List<Atom> newAtoms = new ArrayList<Atom>();

    int position = 0;
    for (final Atom atom : atoms) {
      final int atomStart = position; 
      final int atomEnd = position + atom.getBitSize() - 1;

      if (atomStart <= start && start <= atomEnd) {
        if (end <= atomEnd) {
          final int newStartBitPos = atom.getStartBitPos() + (start - position);
          final Atom newSource = atom.resize(newBitSize, newStartBitPos);
          newAtoms.add(newSource);
          break;
        } else {
          newAtoms.add(atom);
        }
      } else if (atomStart <= end && end <= atomEnd) {
        newAtoms.add(atom.resize(atom.getBitSize() - (atomEnd - end), atom.getStartBitPos()));
        break;
      }

      position = atomEnd + 1;
    }

    return new LocationImpl(newType, newAtoms);
  }

  public final Location bitField(final int index) {
    return bitField(index, index);
  }

  public final Location bitField(final Data start, final Data end) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkTrue(start.getRawData().getBitSize() <= Integer.SIZE);

    InvariantChecks.checkNotNull(end);
    InvariantChecks.checkTrue(end.getRawData().getBitSize() <= Integer.SIZE);

    return bitField(start.getRawData().intValue(), end.getRawData().intValue());
  }

  public final Location bitField(final Data index) {
    InvariantChecks.checkNotNull(index);
    InvariantChecks.checkTrue(index.getRawData().getBitSize() <= Integer.SIZE);

    return bitField(index.getRawData().intValue());
  }

  public final Location concat(final Location other) {
    InvariantChecks.checkNotNull(other);
    return Location.concat(this, other);
  }

  public static Location concat(final Location... locations) {
    InvariantChecks.checkNotEmpty(locations);

    if (locations.length == 1) {
      return locations[0];
    }

    int newBitSize = 0;
    final List<Atom> newAtoms = new ArrayList<Atom>();

    for (final Location location : locations) {
      InvariantChecks.checkNotNull(location);
      newBitSize += location.getBitSize();
      newAtoms.addAll(location.getAtoms());
    }

    final Type newType = locations[0].getType().resize(newBitSize);
    return new LocationImpl(newType, newAtoms);
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

  protected static BitVector readData(
      final List<Atom> atoms,
      final boolean callHandlers) {
    final BitVector[] dataItems = new BitVector[atoms.size()];
    for (int index = 0; index < atoms.size(); ++index) {
      final Atom atom = atoms.get(index);
      dataItems[index] = atom.load();
    }

    return BitVector.newMapping(dataItems).copy();
  }

  protected static void writeData(
      final List<Atom> atoms,
      final BitVector data,
      final boolean callHandlers) {
    int position = 0;
    for (final Atom atom : atoms) {
      final BitVector dataItem =
          BitVector.newMapping(data, position, atom.getBitSize());

      atom.store(dataItem);
      position += dataItem.getBitSize();
    }
  }
}
