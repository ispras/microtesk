/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.ArrayList;
import java.util.List;

public final class GeneratorPrologueEpilogue<T> implements Generator<T> {
  private final Iterator<List<T>> iterator;
  private final List<T> prologue;
  private final List<T> epilogue;

  public GeneratorPrologueEpilogue(
      final Iterator<List<T>> iterator,
      final List<T> prologue,
      final List<T> epilogue) {
    InvariantChecks.checkNotNull(iterator);
    InvariantChecks.checkNotNull(prologue);
    InvariantChecks.checkNotNull(epilogue);

    this.iterator = iterator;
    this.prologue = prologue;
    this.epilogue = epilogue;
  }

  private GeneratorPrologueEpilogue(final GeneratorPrologueEpilogue<T> other) {
    this.iterator = other.iterator.clone();
    this.prologue = other.prologue;
    this.epilogue = other.epilogue;
  }

  @Override
  public void init() {
    iterator.init();
  }

  @Override
  public boolean hasValue() {
    return iterator.hasValue();
  }

  @Override
  public List<T> value() {
    final List<T> body = iterator.value();

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
    iterator.next();
  }

  @Override
  public void stop() {
    iterator.stop();
  }

  @Override
  public GeneratorPrologueEpilogue<T> clone() {
    return new GeneratorPrologueEpilogue<>(this);
  }
}
