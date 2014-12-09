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
import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterThanZero;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

public final class MemoryAllocator {
  private final MemoryStorage memory;

  private final int addressableSize; // bits
  private final int addressableUnitsInRegion;

  private int currentAddress; // addressable units

  public MemoryAllocator(MemoryStorage memory, int addressableSize) {
    checkNotNull(memory);

    checkGreaterThanZero(addressableSize);
    if (memory.getRegionBitSize() % addressableSize != 0) {
      throw new IllegalArgumentException(String.format(
          "Memory region size (%d) must be a multiple of addressable size (%d).",
          memory.getRegionBitSize(), addressableSize));
    }

    this.memory = memory;
    this.addressableSize = addressableSize;
    this.addressableUnitsInRegion = memory.getRegionBitSize() / addressableSize;

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

  public int allocateSpace(Type type, int count, int fillWith) {
    checkNotNull(type);
    checkNotNull(fillWith);
    checkGreaterThanZero(count);

    final BitVector value = BitVector.valueOf(fillWith, type.getBitSize());

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
    final int sizeInAddressableUnits =
        bitsToAddressableUnits(value.getBitSize());
    
    System.out.println(value.getBitSize());
    System.out.println("sizeInAddressableUnits = " + sizeInAddressableUnits);

    final int allocatedAddress = 
        alignAddress(currentAddress, sizeInAddressableUnits);

    writeToMemory(value, allocatedAddress, sizeInAddressableUnits);
    currentAddress = allocatedAddress + sizeInAddressableUnits;

    return allocatedAddress;
  }

  private void writeToMemory(BitVector value, int address, int sizeInUnits) {
    System.out.println(value);
    
    int regionIndex = address / addressableUnitsInRegion;
    int bitOffset = (address % addressableUnitsInRegion) * addressableSize;

    final int bitSize = value.getBitSize();
    int bitPosition = 0;
    while (bitPosition < bitSize) {
      final int bitsToWrite = Math.min(bitSize - bitPosition, memory.getRegionBitSize() - bitOffset);
      System.out.println(bitPosition + " - " + bitSize + " - " + bitsToWrite);
      final BitVector sourceData = BitVector.newMapping(value, bitPosition, bitsToWrite);
      
      final BitVector dataToWrite;
      if (bitsToWrite == memory.getRegionBitSize()) {
        dataToWrite = sourceData;
      } else {
        final BitVector dataToMerge = memory.read(regionIndex);
        dataToWrite = dataToMerge.copy();
        final BitVector mapping = BitVector.newMapping(dataToWrite, bitOffset, bitsToWrite);
        mapping.assign(sourceData);
      }

      memory.write(regionIndex, dataToWrite);
      bitPosition += bitsToWrite;

      regionIndex++;
      bitOffset = 0;
    }
  }

  private int allocateAcsiiString(BitVector asciiString) {
    final int sizeInAddressableUnits = 
        bitsToAddressableUnits(asciiString.getBitSize());

    int address = 0;
    int startBitPos = 0;
    for (int index = 0; index < sizeInAddressableUnits; ++index) {
      final int bitSize = 
          Math.min(asciiString.getBitSize() - startBitPos, addressableSize); 

      final BitVector value = BitVector.newMapping(asciiString, startBitPos, bitSize);
      startBitPos += bitSize;

      final int allocatedAddress = allocateNext(value);
      if (index == 0) {
        address = allocatedAddress;
      }
    }

    return address;
  }

  private BitVector toAsciiBinary(String string, boolean zeroTerm) {
    final byte[] stringBytes;
    try {
      stringBytes = string.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }

    final int bitSize = 
        (stringBytes.length + (zeroTerm ? 1 : 0)) * BitVector.BITS_IN_BYTE;

    return BitVector.valueOf(stringBytes, bitSize);
  }

  private int bitsToAddressableUnits(int bitSize) {
    System.out.println(addressableSize);
    System.out.printf("|%d - %d|%n", bitSize, addressableSize);
    
    return (bitSize / addressableSize) + bitSize % addressableSize == 0 ? 0 : 1;
  }

  private static int alignAddress(int address, int alignment) {
    return address % alignment == 0 ? address : address + alignment;
  }
}
