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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.state.LocationAccessor;
import ru.ispras.microtesk.model.api.tarmac.Record;
import ru.ispras.microtesk.model.api.tarmac.Tarmac;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

public final class Location implements LocationAccessor {

  protected interface Atom {
    boolean isInitialized();

    int getBitSize();
    int getStartBitPos();
    Atom resize(int newBitSize, int newStartBitPos);

    BitVector load(boolean useHandler);
    void store(BitVector data, boolean callHandler);
  }

  protected interface LoggableAtom {}

  private static final class AtomLogger implements Atom {
    private final Atom atom;
    private final String name;
    private final int originalBitSize;
    private final int originalStartBitPos;

    private AtomLogger(final Atom atom, final String name) {
      InvariantChecks.checkNotNull(atom);
      InvariantChecks.checkNotNull(name);

      this.atom = (atom instanceof AtomLogger) ? ((AtomLogger) atom).atom : atom;
      this.name = name;
      this.originalBitSize = atom.getBitSize();
      this.originalStartBitPos = atom.getStartBitPos();
    }

    private AtomLogger(
        final Atom atom,
        final String name,
        final int originalBitSize,
        final int originalStartBitPos) {
      InvariantChecks.checkNotNull(atom);
      InvariantChecks.checkNotNull(name);

      this.atom = (atom instanceof AtomLogger) ? ((AtomLogger) atom).atom : atom;
      this.name = name;
      this.originalBitSize = originalBitSize;
      this.originalStartBitPos = originalStartBitPos;
    }

    private String getName() {
      if (originalBitSize == getBitSize() && originalStartBitPos == getStartBitPos()) {
        return name;
      }

      final int start = getStartBitPos() - originalStartBitPos;
      final int end = start + getBitSize() - 1;

      return String.format("%s<%d..%d>", name, end, start);
    }

    @Override
    public boolean isInitialized() {
      return atom.isInitialized();
    }

    @Override
    public int getBitSize() {
      return atom.getBitSize();
    }

    @Override
    public int getStartBitPos() {
      return atom.getStartBitPos();
    }

    @Override
    public Atom resize(final int newBitSize, final int newStartBitPos) {
      // Field extending is not currently supported.
      InvariantChecks.checkTrue(newBitSize <= getBitSize());
      InvariantChecks.checkTrue(newStartBitPos >= getStartBitPos());

      final Atom resized = atom.resize(newBitSize, newStartBitPos);
      return new AtomLogger(
          resized,
          name,
          originalBitSize,
          originalStartBitPos
          );
    }

    @Override
    public BitVector load(final boolean useHandler) {
      return atom.load(useHandler);
    }

    @Override
    public void store(final BitVector data, final boolean callHandler) {
      atom.store(data, callHandler);

      if (Tarmac.isEnabled()) {
        Tarmac.addRecord(Record.newRegisterWrite(getName(), data));
      }
    }
  }

  private final Type type;
  private final List<Atom> atoms;

  private Location(final Type type, final List<Atom> atoms) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotEmpty(atoms);

    this.type = type;
    this.atoms = atoms;
  }

  private Location(final Type type, final Atom atom) {
    this(type, atom != null ? Collections.singletonList(atom) : null);
  }

  private Location(final Data data) {
    this(
        data != null ? data.getType() : null,
        data != null ? new VariableAtom(data.getRawData()) : null
        );
  }

  public static Location newLocationForConst(final Data data) {
    InvariantChecks.checkNotNull(data);
    return new Location(data);
  }

  public static Location newLocationForAtom(final Type type, final Atom atom) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(atom);
    InvariantChecks.checkTrue(type.getBitSize() == atom.getBitSize());
    return new Location(type, atom);
  }

  public Location setName(final String name) {
    if (null == name || name.isEmpty()) {
      return this;
    }

    boolean isLoggerAdded = false;
    final List<Atom> newAtoms = new ArrayList<>(atoms.size());

    int bitPos = 0;
    final boolean needSuffix = atoms.size() > 1;

    for (final Atom atom : atoms) {
      final int bitSize = atom.getBitSize();
      if (atom instanceof LoggableAtom) {
        final String atomName = needSuffix ?
            String.format("%s<%d..%d>", name, bitPos + bitSize - 1, bitPos) :
            name
            ;

        newAtoms.add(new AtomLogger(atom, atomName));
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
    for (final Atom atom : atoms) {
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

  public Location assign(final Location source) {
    InvariantChecks.checkNotNull(source);
    store(source.load());
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

    final List<Atom> newAtoms = new ArrayList<>();

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

  public Location concat(final Location other) {
    InvariantChecks.checkNotNull(other);
    return Location.concat(this, other);
  }

  public static Location concat(final Location... locations) {
    InvariantChecks.checkNotEmpty(locations);

    if (locations.length == 1) {
      return locations[0];
    }

    int newBitSize = 0;
    final List<Atom> newAtoms = new ArrayList<>();

    for (final Location location : locations) {
      InvariantChecks.checkNotNull(location);
      newBitSize += location.getBitSize();
      newAtoms.addAll(location.atoms);
    }

    final Type newType = locations[0].getType().resize(newBitSize);
    return new Location(newType, newAtoms);
  }

  public Location repeat(final int count) {
    InvariantChecks.checkGreaterThanZero(count);

    if (count == 1) {
      return this;
    }

    final Location[] array = new Location[count];
    Arrays.fill(array, this);

    return concat(array); 
  }

  @Override
  public int getBitSize() {
    return type.getBitSize();
  }

  @Override
  public String toBinString() {
    final BitVector rawData = readData(false); 
    return rawData.toBinString();
  }

  @Override
  public BigInteger getValue() {
    final BitVector rawData = readData(false);
    return rawData.bigIntegerValue(false);
  }

  @Override
  public void setValue(final BigInteger value) {
    InvariantChecks.checkNotNull(value);

    final BitVector rawData = BitVector.valueOf(value, getBitSize());
    writeData(rawData, false);
  }

  private BitVector readData(
      final boolean callHandlers) {
    final BitVector[] dataItems = new BitVector[atoms.size()];
    for (int index = 0; index < atoms.size(); ++index) {
      final Atom atom = atoms.get(index);
      dataItems[index] = atom.load(callHandlers);
    }

    return BitVector.newMapping(dataItems).copy();
  }

  private void writeData(
      final BitVector data,
      final boolean callHandlers) {
    int position = 0;
    for (final Atom atom : atoms) {
      final BitVector dataItem =
          BitVector.newMapping(data, position, atom.getBitSize());

      atom.store(dataItem, callHandlers);
      position += dataItem.getBitSize();
    }
  }
}
