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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.EmptyIterator;
import ru.ispras.microtesk.test.sequence.GeneratorUtils;
import ru.ispras.testbase.knowledge.iterator.ArrayIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link RearrangerSample} rearranger samples the sequences
 * returned by the original iterator in a random manner.
 * 
 * <p>Logic is the following. {@code N} sequences are selected at random from
 * the original collection of sequences. {@code N} is a random number between
 * 0 and {@code S}, where {@code S} is the number of original sequences.
 * The relative order of selected sequences is preserved.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Sequence item type.
 */
public final class RearrangerSample<T> implements Rearranger<T> {
  private Iterator<List<T>> sampled;

  public RearrangerSample() {
    this.sampled = null;
  }

  private RearrangerSample(final RearrangerSample<T> other) {
    this.sampled = other.sampled.clone();
  }

  @Override
  public void initialize(final Iterator<List<T>> original) {
    this.sampled = sample(original);
  }

  private static <T> Iterator<List<T>> sample(final Iterator<List<T>> original) {
    InvariantChecks.checkNotNull(original);

    original.init();
    if (!original.hasValue()) {
      return EmptyIterator.get();
    }

    final ArrayList<List<T>> sampled = sample(GeneratorUtils.toArrayList(original));
    if (sampled.isEmpty()) {
      return EmptyIterator.get();
    }
 
    return new ArrayIterator<>(sampled);
  }

  private static <T> ArrayList<T> sample(final ArrayList<T> original) {
    InvariantChecks.checkNotNull(original);

    final int originalCount = original.size();
    final int sampleCount = Randomizer.get().nextIntRange(0, originalCount);

    final Map<Integer, T> sample = new TreeMap<>();
    while (sample.size() < sampleCount) {
      final int index = Randomizer.get().nextIntRange(0, originalCount - 1);
      final T value = original.get(index);
      sample.put(index, value);
    }

    return new ArrayList<>(sample.values());
  }

  @Override
  public void init() {
    InvariantChecks.checkNotNull(sampled);
    sampled.init();
  }

  @Override
  public boolean hasValue() {
    InvariantChecks.checkNotNull(sampled);
    return sampled.hasValue();
  }

  @Override
  public List<T> value() {
    InvariantChecks.checkNotNull(sampled);
    return sampled.value();
  }

  @Override
  public void next() {
    InvariantChecks.checkNotNull(sampled);
    sampled.next();
  }

  @Override
  public void stop() {
    InvariantChecks.checkNotNull(sampled);
    sampled.stop();
  }

  @Override
  public RearrangerSample<T> clone() {
    return new RearrangerSample<>(this);
  }
}
