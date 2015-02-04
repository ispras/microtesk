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
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterOrEqZero;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;

import ru.ispras.fortress.data.types.bitvector.BitVector;

public final class Field {
  private final String id;
  private final int bitPos;
  private final int bitSize;
  private final BitVector defValue;

  public Field(String id, int bitPos, int bitSize, BitVector defValue) {
    checkNotNull(id);
    checkGreaterOrEqZero(bitPos);
    checkGreaterThanZero(bitSize);

    if (null != defValue && bitSize != defValue.getBitSize()) {
      throw new IllegalArgumentException(
          "Illegal size of the default value: " + defValue.getBitSize());
    }

    this.id = id;
    this.bitPos = bitPos;
    this.bitSize = bitSize;
    this.defValue = defValue;
  }

  public String getId() {
    return id;
  }

  public int getBitPos() {
    return bitPos;
  }

  public int getBitSize() {
    return bitSize;
  }

  public BitVector getDefValue() {
    return defValue;
  }

  @Override
  public String toString() {
    final int endBitPos = bitPos + bitSize - 1;
    final String defValueText = null != defValue ? ", 0x" + defValue.toHexString() : "";

    return String.format("field %s(%d, [%d..%d]%s)",
        id, bitSize, bitPos, endBitPos, defValueText);  
  }
}
