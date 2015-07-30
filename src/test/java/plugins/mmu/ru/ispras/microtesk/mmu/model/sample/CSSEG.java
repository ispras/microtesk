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
import ru.ispras.microtesk.mmu.model.api.Segment;

/**
 * <pre><code>
 * segment CSSEG(va: VA) = (pa : PA)
 * range = (0xffffffffc0000000, 0xffffffffdfffffff)
 * </code></pre>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class CSSEG extends Segment<PA, VA> {
  public CSSEG() {
    super(
        BitVector.valueOf("ffffffffc0000000", 16, 64),
        BitVector.valueOf("ffffffffdfffffff", 16, 64)
    );
  }
}
