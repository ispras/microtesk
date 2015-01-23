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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import ru.ispras.fortress.data.types.bitvector.BitVector;

public final class Segment {
  private final String name;
  private final BitVector from;
  private final BitVector to;

  Segment(String name, BitVector from, BitVector to) {
    checkNotNull(name);
    checkNotNull(from);
    checkNotNull(to);

    if (from.getBitSize() != to.getBitSize()) {
      throw new IllegalArgumentException(String.format(
          "Size mistach: %d against %d", from.getBitSize(), to.getBitSize()));
    }

    this.name = name;
    this.from = from;
    this.to = to;
  }

  public String getName() {
    return name;
  }

  public BitVector getFrom() {
    return from;
  }

  public BitVector getTo() {
    return to;
  }
}
