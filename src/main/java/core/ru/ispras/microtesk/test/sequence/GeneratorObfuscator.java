/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.permutator.Permutator;

import java.util.List;

public final class GeneratorObfuscator<T> implements Generator<T> {
  private final Generator<T> generator;
  private final Permutator<T> obfuscator;

  private boolean hasValue;

  public GeneratorObfuscator(final Generator<T> generator, final Permutator<T> obfuscator) {
    InvariantChecks.checkNotNull(generator);
    InvariantChecks.checkNotNull(obfuscator);

    this.generator = generator;
    this.obfuscator = obfuscator;
  }

  private GeneratorObfuscator(final GeneratorObfuscator<T> other) {
    this.generator = (Generator<T>) other.generator.clone();
    this.obfuscator = (Permutator<T>) other.obfuscator.clone();
    this.hasValue = other.hasValue;
  }

  private void initGenerator() {
    generator.init();
  }

  private void initModificator() {
    obfuscator.initialize(generator.value());
    obfuscator.init();
  }

  @Override
  public void init() {
    initGenerator();

    if (generator.hasValue()) {
      initModificator();
    }

    hasValue = generator.hasValue() && obfuscator.hasValue();
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public List<T> value() {
    return obfuscator.value();
  }

  @Override
  public void next() {
    obfuscator.next();
    if (obfuscator.hasValue()) {
      return;
    }

    generator.next();
    if (generator.hasValue()) {
      initModificator();
      return;
    }

    hasValue = false;
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public GeneratorObfuscator<T> clone() {
    return new GeneratorObfuscator<>(this);
  }
}
