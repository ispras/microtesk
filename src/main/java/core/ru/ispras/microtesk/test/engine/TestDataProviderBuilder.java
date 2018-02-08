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
import ru.ispras.microtesk.test.sequence.GeneratorConfig;
import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.ArrayList;
import java.util.List;

final class TestDataProviderBuilder {
  private final String combinatorName;
  private final List<Situation> situations;
  private final List<Iterator<TestData>> iterators;

  public TestDataProviderBuilder(final String combinatorName) {
    InvariantChecks.checkNotNull(combinatorName);

    this.combinatorName = combinatorName;
    this.situations = new ArrayList<>();
    this.iterators = new ArrayList<>();
  }

  public void register(final Situation situation, final Iterator<TestData> testDataIterator) {
    InvariantChecks.checkNotNull(situation);
    InvariantChecks.checkNotNull(testDataIterator);

    situations.add(situation);
    iterators.add(testDataIterator);
  }

  public TestDataProvider build() {
    final Combinator<TestData> combinator =
        GeneratorConfig.<TestData>get().getCombinator(combinatorName);

    return new TestDataProvider(situations, combinator);
  }
}
