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
import ru.ispras.microtesk.utils.SparseArray;

import java.math.BigInteger;

/**
 * {@link Cache} represents an abstract partially associative cache memory.
 *
 * A cache unit is characterized by the following parameters (except the data and address types):
 * <ol>
 * <li>{@code length} - the number of sets in the cache,
 * <li>{@code associativity} - the number of lines in each set,
 * <li>{@code policyId} - the data replacement policy,
 * <li>{@code indexer} - the set indexer, and
 * <li>{@code matcher} - the line matcher.
 * </ol>
 *
 * @param <D> the data type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class Cache<D extends Struct<?>, A extends Address<?>>
    extends Buffer<D, A> implements ModelStateManager {

  /** The table of associative sets. */
  private SparseArray<Set<D, A>> sets;
  private SparseArray<Set<D, A>> savedSets;

  private final int associativity;
  private final PolicyId policyId;
  private final Indexer<A> indexer;
  private final Matcher<D, A> matcher;
  private final Cache<? extends Struct<?>, A> next = null; // FIXME:

  /**
   * Proxy class is used to simplify code of assignment expressions.
   */
  public final class Proxy {
    private final A address;

    private Proxy(final A address) {
      this.address = address;
    }

    public D assign(final D data) {
      return setData(address, data);
    }

    public D assign(final BitVector value) {
      final D data = dataCreator.newStruct(value);
      return setData(address, data);
    }
  }

  /**
   * Constructs a buffer of the given length and associativity.
   *
   * @param dataCreator the data creator.
   * @param addressCreator the address creator.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param policyId the data replacement policy.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */
  public Cache(
      final Struct<D> dataCreator,
      final Address<A> addressCreator,
      final BigInteger length,
      final int associativity,
      final PolicyId policyId,
      final Indexer<A> indexer,
      final Matcher<D, A> matcher) {
    super(dataCreator, addressCreator);

    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policyId);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.sets = new SparseArray<>(length);
    this.savedSets = null;
    this.associativity = associativity;
    this.policyId = policyId;
    this.indexer = indexer;
    this.matcher = matcher;
  }

  private Set<D, A> getSet(final BitVector index) {
    Set<D, A> result = sets.get(index);

    if (null == result) {
      result = new Set<>(
          Cache.this.dataCreator,
          Cache.this.addressCreator,
          associativity,
          policyId,
          matcher
      );
      sets.set(index, result);
    }

    return result;
  }

  @Override
  public final boolean isHit(final A address) {
    final BitVector index = indexer.getIndex(address);
    final Set<D, A> set = sets.get(index);
    return null != set && set.isHit(address);
  }

  @Override
  public final D getData(final A address) {
    final BitVector index = indexer.getIndex(address);
    final Set<D, A> set = getSet(index);
    return set.getData(address);
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    final Set<D, A> set = sets.get(index);
    return null != set ? set.seeData(index, way) : null;
  }

  @Override
  public final D setData(final A address, final D data) {
    final BitVector index = indexer.getIndex(address);
    final Set<D, A> set = getSet(index);
    return set.setData(address, data);
  }

  public final Proxy setData(final A address) {
    return new Proxy(address);
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
