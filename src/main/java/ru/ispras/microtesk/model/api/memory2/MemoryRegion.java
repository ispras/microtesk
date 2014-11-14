/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.memory2;

import ru.ispras.fortress.data.types.bitvector.BitVector;

public final class MemoryRegion {
  private final int bitSize;
  private final int index;
  private final BitVector data;

  public MemoryRegion(int bitSize, int index) {
    this(bitSize, index, null);
  }

  private MemoryRegion(int bitSize, int index, BitVector data) {
    if (bitSize <= 0) {
      throw new IllegalArgumentException("bitSize <= 0 : " + bitSize);
    }

    if (index < 0) {
      throw new IndexOutOfBoundsException("index < 0 : " + index);
    }

    if (null != data && bitSize != data.getBitSize()) {
      throw new IllegalArgumentException(String.format(
        "region size (%d) != data size (%d)", bitSize, data.getBitSize()));
    }

    this.bitSize = bitSize;
    this.index = index;
    this.data = data;
  }

  public MemoryRegion linkData(BitVector data) {
    if (null == data) {
      throw new NullPointerException();
    }

    return new MemoryRegion(bitSize, index, data);
  }

  public int getBitSize() {
    return bitSize;
  }

  public int getIndex() {
    return index;
  }

  public boolean hasData() {
    return null != data;
  }

  public BitVector getData() {
    return data;
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryRegion [bitSize=%d, index=%d, data=%s]", bitSize, index, data);
  }
}
