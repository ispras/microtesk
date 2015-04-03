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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class represents a finite integer domain (a finite set of integer values). The domain is
 * defined as a set of disjoint ranges {@link IntegerRange}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerDomain {

  /**
   * This class implements an iterator over the domain's values.
   */
  private final class MmuIterator implements Iterator<BigInteger> {
    /** The range index. */
    private int index = 0;
    /** The current value. */
    private BigInteger value = ranges.isEmpty() ? null : ranges.get(0).getMin();

    @Override
    public void init() {
      index = 0;
      value = ranges.isEmpty() ? null : ranges.get(0).getMin();
    }

    @Override
    public boolean hasValue() {
      return value != null;
    }

    @Override
    public BigInteger value() {
      return value;
    }

    @Override
    public void next() {
      if (!hasValue()) {
        return;
      }

      final IntegerRange range = ranges.get(index);

      if (value.compareTo(range.getMax()) < 0) {
        value = value.add(BigInteger.ONE);
      } else {
        index++;
        value = ranges.size() <= index ? null : ranges.get(index).getMin();
      }
    }
  }

  /** The list of ordered disjoint ranges. */
  private List<IntegerRange> ranges = new ArrayList<IntegerRange>();

  /**
   * Constructs the empty domain.
   */
  public IntegerDomain() {
    // Do nothing.
  }

  /**
   * Constructs a single-range domain ({@code [min, max]}).
   * 
   * @param min the lower bound of the domain.
   * @param max the upper bound of the domain.
   * @throws NullPointerException if {@code min} or {@code max} is null.
   */
  public IntegerDomain(final BigInteger min, final BigInteger max) {
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);

    ranges.add(new IntegerRange(min, max));
  }

  /**
   * Constructs a single-value domain.
   * 
   * @param value the only value of the domain.
   * @throws NullPointerException if {@code value} is null.
   */
  public IntegerDomain(final BigInteger value) {
    this(value, value);
  }

  /**
   * Constructs a single-range domain ({@code [0, 2^width - 1]}).
   * 
   * @param width the bit width.
   */
  public IntegerDomain(int width) {
    this(BigInteger.ZERO, BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE));
  }

  /**
   * Constructs a copy of the given domain.
   * 
   * @param rhs the domain to be copied.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public IntegerDomain(final IntegerDomain rhs) {
    InvariantChecks.checkNotNull(rhs);

    ranges.addAll(rhs.ranges);
  }


  /**
   * Returns the size of the domain.
   * 
   * @return the size of the domain.
   */
  public BigInteger size() {
    BigInteger result = BigInteger.ZERO;

    for (final IntegerRange range : ranges) {
      result = result.add(range.size());
    }

    return result;
  }

  /**
   * Checks whether the domain is empty.
   * 
   * @return {@code true} if the domain is empty; {@code false} otherwise.
   */
  public boolean isEmpty() {
    return ranges.isEmpty();
  }

  /**
   * Checks whether the domain is singular (consists of a single value).
   * 
   * @return {@code true} if the domain is singular; {@code false} otherwise.
   */
  public boolean isSingular() {
    return ranges.size() == 1 && ranges.get(0).isSingular();
  }

  /**
   * Checks whether this domain overlaps with the given one ({@code rhs}).
   * 
   * @param rhs the domain to be compared with this one.
   * @return {@code true} if this domain overlaps with the given one; {@code false} otherwise.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public boolean overlaps(final IntegerDomain rhs) {
    InvariantChecks.checkNotNull(rhs);

    int i = 0;
    int j = 0;

    while (i < ranges.size() && j < rhs.ranges.size()) {
      final IntegerRange range1 = ranges.get(i);
      final IntegerRange range2 = rhs.ranges.get(j);

      // The situation {[range1], [range2]}.
      if (range2.getMin().compareTo(range1.getMax()) > 0) {
        i++;
        continue;
      }

      // The situation {[range2], [range1]}.
      if (range1.getMin().compareTo(range2.getMax()) > 0) {
        j++;
        continue;
      }

      // The ranges overlap.
      return true;
    }

    return false;
  }

  /**
   * Checks whether this domain contains (as a subset) the given one ({@code rhs}).
   * 
   * @param rhs the domain to be compared with this one.
   * @return {@code true} if this domain contains the given one; {@code false} otherwise.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public boolean contains(final IntegerDomain rhs) {
    InvariantChecks.checkNotNull(rhs);

    int i = 0;
    int j = 0;

    while (i < ranges.size() && j < rhs.ranges.size()) {
      final IntegerRange range1 = ranges.get(i);
      final IntegerRange range2 = rhs.ranges.get(j);

      // The situation {[range1], [range2]}.
      if (range2.getMin().compareTo(range1.getMax()) > 0) {
        i++;
        // There is a possibility that range1(i+1) contains range2(i). 
        continue;
      }

      // The situation {[range2], [range1]}.
      if (range1.getMin().compareTo(range2.getMax()) > 0) {
        // There is no range among {range1(0), ..., range1(i)} that contains range2.
        return false;
      }

      // The ranges overlap.
      if (!range1.contains(range2)) {
        return false;
      }

      final int compare = range2.getMax().compareTo(range1.getMax());

      // Get the next indices.
      i = compare >= 0 /* max2 >= max1 */ ? i + 1 : i;
      j = compare <= 0 /* max1 >= max2 */ ? j + 1 : j;
    }

    return false;
  }

  /**
   * Initializes the domain.
   * 
   * @param range the range to be used for initialization.
   * @throws NullPointerException if {@code range} is null.
   */
  public void set(final IntegerRange range) {
    InvariantChecks.checkNotNull(range);

    ranges.clear();
    ranges.add(range);
  }

  /**
   * Initializes the domain.
   * 
   * @param domain the domain to be used for initialization.
   * @throws NullPointerException if {@code domain} is null.
   */
  public void set(final IntegerDomain domain) {
    InvariantChecks.checkNotNull(domain);

    ranges.clear();
    ranges.addAll(domain.ranges);
  }

  /**
   * Includes the given range into the domain.
   * 
   * @param range the range to be included to the domain.
   * @throws NullPointerException if {@code range} is null.
   */
  public void include(final IntegerRange range) {
    InvariantChecks.checkNotNull(range);

    for (int i = 0; i < ranges.size(); i++) {
      IntegerRange current = ranges.get(i);

      // The range does not overlap with the existing ones: {[range(i-1)], [range], [range(i)]}.
      if (range.getMax().compareTo(current.getMin()) < 0) {
        ranges.add(i, range);
        return;
      }

      // If the range overlaps with {range(i), ..., range(j)}, the ranges {range(i), ..., range(j)}
      // are removed from the domain, while merge(range, range(i), ..., range(j)) is added.
      IntegerRange union = range;

      while (range.overlaps(current) && i < ranges.size()) {
        current = ranges.get(i);
        union = union.merge(current);
        ranges.remove(i);
      }

      if (union != range) {
        ranges.add(i, union);
        return;
      }
    }

    // The range does not overlap with the existing ones: {..., [range(n)], [range]}.
    ranges.add(range);
  }

  /**
   * Includes the given domain into this one.
   * 
   * @param domain the domain to be included.
   * @throws NullPointerException if {@code domain} is null.
   */
  public void include(final IntegerDomain domain) {
    InvariantChecks.checkNotNull(domain);

    for (final IntegerRange range : domain.ranges) {
      this.include(range);
    }
  }

  /**
   * Excludes the given range from the domain.
   * 
   * @param range the range to be excluded from the domain.
   * @throws NullPointerException if {@code range} is null.
   */
  public void exclude(final IntegerRange range) {
    InvariantChecks.checkNotNull(range);

    for (int i = 0; i < ranges.size(); i++) {
      final IntegerRange current = ranges.get(i);

      // The range does not overlap with the existing ones: {[range(i-1)], [range], [range(i)]}.
      if (range.getMax().compareTo(current.getMin()) < 0) {
        // Do nothing.
        return;
      }

      // If the range overlaps with an existing one (range(i)), range(i) is removed, while
      // the difference between range(i) and range is added (if it is not empty).
      if (range.overlaps(current)) {
        final List<IntegerRange> difference = current.minus(range);

        ranges.remove(i);
        ranges.addAll(i, difference);

        i += (difference.size() - 1);
      }
    }
  }

  /**
   * Excludes the given domain from this one.
   * 
   * @param domain the domain to be excluded.
   * @throws NullPointerException if {@code domain} is null.
   */
  public void exclude(final IntegerDomain domain) {
    InvariantChecks.checkNotNull(domain);

    for (final IntegerRange range : domain.ranges) {
      this.exclude(range);
    }
  }

  /**
   * Intersects the domain with the given range (restricts the domain to the given range).
   * 
   * @param range the range to be intersected with the domain.
   * @throws NullPointerException if {@code range} is null.
   */
  public void intersect(final IntegerRange range) {
    InvariantChecks.checkNotNull(range);

    final List<IntegerRange> result = new ArrayList<>();

    for (final IntegerRange current : ranges) {
      if (range.getMax().compareTo(current.getMin()) < 0) {
        break;
      }

      if (range.overlaps(current)) {
        result.add(range.intersect(current));
      }
    }

    ranges.clear();
    ranges.addAll(result);
  }

  /**
   * Intersects this domain with the given one.
   * 
   * @param domain the domain to be intersected with this one.
   * @throws NullPointerException if {@code domain} is null.
   */
  public void intersect(final IntegerDomain domain) {
    InvariantChecks.checkNotNull(domain);

    final IntegerDomain result = new IntegerDomain();

    int i = 0;
    int j = 0;

    while (i < ranges.size() && j < domain.ranges.size()) {
      final IntegerRange range1 = ranges.get(i);
      final IntegerRange range2 = domain.ranges.get(j);

      // The situation {[range1], [range2]}.
      if (range2.getMin().compareTo(range1.getMax()) > 0) {
        i++;
        continue;
      }

      // The situation {[range2], [range1]}.
      if (range1.getMin().compareTo(range2.getMax()) > 0) {
        j++;
        continue;
      }

      // The ranges overlap.
      result.include(range1.intersect(range2));

      final int compare = range2.getMax().compareTo(range1.getMax());

      // Get the next indices.
      i = compare >= 0 /* max2 >= max1 */ ? i + 1 : i;
      j = compare <= 0 /* max1 >= max2 */ ? j + 1 : j;
    }

    set(result);
  }

  /**
   * Returns the iterator over the domain's values.
   * 
   * @return the domain iterator.
   */
  public Iterator<BigInteger> iterator() {
    return new MmuIterator();
  }
}
