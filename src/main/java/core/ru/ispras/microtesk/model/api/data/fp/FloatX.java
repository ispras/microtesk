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

  private final BitVector data;
  private final Precision precision;

  FloatX(final BitVector data, final Precision precision) {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkNotNull(precision);
    InvariantChecks.checkTrue(data.getBitSize() == precision.getSize());

    this.data = BitVector.unmodifiable(data);
    this.precision = precision;
  }

  public FloatX(final BitVector data, final int fractionSize, final int exponentSize) {
    this(
        data,
        getPrecision(fractionSize, exponentSize)
    );
  }

  public FloatX(final int fractionSize, final int exponentSize) {
    this(
        BitVector.newEmpty(fractionSize + exponentSize),
        fractionSize,
        exponentSize
    );
  }

  private static Precision getPrecision(final int fractionSize, final int exponentSize) {
    final Precision result = Precision.find(fractionSize, exponentSize);
    if (result != null) {
      return result;
    }

    throw new IllegalStateException(String.format(
        "Unsupported floating-point format: %d bits (sign=1, fraction=%d, exponent=%d)",
        fractionSize + exponentSize + 1, // plus implicit sign bit
        fractionSize,
        exponentSize
        )
    );
  }

  public BitVector getData() {
    return data;
  }

  public int getSize() {
    return data.getBitSize();
  }

  public int getExponentSize() {
    return precision.getExponentSize();
  }

  public int getFractionSize() {
    return precision.getFractionSize();
  }

  public boolean isSingle() {
    return precision.equals(Precision.FLOAT32);
  }

  public boolean isDouble() {
    return precision.equals(Precision.FLOAT64);
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
    result = prime * result + getFractionSize();
    result = prime * result + getExponentSize();

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
    if (!precision.equals(other.precision)) {
      return false;
    }

    if (!data.equals(other.data)) {
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
        "The %s type is not a IEEE 754 single.", precision.getText()));
  }

  @Override
  public double doubleValue() {
    if (isDouble()) {
      return Double.longBitsToDouble(data.longValue());
    }

    throw new IllegalStateException(String.format(
        "The %s type is not a IEEE 754 double.", precision.getText()));
  }

  @Override
  public int intValue() {
    return data.intValue();
  }

  @Override
  public long longValue() {
    return data.longValue();
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
    final int signBitIndex = getSize() - 1;

    final BitVector newData = data.copy();
    newData.setBit(signBitIndex, !data.getBit(signBitIndex));

    return new FloatX(newData, precision);
  }

  public FloatX add(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkSamePrecision(precision, arg.precision);

    return precision.getOperations().add(this, arg);
  }

  public FloatX sub(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkSamePrecision(precision, arg.precision);

    return precision.getOperations().sub(this, arg);
  }

  public FloatX mul(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkSamePrecision(precision, arg.precision);

    return precision.getOperations().mul(this, arg);
  }

  public FloatX div(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkSamePrecision(precision, arg.precision);

    return precision.getOperations().div(this, arg);
  }

  public FloatX mod(FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkSamePrecision(precision, arg.precision);

    return precision.getOperations().rem(this, arg);
  }

  private static void checkSamePrecision(
      final Precision lhsPrecision,
      final Precision rhsPrecision) {
    if (!lhsPrecision.equals(rhsPrecision)) {
      throw new IllegalArgumentException(String.format(
          "Both arguments must have the same precision: %s and %s",
          lhsPrecision.getText(),
          rhsPrecision.getText()));
    }
  }
}
