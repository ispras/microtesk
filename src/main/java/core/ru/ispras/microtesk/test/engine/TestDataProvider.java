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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class TestDataProvider implements Iterator<Map<Situation, TestData>> {
  private final List<Situation> situations;
  private final Iterator<List<TestData>> testDataIterator;

  public TestDataProvider(
      final List<Situation> situations,
      final Iterator<List<TestData>> testDataIterator) {
    InvariantChecks.checkNotNull(situations);
    InvariantChecks.checkNotNull(testDataIterator);

    this.situations = situations;
    this.testDataIterator = testDataIterator;
  }

  @Override
  public void init() {
    testDataIterator.init();
  }

  @Override
  public boolean hasValue() {
    return testDataIterator.hasValue();
  }

  @Override
  public Map<Situation, TestData> value() {
    return newTestDataMap(situations, testDataIterator.value());
  }

  private static Map<Situation,TestData> newTestDataMap(
      final List<Situation> situations,
      final List<TestData> data) {
    InvariantChecks.checkTrue(situations.size() == data.size());

    final Map<Situation, TestData> result = new IdentityHashMap<>();
    for (int index = 0; index < situations.size(); ++index) {
      final Situation situation = situations.get(index);
      InvariantChecks.checkFalse(result.containsKey(situations));
      result.put(situation, data.get(index));
    }

    return result;
  }

  @Override
  public void next() {
    testDataIterator.next();
  }

  @Override
  public void stop() {
    testDataIterator.stop();
  }

  @Override
  public Iterator<Map<Situation, TestData>> clone() {
    throw new UnsupportedOperationException();
  }
}
