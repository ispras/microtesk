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

package ru.ispras.microtesk.basis.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link ClassifierUniversal} implements the trivial policy for object classification: all objects
 * belong to one equivalence class.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ClassifierUniversal<T> implements Classifier<T> {
  @Override
  public List<Set<T>> classify(final Collection<T> objects) {
    InvariantChecks.checkNotNull(objects);

    final List<Set<T>> groups = new ArrayList<>();
    final Set<T> group = new LinkedHashSet<>();

    for (final T object : objects) {
      group.add(object);
    }

    groups.add(group);
    return groups;
  }
}
