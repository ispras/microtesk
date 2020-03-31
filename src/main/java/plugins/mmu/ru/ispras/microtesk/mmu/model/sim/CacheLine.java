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
 * {@link CacheLine} represents an abstract cache line.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class CacheLine<E extends Struct<?>, A extends Address<?>>
    implements ReplaceableBuffer<E, A>, SnoopController<E, A> {

  /** Coherence protocol. */
  private final CoherenceProtocol protocol;
  /** Line matcher. */
  private final Matcher<E, A> matcher;
  /** Cache that contains this line. */
  private final CacheUnit<E, A> cache;

  /** Stored entry. */
  private E entry;
  /** Entry address. */
  private A address;

  /** Dirty bit used to implement the write-back policy. */
  private boolean dirty;
  /** Coherence protocol state. */
  private Enum<?> state;

  /**
   * Constructs an invalid cache line.
   *
   * @param policy the cache policy.
   * @param matcher the entry-address matcher.
   * @param cache the current cache.
   */
  public CacheLine(
      final CachePolicy policy,
      final Matcher<E, A> matcher,
      final CacheUnit<E, A> cache) {
    InvariantChecks.checkNotNull(policy);
    InvariantChecks.checkNotNull(matcher);
    InvariantChecks.checkNotNull(cache);

    this.matcher = matcher;
    this.cache = cache;

    this.protocol = policy.coherence.newProtocol();
    this.state = this.protocol.onReset();

    this.entry = null;
    this.address = null;
    this.dirty = false;
  }

  public final boolean isValid() {
    return entry != null && state != protocol.onReset();
  }

  public final E getEntry() {
    return entry;
  }

  public final void setEntry(final BitVector entry) {
    this.entry.asBitVector().assign(entry);
  }

  public final A getAddress() {
    return address;
  }

  public final void setAddress(final A address) {
    this.address = address;
  }

  public final Enum<?> getState() {
    return state;
  }

  @Override
  public boolean isHit(final A address) {
    // After allocation, when the entry is invalid but not null, the method may return true.
    return entry != null ? matcher.areMatching(entry, address) : false;
  }

  @Override
  public E readEntry(final A address) {
    final Pair<E, Boolean> result = readEntry(address, false);
    return result != null ? result.first : null;
  }

  @Override
  public Pair<E, Boolean> readEntry(final A address, final boolean invalidate) {
    // The entry should be allocated but not necessarily valid.
    InvariantChecks.checkTrue(isHit(address));

    final var oldEntry = isValid() ? entry.asBitVector() : null;
    final var snooped = cache.sendSnoopRead(address, oldEntry, invalidate);
    InvariantChecks.checkTrue(isValid() || snooped != null);

    if (!isValid()) {
      cache.assignEntry(entry, address, snooped.first.asBitVector());
      dirty = snooped.second;
    }

    final Pair<E, Boolean> result = new Pair<>(entry, dirty);

    if (invalidate) {
      resetState();
    } else if (!isValid() && snooped.second) {
      state = protocol.onWrite(state);
    } else {
      state = protocol.onRead(state, cache.isExclusive(address));
    }

    InvariantChecks.checkTrue(cache.isCoherent(address));
    return result;
  }

  @Override
  public void writeEntry(final A address, final BitVector newEntry) {
    writeEntry(address, 0, cache.getEntryBitSize() - 1, newEntry);
  }

  @Override
  public void writeEntry(
      final A address,
      final int lower,
      final int upper,
      final BitVector newData) {
    // The entry should be allocated but not necessarily valid.
    InvariantChecks.checkTrue(isHit(address));

    final var oldEntry = isValid() ? entry.asBitVector() : null;
    final var snooped = cache.sendSnoopWrite(address, oldEntry, lower, upper, newData);
    InvariantChecks.checkTrue(isValid() || snooped != null);

    if (!isValid()) {
      // Place a snooped entry into the line.
      cache.assignEntry(entry, address, snooped.first.asBitVector());
    }

    // Update the required field of the entry.
    cache.assignEntry(entry, address, lower, upper, newData);
    dirty = true;

    state = protocol.onWrite(state);
    InvariantChecks.checkTrue(cache.isCoherent(address));
  }

  @Override
  public void allocEntry(final A address) {
    // The entry should be unallocated.
    InvariantChecks.checkFalse(isHit(address));

    this.entry = cache.newEntry(address);
    this.address = address;

    state = protocol.onReset();
    InvariantChecks.checkTrue(cache.isCoherent(address));
  }

  @Override
  public boolean evictEntry(final ReplaceableBuffer<?, A> initiator, final A address) {
    // The entry should be allocated but not necessarily valid.
    InvariantChecks.checkTrue(isHit(address));

    // Do nothing if the entry is allocated but invalid (not written yet).
    if (isValid()) {
      final boolean result = cache.sendSnoopEvict(initiator, address, entry.asBitVector(), dirty);
      resetState();

      InvariantChecks.checkTrue(cache.isCoherent(address));
      return result;
    }

    return true;
  }

  @Override
  public final Pair<E, Boolean> snoopRead(
      final A address, final BitVector oldEntry, final boolean invalidate) {
    InvariantChecks.checkTrue(isHit(address));

    final Pair<E, Boolean> result = new Pair<>(entry, dirty);

    if (invalidate) {
      resetState();
      return result;
    }

    final Enum<?> newState = protocol.onSnoopRead(state);

    if (isValid() && newState == protocol.onReset()) {
      // If eviction is caused by a snoop, the snoop receiver serves as an initiator.
      evictEntry(cache, address);
    }

    state = newState;

    return result;
  }

  @Override
  public final Pair<E, Boolean> snoopWrite(final A address, final BitVector newEntry) {
    InvariantChecks.checkTrue(isHit(address));

    final Pair<E, Boolean> result = new Pair<>(entry, dirty);
    final Enum<?> newState = protocol.onSnoopWrite(state);

    if (isValid() && newState == protocol.onReset()) {
      // Write-back is not required, because a neighbor contains the updated line.
      dirty = false;
      // If eviction is caused by a snoop, the snoop receiver serves as an initiator.
      evictEntry(cache, address);
    }

    state = newState;
    return result;
  }

  @Override
  public final Pair<E, Boolean> snoopEvict(final A address, final BitVector oldEntry) {
    InvariantChecks.checkTrue(isHit(address));

    final Pair<E, Boolean> result = new Pair<>(entry, dirty);
    final Enum<?> newState = protocol.onSnoopEvict(state);

    if (isValid() && newState == protocol.onReset()) {
      // If eviction is caused by a snoop, the snoop receiver serves as an initiator.
      evictEntry(cache, address);
    }

    state = newState;
    return result;
  }

  @Override
  public final Buffer<?, A> getNext() {
    return cache.getNext();
  }

  @Override
  public void resetState() {
    entry = null;
    address = null;
    dirty = false;
    state = protocol.onReset();
  }

  @Override
  public String toString() {
    return String.format("Line [entry=%s]", entry);
  }
}
