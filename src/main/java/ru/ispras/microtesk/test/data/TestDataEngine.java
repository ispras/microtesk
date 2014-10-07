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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.test.preparator.Preparator;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownValue;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;

public final class TestDataEngine
{
    private final IModel model;
    private final TestBase testBase;

    public TestDataEngine(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        this.model = model;
        this.testBase = new TestBase();
    }

    public List<ConcreteCall> generateData(
        Situation situation, Primitive primitive)
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

        final TestBaseQueryResult queryResult = testBase.executeQuery(query);
        if (TestBaseQueryResult.Status.OK != queryResult.getStatus())
        {
            printErrors(queryResult);
            return Collections.emptyList();
        }

        final TestDataProvider dataProvider = queryResult.getDataProvider(); 
        if (!dataProvider.hasNext())
        {
            System.out.println("No data was generated for the query.");
            return Collections.emptyList();
        }

        final TestData testData = dataProvider.next();
        System.out.println(testData);

        // Set unknown immediate values
        for (Map.Entry<String, UnknownValue> e : unknownValues.entrySet())
        {
            final String name = e.getKey();
            final UnknownValue target = e.getValue();

            final Node value = testData.getBindings().get(name);
            final int intValue = FortressUtils.extractInt(value);

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

            final BitVector value =
                FortressUtils.extractBitVector(e.getValue());

            System.out.println("Value: " + value);

            final Preparator preparator = getPreparator(targetMode);
            if (null == preparator)
                System.out.printf(
                    "No suitable preparator is found for argument %s (%s).%n",
                    name, targetMode.getSignature());
        }

        return Collections.emptyList();
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

    private Preparator getPreparator(Primitive targetMode)
    {
        return null;
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
