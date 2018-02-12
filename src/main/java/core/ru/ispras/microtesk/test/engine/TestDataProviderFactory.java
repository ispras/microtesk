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

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.knowledge.iterator.Iterator;

final class TestDataProviderFactory {
  private TestDataProviderFactory() {}

  public static TestDataProvider newTestDataProvider(
      final EngineContext engineContext,
      final String combinatorName,
      final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(combinatorName);
    InvariantChecks.checkNotNull(abstractSequence);

    final TestDataProviderBuilder dataProviderBuilder =
        new TestDataProviderBuilder(combinatorName);

    for (final AbstractCall abstractCall : abstractSequence.getSequence()) {
      if (abstractCall.isExecutable()) {
        final Primitive primitive = abstractCall.getRootOperation();
        visitPrimitive(engineContext, primitive, dataProviderBuilder);
      }
    }

    return dataProviderBuilder.build();
  }

  private static void visitPrimitive(
      final EngineContext engineContext,
      final Primitive primitive,
      final TestDataProviderBuilder testDataProviderBuilder) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkNotNull(testDataProviderBuilder);

    for (final Argument argument : primitive.getArguments().values()) {
      if (Argument.Kind.OP == argument.getKind() || Argument.Kind.MODE == argument.getKind()) {
        final Primitive argumentPrimitive = (Primitive) argument.getValue();
        visitPrimitive(engineContext, argumentPrimitive, testDataProviderBuilder);
      }
    }

    final Situation situation = primitive.getSituation();
    if (null == situation || !situation.isTestDataProvider()) {
      return;
    }

    final Iterator<TestData> testDataIterator =
        generateTestData(engineContext, primitive, situation);

    InvariantChecks.checkNotNull(testDataIterator);
    testDataProviderBuilder.register(situation, testDataIterator);
  }

  private static Iterator<TestData> generateTestData(
      final EngineContext engineContext,
      final Primitive primitive,
      final Situation situation) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkNotNull(situation);

    final TestBaseQueryCreator queryCreator =
        new TestBaseQueryCreator(engineContext,0,null, situation, primitive);

    Logger.debug("Processing %s for %s...", situation, primitive.getSignature());

    final TestBaseQuery query = queryCreator.getQuery();
    Logger.debug("Query to TestBase: " + query);

    final TestBase testBase = TestBase.get();
    final TestBaseQueryResult queryResult = testBase.executeQuery(query);

    if (TestBaseQueryResult.Status.OK != queryResult.getStatus()) {
      throw new IllegalArgumentException(
          String.format("Query processing has failed: %s", queryResult));
    }

    return queryResult.getDataIterator();
  }
}
