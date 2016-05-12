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

package ru.ispras.microtesk.test.sequence.rearranger;

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link RearrangerTrivial} rearranger does not modify the
 * collection of sequences described by the original iterator.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Sequence item type.
 */
public final class RearrangerTrivial<T> implements Rearranger<T> {
  private Iterator<List<T>> original;

  public RearrangerTrivial() {
    this.original = null;
  }

  private RearrangerTrivial(final RearrangerTrivial<T> other) {
    this.original = other.original;
  }

  @Override
  public void initialize(final Iterator<List<T>> original) {
    InvariantChecks.checkNotNull(original);
    this.original = original;
  }

  @Override
  public void init() {
    InvariantChecks.checkNotNull(original);
    original.init();
  }

  @Override
  public boolean hasValue() {
    InvariantChecks.checkNotNull(original);
    return original.hasValue();
  }

  @Override
  public List<T> value() {
    InvariantChecks.checkNotNull(original);
    return original.value();
  }

  @Override
  public void next() {
    InvariantChecks.checkNotNull(original);
    original.next();
  }

  @Override
  public void stop() {
    InvariantChecks.checkNotNull(original);
    original.stop();
  }

  @Override
  public RearrangerTrivial<T> clone() {
    return new RearrangerTrivial<>(this);
  }
}
