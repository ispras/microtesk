/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.engine.common;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.test.sequence.engine.common.TestDataGeneratorUtils.acquireContext;

import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryBuilder;

/**
 * The TestBaseQueryCreator class forms a query for test data that will be sent to TestBase. It
 * dumps the following information:
 * <ol>
 * <li>Name of the microprocessor being tested.</li>
 * <li>Information about the test situation (its name and attributes).</li>
 * <li>Name of the operation the situation is linked to.</li>
 * <li>All arguments of the operation including immediate values, addressing modes with their
 * arguments and all arguments of nested operations.</li>
 * </ol>
 * Arguments are treated in the following way:
 * <ul>
 * <li>All immediate arguments that have values are constants (see {@link NodeValue}) of type
 * {@link DataType#BIT_VECTOR}.</li>
 * <li>All unknown immediate arguments (see {@link UnknownImmediateValue}) that have not been 
 * assigned values are unknown variables (see {@link NodeVariable}) of type
 * {@link DataType#BIT_VECTOR}.</li>
 * <li>All addressing modes are unknown variables (see {@link NodeVariable}) of type
 * {@link DataType#BIT_VECTOR}.</li>
 * </ul>
 * <p>
 * N.B. If nested operations have linked test situations, these situations are ignored and no
 * information about them is included in the query. These situations are processed separately. If
 * they have been previously processed, unknown immediate arguments that received values are treated
 * as known immediate values.
 * <p>
 * N.B. The above text describes the current behavior that may be changed in the future.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class TestBaseQueryCreator {
  private IModel model;
  private final Situation situation;
  private final Primitive primitive;

  private boolean isCreated;
  private TestBaseQuery query;
  private Map<String, Argument> unknownImmediateValues;
  private Map<String, Argument> modes;

  public TestBaseQueryCreator(
      final IModel model,
      final Situation situation,
      final Primitive primitive) {
    checkNotNull(model);
    checkNotNull(primitive);

    this.model = model;

    this.situation = situation;
    this.primitive = primitive;

    this.isCreated = false;
    this.query = null;
    this.unknownImmediateValues = null;
    this.modes = null;
  }

  // TODO: can return null.
  public TestBaseQuery getQuery() {
    createQuery();
    return query;
  }

  public Map<String, Argument> getUnknownImmediateValues() {
    createQuery();

    checkNotNull(unknownImmediateValues);
    return unknownImmediateValues;
  }

  public Map<String, Argument> getModes() {
    createQuery();

    checkNotNull(modes);
    return modes;
  }

  private void createQuery() {
    if (isCreated) {
      return;
    }

    final TestBaseQueryBuilder queryBuilder = new TestBaseQueryBuilder();

    if (situation != null) {
      createContext(queryBuilder);
      createParameters(queryBuilder);
    }

    final BindingBuilder bindingBuilder = new BindingBuilder(model, queryBuilder, primitive);

    unknownImmediateValues = bindingBuilder.getUnknownValues();
    modes = bindingBuilder.getModes();

    query = queryBuilder.build();

    isCreated = true;
  }

  private void createContext(TestBaseQueryBuilder queryBuilder) {
    queryBuilder.setContextAttribute(TestBaseContext.PROCESSOR, model.getName());
    queryBuilder.setContextAttribute(TestBaseContext.INSTRUCTION, primitive.getName());
    queryBuilder.setContextAttribute(TestBaseContext.TESTCASE, situation.getName());

    queryBuilder.setContextAttribute(primitive.getName(), primitive.getName());
    acquireContext(queryBuilder, primitive.getName(), primitive);
  }

  private void createParameters(final TestBaseQueryBuilder queryBuilder) {
    for (Map.Entry<String, Object> attrEntry : situation.getAttributes().entrySet()) {
      queryBuilder.setParameter(attrEntry.getKey(), attrEntry.getValue());
    }
  }
}
