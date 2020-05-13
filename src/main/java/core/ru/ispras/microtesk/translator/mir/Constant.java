package ru.ispras.microtesk.translator.mir;

import java.math.BigInteger;

public class Constant implements Operand {
  private final int bits;
  private final BigInteger value;

  private Constant(final int bits, final BigInteger value) {
    this.bits = bits;
    this.value = value;
  }

  public static Constant valueOf(int size, long value) {
    validateBitSize(size);

    if (value == 0) {
      return zero(size);
    } else if (size == 1) {
      return bitOf(value);
    } else {
      return new Constant(size, BigInteger.valueOf(value));
    }
  }

  public static Constant valueOf(int size, BigInteger value) {
    validateBitSize(size);
    validateBitLength(size, value);

    if (BigInteger.ZERO.equals(value)) {
      return zero(size);
    } else if (size == 1) {
      return bitOf(value.signum());
    } else {
      return new Constant(size, value);
    }
  }

  public static Constant bitOf(long value) {
    if (value == 0) {
      return BITS[0];
    }
    return BITS[1];
  }

  private static final Constant[] BITS = {
    new Constant(1, BigInteger.ZERO),
    new Constant(1, BigInteger.ONE)
  };

  public static Constant zero(int bits) {
    validateBitSize(bits);

    if (bits < ZERO.length) {
      Constant value = ZERO[bits - 1];
      if (value == null) {
        value = new Constant(bits, BigInteger.ZERO);
        ZERO[bits - 1] = value;
      }
      return value;
    }
    return new Constant(bits, BigInteger.ZERO);
  }

  private static final Constant[] ZERO = new Constant[64];
  static {
    ZERO[0] = BITS[0];
  }

  public static Constant ones(int size) {
    return ones(size, size);
  }

  public static Constant ones(int size, int n) {
    validateBitSize(size);
    return valueOf(size, BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE));
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

  private static void validateBitSize(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Bit size must be positive number");
    }
  }

  private static void validateBitLength(final int bits, final BigInteger value) {
    final int minbits = value.bitLength() + ((value.signum() == -1) ? 1 : 0);
    if (minbits > bits) {
      throw new IllegalArgumentException(
          String.format("%d bits insufficent to store value %s, %d bits required",
            bits, value.toString(16), minbits));
    }
  }
}
