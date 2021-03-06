/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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
import java.util.Map;
import java.util.TreeMap;

public class SparseArray<T> {
  private final Map<BigInteger, T> indexMap;
  private final BigInteger arrayLength;

  public SparseArray(final BigInteger length) {
    InvariantChecks.checkNotNull(length);
    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);

    this.indexMap = new TreeMap<>();
    this.arrayLength = length;
  }

  public BigInteger length() {
    return arrayLength;
  }

  public T get(final BitVector index) {
    InvariantChecks.checkNotNull(index);

    final BigInteger indexValue = index.bigIntegerValue(false);
    InvariantChecks.checkGreaterThan(arrayLength, indexValue);

    return indexMap.get(indexValue);
  }

  public T set(final BitVector index, final T value) {
    InvariantChecks.checkNotNull(index);
    InvariantChecks.checkNotNull(value);

    final BigInteger indexValue = index.bigIntegerValue(false);
    InvariantChecks.checkGreaterThan(arrayLength, indexValue);

    return indexMap.put(indexValue, value);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('[');

    for (final Map.Entry<BigInteger, T> e : indexMap.entrySet()) {
      sb.append(System.lineSeparator());
      sb.append(String.format("%d: %s", e.getKey(), e.getValue()));
    }

    sb.append(System.lineSeparator());
    sb.append(']');

    return sb.toString();
  }
}
