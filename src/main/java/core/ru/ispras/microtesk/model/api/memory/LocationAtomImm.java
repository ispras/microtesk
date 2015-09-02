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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.Location.Atom;

final class LocationAtomImm implements Location.Atom {
  private final BitVector value;
  private final int bitSize;
  private final int startBitPos;

  protected LocationAtomImm(final BitVector value) {
    InvariantChecks.checkNotNull(value);
    this.value = value;
    this.bitSize = value.getBitSize();
    this.startBitPos = 0;
  }

  private LocationAtomImm(
      final BitVector value,
      final int bitSize,
      final int startBitPos) {
    InvariantChecks.checkNotNull(value);

    InvariantChecks.checkGreaterThanZero(bitSize);
    InvariantChecks.checkGreaterOrEqZero(startBitPos);

    InvariantChecks.checkBounds(startBitPos, value.getBitSize());
    InvariantChecks.checkBounds(startBitPos + bitSize, value.getBitSize());

    this.value = value;
    this.bitSize = bitSize;
    this.startBitPos = startBitPos;
  }

  @Override
  public boolean isInitialized() {
    return true;
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
    return new LocationAtomImm(value, newBitSize, newStartBitPos);
  }

  @Override
  public BitVector load() {
    if (value.getBitSize() == bitSize) {
      return value;
    } 

    return BitVector.newMapping(value, startBitPos, bitSize);
  }

  @Override
  public void store(final BitVector data) {
    throw new UnsupportedOperationException(
        "Assigning values to immediate arguments is illegal.");
  }
}
