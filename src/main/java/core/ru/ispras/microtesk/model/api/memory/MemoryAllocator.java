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
import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterOrEqZero;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm;
import ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm.IOperation;
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
   * 
   * @throws NullPointerException if the parameter is {@code null}.
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
   * Allocates memory in the memory storage to hold the specified number of 
   * the specified data and returns the address (in addressable units) of
   * the first element. The data is aligned in the memory by its size
   * (in addressable units). Space between allocations is filled with zeros.
   * 
   * @param data Data to be placed in the memory storage.
   * @param count Number of copies to be placed in the memory storage.
   * @return Address of the allocated memory (in addressable units)
   * 
   * @throws NullPointerException if the parameter is {@code null}.
   */
 
  public int allocate(BitVector data, int count) {
    checkNotNull(data);
    checkGreaterThanZero(count);

    int address = 0;
    for(int index = 0; index < count; ++index) {
      final int allocatedAddress = allocate(data);
      if (0 == index) {
        address = allocatedAddress;
      }
    }

    return address;
  }

  /**
   * Allocates memory in the memory storage to store the specified string converted to
   * the ASCII encoding and returns the address of string. The ASCII string is copied 
   * to memory byte by byte so that each character could be addressable. Therefore,
   * each byte is aligned by the boundary of an addressable units. If any space is left
   * between characters, it is filled with zeros.
   *     
   * @param string String to be placed in the memory. 
   * @param zeroTerm Specifies whether the string must be terminated with zero.
   * @return Address of the allocated ASCII string (in addressable units).
   * 
   * @throws NullPointerException if the {@code string} parameter equals {@code null}.
   * @throws IllegalStateException if failed to convert the string to the "US-ASCII" encoding.
   */

  public int allocateAsciiString(String string, boolean zeroTerm) {
    checkNotNull(string);

    final BitVector data = toAsciiBinary(string, zeroTerm);

    final int dataBitSize = data.getBitSize();
    final int sizeInAddressableUnits = bitsToAddressableUnits(dataBitSize);

    int address = 0;
    int startBitPos = 0;

    for (int index = 0; index < sizeInAddressableUnits; ++index) {
      final int unitBitSize = Math.min(dataBitSize - startBitPos, addressableUnitBitSize);

      final BitVector unitData = BitVector.newMapping(data, startBitPos, unitBitSize);
      startBitPos += unitBitSize;

      final int allocatedAddress = allocate(unitData);
      if (index == 0) {
        address = allocatedAddress;
      }
    }

    return address;
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

  /** 
   * Aligns the specified address by the specified length and returns the resulting 
   * aligned address.  
   * 
   * @param address Address to be aligned.
   * @param alignment Alignment length.
   * @return Aligned address.
   * 
   * @throws IllegalArgumentException if any of the parameters is negative.
   */

  static int alignAddress(int address, int alignment) {
    checkGreaterOrEqZero(address);
    checkGreaterOrEqZero(alignment);

    final int unaligned = address % alignment;
    return unaligned == 0 ? address : address + (alignment - unaligned);
  }

  public int allocateData(Type type, BigInteger data) {
    checkNotNull(type);
    checkNotNull(data);

    final BitVector value = 
        BitVector.valueOf(data.toByteArray(), type.getBitSize());

    return allocate(value);
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

  /**
   * Converts the specified Java string to a bit vector that holds its 
   * ASCII representation.
   * 
   * @param string String to be converted.
   * @param zeroTerm Specifies whether the string must be terminated with zero.
   * @return ASCII representation of the string stored in a bit vector.
   * 
   * @throws NullPointerException if the {@code string} argument is {@code null}.
   * @throws IllegalStateException if failed to convert the string to the "US-ASCII" encoding.  
   */

  private BitVector toAsciiBinary(String string, boolean zeroTerm) {
    checkNotNull(string);

    final byte[] stringBytes;
    try {
      stringBytes = string.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }

    final int charCount = stringBytes.length + (zeroTerm ? 1 : 0);
    final int bitSize = charCount * BitVector.BITS_IN_BYTE;

    final BitVector result = BitVector.newEmpty(bitSize);
    final IOperation op = new IOperation() {
      private int index = 0;
      @Override
      public byte run() {
        return index < stringBytes.length ? stringBytes[index++] : 0;
      }
    };

    BitVectorAlgorithm.generate(result, op);
    return result;
  }
}
