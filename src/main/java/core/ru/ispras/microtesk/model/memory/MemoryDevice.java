/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.memory;

import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * The {@code MemoryDevice} interface provides a unified access
 * to an abstract memory device, which can be implemented as a simple
 * data array or as a complex hierarchy of data buffers.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public interface MemoryDevice {
  /**
   * Returns the size of address used to perform an access to the memory device.
   *
   * @return Address size in bits.
   */
  int getAddressBitSize();

  /**
   * Returns the size of data unit that can be read or written from
   * the memory device at once.
   *
   * @return Data size in bits.
   */
  int getDataBitSize();

  /**
   * Loads data from the given address.
   *
   * @param address Load address.
   * @return Data of size equal to returned by {@link MemoryDevice#getDataBitSize()}.
   */
  BitVector load(BitVector address);

  /**
   * Stores the specified data at the given address.
   *
   * @param address Store address.
   * @param data Data of size equal to returned by {@link MemoryDevice#getDataBitSize()}.
   */
  void store(BitVector address, BitVector data);

  /**
   * Stores the specified data at the given address.
   *
   * @param address Store address.
   * @param offset Data offset in bits.
   * @param data Data of size equal to {@link MemoryDevice#getDataBitSize()} - {@code offset}.
   */
  void store(BitVector address, int offset, BitVector data);

  /**
   * Checks whether the specified address location has been initialized.
   *
   * @param address Address to be checked.
   * @return {@code true} is the address location is initialized or {@code false} otherwise.
   */
  boolean isInitialized(BitVector address);
}
