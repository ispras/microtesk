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
import ru.ispras.microtesk.model.memory.MemoryDevice;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.utils.SparseArray;

import java.math.BigInteger;

/**
 * {@link RegisterMapping} implements a register-mapped buffer.
 *
 * @param <D> the data type.
 * @param <A> the address type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class RegisterMapping<D extends Struct<?>, A extends Address<?>>
    extends Buffer<D, A> {

  private final String name;

  private final int associativity;
  private final EvictPolicyId evictPolicyId;
  private final WritePolicyId writePolicyId;

  private final Indexer<A> indexer;
  private final Matcher<D, A> matcher;

  private final SparseArray<Buffer<D, A>> sets;
  private BigInteger currentRegisterIndex;

  /**
   * {@link RegisterMappedSet} is an extension of {@link Set} for register-mapped buffers.
   */
  private final class RegisterMappedSet extends Set<D, A> {
    public RegisterMappedSet() {
      super(
          RegisterMapping.this.dataCreator,
          RegisterMapping.this.addressCreator,
          associativity,
          evictPolicyId,
          writePolicyId,
          matcher,
          null,
          null
      );
    }

    @Override
    protected Line<D, A> newLine() {
      return new RegisterMappedLine();
    }
  }

  /**
   * {@link RegisterMappedLine} is an implementation of a line for register-mapped buffers.
   */
  private final class RegisterMappedLine extends Line<D, A> {
    private final BitVector registerIndex;

    private RegisterMappedLine() {
      super(RegisterMapping.this.dataCreator, RegisterMapping.this.addressCreator, null, null);

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

      final BitVector rawData = storage.load(registerIndex);
      final D data = dataCreator.newStruct(rawData);

      return matcher.areMatching(data, address);
    }

    @Override
    public D getData(final A address) {
      final MemoryDevice storage = getRegisterDevice();
      final BitVector rawData = storage.load(registerIndex);
      return dataCreator.newStruct(rawData);
    }

    @Override
    public D setData(final A address, final BitVector data) {
      final MemoryDevice storage = getRegisterDevice();
      storage.store(registerIndex, data);
      return null;
    }

    @Override
    public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
      final MemoryDevice storage = getRegisterDevice();
      return storage.isInitialized(registerIndex)
          ? new Pair<>(registerIndex, storage.load(registerIndex))
          : null;
    }

    @Override
    public void setUseTempState(final boolean value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void resetState() {
      // Do nothing.
    }

    @Override
    public String toString() {
      final MemoryDevice storage = getRegisterDevice();
      final BitVector value = storage.load(registerIndex);
      return String.format("RegisterMappedLine [data=%s]", dataCreator.newStruct(value));
    }
  }

  /**
   * The {@link Proxy} class is used to simplify code of assignment expressions.
   */
  public final class Proxy {
    private final A address;

    private Proxy(final A address) {
      this.address = address;
    }

    public D assign(final D data) {
      return setData(address, data);
    }

    public D assign(final BitVector value) {
      final D data = dataCreator.newStruct(value);
      return setData(address, data);
    }
  }

  /**
   * Constructs a register-mapped buffer of the given length and associativity.
   *
   * @param dataCreator the data creator.
   * @param addressCreator the address creator.
   * @param name the name of the register file mapped to the buffer.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param evictPolicyId the data replacement policy.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */
  public RegisterMapping(
      final Struct<D> dataCreator,
      final Address<A> addressCreator,
      final String name,
      final BigInteger length,
      final int associativity,
      final EvictPolicyId evictPolicyId,
      final WritePolicyId writePolicyId,
      final Indexer<A> indexer,
      final Matcher<D, A> matcher) {
    super(dataCreator, addressCreator);

    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(evictPolicyId);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.name = name;

    final MemoryDevice storage = getRegisterDevice();
    InvariantChecks.checkTrue(getDataBitSize() == storage.getDataBitSize());

    this.associativity = associativity;
    this.evictPolicyId = evictPolicyId;
    this.writePolicyId = writePolicyId;
    this.indexer = indexer;
    this.matcher = matcher;

    this.sets = new SparseArray<>(length);
    this.currentRegisterIndex = BigInteger.ZERO;

    for (BigInteger index = BigInteger.ZERO;
         index.compareTo(length) < 0;
         index = index.add(BigInteger.ONE)) {
      final Buffer<D, A> set = new RegisterMappedSet();
      final BitVector setIndex = BitVector.valueOf(index, storage.getAddressBitSize());
      sets.set(setIndex, set);
    }
  }

  private MemoryDevice getRegisterDevice() {
    return TestEngine.getInstance().getModel().getPE().getMemoryDevice(name);
  }

  @Override
  public final boolean isHit(final A address) {
    final Buffer<D, A> set = getSet(address);
    return null != set && set.isHit(address);
  }

  @Override
  public final D getData(final A address) {
    final Buffer<D, A> set = getSet(address);
    return set.getData(address);
  }

  @Override
  public final D setData(final A address, final BitVector data) {
    final Buffer<D, A> set = getSet(address);
    return set.setData(address, data);
  }

  public final Proxy setData(final A address) {
    return new Proxy(address);
  }

  @Override
  public Pair<BitVector, BitVector> seeData(final BitVector index, final BitVector way) {
    final Buffer<D, A> set = sets.get(index);
    return null != set ? set.seeData(index, way) : null;
  }

  private Buffer<D, A> getSet(final A address) {
    final BitVector index = indexer.getIndex(address);
    return sets.get(index);
  }

  protected abstract int getDataBitSize();

  @Override
  public void setUseTempState(final boolean value) {
    // Do nothing.
  }

  @Override
  public void resetState() {
    // Do nothing.
  }

  @Override
  public String toString() {
    return String.format("%s %s", getClass().getSimpleName(), sets);
  }
}
