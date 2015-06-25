/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.testbase;

import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestDataProvider;
import ru.ispras.testbase.generator.DataGenerator;
import ru.ispras.testbase.stub.Utils;

/**
 * {@link BranchDataGenerator} is a base class for test data generators for conditional branch
 * instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class BranchDataGenerator implements DataGenerator {
  /** Branch condition parameter. */
  public static final String PARAM_CONDITION = "condition";
  public static final String PARAM_CONDITION_THEN = "true";
  public static final String PARAM_CONDITION_ELSE = "false";

  @Override
  public final boolean isSuitable(final TestBaseQuery query) {
    final Object condition = Utils.getParameter(query, PARAM_CONDITION);

    return condition != null
        && (condition.equals(PARAM_CONDITION_THEN) || condition.equals(PARAM_CONDITION_ELSE));
  }

  /**
   * Generates test data that satisfy the branch condition.
   * 
   * @param query the test data generation query.
   * @return the test data provider.
   */
  public abstract TestDataProvider generateThen(final TestBaseQuery query);

  /**
   * Generates test data that violates the branch condition.
   * 
   * @param query the test data generation query.
   * @return the test data provider.
   */
  public abstract TestDataProvider generateElse(final TestBaseQuery query);
  
  @Override
  public final TestDataProvider generate(final TestBaseQuery query) {
    final Object condition = Utils.getParameter(query, PARAM_CONDITION);

    if (condition.equals(PARAM_CONDITION_THEN)) {
      return generateThen(query);
    }
    if (condition.equals(PARAM_CONDITION_ELSE)) {
      return generateElse(query);
    }

    return null;
  }
}
