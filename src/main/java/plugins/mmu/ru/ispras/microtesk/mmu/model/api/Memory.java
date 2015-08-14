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
import ru.ispras.microtesk.model.api.instruction.StandardFunctions;
import ru.ispras.microtesk.model.api.memory.MemoryAccessor;

public abstract class Memory <D, A extends Address>
    extends StandardFunctions implements Buffer<D, A>, MemoryAccessor {
  @Override
  public boolean isHit(final A address) {
    return true;
  }

  @Override
  public BitVector load(BitVector address) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void store(BitVector address, BitVector data) {
    // TODO Auto-generated method stub
    
  }
}
