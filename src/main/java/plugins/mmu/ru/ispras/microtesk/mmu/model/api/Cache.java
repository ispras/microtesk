/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;

/**
 * This is an abstract representation of a partially associative cache memory. A cache unit is
 * characterized by the following parameters (except the data and address types):
 * (1) <code>length</code> - the number of sets in the cache,
 * (2) <code>associativity</code> - the number of lines in each set,
 * (3) <code>policyId</code> - the data replacement policy,
 * (4) <code>indexer</code> - the set indexer, and
 * (5) <code>matcher</code> - the line matcher.
 * 
 * @param <D> the data type.
 * @param <A> the address type.
 * 
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */

public final class Cache<D extends Data, A extends Address> implements Buffer<D, A> {
  /** The table of associative sets. */
  private ArrayList<Set<D, A>> sets = new ArrayList<>();

  /** The set indexer. */
  private Indexer<A> indexer;

  /**
   * Constructs a buffer of the given length and associativity.
   * 
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */

  public Cache(int length, int associativity, final PolicyId policyId,
      final Indexer<A> indexer, final Matcher<D, A> matcher) {

    // Fill the cache with the default (invalid) lines.
    for (int i = 0; i < length; i++) {
      sets.add(new Set<D, A>(associativity, policyId, matcher));
    }
  }

  @Override
  public boolean isHit(final A address) {
    final int index = indexer.getIndex(address);
    final Set<D, A> set = sets.get(index);

    return set.isHit(address);
  }

  @Override
  public D getData(final A address) {
    final int index = indexer.getIndex(address);
    final Set<D, A> set = sets.get(index);

    return set.getData(address);
  }

  @Override
  public D setData(final A address, final D data) {
    final int index = indexer.getIndex(address);
    final Set<D, A> set = sets.get(index);

    return set.setData(address, data);
  }
}
