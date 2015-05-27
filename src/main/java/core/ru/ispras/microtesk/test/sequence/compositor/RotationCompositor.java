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

import ru.ispras.microtesk.test.sequence.iterator.Iterator;

/**
 * This class implements the rotation (interleaving) composition of iterators.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RotationCompositor<T> extends Compositor<T> {
  /** The current iterator index. */
  private int i;

  @Override
  protected void onInit() {
    i = 0;
  }

  @Override
  protected void onNext() {
    // Do nothing.
  }

  @Override
  protected Iterator<T> choose() {
    for (int j = 0; j < iterators.size(); j++) {
      final int k = (i + j) % iterators.size();

      if (iterators.get(k).hasValue()) {
        // The next choice will start from the next iterator.
        i = k + 1;

        return iterators.get(k);
      }
    }

    return null;
  }
}
