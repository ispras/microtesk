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

package ru.ispras.microtesk.test.sequence.permutator;

import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.ArrayList;
import java.util.List;

public class PermutatorTestCase {

  @Test
  public void testRandom() {
    final List<Integer> initialData = newData(10);
    final Permutator<Integer> permutator = new PermutatorRandom<>();

    permutator.initialize(initialData);
    permutator.init();
    Assert.assertTrue(permutator.hasValue());

    final List<Integer> resultData = permutator.value();

    Assert.assertNotEquals(initialData, resultData);
    Assert.assertEquals(new TreeSet<>(initialData), new TreeSet<>(resultData));

    permutator.next();
    Assert.assertFalse(permutator.hasValue());
  }

  @Test
  public void testTrivial() {
    final List<Integer> initialData = newData(10);
    final Permutator<Integer> permutator = new PermutatorTrivial<>();

    permutator.initialize(initialData);
    permutator.init();
    Assert.assertTrue(permutator.hasValue());

    final List<Integer> resultData = permutator.value();
    Assert.assertEquals(initialData, resultData);

    permutator.next();
    Assert.assertFalse(permutator.hasValue());
  }

  private static List<Integer> newData(final int length) {
    InvariantChecks.checkGreaterThanZero(length);

    final List<Integer> data = new ArrayList<>();
    for (int index = 0; index < length; index++) {
      data.add(index);
    }

    return data;
  }
}
