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

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link RandomPermutator} implements a random permutator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class RandomPermutator<T> extends Permutator<T> {
  private static final float PERMUTATION_RATE = 1.0f;

  @Override
  public void init() {
    InvariantChecks.checkNotNull(original);
    InvariantChecks.checkNotEmpty(original);

    final int[] permutation = new int[original.size()];
    for (int i = 0; i < permutation.length; i++) {
      permutation[i] = i;
    }

    final int permutationNumber = (int)(PERMUTATION_RATE * permutation.length);

    for (int n = 0; n < permutationNumber; n++) {
      final int i = Randomizer.get().nextIntRange(0, permutation.length - 1);
      final int j = Randomizer.get().nextIntRange(0, permutation.length - 1);

      final int temp = permutation[i];

      permutation[i] = permutation[j];
      permutation[j] = temp;
    }

    sequence = new ArrayList<>(original.size());

    for (int i = 0; i < permutation.length; i++) {
      sequence.add(original.get(permutation[i]));
    }
  }

  @Override
  public boolean hasValue() {
    return sequence != null;
  }

  @Override
  public List<T> value() {
    return sequence;
  }

  @Override
  public void next() {
    sequence = null;
  }

  @Override
  public void stop() {
    sequence = null;
  }
}
