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
 * {@link Buffer} represents a buffer (i.e., a component that stores addressable data).
 *
 * @param <D> the data type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Buffer<D, A> implements BufferObserver, ModelStateManager {
  protected final Struct<D> dataCreator;
  protected final Address<A> addressCreator;

  /**
   * Creates a buffer.
   *
   * @param dataCreator the data creator.
   * @param addressCreator the address creator.
   */
  public Buffer(final Struct<D> dataCreator, final Address<A> addressCreator) {
    InvariantChecks.checkNotNull(dataCreator);
    InvariantChecks.checkNotNull(addressCreator);

    this.dataCreator = dataCreator;
    this.addressCreator = addressCreator;
  }

  /**
   * Checks whether the given address causes a hit.
   *
   * @param address the data address.
   * @return {@code true} iff the address causes a hit.
   */
  public abstract boolean isHit(final A address);

  /**
   * Returns the data associated with the given address.
   *
   * @param address the data address.
   * @return the data object iff the address causes a hit.
   */
  public abstract D getData(final A address);

  /**
   * Updates the data associated with the given address.
   *
   * @param address the data address.
   * @param data the new data.
   *
   * @return the old data if they exist; {@code null} otherwise.
   */
  public abstract D setData(final A address, final D data);

  @Override
  public final boolean isHit(final BitVector value) {
    final A address = addressCreator.setValue(value);
    return isHit(address);
  }

  @Override
  public abstract Pair<BitVector, BitVector> seeData(BitVector index, BitVector way);

  @Override
  public abstract void setUseTempState(final boolean value);

  @Override
  public abstract void resetState();
}
