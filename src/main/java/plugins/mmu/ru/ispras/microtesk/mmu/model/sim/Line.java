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
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class Line<E extends Struct<?>, A extends Address<?>> extends Buffer<E, A> {
  /** Stored entry. */
  private E entry;
  /** Entry address. */
  private A address;

  /** Dirty bit used to implement the write-back policy. */
  private boolean dirty;

  /** Line matcher. */
  private final Matcher<E, A> matcher;
  /** Cache that contains this line. */
  private final Cache<E, A> cache;

  /**
   * Constructs a default (invalid) line.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   * @param matcher the entry-address matcher.
   */
  public Line(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final Matcher<E, A> matcher,
      final Cache<E, A> cache) {
    super(entryCreator, addressCreator);

    this.entry = null;
    this.address = null;
    this.dirty = false;

    this.matcher = matcher;
    this.cache = cache;
  }

  @Override
  public boolean isHit(final A address) {
    if (entry == null) {
      return false;
    }

    return matcher.areMatching(entry, address);
  }

  @Override
  public E loadEntry(final A address) {
    return isHit(address) ? entry : null;
  }

  @Override
  public void storeEntry(final A address, final BitVector entry) {
    this.entry = entryCreator.newStruct(entry);
    this.address = address;

    matcher.assignTag(this.entry, address);
    InvariantChecks.checkTrue(matcher.areMatching(this.entry, this.address));
  }

  public E getEntry() {
    return entry;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(final boolean dirty) {
    this.dirty = dirty;
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    return address != null && entry != null
        ? new Pair<>(address.getValue(), entry.asBitVector())
        : null;
  }

  @Override
  public void setUseTempState(final boolean value) {
    // Do nothing.
  }

  @Override
  public void resetState() {
    entry = null;
    address = null;
    dirty = false;
  }

  @Override
  public String toString() {
    return String.format("Line [entry=%s]", entry);
  }
}
