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

package ru.ispras.microtesk.model.api.data;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.fortress.util.InvariantChecks;

public final class OperationsInteger implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new OperationsInteger();
    }
    return instance;
  }

  private OperationsInteger() {}

  @Override
  public final Data negate(final Data arg) {
    final BitVector result = BitVectorMath.neg(arg.getRawData());
    return new Data(arg.getType(), result);
  }

  @Override
  public final Data add(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs); 
    return newData(signed, BitVectorMath.add(lhs.getRawData(), rhs.getRawData()));
  }

  @Override
  public Data subtract(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs); 
    return newData(signed, BitVectorMath.sub(lhs.getRawData(), rhs.getRawData()));
  }

  @Override
  public Data multiply(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs); 
    return newData(signed, BitVectorMath.mul(lhs.getRawData(), rhs.getRawData()));
  }

  @Override
  public Data divide(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs);

    final BitVector result = signed ?
        BitVectorMath.sdiv(lhs.getRawData(), rhs.getRawData()) :
        BitVectorMath.udiv(lhs.getRawData(), rhs.getRawData());

    return newData(signed, result);
  }

  @Override
  public Data mod(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs);

    final BitVector result = signed ?
        BitVectorMath.srem(lhs.getRawData(), rhs.getRawData()) :
        BitVectorMath.urem(lhs.getRawData(), rhs.getRawData());

    return newData(signed, result);
  }

  @Override
  public Data pow(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs);
    final BigInteger value = lhs.getRawData().bigIntegerValue(signed);

    final int exponent =
        rhs.getRawData().bigIntegerValue(false).intValue();

    final BigInteger result = value.pow(exponent);
    return newData(signed, BitVector.valueOf(result, lhs.getType().getBitSize()));
  }

  @Override
  public final Data not(final Data arg) {
    final BitVector result = BitVectorMath.not(arg.getRawData());
    return new Data(arg.getType(), result);
  }

  @Override
  public Data and(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs);
    return newData(signed, BitVectorMath.and(lhs.getRawData(), rhs.getRawData()));
  }

  @Override
  public Data or(Data lhs, Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs);
    return newData(signed, BitVectorMath.or(lhs.getRawData(), rhs.getRawData()));
  }

  @Override
  public Data xor(final Data lhs, final Data rhs) {
    final boolean signed = isSigned(lhs) && isSigned(rhs);
    return newData(signed, BitVectorMath.xor(lhs.getRawData(), rhs.getRawData()));
  }

  @Override
  public Data shiftLeft(final Data value, final Data amount) {
    final BigInteger shiftAmount = amount.getRawData().bigIntegerValue(isSigned(amount));
    return newData(isSigned(value), BitVectorMath.shl(value.getRawData(), shiftAmount));
  }

  @Override
  public Data shiftRight(final Data value, final Data amount) {
    final boolean signed = isSigned(value);
    final BigInteger shiftAmount = amount.getRawData().bigIntegerValue(isSigned(amount));

    final BitVector result = signed ?
        BitVectorMath.ashr(value.getRawData(), shiftAmount) :
        BitVectorMath.lshr(value.getRawData(), shiftAmount);

    return newData(signed, result);
  }

  @Override
  public Data rotateLeft(final Data value, final Data amount) {
    final BigInteger rotateAmount = amount.getRawData().bigIntegerValue(isSigned(amount));
    return newData(isSigned(value), BitVectorMath.rotl(value.getRawData(), rotateAmount));
  }

  @Override
  public Data rotateRight(final Data value, final Data amount) {
    final BigInteger rotateAmount = amount.getRawData().bigIntegerValue(isSigned(amount));
    return newData(isSigned(value), BitVectorMath.rotr(value.getRawData(), rotateAmount));
  }

  @Override
  public int compare(final Data lhs, final Data rhs) {
    if (lhs.getRawData().equals(rhs.getRawData())) {
      return 0;
    }

    final boolean signed = isSigned(lhs) && isSigned(rhs);
    final BitVector greater = signed ?
        BitVectorMath.sgt(lhs.getRawData(), rhs.getRawData()) :
        BitVectorMath.ugt(lhs.getRawData(), rhs.getRawData());

    return greater.isAllReset() ? -1 : 1;
  }

  @Override
  public String toString(final Data arg) {
    final boolean signed = isSigned(arg);
    return arg.bigIntegerValue(signed).toString();
  }

  @Override
  public final String toHexString(final Data arg) {
    return arg.getRawData().toHexString();
  }

  private static boolean isSigned(final Data data) {
    InvariantChecks.checkTrue(data.getType().isInteger());
    return data.isType(TypeId.INT);
  }

  private static Data newData(final boolean signed, final BitVector value) {
    final int bitSize = value.getBitSize();
    final Type type = signed ? Type.INT(bitSize) : Type.CARD(bitSize);
    return new Data(type, value);
  }
}
