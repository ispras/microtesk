/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.memory;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.model.data.TypeId;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Location implements LocationAccessor {
  private final Type type;
  private final List<LocationAtom> atoms;

  private Location(final Type type, final List<LocationAtom> atoms) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotEmpty(atoms);

    this.type = type;
    this.atoms = atoms;
  }

  private Location(final Type type, final LocationAtom atom) {
    this(type, atom != null ? Collections.singletonList(atom) : null);
  }

  public Location(final Data data) {
    this(
        data != null ? data.getType() : null,
        data != null ? new VariableAtom(null, null, data.getRawData()) : null
        );
  }

  public static Location newLocationForConst(final Data data) {
    InvariantChecks.checkNotNull(data);
    return new Location(data);
  }

  public static Location newLocationForAtom(final Type type, final LocationAtom atom) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(atom);
    InvariantChecks.checkTrue(type.getBitSize() == atom.getBitFieldSize());
    return new Location(type, atom);
  }

  public Location setAddressingMode(final AddressingMode addressingMode) {
    final String name = addressingMode.getSyntax();

    if (null == name || name.isEmpty()) {
      return this;
    }

    boolean isLoggerAdded = false;
    final List<LocationAtom> newAtoms = new ArrayList<>(atoms.size());

    int bitPos = 0;
    final boolean needSuffix = atoms.size() > 1;

    for (final LocationAtom atom : atoms) {
      final int bitSize = atom.getBitFieldSize();
      if (atom.isLoggable()) {
        final String atomName =
            needSuffix ? String.format("%s<%d..%d>", name, bitPos + bitSize - 1, bitPos) : name;

        newAtoms.add(new LocationAtomLogger(atom, atomName));
        isLoggerAdded = true;
      } else {
        newAtoms.add(atom);
      }

      bitPos += bitSize;
    }

    return isLoggerAdded ? new Location(type, newAtoms) : this;
  }

  public Type getType() {
    return type;
  }

  public Location castTo(final TypeId typeId) {
    InvariantChecks.checkNotNull(typeId);

    if (getType().getTypeId() == typeId) {
      return this;
    }

    return new Location(getType().castTo(typeId), atoms);
  }

  public boolean isInitialized() {
    for (final LocationAtom atom : atoms) {
      if (!atom.isInitialized()) {
        return false;
      }
    }
    return true;
  }

  public Data load() {
    final BitVector rawData = readData(true);
    return new Data(rawData, getType());
  }

  public void store(final Data data) {
    InvariantChecks.checkNotNull(data);

    if (getBitSize() != data.getType().getBitSize()) {
      throw new IllegalArgumentException(String.format(
          "Assigning %d-bit data to %d-bit location is not allowed.",
          data.getType().getBitSize(),
          getBitSize())
          );
    }

    final BitVector rawData = data.getRawData();
    InvariantChecks.checkNotNull(rawData);

    writeData(rawData, true);
  }

  public void store(final Location source) {
    InvariantChecks.checkNotNull(source);
    store(source.load());
  }

  public Location assign(final Location source) {
    store(source);
    return this;
  }

  public Location bitField(final int start, final int end) {
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

    final List<LocationAtom> newAtoms = new ArrayList<>();

    int position = 0;
    for (final LocationAtom atom : atoms) {
      final int atomStart = position;
      final int atomEnd = position + atom.getBitFieldSize() - 1;

      if (atomStart <= start && start <= atomEnd) {
        if (end <= atomEnd) {
          final int newStartBitPos = atom.getBitFieldStart() + (start - position);
          final LocationAtom newSource = atom.resize(newBitSize, newStartBitPos);
          newAtoms.add(newSource);
          break;
        } else {
          newAtoms.add(atom);
        }
      } else if (atomStart <= end && end <= atomEnd) {
        newAtoms.add(atom.resize(atom.getBitFieldSize() - (atomEnd - end),
            atom.getBitFieldStart()));
        break;
      }

      position = atomEnd + 1;
    }

    return new Location(newType, newAtoms);
  }

  public Location bitField(final int index) {
    return bitField(index, index);
  }

  public Location bitField(final Data start, final Data end) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkTrue(start.getRawData().getBitSize() <= Integer.SIZE);

    InvariantChecks.checkNotNull(end);
    InvariantChecks.checkTrue(end.getRawData().getBitSize() <= Integer.SIZE);

    return bitField(start.getRawData().intValue(), end.getRawData().intValue());
  }

  public Location bitField(final Data index) {
    InvariantChecks.checkNotNull(index);
    InvariantChecks.checkTrue(index.getRawData().getBitSize() <= Integer.SIZE);

    return bitField(index.getRawData().intValue());
  }

  /**
   * Concatenates the specified locations.
   *
   * @param locations Locations, format is [high, ..., low].
   * @return Concatenated location.
   */
  public static Location concat(final Location... locations) {
    InvariantChecks.checkNotEmpty(locations);

    if (locations.length == 1) {
      return locations[0];
    }

    int newBitSize = 0;
    final List<LocationAtom> newAtoms = new ArrayList<>();

    for (int index = 0; index < locations.length; index++) {
      final Location location = locations[index];
      InvariantChecks.checkNotNull(location);

      newBitSize += location.getBitSize();
      newAtoms.addAll(location.atoms);
    }

    final Type newType = locations[0].getType().resize(newBitSize);
    return new Location(newType, newAtoms);
  }

  @Override
  public int getBitSize() {
    return type.getBitSize();
  }

  @Override
  public BitVector toBitVector() {
    return readData(false);
  }

  @Override
  public String toString() {
    return toBinString();
  }

  @Override
  public String toBinString() {
    final BitVector rawData = toBitVector();
    return rawData.toBinString();
  }

  public String toHexString() {
    final BitVector rawData = toBitVector();
    return rawData.toHexString();
  }

  @Override
  public BigInteger getValue() {
    final BitVector rawData = toBitVector();
    return rawData.bigIntegerValue(false);
  }

  @Override
  public void setValue(final BigInteger value) {
    InvariantChecks.checkNotNull(value);

    final BitVector rawData = BitVector.valueOf(value, getBitSize());
    writeData(rawData, false);
  }

  private BitVector readData(final boolean callHandlers) {
    final int size = atoms.size();
    final BitVector[] dataItems = new BitVector[atoms.size()];

    int index = size - 1;
    for (final LocationAtom atom : atoms) {
      dataItems[index] = atom.load(callHandlers);
      index--;
    }

    // dataItems stores data from HIGH to LOW.
    return BitVector.newMapping(dataItems).copy();
  }

  private void writeData(final BitVector data, final boolean callHandlers) {
    int position = 0;
    for (final LocationAtom atom : atoms) {
      final BitVector dataItem =
          BitVector.newMapping(data, position, atom.getBitFieldSize());

      atom.store(dataItem, callHandlers);
      position += dataItem.getBitSize();
    }
  }
}
