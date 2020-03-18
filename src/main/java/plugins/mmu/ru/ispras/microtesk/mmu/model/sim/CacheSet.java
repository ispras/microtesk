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

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CacheSet} implements a cache set, which is a fully associative buffer consisting of cache lines.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class CacheSet<E extends Struct<?>, A extends Address<?>>
    extends Buffer<E, A> implements Snoopable<E, A> {

  /** Cache policy. */
  private final CachePolicy policy;
  /** Entry-address matcher. */
  private final Matcher<E, A> matcher;
  /** Cache that contains this set. */
  private final Cache<E, A> cache;
  /** Next-level buffer. */
  private final Buffer<? extends Struct<?>, A> next;

  /** Array of cache lines. */
  private final List<CacheLine<E, A>> lines = new ArrayList<>();
  /** Eviction policy with inner state. */
  private final EvictionPolicy evictionPolicy;

  /**
   * Constructs a cache set of the given associativity.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   * @param associativity the number of lines in the set.
   * @param policy the cache policy.
   * @param matcher the entry-address matcher.
   * @param cache the current cache.
   * @param next the next-level buffer.
   */
  public CacheSet(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final int associativity,
      final CachePolicy policy,
      final Matcher<E, A> matcher,
      final Cache<E, A> cache,
      final Buffer<? extends Struct<?>, A> next) {
    super(entryCreator, addressCreator);

    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policy);
    InvariantChecks.checkNotNull(matcher);
    InvariantChecks.checkNotNull(cache);

    this.policy = policy;
    this.matcher = matcher;
    this.cache = cache;
    this.next = next;

    // Fill the set with the default (invalid) lines.
    for (int i = 0; i < associativity; i++) {
      final CacheLine<E, A> line = newLine();
      lines.add(line);
    }

    this.evictionPolicy = policy.eviction.newPolicy(associativity);
  }

  protected CacheLine<E, A> newLine() {
    return new CacheLine<>(entryCreator, addressCreator, policy, matcher, cache);
  }

  @Override
  public final boolean isHit(final A address) {
    return getWay(address) != -1;
  }

  @Override
  public final E readEntry(final A address) {
    final int way = getWay(address);

    // If there is a cache hit, return the entry.
    if (way != -1) {
      evictionPolicy.onAccess(way);

      final CacheLine<E, A> line = lines.get(way);
      return line.readEntry(address);
    }

    // Otherwise, try to access the next-level cache.
    if (next != null) {
      final Struct<?> nextData = next.readEntry(address);

      if (nextData != null) {
        // Allocate the entry and return it.
        return allocEntry(address, nextData.asBitVector());
      }
    }

    return null;
  }

  @Override
  public final void writeEntry(final A address, final BitVector entry) {
     final int way = getWay(address);

    if (way != -1) {
      evictionPolicy.onAccess(way);

      final CacheLine<E, A> line = lines.get(way);
      line.writeEntry(address, entry);
      line.setDirty(true);
    } else if (policy.write.wa) {
      allocEntry(address, entry);

      final CacheLine<E, A> line = getLine(address);
      InvariantChecks.checkNotNull(line);

      line.setDirty(true);
    }

    if (next != null && policy.write.wt) {
      next.writeEntry(address, entry);
    }
  }

  @Override
  public final void evictEntry(final A address) {
    final int way = getWay(address);
    InvariantChecks.checkNotNull(way != -1);

    final CacheLine<E, A> line = lines.get(way);

    switch (policy.inclusion) {
      case INCLUSIVE:
        for (final Cache<?, A> prevCache : this.cache.previous) {
          InvariantChecks.checkTrue(prevCache.policy.inclusion == policy.inclusion);

          if (prevCache.isHit(address)) {
            // Backward invalidation of the previous caches.
            prevCache.evictEntry(address);
          }
        }
        break;

      case EXCLUSIVE:
        if (this.cache.next != null && this.cache.next instanceof Cache) {
          final Cache<?, A> nextCache = (Cache<?, A>) this.cache.next;
          nextCache.allocEntry(address, line.getEntry().asBitVector());

          final CacheLine<?, A> nextLine = nextCache.getLine(address);
          nextLine.setDirty(line.isDirty());
        }
        break;

      default:
        break;
    }

    // Evict the line from this cache.
    evictionPolicy.onEvict(way);
    line.evictEntry(address);
  }

  @Override
  public final E allocEntry(final A address, final BitVector data) {
    final int way = evictionPolicy.getVictim();
    final CacheLine<E, A> line = lines.get(way);

    if (line.isDirty() && next != null && policy.write.wb) {
      next.writeEntry(address, data);
    }

    line.writeEntry(address, data);

    evictionPolicy.onAccess(way);
    return line.getEntry();
  }

  @Override
  public final E snoopRead(final A address) {
    final CacheLine<E, A> line = getLine(address);
    return line.snoopRead(address);
  }

  @Override
  public final void snoopWrite(final A address, final BitVector entry) {
    final CacheLine<E, A> line = getLine(address);
    line.snoopWrite(address, entry);
  }

  @Override
  public final void snoopEvict(final A address) {
    final CacheLine<E, A> line = getLine(address);
    line.snoopEvict(address);
  }

  final CacheLine<E, A> getLine(final A address) {
    final int way = getWay(address);
    InvariantChecks.checkNotNull(way != -1);

    return lines.get(way);
  }

  @Override
  public Pair<BitVector, BitVector> seeEntry(final BitVector index, final BitVector way) {
    final CacheLine<E, A> line = lines.get(way.intValue());
    return line != null ? line.seeEntry(index, way) : null;
  }

  private int getWay(final A address) {
    int way = -1;

    for (int i = 0; i < lines.size(); i++) {
      final CacheLine<E, A> line = lines.get(i);

      if (line.isHit(address)) {
        InvariantChecks.checkTrue(way == -1,
            String.format("Multiple hits in a cache set. Address=%s:0x%s, Lines=%s",
                address.getClass().getSimpleName(),
                address.getValue().toHexString(),
                lines.toString()));

        way = i;
      }
    }

    return way;
  }

  @Override
  public void setUseTempState(final boolean value) {
    // Do nothing.
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
