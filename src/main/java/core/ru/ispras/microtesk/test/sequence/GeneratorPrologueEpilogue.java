/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public final class GeneratorPrologueEpilogue<T> implements Generator<T> {
  private final Generator<T> generator;
  private final List<T> prologue;
  private final List<T> epilogue;

  public GeneratorPrologueEpilogue(
      final Generator<T> generator,
      final List<T> prologue,
      final List<T> epilogue) {
    InvariantChecks.checkNotNull(generator);
    InvariantChecks.checkNotNull(prologue);
    InvariantChecks.checkNotNull(epilogue);

    this.generator = generator;
    this.prologue = prologue;
    this.epilogue = epilogue;
  }

  @Override
  public void init() {
    generator.init();
  }

  @Override
  public boolean hasValue() {
    return generator.hasValue();
  }

  @Override
  public List<T> value() {
    final List<T> body = generator.value();

    if (prologue.isEmpty() && epilogue.isEmpty()) {
      return body;
    }

    final List<T> result = new ArrayList<>(
        prologue.size() + body.size() + epilogue.size());

    result.addAll(prologue);
    result.addAll(body);
    result.addAll(epilogue);

    return result;
  }

  @Override
  public void next() {
    generator.next();
  }

  @Override
  public void stop() {
    generator.stop();
  }

  @Override
  public GeneratorSequence<T> clone() {
    throw new UnsupportedOperationException();
  }
}
