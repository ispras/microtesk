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

  private Operations getOperations() {
    return precision.getOperations();
  }

  public BitVector getData() {
    return data;
  }

  public int getSize() {
    return data.getBitSize();
  }

  public Precision getPrecision() {
    return precision;
  }

  @Override
  public int compareTo(final FloatX other) {
    InvariantChecks.checkNotNull(other);
    checkPrecision(precision, other.precision);

    return getOperations().compare(this, other);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + data.hashCode();
    result = prime * result + precision.getFractionSize();
    result = prime * result + precision.getExponentSize();

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
    if (precision.equals(Precision.FLOAT32)) {
      return Float.intBitsToFloat(data.intValue());
    }

    throw new IllegalStateException(String.format(
        "The %s type is not a IEEE 754 single.", precision.getText()));
  }

  @Override
  public double doubleValue() {
    if (precision.equals(Precision.FLOAT64)) {
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
    return getOperations().toString(this);
  }

  public String toHexString() {
    return getOperations().toHexString(this);
  }

  public FloatX neg() {
    final int signBitIndex = getSize() - 1;

    final BitVector newData = data.copy();
    newData.setBit(signBitIndex, !data.getBit(signBitIndex));

    return new FloatX(newData, precision);
  }

  public FloatX add(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkPrecision(precision, arg.precision);

    return getOperations().add(this, arg);
  }

  public FloatX sub(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkPrecision(precision, arg.precision);

    return getOperations().sub(this, arg);
  }

  public FloatX mul(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkPrecision(precision, arg.precision);

    return getOperations().mul(this, arg);
  }

  public FloatX div(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkPrecision(precision, arg.precision);

    return getOperations().div(this, arg);
  }

  public FloatX mod(final FloatX arg) {
    InvariantChecks.checkNotNull(arg);
    checkPrecision(precision, arg.precision);

    return getOperations().rem(this, arg);
  }

  public FloatX sqrt() {
    return getOperations().sqrt(this);
  }

  public FloatX toFloat(final Precision newPrecision) {
    InvariantChecks.checkNotNull(newPrecision);

    if (precision.equals(newPrecision)) {
      return this;
    }

    return getOperations().toFloat(this, newPrecision);
  }

  public BitVector toInteger() {
    return toInteger(getSize());
  }

  public BitVector toInteger(final int newSize) {
    InvariantChecks.checkGreaterThanZero(newSize);
    return getOperations().toInteger(this, newSize);
  }

  public static FloatX fromInteger(final Precision precision, final BitVector value) {
    InvariantChecks.checkNotNull(precision);
    InvariantChecks.checkNotNull(value);
    return precision.getOperations().fromInteger(value);
  }

  public static FloatX fromInteger(
      final int fractionSize, final int exponentSize, final BitVector value) {
    final Precision precision = getPrecision(fractionSize, exponentSize);
    return fromInteger(precision, value);
  }

  private static void checkPrecision(final Precision lhs, final Precision rhs) {
    if (!lhs.equals(rhs)) {
      throw new IllegalArgumentException(String.format(
          "Both arguments must have the same precision: %s and %s",
          lhs.getText(),
          rhs.getText())
      );
    }
  }
}
