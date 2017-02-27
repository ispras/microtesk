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

/**
 * The {@link LocationAtom} class is to be extended by all location atoms.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
abstract class LocationAtom {
  public abstract boolean isInitialized();

  //String getName();
  //BitVector getIndex();

  public abstract int getBitSize();
  public abstract int getStartBitPos();
  public abstract LocationAtom resize(int newBitSize, int newStartBitPos);

  public abstract BitVector load(boolean useHandler);
  public abstract void store(BitVector data, boolean callHandler);
}
