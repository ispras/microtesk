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

public abstract class Segment<PA extends Address<?>, VA extends Address<?>>
    implements Buffer<PA, VA> {

  private final BitVector start;
  private final BitVector end;

  public Segment(final BitVector start, final BitVector end) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(end);

    this.start = start;
    this.end = end;
  }

  @Override
  public boolean isHit(final VA address) {
    final BitVector value = address.getValue();
    return start.compareTo(value) <= 0 && end.compareTo(value) >= 0;
  }

  @Override
  public PA readEntry(final VA va) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeEntry(final VA address, final BitVector entry) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void allocEntry(final VA address) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void evictEntry(final VA address) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resetState() {
    // Do nothing.
  }
}
