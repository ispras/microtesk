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
 * @param <E> Entry type.
 * @param <A> Address type.
 */
public abstract class MmuMapping<E extends Struct<?>, A extends Address<?>>
    implements Buffer<E, A> {

  private Struct<E> entryCreator;
  private Struct<A> addressCreator;

  private final BigInteger length;
  private final int associativity;
  private final CachePolicy policy;
  private final Indexer<A> indexer;
  private final Matcher<E, A> matcher;

  /**
   * Constructs a memory-mapped buffer of the given length and associativity.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param policy the cache policy.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */
  public MmuMapping(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final BigInteger length,
      final int associativity,
      final CachePolicy policy,
      final Indexer<A> indexer,
      final Matcher<E, A> matcher) {
    InvariantChecks.checkNotNull(entryCreator);
    InvariantChecks.checkNotNull(addressCreator);
    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policy);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.entryCreator = entryCreator;
    this.addressCreator = addressCreator;
    this.length = length;
    this.associativity = associativity;
    this.policy = policy;
    this.indexer = indexer;
    this.matcher = matcher;
  }

  @Override
  public boolean isHit(final A address) {
    // TODO
    return getMmu().isHit(address);
  }

  @Override
  public E readEntry(final A address) {
    final BitVector value = getMmu().readEntry(address);
    InvariantChecks.checkTrue(value.getBitSize() == entryCreator.getBitSize());
    return entryCreator.newStruct(value);
  }

  @Override
  public void writeEntry(final A address, final BitVector data) {
    InvariantChecks.checkTrue(data.getBitSize() == entryCreator.getBitSize());
    getMmu().writeEntry(address, data);
  }

  @Override
  public void writeEntry(final A address, final int lower, final int upper, final BitVector data) {
    getMmu().writeEntry(address, lower, upper, data);
  }

  protected abstract Mmu<A> getMmu();

  @Override
  public void resetState() {
    // Do nothing.
  }
}
