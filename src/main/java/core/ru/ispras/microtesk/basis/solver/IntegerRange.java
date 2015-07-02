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

package ru.ispras.microtesk.basis.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class represents a non-empty integer range (interval).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerRange {

  /** The lower bound of the range. */
  private BigInteger min;
  /** The upper bound of the range. */
  private BigInteger max;

  /**
   * This enumeration contains types of range bounds.
   */
  public static enum RangePointType {
    /** Start point of a range. */
    MIN,
    /** End point of of a range. */
    MAX,
    /** Start/end point of of a range. */
    ALL
  }

  /**
   * Transforms the list of ranges to the list of disjoint ranges.
   * 
   * @param ranges the set of ranges.
   * @return the list of disjoint ranges.
   * @throws NullPointerException if {@code ranges} is null.
   */
  public static List<IntegerRange> divide(final Set<IntegerRange> ranges) {
    InvariantChecks.checkNotNull(ranges);

    if (ranges.isEmpty()) {
      return new ArrayList<>();
    }

    // Create a line of the range points.
    final Map<BigInteger, RangePointType> line = new HashMap<>();

    for (final IntegerRange range : ranges) {
      // Add the starting point of the range into the line.
      if (line.containsKey(range.getMin())) {
        RangePointType type = line.get(range.getMin());
        if (type.equals(RangePointType.MAX)) {
          // MIN + MAX = ALL.
          line.put(range.getMin(), RangePointType.ALL);
        }
      } else {
        line.put(range.getMin(), RangePointType.MIN);
      }
      // Add the end point of the range in to the line.
      if (line.containsKey(range.getMax())) {
        RangePointType type = line.get(range.getMax());
        if (type.equals(RangePointType.MIN)) {
          // MAX + MIN = ALL.
          line.put(range.getMax(), RangePointType.ALL);
        }
      } else {
        line.put(range.getMax(), RangePointType.MAX);
      }
    }

    // Divide the line into disjoint ranges.
    final List<IntegerRange> dividedRanges = new ArrayList<>();
    final SortedSet<BigInteger> keys = new TreeSet<BigInteger>(line.keySet());

    BigInteger minValue = keys.first();
    BigInteger startValue = minValue;

    for (final BigInteger key : keys) {
      final RangePointType type = line.get(key);
      switch (type) {
        case ALL:
          if (!key.equals(minValue)) {
            dividedRanges.add(new IntegerRange(minValue, key.subtract(BigInteger.ONE)));
          }
          dividedRanges.add(new IntegerRange(key, key));
          minValue = key.add(BigInteger.ONE);
          break;
        case MAX:
          dividedRanges.add(new IntegerRange(minValue, key));
          minValue = key.add(BigInteger.ONE);
          break;
        case MIN:
          if (!key.equals(startValue) && !minValue.equals(key)) {
            dividedRanges.add(new IntegerRange(minValue, key.subtract(BigInteger.ONE)));
            minValue = key;
          }
          break;
      }
    }

    return dividedRanges;
  }

  /**
   * Constructs a range with the given lower ({@code min}) and upper ({@code max}) bounds.
   * 
   * @param min the lower bound of the range.
   * @param max the upper bound of the range.
   * @throws NullPointerException if {@code min} or {@code max} is null.
   * @throws IllegalArgumentException if ({@code min > max}).
   */
  public IntegerRange(final BigInteger min, final BigInteger max) {
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);
    InvariantChecks.checkGreaterOrEq(max, min);

    this.min = min;
    this.max = max;
  }

  /**
   * Constructs a single-value range.
   * 
   * @param value the only value of the range.
   */
  public IntegerRange(final BigInteger value) {
    this(value, value);
  }

  /**
   * Constructs a range with the given lower ({@code min}) and upper ({@code max}) bounds.
   * 
   * @param min the lower bound of the range.
   * @param max the upper bound of the range.
   */
  public IntegerRange(final long min, final long max) {
    this(BigInteger.valueOf(min), BigInteger.valueOf(max));
  }

  /**
   * Constructs a single-value range.
   * 
   * @param value the only value of the range.
   */
  public IntegerRange(final long value) {
    this(BigInteger.valueOf(value));
  }

  /**
   * Returns the lower bound of the range.
   * 
   * @return the lower bound of the range.
   */
  public BigInteger getMin() {
    return min;
  }

  /**
   * Sets the lower bound of the range.
   * 
   * @param min the lower bound to be set.
   * @throws NullPointerException if {@code min} is null.
   * @throws IllegalArgumentException if ({@code min > max}).
   */
  public void setMin(final BigInteger min) {
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkGreaterOrEq(this.max, min);

    this.min = min;
  }

  /**
   * Returns the upper bound of the range.
   * 
   * @return the upper bound of the range.
   */
  public BigInteger getMax() {
    return max;
  }

  /**
   * Sets the upper bound of the range.
   * 
   * @param max the upper bound to be set.
   * @throws NullPointerException if {@code max} is null.
   * @throws IllegalArgumentException if ({@code min > max}).
   */
  public void setMax(final BigInteger max) {
    InvariantChecks.checkNotNull(max);
    InvariantChecks.checkGreaterOrEq(max, this.min);

    this.max = max;
  }

  /**
   * Returns the size of the range.
   * 
   * @return the size of the range.
   */
  public BigInteger size() {
    return max.subtract(min).add(BigInteger.ONE);
  }

  /**
   * Checks whether the range is singular (consists of a single value: {@code min == max}).
   * 
   * @return {@code true} if the range is singular; {@code false} otherwise.
   */
  public boolean isSingular() {
    return min.compareTo(max) == 0;
  }

  /**
   * Checks whether this range overlaps with the given one ({@code rhs}).
   * 
   * @param rhs the range to be compared with this one.
   * @return {@code true} if this range overlaps with the given one; {@code false} otherwise.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public boolean overlaps(final IntegerRange rhs) {
    InvariantChecks.checkNotNull(rhs);

    return min.compareTo(rhs.max) <= 0 && rhs.min.compareTo(max) <= 0;
  }

  /**
   * Checks whether this range contains (as a subset) the given one ({@code rhs}).
   * 
   * @param rhs the range to be compared with this one.
   * @return {@code true} if this range contains the given one; {@code false} otherwise.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public boolean contains(final IntegerRange rhs) {
    InvariantChecks.checkNotNull(rhs);

    return min.compareTo(rhs.min) <= 0 && max.compareTo(rhs.max) >= 0;
  }

  /**
   * Returns the intersection of this range with the given one. If the ranges are not overlapping,
   * returns {@code null}.
   * 
   * @param rhs the range to be intersected with this one.
   * @return the range representing the intersection or {@code null} if the ranges are disjoint.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public IntegerRange intersect(final IntegerRange rhs) {
    InvariantChecks.checkNotNull(rhs);

    if (!overlaps(rhs)) {
      return null;
    }

    return new IntegerRange(min.max(rhs.min), max.min(rhs.max));
  }

  /**
   * Returns the union of this range with the given one. If the ranges are not overlapping,
   * returns {@code null}.
   * 
   * @param rhs the range to be merged with this one.
   * @return the range representing the union or {@code null} if the ranges are disjoint.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public IntegerRange merge(final IntegerRange rhs) {
    InvariantChecks.checkNotNull(rhs);

    if (!overlaps(rhs)) {
      return null;
    }

    return new IntegerRange(min.min(rhs.min), max.max(rhs.max));
  }

  /**
   * Returns the list of ranges representing the union of this range with the given one.
   * If the ranges are overlapping, the list consists of one range ({@code merge(rhs)});
   * otherwise, it includes two ranges: {@code this} and {@code rhs}.
   * 
   * @param rhs the range to be united with this one.
   * @return the list of ranges representing the union.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public List<IntegerRange> union(final IntegerRange rhs) {
    InvariantChecks.checkNotNull(rhs);

    final List<IntegerRange> result = new ArrayList<IntegerRange>();

    if (overlaps(rhs)) {
      result.add(merge(rhs));
    } else {
      result.add(this);
      result.add(rhs);
    }

    return result;
  }

  /**
   * Returns the list of ranges representing the difference between this range and the given one.
   * If the ranges are not overlapping, the list consists of this range; otherwise it may include
   * up to 2 ranges.
   * 
   * @param rhs the range to be subtracted from this one.
   * @return the difference.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public List<IntegerRange> minus(final IntegerRange rhs) {
    InvariantChecks.checkNotNull(rhs);

    final List<IntegerRange> result = new ArrayList<IntegerRange>();

    if (overlaps(rhs)) {
      final BigInteger min1 = min;
      final BigInteger max1 = rhs.min.subtract(BigInteger.ONE);

      final BigInteger min2 = rhs.max.add(BigInteger.ONE);
      final BigInteger max2 = max;

      if (min1.compareTo(max1) <= 0) {
        result.add(new IntegerRange(min1, max1));
      }

      if (min2.compareTo(max2) <= 0) {
        result.add(new IntegerRange(min2, max2));
      }
    } else {
      result.add(this);
    }

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof IntegerRange)) {
      return false;
    }

    final IntegerRange r = (IntegerRange) o;

    return min.compareTo(r.min) == 0 && max.compareTo(r.max) == 0;
  }

  @Override
  public int hashCode() {
    return 31 * max.hashCode() + min.hashCode();
  }
  
  @Override
  public String toString() {
    return String.format("[%s, %s]", min, max);
  }
}
