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

package ru.ispras.microtesk.test;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownValue;
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
 * {@link DataType#INTEGER}.</li>
 * <li>All unknown immediate arguments (see {@link UnknownValue}) that have not been assigned values
 * are unknown variables (see {@link NodeVariable}) of type {@link DataType#INTEGER}.</li>
 * <li>All addressing modes are unknown variables (see {@link NodeVariable}) of type
 * {@link DataType#UNKNOWN}.</li>
 * </ul>
 * <p>
 * N.B. If nested operations have linked test situations, these situations are ignored and no
 * information about them is included in the query. These situations are processed separately. If
 * they have been previously processed, unknown immediate arguments that received values are treated
 * as known immediate values.
 * <p>
 * N.B. The above text describes the current behavior that may be changed in the future.
 * 
 * @author Andrei Tatarnikov
 */

final class TestBaseQueryCreator {
  private final String processor;
  private final Situation situation;
  private final Primitive primitive;

  private boolean isCreated;
  private TestBaseQuery query;
  private Map<String, UnknownValue> unknownValues;
  private Map<String, Primitive> modes;

  public TestBaseQueryCreator(
      final String processor,
      final Situation situation,
      final Primitive primitive) {
    checkNotNull(processor);
    checkNotNull(situation);
    checkNotNull(primitive);

    this.processor = processor;
    this.situation = situation;
    this.primitive = primitive;

    this.isCreated = false;
    this.query = null;
    this.unknownValues = null;
    this.modes = null;
  }

  public TestBaseQuery getQuery() {
    createQuery();

    checkNotNull(query);
    return query;
  }

  public Map<String, UnknownValue> getUnknownValues() {
    createQuery();

    checkNotNull(unknownValues);
    return unknownValues;
  }

  public Map<String, Primitive> getModes() {
    createQuery();

    checkNotNull(modes);
    return modes;
  }

  private void createQuery() {
    if (isCreated) {
      return;
    }

    final TestBaseQueryBuilder queryBuilder = new TestBaseQueryBuilder();

    createContext(queryBuilder);
    createParameters(queryBuilder);

    final BindingBuilder bindingBuilder = new BindingBuilder(queryBuilder, primitive);

    unknownValues = bindingBuilder.getUnknownValues();
    modes = bindingBuilder.getModes();
    query = queryBuilder.build();

    isCreated = true;
  }

  private void createContext(TestBaseQueryBuilder queryBuilder) {
    queryBuilder.setContextAttribute(TestBaseContext.PROCESSOR, processor);
    queryBuilder.setContextAttribute(TestBaseContext.INSTRUCTION, primitive.getName());
    queryBuilder.setContextAttribute(TestBaseContext.TESTCASE, situation.getName());

    queryBuilder.setContextAttribute(primitive.getName(), primitive.getName());
    acquireContext(queryBuilder, primitive.getName(), primitive);
  }

  private static void acquireContext(
      final TestBaseQueryBuilder builder,
      final String prefix,
      final Primitive p) {
    for (final Argument arg : p.getArguments().values()) {
      final String ctxArgName = (prefix.isEmpty())
                                ? arg.getName()
                                : prefix + "." + arg.getName();
      builder.setContextAttribute(ctxArgName, arg.getTypeName());
      switch (arg.getKind()) {
      case OP:
      case MODE:
        acquireContext(builder, ctxArgName, (Primitive) arg.getValue());
        break;

      default:
      }
    }
  }

  private void createParameters(final TestBaseQueryBuilder queryBuilder) {
    for (Map.Entry<String, Object> attrEntry : situation.getAttributes().entrySet()) {
      queryBuilder.setParameter(attrEntry.getKey(), attrEntry.getValue());
    }
  }

  private static final class BindingBuilder {
    private final TestBaseQueryBuilder queryBuilder;
    private final Map<String, UnknownValue> unknownValues;
    private final Map<String, Primitive> modes;

    private BindingBuilder(
        final TestBaseQueryBuilder queryBuilder,
        final Primitive primitive) {
      checkNotNull(queryBuilder);
      checkNotNull(primitive);

      this.queryBuilder = queryBuilder;
      this.unknownValues = new HashMap<String, UnknownValue>();
      this.modes = new HashMap<String, Primitive>();

      visit(primitive.getName(), primitive);
    }

    public Map<String, UnknownValue> getUnknownValues() {
      return unknownValues;
    }

    public Map<String, Primitive> getModes() {
      return modes;
    }

    private void visit(final String prefix, final Primitive p) {
      for (Argument arg : p.getArguments().values()) {
        final String argName = prefix.isEmpty() ?
          arg.getName() : String.format("%s.%s", prefix, arg.getName());

        switch (arg.getKind()) {
          case IMM:
            queryBuilder.setBinding(argName, NodeValue.newInteger((Integer) arg.getValue()));
            break;

          case IMM_RANDOM:
            queryBuilder.setBinding(argName,
              NodeValue.newInteger(((RandomValue) arg.getValue()).getValue()));
            break;

          case IMM_UNKNOWN:
            if (!((UnknownValue) arg.getValue()).isValueSet()) {
              queryBuilder.setBinding(argName,
                new NodeVariable(new Variable(argName, DataType.INTEGER)));
              unknownValues.put(argName, (UnknownValue) arg.getValue());
            } else {
              queryBuilder.setBinding(argName,
                NodeValue.newInteger(((UnknownValue) arg.getValue()).getValue()));
            }
            break;

          case MODE:
            queryBuilder.setBinding(argName,
              new NodeVariable(new Variable(argName, DataType.UNKNOWN)));
            modes.put(argName, (Primitive) arg.getValue());
            visit(argName, (Primitive) arg.getValue());
            break;

          case OP:
            visit(argName, (Primitive) arg.getValue());
            break;

          default:
            throw new IllegalArgumentException(String.format(
              "Illegal kind of argument %s: %s.", argName, arg.getKind()));
        }
      }
    }
  }
}
