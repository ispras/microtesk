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

package ru.ispras.microtesk.mmu.model.sample;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.model.api.Buffer;

/**
 * <pre><code>
 * segment XSUSEG(va: VA) = (pa : PA)
 * range = (0x0000000080000000, 0x000000ffffffffff)
 * </code></pre>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class XSUSEG implements Buffer<PA, VA> {

  private final Pair<BitVector, BitVector> range = new Pair<>(
      BitVector.valueOf("0000000080000000", 16, 64),
      BitVector.valueOf("000000ffffffffff", 16, 64)
  );

  public XSUSEG() {

  }

  @Override
  public boolean isHit(final VA address) {
    final BitVector value = address.getField("value");
    return range.first.compareTo(value)  <= 0 &&
           range.second.compareTo(value) >= 0;
  }

  @Override
  public PA getData(final VA va) {
    // NOT SUPPORTED
    throw new UnsupportedOperationException();
  }

  @Override
  public PA setData(final VA address, final PA data) {
    // NOT SUPPORTED
    throw new UnsupportedOperationException();
  }
}
