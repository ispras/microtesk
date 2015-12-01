/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.api;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.memory.MemoryDevice;
import ru.ispras.microtesk.model.api.memory.MemoryDeviceWrapper;

public abstract class RegisterMapping<D extends Data, A extends Address>
    implements Buffer<D, A> {

  private final MemoryDevice storage;

  private final BigInteger length;
  private final int associativity;
  private final PolicyId policyId;
  private final Indexer<A> indexer;
  private final Matcher<D, A> matcher;

  /**
   * Proxy class is used to simplify code of assignment expressions.
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
      final D data = newData(value);
      return setData(address, data);
    }
  }

  /**
   * Constructs a register-mapped buffer of the given length and associativity.
   * 
   * @param name Name of the register file mapped to the buffer.
   * @param length the number of sets in the buffer.
   * @param associativity the number of lines in each set.
   * @param indexer the set indexer.
   * @param matcher the line matcher.
   */
  public RegisterMapping(
      final String name,
      final BigInteger length,
      final int associativity,
      final PolicyId policyId,
      final Indexer<A> indexer,
      final Matcher<D, A> matcher) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);
    InvariantChecks.checkGreaterThanZero(associativity);
    InvariantChecks.checkNotNull(policyId);
    InvariantChecks.checkNotNull(indexer);
    InvariantChecks.checkNotNull(matcher);

    this.storage = MemoryDeviceWrapper.newWrapperFor(name);
    InvariantChecks.checkTrue(getDataBitSize() == storage.getDataBitSize());

    this.length = length;
    this.associativity = associativity;
    this.policyId = policyId;
    this.indexer = indexer;
    this.matcher = matcher;
  }

  @Override
  public final boolean isHit(final A address) {
    throw new UnsupportedOperationException(
        "isHit is unsupported for mapped buffers.");
  }

  @Override
  public final D getData(final A address) {
    final BitVector value = storage.load(address.getValue());
    return newData(value);
  }

  @Override
  public final D setData(final A address, final D data) {
    storage.store(address.getValue(), data.asBitVector());
    return null;
  }

  public final Proxy setData(final A address) {
    return new Proxy(address);
  }

  protected abstract D newData(final BitVector value);
  protected abstract int getDataBitSize();
}
