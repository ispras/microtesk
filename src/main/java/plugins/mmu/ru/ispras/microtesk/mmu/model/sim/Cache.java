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
import ru.ispras.microtesk.utils.SparseArray;

import java.math.BigInteger;

/**
 * {@link Cache} represents an abstract partially associative cache memory.
 *
 * A cache unit is characterized by the following parameters (except the entry and address types):
 * <ol>
 * <li>{@code length} - the number of sets in the cache,
 * <li>{@code associativity} - the number of lines in each set,
 * <li>{@code policyId} - the entry replacement policy,
 * <li>{@code indexer} - the set indexer, and
 * <li>{@code matcher} - the line matcher.
 * </ol>
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class Cache<E extends Struct<?>, A extends Address<?>> extends Buffer<E, A> {

  /** Table of associative sets. */
  private SparseArray<Set<E, A>> sets;
  private SparseArray<Set<E, A>> savedSets;

  private final int associativity;
  private final EvictPolicyId evictPolicyId;
  private final WritePolicyId writePolicyId;
  private final Indexer<A> indexer;
  private final Matcher<E, A> matcher;
  private final Buffer<? extends Struct<?>, A> next;

  /**
   * Proxy class is used to simplify code of assignment expressions.
   */
  public final class Proxy {
    private final A address;

    private Proxy(final A address) {
      this.address = address;
    }

    public void assign(final E entry) {
      storeEntry(address, entry);
    }

    public void assign(final BitVector value) {
      storeEntry(address, value);
    }
  }

  /**
   * Constructs a buffer of the given length and associativity.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param evictPolicyId the entry replacement policy.
   * @param writePolicyId the entry write policy.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   * @param next the next-level cache.
   */
  public Cache(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final BigInteger length,
      final int associativity,
      final EvictPolicyId evictPolicyId,
      final WritePolicyId writePolicyId,
      final Indexer<A> indexer,
      final Matcher<E, A> matcher,
      final Buffer<? extends Struct<?>, A> next) {
    super(entryCreator, addressCreator);

    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(evictPolicyId);
    InvariantChecks.checkNotNull(writePolicyId);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.sets = new SparseArray<>(length);
    this.savedSets = null;
    this.associativity = associativity;
    this.evictPolicyId = evictPolicyId;
    this.writePolicyId = writePolicyId;
    this.indexer = indexer;
    this.matcher = matcher;
    this.next = next;
  }

  private Set<E, A> getSet(final BitVector index) {
    Set<E, A> result = sets.get(index);

    if (null == result) {
      result = new Set<>(
          entryCreator,
          addressCreator,
          associativity,
          evictPolicyId,
          writePolicyId,
          matcher,
          next
      );
      sets.set(index, result);
    }

    return result;
  }

  @Override
  public final boolean isHit(final A address) {
    final BitVector index = indexer.getIndex(address);
    final Set<E, A> set = sets.get(index);
    return null != set && set.isHit(address);
  }

  @Override
  public final E loadEntry(final A address) {
    final BitVector index = indexer.getIndex(address);
    final Set<E, A> set = getSet(index);
    return set.loadEntry(address);
  }

  @Override
  public final void storeEntry(final A address, final BitVector entry) {
    final BitVector index = indexer.getIndex(address);
    final Set<E, A> set = getSet(index);
    set.storeEntry(address, entry);
  }

  public final Proxy storeEntry(final A address) {
    return new Proxy(address);
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    final Set<E, A> set = sets.get(index);
    return null != set ? set.seeData(index, way) : null;
  }

  @Override
  public String toString() {
    return String.format("%s %s", getClass().getSimpleName(), sets);
  }

  @Override
  public void setUseTempState(final boolean value) {
    final boolean isTempStateUsed = savedSets != null;
    if (value == isTempStateUsed) {
      return;
    }

    if (value) {
      savedSets = sets;
      sets = new SparseArray<>(sets.length()); // TODO: NEED A FULL COPY HERE
    } else {
      sets = savedSets;
      savedSets = null;
    }
  }

  @Override
  public void resetState() {
    sets = new SparseArray<>(sets.length());
    savedSets = null;
  }
}
