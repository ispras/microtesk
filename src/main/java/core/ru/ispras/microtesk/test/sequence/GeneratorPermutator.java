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

package ru.ispras.microtesk.test.sequence;

import java.util.List;

import ru.ispras.microtesk.test.sequence.permutator.Permutator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class GeneratorPermutator<T> implements Generator<T> {
  private final Generator<T> generator;
  private final Permutator<T> permutator;

  private boolean hasValue;

  public GeneratorPermutator(final Generator<T> generator, final Permutator<T> permutator) {
    this.generator = generator;
    this.permutator = permutator;
  }

  private void initGenerator() {
    generator.init();
  }

  private void initPermutator() {
    permutator.setSequence(generator.value());
    permutator.init();
  }

  @Override
  public void init() {
    initGenerator();

    if (generator.hasValue()) {
      initPermutator();
    }

    hasValue = generator.hasValue() && permutator.hasValue();
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public List<T> value() {
    return permutator.value();
  }

  @Override
  public void next() {
    permutator.next();
    if (permutator.hasValue()) {
      return;
    }

    generator.next();
    if (generator.hasValue()) {
      initPermutator();
      return;
    }

    hasValue = false;
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public Iterator<List<T>> clone() {
    throw new UnsupportedOperationException();
  }
}
