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
 * {@link Set} implements a cache set, which is a fully associative buffer consisting of cache lines.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Set<E extends Struct<?>, A extends Address<?>> extends Buffer<E, A> {
  /** Array of cache lines. */
  private final List<Line<E, A>> lines = new ArrayList<>();
  /** Entry replacement policy. */
  private final EvictPolicy evictPolicy;
  /** Entry write policy. */
  private final WritePolicyId writePolicyId;
  /** Entry-address matcher. */
  private final Matcher<E, A> matcher;
  /** Next-level buffer. */
  final Buffer<? extends Struct<?>, A> next;

  /**
   * Constructs a cache set of the given associativity.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   * @param associativity the number of lines in the set.
   * @param evictPolicyId the entry replacement policy.
   * @param writePolicyId the entry write policy.
   * @param matcher the entry-address matcher.
   * @param next the next-level buffer.
   */
  public Set(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final int associativity,
      final EvictPolicyId evictPolicyId,
      final WritePolicyId writePolicyId,
      final Matcher<E, A> matcher,
      final Buffer<? extends Struct<?>, A> next) {
    super(entryCreator, addressCreator);

    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(evictPolicyId);
    InvariantChecks.checkNotNull(writePolicyId);
    InvariantChecks.checkNotNull(matcher);

    this.matcher = matcher;
    this.next = next;

    // Fill the set with the default (invalid) lines.
    for (int i = 0; i < associativity; i++) {
      final Line<E, A> line = newLine();
      lines.add(line);
    }

    this.evictPolicy = evictPolicyId.newPolicy(associativity);
    this.writePolicyId = writePolicyId;
  }

  protected Line<E, A> newLine() {
    return new Line<>(entryCreator, addressCreator, matcher);
  }

  @Override
  public final boolean isHit(final A address) {
    return getLine(address) != null;
  }

  @Override
  public final E loadEntry(final A address) {
    final Line<E, A> line = getLine(address);

    // If there is a cache hit, return the entry.
    if (line != null) {
      return line.loadEntry(address);
    }

    // Otherwise, try to access the next-level cache.
    if (next != null) {
      final Struct<?> nextData = next.loadEntry(address);

      if (nextData != null) {
        // Allocate the entry and return it.
        return allocEntry(address, nextData.asBitVector(), false);
      }
    }

    return null;
  }

  @Override
  public final void storeEntry(final A address, final BitVector entry) {
    final Line<E, A> line = getLine(address);

    if (line != null) {
      line.storeEntry(address, entry);
      line.setDirty(true);
    } else if (writePolicyId.wa) {
      // Allocate the entry and write it.
      allocEntry(address, entry, true);
    }

    if (next != null && writePolicyId.wt) {
      next.storeEntry(address, entry);
    }
  }

  private final E allocEntry(final A address, final BitVector data, final boolean dirty) {
    final int index = evictPolicy != null ? evictPolicy.chooseVictim() : 0;
    final Line<E, A> line = lines.get(index);

    if (line.isDirty() && next != null && writePolicyId.wb) {
      next.storeEntry(address, data);
    }

    line.setDirty(dirty);
    line.storeEntry(address, data);

    if (evictPolicy != null) {
      evictPolicy.accessLine(index);
    }

    return line.getEntry();
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    final Buffer<E, A> line = lines.get(way.intValue());
    return line != null ? line.seeData(index, way) : null;
  }

  /**
   * Returns the line associated with the given address.
   *
   * @param address the address.
   * @return the line associated with the given address if it exists; {@code null} otherwise.
   */
  private Line<E, A> getLine(final A address) {
    int index = -1;

    for (int i = 0; i < lines.size(); i++) {
      final Line<E, A> line = lines.get(i);

      if (line.isHit(address)) {
        InvariantChecks.checkTrue(index == -1,
            String.format("Multiple hits in a cache set. Address=%s:0x%s, Lines=%s",
                address.getClass().getSimpleName(),
                address.getValue().toHexString(),
                lines.toString()));

        index = i;
      }
    }

    if (index != -1 && evictPolicy != null) {
      evictPolicy.accessLine(index);
    }

    return index == -1 ? null : lines.get(index);
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

    evictPolicy.resetState();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Set [");

    for (int index = 0; index < lines.size(); index++) {
      if (0 != index) {
        sb.append(", ");
      }

      final Buffer<E, A> line = lines.get(index);
      sb.append(String.format("%d: %s", index, line));
    }

    sb.append(']');
    return sb.toString();
  }
}
