/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.SharedObject;

import java.math.BigInteger;

public class LazyValue extends SharedObject<LazyValue> implements Value {
  private interface ValueModifier {
    BitVector modify(BitVector value);
  }

  private static class Field implements ValueModifier {
    private final int start;
    private final int end;

    public Field(final int start, final int end) {
      InvariantChecks.checkGreaterOrEqZero(start);
      InvariantChecks.checkGreaterOrEqZero(end);

      this.start = start;
      this.end = end;
    }

    @Override
    public BitVector modify(final BitVector value) {
      return value.field(start, end);
    }
  }

  private static class Extended implements ValueModifier {
    private final int bitSize;
    private final boolean signExtend;

    public Extended(int bitSize, boolean signExtend) {
      InvariantChecks.checkGreaterThanZero(bitSize);

      this.bitSize = bitSize;
      this.signExtend = signExtend;
    }

    @Override
    public BitVector modify(final BitVector value) {
      return value.getBitSize() == bitSize ? value : value.resize(bitSize, signExtend);
    }
  }

  private static class Nested implements ValueModifier {
    private final ValueModifier inner;
    private final ValueModifier outer;

    public Nested(final ValueModifier inner, final ValueModifier outer) {
      InvariantChecks.checkNotNull(inner);
      InvariantChecks.checkNotNull(outer);

      this.inner = inner;
      this.outer = outer;
    }

    @Override
    public BitVector modify(final BitVector value) {
      return outer.modify(inner.modify(value));
    }
  }

  private static final ValueModifier UNMODIFIED = new ValueModifier() {
    @Override
    public BitVector modify(final BitVector value) {
      return value;
    }
  };

  private static ValueModifier newNested(final ValueModifier inner, final ValueModifier outer) {
    return inner == UNMODIFIED ? outer : new Nested(inner, outer);
  }

  private static final LazyData DATA = new LazyData();
  static {
    DATA.setValue(BitVector.valueOf(0, 1));
  }

  private static class LazyAddress extends LazyValue {
    @Override
    public LazyValue newCopy() {
      publishSharedCopy(this, this);
      return this;
    }

    @Override
    public Value copy() {
      return newCopy();
    }

    protected LazyAddress() {
      super(DATA);
    }
  }

  public static final LazyValue ADDRESS = new LazyAddress();

  private final LazyData data;
  private final ValueModifier modifier;

  private LazyValue(final LazyData data, final ValueModifier modifier) {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkNotNull(modifier);

    this.data = data;
    this.modifier = modifier;
  }

  protected LazyValue(final LazyData data) {
    this(data, UNMODIFIED);
  }

  protected LazyValue(final LazyData data, final int start, final int end) {
    this(data, new Field(start, end));
  }

  protected LazyValue(final LazyValue other) {
    super(other);

    this.data = new LazyData(other.data);
    this.modifier = other.modifier;
  }

  public LazyValue signExtend( final int bitSize) {
    InvariantChecks.checkNotNull(bitSize);

    final ValueModifier newModifier = newNested(modifier, new Extended(bitSize, true));
    return new LazyValue(data, newModifier);
  }

  public LazyValue zeroExtend(final int bitSize) {
    InvariantChecks.checkNotNull(bitSize);

    final ValueModifier newModifier = newNested(modifier, new Extended(bitSize, false));
    return new LazyValue(data, newModifier);
  }

  @Override
  public LazyValue newCopy() {
    return new LazyValue(this);
  }

  @Override
  public Value copy() {
    return newCopy();
  }

  public BitVector asBitVector() {
    final BitVector value = data.getValue();
    InvariantChecks.checkNotNull(value, "LazyData does not have a value.");
    return modifier.modify(value);
  }

  @Override
  public BigInteger getValue() {
    return asBitVector().bigIntegerValue(false);
  }

  @Override
  public String toString() {
    return null != data.getValue() ? getValue().toString() : "LazyValue";
  }
}
