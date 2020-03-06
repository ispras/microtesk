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

/**
 * {@link Line} represents an abstract cache line.
 *
 * @param <D> the data type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class Line<D extends Struct<?>, A extends Address<?>> extends Buffer<D, A> {
  /** Stored data. */
  private D data;
  /** Address of the data */
  private A address;
  /** Dirty bit used to implement the write-back policy. */
  private boolean dirty;

  /** Line matcher. */
  private final Matcher<D, A> matcher;
  /** Line coercer. */
  private final Coercer<D> coercer;

  /**
   * Constructs a default (invalid) line.
   *
   * @param dataCreator the data creator.
   * @param addressCreator the address creator.
   * @param matcher the data-address matcher.
   */
  public Line(
      final Struct<D> dataCreator,
      final Address<A> addressCreator,
      final Matcher<D, A> matcher,
      final Coercer<D> coercer) {
    super(dataCreator, addressCreator);

    this.data = null;
    this.address = null;
    this.dirty = false;

    this.matcher = matcher;
    this.coercer = coercer;
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
  public D setData(final A address, final BitVector newData) {
    final D oldData = data;

    this.data = coerce(address, newData);
    this.address = address;
    InvariantChecks.checkTrue(matcher.areMatching(this.data, this.address));

    return oldData;
  }

  private D coerce(final A address, final BitVector data) {
    if (coercer == null) {
      return dataCreator.newStruct(data);
    }

    final D newData = coercer.coerce(data);
    matcher.assignTag(newData, address);

    return newData;
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    return address != null && data != null
        ? new Pair<>(address.getValue(), data.asBitVector())
        : null;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(final boolean dirty) {
    this.dirty = dirty;
  }

  @Override
  public void setUseTempState(final boolean value) {
    // Do nothing.
  }

  @Override
  public void resetState() {
    data = null;
    address = null;
    dirty = false;
  }

  @Override
  public String toString() {
    return String.format("Line [data=%s]", data);
  }
}
