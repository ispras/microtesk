package ru.ispras.microtesk.translator.mir;

import java.math.BigInteger;

public class Constant implements Operand {
  private final int bits;
  private final BigInteger value;

  public Constant(final int bits, final long value) {
    this(bits, BigInteger.valueOf(value));
  }

  public Constant(final int bits, final BigInteger value) {
    final int minbits = value.bitLength() + ((value.signum() == -1) ? 1 : 0);
    if (minbits > bits) {
      throw new IllegalArgumentException();
    }
    this.bits = bits;
    this.value = value;
  }

  public BigInteger getValue() {
    return value;
  }

  @Override
  public MirTy getType() {
    return new IntTy(bits);
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Constant) {
      final Constant that = (Constant) o;
      return this.value.equals(that.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return value.hashCode() * 31 + bits;
  }
}
