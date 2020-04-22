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

package ru.ispras.microtesk.utils;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class BigIntegerUtils {
  private BigIntegerUtils() {}

  private static final BigInteger TWO_POWER_64 = BigInteger.ONE.shiftLeft(64);

  public static BigInteger asUnsigned(final long value) {
    if (value >= 0) {
      return BigInteger.valueOf(value);
    }

    return BigInteger.valueOf(value).add(TWO_POWER_64);
  }

  public static Collection<BigInteger> asUnsigned(final Collection<Long> values) {
    InvariantChecks.checkNotNull(values);

    final Collection<BigInteger> result = new ArrayList<>(values.size());

    for (final long value : values) {
      result.add(BigIntegerUtils.asUnsigned(value));
    }

    return result;
  }

  public static Collection<Integer> toIntCollection(final Collection<BigInteger> values) {
    InvariantChecks.checkNotNull(values);

    final Collection<Integer> result = new ArrayList<>(values.size());

    for (final BigInteger value : values) {
      result.add(value.intValue());
    }

    return result;
  }

  public static List<Integer> toIntList(final Collection<BigInteger> values) {
    return new ArrayList<>(toIntCollection(values));
  }

  public static Set<Integer> toIntSet(final Collection<BigInteger> values) {
    return new LinkedHashSet<>(toIntCollection(values));
  }

  public static Collection<Long> toLongCollection(final Collection<BigInteger> values) {
    InvariantChecks.checkNotNull(values);

    final Collection<Long> result = new ArrayList<>(values.size());

    for (final BigInteger value : values) {
      result.add(value.longValue());
    }

    return result;
  }

  public static List<Long> toLongList(final Collection<BigInteger> values) {
    return new ArrayList<>(toLongCollection(values));
  }

  public static Set<Long> toLongSet(final Collection<BigInteger> values) {
    return new LinkedHashSet<>(toLongCollection(values));
  }

  public static Collection<BitVector> toBvCollection(
      final Collection<BigInteger> values, final int bitSize) {
    InvariantChecks.checkNotNull(values);

    final Collection<BitVector> result = new ArrayList<>(values.size());

    for (final BigInteger value : values) {
      result.add(BitVector.valueOf(value, bitSize));
    }

    return result;
  }

  public static List<BitVector> toBvList(final Collection<BigInteger> values, final int bitSize) {
    return new ArrayList<>(toBvCollection(values, bitSize));
  }

  public static Set<BitVector> toBvSet(
      final Collection<BigInteger> values, final int bitSize) {
    return new LinkedHashSet<>(toBvCollection(values, bitSize));
  }
}
