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

package ru.ispras.microtesk.test.sequence;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link GeneratorRandom} uses another randomly selected generator to generate instruction
 * sequences. Each time the generator is initialized, a new delegate generator is selected.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Sequence element type.
 */
public final class GeneratorRandom<T> implements Generator<T> {
  private final Variate<Iterator<List<T>>> variate;
  private Iterator<List<T>> iterator;

  public GeneratorRandom(final Variate<Iterator<List<T>>> variate) {
    InvariantChecks.checkNotNull(variate);

    this.variate = variate;
    this.iterator = null;
  }

  private GeneratorRandom(final GeneratorRandom<T> other) {
    this.variate = other.variate;
    this.iterator = other.iterator != null ? other.iterator.clone() : null;
  }

  @Override
  public void init() {
    iterator = variate.value();
    iterator.init();
  }

  @Override
  public boolean hasValue() {
    return null != iterator && iterator.hasValue();
  }

  @Override
  public List<T> value() {
    if (!hasValue()) {
      throw new NoSuchElementException();
    }
    return iterator.value();
  }

  @Override
  public void next() {
    if (hasValue()) {
      iterator.next();
    }
  }

  @Override
  public void stop() {
    iterator = null;
  }

  @Override
  public GeneratorRandom<T> clone() {
    return new GeneratorRandom<>(this);
  }
}
