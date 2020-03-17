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
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.ModelStateManager;

/**
 * {@link Buffer} represents a buffer (i.e., a component that stores addressable entries).
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Buffer<E, A> implements BufferObserver, ModelStateManager {
  protected final Struct<E> entryCreator;
  protected final Address<A> addressCreator;

  /**
   * Creates a buffer.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   */
  public Buffer(final Struct<E> entryCreator, final Address<A> addressCreator) {
    InvariantChecks.checkNotNull(entryCreator);
    InvariantChecks.checkNotNull(addressCreator);

    this.entryCreator = entryCreator;
    this.addressCreator = addressCreator;
  }

  /**
   * Checks whether the given address causes a hit.
   *
   * @param address the address.
   * @return {@code true} iff the address causes a hit.
   */
  public abstract boolean isHit(A address);

  /**
   * Loads the entry associated with the given address.
   *
   * @param address the address.
   * @return the entry associated with the address or {@code null}.
   */
  public abstract E readEntry(A address);

  /**
   * Stores the entry associated with the given address.
   *
   * <p>
   * An incoming entry is not necessarily of the {@code E} type.
   * It may be returned from the previous- or next-level cache unit.
   * It is the method's responsibility to convert the entry to the relevant type.
   * </p>
   *
   * @param address the address.
   * @param entry the new entry.
   */
  public abstract void writeEntry(A address, BitVector entry);

  /**
   * Stores the entry associated with the given address.
   *
   * @param address the address.
   * @param entry the new entry.
   */
  public final void writeEntry(final A address, final Struct<?> entry) {
    writeEntry(address, entry.asBitVector());
  }

  /**
   * Invalidates the entry associated with the given address.
   *
   * @param address the address.
   */
  public abstract void evictEntry(A address);

  /**
   * Allocates the entry associated with the given address.
   *
   * @param address the address.
   * @param entry the allocated entry.
   */
  public abstract E allocEntry(A address, final BitVector entry);

  @Override
  public final boolean isHit(final BitVector value) {
    final A address = addressCreator.setValue(value);
    return isHit(address);
  }

  @Override
  public abstract Pair<BitVector, BitVector> seeEntry(BitVector index, BitVector way);

  @Override
  public abstract void setUseTempState(boolean value);

  @Override
  public abstract void resetState();
}
