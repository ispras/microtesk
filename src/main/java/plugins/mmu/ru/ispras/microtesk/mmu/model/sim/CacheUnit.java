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
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class CacheUnit<E extends Struct<?>, A extends Address<?>>
    implements Buffer<E, A>, SnoopController<E, A>, BufferObserver, ModelStateManager {

  private final Struct<E> entryCreator;
  private final Address<A> addressCreator;

  final int associativity;
  final CachePolicy policy;
  final Indexer<A> indexer;
  final Matcher<E, A> matcher;
  final Buffer<? extends Struct<?>, A> next;
  final Collection<CacheUnit<?, A>> previous = new ArrayList<>();
  final Collection<CacheUnit<?, A>> neighbor = new ArrayList<>();

  private SparseArray<CacheSet<E, A>> sets;
  private SparseArray<CacheSet<E, A>> savedSets;

  /**
   * If not {@code null}, holds an entry to be returned by a send-snoop operation, namely
   * {@link #sendSnoopRead} or {@link #sendSnoopWrite}.
   */
  private Struct<?> snoopedEntry = null;

  /**
   * {@link Proxy} eases code generation for assignment statements.
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
   * Constructs a cache unit of the given length and associativity.
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
    // If it is a cache unit, add a backward link to this one.
    if (next != null && next instanceof CacheUnit) {
      final CacheUnit<? extends Struct<?>, A> nextCache = (CacheUnit<?, A>) next;
      nextCache.previous.add(this);
    }
  }

  /**
   * Returns the entry bit size.
   *
   * @return the entry bit size.
   */
  final int getEntryBitSize() {
    return entryCreator.getBitSize();
  }

  /**
   * Constructs a filled entry w/o tag.
   *
   * @param entry the entry data.
   * @return the constructed entry.
   */
  final E newEntry(final BitVector entry) {
    return entryCreator.newStruct(entry);
  }

  /**
   * Constructs an empty entry w/ tag.
   *
   * @param address the address (used to assign the tag).
   * @return the constructed entry w/o data.
   */
  final E newEntry(final A address) {
    final int bitSize = getEntryBitSize();
    return newEntry(address, BitVector.newEmpty(bitSize));
  }

  /**
   * Constructs a filled entry w/ tag.
   *
   * @param address the address (used to assign the tag).
   * @param entry the entry data w/o tag.
   * @return the constructed entry w/ tag.
   */
  final E newEntry(final A address, final BitVector entry) {
    final E result = entryCreator.newStruct(entry);

    matcher.assignTag(result, address);
    InvariantChecks.checkTrue(matcher.areMatching(result, address));

    return result;
  }

  /**
   * Assigns the source entry to the target one and fills the tag.
   *
   * @param target the target entry.
   * @param address the address.
   * @param source the source entry.
   */
  final void assignEntry(final E target, final A address, final BitVector source) {
    target.asBitVector().assign(source);
    matcher.assignTag(target, address);
    InvariantChecks.checkTrue(matcher.areMatching(target, address));
  }

  /**
   * Assigns the source data into the given field of the target entry and fills the tag.
   *
   * @param target the target entry.
   * @param address the address.
   * @param lower the lower bit.
   * @param upper the upper bit.
   * @param source the source data.
   */
  final void assignEntry(
        final E target, final A address, final int lower, final int upper, final BitVector source) {
    target.asBitVector().field(lower, upper).assign(source);
    matcher.assignTag(target, address);
    InvariantChecks.checkTrue(matcher.areMatching(target, address));
  }

  /**
   * Constructs an address.
   *
   * @param value the address value (other fields of the address struct are ignored).
   * @return the constructed address.
   */
  final A newAddress(final BitVector value) {
    return addressCreator.setValue(value);
  }

  /**
   * Makes a link between this cache and a neighbor.
   *
   * @param other the neighbor to be linked.
   */
  public void addNeighbor(final CacheUnit<?, A> other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(other != this);

    neighbor.add(other);
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
  public final void writeEntry(final A address, final BitVector newEntry) {
    final CacheSet<E, A> set = getSet(address);
    set.writeEntry(address, newEntry);
  }

  @Override
  public final void writeEntry(
      final A address, final int lower, final int upper, final BitVector data) {
    final CacheSet<E, A> set = getSet(address);
    set.writeEntry(address, lower, upper, data);
  }

  @Override
  public final void allocEntry(final A address) {
    final CacheSet<E, A> set = getSet(address);
    set.allocEntry(address);
  }

  @Override
  public final void evictEntry(final A address) {
    final CacheSet<E, A> set = getSet(address);
    set.evictEntry(address);
  }

  @Override
  public final E snoopRead(final A address, final BitVector oldEntry) {
    final CacheSet<E, A> set = getSet(address);
    return set.snoopRead(address, oldEntry);
  }

  @Override
  public final E snoopWrite(final A address, final BitVector newEntry) {
    final CacheSet<E, A> set = getSet(address);
    return set.snoopWrite(address, newEntry);
  }

  @Override
  public final E snoopEvict(final A address, final BitVector oldEntry) {
    final CacheSet<E, A> set = getSet(address);
    return set.snoopEvict(address, oldEntry);
  }

  final CacheLine<E, A> getLine(final A address) {
    final CacheSet<E, A> set = getSet(address);
    return set.getLine(address);
  }

  final boolean isCoherent(final A address) {
    final ArrayList<Enum<?>> states = new ArrayList<>();

    final CacheLine<?, A> thisLine = getLine(address);
    if (thisLine != null) {
      states.add(thisLine.getState());
    }

    for (final CacheUnit<?, A> other : neighbor) {
      final CacheLine<?, A> otherLine = other.getLine(address);
      if (otherLine != null) {
        states.add(otherLine.getState());
      }
    }

    final CoherenceProtocol protocol = policy.coherence.newProtocol();
    return protocol.isCoherent(states.toArray(new Enum<?>[]{}));
  }

  final boolean isExclusive(final A address) {
    for (final CacheUnit<?, A> other : neighbor) {
      InvariantChecks.checkTrue(other != this);
      if (other.isHit(address)) {
        return false;
      }
    }
    return true;
  }

  final Struct<?> sendSnoopRead(final A address, final BitVector oldEntry) {
    Struct<?> result = snoopedEntry;

    // Broadcast snoop requests to the same-level caches.
    for (final CacheUnit<?, A> other : neighbor) {
      InvariantChecks.checkTrue(other != this);
      final Struct<?> snoop = other.snoopRead(address, oldEntry);

      if (snoop != null && result == null) {
        result = snoop;
      }
    }

    // If no data are available at this level, access the next one.
    return (result != null || oldEntry != null) ? result : readThrough(address);
  }

  final Struct<?> sendSnoopWrite(final A address, final BitVector newEntry) {
    Struct<?> result = snoopedEntry;

    // Broadcast snoop requests to the same-level caches.
    for (final CacheUnit<?, A> other : neighbor) {
      InvariantChecks.checkTrue(other != this);
      final Struct<?> snoop = other.snoopWrite(address, newEntry);

      if (snoop != null && result == null) {
        result = snoop;
      }
    }

    // Calling this method implies that write-allocate is enabled.
    // If no data are available at this level, access the next one.
    return result != null || newEntry != null ? result : readThrough(address);
  }

  final void sendSnoopEvict(final A address, final BitVector oldEntry, final boolean dirty) {
    // Broadcast snoop requests to the same-level caches.
    for (final CacheUnit<?, A> other : neighbor) {
      InvariantChecks.checkTrue(other != this);
      other.snoopEvict(address, oldEntry);
    }

    // Invalidate the previous caches (backward invalidation).
    for (final CacheUnit<?, A> other : previous) {
      if (other.isHit(address) && other.policy.inclusion.yes) {
        other.evictEntry(address);
      }
    }

    // Do write-back if required.
    if (policy.write.back && dirty) {
      InvariantChecks.checkNotNull(next);
      next.writeEntry(address, oldEntry);
      return;
    }

    // Reallocate the entry to the next cache.
    if (policy.inclusion.no) {
      if (next != null && next instanceof CacheUnit) {
        InvariantChecks.checkFalse(next.isHit(address));

        final CacheUnit<?, A> other = (CacheUnit<?, A>) next;
        other.allocEntry(address);

        // This makes a snoop operation returns the reallocated entry
        // while executing the read or write operation.
        other.snoopedEntry = other.newEntry(address, oldEntry);

        // Update the protocol state by executing the read or write operation.
        if (dirty) {
          other.writeEntry(address, oldEntry);
        } else {
          other.readEntry(address);
        }

        // Disable that snooping hack.
        other.snoopedEntry = null;
      }
    }
  }

  final Struct<?> readThrough(final A address) {
    // No forward link.
    if (next == null) {
      return null;
    }

    // Inclusive cache.
    if (policy.inclusion.yes || policy.inclusion.dontCare) {
      return next.readEntry(address);
    }

    // Exclusive cache.
    Buffer<? extends Struct<?>, A> other = next;
    while (other != null && !other.isHit(address)) {
      final CacheUnit<?, A> cache = other instanceof CacheUnit ? (CacheUnit<?, A>) other : null;
      other = cache != null ? cache.next : null;
    }

    InvariantChecks.checkNotNull(other);
    final Struct<?> result = other.readEntry(address);

    if (other instanceof CacheUnit) {
      other.evictEntry(address);
    }

    return result;
  }

  final void writeThrough(final A address, final int lower, final int upper, final BitVector data) {
    // No forward link.
    if (next == null) {
      return;
    }

    // Inclusive cache.
    if (policy.inclusion.yes || policy.inclusion.dontCare) {
      next.writeEntry(address, lower, upper, data);
      return;
    }

    // Exclusive cache.
    boolean allocated = policy.write.alloc;
    Buffer<? extends Struct<?>, A> other = next;

    while (other != null) {
      final CacheUnit<?, A> cache = other instanceof CacheUnit ? (CacheUnit<?, A>) other : null;

      if (other.isHit(address)) {
        if (cache != null) {
          if (allocated) {
            // Evict the entry to maintain exclusiveness.
            cache.evictEntry(address);
          } else if (cache.policy.write.alloc) {
            // Write the entry into the cache.
            cache.writeEntry(address, lower, upper, data);
            allocated = true;
          }
        } else {
          // Write the entry into the main memory.
          other.writeEntry(address, lower, upper, data);
        }
      }

      other = cache != null ? cache.next : null;
    }
  }

  public final Proxy writeEntry(final A address) {
    return new Proxy(address);
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