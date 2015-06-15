/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;

public final class LazyValue implements Value {
  private final LazyData data;
  private final int start;
  private final int size;

  LazyValue(final LazyData data) {
    checkNotNull(data);

    this.data = data;
    this.start = 0;
    this.size = 0; // This means that we will use all data.
  }

  LazyValue(final LazyData data, final int start, final int end) {
    checkNotNull(data);

    if ((start < 0) || (end < 0)) {
      throw new IllegalArgumentException();
    }

    this.data = data;
    this.start = Math.min(start, end);
    this.size = Math.abs(end - start) + 1;
  }

  @Override
  public BigInteger getValue() {
    final BitVector value = data.getValue();

    if (null == value) {
      throw new IllegalStateException("LazyData does not have a value.");
    }

    if (0 == start && (0 == size || value.getBitSize() == size)) {
      return value.bigIntegerValue(); // Use all data.
    }

    final BitVector mapping = BitVector.newMapping(value, start, size);
    return mapping.bigIntegerValue();
  }
}
