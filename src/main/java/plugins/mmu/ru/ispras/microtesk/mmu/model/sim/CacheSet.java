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

import java.util.ArrayList;
import java.util.List;
import ru.ispras.fortress.util.Pair;

/**
 * {@link CacheSet} implements a cache set, i.e. a fully associative buffer of cache lines.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class CacheSet<E extends Struct<?>, A extends Address<?>>
    implements ReplaceableBuffer<E, A>, SnoopController<E, A> {

  /** Cache policy. */
  private final CachePolicy policy;
  /** Entry-address matcher. */
  private final Matcher<E, A> matcher;
  /** Cache that contains this set. */
  private final CacheUnit<E, A> cache;
  /** Next-level buffer. */
  private final Buffer<? extends Struct<?>, A> next;

  /** Array of cache lines. */
  private final List<CacheLine<E, A>> lines = new ArrayList<>();
  /** Eviction policy w/ inner state. */
  private final EvictionPolicy evictionPolicy;

  /**
   * Constructs a cache set of the given associativity.
   *
   * @param associativity the number of lines in the set.
   * @param policy the cache policy.
   * @param matcher the entry-address matcher.
   * @param cache the current cache.
   * @param next the next-level buffer.
   */
  public CacheSet(
      final int associativity,
      final CachePolicy policy,
      final Matcher<E, A> matcher,
      final CacheUnit<E, A> cache,
      final Buffer<? extends Struct<?>, A> next) {
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policy);
    InvariantChecks.checkNotNull(matcher);
    InvariantChecks.checkNotNull(cache);

    this.policy = policy;
    this.matcher = matcher;
    this.cache = cache;
    this.next = next;

    // Fill the set w/ the default (invalid) lines.
    for (int i = 0; i < associativity; i++) {
      lines.add(newLine(i));
    }

    this.evictionPolicy = policy.eviction.newPolicy(associativity);
  }

  protected CacheLine<E, A> newLine(final int way) {
    return new CacheLine<>(policy, matcher, cache);
  }

  @Override
  public final boolean isHit(final A address) {
    return getWay(address) != -1;
  }

  @Override
  public final E readEntry(final A address) {
    final Pair<E, Boolean> result = readEntry(address, false);
    return result != null ? result.first : null;
  }

  @Override
  public final Pair<E, Boolean> readEntry(final A address, final boolean invalidate) {
    final int way = getWay(address);

    if (way != -1) {
      // If there is a hit, return the local entry.
      final CacheLine<E, A> line = lines.get(way);

      if (invalidate) {
        evictionPolicy.onEvict(way);
      } else {
        evictionPolicy.onAccess(way);
      }

      return line.readEntry(address, invalidate);
    }

    if (!invalidate && next != null) {
      // If there is a link to the next level, allocate an entry.
      allocEntry(address);
      InvariantChecks.checkTrue(isHit(address));

      // Re-run the read operation (the entry should be valid after snooping).
      return readEntry(address, false);
    }

    if (invalidate) {
      // Send snoops w/o evicting the entry (there is nothing to evict).
      final var snooped = cache.sendSnoopRead(address, null, true);
      return new Pair<>(cache.newEntry(address, snooped.first.asBitVector()), snooped.second);
    }

    // If automation is disabled, return null.
    return null;
  }

  @Override
  public final void writeEntry(final A address, final BitVector newEntry) {
    writeEntry(address, 0, cache.getEntryBitSize() - 1, newEntry);
  }

  @Override
  public final void writeEntry(
      final A address,
      final int lower,
      final int upper,
      final BitVector newData) {

    final int way = getWay(address);

    if (way != -1) {
      // If there is a hit, write into the local entry.
      final CacheLine<E, A> line = lines.get(way);

      evictionPolicy.onAccess(way);
      line.writeEntry(address, lower, upper, newData);
      return;
    }

    if (policy.write.alloc) {
      // If the write policy implies allocation, allocate an entry.
      allocEntry(address);
      InvariantChecks.checkTrue(isHit(address));

      // Re-run the write operation to fill the allocated entry.
      writeEntry(address, lower, upper, newData);
      return;
    }

    // If allocation is disabled, send snoops w/o writing.
    cache.sendSnoopWrite(address, null, lower, upper, newData);
  }

  @Override
  public final void allocEntry(final A address) {
    InvariantChecks.checkFalse(isHit(address));

    final int way = evictionPolicy.getVictim();
    final CacheLine<E, A> line = lines.get(way);

    if (line.isValid()) {
      // Evict the chosen old entry.
      line.evictEntry(cache, line.getAddress());
    }

    // Allocate a new one.
    line.allocEntry(address);
  }

  @Override
  public final boolean evictEntry(final ReplaceableBuffer<?, A> initiator, final A address) {
    InvariantChecks.checkTrue(isHit(address));

    final int way = getWay(address);
    final CacheLine<E, A> line = lines.get(way);

    // Evict the line from the cache.
    evictionPolicy.onEvict(way);
    return line.evictEntry(initiator, address);
  }

  @Override
  public final Pair<E, Boolean> snoopRead(
      final A address, final BitVector oldEntry, final boolean invalidate) {
    final CacheLine<E, A> line = getLine(address);
    return line != null ? line.snoopRead(address, oldEntry, invalidate) : null;
  }

  @Override
  public final Pair<E, Boolean> snoopWrite(final A address, final BitVector newEntry) {
    final CacheLine<E, A> line = getLine(address);
    return line != null ? line.snoopWrite(address, newEntry) : null;
  }

  @Override
  public final Pair<E, Boolean> snoopEvict(final A address, final BitVector oldEntry) {
    final CacheLine<E, A> line = getLine(address);
    return line != null ? line.snoopEvict(address, oldEntry) : null;
  }

  @Override
  public final Buffer<?, A> getNext() {
    return cache.getNext();
  }

  final CacheLine<E, A> getLine(final A address) {
    final int way = getWay(address);
    return getLine(way);
  }

  final CacheLine<E, A> getLine(final int way) {
    return way != -1 ? lines.get(way) : null;
  }

  final int getWay(final A address) {
    int way = -1;

    for (int i = 0; i < lines.size(); i++) {
      final CacheLine<E, A> line = lines.get(i);

      if (line.isHit(address)) {
        InvariantChecks.checkTrue(way == -1,
            String.format("Multiple hits in a cache set: address=%s:0x%s, lines=%s",
                address.getClass().getSimpleName(),
                address.getValue().toHexString(),
                lines.toString()));

        way = i;
      }
    }

    return way;
  }

  @Override
  public void resetState() {
    for (final Buffer<E, A> line : lines) {
      line.resetState();
    }

    evictionPolicy.resetState();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Set [");

    for (int way = 0; way < lines.size(); way++) {
      if (way != 0) {
        sb.append(", ");
      }

      final Buffer<E, A> line = lines.get(way);
      sb.append(String.format("%d: %s", way, line));
    }

    sb.append(']');
    return sb.toString();
  }
}
