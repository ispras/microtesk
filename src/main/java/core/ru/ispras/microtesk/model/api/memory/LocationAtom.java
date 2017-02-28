/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

/**
 * The {@link LocationAtom} class is to be extended by all location atoms.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
abstract class LocationAtom {
  private final int bitSize;
  private final int startBitPos;

  protected LocationAtom(final LocationAtom other) {
    InvariantChecks.checkNotNull(other);
    this.bitSize = other.bitSize;
    this.startBitPos = other.startBitPos;
  }

  protected LocationAtom(final int bitSize, final int startBitPos) {
    this.bitSize = bitSize;
    this.startBitPos = startBitPos;
  }

  public final int getBitSize() {
    return bitSize;
  }

  public final int getStartBitPos() {
    return startBitPos;
  }

  //String getName();
  //BitVector getIndex();

  public abstract boolean isInitialized();
  public abstract LocationAtom resize(int newBitSize, int newStartBitPos);

  public abstract BitVector load(boolean useHandler);
  public abstract void store(BitVector data, boolean callHandler);
}
