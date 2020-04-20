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
 * {@link Struct} must be supported by all structures (including data and addresses).
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public interface Struct<T> {

  /**
   * Returns the bit size of the struct.
   * @return the bit size as an integer value
   */
  int getBitSize();

  /**
   * Returns the struct initialized by the given bit vector.
   *
   * @param value the bit vector representing all fields of the struct.
   * @return the data struct.
   */
  T newStruct(BitVector value);

  /**
   * Converts the struct to the bit vector.
   *
   * @return the bit vector representing all fields of the struct.
   */
  BitVector asBitVector();
}
