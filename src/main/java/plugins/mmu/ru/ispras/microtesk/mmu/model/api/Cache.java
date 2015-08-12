/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.api;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.SparseArray;

/**
 * This is an abstract representation of a partially associative cache memory. A cache unit is
 * characterized by the following parameters (except the data and address types):
 * <ol><li><code>length</code> - the number of sets in the cache,
 * <li><code>associativity</code> - the number of lines in each set,
 * <li><code>policyId</code> - the data replacement policy,
 * <li><code>indexer</code> - the set indexer, and
 * <li><code>matcher</code> - the line matcher.</ol>
 * 
 * @param <D> the data type.
 * @param <A> the address type.
 * 
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public abstract class Cache<D, A extends Address> implements Buffer<D, A> {
  /** The table of associative sets. */
  private final SparseArray<Set<D, A>> sets;

  /** The set indexer. */
  private final Indexer<A> indexer;

  private final int associativity;
  private final PolicyId policyId;
  private final Matcher<D, A> matcher;

  /**
   * Constructs a buffer of the given length and associativity.
   * 
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */

  public Cache(
      final BigInteger length,
      final int associativity,
      final PolicyId policyId,
      final Indexer<A> indexer,
      final Matcher<D, A> matcher) {
    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policyId);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.sets = new SparseArray<>(length);
    this.indexer = indexer;

    this.associativity = associativity;
    this.policyId = policyId;
    this.matcher = matcher;
  }

  private final Set<D, A> getSet(final BitVector index) {
    Set<D, A> result = sets.get(index);

    if (null == result) {
      result = new Set<D, A>(associativity, policyId, matcher);
      sets.set(index, result);
    }

    return result;
  }

  @Override
  public boolean isHit(final A address) {
    final BitVector index = indexer.getIndex(address);
    final Set<D, A> set = sets.get(index);
    return null != set && set.isHit(address);
  }

  @Override
  public D getData(final A address) {
    final BitVector index = indexer.getIndex(address);
    final Set<D, A> set = getSet(index);
    return set.getData(address);
  }

  @Override
  public D setData(final A address, final D data) {
    final BitVector index = indexer.getIndex(address);
    final Set<D, A> set = getSet(index);
    return set.setData(address, data);
  }
}
