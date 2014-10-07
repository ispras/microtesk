/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestBaseQueryCreator.java, Oct 7, 2014 5:43:46 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test.data;

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

public final class TestBaseQueryCreator
{
    private static final String NESTED_SITUATIONS_ERROR =
        "Error: The %s argument (type %s) is an operation with " +
        "test situation %s. The current version does not support " +
        "nesting of test situations.";

    private final String processor;
    private final Situation situation;
    private final Primitive primitive;

    private boolean isCreated;
    private TestBaseQuery query;
    private Map<String, UnknownValue> unknownValues;
    private Map<String, Primitive> modes;

    public TestBaseQueryCreator(
        String processor, Situation situation, Primitive primitive)
    {
        if (null == processor)
            throw new NullPointerException();

        if (null == situation)
            throw new NullPointerException();

        if (null == primitive)
            throw new NullPointerException();

        this.processor = processor;
        this.situation = situation;
        this.primitive = primitive;

        this.isCreated = false;
        this.query = null;
        this.unknownValues = null;
        this.modes = null;
    }

    public TestBaseQuery getQuery()
    {
        createQuery();

        if (null == query)
            throw new NullPointerException();

        return query;
    }

    public Map<String, UnknownValue> getUnknownValues()
    {
        createQuery();

        if (null == unknownValues)
            throw new NullPointerException();

        return unknownValues;
    }

    public Map<String, Primitive> getModes()
    {
        createQuery();

        if (null == modes)
            throw new NullPointerException();

        return modes;
    }

    private void createQuery()
    {
        if (isCreated)
            return;

        final TestBaseQueryBuilder queryBuilder = 
            new TestBaseQueryBuilder();

        createContext(queryBuilder);
        createParameters(queryBuilder);

        final BindingBuilder bindingBuilder = 
            new BindingBuilder(queryBuilder, primitive);

        unknownValues = bindingBuilder.getUnknownValues();
        modes = bindingBuilder.getModes();
        query = queryBuilder.build();

        isCreated = true;
    }

    private void createContext(TestBaseQueryBuilder queryBuilder)
    {
        queryBuilder.setContextAttribute(
            TestBaseContext.PROCESSOR, processor);

        queryBuilder.setContextAttribute(
            TestBaseContext.INSTRUCTION, primitive.getName());

        queryBuilder.setContextAttribute(
            TestBaseContext.TESTCASE, situation.getName());

        for (Argument arg : primitive.getArguments().values())
        {
            queryBuilder.setContextAttribute(arg.getName(), arg.getTypeName());
        }
    }

    private void createParameters(TestBaseQueryBuilder queryBuilder)
    {
        for (Map.Entry<String, Object> attrEntry :
            situation.getAttributes().entrySet())
        {
            queryBuilder.setParameter(
                attrEntry.getKey(), attrEntry.getValue().toString());
        }
    }

    private static final class BindingBuilder
    {
        private final TestBaseQueryBuilder queryBuilder;
        private final Map<String, UnknownValue> unknownValues;
        private final Map<String, Primitive> modes;

        private BindingBuilder(
            TestBaseQueryBuilder queryBuilder,
            Primitive primitive
            )
        {
            if (null == queryBuilder)
                throw new NullPointerException();

            if (null == primitive)
                throw new NullPointerException();

            this.queryBuilder = queryBuilder;
            this.unknownValues = new HashMap<String, UnknownValue>();
            this.modes = new HashMap<String, Primitive>();

            visit("", primitive);
        }

        public Map<String, UnknownValue> getUnknownValues()
        {
            return unknownValues;
        }

        public Map<String, Primitive> getModes()
        {
            return modes;
        }

        private void visit(String prefix, Primitive p)
        {
            if (p.getSituation() != null && !prefix.isEmpty())
                throw new IllegalArgumentException(String.format(
                    NESTED_SITUATIONS_ERROR, prefix, p.getTypeName()));

            for (Argument arg : p.getArguments().values())
            {
                final String argName = prefix.isEmpty() ?
                    arg.getName() : String.format("%s.%s", prefix, arg.getName());

                switch (arg.getKind())
                {
                case IMM:
                    queryBuilder.setBinding(argName,
                        NodeValue.newInteger((Integer) arg.getValue()));
                    break;

                case IMM_RANDOM:
                    queryBuilder.setBinding(argName,
                        NodeValue.newInteger(((RandomValue) arg.getValue()).getValue()));
                    break;

                case IMM_UNKNOWN:
                    queryBuilder.setBinding(argName,
                        new NodeVariable(new Variable(argName, DataType.INTEGER)));
                    unknownValues.put(argName, (UnknownValue) arg.getValue());
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
