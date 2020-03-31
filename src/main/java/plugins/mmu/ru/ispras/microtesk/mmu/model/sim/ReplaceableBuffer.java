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

import ru.ispras.fortress.util.Pair;

/**
 * {@link ReplaceableBuffer} represents a replaceable buffer.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface ReplaceableBuffer<E extends Struct<?>, A extends Address<?>> extends Buffer<E, A> {

  /**
   * Allocates an invalid entry in the buffer and associates it with the given address.
   *
   * @param address the address.
   */
  void allocEntry(A address);

  /**
   * Evicts the entry associated with the given address from the buffer.
   *
   * @param initiator the buffer that initiates the operation.
   * @param address the address.
   * @return {@code true} iff the entry is not dirty or it has been synchronized with the storage.
   */
  boolean evictEntry(ReplaceableBuffer<?, A> initiator, A address);

  /**
   * Reads the entry associated with the given address and, if required, invalidates it.
   *
   * @param address the address.
   * @param invalidate the invalidation flag.
   * @return the entry associated with the address with the dirty bit or {@code null}.
   */
  Pair<E, Boolean> readEntry(A address, boolean invalidate);

  /**
   * Returns the next-level buffer.
   *
   * @return the next-level buffer.
   */
  Buffer<?, A> getNext();
}
