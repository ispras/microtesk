/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.memory.MemoryDevice;

public abstract class Mmu<A extends Address<?>>
    extends Buffer<BitVector, A> implements MemoryDevice {

  public Mmu(final Address<A> addressCreator) {
    super(new Struct<BitVector>() {
      @Override public BitVector newStruct(BitVector value) {
        return value;
      }
      @Override public BitVector asBitVector() {
        return null;
      }
    }, addressCreator);
  }

  @Override
  public boolean isInitialized(final BitVector address) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkTrue(address.getBitSize() == getAddressBitSize());
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isHit(final A address) {
    return true;
  }

  @Override
  public BitVector load(final BitVector address) {
    return loadEntry(toAddress(address));
  }

  @Override
  public void store(final BitVector address, final int offset, final BitVector data) {
    // FIXME: NEED PROPER IMPLEMENTATION
    InvariantChecks.checkTrue(offset == 0);
    store(address, data);
  }

  @Override
  public void store(final BitVector address, final BitVector data) {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkTrue(data.getBitSize() == getDataBitSize(),
        String.format("Data size mismatch: storing %d-bit data to %d-bit location",
            data.getBitSize(), getDataBitSize()));

    storeEntry(toAddress(address), data);
  }

  @Override
  public Pair<BitVector, BitVector> seeData(BitVector index, BitVector way) {
    throw new UnsupportedOperationException();
  }

  private A toAddress(final BitVector value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(value.getBitSize() == getAddressBitSize(),
        String.format("Address size mismatch: storing %d-bit address to %d-bit location",
            value.getBitSize(), getAddressBitSize()));

    final A address = addressCreator.setValue(value);
    Operation.initAddress(address);

    return address;
  }

  @Override
  public void setUseTempState(final boolean value) {
    // Do nothing.
  }

  @Override
  public void resetState() {
    // Do nothing.
  }
}
