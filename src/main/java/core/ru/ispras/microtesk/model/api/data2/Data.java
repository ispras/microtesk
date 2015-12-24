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

package ru.ispras.microtesk.model.api.data2;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.fp.FloatX;

public final class Data implements Comparable<Data> {
  private final Type type;
  private final BitVector rawData;

  public static Data valueOf(final Type type, final BigInteger value) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(value);
    return new Data(type, BitVector.valueOf(value, type.getBitSize()));
  }

  public static Data valueOf(final Type type, final long value) {
    InvariantChecks.checkNotNull(type);
    return new Data(type, BitVector.valueOf(value, type.getBitSize()));
  }

  public static Data valueOf(final Type type, final int value) {
    InvariantChecks.checkNotNull(type);
    return new Data(type, BitVector.valueOf(value, type.getBitSize()));
  }

  public Data(final Type type, final BitVector rawData) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(rawData);
    InvariantChecks.checkTrue(rawData.getBitSize() == type.getBitSize());

    this.type = type;
    this.rawData = rawData;
  }

  public Data(final Data other) {
    InvariantChecks.checkNotNull(other);

    this.type = other.getType();
    this.rawData = other.getRawData().copy();
  }

  public Data(final Type type) {
    InvariantChecks.checkNotNull(type);

    this.type = type;
    this.rawData = BitVector.newEmpty(type.getBitSize());
  }

  public Type getType() {
    return type;
  }

  public boolean isType(final TypeId typeId) {
    return type.getTypeId() == typeId;
  }

  public BitVector getRawData() {
    return rawData;
  }

  public Data signExtendTo(final Type newType) {
    InvariantChecks.checkNotNull(newType);
    InvariantChecks.checkTrue(type.isInteger() && newType.isInteger());

    if (type.equals(newType)) {
      return this;
    }

    InvariantChecks.checkTrue(newType.getBitSize() >= type.getBitSize());
    final BitVector newRawData = rawData.resize(newType.getBitSize(), true);

    return new Data(newType, newRawData);
  }

  public Data zeroExtendTo(final Type newType) {
    InvariantChecks.checkNotNull(newType);
    InvariantChecks.checkTrue(type.isInteger() && newType.isInteger());

    if (type.equals(newType)) {
      return this;
    }

    InvariantChecks.checkTrue(newType.getBitSize() >= type.getBitSize());
    final BitVector newRawData = rawData.resize(newType.getBitSize(), false);

    return new Data(newType, newRawData);
  }

  public Data coerceTo(final Type newType) {
    InvariantChecks.checkNotNull(newType);
    InvariantChecks.checkTrue(type.isInteger() && newType.isInteger());

    if (type.equals(newType)) {
      return this;
    }

    // Sign extension applies only to signed integer values (TypeId.INT).
    final boolean signExt = isType(TypeId.INT);

    final BitVector newRawData = rawData.resize(newType.getBitSize(), signExt);
    return new Data(newType, newRawData);
  }

  public Data castTo(final Type newType) {
    InvariantChecks.checkNotNull(newType);

    if (type.equals(newType)) {
      return this;
    }

    InvariantChecks.checkTrue(type.getBitSize() == newType.getBitSize());
    return new Data(newType, rawData);
  }

  public Data negate() {
    return getOperations().negate(this);
  }

  public Data add(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().add(this, other);
  }

  public Data subtract(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().subtract(this, other);
  }

  public Data multiply(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().multiply(this, other);
  }

  public Data divide(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().divide(this, other);
  }

  public Data mod(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().mod(this, other);
  }

  public Data pow(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().pow(this, other);
  }

  public Data not() {
    return getOperations().not(this);
  }

  public Data and(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().and(this, other);
  }

  public Data or(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().or(this, other);
  }

  public Data xor(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().xor(this, other);
  }

  public Data shiftLeft(final Data amount) {
    InvariantChecks.checkNotNull(amount);
    InvariantChecks.checkTrue(amount.getType().isInteger());

    return getOperations().shiftLeft(this, amount);
  }

  public Data shiftRight(final Data amount) {
    InvariantChecks.checkNotNull(amount);
    InvariantChecks.checkTrue(amount.getType().isInteger());

    return getOperations().shiftRight(this, amount);
  }

  public Data rotateLeft(final Data amount) {
    InvariantChecks.checkNotNull(amount);
    InvariantChecks.checkTrue(amount.getType().isInteger());

    return getOperations().rotateLeft(this, amount);
  }

  public Data rotateRight(final Data amount) {
    InvariantChecks.checkNotNull(amount);
    InvariantChecks.checkTrue(amount.getType().isInteger());

    return getOperations().rotateRight(this, amount);
  }

  @Override
  public int compareTo(final Data other) {
    InvariantChecks.checkNotNull(other);
    InvariantChecks.checkTrue(this.type.equals(other.type));

    return getOperations().compare(this, other);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + type.hashCode();
    result = prime * result + rawData.hashCode();

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

    final Data other = (Data) obj;
    if (!type.equals(other.type)) {
      return false;
    }

    return rawData.equals(other.rawData);
  }

  public BigInteger bigIntegerValue(final boolean signed) {
    return rawData.bigIntegerValue(signed);
  }

  public boolean booleanValue() {
    return !rawData.isAllReset();
  }

  public FloatX floatXValue() {
    InvariantChecks.checkTrue(type.getTypeId() == TypeId.FLOAT, "Not a float value!");
    return new FloatX(rawData, type.getFieldSize(0), type.getFieldSize(1));
  }

  @Override
  public String toString() {
    return getOperations().toString(this);
  }

  public String toHexString() {
    return getOperations().toHexString(this);
  }

  public String toBinString() {
    return rawData.toBinString();
  }

  private Operations getOperations() {
    return type.getTypeId().getOperations();
  }
}
