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
import ru.ispras.testbase.knowledge.iterator.EmptyIterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.Collections;
import java.util.Map;

final class AbstractSequenceTestDataIterator implements Iterator<AbstractSequence> {
  private final EngineContext engineContext;
  private final String combinatorName;

  private final Iterator<AbstractSequence> sequenceIterator;
  private Iterator<Map<Situation, TestData>> dataIterator;

  // This is need to process a sequence at least once even if no test data are provided.
  private static final Iterator<Map<Situation, TestData>> DEFAULT_DATA_PROVIDER =
      new SingleValueIterator<Map<Situation, TestData>>(
          Collections.<Situation, TestData>emptyMap());

  public AbstractSequenceTestDataIterator(
      final EngineContext engineContext,
      final String combinatorName,
      final Iterator<AbstractSequence> sequenceIterator) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(combinatorName);
    InvariantChecks.checkNotNull(sequenceIterator);

    this.engineContext = engineContext;
    this.combinatorName = combinatorName;

    this.sequenceIterator = sequenceIterator;
    this.dataIterator = EmptyIterator.get();
  }

  @Override
  public void init() {
    sequenceIterator.init();
    initDataIterator();
  }

  private void initDataIterator() {
    if (!sequenceIterator.hasValue()) {
      dataIterator = EmptyIterator.get();
      return;
    }

    dataIterator = TestDataProviderFactory.newTestDataProvider(
        engineContext, combinatorName, sequenceIterator.value());

    dataIterator.init();

    if (!dataIterator.hasValue()) {
      dataIterator = DEFAULT_DATA_PROVIDER;
      dataIterator.init();
    }
  }

  @Override
  public boolean hasValue() {
    return dataIterator.hasValue();
  }

  @Override
  public AbstractSequence value() {
    final Map<Situation, TestData> testDataMap = dataIterator.value();
    for (final Map.Entry<Situation, TestData> entry : testDataMap.entrySet()) {
      final Situation situation = entry.getKey();
      final TestData testData = entry.getValue();
      situation.setTestData(testData);
    }

    final AbstractSequence sequence = sequenceIterator.value();
    return EngineUtils.cloneAbstractSequence(sequence);
  }

  @Override
  public void next() {
    if (dataIterator.hasValue()) {
      dataIterator.next();
      return;
    }

    sequenceIterator.next();
    initDataIterator();
  }

  @Override
  public void stop() {
    sequenceIterator.stop();
    dataIterator.stop();
  }

  @Override
  public Iterator<AbstractSequence> clone() {
    throw new UnsupportedOperationException();
  }
}
