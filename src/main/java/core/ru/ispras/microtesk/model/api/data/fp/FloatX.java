/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.data.fp;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

public final class FloatX extends Number implements Comparable<FloatX> {
  private static final long serialVersionUID = 2006185347947148830L;

  private static final int FLOAT_FRACTION_SIZE = 23;
  private static final int DOUBLE_FRACTION_SIZE = 52;

  private final BitVector data;
  private final int fractionSize;
  private final int exponentSize;

  public FloatX(final BitVector data, final int fractionSize, final int exponentSize) {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkGreaterThanZero(exponentSize);
    InvariantChecks.checkGreaterThanZero(fractionSize);

    // 1 is added to make room for implicit sign bit
    final int expectedBitSize = fractionSize + exponentSize + 1;
    InvariantChecks.checkTrue(data.getBitSize() == expectedBitSize, "Not IEEE 754 format!");

    this.data = BitVector.unmodifiable(data);
    this.fractionSize = fractionSize;
    this.exponentSize = exponentSize;
  }

  public FloatX(final int fractionSize, final int exponentSize) {
    // 1 is added to make room for implicit sign bit
    this(
        BitVector.newEmpty(fractionSize + exponentSize + 1),
        fractionSize,
        exponentSize
    );
  }

  public FloatX(final float floatData) {
    this(
        BitVector.valueOf(Float.floatToIntBits(floatData), Float.SIZE),
        FLOAT_FRACTION_SIZE,
        Float.SIZE - FLOAT_FRACTION_SIZE - 1 // Minus sign bit
    );
  }

  public FloatX(final double doubleData) {
    this(
        BitVector.valueOf(Double.doubleToLongBits(doubleData), Double.SIZE),
        DOUBLE_FRACTION_SIZE,
        Double.SIZE - DOUBLE_FRACTION_SIZE - 1 // Minus sign bit 
    );
  }

  public BitVector getData() {
    return data;
  }

  public int getSize() {
    return data.getBitSize();
  }

  public int getExponentSize() {
    return exponentSize;
  }

  public int getFractionSize() {
    return fractionSize;
  }

  public boolean isSingle() {
    return getSize() == Float.SIZE && getFractionSize() == FLOAT_FRACTION_SIZE;
  }

  public boolean isDouble() {
    return getSize() == Double.SIZE && getFractionSize() == DOUBLE_FRACTION_SIZE;
  }

  @Override
  public int compareTo(final FloatX other) {
    InvariantChecks.checkNotNull(other);

    if (this.equals(other)){
      return 0;
    }

    if (this.isSingle() && other.isSingle()) {
      return Float.compare(floatValue(), other.floatValue());
    }

    if (this.isDouble() && other.isDouble()) {
      return Double.compare(doubleValue(), other.doubleValue());
    }

    return this.data.compareTo(other.data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + data.hashCode();
    result = prime * result + fractionSize;
    result = prime * result + exponentSize;

    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final FloatX other = (FloatX) obj;
    if (!data.equals(other.data)) {
      return false;
    }

    if (exponentSize != other.exponentSize) {
      return false;
    }

    if (fractionSize != other.fractionSize) {
      return false;
    }

    return true;
  }

  @Override
  public float floatValue() {
    if (isSingle()) {
      return Float.intBitsToFloat(data.intValue());
    }

    throw new IllegalStateException(String.format(
      "The %s type is not a IEEE 754 single.", getTypeName()));
  }

  @Override
  public double doubleValue() {
    if (isDouble()) {
      return Double.longBitsToDouble(data.longValue());
    }

    throw new IllegalStateException(
      String.format("The %s type is not a IEEE 754 double.", getTypeName()));
  }

  @Override
  public int intValue() {
    return data.intValue();
  }

  @Override
  public long longValue() {
    return data.longValue();
  }

  public String getTypeName() {
    return String.format("float(%d, %d)", getFractionSize(), getExponentSize());
  }

  @Override
  public String toString() {
    if (isSingle()) {
      return Float.toString(floatValue());
    }

    if (isDouble()) {
      return Double.toString(doubleValue());
    }

    return data.toString();
  }

  public String toHexString() {
    if (isSingle()) {
      return Float.toHexString(floatValue());
    }

    if (isDouble()) {
      return Double.toHexString(doubleValue());
    }

    return data.toHexString();
  }

  public FloatX neg() {
    if (isSingle()) {
      return new FloatX(-floatValue());
    }

    if (isDouble()) {
      return new FloatX(-doubleValue());
    }

    throw new UnsupportedOperationException(String.format(
        "Not supported for argument type: %s.", getTypeName()));
  }

  public FloatX add(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);

    if (isSingle() && arg.isSingle()) {
      return new FloatX(floatValue() + arg.floatValue());
    }

    if (isDouble() && arg.isDouble()) {
      return new FloatX(doubleValue() + arg.doubleValue());
    }

    return raiseNotSupported(getTypeName(), arg.getTypeName());
  }

  public FloatX sub(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);

    if (isSingle() && arg.isSingle()) {
      return new FloatX(floatValue() - arg.floatValue());
    }

    if (isDouble() && arg.isDouble()) {
      return new FloatX(doubleValue() - arg.doubleValue());
    }

    return raiseNotSupported(getTypeName(), arg.getTypeName());
  }

  public FloatX mul(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);

    if (isSingle() && arg.isSingle()) {
      return new FloatX(floatValue() * arg.floatValue());
    }

    if (isDouble() && arg.isDouble()) {
      return new FloatX(doubleValue() * arg.doubleValue());
    }

    return raiseNotSupported(getTypeName(), arg.getTypeName());
  }

  public FloatX div(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);

    if (isSingle() && arg.isSingle()) {
      return new FloatX(floatValue() / arg.floatValue());
    }

    if (isDouble() && arg.isDouble()) {
      return new FloatX(doubleValue() / arg.doubleValue());
    }

    return raiseNotSupported(getTypeName(), arg.getTypeName());
  }

  private static FloatX raiseNotSupported(final String argType1, final String argType2) {
    throw new UnsupportedOperationException(String.format(
        "Not supported for argument types: %s and %s.", argType1, argType2));
  }
}
