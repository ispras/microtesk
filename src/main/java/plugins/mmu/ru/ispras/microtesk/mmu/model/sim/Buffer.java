/*
 * Copyright 2012-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * {@link Buffer} represents a buffer that stores addressable entries.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Buffer<E, A> {

  /**
   * Checks whether the given address hits the buffer.
   *
   * @param address the address.
   * @return {@code true} iff the address hits the buffer.
   */
  boolean isHit(A address);

  /**
   * Reads the entry associated with the given address from the buffer.
   *
   * @param address the address.
   * @return the entry associated with the address or {@code null}.
   */
  E readEntry(A address);

  /**
   * Writes the entry associated with the given address into the buffer.
   *
   * <p>
   * Note that an incoming entry is not necessarily of the {@code E} type. It may be returned from
   * the previous- or next-level cache unit (thus, be different). It is the method's responsibility
   * to convert the entry to the specified type.
   * </p>
   *
   * @param address the address.
   * @param newEntry the new entry.
   */
  void writeEntry(A address, BitVector newEntry);

  /**
   * Writes the data associated with the given address into the given field of the entry.
   *
   * @param address the address.
   * @param lower the lower bit.
   * @param upper the upper bit.
   * @param newData the new data.
   */
  void writeEntry(A address, int lower, int upper, BitVector newData);

  /**
   * Resets the state of the buffer.
   */
  void resetState();
}
