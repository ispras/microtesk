/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim.model;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.mmu.model.sim.Address;
import ru.ispras.microtesk.mmu.model.sim.StructBase;

public final class PA extends StructBase<PA> implements Address<PA> {

  public final BitVector value = BitVector.newEmpty(32);

  public PA() {
    setEntry(BitVector.newMapping(value));
  }

  public PA(final BitVector value) {
    this();
    assign(value);
  }

  public void assign(final PA other) {
    this.value.assign(other.value);
  }

  @Override
  public PA newStruct(final BitVector value) {
    return new PA(value);
  }

  @Override
  public String toString() {
    return String.format("PA [value=0x%s]", value.toHexString());
  }

  @Override
  public BitVector getValue() {
    return value;
  }

  @Override
  public PA setValue(BitVector value) {
    final PA address = new PA();
    address.value.assign(value);
    return address;
  }
}
