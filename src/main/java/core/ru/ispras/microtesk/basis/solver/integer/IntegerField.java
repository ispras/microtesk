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

package ru.ispras.microtesk.basis.solver.integer;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerField} class represents an integer field.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerField {
  public static Map<IntegerField, BigInteger> split(
      final List<IntegerField> cat, final BigInteger value) {
    InvariantChecks.checkNotNull(cat);
    InvariantChecks.checkNotNull(value);

    final Map<IntegerField, BigInteger> result = new LinkedHashMap<>();

    int offset = 0;
    for (final IntegerField field : cat) {
      final int lo = offset;
      final int hi = offset + (field.getWidth() - 1);

      final BigInteger fieldValue = BitUtils.getField(value, lo, hi);

      result.put(field, fieldValue);
      offset += field.getWidth();
    }

    return result;
  }

  public static IntegerField create(final IntegerVariable variable, final BigInteger mask) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(mask);
    InvariantChecks.checkTrue(mask.compareTo(BigInteger.ZERO) > 0);

    final int lo = mask.getLowestSetBit();
    final int hi = mask.bitLength() - 1;

    return new IntegerField(variable, lo, hi);
  }

  /** The variable. */
  private final IntegerVariable var;
  /** The lower bit index. */
  private final int lo;
  /** The upper bit index. */
  private final int hi;

  /**
   * Constructs an integer field.
   * 
   * @param var the variable whose bits to be selected.
   * @param lo the lower bit index.
   * @param hi the upper bit index.
   * @throws IllegalArgumentException if {@code var} is null.
   */
  public IntegerField(final IntegerVariable var, int lo, int hi) {
    InvariantChecks.checkNotNull(var);

    this.var = var;
    this.lo = lo;
    this.hi = hi;
  }

  /**
   * Constructs an integer field.
   * 
   * @param var the variable whose bits to be selected.
   * @param bit the bit index.
   */
  public IntegerField(final IntegerVariable var, int bit) {
    this(var, bit, bit);
  }

  /**
   * Constructs an integer field.
   * 
   * @param var the variable.
   */
  public IntegerField(final IntegerVariable var) {
    this(var, 0, var.getWidth() - 1);
  }

  /**
   * Returns the variable of the integer field.
   * 
   * @return the variable.
   */
  public IntegerVariable getVariable() {
    return var;
  }

  /**
   * Returns the lower bit index of the integer field.
   * 
   * @return the lower bit index.
   */
  public int getLoIndex() {
    return lo;
  }

  /**
   * Returns the upper bit index of the integer field.
   * 
   * @return the upper bit index.
   */
  public int getHiIndex() {
    return hi;
  }

  /**
   * Returns the size (bit width) of the integer field.
   * 
   * @return the size of the integer field.
   */
  public int getWidth() {
    return (getHiIndex() - getLoIndex()) + 1;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof IntegerField)) {
      return false;
    }

    final IntegerField r = (IntegerField) o;
    return var.equals(r.var) && hi == r.hi && lo == r.lo;
  }

  @Override
  public int hashCode() {
    return var.hashCode() ^ lo ^ (hi << 16);
  }

  @Override
  public String toString() {
    if (lo == 0 && hi == var.getWidth() - 1) {
      return var.getName();
    }

    if (lo == hi) {
      return String.format("%s[%d]", var.getName(), lo);
    }

    return String.format("%s[%d:%d]", var.getName(), lo, hi);
  }
}
