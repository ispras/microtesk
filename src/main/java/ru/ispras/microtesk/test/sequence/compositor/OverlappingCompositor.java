/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.compositor;

import java.util.ArrayList;

import ru.ispras.microtesk.test.sequence.internal.IteratorEntry;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;

/**
 * This class implements the overlapping (shift) composition of iterators.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class OverlappingCompositor<T> extends Compositor<T> {
  // / The index of the recently used iterator from the compositor's list.
  private int n;
  // / The index of the recently selected iterator from <code>overlap</code>.
  private int k;
  // / The next selection.
  private int i;

  // / The list of the currently used iterators.
  private ArrayList<IteratorEntry<T>> overlap = new ArrayList<IteratorEntry<T>>();

  @Override
  protected void onInit() {
    k = i = n = 0;

    overlap.clear();

    if (iterators.size() > 0) {
      overlap.add(new IteratorEntry<T>(iterators.get(0)));
    }
  }

  @Override
  protected void onNext() {
    overlap.get(k).index++;
  }

  @Override
  protected IIterator<T> choose() {
    while (!overlap.isEmpty() || (n + 1) < iterators.size()) {
      if (overlap.isEmpty()) {
        overlap.add(new IteratorEntry<T>(iterators.get(++n)));
      }

      for (int j = 0; j < overlap.size(); j++) {
        k = (i + j) % overlap.size();
        IteratorEntry<T> entry = overlap.get(k);

        if (entry.index == entry.point && !entry.done) {
          if ((n + 1) < iterators.size()) {
            entry.done = true;
            overlap.add(new IteratorEntry<T>(iterators.get(++n)));

            continue;
          }
        }

        if (entry.iterator.hasValue()) {
          // The next choice will start from the next iterator.
          i = k + 1;

          return entry.iterator;
        }

        // Remove the exhausted iterator from the overlap list.
        overlap.remove(k);
        j--;
      }
    }

    return null;
  }
}
