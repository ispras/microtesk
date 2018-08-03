/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.util.Pair;

/**
 * This is an abstract representation of a cache line.
 *
 * @param <D> the data type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Line<D extends Data, A extends Address> implements Buffer<D, A> {
  /** The stored data. */
  private D data;

  /** Address of the data */
  private A address;

  /** The data-address matcher. */
  private final Matcher<D, A> matcher;

  /**
   * Constructs a default (invalid) line.
   *
   * @param matcher the data-address matcher.
   */
  public Line(final Matcher<D, A> matcher) {
    this.data = null;
    this.address = null;
    this.matcher = matcher;
  }

  @Override
  public boolean isHit(final A address) {
    if (null == data) {
      return false;
    }

    return matcher.areMatching(data, address);
  }

  @Override
  public D getData(final A address) {
    return isHit(address) ? data : null;
  }

  @Override
  public D setData(final A address, final D newData) {
    final D oldData = data;

    this.data = newData;
    this.address = address;

    return oldData;
  }

  @Override
  public String toString() {
    return String.format("Line [data=%s]", data);
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    return null != address && null != data
        ? new Pair<>(address.getValue(), data.asBitVector())
        : null
        ;
  }
}
