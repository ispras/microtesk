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

/**
 * {@link PermutatorBase} is a basic permutator. It takes a list of items and returns an iterator of
 * permuted lists.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
abstract class PermutatorBase<T> implements Permutator<T> {
  protected ArrayList<T> original;
  protected ArrayList<T> sequence;

  public PermutatorBase() {
    this.original = null;
    this.sequence = null;
  }

  protected PermutatorBase(final PermutatorBase<T> other) {
    this.original = other.original;
    this.sequence = other.sequence;
  }

  @Override
  public final void initialize(final List<T> original) {
    InvariantChecks.checkNotNull(original);
    this.original = new ArrayList<>(original);
  }

  @Override
  public PermutatorBase<T> clone() {
    throw new UnsupportedOperationException();
  }
}
