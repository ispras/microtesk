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

import java.util.NoSuchElementException;
import ru.ispras.fortress.util.InvariantChecks;

import java.util.List;

/**
 * {@link GeneratorNitems} generates N sequences with the help of another generator.
 *
 * <p>If the nested generator produces less than N items, generation is repeated
 * until N items are generated. If the nested generator is empty, no sequences are generated.</p>
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Sequence element type.
 */
public final class GeneratorNitems<T> implements Generator<T> {
  private final Generator<T> generator;
  private final int itemNumber;
  private int itemIndex;

  public GeneratorNitems(final Generator<T> generator, final int itemNumber) {
    InvariantChecks.checkNotNull(generator);
    InvariantChecks.checkGreaterThanZero(itemNumber);

    this.generator = generator;
    this.itemNumber = itemNumber;
    this.itemIndex = 0;
  }

  private GeneratorNitems(final GeneratorNitems<T> other) {
    this.generator = (Generator<T>) other.generator.clone();
    this.itemNumber = other.itemNumber;
    this.itemIndex = other.itemIndex;
  }

  @Override
  public void init() {
    generator.init();
    itemIndex = 0;
  }

  @Override
  public boolean hasValue() {
    return itemIndex < itemNumber && generator.hasValue();
  }

  @Override
  public List<T> value() {
    if (!hasValue()) {
      throw new NoSuchElementException();
    }
    return generator.value();
  }

  @Override
  public void next() {
    if (itemIndex >= itemNumber) {
      return;
    }

    itemIndex++;
    if (itemIndex >= itemNumber) {
      return;
    }

    generator.next();
    if (!generator.hasValue()) {
      generator.init();
    }
  }

  @Override
  public void stop() {
    itemIndex = itemNumber;
  }

  @Override
  public GeneratorNitems<T> clone() {
    return new GeneratorNitems<>(this);
  }
}
