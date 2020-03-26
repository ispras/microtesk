/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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
 * {@link SnoopController} is an interface of a snoopy cache unit.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface SnoopController<E, A>  {

  /**
   * Snoops a read transaction from another cache unit.
   *
   * @param address the address used in the transaction.
   * @param oldEntry the entry in the cache or {@code null}.
   * @return the local entry if it is valid.
   */
  E snoopRead(A address, BitVector oldEntry);

  /**
   * Snoops a write transaction from another cache unit.
   *
   * @param address the address used in the transaction.
   * @param newEntry the updated entry or {@code null}.
   * @return the local entry if it is valid.
   */
  E snoopWrite(A address, BitVector newEntry);

  /**
   * Snoops an evict transaction from another cache unit.
   *
   * @param address the address used in the transaction.
   * @param oldEntry the entry being evicted.
   * @return the local entry if it is valid.
   */
  E snoopEvict(A address, BitVector oldEntry);
}
