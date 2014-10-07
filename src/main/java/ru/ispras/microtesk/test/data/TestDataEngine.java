/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestDataEngine.java, Oct 3, 2014 3:18:19 PM Andrei Tatarnikov
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownValue;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryBuilder;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;

public final class TestDataEngine
{
    private final IModel model;
    private final List<TestDataProviderBase> providers;

    public TestDataEngine(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        this.model = model;
        this.providers = Arrays.asList(
            new TDPImmRandom(),
            new TDPImmRange(),
            new TDPZero(),
            new TDPRandom()
            );
    }

    public void generateData(Situation situation, Primitive primitive)
    {
        System.out.printf("Processing situation %s for %s...%n",
            situation, primitive.getSignature());

        final TestBaseQueryCreator queryCreator =
            new TestBaseQueryCreator(model.getName(), situation, primitive);

        final TestBaseQuery query = queryCreator.getQuery(); 
        System.out.println("Query to TestBase: " + query);

        final Map<String, UnknownValue> unknownValues = queryCreator.getUnknownValues();
        System.out.println("Unknown values: " + unknownValues.keySet());

        final Map<String, Primitive> modes = queryCreator.getModes();
        System.out.println("Modes used as arguments: " + modes);

        final TestBaseQueryResult queryResult = executeQuery(query);
        if (TestBaseQueryResult.Status.OK != queryResult.getStatus())
        {
            printErrors(queryResult);
            return;
        }

        final TestDataProvider dataProvider = queryResult.getDataProvider(); 
        if (!dataProvider.hasNext())
        {
            System.out.println("No data was generated for the query.");
            return;
        }

        final TestData testData = dataProvider.next();
        System.out.println(testData);

        // Set unknown immediate values
        for (Map.Entry<String, UnknownValue> e : unknownValues.entrySet())
        {
            final String name = e.getKey();
            final UnknownValue target = e.getValue();

            final Node value = testData.getBindings().get(name);
            final int intValue = extractInt(name, value);

            target.setValue(intValue);
        }

        // Set model state using preparators that create initializing
        // sequences based on addressing modes.
        for (Map.Entry<String, Node> e : testData.getBindings().entrySet())
        {
            final String name = e.getKey();
            final Primitive targetMode = modes.get(name);

            if (null == targetMode)
                continue;

            System.out.printf("!!! Argument %s (%s) needs a preparator.%n",
                name, targetMode.getSignature());
        }
    }

    private TestBaseQueryResult executeQuery(TestBaseQuery query)
    {
        for(TestDataProviderBase provider : providers)
        {
            if (provider.isSuitable(query))
            {
                provider.initialize(query);
                return TestBaseQueryResult.success(provider);
            }
        }

        return TestBaseQueryResult.reportErrors(
            Collections.<String>singletonList(
                "No suitable test data generator is found."));
    }

    private void printErrors(TestBaseQueryResult queryResult)
    {
        final StringBuilder sb = new StringBuilder(String.format(
            "Failed to execute the query. Status: %s.", queryResult.getStatus()));

        if (queryResult.hasErrors())
        {
            sb.append(" Errors: ");
            for (String error : queryResult.getErrors())
            {
                sb.append("\r\n  ");
                sb.append(error);
            }
        }

        System.out.println(sb);
    }
    
    private int extractInt(final String name, final Node value)
    {
        if (null == value || !ExprUtils.isConstant(value))
        {
            System.out.printf("Failed to generate a value for the %s argument." +
                "Default value (0) will be used.%n", name);
            return 0;
        }

        if (value.getKind() != Node.Kind.VALUE)
           throw new IllegalStateException(String.format(
              "Illegal kind of the %s argument value: %s.", name, value.getKind()));

        final Data data = ((NodeValue) value).getData();
        final int intValue;
        
        if (data.getType().getTypeId() == DataTypeId.LOGIC_INTEGER)
        {
            intValue = ((Integer) data.getValue()).intValue();
        }
        else if (data.getType().getTypeId() == DataTypeId.BIT_VECTOR)
        {
            intValue = ((BitVector) data.getValue()).intValue();
        }
        else
        {
            throw new IllegalStateException(String.format(
                "The value generated for the %s argument has an illegal type: %s.",
                name, data.getType()));
        }

        return intValue;
    }
}

final class TestBaseQueryCreator
{
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
                    "Error: The %s argument (type %s) is an operation with " +
                    "test situation %s. The current version does not support " +
                    "nesting of test situations.", prefix, p.getTypeName()));

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
                    throw new IllegalArgumentException(
                        "Illegal kind: " + arg.getKind());
                }
            }
        }
    }
}

/*
private void insertInitializingCalls(Argument argument, Data value) throws ConfigurationException
{
    final String argumentTypeName = argument.isImmediate() ?
        AddressingModeImm.NAME : ((Primitive) argument.getValue()).getName();
    
    System.out.printf(
        "Initializer: argument: %7s, mode: %10s, value: %s (%s) %n",
        argument.getName(),
        argumentTypeName,
        Integer.toHexString(value.getRawData().intValue()),
        value.getRawData().toBinString()
    );

    for(IInitializerGenerator ig : model.getInitializers())
    {
        if (ig.isCompatible(argument))
        {
            final List<ConcreteCall> calls = ig.createInitializingCode(argument, value);
            sequenceBuilder.addInitializingCalls(calls);
            return;
        }
    }

    System.out.println(
        String.format(
            "Error! Failed to find an initializer generator for argument %s (addressing mode: %s).",
             argument.getName(),
             argumentTypeName
        )
    );
}
*/
