/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.ArrayList;
import java.util.List;

public final class GeneratorUtils {
  private GeneratorUtils() {}

  public static <T> List<T> expand(final Iterator<List<T>> iterator) {
    InvariantChecks.checkNotNull(iterator);

    final List<T> result = new ArrayList<>();
    for (iterator.init(); iterator.hasValue(); iterator.next()) {
      result.addAll(iterator.value());
    }

    return result;
  }

  public static <T> List<T> expandAll(final List<Iterator<List<T>>> iterators) {
    InvariantChecks.checkNotNull(iterators);

    final List<T> result = new ArrayList<>();
    for (final Iterator<List<T>> sequenceIterator : iterators) {
      final List<T> sequence = expand(sequenceIterator);
      result.addAll(sequence);
    }

    return result;
  }

  public static <T> ArrayList<List<T>> toArrayList(final Iterator<List<T>> iterator) {
    InvariantChecks.checkNotNull(iterator);

    final ArrayList<List<T>> result = new ArrayList<>();
    for (iterator.init(); iterator.hasValue(); iterator.next()) {
      result.add(iterator.value());
    }

    return result;
  }
}
