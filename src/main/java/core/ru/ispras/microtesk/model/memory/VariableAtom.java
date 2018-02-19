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

package ru.ispras.microtesk.model.memory;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link VariableAtom} class is a location atom used in variables and
 * immediate values. It does not track modification and does require logging.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class VariableAtom extends LocationAtom {
  private final BitVector value;

  protected VariableAtom(final String memory, final BitVector index, final BitVector value) {
    super(memory, index, null != value ? value.getBitSize() : 0, 0);
    InvariantChecks.checkNotNull(value);
    this.value = value;
  }

  private VariableAtom(
      final VariableAtom other,
      final int bitSize,
      final int startBitPos) {
    super(other, bitSize, startBitPos);

    InvariantChecks.checkGreaterThanZero(bitSize);
    InvariantChecks.checkGreaterOrEqZero(startBitPos);

    InvariantChecks.checkBounds(startBitPos, other.value.getBitSize());
    InvariantChecks.checkBoundsInclusive(startBitPos + bitSize, other.value.getBitSize());

    this.value = other.value;
  }

  @Override
  public int getStorageBitSize() {
    return value.getBitSize();
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public LocationAtom resize(final int newBitSize, final int newStartBitPos) {
    return new VariableAtom(this, newBitSize, newStartBitPos);
  }

  @Override
  public BitVector load(final boolean callHandler) {
    return BitVector.newMapping(value, getBitFieldStart(), getBitFieldSize());
  }

  @Override
  public void store(final BitVector data, final boolean callHandler) {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkTrue(data.getBitSize() == getBitFieldSize());

    final BitVector target = BitVector.newMapping(value, getBitFieldStart(), getBitFieldSize());
    target.assign(data);
  }
}
