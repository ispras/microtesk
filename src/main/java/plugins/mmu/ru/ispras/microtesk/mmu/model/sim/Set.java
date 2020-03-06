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
 * @param <D> the data type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Set<D extends Struct<?>, A extends Address<?>> extends Buffer<D, A> {
  /** Array of cache lines. */
  private final List<Line<D, A>> lines = new ArrayList<>();
  /** Data replacement policy. */
  private final EvictPolicy evictPolicy;
  /** Data write policy. */
  private final WritePolicyId writePolicyId;
  /** Line matcher. */
  private final Matcher<D, A> matcher;
  /** Line coercer. */
  private final Coercer<D> coercer;
  /** Next-level buffer. */
  final Buffer<? extends Struct<?>, A> next;

  /**
   * Constructs a cache set of the given associativity.
   *
   * @param dataCreator the data creator.
   * @param addressCreator the address creator.
   * @param associativity the number of lines in the set.
   * @param evictPolicyId the data replacement policy.
   * @param writePolicyId the data write policy.
   * @param matcher the line matcher.
   * @param coercer the line coercer.
   * @param next the next-level buffer.
   */
  public Set(
      final Struct<D> dataCreator,
      final Address<A> addressCreator,
      final int associativity,
      final EvictPolicyId evictPolicyId,
      final WritePolicyId writePolicyId,
      final Matcher<D, A> matcher,
      final Coercer<D> coercer,
      final Buffer<? extends Struct<?>, A> next) {
    super(dataCreator, addressCreator);

    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(evictPolicyId);
    InvariantChecks.checkNotNull(writePolicyId);
    InvariantChecks.checkNotNull(matcher);
    InvariantChecks.checkTrue(next == null || coercer != null);

    this.matcher = matcher;
    this.coercer = coercer;
    this.next = next;

    // Fill the set with the default (invalid) lines.
    for (int i = 0; i < associativity; i++) {
      final Line<D, A> line = newLine();
      lines.add(line);
    }

    this.evictPolicy = evictPolicyId.newPolicy(associativity);
    this.writePolicyId = writePolicyId;
  }

  protected Line<D, A> newLine() {
    return new Line<>(dataCreator, addressCreator, matcher, coercer);
  }

  @Override
  public final boolean isHit(final A address) {
    return getLine(address) != null;
  }

  @Override
  public final D getData(final A address) {
    final Line<D, A> line = getLine(address);

    // If there is a cache hit, returns the data
    if (line != null) {
      return line.getData(address);
    }

    // Otherwise, tries to access the next-level cache.
    if (next != null) {
      final Struct<?> nextData = next.getData(address);

      if (nextData != null) {
        final D data = coercer.coerce(nextData.asBitVector());
        InvariantChecks.checkNotNull(data);

        // Allocates the data and returns them.
        allocData(address, data.asBitVector(), false);
        return data;
      }
    }

    return null;
  }

  @Override
  public final D setData(final A address, final BitVector data) {
    final Line<D, A> line = getLine(address);

    final D oldData;
    if (line != null) {
      oldData = line.setData(address, data);
      line.setDirty(true);
    } else if (writePolicyId.wa) {
      // Allocates the data and returns them.
      oldData = allocData(address, data, true);
    } else {
      oldData = null;
    }

    if (next != null && writePolicyId.wt) {
      next.setData(address, data);
    }

    return oldData;
  }

  private final D allocData(final A address, final BitVector data, final boolean dirty) {
    final Line<D, A> line = lines.get(evictPolicy.chooseVictim());

    if (line.isDirty() && writePolicyId.wb) {
      next.setData(address, data);
    }

    line.setDirty(dirty);
    return line.setData(address, data);
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    final Buffer<D, A> line = lines.get(way.intValue());
    return line != null ? line.seeData(index, way) : null;
  }

  /**
   * Returns the line associated with the given address.
   *
   * @param address the data address.
   * @return the line associated with the given address if it exists; {@code null} otherwise.
   */
  private Line<D, A> getLine(final A address) {
    int index = -1;

    for (int i = 0; i < lines.size(); i++) {
      final Line<D, A> line = lines.get(i);

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
    for (final Buffer<D, A> line : lines) {
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

      final Buffer<D, A> line = lines.get(index);
      sb.append(String.format("%d: %s", index, line));
    }

    sb.append(']');
    return sb.toString();
  }
}
