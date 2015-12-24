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

package ru.ispras.microtesk.model.api.memory;

import static ru.ispras.fortress.util.InvariantChecks.checkBounds;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;

final class AliasForMemory extends Memory {
  private final int itemBitSize;
  private final int sourceItemBitSize;
  private final Memory source;
  private final int base; 

  public AliasForMemory(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length,
      final Memory source,
      final int min, 
      final int max
      ) {
    super(kind, name, type, length, true);
    checkNotNull(source);

    checkBounds(min, source.getLength().intValue());
    checkBounds(max, source.getLength().intValue());

    this.itemBitSize = type.getBitSize();
    this.sourceItemBitSize = source.getType().getBitSize();

    if (itemBitSize > sourceItemBitSize) {
      throw new IllegalArgumentException(String.format(
          "Alias data size (%d) must be greater or equal to %s data size (%d).",
          sourceItemBitSize, name, itemBitSize));
    }

    final int bitSize = itemBitSize * length.intValue();
    final int sourceLength = Math.abs(max - min) + 1;
    final int sourceBitSize = sourceItemBitSize * sourceLength;

    if (bitSize != sourceBitSize) {
      throw new IllegalArgumentException(String.format(
          "Alias size mismatch. Expected alias size: %d.", bitSize));
    }

    this.source = source;
    this.base = Math.min(min, max);
  }

  @Override
  public Location access(final int address) {
    return access((long) address);
  }

  /*
   * TODO:
   * NOTE: the implementation based on longs has limitations.
   * But we usually dont use aliases for registers. So it implies 
   * that the index value is small enough to fit a Java long value
   * without causing any troubles. 
   */

  @Override
  public Location access(final long address) {
    if (itemBitSize == sourceItemBitSize) {
      return source.access(base + address);
    }

    final long relativeBitOffset = address * itemBitSize;
    final long sourceAddress = base  + relativeBitOffset / sourceItemBitSize;

    final Location sourceLocation = source.access(sourceAddress);
    final int start = (int) (relativeBitOffset % sourceItemBitSize);

    return sourceLocation.bitField(start, start + itemBitSize - 1);
  }

  @Override
  public Location access(final BigInteger address) {
    return access(address.longValue());
  }

  @Override
  public Location access(final Data address) {
    return access(address.getRawData().longValue());
  }

  @Override
  public void reset() {
    // Does not work for aliases (and should not be called)
  }

  @Override
  public void setUseTempCopy(boolean value) {
    // Do nothing
  }
}
