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

package ru.ispras.microtesk.basis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link ClassifierFilter} implements a classifier with a filter.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ClassifierFilter<T> implements Classifier<T> {
  private final Classifier<T> classifier;
  private final Predicate<T> filter;

  public ClassifierFilter(final Classifier<T> classifier, final Predicate<T> filter) {
    InvariantChecks.checkNotNull(classifier);
    InvariantChecks.checkNotNull(filter);

    this.classifier = classifier;
    this.filter = filter;
  }

  @Override
  public List<Set<T>> classify(final Collection<T> objects) {
    InvariantChecks.checkNotNull(objects);

    final List<Set<T>> classes1 = classifier.classify(objects);
    final List<Set<T>> classes2 = new ArrayList<>();

    for (final Set<T> class1 : classes1) {
      final Set<T> class2 = new LinkedHashSet<>();

      for (final T object : class1) {
        if (filter.test(object)) {
          class2.add(object);
        }
      }

      if (!class2.isEmpty()) {
        classes2.add(class2);
      }
    }

    return classes2;
  }
}
