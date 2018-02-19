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

package ru.ispras.microtesk.utils;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.randomizer.Randomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharedObjectTestCase {

  public static final class RandomValue extends SharedObject<RandomValue> {
    private int value;

    public RandomValue() {
      this.value = -1;
    }

    public RandomValue(final RandomValue other) {
      super(other);
      this.value = other.value;
    }

    @Override
    public RandomValue newCopy() {
      return new RandomValue(this);
    }

    public int getValue() {
      if (-1 == value) {
        value = Randomizer.get().nextIntRange(0, 10);
      }
      return value;
    }

    @Override
    public String toString() {
      return String.format("%d", getValue());
    }
  }

  public static final class SharedObjectHolder<T extends SharedObject<T>> {
    private final T object;

    public SharedObjectHolder(final T object) {
      this.object = object;
    }

    public SharedObjectHolder(final SharedObjectHolder<T> other) {
      this.object = other.object.getCopy();
    }

    public T getObject() {
      return object;
    }

    @Override
    public String toString() {
      return object.toString();
    }

    public static <U extends SharedObject<U>> List<SharedObjectHolder<U>> copyAll(
        final List<SharedObjectHolder<U>> list) {
      final List<SharedObjectHolder<U>> result = new ArrayList<>();
      for (final SharedObjectHolder<U> item : list) {
        result.add(new SharedObjectHolder<>(item));
      }
      return result;
    }
  }

  @Test
  public void test() {
    final RandomValue value = new RandomValue();

    final List<SharedObjectHolder<RandomValue>> original = new ArrayList<>();
    for (int index = 0; index < 10; ++index) {
      original.add(new SharedObjectHolder<>(value));
    }

    final List<SharedObjectHolder<RandomValue>> copy =
        SharedObjectHolder.copyAll(original);

    checkUseSharedObjects(copy);
    Assert.assertFalse(copy.get(0).getObject() == original.get(0).getObject());

    final List<SharedObjectHolder<RandomValue>> shuffled = new ArrayList<>(original);
    Collections.shuffle(shuffled);

    SharedObject.freeSharedCopies();
    final List<SharedObjectHolder<RandomValue>> shuffledCopy =
        SharedObjectHolder.copyAll(shuffled);

    checkUseSharedObjects(shuffledCopy);
    Assert.assertFalse(shuffledCopy.get(0).getObject() == original.get(0).getObject());
    Assert.assertFalse(shuffledCopy.get(0).getObject() == copy.get(0).getObject());

    System.out.println(original);
    System.out.println(shuffled);

    System.out.println(copy);
    System.out.println(shuffledCopy);
  }

  public static <U extends SharedObject<U>> void checkUseSharedObjects(
      final List<SharedObjectHolder<U>> list) {
    for (int index = 1; index < list.size(); index++) {
      final SharedObjectHolder<U> previous = list.get(index - 1);
      final SharedObjectHolder<U> current = list.get(index);
      Assert.assertEquals(previous.getObject(), current.getObject());
    }
  }
}
