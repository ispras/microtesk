/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.permutator;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link Permutator} is a basic permutator. It takes a list of items and returns an iterator of
 * permutated lists.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Permutator<T> implements Iterator<List<T>> {
  protected ArrayList<T> original;
  protected ArrayList<T> sequence; 

  public final void setSequence(final List<T> original) {
    InvariantChecks.checkNotNull(original);
    this.original = new ArrayList<>(original);
  }

  @Override
  public Permutator<T> clone() {
    throw new UnsupportedOperationException();
  }
}
