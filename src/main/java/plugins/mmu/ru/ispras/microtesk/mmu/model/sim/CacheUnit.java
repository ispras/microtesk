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
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class CacheUnit<E extends Struct<?>, A extends Address<?>>
    implements ReplaceableBuffer<E, A>, SnoopController<E, A>, BufferObserver, ModelStateManager {

  private final Struct<E> entryCreator;
  private final Address<A> addressCreator;

  final int associativity;
  final CachePolicy policy;
  final Indexer<A> indexer;
  final Matcher<E, A> matcher;
  final Buffer<? extends Struct<?>, A> next;
  final Collection<CacheUnit<? extends Struct<?>, A>> previous = new ArrayList<>();
  final Collection<CacheUnit<? extends Struct<?>, A>> neighbor = new ArrayList<>();

  private SparseArray<CacheSet<E, A>> sets;
  private SparseArray<CacheSet<E, A>> savedSets;

  /**
   * If not {@code null}, holds an entry to be returned by a send-snoop operation, namely
   * {@link #sendSnoopRead}, {@link #sendSnoopWrite}, or {@link #sendSnoopEvict}.
   */
  private Pair<? extends Struct<?>, Boolean> snooped = null;

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
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(source);

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
      final E target,
      final A address,
      final int lower,
      final int upper,
      final BitVector source) {
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(source);

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
   * Creates a new set (can be overridden in a subclass).
   *
   * @param index the index of the set.
   * @return the created set.
   */
  protected CacheSet<E, A> newSet(final BitVector index) {
    return new CacheSet<>(index, associativity, policy, matcher,this, next);
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
  public final Pair<E, Boolean> readEntry(final A address, final boolean invalidate) {
    final CacheSet<E, A> set = getSet(address);
    return set.readEntry(address, invalidate);
  }

  @Override
  public final void writeEntry(final A address, final BitVector newEntry) {
    final CacheSet<E, A> set = getSet(address);
    set.writeEntry(address, newEntry);
  }

  @Override
  public final void writeEntry(
      final A address,
      final int lower,
      final int upper,
      final BitVector newData) {
    final CacheSet<E, A> set = getSet(address);
    set.writeEntry(address, lower, upper, newData);
  }

  @Override
  public final void allocEntry(final A address) {
    final CacheSet<E, A> set = getSet(address);
    set.allocEntry(address);
  }

  @Override
  public final boolean evictEntry(final ReplaceableBuffer<?, A> initiator, final A address) {
    InvariantChecks.checkNotNull(initiator);

    final CacheSet<E, A> set = getSet(address);
    return set.evictEntry(initiator, address);
  }

  @Override
  public final Pair<E, Boolean> snoopRead(
      final A address, final BitVector oldEntry, final boolean invalidate) {
    final CacheSet<E, A> set = getSet(address);
    return set.snoopRead(address, oldEntry, invalidate);
  }

  @Override
  public final Pair<E, Boolean> snoopWrite(final A address, final BitVector newEntry) {
    final CacheSet<E, A> set = getSet(address);
    return set.snoopWrite(address, newEntry);
  }

  @Override
  public final Pair<E, Boolean> snoopEvict(final A address, final BitVector oldEntry) {
    final CacheSet<E, A> set = getSet(address);
    return set.snoopEvict(address, oldEntry);
  }

  @Override
  public final Buffer<?, A> getNext() {
    return next;
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

  final Pair<? extends Struct<?>, Boolean> sendSnoopRead(
      final A address,
      final BitVector oldEntry,
      final boolean invalidate) {

    var result = snooped;

    // Broadcast snoop requests to the same-level caches.
    for (final CacheUnit<?, A> other : neighbor) {
      InvariantChecks.checkTrue(other != this);
      final var snooped = other.snoopRead(address, oldEntry, invalidate);

      if (snooped != null && (result == null || snooped.second)) {
        result = snooped;
      }
    }

    // If no data are available at this level, access the next one.
    return (result != null || oldEntry != null) ? result : readThrough(address);
  }

  final Pair<? extends Struct<?>, Boolean> sendSnoopWrite(
      final A address,
      final BitVector oldEntry,
      final int lower,
      final int upper,
      final BitVector newData) {

    final BitVector newEntry = oldEntry;

    // If there is a hit, update the existing entry.
    if (newEntry != null) {
      newEntry.field(lower, upper).assign(newData);
    }

    var result = snooped;

    // Broadcast snoop requests to the same-level caches.
    final boolean invalidate = newEntry == null && !policy.write.alloc;
    for (final CacheUnit<?, A> other : neighbor) {
      InvariantChecks.checkTrue(other != this);
      final var snooped = !invalidate
          ? other.snoopWrite(address, newEntry)
          : other.snoopRead(address, null, true);

      if (!invalidate && snooped != null && (result == null || snooped.second)) {
        result = snooped;
      }
    }

    if (result == null && newEntry == null && policy.write.alloc) {
      // If no data are available at this level, access the next one.
      result = readThrough(address);
    }

    // Do write-through if required.
    if (policy.write.through) {
      writeThrough(address, lower, upper, newData);
    }

    return result;
  }

  final boolean sendSnoopEvict(
      final ReplaceableBuffer<?, A> initiator,
      final A address,
      final BitVector oldEntry,
      final boolean dirty) {

    var result = snooped;

    // Broadcast snoop requests to the same-level caches.
    final boolean invalidate = dirty && policy.write.back && policy.inclusion.no;
    for (final CacheUnit<?, A> other : neighbor) {
      InvariantChecks.checkTrue(other != this);
      final var snooped = !invalidate
          ? other.snoopEvict(address, oldEntry)
          : other.snoopRead(address, oldEntry, true);

      if (snooped != null && (result == null || snooped.second)) {
        result = snooped;
      }
    }

    // Invalidate the previous caches (backward invalidation).
    boolean clean = !dirty;
    for (final CacheUnit<?, A> other : previous) {
      if (other.isHit(address) && other.policy.inclusion.yes) {
        if (other.evictEntry(initiator, address)) {
          clean = true;
        }
      }
    }

    // Do write-back if required.
    if (policy.write.back && !clean) {
      InvariantChecks.checkNotNull(initiator);
      InvariantChecks.checkNotNull(initiator.getNext());

      // If the initiator does backward invalidation and we are at the previous level,
      // it makes sense to write back to the initiator's next cache.
      initiator.getNext().writeEntry(address, oldEntry);
      return true;
    }

    // Reallocate the entry to the next cache if no copies are left.
    if (result == null && policy.inclusion.no) {
      if (next != null && next instanceof CacheUnit) {
        InvariantChecks.checkFalse(next.isHit(address),
            String.format("Exclusiveness violation: %s", address.getValue().toHexString()));

        final var other = (CacheUnit<?, A>) next;
        other.allocEntry(address);

        // This makes a snoop operation returns the reallocated entry.
        other.snooped = new Pair<>(other.newEntry(address, oldEntry), dirty);
        // Update the protocol state by executing the read or write operation.
        other.readEntry(address);
        // Disable that snooping hack.
        other.snooped = null;

        return true;
      }
    }

    return !dirty;
  }

  final Pair<? extends Struct<?>, Boolean> readThrough(final A address) {
    // No forward link.
    if (next == null) {
      return null;
    }

    if (next instanceof CacheUnit) {
      // Exclusive or inclusive cache.
      final var cache = (CacheUnit<? extends Struct<?>, A>) next;
      return cache.readEntry(address, policy.inclusion.no);
    }

    // Main memory.
    return new Pair<>(next.readEntry(address), false);
  }

  final void writeThrough(
      final A address,
      final int lower,
      final int upper,
      final BitVector newData) {

    // No forward link.
    if (next == null) {
      return;
    }

    // Inclusive cache.
    if (policy.inclusion.yes || policy.inclusion.dontCare) {
      next.writeEntry(address, lower, upper, newData);
      return;
    }

    // Exclusive cache.
    boolean allocated = policy.write.alloc || isHit(address);
    var other = next;

    while (other != null) {
      final var cache = other instanceof CacheUnit ? (CacheUnit<?, A>) other : null;

      if (cache != null) {
        if (cache.policy.write.alloc && !allocated) {
          // Write the entry into the cache.
          cache.writeEntry(address, lower, upper, newData);
          allocated = true;
        } else {
          // Invalidate the entry to maintain exclusiveness.
          cache.readEntry(address, true);
        }
      } else {
        // Write the entry into the main memory.
        other.writeEntry(address, lower, upper, newData);
      }

      other = cache != null ? cache.next : null;
    }
  }

  public final Proxy writeEntry(final A address) {
    return new Proxy(address);
  }

  private final CacheSet<E, A> getSet(final BitVector index) {
    CacheSet<E, A> set = sets.get(index);

    if (set == null) {
      set = newSet(index);
      sets.set(index, set);
    }

    return set;
  }

  private final CacheSet<E, A> getSet(final A address) {
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