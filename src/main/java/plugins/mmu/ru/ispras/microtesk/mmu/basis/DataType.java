/*
 * Copyright 2006-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.basis;

import java.math.BigInteger;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This enumeration contains basic data types.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum DataType {
  /** The byte data type. */
  BYTE(1),
  /** The half-word data type. */
  HWORD(2),
  /** The word data type. */
  WORD(4),
  /** The double-word data type. */
  DWORD(8),
  /** The quad-word data type. */
  QWORD(16),
  /** The octo-word data type. */
  OWORD(32);

  public static DataType type(final int sizeInBytes) {
    switch(sizeInBytes) {
      case 1:
        return BYTE;
      case 2:
        return HWORD;
      case 4:
        return WORD;
      case 8:
        return DWORD;
      case 16:
        return QWORD;
      case 32:
        return OWORD;
      default:
        InvariantChecks.checkTrue(false);
        return null;
    }
  }
  

  /** The size in bytes. */
  private final int sizeInBytes;
  /** The lower significant bit of the address. */
  private final int lowerAddressBit;

  /**
   * Constructs a data type for the given size.
   * 
   * @param size the size in bytes.
   */
  private DataType(final int size) {
    this.sizeInBytes = size;

    int i = 0;

    while ((sizeInBytes & (1 << i)) == 0) {
      i++;
    }

    this.lowerAddressBit = i;
  }

  /**
   * Returns the size in bytes.
   * 
   * @return the size.
   */
  public int getSizeInBytes() {
    return sizeInBytes;
  }

  /**
   * Returns the lower significant bit of the address.
   * 
   * @return the lower significant bit.
   */
  public int getLowerAddressBit() {
    return lowerAddressBit;
  }

  /**
   * Checks whether the address is aligned (contains a sufficient number of zero bits at the end).
   * 
   * @param address the address to be checked.
   * @return {@code true} if the address is aligned; {@code false} otherwise.
   */
  public boolean isAligned(final BigInteger address) {
    return address.and(BigInteger.valueOf(sizeInBytes - 1)).equals(BigInteger.ZERO);
  }

  /**
   * Returns the aligned address (zero a required number of bits at the end).
   * 
   * @param address the address to be aligned.
   * @return the aligned address.
   */
  public BigInteger align(final BigInteger address) {
    return address.andNot(BigInteger.valueOf(sizeInBytes - 1));
  }
}
