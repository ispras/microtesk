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

import java.util.Map;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryBuilder;

public final class TestDataEngine
{
    private final IModel model;
    
    public TestDataEngine(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        this.model = model;
    }

    public TestResult generateData(Situation situation, Primitive primitive)
    {
        System.out.printf("Processing situation %s for %s...%n",
            situation, primitive.getSignature());

        final TestBaseQueryCreator queryCreator =
            new TestBaseQueryCreator(model.getName(), situation, primitive);

        final TestBaseQuery query = queryCreator.getQuery(); 
        System.out.println("Query to TestBase: " + query);

        return new TestResult(TestResult.Status.NODATA);
    }
}

final class TestBaseQueryCreator
{
    private final String processor;
    private final Situation situation;
    private final Primitive primitive;

    private boolean isCreated;
    private TestBaseQueryBuilder builder;
    private TestBaseQuery query;

    public TestBaseQueryCreator(
        String processor, Situation situation, Primitive primitive)
    {
        this.processor = processor;
        this.situation = situation;
        this.primitive = primitive;

        this.isCreated = false;
        this.builder   = null;
        this.query     = null;
    }

    public TestBaseQuery getQuery()
    {
        createQuery();

        if (null == query)
            throw new NullPointerException();

        return query;
    }

    private void createQuery()
    {
        if (isCreated)
            return;

        builder = new TestBaseQueryBuilder();

        createContext();
        createParameters();
        createBindings();

        query = builder.build();
        builder = null;

        isCreated = true;
    }

    private void createContext()
    {
        builder.setContextAttribute(TestBaseContext.PROCESSOR, processor);
        builder.setContextAttribute(TestBaseContext.INSTRUCTION, primitive.getName());
        builder.setContextAttribute(TestBaseContext.TESTCASE, situation.getName());

        for (Argument arg : primitive.getArguments().values())
        {
            builder.setContextAttribute(arg.getName(), arg.getTypeName());
        }
    }

    private void createParameters()
    {
        for (Map.Entry<String, Object> attrEntry : situation.getAttributes().entrySet())
        {
            builder.setParameter(attrEntry.getKey(), attrEntry.getValue().toString());
        }
    }

    private void createBindings()
    {
        // TODO Auto-generated method stub
        
    }
}


    /*

final TestSituation testSituation =
    testKnowledge.getSituation(p.getSituation(), p);

if (null == testSituation)
{
    System.out.printf(
        "Situation %s is not found.%n", situation.getName());
    return;
}

if (!testSituation.isApplicable(p))
{
    System.out.printf(
        "Situation %s is not applicable to %s %s.%n",
        situation.getName(), p.getKind().getText(), p.getName());
    return;
}

/*
for (Argument argument : p.getArguments().values())
{
    // TODO:
    // situation.setOutput(argument.getName());
}
*/
/*
final TestResult testResult = testSituation.solve();
if (TestResult.Status.OK == testResult.getStatus())
{
    // TODO
}

    }
}


/*

for (Argument argument : rootOperation.getArguments().values())
{
    System.out.print(" ");
    System.out.print(argument.isImmediate() ?
       AddressingModeImm.PARAM_NAME :
       ((Primitive) argument.getValue()).getName()); 
}

System.out.println(")");
*/

/*
final ISituation situation =
    instruction.createSituation(situationName);

// This is needed for situations like random that do not have a signature 
// and generate values for any parameters the client code might request.
// Other situations may ignore these calls.

for (Argument argument : rootOperation.getArguments().values())
    situation.setOutput(argument.getName());

Map<String, Data> output = null;

try 
{
    output = situation.solve();
}
catch (ConfigurationException e)
{
    System.out.printf("Warning! Failed to generate test data for the %s situation.\nReason: %s.\n",
        situationName, e.getMessage());

    return;
}

for (Map.Entry<String, Data> entry : output.entrySet())
{
    final Argument argument = rootOperation.getArguments().get(entry.getKey());

    if (null == argument)
    {
        System.out.printf("Argument %s is not defined for instruction %s.%n",
           entry.getKey(), rootOperation.getName());
        continue;
    }

    insertInitializingCalls(argument, entry.getValue());
}
*/

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
