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

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link TrivialPermutator} implements a trivial permutator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class TrivialPermutator<T> extends Permutator<T> {
  @Override
  public void init() {
    InvariantChecks.checkNotNull(original);
    sequence = original;
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
