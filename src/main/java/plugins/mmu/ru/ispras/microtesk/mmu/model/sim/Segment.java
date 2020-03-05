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

public abstract class Segment<PA extends Address<?>, VA extends Address<?>> extends Buffer<PA, VA> {
  private final BitVector start;
  private final BitVector end;

  public Segment(
      final Address<PA> targetCreator,
      final Address<VA> sourceCreator,
      final BitVector start,
      final BitVector end) {
    super(targetCreator, sourceCreator);

    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(end);

    this.start = start;
    this.end = end;
  }

  @Override
  public boolean isHit(final VA address) {
    InvariantChecks.checkNotNull(address);
    final BitVector value = address.getValue();
    return start.compareTo(value) <= 0 && end.compareTo(value) >= 0;
  }

  @Override
  public PA getData(final VA va) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PA setData(final VA address, final BitVector data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pair<BitVector, BitVector> seeData(BitVector index, BitVector way) {
    throw new UnsupportedOperationException();
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
