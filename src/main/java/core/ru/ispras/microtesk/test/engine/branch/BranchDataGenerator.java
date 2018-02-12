/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine.branch;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.generator.DataGenerator;
import ru.ispras.testbase.generator.Utils;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link BranchDataGenerator} is a base class for test data generators for conditional branch
 * instructions.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class BranchDataGenerator implements DataGenerator {
  public static final String PARAM_CONDITION = "condition";
  public static final String PARAM_CONDITION_THEN = "true";
  public static final String PARAM_CONDITION_ELSE = "false";
  public static final String PARAM_STREAM = "stream";

  public static Map<String, Node> extractUnknown(final TestBaseQuery query) {
    InvariantChecks.checkNotNull(query);

    final Map<String, Node> result =  new LinkedHashMap<>();
    for (final Map.Entry<String, Node> e : query.getBindings().entrySet()) {
      final Node value = e.getValue();

      if (/* Known registers are considered to be unknown (stream based initialization) */
          value.getDataType().getTypeId() != DataTypeId.LOGIC_INTEGER
          /* Known immediate values are considered to be known */
          || Utils.isUnknownVariable(value)) {
        result.put(e.getKey(), value);
      }
    }

    return result;
  }

  @Override
  public final boolean isSuitable(final TestBaseQuery query) {
    final Object condition = Utils.getParameter(query, PARAM_CONDITION);
    final Object stream = Utils.getParameter(query, PARAM_STREAM);

    return stream != null
        && (condition == null /* No test data are required */
        || condition.equals(PARAM_CONDITION_THEN)
        || condition.equals(PARAM_CONDITION_ELSE));
  }

  /**
   * Generates test data that satisfy the branch condition.
   * 
   * @param query the test data generation query.
   * @return the test data provider.
   */
  public abstract Iterator<TestData> generateThen(final TestBaseQuery query);

  /**
   * Generates test data that violates the branch condition.
   * 
   * @param query the test data generation query.
   * @return the test data provider.
   */
  public abstract Iterator<TestData> generateElse(final TestBaseQuery query);

  @Override
  public final Iterator<TestData> generate(final TestBaseQuery query) {
    final Object condition = Utils.getParameter(query, PARAM_CONDITION);

    if (condition == null) {
      return generateThen(query);
    }
    if (condition.equals(PARAM_CONDITION_THEN)) {
      return generateThen(query);
    }
    if (condition.equals(PARAM_CONDITION_ELSE)) {
      return generateElse(query);
    }

    return null;
  }
}
