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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The job of the MemoryAllocator class is to place data in the memory storage.
 * 
 * @author Andrei Tatarnikov
 */

public final class MemoryAllocator {
  private final MemoryStorage memory;

  private final int addressableUnitBitSize;
  private final int addressableUnitsInRegion;

  private int currentAddress; // in addressable units
  
  private static String ERROR_INVALID_SIZE = 
      "Memory region size (%d) must be a multiple of addressable unit size (%d).";

  /**
   * Constructs a memory allocator object with the specified parameters.
   * Important precondition: memory region size must be a multiple of addressable unit size.
   * 
   * @param memory Memory storage to store the data.
   * @param addressableBitSize Size of an addressable unit in bits.
   * 
   * @throws NullPointerException if the {@code memory} parameter is {@code null}.
   * @throws IllegalArgumentException if the specified size of an addressable unit
   * is negative or is not a divisor of memory region size.
   */

  public MemoryAllocator(MemoryStorage memory, int addressableBitSize) {
    checkNotNull(memory);
    checkGreaterThanZero(addressableBitSize);

    final int regionBitSize = memory.getRegionBitSize();
    if (regionBitSize % addressableBitSize != 0) {
      throw new IllegalArgumentException(String.format(
          ERROR_INVALID_SIZE, regionBitSize, addressableBitSize));
    }

    this.memory = memory;
    this.addressableUnitBitSize = addressableBitSize;
    this.addressableUnitsInRegion = regionBitSize / addressableBitSize;

    this.currentAddress = 0;
  }

  /**
   * Returns the current address.
   * @return Current address (in addressable units).
   */

  public int getCurrentAddress() {
    return currentAddress;
  }

  /**
   * Returns the size of an addressable unit.
   * @return Size of an addressable unit in bits.
   */

  public int getAddressableUnitBitSize() {
    return addressableUnitBitSize;
  }

  /**
   * Returns the size of memory regions stored in the memory storage. 
   * @return Bit size of memory regions stored in the memory storage.
   */

  public int getRegionBitSize() {
    return memory.getRegionBitSize();
  }

  /** 
   * Returns the number of addressable units in a memory region.
   * @return Number of addressable units in a memory region
   */

  public int getAddressableUnitsInRegion() {
    return addressableUnitsInRegion;
  }

  /**
   * Allocates memory in the memory storage to hold the specified data and
   * returns its address (in addressable units). The data is aligned in
   * the memory by its size (in addressable units). Space between allocations
   * is filled with zeros. 
   * 
   * @param data Data to be stored in the memory storage.
   * @return Address of the allocated memory (in addressable units).
   */

  public int allocate(BitVector data) {
    checkNotNull(data);

    final int sizeInAddressableUnits = bitsToAddressableUnits(data.getBitSize());
    final int allocatedAddress = alignAddress(currentAddress, sizeInAddressableUnits);

    currentAddress = allocatedAddress + sizeInAddressableUnits;
    writeToMemory(data, allocatedAddress, sizeInAddressableUnits);

    return allocatedAddress;
  }

  /**
   * Returns the minimal number of addressable units required to store data of
   * the specified size (in bits). 
   * 
   * @param bitSize Size in bits.
   * @return Size in addressable units.
   * 
   * @throws IllegalArgumentException if the {@code bitSize} argument is 0 or negative.
   */

  int bitsToAddressableUnits(int bitSize) {
    checkGreaterThanZero(bitSize);
    return bitSize / addressableUnitBitSize + (bitSize % addressableUnitBitSize == 0 ? 0 : 1);
  }

  /* TODO: Description + Unit tests */
  static int alignAddress(int address, int alignment) {
    return address % alignment == 0 ? address : address + (alignment - address % alignment);
  }

  public int allocateData(Type type, BigInteger data) {
    checkNotNull(type);
    checkNotNull(data);

    final BitVector value = 
        BitVector.valueOf(data.toByteArray(), type.getBitSize());

    return allocate(value);
  }

  public int allocateSpace(Type type, int count, int fillWith) {
    checkNotNull(type);
    checkNotNull(fillWith);
    checkGreaterThanZero(count);

    final BitVector value = BitVector.valueOf(fillWith, type.getBitSize());

    int address = 0;
    for(int index = 0; index < count; ++index) {
      final int allocatedAddress = allocate(value);
      if (0 == index) {
        address = allocatedAddress;
      }
    }

    return address;
  }

  public int allocateAsciiString(String string, boolean zeroTerm) {
    checkNotNull(string);

    final BitVector asciiString = toAsciiBinary(string, zeroTerm);
    return allocateAcsiiString(asciiString);
  }
  
  @Override
  public String toString() {
    return String.format(
        "MemoryAllocator [memory=%s, addressableUnitBitSize=%d, addressableUnitsInRegion=%d]",
        memory, addressableUnitBitSize, addressableUnitsInRegion);
  }

  private void writeToMemory(BitVector value, int address, int sizeInUnits) {
    int regionIndex = address / addressableUnitsInRegion;
    int bitOffset = (address % addressableUnitsInRegion) * addressableUnitBitSize;

    final int bitSize = value.getBitSize();
    int bitPosition = 0;
    while (bitPosition < bitSize) {
      final int bitsToWrite = Math.min(bitSize - bitPosition, memory.getRegionBitSize() - bitOffset);
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
          Math.min(asciiString.getBitSize() - startBitPos, addressableUnitBitSize); 

      final BitVector value = BitVector.newMapping(asciiString, startBitPos, bitSize);
      startBitPos += bitSize;

      final int allocatedAddress = allocate(value);
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
}
