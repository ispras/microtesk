/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;

/**
 * {@link MmuMapping} describes a buffer mapped to memory.
 *
 * <p>An access to such a buffer causes a access to memory by virtual address using MMU
 * (address translation, caches, physical memory).</p>
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <D> Data type.
 * @param <A> Address type.
 */
public abstract class MmuMapping<D extends Struct<?>, A extends Address<?>> extends Buffer<D, A> {

  private final BigInteger length;
  private final int associativity;
  private final EvictPolicyId evictPolicyId;
  private final WritePolicyId writePolicyId;
  private final Indexer<A> indexer;
  private final Matcher<D, A> matcher;

  /**
   * Constructs a memory-mapped buffer of the given length and associativity.
   *
   * @param dataCreator the data creator.
   * @param addressCreator the address creator.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param evictPolicyId the data replacement policy.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */
  public MmuMapping(
      final Struct<D> dataCreator,
      final Address<A> addressCreator,
      final BigInteger length,
      final int associativity,
      final EvictPolicyId evictPolicyId,
      final WritePolicyId writePolicyId,
      final Indexer<A> indexer,
      final Matcher<D, A> matcher) {
    super(dataCreator, addressCreator);

    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(evictPolicyId);
    InvariantChecks.checkNotNull(writePolicyId);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.length = length;
    this.associativity = associativity;
    this.evictPolicyId = evictPolicyId;
    this.writePolicyId = writePolicyId;
    this.indexer = indexer;
    this.matcher = matcher;
  }

  @Override
  public boolean isHit(final A address) {
    // TODO
    return getMmu().isHit(address);
  }

  @Override
  public D getData(final A address) {
    final BitVector value = getMmu().getData(address);
    InvariantChecks.checkTrue(value.getBitSize() == getDataBitSize());
    return dataCreator.newStruct(value);
  }

  @Override
  public void setData(final A address, final BitVector data) {
    InvariantChecks.checkTrue(data.getBitSize() == getDataBitSize());
    getMmu().setData(address, data);
  }

  @Override
  public Pair<BitVector, BitVector> seeData(BitVector index, BitVector way) {
    throw new UnsupportedOperationException();
  }

  protected abstract Mmu<A> getMmu();

  protected abstract int getDataBitSize();

  @Override
  public void setUseTempState(final boolean value) {
    // Do nothing.
  }

  @Override
  public void resetState() {
    // Do nothing.
  }
}
