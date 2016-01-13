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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

public final class BigIntegerUtils {
  private BigIntegerUtils() {}

  private static final BigInteger TWO_POWER_64 = BigInteger.ONE.shiftLeft(64);

  public static BigInteger valueOfUnsignedLong(final long value) {
    if (value >= 0) {
      return BigInteger.valueOf(value);
    }

    return BigInteger.valueOf(value).add(TWO_POWER_64);
  }

  public static Collection<BigInteger> valuesOfUnsignedLongs(final Collection<Long> values) {
    InvariantChecks.checkNotNull(values);

    final Collection<BigInteger> result = new ArrayList<>(values.size());

    for (final long value : values) {
      result.add(BigIntegerUtils.valueOfUnsignedLong(value));
    }

    return result;
  }

  public static Collection<Integer> toIntegerCollection(final Collection<BigInteger> values) {
    InvariantChecks.checkNotNull(values);

    final Collection<Integer> result = new ArrayList<>(values.size());

    for (final BigInteger value : values) {
      result.add(value.intValue());
    }

    return result;
  }

  public static List<Integer> toIntegerList(final Collection<BigInteger> values) {
    return new ArrayList<>(toIntegerCollection(values));
  }

  public static Set<Integer> toIntegerSet(final Collection<BigInteger> values) {
    return new LinkedHashSet<>(toIntegerCollection(values));
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
}
