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
public class Set<D extends Struct, A extends Address> extends Buffer<D, A> {
  /** The line matcher. */
  private final Matcher<D, A> matcher;

  /** The array of cache lines. */
  private final List<Buffer<D, A>> lines = new ArrayList<>();

  /** The data replacement policy. */
  private final Policy policy;

  /**
   * Constructs a cache set of the given associativity.
   *
   * @param dataCreator the data creator.
   * @param addressCreator the address creator.
   * @param associativity the number of lines in the set.
   * @param policyId the identifier of the data replacement policy.
   * @param matcher the data-address matcher.
   */
  public Set(
      final Struct<D> dataCreator,
      final Address<A> addressCreator,
      final int associativity,
      final PolicyId policyId,
      final Matcher<D, A> matcher) {
    super(dataCreator, addressCreator);

    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policyId);
    InvariantChecks.checkNotNull(matcher);

    this.matcher = matcher;

    // Fill the set with the default (invalid) lines.
    for (int i = 0; i < associativity; i++) {
      final Buffer<D, A> line = newLine();
      lines.add(line);
    }

    this.policy = policyId.newPolicy(associativity);
  }

  protected Buffer<D, A> newLine() {
    return new Line<D, A>(dataCreator, addressCreator, matcher);
  }

  @Override
  public final boolean isHit(final A address) {
    return getLine(address) != null;
  }

  @Override
  public final D getData(final A address) {
    final Buffer<D, A> line = getLine(address);
    return line != null ? line.getData(address) : null;
  }

  @Override
  public final D setData(final A address, final D data) {
    Buffer<D, A> line = getLine(address);

    // If there is a miss, choose a victim.
    if (line == null) {
      if (null != policy) {
        line = lines.get(policy.chooseVictim());
      } else {
        InvariantChecks.checkTrue(1 == lines.size());
        line = lines.get(0);
      }
    }

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
  private Buffer<D, A> getLine(final A address) {
    int index = -1;

    for (int i = 0; i < lines.size(); i++) {
      final Buffer<D, A> line = lines.get(i);

      if (line.isHit(address)) {
        InvariantChecks.checkTrue(index == -1,
            String.format("Multiple hits in a cache set. Address=%s:0x%s, Lines=%s",
                address.getClass().getSimpleName(),
                address.getValue().toHexString(),
                lines.toString()));

        index = i;
      }
    }

    if (index != -1 && policy != null) {
      policy.accessLine(index);
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

    policy.resetState();
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
