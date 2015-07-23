/*
 * Copyright 2007-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.basis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BufferStateTracker} implements a simplified buffer used to imitate data replacement logic.
 * 
 * @param <A> address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferStateTracker<A extends Number> {
  /** Number of sets in the buffer. */
  private final long sets;
  /** Number of ways in the buffer. */
  private final long ways;

  /** The address view. */
  private final AddressView<A> addressView;

  /** The state of the buffer. */
  private final Map<A, List<A>> state = new LinkedHashMap<>();

  /**
   * Constructs a buffer state tracker.
   * 
   * @param sets the number of sets (index range).
   * @param ways the number of ways (associativity).
   * @param addressView the address view.
   */
  public BufferStateTracker(final long sets, final long ways, final AddressView<A> addressView) {
    InvariantChecks.checkNotNull(addressView);

    if (sets <= 0 || ways <= 0) {
      throw new IllegalArgumentException(
          String.format("Illegal parameters: sets=%d, ways=%d", sets, ways));
    }

    this.sets = sets;
    this.ways = ways;

    this.addressView = addressView;
  }

  /**
   * Returns the number of sets (index range).
   * 
   * @return the number of sets.
   */
  public long getSets() {
    return sets;
  }

  /**
   * Returns the number of ways (associativity).
   * 
   * @return the number of ways.
   */
  public long getWays() {
    return ways;
  }

  /**
   * Resets the buffer state.
   */
  public void reset() {
    state.clear();
  }

  /**
   * Returns the number of items stored in the buffer.
   * 
   * @return the number of items.
   */
  public int size() {
    int count = 0;
 
    for (final List<A> set : state.values()) {
      count += set.size();
    }

    return count;
  }

  /**
   * Returns the number of items stored in the i-th set of the buffer.
   * 
   * @param i the set index.
   * @return the number of items.
   */
  public int size(final int i) {
    final List<A> set = state.get(i);

    return set == null ? 0 : set.size();
  }

  /**
   * Imitates access to the buffer (updates the buffer state).
   * 
   * @param address the address being accessed.
   * @return the tag having being replaced or {@code null}.
   */
  public A track(final A address) {
    InvariantChecks.checkNotNull(address);

    A replacedTag = null;

    final A setIndex = addressView.getIndex(address);
    if (setIndex.intValue() < 0 || setIndex.intValue() >= sets) {
      throw new IndexOutOfBoundsException();
    }

    List<A> set = state.get(setIndex);
    if (set == null) {
      state.put(setIndex, set = new ArrayList<>());
    }

    final A dataTag = addressView.getTag(address);
    final int hitIndex = set.indexOf(dataTag);

    if (hitIndex == -1) {
      // Buffer miss.
      if (set.size() == ways) {
        replacedTag = set.remove(0);
      }
      set.add(dataTag);
    } else {
      // Buffer hit.
      set.remove(hitIndex);
      set.add(dataTag);
    }

    return replacedTag;
  }
}
