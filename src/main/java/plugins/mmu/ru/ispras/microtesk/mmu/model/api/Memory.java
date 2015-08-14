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

package ru.ispras.microtesk.mmu.model.api;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.instruction.StandardFunctions;
import ru.ispras.microtesk.model.api.memory.MemoryAccessor;

public abstract class Memory <A extends Address>
    extends StandardFunctions implements Buffer<BitVector, A>, MemoryAccessor {
  @Override
  public boolean isHit(final A address) {
    return true;
  }

  @Override
  public BitVector load(final BitVector address) {
    return getData(toAddress(address));
  }

  @Override
  public void store(final BitVector address, final BitVector data) {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkTrue(data.getBitSize() == getDataBitSize(), "Data size mismatch");

    setData(toAddress(address), data);
  }

  private A toAddress(final BitVector value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(value.getBitSize() == getAddressBitSize(), "Address size mismatch");

    final A address = newAddress();
    address.getValue().assign(value);

    return address;
  }

  protected abstract A newAddress();
}
