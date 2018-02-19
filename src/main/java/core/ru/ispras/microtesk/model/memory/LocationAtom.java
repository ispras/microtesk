/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

/**
 * The {@link LocationAtom} class is to be extended by all location atoms.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
abstract class LocationAtom {
  private final String storage;
  private final BitVector index;
  private final int bitFieldStart;
  private final int bitFieldSize;

  protected LocationAtom(
      final String storage,
      final BitVector index,
      final int bitFieldSize,
      final int bitFieldStart) {
    this.storage = storage;
    this.index = index;
    this.bitFieldStart = bitFieldStart;
    this.bitFieldSize = bitFieldSize;
  }

  protected LocationAtom(final LocationAtom other) {
    InvariantChecks.checkNotNull(other);

    this.storage = other.storage;
    this.index = other.index;
    this.bitFieldSize = other.bitFieldSize;
    this.bitFieldStart = other.bitFieldStart;
  }

  protected LocationAtom(
      final LocationAtom other,
      final int newBitFieldSize,
      final int newBitFieldStart) {
    InvariantChecks.checkNotNull(other);

    this.storage = other.storage;
    this.index = other.index;
    this.bitFieldStart = newBitFieldStart;
    this.bitFieldSize = newBitFieldSize;
  }

  public boolean isLoggable() {
    return false;
  }

  public final String getMemory() {
    return storage;
  }

  public final BitVector getIndex() {
    return index;
  }

  public final int getBitFieldStart() {
    return bitFieldStart;
  }

  public final int getBitFieldSize() {
    return bitFieldSize;
  }

  public abstract int getStorageBitSize();

  public abstract boolean isInitialized();

  public abstract LocationAtom resize(int bitFieldSize, int bitFieldStart);

  public abstract BitVector load(boolean useHandler);

  public abstract void store(BitVector data, boolean callHandler);

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    if (null != storage) {
      sb.append(storage);
    }

    if (null != index) {
      sb.append(String.format("[0x%s]", index.toHexString()));
    }

    if (bitFieldStart != 0 || bitFieldSize != getStorageBitSize()) {
      sb.append(String.format("<%d..%d>", bitFieldStart, bitFieldStart + bitFieldSize - 1));
    }

    return sb.toString();
  }
}
