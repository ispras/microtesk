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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class implements randomization methods.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RandomUtils {

  /**
   * Randomly chooses a value from the {@code domain} set.
   * 
   * @param <T> the item type.
   * @param domain the set of possible values.
   * @return a value from {@code domain} if it is not empty; {@code null} otherwise.
   * @throws NullPointerException if {@code domain} is null.
   */
  public static <T> T choose(final Set<T> domain) {
    InvariantChecks.checkNotNull(domain);

    if (domain.isEmpty()) {
      return null;
    }

    final ArrayList<T> values = new ArrayList<>(domain);
    return values.get(Randomizer.get().nextIntRange(0, values.size() - 1));
  }

  /**
   * Randomly chooses a value from the {@code include} set that does not belong to the
   * {@code exclude} set.
   * 
   * @param <T> the item type.
   * @param include the set of possible values (domain).
   * @param exclude the set of values that should be excluded from the domain.
   * @return a value from {@code include\exclude} if it is not empty; {@code null} otherwise.
   * @throws NullPointerException if {@code domain} or {@code exclude} is null.
   */
  public static <T> T choose(final Set<T> include, final Set<T> exclude) {
    InvariantChecks.checkNotNull(include);
    InvariantChecks.checkNotNull(exclude);

    final Set<T> domain = new LinkedHashSet<>(include);
    domain.removeAll(exclude);

    return choose(domain);
  }

  /**
   * Randomly chooses a value from the {@code [min, max]} range that does not belong to the
   * {@code exclude} set.
   * 
   * @param min the lower bound of the range.
   * @param max the upper bound of the range.
   * @param exclude the set of values that should be excluded from the domain.
   * @return a value from {@code [min, max] \ exclude} if it is not empty; {@code -1} otherwise.
   * @throws IllegalArgumentException if {@code min} is greater than {@code max}.
   * @throws NullPointerException if {@code exclude} is null.
   */
  public static long choose(final long min, final long max, final Set<Long> exclude) {
    InvariantChecks.checkGreaterOrEq(max, min);
    InvariantChecks.checkNotNull(exclude);

    final float factor = 0.25f;

    if ((max - min) + 1 > factor * exclude.size()) {
      while (true) {
        final long result = ((Randomizer.get().nextLong() >>> 1) % ((max - min) + 1)) + min;

        if (!exclude.contains(result)) {
          return result;
        }
      }
    }

    final Set<Long> include = new LinkedHashSet<>();
    for (long i = min; i <= max; i++) {
      include.add(i);
    }

    return choose(include, exclude);
  }
}
