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

package ru.ispras.microtesk.model.api.memory;

import static ru.ispras.fortress.util.InvariantChecks.checkGreaterOrEq;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterOrEqZero;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * The job of the MemoryAllocator class is to place data in the memory storage.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MemoryAllocator {
  private final MemoryStorage memory;

  private final int addressableUnitBitSize;
  private final int addressableUnitsInRegion;

  private BigInteger currentAddress; // in addressable units

  private static String ERROR_INVALID_SIZE =
      "Memory region size (%d) must be a multiple of addressable unit size (%d).";

  /**
   * Constructs a memory allocator object with the specified parameters. Important precondition:
   * memory region size must be a multiple of addressable unit size.
   * 
   * @param memory Memory storage to store the data.
   * @param addressableUnitBitSize Size of an addressable unit in bits.
   * @param baseAddress Address where allocation starts.
   * 
   * @throws IllegalArgumentException if the {@code memory} parameter is {@code null};
   *         if the specified size of an addressable unit is negative or is not a divisor
   *         of memory region size.
   */

  public MemoryAllocator(
      final MemoryStorage memory,
      final int addressableUnitBitSize,
      final BigInteger baseAddress) {

    checkNotNull(memory);
    checkGreaterThanZero(addressableUnitBitSize);

    final int regionBitSize = memory.getRegionBitSize();
    if (regionBitSize % addressableUnitBitSize != 0) {
      throw new IllegalArgumentException(String.format(
          ERROR_INVALID_SIZE, regionBitSize, addressableUnitBitSize));
    }

    this.memory = memory;
    this.addressableUnitBitSize = addressableUnitBitSize;
    this.addressableUnitsInRegion = regionBitSize / addressableUnitBitSize;

    this.currentAddress = baseAddress;
  }
  
  public MemoryAllocator(
      final MemoryStorage memory,
      final int addressableUnitBitSize) {
    this(memory, addressableUnitBitSize, BigInteger.ZERO);
  }

  /**
   * Returns the current address.
   * 
   * @return Current address (in addressable units).
   */

  public BigInteger getCurrentAddress() {
    return currentAddress;
  }

  /**
   * Returns the size of an addressable unit.
   * 
   * @return Size of an addressable unit in bits.
   */

  public int getAddressableUnitBitSize() {
    return addressableUnitBitSize;
  }

  /**
   * Returns the size of memory regions stored in the memory storage.
   * 
   * @return Bit size of memory regions stored in the memory storage.
   */

  public int getRegionBitSize() {
    return memory.getRegionBitSize();
  }

  /**
   * Returns the number of addressable units in a memory region.
   * 
   * @return Number of addressable units in a memory region
   */

  public int getAddressableUnitsInRegion() {
    return addressableUnitsInRegion;
  }

  /**
   * Allocates memory in the memory storage to hold the specified data and returns its address (in
   * addressable units). The data is aligned in the memory by its size (in addressable units). Space
   * between allocations is filled with zeros.
   * 
   * @param data Data to be stored in the memory storage.
   * @return Address of the allocated memory (in addressable units).
   * 
   * @throws IllegalArgumentException if the parameter is {@code null}.
   */

  public BigInteger allocate(final BitVector data) {
    checkNotNull(data);
    final int dataBitSize = data.getBitSize();

    final int sizeInAddressableUnits = bitsToAddressableUnits(dataBitSize);
    final BigInteger address = alignAddress(currentAddress, sizeInAddressableUnits);

    BigInteger regionIndex = regionIndexForAddress(address);
    int regionBitOffset = regionBitOffsetForAddress(address);

    int bitPos = 0;
    while (bitPos < dataBitSize) {
      final int bitsToWrite = Math.min(dataBitSize - bitPos, getRegionBitSize() - regionBitOffset);
      final BitVector dataItem = BitVector.newMapping(data, bitPos, bitsToWrite);

      final BitVector dataToWrite;
      if (bitsToWrite == memory.getRegionBitSize()) {
        dataToWrite = dataItem;
      } else {
        final BitVector dataToMerge = memory.read(regionIndex);
        dataToWrite = dataToMerge.copy();
        final BitVector mapping = BitVector.newMapping(dataToWrite, regionBitOffset, bitsToWrite);
        mapping.assign(dataItem);
      }

      memory.write(regionIndex, dataToWrite);
      bitPos += bitsToWrite;

      regionIndex = regionIndex.add(BigInteger.ONE);
      regionBitOffset = 0;
    }

    currentAddress = address.add(BigInteger.valueOf(sizeInAddressableUnits));
    return address;
  }

  private int regionBitOffsetForAddress(final BigInteger address) {
    return address.mod(BigInteger.valueOf(addressableUnitsInRegion)).intValue() * addressableUnitBitSize;
  }

  private BigInteger regionIndexForAddress(final BigInteger address) {
    return address.divide(BigInteger.valueOf(addressableUnitsInRegion));
  }

  /**
   * Allocates memory in the memory storage to hold the specified number of the specified data and
   * returns the address (in addressable units) of the first element. The data is aligned in the
   * memory by its size (in addressable units). Space between allocations is filled with zeros.
   * 
   * @param data Data to be placed in the memory storage.
   * @param count Number of copies to be placed in the memory storage.
   * @return Address of the allocated memory (in addressable units)
   * 
   * @throws IllegalArgumentException if the parameter is {@code null}.
   */

  public BigInteger allocate(final BitVector data, final int count) {
    checkNotNull(data);
    checkGreaterThanZero(count);

    BigInteger address = BigInteger.ZERO;
    for (int index = 0; index < count; ++index) {
      final BigInteger allocatedAddress = allocate(data);
      if (0 == index) {
        address = allocatedAddress;
      }
    }

    return address;
  }
  
  /**
   * Allocates memory in the memory storage to hold data elements provided as arguments
   * and return the address (in addressable units) of the first element. The data is aligned
   * in memory by the size of data elements (in addressable units). Space between allocations
   * (if any is left) is filled with zeros.
   * 
   * @param data Collection of data elements to be stored in the memory storage.
   * @return Address of the first allocated element.
   * 
   * @throws IllegalArgumentException if the list is empty or it list elements have
   * different sizes. 
   */

  public BigInteger allocate(final BitVector... data) {
    checkGreaterThanZero(data.length);
    return allocate(Arrays.asList(data));
  }

  /**
   * Allocates memory in the memory storage to hold data elements in the specified list
   * and return the address (in addressable units) of the first element. The data is aligned
   * in memory by the size of data elements (in addressable units). Space between allocations
   * (if any is left) is filled with zeros.
   * 
   * @param data Collection of data elements to be stored in the memory storage.
   * @return Address of the first allocated element.
   * 
   * @throws IllegalArgumentException if the parameter is {@code null};
   *         if the list is empty or it list elements have different sizes.
   */

  public BigInteger allocate(final List<BitVector> data) {
    checkNotNull(data);

    if (data.isEmpty()) {
      throw new IllegalArgumentException("The list is empty.");
    }

    checkEqualBitSize(data);

    final Iterator<BitVector> dataIt = data.iterator();
    final BigInteger address = allocate(dataIt.next());

    while (dataIt.hasNext()) {
      allocate(dataIt.next());
    }

    return address;
  }

  /**
   * Checks whether all bit vectors in the list have the same size and throws an exception of the
   * they do not.
   * 
   * @param data List of bit vectors to be checked.
   * @throws IllegalArgumentException if bit vectors in the list have different sizes.
   */

  private static void checkEqualBitSize(final List<BitVector> data) {
    final Iterator<BitVector> it = data.iterator();
    final BitVector first = it.next();

    while (it.hasNext()) {
      final BitVector current = it.next();
      if (current.getBitSize() != first.getBitSize()) {
        throw new IllegalArgumentException("All data items must have the same bit size!");
      }
    }
  }

  /**
   * Allocates memory in the memory storage to store the specified string converted to the ASCII
   * encoding and returns the address of string. The ASCII string is copied to memory byte by byte
   * so that each character could be addressable. Therefore, each byte is aligned by the boundary of
   * an addressable units. If any space is left between characters, it is filled with zeros.
   * 
   * @param string String to be placed in the memory.
   * @param zeroTerm Specifies whether the string must be terminated with zero.
   * @return Address of the allocated ASCII string (in addressable units).
   * 
   * @throws IllegalArgumentException if the {@code string} parameter equals {@code null};
   *         if failed to convert the string to the "US-ASCII" encoding.
   */

  public BigInteger allocateAsciiString(final String string, final boolean zeroTerm) {
    checkNotNull(string);

    final BitVector data = toAsciiBinary(string, zeroTerm);

    final int dataBitSize = data.getBitSize();
    final int sizeInAddressableUnits = bitsToAddressableUnits(dataBitSize);

    BigInteger address = BigInteger.ZERO;
    int startBitPos = 0;

    for (int index = 0; index < sizeInAddressableUnits; ++index) {
      final int unitBitSize = Math.min(dataBitSize - startBitPos, addressableUnitBitSize);

      final BitVector unitData = BitVector.newMapping(data, startBitPos, unitBitSize);
      startBitPos += unitBitSize;

      final BigInteger allocatedAddress = allocate(unitData);
      if (index == 0) {
        address = allocatedAddress;
      }
    }

    return address;
  }

  /**
   * Returns the minimal number of addressable units required to store data of the specified size
   * (in bits).
   * 
   * @param bitSize Size in bits.
   * @return Size in addressable units.
   * 
   * @throws IllegalArgumentException if the {@code bitSize} argument is 0 or negative.
   */

  public int bitsToAddressableUnits(final int bitSize) {
    checkGreaterThanZero(bitSize);
    return bitSize / addressableUnitBitSize + (bitSize % addressableUnitBitSize == 0 ? 0 : 1);
  }

  /**
   * Aligns the specified address by the specified length and returns the resulting aligned address.
   * 
   * @param address Address to be aligned.
   * @param alignment Alignment length.
   * @return Aligned address.
   * 
   * @throws IllegalArgumentException if any of the parameters is negative.
   */

  static BigInteger alignAddress(final BigInteger address, final int alignment) {
    checkGreaterOrEq(address, BigInteger.ZERO);
    checkGreaterOrEqZero(alignment);

    final BigInteger alignmentLength = BigInteger.valueOf(alignment);
    final BigInteger unaligned = address.mod(alignmentLength);
    return unaligned.equals(BigInteger.ZERO) ? 
        address :
        address.add(alignmentLength.subtract(unaligned));
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryAllocator [memory=%s, addressableUnitBitSize=%d, addressableUnitsInRegion=%d]",
        memory, addressableUnitBitSize, addressableUnitsInRegion);
  }

  /**
   * Converts the specified Java string to a bit vector that holds its ASCII representation.
   * 
   * @param string String to be converted.
   * @param zeroTerm Specifies whether the string must be terminated with zero.
   * @return ASCII representation of the string stored in a bit vector.
   * 
   * @throws IllegalArgumentException if the {@code string} argument is {@code null}.
   * @throws IllegalStateException if failed to convert the string to the "US-ASCII" encoding.
   */

  private BitVector toAsciiBinary(final String string, final boolean zeroTerm) {
    checkNotNull(string);

    final byte[] stringBytes;
    try {
      stringBytes = string.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }

    final int charCount = stringBytes.length + (zeroTerm ? 1 : 0);
    final int bitSize = charCount * BitVector.BITS_IN_BYTE;

    return BitVector.valueOf(stringBytes, bitSize);  }
}
