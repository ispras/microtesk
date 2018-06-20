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

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class PermutatorTestCase {
  public static final int SIZE = 10;

  @Test
  public void testRandom() {
    final List<Integer> initialData = newData(SIZE);
    final Permutator<Integer> permutator = new PermutatorRandom<>();

    permutator.initialize(initialData);
    permutator.init();
    Assert.assertTrue(permutator.hasValue());

    final List<Integer> resultData = permutator.value();

    Assert.assertNotEquals(initialData, resultData);
    Assert.assertEquals(new HashSet<>(initialData), new HashSet<>(resultData));

    permutator.next();
    Assert.assertFalse(permutator.hasValue());
  }

  @Test
  public void testTrivial() {
    final List<Integer> initialData = newData(SIZE);
    final Permutator<Integer> permutator = new PermutatorTrivial<>();

    permutator.initialize(initialData);
    permutator.init();
    Assert.assertTrue(permutator.hasValue());

    final List<Integer> resultData = permutator.value();
    Assert.assertEquals(initialData, resultData);

    permutator.next();
    Assert.assertFalse(permutator.hasValue());
  }

  @Test
  public void testExhaustive() {
    final List<Integer> initialData = newData(SIZE);
    final Set<Integer> initialDataSet = new HashSet<>(initialData);

    final Permutator<Integer> permutator = new PermutatorExhaustive<>();
    permutator.initialize(initialData);

    int count = 0;
    final Set<List<Integer>> resultSet = new HashSet<>();

    for (permutator.init(); permutator.hasValue(); permutator.next()) {
      final List<Integer> resultData = permutator.value();

      // Result must include the same elements as Initial.
      Assert.assertEquals(initialDataSet, new HashSet<>(resultData));

      // Result must be unique.
      Assert.assertFalse(resultSet.contains(resultData));

      count++;
      resultSet.add(resultData);
    }

    // SIZE! permutations must be produced.
    Assert.assertEquals(factorial(SIZE), count);
  }

  private static List<Integer> newData(final int length) {
    InvariantChecks.checkGreaterThanZero(length);

    final List<Integer> data = new ArrayList<>();
    for (int index = 0; index < length; index++) {
      data.add(index);
    }

    return data;
  }

  private static int factorial(final int number) {
    InvariantChecks.checkGreaterThanZero(number);

    int result = 1;

    for (int factor = 2; factor <= number; factor++) {
      InvariantChecks.checkGreaterThanZero(result);
      result *= factor;
    }

    return result;
  }
}
