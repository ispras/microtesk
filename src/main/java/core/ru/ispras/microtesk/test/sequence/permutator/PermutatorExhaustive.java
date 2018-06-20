/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link PermutatorExhaustive} implements an exhaustive permutator that produces all
 * possible permutations for a given tuple. The implementation is based on Heap's algorithm.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class PermutatorExhaustive<T> extends PermutatorBase<T> {
  private int index = 0;
  private int[] indices = null;

  public PermutatorExhaustive() {
    super();
  }

  private PermutatorExhaustive(final PermutatorExhaustive<T> other) {
    super(other);

    this.index = other.index;
    this.indices = Arrays.copyOf(other.indices, other.indices.length);
  }

  @Override
  public void init() {
    InvariantChecks.checkNotNull(original);

    sequence = new ArrayList<>(original);
    index = 0;

    indices = new int[sequence.size()];
    Arrays.fill(indices, 0);
  }

  @Override
  public boolean hasValue() {
    return indices != null && index < indices.length;
  }

  @Override
  public List<T> value() {
    return new ArrayList<>(sequence);
  }

  @Override
  public void next() {
    while (indices[index] >= index) {
      indices[index] = 0;
      index++;

      if (index >= indices.length) {
        return;
      }
    }

    Collections.swap(sequence, index % 2 == 0 ? 0 : indices[index], index);
    indices[index]++;
    index = 0;
  }

  @Override
  public void stop() {
    sequence = null;
    index = 0;
    indices = null;
  }

  @Override
  public PermutatorExhaustive<T> clone() {
    return new PermutatorExhaustive<>(this);
  }
}
