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
import java.util.ArrayList;
import java.util.Collection;

/**
 * {@link CacheUnit} represents an abstract way-associative cache memory.
 *
 * A cache unit is characterized by the following parameters (except the entry and address types):
 * <ul>
 * <li>{@code length} - the number of sets in the cache,
 * <li>{@code associativity} - the number of lines in each set,
 * <li>{@code policy} - the cache policy,
 * <li>{@code indexer} - the set indexer, and
 * <li>{@code matcher} - the line matcher.
 * </ul>
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class CacheUnit<E extends Struct<?>, A extends Address<?>>
    implements Buffer<E, A>, Snoopable<E, A>, BufferObserver, ModelStateManager {

  private final Struct<E> entryCreator;
  private final Address<A> addressCreator;

  final int associativity;
  final CachePolicy policy;
  final Indexer<A> indexer;
  final Matcher<E, A> matcher;
  final Buffer<? extends Struct<?>, A> next;
  final Collection<CacheUnit<? extends Struct<?>, A>> previous = new ArrayList<>();

  private SparseArray<CacheSet<E, A>> sets;
  private SparseArray<CacheSet<E, A>> savedSets;

  /**
   * Proxy class is used to ease code generation for assignments.
   */
  public final class Proxy {
    private final A address;

    private Proxy(final A address) {
      this.address = address;
    }

    public void assign(final E entry) {
      writeEntry(address, entry.asBitVector());
    }

    public void assign(final BitVector value) {
      writeEntry(address, value);
    }
  }

  /**
   * Constructs a buffer of the given length and associativity.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param policy the cache policy.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   * @param next the next-level cache.
   */
  public CacheUnit(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final BigInteger length,
      final int associativity,
      final CachePolicy policy,
      final Indexer<A> indexer,
      final Matcher<E, A> matcher,
      final Buffer<? extends Struct<?>, A> next) {
    InvariantChecks.checkNotNull(entryCreator);
    InvariantChecks.checkNotNull(addressCreator);
    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policy);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.entryCreator = entryCreator;
    this.addressCreator = addressCreator;
    this.sets = new SparseArray<>(length);
    this.savedSets = null;
    this.associativity = associativity;
    this.policy = policy;
    this.indexer = indexer;
    this.matcher = matcher;
    this.next = next;

    // The next buffer is allowed to be the main memory.
    if (next != null && next instanceof CacheUnit) {
      final CacheUnit<? extends Struct<?>, A> nextCache = (CacheUnit<? extends Struct<?>, A>) next;
      nextCache.previous.add(this);
    }
  }

  /**
   * Constructs an entry.
   *
   * @param entry the entry data.
   * @return the constructed entry.
   */
  final E newEntry(final BitVector entry) {
    return entryCreator.newStruct(entry);
  }

  /**
   * Constructs an entry.
   *
   * @param address the address (used to assign the tag).
   * @param entry the entry data w/o tag.
   * @return the constructed entry.
   */
  final E newEntry(final A address, final BitVector entry) {
    final E result = entryCreator.newStruct(entry);

    matcher.assignTag(result, address);
    InvariantChecks.checkTrue(matcher.areMatching(result, address));

    return result;
  }

  /**
   * Constructs an address.
   *
   * @param value the address value (other fields are ignored).
   * @return the constructed address.
   */
  final A newAddress(final BitVector value) {
    return addressCreator.setValue(value);
  }

  @Override
  public final boolean isHit(final A address) {
    final CacheSet<E, A> set = getSet(address);
    return set.isHit(address);
  }

  @Override
  public final E readEntry(final A address) {
    final CacheSet<E, A> set = getSet(address);
    return set.readEntry(address);
  }

  @Override
  public final void writeEntry(final A address, final BitVector entry) {
    final CacheSet<E, A> set = getSet(address);
    set.writeEntry(address, entry);
  }

  public final Proxy writeEntry(final A address) {
    return new Proxy(address);
  }

  @Override
  public final void evictEntry(final A address) {
    final CacheSet<E, A> set = getSet(address);
    set.evictEntry(address);
  }

  @Override
  public final E allocEntry(final A address, final BitVector entry) {
    final CacheSet<E, A> set = getSet(address);
    return set.allocEntry(address, entry);
  }

  @Override
  public final E snoopRead(final A address) {
    final CacheSet<E, A> set = getSet(address);
    return set.snoopRead(address);
  }

  @Override
  public final void snoopWrite(final A address, final BitVector entry) {
    final CacheSet<E, A> set = getSet(address);
    set.snoopWrite(address, entry);
  }

  @Override
  public final void snoopEvict(final A address) {
    final CacheSet<E, A> set = getSet(address);
    set.snoopEvict(address);
  }

  public final CacheLine<E, A> getLine(final A address) {
    final CacheSet<E, A> set = getSet(address);
    return set.getLine(address);
  }

  protected final CacheSet<E, A> getSet(final BitVector index) {
    CacheSet<E, A> set = sets.get(index);

    if (set == null) {
      set = new CacheSet<>(associativity, policy, matcher,this, next);
      sets.set(index, set);
    }

    return set;
  }

  protected final void setSet(final BitVector index, final CacheSet<E, A> set) {
    sets.set(index, set);
  }

  protected final CacheSet<E, A> getSet(final A address) {
    final BitVector index = indexer.getIndex(address);
    return getSet(index);
  }

  @Override
  public final boolean isHit(final BitVector value) {
    final A address = newAddress(value);
    return isHit(address);
  }

  @Override
  public final Pair<BitVector, BitVector> seeEntry(final BitVector index, final BitVector way) {
    final CacheSet<E, A> set = sets.get(index);

    if (set != null) {
      final CacheLine<E, A> line = set.getLine(way.intValue());

      if (line != null && line.isValid()) {
        return new Pair<>(line.getAddress().asBitVector(), line.getEntry().asBitVector());
      }
    }

    return null;
  }

  @Override
  public final String toString() {
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
