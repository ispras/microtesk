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

package ru.ispras.microtesk.model.api.memory;

import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.utils.InvariantChecks.checkBounds;
import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterThanZero;

import java.math.BigInteger;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * Minimum alignment = 1 byte (8 bits)
 * Maximum align is specified by user.
 * 
 * @author andrewt
 */

public final class MemoryAllocator {
  private final MemoryStorage memory;

  private final int baseRegionIndex;
  private final int baseOffset; // bits
  private final int addressableSize; // bits

  private int currentAddress;   // bytes

  public MemoryAllocator(MemoryStorage memory, int addressableSize) {
    this(memory, 0, 0, addressableSize);
  }

  public MemoryAllocator(
      MemoryStorage memory, int baseRegionIndex, int baseOffset, int addressableSize) {
    checkNotNull(memory);

    checkBounds(baseRegionIndex, memory.getRegionCount());
    checkBounds(baseOffset, memory.getRegionBitSize());

    checkGreaterThanZero(addressableSize);
    if (memory.getRegionBitSize() % addressableSize != 0) {
      throw new IllegalArgumentException(String.format(
          "Memory region size (%d) must be a multiple of addressable size (%d).",
          memory.getRegionBitSize(), addressableSize));
    }

    this.memory = memory;
    this.baseRegionIndex = baseRegionIndex;
    this.baseOffset = baseOffset;

    this.addressableSize = addressableSize;
    this.currentAddress = 0;
  }

  public int allocateData(Type type, List<BigInteger> data) {
    checkNotNull(type);
    checkNotNull(data);

    if (data.isEmpty()) {
      throw new IllegalArgumentException();
    }

    int address = 0;
    boolean isFirst = true;
    for (BigInteger dataItem : data) {
      final BitVector value = 
          BitVector.valueOf(dataItem.toByteArray(), type.getBitSize());
      final int allocatedAddress = allocateNext(value);
      if (isFirst) {
        address = allocatedAddress;
        isFirst = false;
      }
    }

    return address;
  }

  public int allocateSpace(Type type, int count, BigInteger fillWith) {
    checkNotNull(type);
    checkNotNull(fillWith);
    checkGreaterThanZero(count);

    final BitVector value = 
        BitVector.valueOf(fillWith.toByteArray(), type.getBitSize());

    int address = 0;
    for(int index = 0; index < count; ++index) {
      final int allocatedAddress = allocateNext(value);
      if (0 == index) {
        address = allocatedAddress;
      }
    }

    return address;
  }

  public int allocateAsciiStrings(List<String> strings, boolean zeroTerm) {
    checkNotNull(strings);
    if (strings.isEmpty()) {
      throw new IllegalArgumentException();
    }

    int address = 0;
    boolean isFirst = true;
    for (String string : strings) {
      final BitVector asciiString = toAsciiBinary(string, zeroTerm);
      final int allocatedAddress = allocateAcsiiString(asciiString);
      if (isFirst) {
        address = allocatedAddress;
        isFirst = false;
      }
    }

    return address;
  }

  private int allocateNext(BitVector value) {
    return 0;
  }

  private int allocateAcsiiString(BitVector asciiString) {
    // TODO Auto-generated method stub
    return 0;
  }

  private BitVector toAsciiBinary(String string, boolean zeroTerm) {
    //final byte[] stringBytes = string.getBytes("US-ASCII");
    return null;
  }

  /*
  private static int bitsToBytes(int bits) {
    checkGreaterThanZero(bits);
    final int quotient = bits / BitVector.BITS_IN_BYTE;
    return bits % BitVector.BITS_IN_BYTE == 0 ? quotient : quotient + 1; 
  }
  */
}
