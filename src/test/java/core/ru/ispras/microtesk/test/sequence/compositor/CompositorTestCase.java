/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
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

package ru.ispras.microtesk.test.sequence.compositor;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.testbase.knowledge.iterator.CollectionIterator;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CompositorTestCase {
  public CompositorTestCase() {}

  @Test
  public void runCatenationTest() {
    runTest(new CompositorCatenation<Integer>());
  }

  @Test
  public void runRotationTest() {
    runTest(new CompositorRotation<Integer>());
  }

  @Test
  public void runRandomTest() {
    runTest(new CompositorRandom<Integer>());
  }

  @Test
  public void runNestingTest() {
    runTest(new CompositorNesting<Integer>());
  }

  @Test
  public void runOverlappingTest() {
    runTest(new CompositorOverlapping<Integer>());
  }

  private void runTest(CompositorBase<Integer> compositor) {
    final int N = 10;
    final int L = 10;

    int size1 = 0;
    int size2 = 0;
    HashSet<Integer> set1 = new HashSet<Integer>();
    HashSet<Integer> set2 = new HashSet<Integer>();

    for (int i = 0; i < N; i++) {
      ArrayList<Integer> list = new ArrayList<Integer>();

      for (int j = 0; j < L + i; j++) {
        final Integer integer = new Integer(10 * i + j);

        list.add(integer);
        set1.add(integer);

        size1++;
      }

      compositor.addIterator(new CollectionIterator<Integer>(list));
    }

    for (compositor.init(); compositor.hasValue(); compositor.next()) {
      set2.add(compositor.value());

      size2++;
    }

    if (size1 != size2) {
      Assert.fail("Incorrect number of items: " + size2 + " (should be " + size1 + ")");
    }

    if (!set1.equals(set2)) {
      Assert.fail("Incorrect items: " + set2 + " (should be " + set1 + ")");
    }
  }
}
