/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LazyValue.java, Oct 8, 2014 8:46:10 PM Andrei Tatarnikov
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

import ru.ispras.fortress.data.types.bitvector.BitVector;

public final class LazyValue {
  private final LazyData data;
  private final int start;
  private final int size;

  public LazyValue(LazyData data) {
    if (null == data) {
      throw new NullPointerException();
    }

    this.data = data;
    this.start = 0;
    this.size = 0; // This means that we will use all data.
  }

  public LazyValue(LazyData data, int start, int end) {
    if (null == data) {
      throw new NullPointerException();
    }

    if ((start < 0) || (end < 0)) {
      throw new IllegalArgumentException();
    }

    this.data = data;
    this.start = Math.min(start, end);
    this.size = Math.abs(end - start) + 1;
  }

  public int getValue() {
    final BitVector value = data.getValue();

    if (null == value) {
      throw new IllegalStateException("LazyData does not have a value.");
    }

    if (0 == start && (0 == size || value.getBitSize() == size)) {
      return value.intValue(); // Use all data.
    }

    final BitVector mapping = BitVector.newMapping(value, start, size);
    return mapping.intValue();
  }
}
