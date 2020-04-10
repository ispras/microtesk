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
import ru.ispras.microtesk.model.memory.MemoryDevice;
import ru.ispras.microtesk.test.TestEngine;

import java.math.BigInteger;

/**
 * {@link RegisterMapping} implements a register-mapped buffer.
 *
 * @param <E> the entry type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class RegisterMapping<E extends Struct<?>, A extends Address<?>>
    extends CacheUnit<E, A> {

  private final String name;
  private BigInteger currentRegisterIndex;

  /**
   * {@link RegisterMappedSet} is an extension of {@link CacheSet} for register-mapped buffers.
   */
  private final class RegisterMappedSet extends CacheSet<E, A> {
    public RegisterMappedSet() {
      super(
          associativity,
          policy,
          matcher,
          RegisterMapping.this,
          null
      );
    }

    @Override
    protected CacheLine<E, A> newLine() {
      return new RegisterMappedLine();
    }
  }

  /**
   * {@link RegisterMappedLine} is an implementation of a line for register-mapped buffers.
   */
  private final class RegisterMappedLine extends CacheLine<E, A> {
    private final BitVector registerIndex;

    private RegisterMappedLine() {
      super(
          policy,
          matcher,
          RegisterMapping.this);

      final MemoryDevice storage = getRegisterDevice();
      this.registerIndex = BitVector.valueOf(currentRegisterIndex, storage.getAddressBitSize());
      currentRegisterIndex = currentRegisterIndex.add(BigInteger.ONE);
    }

    @Override
    public boolean isHit(final A address) {
      final MemoryDevice storage = getRegisterDevice();
      if (!storage.isInitialized(registerIndex)) {
        return false;
      }

      final BitVector data = storage.load(registerIndex);
      final E entry = newEntry(data);

      return matcher.areMatching(entry, address);
    }

    @Override
    public E readEntry(final A address) {
      final MemoryDevice storage = getRegisterDevice();
      final BitVector data = storage.load(registerIndex);

      return newEntry(data);
    }

    @Override
    public void writeEntry(final A address, final BitVector data) {
      final MemoryDevice storage = getRegisterDevice();
      storage.store(registerIndex, data);
    }

    @Override
    public void writeEntry(
        final A address,
        final int lower,
        final int upper,
        final BitVector newData) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void allocEntry(final A address) {
    }

    @Override
    public boolean evictEntry(final ReplaceableBuffer<?, A> initiator, final A address) {
      return true;
    }

    @Override
    public void resetState() {
      // Do nothing.
    }

    @Override
    public String toString() {
      final MemoryDevice storage = getRegisterDevice();
      final BitVector data = storage.load(registerIndex);

      return String.format("RegisterMappedLine [entry=%s]", newEntry(data));
    }
  }

  /**
   * Constructs a register-mapped buffer of the given length and associativity.
   *
   * @param entryCreator the entry creator.
   * @param addressCreator the address creator.
   * @param name the name of the register file mapped to the buffer.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param policy the cache policy.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */
  public RegisterMapping(
      final Struct<E> entryCreator,
      final Address<A> addressCreator,
      final String name,
      final BigInteger length,
      final int associativity,
      final CachePolicy policy,
      final Indexer<A> indexer,
      final Matcher<E, A> matcher) {
    super(entryCreator, addressCreator, length, associativity, policy, indexer, matcher, null);

    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policy);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.name = name;

    final MemoryDevice storage = getRegisterDevice();
    InvariantChecks.checkTrue(getEntryBitSize() == storage.getDataBitSize());

    this.currentRegisterIndex = BigInteger.ZERO;

    for (BigInteger index = BigInteger.ZERO;
         index.compareTo(length) < 0;
         index = index.add(BigInteger.ONE)) {
      setSet(BitVector.valueOf(index, storage.getAddressBitSize()), new RegisterMappedSet());
    }
  }

  private MemoryDevice getRegisterDevice() {
    return TestEngine.getInstance().getModel().getPE().getMemoryDevice(name);
  }

  @Override
  public void setUseTempState(final boolean value) {
    // Do nothing.
  }

  @Override
  public void resetState() {
    // Do nothing.
  }
}
